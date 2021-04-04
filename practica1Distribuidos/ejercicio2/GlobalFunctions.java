package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import java.io.File;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class GlobalFunctions {
	static UserTable users = UserTable.getInstance();
	
    static Cipher getCipher(boolean allowEncrypt) throws Exception {
        final String private_key = "idbwidbwjNFJERNFEJNFEJIuhifbewbaicaojopqjpu3873�kxnmknmakKAQIAJ3981276396^=)(/&/(ISJ";
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
    
    static synchronized void addUser(byte [] name, byte [] password) throws Exception {
    	String users = "";
    	File file = new File("Users.txt");
        if(file.exists()) {
            Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
                users += scanner.nextLine() + "\n";
            }
            scanner.close();
            PrintWriter outputFile = new PrintWriter(file);
            outputFile.print(users);
            for(int i = 0; i < name.length; i++) {
            	if(i == name.length-1) outputFile.print(name[i]);
            	else outputFile.print(name[i] + " ");
            }
            outputFile.print("/");
            for(int i = 0; i < password.length; i++) {
            	if(i == password.length-1) outputFile.print(password[i]);
            	else outputFile.print(password[i] + " ");
            }
            outputFile.close();
        }else {
            throw new Exception("The file "+file.getName()+" does not exist");
        }
    }
    
    static synchronized boolean isUser(String name) throws Exception {
    	File file = new File("Users.txt");
        if(file.exists()) {
            @SuppressWarnings("resource")
			Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
            	String [] encryptedName = scanner.nextLine().split("/")[0].split(" ");
                byte [] nameInByte = new byte[encryptedName.length];
                for(int i = 0; i < encryptedName.length; i++) {
                	nameInByte[i] = Byte.valueOf(encryptedName[i]);
                }
                if(GlobalFunctions.decrypt(nameInByte).equals(name)) {
                	return true;
                }
            }
            scanner.close();
        }else {
            throw new Exception("The file " + file.getName() + " does not exist");
        }
        
        return false;
    }
    
    static synchronized String getPassword(String name) throws Exception {
    	File file = new File("Users.txt");
        if(file.exists()) {
            @SuppressWarnings("resource")
			Scanner scanner = new Scanner(file);
            while(scanner.hasNext()){
            	String [] encryptedPair = scanner.nextLine().split("/");
            	String [] encryptedName = encryptedPair[0].split(" ");
            	String [] encryptedPassword = encryptedPair[1].split(" ");
                byte [] nameInByte = new byte[encryptedName.length], passwordInByte = new byte[encryptedPassword.length];
                for(int i = 0; i < encryptedName.length; i++) nameInByte[i] = Byte.valueOf(encryptedName[i]);
                for(int i = 0; i < encryptedPassword.length; i++) passwordInByte[i] = Byte.valueOf(encryptedPassword[i]);
                if(GlobalFunctions.decrypt(nameInByte).equals(name)) {
                	return GlobalFunctions.decrypt(passwordInByte);
                }
            }
            scanner.close();
        }else {
            throw new Exception("The file " + file.getName() + " does not exist");
        }
        
        return "";
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
    
    static synchronized void insertPair(String userName, ObjectOutputStream os, Socket socket) throws NullPointerException {
    	users.insertUser(userName, socket);
    	users.insertOs(userName, os);
    }

    static synchronized void deleteUser(String userName) throws NullPointerException {
        users.deleteUser(userName);
    }

    static synchronized ObjectOutputStream getOs(String name) throws NullPointerException{
        return users.getOs(name);
    }
    
    static synchronized Socket getSocket(String name) throws NullPointerException {
    	return users.getSocket(name);
    }
    
    static synchronized ObjectOutputStream [] getOsContacts(String[] contacts) throws NullPointerException{
        return users.getOsContacts(contacts);
    }
}
