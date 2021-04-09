package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import java.io.*;

public class Console {
    public static String prompt;
    private InputStreamReader isr;
    private BufferedReader br;
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
