package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Hashtable;

public class UserTable {
    private static UserTable userTable = null;
    private Hashtable<String, Socket> sockets;
    private Hashtable<String, ObjectOutputStream> os;

    public UserTable() {
        this.sockets = new Hashtable<String, Socket>();
        this.os=new Hashtable<String, ObjectOutputStream>();
    }
    
    public synchronized static UserTable getInstance() {
        if(userTable == null) {
        	System.out.println("no hay instancias de UserTable");
        	userTable= new UserTable();
        }
        	
        return userTable;
    }

    public void insertUser(String userName, Socket socket) {
    	System.out.println("userTable insertUser"+this.sockets.size());
        this.sockets.put(userName, socket);
        System.out.println(this.sockets.get(userName).toString());
    }

    public void insertOs(String userName,ObjectOutputStream os) {
    	this.os.put(userName, os);
    }

    public void deleteUser(String userName) {
        this.sockets.remove(userName);
    }
    
    public ObjectOutputStream getOs(String name){
    	return this.os.get(name);
    }

    public Socket getSocket(String userName) {
    	System.out.println("userTable getSocket "+this.sockets.size()+" "+this.sockets.get(userName).toString());
        return this.sockets.get(userName);
    }

    public Socket[] getContacts(String [] userNames) {
        Socket [] contacts = new Socket[userNames.length];
        for(int i = 0; i < userNames.length; i++){
            if(this.sockets.get(userNames[i]) != null) contacts[i]=this.sockets.get(userNames[i]);
        }
        return contacts;
    }
    
    public ObjectOutputStream [] getOsContacts(String [] userNames) {
    	ObjectOutputStream [] os = new ObjectOutputStream[userNames.length];
        for(int i = 0; i < userNames.length; i++){
            if(this.sockets.get(userNames[i]) != null) os[i]=this.os.get(userNames[i]);
        }
        return os;
    }
}
