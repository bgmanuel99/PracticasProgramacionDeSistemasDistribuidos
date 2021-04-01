package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import java.io.File;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class GlobalFunctions {
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

    static byte[] encryptMessage(String message) throws Exception {
        final byte[] bytes = message.getBytes("UTF-8");
        final Cipher aes = GlobalFunctions.getCipher(true);
        final byte[] encryptedMessage = aes.doFinal(bytes);
        return encryptedMessage;
    }

    static String decrypt(byte [] encryptedMessage) throws Exception {
        final Cipher aes = GlobalFunctions.getCipher(false);
        final byte [] bytes = aes.doFinal(encryptedMessage);
        final String message = new String(bytes, "UTF-8");
        return message;
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
    		if(name.contains("Central")){
                outputfile.print(500);
            }else if(name.contains("Client")) {
    			outputfile.print(1000);
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

    static synchronized void insertUser(String userName, Socket socket) throws NullPointerException {
        UserTable users = UserTable.getInstance();
        users.insertUser(userName, socket);
    }

    static synchronized void deleteUser(String userName) throws NullPointerException {
        UserTable users = UserTable.getInstance();
        users.deleteUser(userName);
    }

    static synchronized Socket getSocket(String userName) throws NullPointerException{
        UserTable users = UserTable.getInstance();
        return users.getSocket(userName);
    }

    static synchronized Socket [] getContacts(String[] contacts) throws NullPointerException{
        UserTable users = UserTable.getInstance();
        return users.getContacts(contacts);
    }
}
