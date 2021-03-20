package PracticasDistribuidos.practica1Distribuidos.ejercicio1.clientServerInterface;

import PracticasDistribuidos.practica1Distribuidos.ejercicio1.protocol.*;
import java.net.*;

import java.io.*;

public class Server2 {
    public static void main(String[] args) {
        try {
            ServerSocket listenSocket = new ServerSocket(8002);
            
            while(true){
            	System.out.println("Waiting server 2...");
                Socket socket = listenSocket.accept();
                System.out.println("Acceptada conexion de: " + socket.getInetAddress().toString());
                   
                new ConnectionServer2(socket);
            }
        } catch (IOException e) { 
            System.out.println("Listen socket: "+ e.getMessage());
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
        	System.out.println("1");
            Request r = (Request) this.isProxy.readObject();
            System.out.println("2");
            System.out.println(r.getType().toString());
            System.out.println(r.getSubtype().toString());
            if(r.getType().equals("CONTROL_REQUEST")){
                ControlRequest cr = (ControlRequest) r;
                System.out.println("3");
                if(cr.getSubtype().equals("OP_DECRYPT_MESSAGE")){
                	System.out.println(cr.getArgs().get(0).toString());
                    System.out.println("mensaje desencriptandose...");
                    this.doDisconnect();
                }
            }else if(r.getType().equals("DATA_REQUEST")){
                DataRequest dr = (DataRequest) r;
                if(dr.getSubtype().equals("OP_CPU")){
                    ControlResponse crsCPU = new ControlResponse("OP_CPU_OK");
                    crsCPU.getArgs().add("30");
                    this.osProxy.writeObject(crsCPU);
                    this.doDisconnect();
                }else if(dr.getSubtype().equals("OP_RANKING_SERVER")){
                    ControlResponse crsRanking = new ControlResponse("OP_RANKING_SERVER_OK");
                    crsRanking.getArgs().add("4");
                    this.osProxy.writeObject(crsRanking);
                    this.doDisconnect();
                }
            }
            this.doDisconnect();
        }catch(ClassNotFoundException e) {
			System.out.println("ClassNotFoundException: " + e.getMessage());
		}catch(IOException e) {
			System.out.println("readline: " + e.getMessage());
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