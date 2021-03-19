package PracticasDistribuidos.practica1Distribuidos.ejercicio1.clientServerInterface;

import PracticasDistribuidos.practica1Distribuidos.ejercicio1.protocol.*;
import java.net.*;

import java.io.*;

public class Proxy {
    public static void main(String[] args) {
        try {
            ServerSocket listenSocket = new ServerSocket(Integer.valueOf(System.getenv("PORT")));
            
            while(true){
                Socket socket = listenSocket.accept();
                   
                new Connection(socket);
            }
        } catch (IOException e) { 
            System.out.println("Listen socket: "+ e.getMessage());
        }
    }
}

class Connection extends Thread {
    private ObjectOutputStream osClient, osServer1, osServer2;
    private ObjectInputStream isClient, isServer1, isServer2;
    private Socket clientSocket, serverSocket1, serverSocket2;
    private boolean done;

    public Connection(Socket clientSocket) {
        try{
            this.clientSocket = clientSocket;
            this.serverSocket1 = new Socket("localhost", Integer.valueOf(System.getenv("SERVERPORT1")));
            this.serverSocket2 = new Socket("localhost", Integer.valueOf(System.getenv("SERVERPORT2")));
            this.isServer1 = new ObjectInputStream(this.serverSocket1.getInputStream());
            this.isServer2 = new ObjectInputStream(this.serverSocket2.getInputStream());
            this.osServer1 = new ObjectOutputStream(this.serverSocket1.getOutputStream());
            this.osServer2 = new ObjectOutputStream(this.serverSocket2.getOutputStream());
            this.osClient = new ObjectOutputStream(this.clientSocket.getOutputStream());
            this.isClient = new ObjectInputStream(this.clientSocket.getInputStream());
            this.done = false;
            this.start();
        }catch(IOException e) {
            System.out.println("Connection: " + e.getMessage());
        }
    }

    public void run() {
        try{
            Request r = (Request) this.isClient.readObject();

            if(r.getType().equals("CONTROL_REQUEST")){
                ControlRequest cr = (ControlRequest) r;
                if(cr.getSubtype().equals("OP_DECRYPT")) {
                    this.doDecrypt(cr.getArgs().get(0).toString());
                    this.doDisconnect();
                }

                ControlResponse crs = new ControlResponse("OP_DECRYPT_OK");
                this.osClient.writeObject(crs);
            }else if(r.getType().equals("DATA_REQUEST")){
               DataRequest dr = (DataRequest) r;
               if(dr.getSubtype().equals("OP_RANKING")) {
                   this.doRanking();
                   this.doDisconnect();
               }
            }
        }catch(ClassNotFoundException e) {
			System.out.println("ClassNotFoundException: " + e.getMessage());
		}catch(IOException e) {
			System.out.println("readline: " + e.getMessage());
		}
    }

    private void doDecrypt(String message) {
        try{
            int cpu1 = 0, cpu2 = 0;

            DataRequest dr = new DataRequest("OP_CPU");
            this.osServer1.writeObject(dr);

            ControlResponse crs1 = (ControlResponse) this.isServer1.readObject();
            if(crs1 != null && crs1.getSubtype().equals("OP_CPU_OK")) cpu1 = Integer.valueOf(crs1.getArgs().get(0).toString());
            
            this.osServer2.writeObject(dr);
            ControlResponse crs2 = (ControlResponse) this.isServer2.readObject();
            if(crs2 != null && crs2.getSubtype().equals("OP_CPU_OK")) cpu2 = Integer.valueOf(crs2.getArgs().get(0).toString());

            ControlRequest crMessage = new ControlRequest("OP_DECRYPT_MESSAGE");
            crMessage.getArgs().add(message);
            if(cpu1 >= cpu2) {
                this.osServer2.writeObject(crMessage);
            }else{
                this.osServer1.writeObject(crMessage);
            }
        }catch(ClassNotFoundException e) {
            System.out.println("ClassNotFoundException: " + e.getMessage());
        }catch(IOException e) {
            System.out.println("Readline: " + e.getMessage());
        }
    }

    private void doRanking(){
        try {
            String rankingServer1="", rankingServer2="";

            DataRequest dr = new DataRequest("OP_RANKING_SERVER");
            this.osServer1.writeObject(dr);

            //Thread inactiveServer1 = new Thread(new InactiveServer(this, "OP_RANKING"));
            //inactiveServer1.start();
            ControlResponse cr1 = (ControlResponse) this.isServer1.readObject();
            this.done = true;
            if(cr1 != null && cr1.getSubtype().equals("OP_RANKING_SERVER_OK")) rankingServer1 = cr1.getArgs().get(0).toString();
            
            this.osServer2.writeObject(dr);
            
            //Thread inactiveServer2 = new Thread(new InactiveServer(this, "OP_RANKING"));
            //inactiveServer2.start();
            ControlResponse cr2 = (ControlResponse) this.isServer2.readObject();
            this.done = true;
            if(cr2 != null && cr2.getSubtype().equals("OP_RANKING_SERVER_OK")) rankingServer2 = cr2.getArgs().get(0).toString();
            
            String response = "Server 1: " + rankingServer1 + " - Server 2: " + rankingServer2;
            
            ControlResponse clientResponse = new ControlResponse("OP_RANKING_OK");
            clientResponse.getArgs().add(response);
            this.osClient.writeObject(clientResponse);
        }catch(ClassNotFoundException e) {
			System.out.println("ClassNotFoundException: " + e.getMessage());
		}catch(IOException e) {
			System.out.println("readline: " + e.getMessage());
		}
    }

    private void doDisconnect() {
        try{
            if(clientSocket != null){
                this.clientSocket.close();
                this.clientSocket = null;
                this.isClient.close();
                this.isClient = null;
                this.osClient.close();
                this.osClient = null;
            }
            
            if(serverSocket1 != null){
                this.serverSocket1.close();
                this.serverSocket1 = null;
                this.isServer1.close();
                this.isServer1 = null;
                this.osServer1.close();
                this.osServer1 = null;
            }
            
            if(serverSocket2 != null){
                this.serverSocket2.close();
                this.serverSocket2 = null;
                this.isServer2.close();
                this.isServer2 = null;
                this.osServer2.close();
                this.osServer2 = null;
            }

            this.done = false;
        }catch(UncheckedIOException e) {
            System.out.println(e.getMessage());
        }catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void doIsDisconnect() {
        
    }

    class InactiveServer implements Runnable{
        private Connection connection;
        private String type, finalData;

        public InactiveServer(Connection connection, String type){
            this.connection = connection;
            this.type = type;
            this.finalData = null;
        }

        @Override
        public void run(){
            try {
                Thread.sleep(100);
                if(this.type.equals("OP_RANKING") && !this.connection.done) {
                    this.finalData = "0";
                    this.connection.doDisconnect();
                }else if(this.type.equals("OP_DECRYPT") && !this.connection.done) this.finalData = "100";
            } catch(InterruptedException e){
                System.out.println(e.getMessage());
            }
        }

        public String getFinalData() {
            return this.finalData;
        }
    }
}