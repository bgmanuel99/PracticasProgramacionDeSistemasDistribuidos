package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import PracticasDistribuidos.practica1Distribuidos.protocol.*;

import java.net.*;
import java.io.*;
import java.util.Random;

public class Server2 {
    public static void main(String[] args) {
        try {
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getExternalVariables("PORTSERVER2"));
            
            while(true){
            	System.out.println("Waiting server 2...");
                Socket socket = listenSocket.accept();
                System.out.println("Accepted conexion from: " + socket.getInetAddress().toString());
                   
                new ConnectionServer2(socket);
            }
        } catch(IOException e) { 
            System.out.println("Listen socket: "+ e.getMessage());
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
}
class ConnectionServer2 extends Thread{
    private ObjectOutputStream osProxy;
    private ObjectInputStream isProxy;
    private Socket proxySocket;

    public ConnectionServer2(Socket proxySocket){
        try {
            this.proxySocket = proxySocket;
            this.osProxy = new ObjectOutputStream(this.proxySocket.getOutputStream());
            this.isProxy = new ObjectInputStream(this.proxySocket.getInputStream());
            this.start();
        }catch(IOException e) {
            System.out.println("Connection: " + e.getMessage());
        }
    }

    public void run() {
        try {
        	Request r = (Request) this.isProxy.readObject();

            if(r.getType().equals("CONTROL_REQUEST")){
                ControlRequest cr = (ControlRequest) r;
                if(cr.getSubtype().equals("OP_REGISTER")){
                	if(GlobalFunctions.isUser(GlobalFunctions.decrypt((byte [])cr.getArgs().get(0)))) {
                		ControlResponse crs = new ControlResponse("REGISTER_NOK");
                		crs.getArgs().add("There is already a user with that name in the DDBB");
                		this.osProxy.writeObject(crs);
                	}else {
                		GlobalFunctions.addUser((byte []) cr.getArgs().get(0), (byte []) cr.getArgs().get(1));
                		ControlResponse crs = new ControlResponse("REGISTER_OK");
                		crs.getArgs().add("Registration succesful");
                		this.osProxy.writeObject(crs);
                	}
                }else if(cr.getSubtype().equals("OP_LOGIN")){
                	if(GlobalFunctions.isUser(GlobalFunctions.decrypt((byte []) cr.getArgs().get(0)))) {
                		if(GlobalFunctions.getPassword(GlobalFunctions.decrypt((byte []) cr.getArgs().get(0))).equals(GlobalFunctions.decrypt((byte []) cr.getArgs().get(1)))) {
                			ControlResponse crs = new ControlResponse("LOGIN_OK");
                			crs.getArgs().add("Log in succesful");
                			this.osProxy.writeObject(crs);
                		}else {
                			ControlResponse crs = new ControlResponse("LOGIN_NOK");
                			crs.getArgs().add("Wrong password");
                			this.osProxy.writeObject(crs);
                		}
                	}else {
                		ControlResponse crs = new ControlResponse("LOGIN_NOK");
            			crs.getArgs().add("Wrong user");
            			this.osProxy.writeObject(crs);
                	}
                }
            }else if(r.getType().equals("DATA_REQUEST")){
                DataRequest dr = (DataRequest) r;
                if(dr.getSubtype().equals("OP_CPU")){
                    ControlResponse crsCPU = new ControlResponse("OP_CPU_OK");
                    Random random = new Random();
                    crsCPU.getArgs().add(random.nextInt(101));
                    this.osProxy.writeObject(crsCPU);
                }
            }
            this.doDisconnect();
        }catch(ClassNotFoundException e) {
            System.out.println("ClassNotFoundException: " + e.getMessage());
        }catch(IOException e) {
            System.out.println("readline: " + e.getMessage());
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
    
    public void doDisconnect() {
        try {
            if(this.proxySocket != null){
                this.proxySocket.close();
                this.proxySocket = null;
                this.isProxy.close();
                this.isProxy = null;
                this.osProxy.close();
                this.osProxy = null;
            }
        }catch(UncheckedIOException e) {
            System.out.println(e.getMessage());
        }catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
