package PracticasDistribuidos.practica1Distribuidos.ejercicio1.clientServerInterface;

import PracticasDistribuidos.practica1Distribuidos.ejercicio1.protocol.*;
import java.io.*;
import java.net.*;
import javax.crypto.Cipher;


public class Client {
    public static final String version = "1.0";

    private Console console;
    private Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private boolean done;

    public static void main(String [] args) {
        new Client();
    }

    public void init() {
        this.console = new Console();
        this.done = false;
    }
    
    public Client(){
        this.init();

        String cmd= this.console.getCommand();
        
        while(!cmd.equals("close")){
            if(cmd.equals("decrypt")){
                String message = this.console.getCommandDecrypt();
                this.console.writeMessage("This is your message: " + message);
                this.doConnect(8000);
                this.doDecrypt(message);
                this.doDisconnect();
            }else if(cmd.equals("ranking")){
                this.doConnect(8000);
                this.doRanking();
                this.doDisconnect();
            }
            
            cmd = this.console.getCommand();
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
        final Cipher aes = Encrypt.getCipher(true);
        final byte[] encryptedMessage = aes.doFinal(bytes);
        return encryptedMessage;
    }
    
    private void doConnect(int port) {
        try{
            if(this.socket == null) {
                this.socket = new Socket("localhost", port);
    
                this.os = new ObjectOutputStream(this.socket.getOutputStream());
                this.is = new ObjectInputStream(this.socket.getInputStream());
            }
        }catch(UncheckedIOException e) {
            System.out.println(e.getMessage());
        }catch(IOException e) {
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
                Thread.sleep(200);
                if(!this.client.done) this.client.doDisconnect();
            }catch(InterruptedException e){
                System.out.println(e.getMessage());
            }
        }
    }
}
