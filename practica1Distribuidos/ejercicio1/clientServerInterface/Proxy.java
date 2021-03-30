package PracticasDistribuidos.practica1Distribuidos.ejercicio1.clientServerInterface;

import PracticasDistribuidos.practica1Distribuidos.ejercicio1.protocol.*;
import java.net.*;
import java.util.Scanner;
import java.io.*;

public class Proxy {
    public static void main(String[] args) {
        try {
            int numberSocket = 8000, numberServers = 2;
            ServerSocket listenSocket = new ServerSocket(4000);
            
            while(true){
            	System.out.println("Waiting proxy1...");
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
    private int [] dataCpu;
    private String [] dataRanking;
    private int numberOfServers;
    private boolean error =false;

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
                this.dataCpu = new int[this.numberOfServers];
                this.dataRanking = new String[this.numberOfServers];
                for(int i = 0; i < this.dataCpu.length; i++) dataCpu[i] = 0;
                for(int i = 0; i < this.dataRanking.length; i++) dataRanking[i] = "";
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
                if(cr.getSubtype().equals("OP_DECRYPT")) {
                	this.doDecrypt((byte [])cr.getArgs().get(0));
                	
                    if(!this.error) {
                    	ControlResponse crs = new ControlResponse("OP_DECRYPT_OK");
                        this.os[0].writeObject(crs);
                    }else {
                    	ControlResponse crs = new ControlResponse("OP_DECRYPT_NOK");
                        this.os[0].writeObject(crs);
                    }
                    this.error = false;
                }
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
        	
            DataRequest dr = new DataRequest("OP_CPU");
            for(int i = 1; i <= this.numberOfServers; i++){
                DataCpuRanking dataCPU = new DataCpuRanking(this, "CPU", i, dr);
                dataCPU.start();
            }
            
            while(true) {
            	int done = 0;
            	for(int data : this.dataCpu) if(data != 0) done++;
            	if(done == this.numberOfServers) break;
            }
            
            this.doDisconnect();
            this.doConnect();
            
            ControlRequest cr = new ControlRequest("OP_DECRYPT_MESSAGE");
            int minCpu = 100;
            int indexServer = 0;
            for(int i = 0; i < this.dataCpu.length; i++) {
                if(this.dataCpu[i] < minCpu) {
                    minCpu = this.dataCpu[i];
                    indexServer = i+1;
                }
            }
            
            if(indexServer !=0) {
            	cr.getArgs().add(message);
            	this.os[indexServer].writeObject(cr);
            }else this.error = true;
        }catch(IOException e) {
            System.out.println("Readline: " + e.getMessage());
        }
    }

    private void doRanking(){
        try {
            String rankingServer = "";

            DataRequest dr = new DataRequest("OP_RANKING_SERVER");
            for(int i = 1; i <= this.numberOfServers; i++){
                DataCpuRanking dataRANKING = new DataCpuRanking(this, "RANKING", i, dr);
                dataRANKING.start();
            }
            
            while(true) {
            	int done = 0;
            	for(String data : this.dataRanking) if(data != "") done++;
            	if(done == this.numberOfServers) break;
            }
            
            for(int i = 0; i < this.dataRanking.length; i++) rankingServer += "Server " + (i+1) + " has decrypted: " + this.dataRanking[i] + " messages.\n  ";

            ControlResponse crClient = new ControlResponse("OP_RANKING_OK");
            crClient.getArgs().add(rankingServer);
            this.os[0].writeObject(crClient);
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
                if(this.sockets[i]!=null) {
                	this.os[i].close();
                    this.os[i] = null;
                    this.is[i].close();
                    this.is[i] = null;
                    this.sockets[i].close();
                    this.sockets[i]=null;
                }
            }
        }catch(UncheckedIOException e) {
            System.out.println(e.getMessage());
        }catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void doDisconnect(int indexServer) {
        try {
            if(this.sockets[indexServer] != null && indexServer != 0){
                this.is[indexServer].close();
                this.is[indexServer] = null;
                this.os[indexServer].close();
                this.os[indexServer] = null;
                this.sockets[indexServer].close();
                this.sockets[indexServer] = null;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    class DataCpuRanking extends Thread {
        private Connection connection;
        private int indexServer;
        private String type;
        private DataRequest dataRequest;
        private boolean done;

        public DataCpuRanking(Connection connection, String type, int indexServer, DataRequest dataRequest) {
            this.connection = connection;
            this.type = type;
            this.indexServer = indexServer;
            this.dataRequest = dataRequest;
            this.done = false;
        }

		@Override
        public void run() {
			
			
            Marshalling m = new Marshalling(this.type, this.indexServer, this);
            long start= System.currentTimeMillis();
            try{
                this.connection.os[this.indexServer].writeObject(this.dataRequest);
                m.start();
                ControlResponse crs = (ControlResponse) this.connection.is[this.indexServer].readObject();
                this.done = true;
                if(this.type.equals("CPU")) this.connection.dataCpu[this.indexServer-1] = Integer.valueOf(crs.getArgs().get(0).toString());
                else this.connection.dataRanking[this.indexServer-1] = crs.getArgs().get(0).toString();
                
            }catch(IOException e) {
                System.out.println(e.getMessage());
                if(this.type.equals("CPU")) this.connection.dataCpu[this.indexServer-1] = m.getFinalData();
                else this.connection.dataRanking[this.indexServer-1] = String.valueOf(m.getFinalData());
            }catch(ClassNotFoundException e) {
                System.out.println(e.getMessage());
                if(this.type.equals("CPU")) this.connection.dataCpu[this.indexServer-1] = m.getFinalData();
                else this.connection.dataRanking[this.indexServer-1] = String.valueOf(m.getFinalData());
            }
            long end =System.currentTimeMillis();
            System.out.println("Latency server "+ this.indexServer+": " +(end-start)+" ms");
            File file= new File("mean"+this.indexServer+".txt");
            if(file.exists()) {
            	PrintWriter outputFile;
				try {
					String latency="";
					Scanner scanner = new Scanner(file);
					while(scanner.hasNext()) {
                       latency +=scanner.next();
                       latency +=" ";
                    }
                    scanner.close();
					outputFile = new PrintWriter(file);
					String text=latency+(end-start);
					
					outputFile.print(text);
	                outputFile.close();
				} catch (FileNotFoundException e) {
					
					System.out.println(e.getMessage());
				}
            	

            }else {
            	System.out.println("The file "+file.getName()+" does not exists");
            }
            
        }
    }

    class Marshalling extends Thread {
        private String type;
        private int finalData, indexServer;
        private DataCpuRanking dataCpuRanking;

        public Marshalling(String type, int indexServer, DataCpuRanking dataCpuRanking) {
            this.type = type;
            if(this.type.equals("CPU")) this.finalData = 100;
            else this.finalData = 0;
            this.indexServer = indexServer;
            this.dataCpuRanking = dataCpuRanking;
        }

        @Override
        public void run(){
        	File file= new File("mean"+this.indexServer+".txt");
        	long sleep= 300; 
            if(file.exists()) {
            	
				try {
					int i = 0;
					int sum = 0;
					Scanner scanner = new Scanner(file);
					while(scanner.hasNext()) {
                       sum +=scanner.nextInt();
                       i+=1;
                    }
                    scanner.close();
                    if(sum!=0) {
                    	sleep=(sum/i);
                    }
                    System.out.println("Current waiting time: "+sleep+" ms");
                    
					
				} catch (FileNotFoundException e) {
					
					System.out.println(e.getMessage());
				}
            	

            }else {
            	System.out.println("The file "+file.getName()+" does not exists");
            }
        	
            try {
            	
                Thread.sleep(sleep);
                if(this.type.equals("RANKING") && !this.dataCpuRanking.done) {
                    this.finalData = 0;
                    this.dataCpuRanking.connection.doDisconnect(this.indexServer);
                }else if(this.type.equals("CPU") && !this.dataCpuRanking.done) {
                	System.out.println("cpu fails");
                    this.finalData = 100;
                    this.dataCpuRanking.connection.doDisconnect(this.indexServer);
                }
            }catch(InterruptedException e){
                System.out.println(e.getMessage());
            }
        }

        public int getFinalData() {
            return this.finalData;
        }
    }
}