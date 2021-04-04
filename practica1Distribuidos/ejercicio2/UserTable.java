package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Hashtable;

public class UserTable {
	public static UserTable userTable = null;
	private Hashtable<String, Socket> sockets;
    private Hashtable<String, ObjectOutputStream> os;

    public UserTable() {
    	this.sockets = new Hashtable<String, Socket>();
        this.os = new Hashtable<String, ObjectOutputStream>();
    }
    
    public synchronized static UserTable getInstance() {
    	if(userTable == null) userTable = new UserTable();
        return userTable;
    }

    public void insertUser(String userName, Socket socket) {
        this.sockets.put(userName, socket);
    }
    
    public void insertOs(String userName, ObjectOutputStream os) {
    	this.os.put(userName, os);
    }

    public void deleteUser(String userName) {
        this.sockets.remove(userName);
    	this.os.remove(userName);
    }
    
    public Socket getSocket(String name) {
    	return this.sockets.get(name);
    }
    
    public ObjectOutputStream getOs(String name){
    	return this.os.get(name);
    }
    
    public ObjectOutputStream [] getOsContacts(String [] userNames) {
    	ObjectOutputStream [] os = new ObjectOutputStream[userNames.length];
        for(int i = 0; i < userNames.length; i++){
            if(this.os.get(userNames[i]) != null) os[i] = this.os.get(userNames[i]);
        }
        return os;
    }
}
