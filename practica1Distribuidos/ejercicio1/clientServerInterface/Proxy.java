package PracticasDistribuidos.practica1Distribuidos.ejercicio1.clientServerInterface;

import PracticasDistribuidos.practica1Distribuidos.ejercicio1.protocol.*;
import java.net.*;
import java.io.*;

public class Proxy {
    public static void main(String[] args) {
        try {
            int numberSocket = 8000, numberServers = 2;
            ServerSocket listenSocket = new ServerSocket(numberSocket);
            
            while(true){
            	System.out.println("Waiting proxy...");
                Socket socket = listenSocket.accept();
                System.out.println("Acceptada conexion de: " + socket.getInetAddress().toString());
                   
                new Connection(socket, numberSocket, numberServers);
            }
        }catch(IOException e) { 
            System.out.println("Listen socket: "+ e.getMessage());
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

class Connection extends Thread {
    private ObjectOutputStream [] os;
    private ObjectInputStream [] is;
    private Socket [] sockets;
    private boolean done;
    private int numberOfServers;

    public Connection(Socket clientSocket, int numberSocket, int numberServers) throws Exception {
        try{
            if(numberServers > 0){
                this.numberOfServers = numberServers;
                this.sockets = new Socket[numberServers+1];
                this.os = new ObjectOutputStream[numberServers+1];
                this.is = new ObjectInputStream[numberServers+1];
                
                this.sockets[0] = clientSocket;
                this.os[0] = new ObjectOutputStream(this.sockets[0].getOutputStream());
                this.is[0] = new ObjectInputStream(this.sockets[0].getInputStream());
                for(int i = 1; i < sockets.length; i++) {
                    this.sockets[i] = new Socket("localhost", numberSocket+=1);
                    this.os[i] =  new ObjectOutputStream(this.sockets[i].getOutputStream());
                    this.is[i] = new ObjectInputStream(this.sockets[i].getInputStream());
                }
                this.done = false;
                this.start();
            }else{
                throw new Exception("Number of servers cannot be less than one");
            }
        }catch(IOException e) {
            System.out.println("Connection: " + e.getMessage());
        }
    }

    public void run() {
        try{
            Request r = (Request) this.is[0].readObject();
            if(r.getType().equals("CONTROL_REQUEST")){
                ControlRequest cr = (ControlRequest) r;
                if(cr.getSubtype().equals("OP_DECRYPT")) this.doDecrypt((byte [])cr.getArgs().get(0));
                
                ControlResponse crs = new ControlResponse("OP_DECRYPT_OK");
                this.os[0].writeObject(crs);
            }else if(r.getType().equals("DATA_REQUEST")){
               DataRequest dr = (DataRequest) r;
               if(dr.getSubtype().equals("OP_RANKING")) this.doRanking();
            }
        }catch(ClassNotFoundException e) {
			System.out.println("ClassNotFoundException: " + e.getMessage());
		}catch(IOException e) {
			System.out.println("readline: " + e.getMessage());
		}
    }

    private void doDecrypt(byte [] message) {
        try{
            int [] cpuPercentage = new int[this.numberOfServers];
            DataRequest dr = new DataRequest("OP_CPU");
            for(int i = 1; i <= this.numberOfServers; i++){
                this.os[i].writeObject(dr);
                ControlResponse cr = (ControlResponse) this.is[i].readObject();
                cpuPercentage[i-1] = Integer.valueOf(cr.getArgs().get(0).toString());
            }
            
            this.doDisconnect();
            this.doConnect();
            
            ControlRequest cr = new ControlRequest("OP_DECRYPT_MESSAGE");
            int minCpu = 100;
            int indexServer = 0;
            for(int i = 0; i < cpuPercentage.length; i++) {
                if(cpuPercentage[i] < minCpu) {
                    minCpu = cpuPercentage[i];
                    indexServer = i+1;
                }
            }
            cr.getArgs().add(message);
            this.os[indexServer].writeObject(cr);
        }catch(ClassNotFoundException e) {
            System.out.println("ClassNotFoundException: " + e.getMessage());
        }catch(IOException e) {
            System.out.println("Readline: " + e.getMessage());
        }
    }

    private void doRanking(){
        try {
            String rankingServer = "";

            DataRequest dr = new DataRequest("OP_RANKING_SERVER");
            for(int i = 1; i <= this.numberOfServers; i++){
                this.os[i].writeObject(dr);
                ControlResponse cr = (ControlResponse) this.is[i].readObject();
                rankingServer += "- Server " + i + ": " + cr.getArgs().get(0).toString() + "\n";
            }

            ControlResponse crClient = new ControlResponse("OP_RANKING_OK");
            crClient.getArgs().add(rankingServer);
            this.os[0].writeObject(crClient);
        }catch(ClassNotFoundException e) {
			System.out.println("ClassNotFoundException: " + e.getMessage());
		}catch(IOException e) {
			System.out.println("readline: " + e.getMessage());
		}
    }
    
    private void doConnect() {
    	try {
            int socketServer = 8000;
            for(int i = 1; i < this.sockets.length; i++){
                this.sockets[i] = new Socket("localhost", socketServer+=1);
                this.os[i] =  new ObjectOutputStream(this.sockets[i].getOutputStream());
                this.is[i] = new ObjectInputStream(this.sockets[i].getInputStream());
            }
    	}catch(UncheckedIOException e) {
            System.out.println(e.getMessage());
        }catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void doDisconnect() {
        try{
            for(int i = 1; i < this.sockets.length; i++){
                this.os[i].close();
                this.os[i] = null;
                this.is[i].close();
                this.is[i] = null;
                this.sockets[i].close();
                this.sockets[i]=null;
            }
        }catch(UncheckedIOException e) {
            System.out.println(e.getMessage());
        }catch(IOException e) {
            System.out.println(e.getMessage());
        }
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