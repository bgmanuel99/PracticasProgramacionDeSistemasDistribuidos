package PracticasDistribuidos.practica1Distribuidos.ejercicio1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class CentralServer2 {
	public static void main(String [] args) {
        try{
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getExternalVariables("PORTCENTER2"));
            
            while(true) {
                System.out.println("Waiting central server 2...");
                Socket socket = listenSocket.accept();
                System.out.println("Accepted conexion from: " + socket.getInetAddress().toString());
                new ConnectionCentral(socket, 2);
            }
        }catch(IOException e) { 
            System.out.println("Listen socket: "+ e.getMessage());
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
