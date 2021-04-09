package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import java.net.*;

class NormalNode8{
    public static void main(String[] args) {
        try{
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getExternalVariables("PORTROBOT9"));

            while(true) {
                System.out.println("Waiting robot8...");
                Socket socket = listenSocket.accept();
                System.out.println("Accepted connection from: " + socket.getInetAddress().toString());
                
                new Connection(socket, 9);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}