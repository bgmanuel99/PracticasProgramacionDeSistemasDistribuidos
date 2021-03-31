package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import PracticasDistribuidos.practica1Distribuidos.protocol.*;

import java.net.*;
import java.io.*;

public class Proxy1 {
    public static void main(String[] args) {
        try {
            int numberServer = 0;
            numberServer = GlobalFunctions.getExternalVariables("MAXSERVER");
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getExternalVariables("PORTPROXY1"));
            
            if(numberServer != 0){
                while(true){
                    System.out.println("Waiting proxy1...");
                    Socket socket = listenSocket.accept();
                    System.out.println("Accepted connection from: " + socket.getInetAddress().toString());
                    
                    new Connection(socket, numberServer, 1);
                }
            }else {
                throw new Exception("There was an internal problem");
            }
        }catch(IOException e) { 
            System.out.println("Listen socket: "+ e.getMessage());
        }catch(Exception e) {
            System.out.println("Exception(main): " + e.getMessage());
        }
    }
}

class Connection extends Thread {
    private ObjectOutputStream [] os;
    private ObjectInputStream [] is;
    private Socket [] sockets;
    private int numberOfServers, numberProxy;
    private boolean error;
    private int [] dataCpu;

    public Connection(Socket clientSocket, int numberServers, int numberProxy) throws Exception {
        try{
            this.numberOfServers = numberServers;
            this.sockets = new Socket[numberServers+1];
            this.os = new ObjectOutputStream[numberServers+1];
            this.is = new ObjectInputStream[numberServers+1];
            
            this.sockets[0] = clientSocket;
            this.os[0] = new ObjectOutputStream(this.sockets[0].getOutputStream());
            this.is[0] = new ObjectInputStream(this.sockets[0].getInputStream());
            for(int i = 1; i < sockets.length; i++) {
                this.sockets[i] = new Socket("localhost", GlobalFunctions.getExternalVariables("PORTSERVER"+i));
                this.os[i] =  new ObjectOutputStream(this.sockets[i].getOutputStream());
                this.is[i] = new ObjectInputStream(this.sockets[i].getInputStream());
            }
            this.dataCpu = new int[this.numberOfServers];
            for(int i = 0; i < this.dataCpu.length; i++) dataCpu[i] = 0;
            this.error = false;
            this.numberProxy = numberProxy;
            this.start();
        }catch(IOException e) {
            System.out.println("Connection(Connection constructor): " + e.getMessage());
        }
    }

    public void run() {
        try{
            Request r = (Request) this.is[0].readObject();
            if(r.getType().equals("CONTROL_REQUEST")){
                ControlRequest cr = (ControlRequest) r;
                if(cr.getSubtype().equals("OP_REGISTER")) {
                    this.doRegister((byte []) cr.getArgs().get(0), (byte []) cr.getArgs().get(1));
                }else if(cr.getSubtype().equals("OP_LOGIN")){
                    this.doLogin((byte []) cr.getArgs().get(0), (byte []) cr.getArgs().get(1));
                }
            }
        }catch(ClassNotFoundException e) {
			System.out.println("ClassNotFoundException(run): " + e.getMessage());
		}catch(IOException e) {
			System.out.println("Readline(run): " + e.getMessage());
		}
    }

    private void doRegister(byte [] user, byte [] password) {
        try{
            DataRequest dr = new DataRequest("OP_CPU");
            for(int i = 1; i <= this.numberOfServers; i++){
                new DataCPU(this, i, dr);
            }

            while (true){
                int done = 0;
                for(int data : this.dataCpu) if(data!=0) done++;
                if(done == this.numberOfServers) break;
            }
            
            this.doDisconnect();
            this.doConnect();

            ControlRequest cr = new ControlRequest("OP_REGISTER");
            cr.getArgs().add(user);
            cr.getArgs().add(password);

            int indexServer = 0;
            int minCPU = 100;
            for(int i = 0; i < this.dataCpu.length; i++){
                if(this.dataCpu[i] < minCPU){
                    minCPU = this.dataCpu[i];
                    indexServer = i+1;
                }
            }

            if(indexServer != 0){
                this.os[indexServer].writeObject(cr);
            }else {
                this.os[0].writeObject(new ControlResponse("REGISTER_NOK").getArgs().add("There are no disponible servers to do the register, try later."));
                return;
            }
            
            ControlResponse crs = (ControlResponse) this.is[1].readObject();
            this.os[0].writeObject(crs);
        }catch(IOException e) {
            System.out.println("IOException (doRegister): " + e.getMessage());

        }catch(ClassNotFoundException e) {
            System.out.println("ClassNotFoundException (doRegister)"+e.getMessage());
        }
    }

    private void doLogin(byte [] user, byte [] password){
        try{
            DataRequest dr = new DataRequest("OP_CPU");
            for(int i = 1; i <= this.numberOfServers; i++){
                new DataCPU(this, i, dr);
            }

            while (true){
                int done = 0;
                for(int data : this.dataCpu) if(data!=0) done++;
                if(done == this.numberOfServers) break;
            }
            
            this.doDisconnect();
            this.doConnect();

            ControlRequest cr = new ControlRequest("OP_LOGIN");
            cr.getArgs().add(user);
            cr.getArgs().add(password);

            int indexServer = 0;
            int minCPU = 100;
            for(int i = 0; i < this.dataCpu.length; i++){
                if(this.dataCpu[i] < minCPU){
                    minCPU = this.dataCpu[i];
                    indexServer = i+1;
                }
            }

            if(indexServer != 0){
                this.os[indexServer].writeObject(cr);
            }else {
                this.os[0].writeObject(new ControlResponse("LOGIN_NOK").getArgs().add("There are no disponible servers to do the log in, try later."));
                return;
            }

            ControlResponse crs = (ControlResponse) this.is[1].readObject();
            this.os[0].writeObject(crs);
        }catch(IOException e) {
            System.out.println("IOException (doLogin): " + e.getMessage());
        }catch(ClassNotFoundException e) {
            System.out.println("ClassNotFoundException (doLogin): " + e.getMessage());
        }
    }

    private void doConnect() {
    	try {
            for(int i = 1; i < this.sockets.length; i++){
                this.sockets[i] = new Socket("localhost", GlobalFunctions.getExternalVariables("PORTSERVER"+i));
                this.os[i] =  new ObjectOutputStream(this.sockets[i].getOutputStream());
                this.is[i] = new ObjectInputStream(this.sockets[i].getInputStream());
            }
    	}catch(UncheckedIOException e) {
            System.out.println(e.getMessage());
        }catch(IOException e) {
            System.out.println(e.getMessage());
        }catch(Exception e) {
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
            System.out.println("Exception (doDisconnect(index))"+e.getMessage());
        }
    }

    class DataCPU extends Thread{
        private Connection connection;
        private int indexServer;
        private DataRequest dataRequest;
        private boolean done;

        public DataCPU(Connection connection, int indexServer, DataRequest dataRequest){
            this.connection = connection;
            this.indexServer = indexServer;
            this.dataRequest = dataRequest;
            this.done = false;
            this.start();
        }

        @Override
        public void run() {
            Masking m = new Masking(this.indexServer, this);
            long start= System.currentTimeMillis();
            try {
                this.connection.os[this.indexServer].writeObject(this.dataRequest);
                m.start();
                ControlResponse crs = (ControlResponse)  this.connection.is[this.indexServer].readObject();
                this.done = true;
                this.connection.dataCpu[this.indexServer-1] = Integer.valueOf(crs.getArgs().get(0).toString());
            }catch(IOException e) {
                System.out.println("(IOException) DataCpu: "+e.getMessage());
                this.connection.dataCpu[this.indexServer-1] = 100;
            }catch(ClassNotFoundException e) {
                System.out.println("(ClassNotFoundException) DataCpu: "+e.getMessage());
                this.connection.dataCpu[this.indexServer-1] = 100;
            }
            long end = System.currentTimeMillis();
            try {
                GlobalFunctions.setLatency((end-start), this.connection.numberProxy, "Proxy");
            } catch (Exception e) {
                System.out.println("Exception (DataCpu): "+e.getMessage());
            }
        }

    }
    
    class Masking extends Thread {
        private int indexServer;
        private DataCPU dataCpu;

        public Masking(int indexServer, DataCPU dataCpu) {
            this.indexServer = indexServer;
            this.dataCpu = dataCpu;
        }

        @Override
        public void run() {
            long sleep = 300;

            try{
                sleep = GlobalFunctions.getLatency(this.dataCpu.connection.numberProxy, "Proxy");
            }catch(Exception e) {
                System.out.println("Exception (Masking run 1): " + e.getMessage());
            }

            try{
                Thread.sleep(sleep);
                if(!this.dataCpu.done) {
                    this.dataCpu.connection.doDisconnect(this.indexServer);
                }
            }catch(InterruptedException e) {
                System.out.println("InterruptedException (Masking run): " + e.getMessage());
            }catch(Exception e){
                System.out.println("Exception (Masking run 2): " + e.getMessage());
            }
        }
    }
}
