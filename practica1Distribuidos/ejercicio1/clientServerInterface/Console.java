package PracticasDistribuidos.practica1Distribuidos.ejercicio1.clientServerInterface;

import java.io.*;

import InterfazClienteServidor.Client;

public class Console {
    public static String prompt = "Cliente v " + Client.version + "> ";
    
    private InputStreamReader isr;
    private BufferedReader br;
    
    public Console(){
        this.isr = new InputStreamReader(System.in);
        this.br = new BufferedReader(isr);
    }

    public void writeMessage(String msg) {
        System.out.println("> " + msg);
    }
    
    public String getCommand() {
        String line = "";

        try {
            System.out.println(Console.prompt);
            line = this.br.readLine();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        
        return line;
    }

    public String[] getCommandDecrypt() {
        String [] message = new String[1];
        
        try{
            System.out.println("Introduce the message to decrypt: ");
            message[0] = this.br.readLine();
        }catch(IOException e) {
            System.out.println(e.getMessage());
        }

        return message;
    }
}
