package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import PracticasDistribuidos.practica1Distribuidos.protocol.*;
import java.net.*;
import java.io.*;

public class SpecialNode {
    public static void main(String[] args) {
        new MainRobot();
    }
}

class MainRobot extends Thread{
    private Socket socketRight;
    private ObjectInputStream isRight;
    private ObjectOutputStream osRight;
    private int index = 1;
    private Console console;

    public void init() {
        this.console = new Console("1.0");
        new InterceptMessage(this);
    }
    
    public MainRobot() {
        this.init();

        String cmd = this.console.getCommand();

        while(!cmd.equals("close")) {
            try{
                if(cmd.equals("rotate")) {
                    this.doConnect();
                    this.osRight.writeObject(new ControlRequest("OP_ROTATE"));
                    this.doDisconnect();
                }else if(cmd.equals("translate")) {
                    this.doConnect();
                    this.osRight.writeObject(new ControlRequest("OP_TRANSLATE"));
                    this.doDisconnect();
                }else if(cmd.equals("stop")) {
                    int index = this.console.getCommandStopRobot();
                    this.doConnect();
                    ControlRequest cr = new ControlRequest("OP_STOP_ROBOT");
                    cr.getArgs().add(index);
                    this.osRight.writeObject(cr);
                    this.doDisconnect();
                }
            }catch(Exception e) {
                System.out.println(e.getMessage());
            }
            cmd = this.console.getCommand();
        }
    }

    private void doConnect(){
        try {
            if(this.socketRight==null){
                this.socketRight = new Socket("localhost",GlobalFunctions.getExternalVariables("PORTROBOT"+(this.index+1)));
                this.osRight = new ObjectOutputStream(this.socketRight.getOutputStream());
                this.isRight = new ObjectInputStream(this.socketRight.getInputStream());
            }
        } catch (Exception e) {
            System.out.println("MainRobot (doConnect)"+e.getMessage());
        }
    }

    private void doDisconnect(){
        try {
            if(this.socketRight!=null){
                this.osRight.close();
                this.osRight = null;
                this.isRight.close();
                this.socketRight.close();
                this.socketRight = null;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Console getConsole() {
        return this.console;
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    public Socket getSocketRight() {
        return this.socketRight;
    }

    public void setSocketRight(Socket socketRight) {
        this.socketRight = socketRight;
    }

    public ObjectInputStream getIsRight() {
        return this.isRight;
    }

    public void setIsRight(ObjectInputStream isRight) {
        this.isRight = isRight;
    }

    public ObjectOutputStream getOsRight() {
        return this.osRight;
    }

    public void setOsRight(ObjectOutputStream osRight) {
        this.osRight = osRight;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    
}

class InterceptMessage extends Thread{
    private int index = 1;
    private Socket socketLeft;
    private ObjectInputStream isLeft;
    private ObjectOutputStream osLeft;
    private MainRobot mainRobot;
    
    public InterceptMessage(MainRobot mainRobot) {
        this.mainRobot = mainRobot;
        this.start();
    }
    
    @Override
    public void run() {
    	try{
    		ServerSocket listen = new ServerSocket(GlobalFunctions.getExternalVariables("PORTROBOT1"));
    		while(true){
                System.out.println("Waiting special node...");
                this.socketLeft = listen.accept();
                this.osLeft = new ObjectOutputStream(this.socketLeft.getOutputStream());
                this.isLeft = new ObjectInputStream(this.socketLeft.getInputStream());
                ControlResponse crs = (ControlResponse) this.isLeft.readObject();
                if(crs.getSubtype().equals("OP_ROTATE_OK")){
                    this.mainRobot.getConsole().writeMessage(crs.getArgs().get(0).toString());
                }else if(crs.getSubtype().equals("OP_TRANSLATION_OK")) {
                    this.mainRobot.getConsole().writeMessage(crs.getArgs().get(0).toString());
                }else if(crs.getSubtype().equals("OP_STOP_ROBOT_OK")){
                    this.mainRobot.getConsole().writeMessage(crs.getArgs().get(0).toString());
                }
                if(this.socketLeft!=null){
                    this.isLeft.close();
                    this.isLeft = null;
                    this.osLeft.close();
                    this.osLeft = null;
                    this.socketLeft.close();
                    this.socketLeft=null;
                }
    		}
    	}catch(Exception e) {
            System.out.println("InterceptMessage (run) " + e.getMessage());
        }
    }
}
