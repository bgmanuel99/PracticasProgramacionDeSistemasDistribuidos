package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import PracticasDistribuidos.practica1Distribuidos.protocol.*;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class CentralServer {
    public static void main(String [] args) {
        try{
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getExternalVariables("PORTCENTER1"));
            
            while(true) {
                System.out.println("Waiting central server 1...");
                Socket socket = listenSocket.accept();
                System.out.println("Accepted conexion from: " + socket.getInetAddress().toString());

                new ConnectionCentral(socket);
            }
        }catch(IOException e) { 
            System.out.println("Listen socket: "+ e.getMessage());
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

class  ConnectionCentral extends Thread{
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Socket socket;
    private boolean done, error;

    public ConnectionCentral(Socket socket){
        try {
            this.socket = socket;
            this.os = new ObjectOutputStream(this.socket.getOutputStream());
            this.is = new ObjectInputStream(this.socket.getInputStream());
            this.done = false;
            this.error = false;
            this.start();
        } catch (Exception e) {
            System.out.println("Exception (ConnectionCentral): " + e.getMessage());
        }
    }

    @Override
    public void run() {
        while(true) {
        	try{
                Request r = (Request) this.is.readObject();
                if(r.getType().equals("CONTROL_REQUEST")) {
                    ControlRequest cr = (ControlRequest) r;
                    System.out.println(cr.getSubtype());
                    if(cr.getSubtype().equals("OP_MAP")) {
                        this.doMap((byte []) cr.getArgs().get(0));
                    }else if(cr.getSubtype().equals("OP_MESSAGE")) {
                        this.doMessage((byte []) cr.getArgs().get(0), (byte []) cr.getArgs().get(1),(byte []) cr.getArgs().get(2));
                        if(error) {
                            ControlResponse crs = new ControlResponse("MESSAGE_NOK");
                            crs.getArgs().add("Your message has not been received");
                            this.os.writeObject(crs);
                        }
                    }else if(cr.getSubtype().equals("OP_BROADCASTING")) {
                        this.doBroadcasting(cr.getArgs());
                        ControlResponse crs =new ControlResponse("BROADCASTING_OK");
                        crs.getArgs().add("The broadcast was correct");
                        this.os.writeObject(crs);
                    }else if(cr.getSubtype().equals("OP_LOGOUT")) {
                        this.doDisconnect();
                    }
                }
            }catch(ClassNotFoundException e) {
    			System.out.println("ClassNotFoundException: " + e.getMessage());
    		}catch(IOException e) {
    			System.out.println("Readline run function: " + e.getMessage());
    			break;
    		}
        }
    }

    private void doMap(byte [] user) {
        try{
            GlobalFunctions.insertUser(GlobalFunctions.decrypt(user), this.socket);
            GlobalFunctions.insertOs(GlobalFunctions.decrypt(user), this.os);
           
        }catch(IOException e) {
            System.out.println("IOException (doMap): " + e.getMessage());
        }catch(Exception e) {
            System.out.println("Exception (doMap): " + e.getMessage());
        }
    }

    private void doMessage(byte [] message, byte [] user, byte [] from) {
        try{
        	System.out.println("1");
            Socket socket = GlobalFunctions.getSocket(GlobalFunctions.decrypt(user));
            System.out.println("2");
            ObjectOutputStream osClient = GlobalFunctions.getOs(GlobalFunctions.decrypt(user));
            System.out.println("3");
            ControlResponse cr = new ControlResponse("OP_MESSAGE");
            cr.getArgs().add(message);
            cr.getArgs().add(from);

            osClient.writeObject(cr);
            
            //Thread inactiveClient = new Thread(new InactiveClient(this, socket, osClient));
            //inactiveClient.start();
            
            
        }catch(IOException e) {
            System.out.println("IOException (doMessage): " + e.getMessage());
            this.error = true;
        }catch(Exception e) {
            System.out.println("Exception (doMessage): " + e.getMessage());
            e.printStackTrace();
            this.error = true;
        }
    }

    private void doBroadcasting(ArrayList users) {
        try{
            String [] user = (String []) users.get(0);

            String nick = users.get(1).toString();

            Socket [] contactSocket = GlobalFunctions.getContacts(user);
            ObjectOutputStream [] contactOs= GlobalFunctions.getOsContacts(user);
            
            for(int i = 0; i < user.length; i++) {
            	System.out.println(user[i]);
            	ControlResponse crs = new ControlResponse("OP_BROADCASTING");
            	crs.getArgs().add("New user online "+nick);
                contactOs[i].writeObject(crs);
            }
        }catch(Exception e) {
            System.out.println("Exception (doBroadcasting): " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ObjectOutputStream getOs() {
        return os;
    }

    public void setOs(ObjectOutputStream os) {
        this.os = os;
    }

    public ObjectInputStream getIs() {
        return is;
    }

    public void setIs(ObjectInputStream is) {
        this.is = is;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    private void doDisconnect() {
        if(this.socket != null) {
            try{
                this.os.close();
                this.os = null;
                this.is.close();
                this.is = null;
                this.socket.close();
                this.socket = null;
            } catch (Exception e) {
                System.out.println("Exception (doDisconnect): " + e.getMessage());
            }
        }
    }

    public void doDisconnect(Socket socketClient, ObjectOutputStream osClient){
        if(socketClient !=null){
            try {
                osClient.close();
                osClient = null;
                socketClient.close();
                socketClient = null;
                this.error = false;
            } catch (Exception e) {
                System.out.println("Exception (doDisconnect): " + e.getMessage());
            }
        }
    }

    class Broadcast implements Runnable {
        private ObjectOutputStream osClient;
        private String nick;

        public Broadcast( String nick, ObjectOutputStream os) {
			this.osClient = os;
			this.nick = nick;
			start();
        }

        @Override
        public void run() {
            try{
            	System.out.println("thread");
                this.osClient.writeObject(new ControlResponse("OP_BROADCASTING").getArgs().add("Se ha conectado: "+this.nick));
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    class InactiveClient implements Runnable{
        private ConnectionCentral connectionCentral;
        private Socket socketClient;
        private ObjectOutputStream osClient;

        public InactiveClient(ConnectionCentral connectionCentral, Socket socketClient, ObjectOutputStream osClient){
            this.connectionCentral = connectionCentral;
            this.socketClient = socketClient;
            this.osClient = osClient;
            
        }

        @Override
        public void run() {
            long sleep = 300;
            try {
                Thread.sleep(sleep);

                if(!this.connectionCentral.done) this.connectionCentral.doDisconnect(socketClient, osClient);
            } catch(InterruptedException e){
                System.out.println("Interrupted exception: " + e.getMessage());
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }
}