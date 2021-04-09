package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import java.net.*;

class NormalNode3{
    public static void main(String[] args) {
        try{
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getExternalVariables("PORTROBOT4"));

            while(true) {
                System.out.println("Waiting robot3...");
                Socket socket = listenSocket.accept();
                System.out.println("Accepted connection from: " + socket.getInetAddress().toString());
                
                new Connection(socket, 4);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}