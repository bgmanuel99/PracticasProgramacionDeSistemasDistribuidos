package PracticasDistribuidos.practica1Distribuidos.ejercicio1.clientServerInterface;

import PracticasDistribuidos.practica1Distribuidos.ejercicio1.protocol.ControlRequest;
import PracticasDistribuidos.practica1Distribuidos.ejercicio1.protocol.ControlResponse;
import java.io.*;
import java.net.*;


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
                String [] message = this.console.getCommandDecrypt();
                this.console.writeMessage("This is your message: " + message[0]);
                this.doConnect(Integer.valueOf(System.getenv("PORT")));
                this.doDecrypt(message);
                this.doDisconnect();
            }else if(cmd.equals("ranking")){
                this.doConnect(Integer.valueOf(System.getenv("PORT")));
                this.doRanking();
                this.doDisconnect();
            }
        }
    }

    private void doDecrypt(String [] message) {
        try{
            ControlRequest cr = new ControlRequest("OP_DECRYPT");
            cr.getArgs().add(message[0]);
            this.os.writeObject(cr);
            
            Thread inactiveProxy = new Thread(new InactiveProxy(this));
            inactiveProxy.start();
            ControlResponse crs = (ControlResponse) this.is.readObject();
            this.done = true;
            if(crs.getSubtype().equals("OP_DECRYPT_OK")) {
                this.console.writeMessage("Data sended");
            }else {
                this.console.writeMessage("The proxy is disconn");
            }
        }catch(ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    private void doRanking() {
        try {
            ControlRequest crRanking = new ControlRequest("OP_RANKING");
            this.os.writeObject(crRanking);

            ControlResponse crsRanking = (ControlResponse) this.is.readObject();
            this.done = true;
            if(crsRanking.getSubtype().equals("OP_RANKING_OK")){
                this.console.writeMessage(crsRanking.getArgs().get(0).toString());
            }else if(crsRanking.getSubtype().equals("OP_RANKING_NOK")){
                this.console.writeMessage("The proxy is a bit shy");
            }else{
                this.console.writeMessage("The proxy is disconnected");
            }
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        
    }
}
    
    private void doConnect(int port) {
        try{
            if(this.socket != null) {
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
        if(this.socket!=null){
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
                Thread.sleep(100);
                if(!this.client.done) this.client.doDisconnect();
            }catch(InterruptedException e){
                System.out.println(e.getMessage());
            }
        }
    }
}
