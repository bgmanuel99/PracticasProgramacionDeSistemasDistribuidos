package PracticasDistribuidos.practica1Distribuidos.ejercicio1.clientServerInterface;

import PracticasDistribuidos.practica1Distribuidos.ejercicio1.protocol.*;
import java.net.*;
import java.io.*;

public class Proxy2 {
    public static void main(String[] args) {
        try {
            int numberSocket = 8000, numberServers = 2;
            ServerSocket listenSocket = new ServerSocket(4001);
            
            while(true){
            	System.out.println("Waiting proxy2...");
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

