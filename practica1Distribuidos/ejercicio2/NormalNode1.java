package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import PracticasDistribuidos.practica1Distribuidos.protocol.*;
import java.net.*;
import java.io.*;

class NormalNode1{
    public static void main(String[] args) {
        try{
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getExternalVariables("PORTROBOT2"));

            while(true) {
                System.out.println("Waiting robot1...");
                Socket socket = listenSocket.accept();
                System.out.println("Accepted connection from: " + socket.getInetAddress().toString());
                
                new Connection(socket, 2);
            }
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

    public Connection(Socket socket, int index) {
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
                    this.osRight.writeObject(cr);
                    this.doDisconnectRight();
                }else if(cr.getSubtype().equals("OP_TRANSLATE")){
                    this.doConnectionRight();
                    this.osRight.writeObject(cr);
                    this.doDisconnectRight();
                }else if(cr.getSubtype().equals("OP_STOP_ROBOT")) {
                	System.out.println((int) cr.getArgs().get(0));
                    if(this.index != (int) cr.getArgs().get(0)){
                        this.doConnectionRight();
                        this.osRight.writeObject(cr);
                        this.doDisconnectRight();
                    }else{
                        System.out.println("i´m going to mimir,xoxo");
                        this.doConnectionRight();
                        this.osRight.writeObject(new ControlRequest("OP_STOP_ROBOT_OK"));
                        this.doDisconnectRight();
                    }
                }else if(cr.getSubtype().equals("OP_STOP_ROBOT_OK")) {
                	this.doConnectionRight();
                	this.osRight.writeObject(cr);
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
                this.robotRight = new Socket("localhost",GlobalFunctions.getExternalVariables("PORTROBOT"+(this.index+1)));
                this.osRight = new ObjectOutputStream(this.robotRight.getOutputStream());
                this.isRight = new ObjectInputStream(this.robotRight.getInputStream());
            }
        } catch (Exception e) {
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