package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import PracticasDistribuidos.practica1Distribuidos.protocol.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;

public class Client2 {
    public final String version = "1.0";

    private Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private Console console;
    private boolean done;
    private int maxProxy = 0;
    private String nick;
    private long start, end;
    private int numberClient;

	public static void main(String[] args) {
        GlobalFunctions.initFile("Client1Latency.txt");
    	GlobalFunctions.initFile("Server1Ranking.txt");
    	GlobalFunctions.initFile("Server2Ranking.txt");
    	GlobalFunctions.initFile("Proxy1Latency.txt");
    	GlobalFunctions.initFile("Proxy2Latency.txt");
        new Client2(2);
    }

    public void init(int numberClient) {
        try{
            this.console = new Console(this.version);
            this.done = false;
            this.maxProxy = GlobalFunctions.getExternalVariables("MAXPROXY");
            this.nick = "";
            start = 0;
            end = 0;
            this.numberClient = numberClient;
            new Messages2(this);
        }catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public Client2(int numberClient){
        this.init(numberClient);

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
                        if(this.nick == ""){
                            String [] credentials = this.console.getCommandLogin();
                            this.console.writeMessage("This are your credentials: " + credentials[0] + " " + credentials[1]);
                            this.doConnect(GlobalFunctions.getExternalVariables("PORTPROXY1"), 1);
                            this.doLogin(credentials);
                        }else throw new Exception("There is another user connected");
                    }else if(cmd.equals("message")) {
                        String [] message = this.console.getCommandMessage();
                        this.console.writeMessage("This is yout message: " + message[0] + ", and this is the person you wanna send it: " + message[1]);
                        this.doConnect(GlobalFunctions.getExternalVariables("PORTPROXY1"), 1);
                        this.doSendMessage(message);
                    }else if(cmd.equals("broadcasting")) {
                        if(this.nick != "") {
                            this.console.writeMessage("Making the broadcast to your contacts...");
                            this.doConnect(GlobalFunctions.getExternalVariables("PORTPROXY1"), 1);
                            this.doBroadcasting();
                        }else throw new Exception("If you dont connect you can not broadcast, is logical");
                    }else if(cmd.equals("logout")) {
                        if(this.nick != ""){
                            this.nick = "";
                            this.console.writeMessage("Disconnecting from the client...");
                            break;
                        }else throw new Exception("You were already disconnected");
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
        	start = System.currentTimeMillis();
            ControlRequest cr = new ControlRequest("OP_REGISTER");
            cr.getArgs().add(GlobalFunctions.encryptMessage(credentials[0]));
            cr.getArgs().add(GlobalFunctions.encryptMessage(credentials[1]));

            this.os.writeObject(cr);
            
            Thread inactiveProxy = new Thread(new InactiveProxy2(this));
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
        try {
            ControlRequest cr = new ControlRequest("OP_LOGIN");
            cr.getArgs().add(GlobalFunctions.encryptMessage(credentials[0]));
            cr.getArgs().add(GlobalFunctions.encryptMessage(credentials[1]));

            this.os.writeObject(cr);
            this.nick = credentials[0];

            Thread inactiveProxy = new Thread(new InactiveProxy2(this));
            inactiveProxy.start();
        }catch(ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }catch(IOException e) {
        	this.console.writeMessage("An error has ocurred: The proxy is a bit shy");
            this.nick = "";
        }catch (Exception e) {
        	System.out.println(e.getMessage());
		}
    }

    private void doBroadcasting() {
        try {
            ControlRequest cr = new ControlRequest("OP_BROADCASTING");
            cr.getArgs().add(this.nick);
            this.os.writeObject(cr);

            Thread inactiveProxy = new Thread(new InactiveProxy2(this));
            inactiveProxy.start();
        }catch(IOException e) {
        	this.console.writeMessage("An error has ocurred: The proxy is a bit shy");
        }catch (Exception e) {
        	System.out.println(e.getMessage());
		}
    }

    private void doSendMessage(String [] message) {
        try {
            ControlRequest cr = new ControlRequest("OP_MESSAG");
            cr.getArgs().add(GlobalFunctions.encryptMessage(message[0]));
            cr.getArgs().add(GlobalFunctions.encryptMessage(message[1]));
            this.os.writeObject(cr);

            Thread inactiveProxy = new Thread(new InactiveProxy2(this));
            inactiveProxy.start();
        }catch(IOException e) {
        	this.console.writeMessage("An error has ocurred: The proxy is a bit shy");
        }catch (Exception e) {
        	System.out.println(e.getMessage());
		}
    }

    public void doConnect(int port, int count){
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

    public void doDisconnect(){
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

    public String getVersion() {
        return this.version;
    }

    public String getNick() {
        return this.nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
    
    public long getStart() {
		return this.start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return this.end;
	}

	public void setEnd(long end) {
		this.end = end;
	}
	
	public int getNumberClient() {
		return numberClient;
	}

	public void setNumberClient(int numberClient) {
		this.numberClient = numberClient;
	}
}

class Messages2 extends Thread {
    private Client2 client;

    public Messages2(Client2 client) {
        this.client = client;
        this.start();
    }
    
    @Override
    public void run() {
        while(true){
        	try{
                if(this.client.getSocket()==null){
                    this.client.doConnect(GlobalFunctions.getExternalVariables("PORTPROXY1"),1);
                }
                
                ControlResponse crs = (ControlResponse) this.client.getIs().readObject();
                this.client.setDone(true);
                
                if(crs.getSubtype().equals("NEW_MESSAGE")){
                    
                }else if(crs.getSubtype().equals("LOGIN_NOK")) {
                    this.client.setNick("");
                }else if(crs.getSubtype().equals("REGISTER_OK")) {
                	this.client.setEnd(System.currentTimeMillis());
                	GlobalFunctions.setLatency((this.client.getEnd() - this.client.getStart()), this.client.getNumberClient(), "Client");
        		}

                this.client.getConsole().writeMessage(crs.getArgs().get(0).toString());
                this.client.doDisconnect();
            }catch (IOException e) {
                System.out.println("IOException (Messages run): " + e.getMessage());
            }catch (Exception e) {
                System.out.println("Exception (Messages run): " + e.getMessage());
            }
        }
    }
}

class InactiveProxy2 implements Runnable {
    private Client2 client;

    public InactiveProxy2(Client2 client) {
        this.client = client;
    }

    @Override
    public void run() {
    	long sleep = 300;

        try{
            sleep = GlobalFunctions.getLatency(this.client.getNumberClient(), "Client");
        }catch(Exception e) {
            System.out.println("Exception (Masking run 1): " + e.getMessage());
        }
        
        try{
        	System.out.println(sleep);
            Thread.sleep(sleep);
            if(!this.client.getDone()) {
            	this.client.doDisconnect();
            	GlobalFunctions.setLatency(GlobalFunctions.getLatency(this.client.getNumberClient()*2, "Client"), this.client.getNumberClient(), "Client");
            }
            this.client.setDone(false);
        }catch(InterruptedException e){
            System.out.println("Interrupted exception: " + e.getMessage());
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
