package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import PracticasDistribuidos.practica1Distribuidos.protocol.*;
import java.net.*;
import java.io.*;

class NormalNode9{
    public static void main(String[] args) {
        try{
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getExternalVariables("PORTROBOT10"));

            while(true) {
                System.out.println("Waiting robot9...");
                Socket socket = listenSocket.accept();
                System.out.println("Accepted connection from: " + socket.getInetAddress().toString());
                
                new Connection2(socket);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}

class Connection2 extends Thread{
	private int index = 10;
    private Socket robotLeft, robotRight;
    private ObjectOutputStream osLeft, osRight;
    private ObjectInputStream isLeft, isRight;

    public Connection2(Socket socket) {
        try{
            if(this.robotLeft==null) {
                this.robotLeft = socket;
                this.osLeft = new ObjectOutputStream(this.robotLeft.getOutputStream());
                this.isLeft = new ObjectInputStream(this.robotLeft.getInputStream());
            }
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
        this.start();
    }

    @Override
    public void run(){
        try{
            Request r = (Request) this.isLeft.readObject();
            if(r.getType().equals("CONTROL_REQUEST")){
                ControlRequest cr = (ControlRequest) r;
                if(cr.getSubtype().equals("OP_ROTATE")) {
                    this.doConnectionRight();
                    ControlResponse crs = new ControlResponse("OP_ROTATE_OK");
                    crs.getArgs().add("All the rotations where done successfully");
                    this.osRight.writeObject(crs);
                    this.doDisconnectRight();
                }else if(cr.getSubtype().equals("OP_TRANSLATE")){
                    this.doConnectionRight();
                    ControlResponse crs = new ControlResponse("OP_TRANSLATION_OK");
                    crs.getArgs().add("All the translations where done successfully");
                    this.osRight.writeObject(crs);
                    this.doDisconnectRight();
                }else if(cr.getSubtype().equals("OP_STOP_ROBOT")) {
                    if(this.index == (int) cr.getArgs().get(0)) {
                    	System.out.println("i´m going to mimir,xoxo");
                    	this.doConnectionRight();
                    	ControlResponse crs = new ControlResponse("OP_STOP_ROBOT_OK");
                    	crs.getArgs().add("The node went to sleep correctly");
                    	this.osRight.writeObject(crs);
                    	this.doDisconnectRight();
                    }else {
                    	this.doConnectionRight();
                    	ControlResponse crs = new ControlResponse("OP_STOP_ROBOT_OK");
                    	crs.getArgs().add("There was an error sleeping the node");
                    	this.osRight.writeObject(crs);
                    	this.doDisconnectRight();
                    }
                }else if(cr.getSubtype().equals("OP_STOP_ROBOT_OK")) {
                	this.doConnectionRight();
                	ControlResponse crs = new ControlResponse("OP_STOP_ROBOT_OK");
                	crs.getArgs().add("The node went to sleep correctly");
                	this.osRight.writeObject(crs);
                	this.doDisconnectRight();
                }
            }
        }catch(ClassNotFoundException e) {
			System.out.println("ClassNotFoundException(run): " + e.getMessage());
		}catch(IOException e) {
			System.out.println("Readline(run): " + e.getMessage());
		}
    }

    public void doConnectionRight(){
        try {
            if(this.robotRight==null){
                this.robotRight = new Socket("localhost",GlobalFunctions.getExternalVariables("PORTROBOT1"));
                this.osRight = new ObjectOutputStream(this.robotRight.getOutputStream());
                this.isRight = new ObjectInputStream(this.robotRight.getInputStream());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void doDisconnectRight(){
        try {
            if(this.robotRight!=null){
                this.isRight.close();
                this.isRight = null;
                this.osRight.close();
                this.osRight = null;
                this.robotRight.close();
                this.robotRight = null;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}