package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import PracticasDistribuidos.practica1Distribuidos.protocol.*;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.Scanner;

public class Client1 {
    public final String version = "1.0";

    private Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Console console;
    private boolean done;
    private int maxProxy = 0;

    public static void main(String[] args) {
        GlobalFunctions.initFile("ClientLatency.txt");
    	GlobalFunctions.initFile("Server1Ranking.txt");
    	GlobalFunctions.initFile("Server2Ranking.txt");
    	GlobalFunctions.initFile("Proxy1Latency.txt");
    	GlobalFunctions.initFile("Proxy2Latency.txt");
        new Client1();
    }

    public void init() {
        try{
            this.console = new Console(this.version);
            this.done = false;
            this.maxProxy = GlobalFunctions.getExternalVariables("MAXPROXY");
            new Messages(this);
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Client1(){
        this.init();

        if(this.maxProxy != 0){
            String cmd = this.console.getCommand();

            while(!cmd.equals("close")) {
                try{
                    if(cmd.equals("register")) {
                        String [] credentials = this.console.getCommandRegister();
                        this.console.writeMessage("This is your user name and password: " + credentials[0] + " " + credentials[1]);
                        this.doConnect(GlobalFunctions.getExternalVariables("PORTPROXY1"), 1);
                        this.doRegister(credentials);
                    }else if(cmd.equals("login")) {
                        String [] credentials = this.console.getCommandLogin();
                        this.console.writeMessage("This are your credentials: " + credentials[0] + " " + credentials[1]);
                        this.doConnect(GlobalFunctions.getExternalVariables("PORTPROXY1"), 1);
                        this.doLogin(credentials);
                    }else if(cmd.equals("message")) {
                        String [] message = this.console.getCommandMessage();
                        this.console.writeMessage("This is yout message: " + message[0] + ", and this is the person you wanna send it: " + message[1]);
                        this.doConnect(GlobalFunctions.getExternalVariables("PORTPROXY1"), 1);
                        this.doSendMessage(message);
                    }else if(cmd.equals("broadcasting")) {
                        this.console.writeMessage("Making the broadcast to your contacts...");
                        this.doConnect(GlobalFunctions.getExternalVariables("PORTPROXY1"), 1);
                        this.doBroadcasting();
                    }else if(cmd.equals("logout")) {
                        this.console.writeMessage("Disconnecting from the client...");
                        break;
                    }
                }catch(Exception e) {
                    System.out.println(e.getMessage());
                }
                
                cmd = this.console.getCommand();
            }
        }

        this.doDisconnect();
    }

    private void doRegister(String [] credentials) {
        try {
            ControlRequest cr = new ControlRequest("OP_REGISTER");
            cr.getArgs().add(GlobalFunctions.encryptMessage(credentials[0]));
            cr.getArgs().add(GlobalFunctions.encryptMessage(credentials[1]));

            this.os.writeObject(cr);

            Thread inactiveProxy = new Thread(new InactiveProxy(this));
            inactiveProxy.start();
        }catch(ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }catch(IOException e) {
        	this.console.writeMessage("An error has ocurred: The proxy is a bit shy");
        }catch (Exception e) {
        	System.out.println(e.getMessage());
		}
    }

    private void doLogin(String [] credentials) {

    }

    private void doBroadcasting() {

    }

    private void doSendMessage(String [] message) {

    }

    private void doConnect(int port,int count){
        int auxProxy = 0;
        try {
            if(count > this.maxProxy) throw new Exception("All the proxys are disconnected");
            count++;
            auxProxy = GlobalFunctions.getExternalVariables("PORTPROXY" + count);
            if(this.socket == null){
                this.socket=new Socket("localhost",port);
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

    private void doDisconnect(){
        if(this.socket!=null){
            try{
                this.os.close();
                this.os = null;
                this.is.close();
                this.is = null;
                this.socket.close();
                this.socket = null;
            }catch(UncheckedIOException e) {
                System.out.println(e.getMessage());
            }catch(IOException e) {
                System.out.println(e.getMessage());
            }catch(NullPointerException e) {
            	System.out.println(e.getMessage());
            }
        }
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public ObjectOutputStream getOs() {
        return this.os;
    }

    public void setOs(ObjectOutputStream os) {
        this.os = os;
    }

    public ObjectInputStream getIs() {
        return this.is;
    }

    public void setIs(ObjectInputStream is) {
        this.is = is;
    }

    public Console getConsole() {
        return this.console;
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    public boolean isDone() {
        return this.done;
    }

    public boolean getDone() {
        return this.done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public int getMaxProxy() {
        return this.maxProxy;
    }

    public void setMaxProxy(int maxProxy) {
        this.maxProxy = maxProxy;
    }

    public void connect(int port, int count){
        this.doConnect(port, count);
    }
    public void disconnect(){
    	System.out.println("1");
        this.doDisconnect();
    }
}

class Messages extends Thread {
    private Client1 client;

    public Messages(Client1 client) {
        this.client = client;
        this.start();
    }
    
    @Override
    public void run() {
        while(true){
        	try{
                if(this.client.getSocket()==null){
                    this.client.connect(GlobalFunctions.getExternalVariables("PORTPROXY1"),1);
                }
                System.out.print("Waiting...");
                ControlResponse crs = (ControlResponse) this.client.getIs().readObject();
                this.client.setDone(true);
                System.out.println("Hilo uno: " + this.client.getDone());
                
                if(crs.getSubtype().equals("LOGIN_OK")){
                    this.client.getConsole().writeMessage(crs.getArgs().get(0).toString());
                }else if(crs.getSubtype().equals("LOGIN_NOK")){
                    this.client.getConsole().writeMessage(crs.getArgs().get(0).toString());
                }else if(crs.getSubtype().equals("LOGOUT_OK")){
                    this.client.getConsole().writeMessage(crs.getArgs().get(0).toString());
                }else if(crs.getSubtype().equals("NEW_MESSAGE")){

                }else if(crs.getSubtype().equals("CONTACT_CONNECTED")){
                    this.client.getConsole().writeMessage(crs.getArgs().get(0).toString());
                }else if(crs.getSubtype().equals("REGISTER_OK")){
                    this.client.getConsole().writeMessage(crs.getArgs().get(0).toString());
                }else if(crs.getSubtype().equals("REGISTER_NOK")){
                    this.client.getConsole().writeMessage(crs.getArgs().get(0).toString());
                }
                this.client.disconnect();
            }catch (IOException e) {
            	System.out.println(e.getMessage());
            }catch (Exception e) {
            	System.out.println(e.getMessage());
            }
        }
    }
}

class InactiveProxy implements Runnable {
    private Client1 client;

    public InactiveProxy(Client1 client) {
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
            if(!this.client.getDone()) this.client.disconnect();
            this.client.setDone(false);
        }catch(InterruptedException e){
            System.out.println("Interrupted exception: " + e.getMessage());
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
