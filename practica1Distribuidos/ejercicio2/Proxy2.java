package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import java.net.*;
import java.io.*;

public class Proxy2 {
    public static void main(String[] args) {
        try {
            int numberServer = 0;
            numberServer = GlobalFunctions.getExternalVariables("MAXSERVER");
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getExternalVariables("PORTPROXY2"));
            
            if(numberServer != 0){
                while(true){
                    System.out.println("Waiting proxy2...");
                    Socket socket = listenSocket.accept();
                    System.out.println("Accepted connection from: " + socket.getInetAddress().toString());
                    
                    new Connection(socket, numberServer, 2);
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
