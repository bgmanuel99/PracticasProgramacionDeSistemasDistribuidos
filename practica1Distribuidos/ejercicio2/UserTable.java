package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import java.net.Socket;
import java.util.Hashtable;

public class UserTable {
    private static  UserTable userTable;
    private Hashtable<String, Socket> sockets;

    public UserTable() {
        this.sockets = new Hashtable<String, Socket>();
    }
    
    public synchronized static UserTable getInstance() {
        if(userTable == null) return new UserTable();
        else return userTable;
    }

    public void insertUser(String userName, Socket socket) {
        this.sockets.put(userName, socket);
    }

    public void deleteUser(String userName) {
        this.sockets.remove(userName);
    }

    public Socket getSocket(String userName) {
        return this.sockets.get(userName);
    }

    public Socket[] getContacts(String [] userNames) {
        Socket [] contacts = new Socket[userNames.length];
        for(int i = 0; i < userNames.length; i++){
            if(this.sockets.get(userNames[i]) != null) contacts[i]=this.sockets.get(userNames[i]);
        }
        return contacts;
    }
}
