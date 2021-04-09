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

    static void initFile(String name) {
    	try {    		
    		File file = new File(name);
    		PrintWriter outputfile = new PrintWriter(file);
    		if(name.contains("Central")){
                outputfile.print(500);
            }else if(name.contains("Client")) {
    			outputfile.print(1000);
    		}else if(name.contains("Proxy")) {
    			outputfile.print(500);
    		}else if(name.contains("Server")){
    			outputfile.print(0);
    		}
    		outputfile.close();
    	}catch (Exception e) {
    		System.out.println(e.getMessage());
    	}
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
