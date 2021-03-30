package PracticasDistribuidos.practica1Distribuidos.ejercicio1.clientServerInterface;

import PracticasDistribuidos.practica1Distribuidos.ejercicio1.protocol.*;
import java.net.*;
import java.util.Random;
import java.util.Scanner;
import java.io.*;
import javax.crypto.Cipher;

public class Server2 {
    public static void main(String[] args) {
        try {
            ServerSocket listenSocket = new ServerSocket(GlobalFunctions.getExternalVariables("PORTSERVER2"));
            
            while(true){
            	System.out.println("Waiting server 2...");
                Socket socket = listenSocket.accept();
                System.out.println("Acceptada conexion de: " + socket.getInetAddress().toString());
                   
                new ConnectionServer2(socket);
            }
        } catch (IOException e) { 
            System.out.println("Listen socket: "+ e.getMessage());
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

class ConnectionServer2 extends Thread{
    private ObjectOutputStream osProxy;
    private ObjectInputStream isProxy;
    private Socket proxySocket;

    public ConnectionServer2(Socket proxySocket){
        try {
            this.proxySocket = proxySocket;
            this.osProxy = new ObjectOutputStream(this.proxySocket.getOutputStream());
            this.isProxy = new ObjectInputStream(this.proxySocket.getInputStream());
            this.start();
        }catch(IOException e) {
            System.out.println("Connection: " + e.getMessage());
        }
    }

    public void run() {
        try {
            Request r = (Request) this.isProxy.readObject();
            
            if(r.getType().equals("CONTROL_REQUEST")){
                ControlRequest cr = (ControlRequest) r;
                if(cr.getSubtype().equals("OP_DECRYPT_MESSAGE")){
                    System.out.println(this.decrypt((byte []) cr.getArgs().get(0)));
                    System.out.println("The message has been desencrypted");

                    File file = new File("Server2Ranking.txt");
                    int decryptedMessages = 0;
                    if(file.exists()){
                        Scanner scanner = new Scanner(file);
                        while(scanner.hasNext()) {
                            decryptedMessages += Integer.valueOf(scanner.next());
                        }
                        scanner.close();
                    }else{
                        throw new Exception("The file Server2Ranking.txt does not exist");
                    }
                    PrintWriter outputFile = new PrintWriter(file);
                    outputFile.print(decryptedMessages+1);
                    outputFile.close();

                    System.out.println("Server state saved");

                    this.doDisconnect();
                }
            }else if(r.getType().equals("DATA_REQUEST")){
                DataRequest dr = (DataRequest) r;
                if(dr.getSubtype().equals("OP_CPU")){
                    ControlResponse crsCPU = new ControlResponse("OP_CPU_OK");
                    Random random = new Random();
                    crsCPU.getArgs().add(random.nextInt(101));
                    this.osProxy.writeObject(crsCPU);
                    this.doDisconnect();
                }else if(dr.getSubtype().equals("OP_RANKING_SERVER")){
                    File file = new File("Server2Ranking.txt");
                    String ranking = "0";
                    if(file.exists()){
                        Scanner scanner = new Scanner(file);
                        while(scanner.hasNext()) ranking=scanner.next();
                        scanner.close();
                    }else{
                        throw new Exception("The file Server2ranking.txt does not exist");
                    }
                    
                    ControlResponse crsRanking = new ControlResponse("OP_RANKING_SERVER_OK");
                    crsRanking.getArgs().add(ranking);
                    this.osProxy.writeObject(crsRanking);
                    this.doDisconnect();
                }
            }
            this.doDisconnect();
        }catch(ClassNotFoundException e) {
			System.out.println("ClassNotFoundException: " + e.getMessage());
		}catch(IOException e) {
			System.out.println("readline: " + e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
    }

    public String decrypt(byte [] encryptedMessage) throws Exception {
        final Cipher aes = GlobalFunctions.getCipher(false);
        final byte [] bytes = aes.doFinal(encryptedMessage);
        final String message = new String(bytes, "UTF-8");
        return message;
    }

    public void doDisconnect() {
        try {
            if(this.proxySocket != null){
                this.proxySocket.close();
                this.proxySocket = null;
                this.isProxy.close();
                this.isProxy = null;
                this.osProxy.close();
                this.osProxy = null;
            }
        }catch(UncheckedIOException e) {
            System.out.println(e.getMessage());
        }catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
}