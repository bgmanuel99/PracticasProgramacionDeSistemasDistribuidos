package ejercicio3;


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
        prompt = "Cliente v " + this.version + "> ";
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

   
}
