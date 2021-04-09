package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import java.net.*;

class NormalNode2{
    public static void main(String[] args) {
        try{
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getExternalVariables("PORTROBOT3"));

            while(true) {
                System.out.println("Waiting robot2...");
                Socket socket = listenSocket.accept();
                System.out.println("Accepted connection from: " + socket.getInetAddress().toString());
                
                new Connection(socket, 3);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }
}
