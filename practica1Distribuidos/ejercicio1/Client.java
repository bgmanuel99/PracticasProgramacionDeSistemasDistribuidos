package PracticasDistribuidos.practica1Distribuidos.ejercicio1;

import PracticasDistribuidos.practica1Distribuidos.protocol.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import javax.crypto.Cipher;


public class Client {
    public static final String version = "1.0";

    private Console console;
    private Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private boolean done;
    private int maxProxy = 0;

    public static void main(String [] args) {
    	GlobalFunctions.initFile("ClientLatency.txt");
    	GlobalFunctions.initFile("Server1Ranking.txt");
    	GlobalFunctions.initFile("Server2Ranking.txt");
    	GlobalFunctions.initFile("Proxy1Latency.txt");
    	GlobalFunctions.initFile("Proxy2Latency.txt");
        new Client();
    }

    public void init() {
        try{
            this.console = new Console();
            this.done = false;
            this.maxProxy = GlobalFunctions.getExternalVariables("MAXPROXY");
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    public Client() {
        this.init();

        if(this.maxProxy != 0){
            String cmd= this.console.getCommand();
            
            while(!cmd.equals("close")){
                long start = System.currentTimeMillis();
                try {                	
                	if(cmd.equals("decrypt")){
                		String message = this.console.getCommandDecrypt();
                		this.console.writeMessage("This is your message: " + message);
                		this.doConnect(GlobalFunctions.getExternalVariables("PORTPROXY1"), 1);
                		this.doDecrypt(message);
                		this.doDisconnect();
                	}else if(cmd.equals("ranking")){
                		this.doConnect(GlobalFunctions.getExternalVariables("PORTPROXY1"), 1);
                		this.doRanking();
                		this.doDisconnect();
                	}
                }catch (Exception e) {
                	System.out.println(e.getMessage());
                }
                long end = System.currentTimeMillis();
                
                try{
                    File file = new File("ClientLatency.txt");
                    int i = 0;
                    long latency = 0;
                    if(file.exists()){
                        Scanner scanner = new Scanner(file);
                        while(scanner.hasNext()){
                            latency += Long.valueOf(scanner.next());
                            i++;
                        }
                        scanner.close();
                        latency += (end - start);
                        PrintWriter outputFile = new PrintWriter(file);
                        outputFile.print(latency/(i+1));
                        outputFile.close();
                    }else {
                        throw new Exception("The file ClientLatency.txt does not exist");
                    }
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
                
                cmd = this.console.getCommand();
            }
        }
        
        this.doDisconnect();
    }

    private void doDecrypt(String message) {
        try{
            ControlRequest cr = new ControlRequest("OP_DECRYPT");
            cr.getArgs().add(this.encryptMessage(message));
            this.os.writeObject(cr);
            
            Thread inactiveProxy = new Thread(new InactiveProxy(this));
            inactiveProxy.start();
            ControlResponse crs = (ControlResponse) this.is.readObject();
            this.done = true;
            if(crs != null && crs.getSubtype().equals("OP_DECRYPT_OK")) this.console.writeMessage("Data sended");
            else if(crs != null && crs.getSubtype().equals("OP_DECRYPT_NOK")) this.console.writeMessage("Error: The message could not be sent to the servers");
        }catch(ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }catch(IOException e) {
        	this.console.writeMessage("An error has ocurred: The proxy is a bit shy and the message has not been received");
        }catch (Exception e) {
        	System.out.println(e.getMessage());
		}
    }
    
    private void doRanking() {
        try {
            DataRequest dr = new DataRequest("OP_RANKING");
            this.os.writeObject(dr);

            Thread inactiveProxy = new Thread(new InactiveProxy(this));
            inactiveProxy.start();
            ControlResponse crs = (ControlResponse) this.is.readObject();
            this.done = true;
            if(crs != null && crs.getSubtype().equals("OP_RANKING_OK")) this.console.writeMessage(crs.getArgs().get(0).toString());
        }catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }catch (IOException e) {
        	this.console.writeMessage("An error has ocurred: The proxy is a bit shy and the message has not been received");
        }catch(Exception e) {
        	System.out.println(e.getMessage());
        }
    }
    
    public byte[] encryptMessage(String message) throws Exception {
        final byte[] bytes = message.getBytes("UTF-8");
        final Cipher aes = GlobalFunctions.getCipher(true);
        final byte[] encryptedMessage = aes.doFinal(bytes);
        return encryptedMessage;
    }
    
    private void doConnect(int port, int count) {
    	int auxProxy = 0;
        try{
            if(count > this.maxProxy) throw new Exception("All the proxys are disconnected");
            count++;
            auxProxy = GlobalFunctions.getExternalVariables("PORTPROXY" + count);
            if(this.socket == null) {
                this.socket = new Socket("localhost", port);
    
                this.os = new ObjectOutputStream(this.socket.getOutputStream());
                this.is = new ObjectInputStream(this.socket.getInputStream());
            }
        }catch(UncheckedIOException e) {
            System.out.println(e.getMessage() + "\n> Establishing connection with proxy 2...");
            this.doConnect(auxProxy, count);
        }catch(IOException e) {
            System.out.println(e.getMessage() + "\n> Establishing connection with proxy 2...");
            this.doConnect(auxProxy, count);
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    private void doDisconnect() {
        if(this.socket != null){
            try {
                this.os.close();
                this.os = null;
                this.is.close();
                this.is = null;
                this.socket.close();
                this.socket = null;
                this.done = false;
            }catch(UncheckedIOException e) {
                System.out.println(e.getMessage());
            }catch(IOException e) {
                System.out.println(e.getMessage());
            }catch(NullPointerException e) {
            	System.out.println(e.getMessage());
            }
        }
    }

    class InactiveProxy implements Runnable {
        private Client client;

        public InactiveProxy(Client client) {
            this.client = client;
        }

        @Override
        public void run() {
            try{
                File file = new File("ClientLatency.txt");
                long latency = 0;
                if(file.exists()){
                    Scanner scanner = new Scanner(file);
                    while(scanner.hasNext()){
                        latency = Long.valueOf(scanner.next());
                    }
                    scanner.close();
                }else {
                    throw new Exception("The file ClientLatency.txt does not exist");
                }
                Thread.sleep(latency);
                if(!this.client.done) this.client.doDisconnect();
            }catch(InterruptedException e){
                System.out.println(e.getMessage());
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }
}