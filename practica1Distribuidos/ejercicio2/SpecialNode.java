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
    private boolean sleep, error, fallNode;
    private InterceptMessage interceptMessage;

    public void init() {
        this.console = new Console("1.0");
        this.sleep = false;
        this.error = false;
        this.fallNode = false;
        try {
			GlobalFunctions.initFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
        this.interceptMessage = new InterceptMessage(this);
    }
    
    public MainRobot() {
        this.init();

        String cmd = this.console.getCommand();

        while(!cmd.equals("close")) {
            try{
                if(cmd.equals("rotate")) {
                    this.doConnect(this.index+1, 10);
                    if(this.error){
                        this.doDisconnect();
                        this.interceptMessage.getSocketLeft().close();
                        break;
                    }
                    if(!this.fallNode) {
                        if(!this.sleep) GlobalFunctions.doMoveRobot("ROTATE", this.index-1);
                        else this.console.writeMessage("The node is sleeping you cant do a rotate move");
                        this.osRight.writeObject(new ControlRequest("OP_ROTATE"));
                    }else {
                        this.osRight.writeObject(new ControlRequest("ERROR"));
                        
                        this.doDisconnect();
                        break;
                    }
                    this.doDisconnect();
                }else if(cmd.equals("translate")) {
                    this.doConnect(this.index+1, 10);
                    if(this.error) {
                        this.doDisconnect();
                        this.interceptMessage.getSocketLeft().close();
                        break;
                    }
                    if(!this.fallNode){
                        if(!this.sleep) GlobalFunctions.doMoveRobot("TRASLATE", this.index-1);
                        else this.console.writeMessage("The node is sleeping you cant do a traslate move");
                        this.osRight.writeObject(new ControlRequest("OP_TRASLATE"));
                    }else {
                        this.osRight.writeObject(new ControlRequest("ERROR"));
                        this.doDisconnect();
                        break;
                    }
                    this.doDisconnect();
                }else if(cmd.equals("stop")) {
                    this.doConnect(this.index+1, 10);
                    if(this.error) {
                        this.doDisconnect();
                        this.interceptMessage.getSocketLeft().close();
                        break;
                    }
                    if(!this.fallNode) {
                        int index = this.console.getCommandStopRobot();
                        if(index==this.index){ 
                            this.sleep=true;
                        }else{
                            ControlRequest cr = new ControlRequest("OP_STOP_ROBOT");
                            cr.getArgs().add(index);
                            this.osRight.writeObject(cr);
                        }
                    }else{
                        this.osRight.writeObject(new ControlRequest("ERROR"));
                        this.doDisconnect();
                        break;
                    }
                    this.doDisconnect();
                }
            }catch(Exception e) {
                System.out.println("Exception mainrobot: " + e.getMessage());
            }
            cmd = this.console.getCommand();
        }
    }

    public void doConnect(int indexNext, int max_nodes){
        try {
            if(this.socketRight==null){
                if(indexNext<=max_nodes){
                    this.socketRight = new Socket("localhost",GlobalFunctions.getExternalVariables("PORTROBOT"+(indexNext)));
                    this.osRight = new ObjectOutputStream(this.socketRight.getOutputStream());
                    this.isRight = new ObjectInputStream(this.socketRight.getInputStream());
                }else{
                    this.error = true;
                }
            }
        } catch (Exception e) {
            if(e.getMessage().equals("Connection refused: connect")) {
                this.fallNode = true;
                this.doConnect(indexNext++, max_nodes);
            }else System.out.println("MainRobot (doConnect)"+e.getMessage());
        }
    }

    public void doDisconnect(){
        try {
            if(this.socketRight!=null){
                this.osRight.close();
                this.osRight = null;
                this.isRight.close();
                this.isRight = null;
                this.socketRight.close();
                this.socketRight = null;
            }
        } catch (Exception e) {
            System.out.println("Exception doDisconnect: " + e.getMessage());
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
                }else if(crs.getSubtype().equals("OP_TRASLATION_OK")) {
                    this.mainRobot.getConsole().writeMessage(crs.getArgs().get(0).toString());
                }else if(crs.getSubtype().equals("OP_STOP_ROBOT_OK")){
                    this.mainRobot.getConsole().writeMessage(crs.getArgs().get(0).toString());
                }else if(crs.getSubtype().equals("ERROR")) {
                    this.mainRobot.getConsole().writeMessage("Type close...");
                    this.mainRobot.doConnect(2, 10);
                    this.mainRobot.getOsRight().writeObject(new ControlRequest("ERROR"));
                    this.mainRobot.doDisconnect();
                    break;
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
    
    public Socket getSocketLeft() {
		return socketLeft;
	}

	public void setSocketLeft(Socket socketLeft) {
		this.socketLeft = socketLeft;
	}

	public ObjectInputStream getIsLeft() {
		return isLeft;
	}

	public void setIsLeft(ObjectInputStream isLeft) {
		this.isLeft = isLeft;
	}

	public ObjectOutputStream getOsLeft() {
		return osLeft;
	}

	public void setOsLeft(ObjectOutputStream osLeft) {
		this.osLeft = osLeft;
	}

	public MainRobot getMainRobot() {
		return mainRobot;
	}

	public void setMainRobot(MainRobot mainRobot) {
		this.mainRobot = mainRobot;
	}
}
