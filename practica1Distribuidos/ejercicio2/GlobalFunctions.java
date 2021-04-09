package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class GlobalFunctions {
    static int getExternalVariables(String name) throws Exception {
        File file = new File("ExternalVariables.txt");
        if(file.exists()) {
            @SuppressWarnings("resource")
			Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
                String [] port = scanner.nextLine().split(" ");
                if(port[0].equals(name)) return Integer.valueOf(port[1]);
            }
            scanner.close();
        }else {
            throw new Exception("The file ExternalVariables.txt does not exist");
        }
        if(name.equals("MAXSERVER")) return 0;
        return 8000;
    }

    static synchronized void initFile() throws Exception{
        File file=new File("GlobalSleep.txt");
        PrintWriter pw=new PrintWriter(file);
        for(int  i = 0; i < 10;i++){
            pw.println("nosleep");
        }
        pw.close();

    }

    static synchronized void doMoveRobot(String move, int node) throws Exception {
        File file;
        PrintWriter pw;
        if(node == 0) {
            file = new File("SpecialNode.txt");
        }else{
            file = new File("NormalNode" + node + ".txt");
        }

        String [] joint = new String[6];
        int i = 0;
        if(file.exists()) {
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()) {
            	String scan = scanner.nextLine();
                joint[i] = scan;
                i++;
            }
            
            if(move.equals("ROTATE")) {
                for(int j = 0; j < joint.length; j++) {
                    if(joint[j].equals("NORTH")) joint[j] = "EAST";
                    else if(joint[j].equals("EAST")) joint[j] = "SOUTH";
                    else if(joint[j].equals("SOUTH")) joint[j] = "WEST";
                    else if(joint[j].equals("WEST")) joint[j] = "NORTH";
                }
            }else if(move.equals("TRASLATE")) {
                for(int j = 1; j < joint.length; j++) {
                    String aux = joint[0];
                    joint[0] = joint[j];
                    joint[j] = aux;
                }
            }

            scanner.close();

            pw = new PrintWriter(file);
            for(int k = 0; k < joint.length; k++) {
                pw.println(joint[k]);
            }

            pw.close();
        }else {
            throw new Exception("The file " + file.getName() + " does not exist");
        }
    }

    static synchronized boolean isSleeping(int index) throws Exception {
        File file = new File("GlobalSleep.txt");
        
        if(!file.exists()) throw new Exception("The file " + file.getName() + " does not exists");
        
        String [] state = new String[10];
        int i = 0;
        
        Scanner scanner = new Scanner(file);
        while(scanner.hasNext()) {
            state[i] = scanner.nextLine();
            i++;
        }

        scanner.close();
        
        if(state[index].equals("nosleep")) return false;
        return true;
    }

    @SuppressWarnings("resource")
	static synchronized void setSleeping(int index) throws Exception {
        File file = new File("GlobalSleep.txt");

        if(!file.exists()) throw new Exception("The file " + file.getName() + " does not exists");
        
        String state [] = new String[10];
        int i = 0;
        
        Scanner scanner = new Scanner(file);
        while(scanner.hasNext()) {
            state[i] = scanner.nextLine();
            i++;
        }
        
        if(state[index].equals("nosleep")) state[index] = "sleep";

        scanner.close();
        
        PrintWriter pw = new PrintWriter(file);
        for(int k = 0; k < state.length; k++) {
            pw.println(state[k]);
        }
        pw.close();
    }

    static synchronized void setLatency(long latency, int number, String type) throws Exception {
        File file = new File(type + number + "Latency.txt");
        if(file.exists()){
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
                latency += Long.valueOf(scanner.next());
            }
            scanner.close();
            PrintWriter outputFile = new PrintWriter(file);
            outputFile.print(latency/2);
            outputFile.close();
        }else {
            throw new Exception("The file " + file.getName() + " does not exist");
        }
    }

    static synchronized void setLatency(long latency, int number) throws Exception {
        File file = new File("Client" + number + "CentralLatency.txt");
        if(file.exists()){
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
                latency += Long.valueOf(scanner.next());
            }
            scanner.close();
            PrintWriter outputFile = new PrintWriter(file);
            outputFile.print(latency/2);
            outputFile.close();
        }else {
            throw new Exception("The file " + file.getName() + " does not exist");
        }
    }

    static synchronized long getLatency(int number, String type) throws Exception {
        File file = new File(type + number + "Latency.txt");
        long latency = 0;
        if(type.equals("Proxy")) latency = 300;
        else latency = 1000;
        if(file.exists()){
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
                latency = Long.valueOf(scanner.next());
            }
            scanner.close();
        }else {
            throw new Exception("The file " + file.getName() + " does not exist");
        }
        return latency;
    }

    static synchronized long getLatency(int number) throws Exception {
        File file = new File("Client" + number + "CentralLatency.txt");
        long latency = 500;
        if(file.exists()){
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
                latency = Long.valueOf(scanner.next());
            }
            scanner.close();
        }else {
            throw new Exception("The file " + file.getName() + " does not exist");
        }
        return latency;
    }
}
