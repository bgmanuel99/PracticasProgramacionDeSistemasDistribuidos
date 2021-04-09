package PracticasDistribuidos.practica1Distribuidos.ejercicio3;

import java.io.File;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

class GlobalFunctions {
    static Cipher getCipher(boolean allowEncrypt) throws Exception {
        final String private_key = "idbwidbwjNFJERNFEJNFEJIuhifbewbaicaojopqjpu3873ºkxnmknmakKAQIAJ3981276396^=)(/&/(ISJ";
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(private_key.getBytes("UTF-8"));
        final SecretKeySpec key = new SecretKeySpec(digest.digest(), 0, 16, "AES");
    
        final Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
        if(allowEncrypt) {
            aes.init(Cipher.ENCRYPT_MODE, key);
        } else {
            aes.init(Cipher.DECRYPT_MODE, key);
        }
    
        return aes;
    }
    
    static synchronized void setLatency(long latency, int numberProxy) throws Exception {
        File file = new File("Proxy" + numberProxy + "Latency.txt");
        int i = 0;
        if(file.exists()){
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
                latency += Long.valueOf(scanner.next());
                i++;
            }
            scanner.close();
            PrintWriter outputFile = new PrintWriter(file);
            outputFile.print(latency/(i+1));
            outputFile.close();
        }else {
            throw new Exception("The file Proxy" + numberProxy + "Latency.txt does not exist");
        }
    }

    static synchronized long getLatency(int numberProxy) throws Exception {
        File file = new File("Proxy" + numberProxy + "Latency.txt");
        long latency = 300;
        if(file.exists()){
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
                latency = Long.valueOf(scanner.next());
            }
            scanner.close();
        }else {
            throw new Exception("The file Proxy" + numberProxy + "Latency.txt does not exist");
        }
        return latency;
    }

    static int getExternalVariables(String name) throws Exception {
        File file = new File("ExternalVariables.txt");
        if(file.exists()) {
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
    		if(name.equals("ClientLatency.txt")) {
    			outputfile.print(500);
    		}else if(name.contains("Proxy")) {
    			outputfile.print(300);
    		}else if(name.contains("Server")){
    			outputfile.print(0);
    		}
    		outputfile.close();
    	}catch (Exception e) {
    		System.out.println(e.getMessage());
    	}
    }
}