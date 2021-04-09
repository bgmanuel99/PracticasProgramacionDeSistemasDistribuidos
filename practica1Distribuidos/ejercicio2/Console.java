package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import java.io.*;

public class Console {
    public static String prompt;
    private InputStreamReader isr;
    private BufferedReader br;
    public static String getPrompt() {
		return prompt;
	}

	public static void setPrompt(String prompt) {
		Console.prompt = prompt;
	}

	public InputStreamReader getIsr() {
		return isr;
	}

	public void setIsr(InputStreamReader isr) {
		this.isr = isr;
	}

	public BufferedReader getBr() {
		return br;
	}

	public void setBr(BufferedReader br) {
		this.br = br;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	private String version;

    public Console(String version){
    	System.out.println("Consola iniciada");
        this.isr = new InputStreamReader(System.in);
        this.br = new BufferedReader(this.isr);
        this.version = version;
        prompt = "Console v " + this.version + "> ";
    }
    
    public synchronized void writeMessage(String msg){
    	
        System.out.println("> " + msg);
    }

    public String getCommand(){
        String line = "";
       
        try {
            System.out.println(Console.prompt);
            line = this.br.readLine();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return line;
    }

    public int getCommandStopRobot() {
        String line = "";

        try {
            System.out.println("What robot do you want to stop?");
            line = this.br.readLine();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return Integer.valueOf(line);
    }
}
