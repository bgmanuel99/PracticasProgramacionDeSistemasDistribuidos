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
        try{
            Request r = (Request) this.is.readObject();
            if(r.getType().equals("CONTROL_REQUEST")) {
                ControlRequest cr = (ControlRequest) r;
                if(cr.getSubtype().equals("OP_MAP")) {
                    this.doMap((byte []) cr.getArgs().get(0));
                }else if(cr.getSubtype().equals("OP_MESSAGE")) {
                    this.doMessage((byte []) cr.getArgs().get(0), (byte []) cr.getArgs().get(1));
                    if(error) this.os.writeObject(new ControlResponse("MESSAGE_NOK").getArgs().add("Your message has not been received"));
                }else if(cr.getSubtype().equals("OP_BROADCASTING")) {
                    this.doBroadcasting(cr.getArgs());
                    this.os.writeObject(new ControlResponse("BROADCASTING_OK").getArgs().add("The broadcast was correct"));
                }else if(cr.getSubtype().equals("OP_LOGOUT")) {
                    this.doDisconnect();
                }
            }
        }catch(ClassNotFoundException e) {
			System.out.println("ClassNotFoundException: " + e.getMessage());
		}catch(IOException e) {
			System.out.println("Readline run function: " + e.getMessage());
		}
    }

    private void doMap(byte [] user) {
        try{
            GlobalFunctions.insertUser(GlobalFunctions.decrypt(user), this.socket);
        }catch(IOException e) {
            System.out.println("IOException (doMap): " + e.getMessage());
        }catch(Exception e) {
            System.out.println("Exception (doMap): " + e.getMessage());
        }
    }

    private void doMessage(byte [] message, byte [] user) {
        try{
            Socket socket = GlobalFunctions.getSocket(GlobalFunctions.decrypt(user));
            ObjectOutputStream osClient = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream isClient = new ObjectInputStream(socket.getInputStream());

            ControlResponse cr = new ControlResponse("OP_MESSAGE");
            cr.getArgs().add(message);
            cr.getArgs().add(user);

            osClient.writeObject(cr);
            
            Thread inactiveClient = new Thread(new InactiveClient(this, socket, osClient, isClient));
            inactiveClient.start();
            
            ControlResponse crs = (ControlResponse) isClient.readObject();

            this.os.writeObject(crs);
        }catch(IOException e) {
            System.out.println("IOException (doMap): " + e.getMessage());
            this.error = true;
        }catch(Exception e) {
            System.out.println("Exception (doMap): " + e.getMessage());
            this.error = true;
        }
    }

    private void doBroadcasting(ArrayList users) {
        try{
            byte [][] encryptContacts = new byte [users.size()][];
            for(int i = 0; i < users.size(); i++){
                encryptContacts[i] = (byte []) users.get(i);
            }

            String [] contacts = new String [encryptContacts.length];
            for(int i = 0; i < encryptContacts.length-1; i++) {
                contacts[i] = GlobalFunctions.decrypt(encryptContacts[i]);
            }

            String nick = GlobalFunctions.decrypt(encryptContacts[encryptContacts.length-1]);

            Socket [] contactSocket = GlobalFunctions.getContacts(contacts);
            for(Socket socket : contactSocket) {
                Thread broadcast = new Thread(new Broadcast(socket, nick));
                broadcast.start();
            }
        }catch(IOException e) {
            System.out.println("IOException (doMap): " + e.getMessage());
        }catch(Exception e) {
            System.out.println("Exception (doMap): " + e.getMessage());
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

    public void doDisconnect(Socket socketClient, ObjectOutputStream osClient, ObjectInputStream isClient){
        if(socketClient !=null){
            try {
                osClient.close();
                osClient = null;
                isClient.close();
                isClient = null;
                socketClient.close();
                socketClient = null;
                this.error = false;
            } catch (Exception e) {
                System.out.println("Exception (doDisconnect): " + e.getMessage());
            }
        }
    }

    class Broadcast implements Runnable {
        private Socket clientSocket;
        private ObjectInputStream isClient;
        private ObjectOutputStream osClient;
        private String nick;

        public Broadcast(Socket clientSocket, String nick) {
            try{
                this.clientSocket = clientSocket;
                this.isClient = new ObjectInputStream(this.clientSocket.getInputStream());
                this.osClient = new ObjectOutputStream(this.clientSocket.getOutputStream());
                this.nick = nick;
            }catch(IOException e){
                System.out.println("IOException (Broadcast): " +e.getMessage());
            }
        }

        @Override
        public void run() {
            try{
                this.osClient.writeObject(new ControlResponse("OP_BROADCASTING").getArgs().add(this.nick));
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    class InactiveClient implements Runnable{
        private ConnectionCentral connectionCentral;
        private Socket socketClient;
        private ObjectInputStream isClient;
        private ObjectOutputStream osClient;

        public InactiveClient(ConnectionCentral connectionCentral, Socket socketClient, ObjectOutputStream osClient, ObjectInputStream isClient){
            this.connectionCentral = connectionCentral;
            this.socketClient = socketClient;
            this.osClient = osClient;
            this.isClient = isClient;
        }

        @Override
        public void run() {
            long sleep = 300;
            try {
                Thread.sleep(sleep);

                if(!this.connectionCentral.done) this.connectionCentral.doDisconnect(socketClient, osClient, isClient);
            } catch(InterruptedException e){
                System.out.println("Interrupted exception: " + e.getMessage());
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }
}