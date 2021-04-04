package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import java.io.*;

public class Console {
    public static String prompt;
	private InputStreamReader isr;
    private BufferedReader br;
    private String version, nick = "v";

	public Console(String version){
        this.isr = new InputStreamReader(System.in);
        this.br = new BufferedReader(this.isr);
        this.version = version;
        prompt = "Cliente " + this.nick + " " + this.version + "> ";
    }
    
    public synchronized void writeMessage(String msg){
        System.out.println("> " + msg);
    }

    public String getCommand(){
        String line = "";

        try {
            System.out.print(Console.prompt);
            line = this.br.readLine();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return line;
    }

    public String[] getCommandRegister() {
        String [] credentials = new String[2];

        try {
            System.out.print("Choose a user name: ");
            credentials[0] = this.br.readLine();

            System.out.print("Choose a password: ");
            credentials[1] = this.br.readLine();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        
        return credentials;
    }


    public String [] getCommandLogin(){
        String [] credentials = new String[2];

        try {
            System.out.print("User name: ");
            credentials[0] = this.br.readLine();

            System.out.print("Password: ");
            credentials[1] = this.br.readLine();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        return credentials;
    }

    public String[] getCommandMessage() {
        String [] message = new String[2];

        try{
            System.out.print("Introduce de message: ");
            message[0] = this.br.readLine();

            System.out.print("Choose the user to send the message: ");
            message[1] = this.br.readLine();
        }catch(Exception ex) {
            System.out.println(ex.getMessage());
        }

        return message;
    }
    
    public String[] getCommandBroadcasting() {
    	String [] message= null;
        try{
        	System.out.print("How many users?: ");
        	int size = Integer.valueOf(this.br.readLine());
        	
        	message = new String[Integer.valueOf(size)];
        	System.out.println("Choose the contacts: ");
            for(int i = 0; i < size; i++) {
            	System.out.print((i+1) + "> ");
            	message[i] = this.br.readLine();
            }
        }catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
        return message;
    }

	public void setPrompt(String nick, String version) {
		if(nick == null) nick = "v";
		else if(nick != "v"){
			if(nick.length() == 1) nick = nick.toUpperCase();
			else nick = String.valueOf(nick.charAt(0)).toUpperCase() + nick.substring(1, nick.length());
		}
		if(version == null) version = "1.0";
				
		Console.prompt = "Client " + nick + " " + version + "> ";
	}
}
