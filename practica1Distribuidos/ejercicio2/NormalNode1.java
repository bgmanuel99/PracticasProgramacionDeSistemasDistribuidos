package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import PracticasDistribuidos.practica1Distribuidos.protocol.*;
import java.net.*;
import java.io.*;

class NormalNode1{
    public static void main(String[] args) {
        new Listen(2);
    }
}

class Listen {

    public boolean keep = true;

    public Listen(int index){
        try{
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getExternalVariables("PORTROBOT"+index));

            while(true) {
                if(!keep) break;
                System.out.println("Waiting robot" + (index-1)+ "...");
                Socket socket = listenSocket.accept();
                System.out.println("Accepted connection from: " + socket.getLocalAddress().toString());
                
                new Connection(socket, index, this);
            }
            System.out.println("Close Robot1");
        }catch (Exception e){
            System.out.println("Exception main: " + e.getMessage());
        }
    }
}

class Connection extends Thread{
    private int index;
    private Socket robotLeft, robotRight;
    private ObjectOutputStream osLeft, osRight;
    private ObjectInputStream isLeft, isRight;
    private boolean error, fallNode;
    private Listen listen;

    public Connection(Socket socket, int index, Listen listen) {
        try{
            if(this.robotLeft==null) {
                this.robotLeft = socket;
                this.osLeft = new ObjectOutputStream(this.robotLeft.getOutputStream());
                this.isLeft = new ObjectInputStream(this.robotLeft.getInputStream());
            }
        }catch(Exception e) {
            System.out.println("Connection constructor: " + e.getMessage());
        }
        this.index = index;
        this.listen = listen;
        this.start();
    }

    @Override
    public void run(){
        try{
            Request r = (Request) this.isLeft.readObject();
            if(r.getType().equals("CONTROL_REQUEST")){
                ControlRequest cr = (ControlRequest) r;
                if(cr.getSubtype().equals("OP_ROTATE")) {
                    this.doConnectionRight(this.index+1, 10-this.index);
                    if(!GlobalFunctions.isSleeping(this.index-1)) GlobalFunctions.doMoveRobot("ROTATE", this.index-1);
                    else System.out.println("The node is sleeping you cant do a rotate move");
                    this.osRight.writeObject(cr);
                    this.doDisconnectRight();
                }else if(cr.getSubtype().equals("OP_TRASLATE")){
                    this.doConnectionRight(this.index+1, 10-this.index);
                    if(!GlobalFunctions.isSleeping(this.index-1)) GlobalFunctions.doMoveRobot("TRASLATE", this.index-1);
                    else System.out.println("The node is sleeping you cant do a traslate move");
                    this.osRight.writeObject(cr);
                    this.doDisconnectRight();
                }else if(cr.getSubtype().equals("OP_STOP_ROBOT")) {
                	System.out.println((int) cr.getArgs().get(0));
                    if(this.index != (int) cr.getArgs().get(0)){
                        this.doConnectionRight(this.index+1, 10-this.index);
                        this.osRight.writeObject(cr);
                        this.doDisconnectRight();
                    }else{
                        System.out.println("i´m going to mimir,xoxo");
                        this.doConnectionRight(this.index+1, 10-this.index);
                        GlobalFunctions.setSleeping(this.index-1);
                        this.osRight.writeObject(new ControlRequest("OP_STOP_ROBOT_OK"));
                        this.doDisconnectRight();
                    }
                }else if(cr.getSubtype().equals("OP_STOP_ROBOT_OK")) {
                	this.doConnectionRight(this.index+1, 10-this.index);
                	this.osRight.writeObject(cr);
                	this.doDisconnectRight();
                }else if(cr.getSubtype().equals("ERROR")){
                    this.doConnectionRight(this.index+1, 10-this.index);
                    if(this.error) {
                        this.doDisconnectRight();
                    }
                    this.listen.keep=false;
                }
            }
        }catch(ClassNotFoundException e) {
			System.out.println("ClassNotFoundException(run): " + e.getMessage());
		}catch(IOException e) {
			System.out.println("Readline(run): " + e.getMessage());
		}catch(Exception e) {
            System.out.println("Exception(run): " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void doConnectionRight(int indexNext, int max_nd) {
        try {
            if(this.robotRight==null){
                if(indexNext<=max_nd){
                    this.robotRight = new Socket("localhost",GlobalFunctions.getExternalVariables("PORTROBOT"+(this.index+1)));
                    this.osRight = new ObjectOutputStream(this.robotRight.getOutputStream());
                    this.isRight = new ObjectInputStream(this.robotRight.getInputStream());
                }else{
                    this.error = true;
                }
            }
        }catch (Exception e) {
            if(e.getMessage().equals("Connection refused: connect")) {
                this.fallNode = true;
                this.doConnectionRight(indexNext++, max_nd);
            }
            System.out.println("Exception doConnectionRight: " + e.getMessage());
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
            System.out.println("Exception doDisconnectRight: " + e.getMessage());
            e.printStackTrace();
        }
    }
}