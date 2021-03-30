package PracticasDistribuidos.practica1Distribuidos.ejercicio1.clientServerInterface;

import java.net.*;
import java.io.*;

public class Proxy2 {
    public static void main(String[] args) {
        try {
            int numberServers = 0;
            numberServers = GlobalFunctions.getExternalVariables("MAXSERVER");
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getExternalVariables("PORTPROXY2"));
            
            if(numberServers != 0) {
                while(true){
                    System.out.println("Waiting proxy2...");
                    Socket socket = listenSocket.accept();
                    System.out.println("Acceptada conexion de: " + socket.getInetAddress().toString());
                       
                    new Connection(socket, numberServers, 2);
                }
            }else {
                throw new Exception("There was an internal problem.");
            }
        }catch(IOException e) { 
            System.out.println("Listen socket: "+ e.getMessage());
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}