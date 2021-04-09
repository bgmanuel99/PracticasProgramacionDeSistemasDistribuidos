package PracticasDistribuidos.practica1Distribuidos.ejercicio1;

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
                new ConnectionCentral(socket, 1);
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
    private int centralNumber;

	public ConnectionCentral(Socket socket, int centralNumber){
        try {
        	this.centralNumber = centralNumber;
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
                    if(cr.getSubtype().equals("OP_MAP")) {
                        this.doMap((byte []) cr.getArgs().get(0));
                    }else if(cr.getSubtype().equals("OP_MESSAGE")) {
                    	if((GlobalFunctions.getSocket(GlobalFunctions.decrypt((byte []) cr.getArgs().get(2))) != null) &&
                    			(GlobalFunctions.getSocket(GlobalFunctions.decrypt((byte []) cr.getArgs().get(2))).getInetAddress().getHostAddress().equals(
                    			this.socket.getInetAddress().getHostAddress()))) {
                    		this.doMessage((byte []) cr.getArgs().get(0), (byte []) cr.getArgs().get(1), (byte []) cr.getArgs().get(2));
                            if(error) {
                                ControlResponse crs = new ControlResponse("MESSAGE_NOK");
                                crs.getArgs().add("Your message has not been received");
                                this.os.writeObject(crs);
                            }
                    	}else throw new Exception("This address is not verified, it migth be an attack.");
                    }else if(cr.getSubtype().equals("OP_BROADCASTING")) {
                    	if((GlobalFunctions.getSocket(cr.getArgs().get(1).toString()) != null) &&
                    			(GlobalFunctions.getSocket(cr.getArgs().get(1).toString()).getInetAddress().getHostAddress().equals(
                    			this.socket.getInetAddress().getHostAddress()))) {
	                        this.doBroadcasting(cr.getArgs());
	                        ControlResponse crs =new ControlResponse("BROADCASTING_OK");
	                        crs.getArgs().add("The broadcast was correct");
	                        this.os.writeObject(crs);
                    	}else throw new Exception("This address is not verified, it migth be an attack.");
                    }else if(cr.getSubtype().equals("OP_LOGOUT")) {
                    	GlobalFunctions.deleteUser(GlobalFunctions.decrypt((byte []) cr.getArgs().get(0)));
                    }else if(cr.getSubtype().equals("OP_CLOSE")) {
                    	this.doDisconnect();
                    	break;
                    }
                }
            }catch(ClassNotFoundException e) {
    			System.out.println("ClassNotFoundException run function: " + e.getMessage());
    		}catch(IOException e) {
    			System.out.println("IOException run function: " + e.getMessage());
    			break;
    		}catch(Exception e) {
    			System.out.println("Exception run functions: " + e.getMessage());
    		}
        }
    }

    private void doMap(byte [] user) {
        try{
            GlobalFunctions.insertPair(GlobalFunctions.decrypt(user), this.os, this.socket);
        }catch(IOException e) {
            System.out.println("IOException (doMap): " + e.getMessage());
        }catch(Exception e) {
            System.out.println("Exception (doMap): " + e.getMessage());
        }
    }

    private void doMessage(byte [] message, byte [] user, byte [] from) {
        try{
            ObjectOutputStream osClient = GlobalFunctions.getOs(GlobalFunctions.decrypt(user));
            
            ControlResponse cr = new ControlResponse("OP_MESSAGE");
            cr.getArgs().add(message);
            cr.getArgs().add(from);
            
            osClient.writeObject(cr);
        }catch(IOException e) {
            System.out.println("IOException (doMessage) Centralserver number" + this.centralNumber + ": " + e.getMessage());
            this.error = true;
        }catch(Exception e) {
            System.out.println("Exception (doMessage) Centralserver number" + this.centralNumber + ": " + e.getMessage());
            this.error = true;
        }
    }

    private void doBroadcasting(ArrayList users) {
        try{
            String [] user = (String []) users.get(0);
            
            String nick = users.get(1).toString();
            
            ObjectOutputStream [] contactOs= GlobalFunctions.getOsContacts(user);
            
            for(int i = 0; i < user.length; i++) new Broadcast(nick, contactOs[i]);
        }catch(Exception e) {
            System.out.println("Exception (doBroadcasting): " + e.getMessage());
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
    
    public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public int getCentralNumber() {
		return centralNumber;
	}

	public void setCentralNumber(int centralNumber) {
		this.centralNumber = centralNumber;
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

    class Broadcast extends Thread {
        private ObjectOutputStream osClient;
        private String nick;

        public Broadcast(String nick, ObjectOutputStream os) {
			this.osClient = os;
			this.nick = nick;
			this.start();
        }

        @Override
        public void run() {
            try{
            	ControlResponse crs = new ControlResponse("OP_BROADCASTING");
            	crs.getArgs().add("Se ha conectado: " + this.nick);
                this.osClient.writeObject(crs);
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