package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import PracticasDistribuidos.practica1Distribuidos.protocol.*;

import java.net.*;
import java.util.Random;
import java.io.*;

public class Server1 {
    public static void main(String[] args) {
        try {
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getExternalVariables("PORTSERVER1"));
            
            while(true){
            	System.out.println("Waiting server 1... " +listenSocket.getLocalPort());
                Socket socket = listenSocket.accept();
                System.out.println("Accepted conexion from: " + socket.getInetAddress().toString());
                   
                new ConnectionServer1(socket);
            }
        } catch(IOException e) { 
            System.out.println("Listen socket: "+ e.getMessage());
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
}
class ConnectionServer1 extends Thread{
    private ObjectOutputStream osProxy;
    private ObjectInputStream isProxy;
    private Socket proxySocket;

    public ConnectionServer1(Socket proxySocket){
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
                System.out.println("Request : "+cr.getSubtype());
                if(cr.getSubtype().equals("OP_REGISTER")){
                	
                	if(GlobalFunctions.isUser(GlobalFunctions.decrypt((byte [])cr.getArgs().get(0)))) {
                		System.out.println("1");
                		ControlResponse crs = new ControlResponse("REGISTER_NOK");
                		crs.getArgs().add("Registration has not been completed");
                		this.osProxy.writeObject(crs);
                	}else {
                		System.out.println("2");
                		GlobalFunctions.addUser(GlobalFunctions.decrypt((byte [])cr.getArgs().get(0)), GlobalFunctions.decrypt((byte [])cr.getArgs().get(1)));
                		ControlResponse crs = new ControlResponse("REGISTER_OK");
                		crs.getArgs().add("Registration succesful");
                		this.osProxy.writeObject(crs);
                		
                	}
                }else if(cr.getSubtype().equals("OP_LOGIN")){
                	if(GlobalFunctions.isUser(GlobalFunctions.decrypt((byte [])cr.getArgs().get(0)))) {
                		System.out.println("3");
                		if(GlobalFunctions.getUser(GlobalFunctions.decrypt((byte [])cr.getArgs().get(0))).equals(GlobalFunctions.decrypt((byte [])cr.getArgs().get(1)))) {
                			System.out.println("4");
                			ControlResponse crs = new ControlResponse("LOGIN_OK");
                			crs.getArgs().add("Log in succesful");
                			this.osProxy.writeObject(crs);
                			
                		}else {
                			System.out.println("5");
                			ControlResponse crs = new ControlResponse("LOGIN_NOK");
                			crs.getArgs().add("Wrong pass");
                			this.osProxy.writeObject(crs);
                			
                		}
                	}else {
                		System.out.println("6");
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
