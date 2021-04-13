package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import PracticasDistribuidos.practica1Distribuidos.protocol.*;
import java.net.*;
import java.io.*;

class NormalNode9{
    public static void main(String[] args) {
        new Listen2();
    }
}

class Listen2{
    
    public boolean keep = true;
    public ServerSocket listenSocket;

    public Listen2(){
        try{
            this.listenSocket = new ServerSocket(GlobalFunctions.getExternalVariables("PORTROBOT10"));

            do {
            	if(!this.keep) break;
                System.out.println("Waiting robot9...");
                Socket socket = this.listenSocket.accept();
                System.out.println("Accepted connection from: " + socket.getInetAddress().toString());
                
                new Connection2(socket, this);
			} while (keep);
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
    private Listen2 listen2;

    public Connection2(Socket socket, Listen2 listen2) {
        try{

            if(this.robotLeft==null) {
                this.robotLeft = socket;
                this.osLeft = new ObjectOutputStream(this.robotLeft.getOutputStream());
                this.isLeft = new ObjectInputStream(this.robotLeft.getInputStream());
            }
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
        this.listen2 = listen2;
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
                    if(!GlobalFunctions.isSleeping(this.index-1)) GlobalFunctions.doMoveRobot("ROTATE", this.index-1);
                    else System.out.println("The node is sleeping you cant do a rotate move");
                    ControlResponse crs = new ControlResponse("OP_ROTATE_OK");
                    crs.getArgs().add("All the rotations where done successfully");
                    this.osRight.writeObject(crs);
                    this.doDisconnectRight();
                }else if(cr.getSubtype().equals("OP_TRASLATE")){
                    this.doConnectionRight();
                    if(!GlobalFunctions.isSleeping(this.index-1)) GlobalFunctions.doMoveRobot("TRASLATE", this.index-1);
                    else System.out.println("The node is sleeping you cant do a traslate move");
                    ControlResponse crs = new ControlResponse("OP_TRASLATION_OK");
                    crs.getArgs().add("All the translations where done successfully");
                    this.osRight.writeObject(crs);
                    this.doDisconnectRight();
                }else if(cr.getSubtype().equals("OP_STOP_ROBOT")) {
                    if(this.index == (int) cr.getArgs().get(0)) {
                    	System.out.println("i´m going to mimir,xoxo");
                    	this.doConnectionRight();
                    	GlobalFunctions.setSleeping(this.index-1);
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
                }else if(cr.getSubtype().equals("ERROR")){
                	this.doConnectionRight();
                	this.osRight.writeObject(new ControlResponse("ERROR"));
                    this.doDisconnectRight();
                    this.listen2.keep = false;
                    this.listen2.listenSocket.close();
                }
            }
        }catch(ClassNotFoundException e) {
			System.out.println("ClassNotFoundException(run): " + e.getMessage());
		}catch(IOException e) {
			System.out.println("Readline(run): " + e.getMessage());
		}catch(Exception e) {
            System.out.println("Exception(run): " + e.getMessage());
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