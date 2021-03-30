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
                    System.out.println("Acceptada conexion de: " + socket.getInetAddress().toString());
                    
                    new Connection(socket, numberServer, 1);
                }
            }else {
                throw new Exception("There was an internal problem");
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
    private boolean error;
    private int numberProxy;

    public Connection(Socket clientSocket, int numberServers, int numberProxy) throws Exception {
        try{
            this.numberOfServers = numberServers;
            this.sockets = new Socket[numberServers+1];
            this.os = new ObjectOutputStream[numberServers+1];
            this.is = new ObjectInputStream[numberServers+1];
            
            this.sockets[0] = clientSocket;
            this.os[0] = new ObjectOutputStream(this.sockets[0].getOutputStream());
            this.is[0] = new ObjectInputStream(this.sockets[0].getInputStream());
            /*for(int i = 1; i < sockets.length; i++) {
                this.sockets[i] = new Socket("localhost", GlobalFunctions.getExternalVariables("PORTSERVER"+i));
                this.os[i] =  new ObjectOutputStream(this.sockets[i].getOutputStream());
                this.is[i] = new ObjectInputStream(this.sockets[i].getInputStream());
            }*/
            this.dataCpu = new int[this.numberOfServers];
            this.dataRanking = new String[this.numberOfServers];
            for(int i = 0; i < this.dataCpu.length; i++) dataCpu[i] = 0;
            for(int i = 0; i < this.dataRanking.length; i++) dataRanking[i] = "";
            this.error = false;
            this.numberProxy = numberProxy;
            this.start();
        }catch(IOException e) {
            System.out.println("Connection: " + e.getMessage());
        }
    }

    public void run() {
        try{
            Request r = (Request) this.is[0].readObject();
            if(r.getType().equals("CONTROL_REQUEST")){
                ControlRequest cr = (ControlRequest) r;
                if(cr.getSubtype().equals("OP_REGISTER")) {
                	ControlResponse crs = new ControlResponse("REGISTER_OK");
                    crs.getArgs().add("Se ha registrado correctamente");
                    this.os[0].writeObject(crs);
                }
            }
        }catch(ClassNotFoundException e) {
			System.out.println("ClassNotFoundException: " + e.getMessage());
		}catch(IOException e) {
			System.out.println("Readline run function: " + e.getMessage());
		}
    }
}
