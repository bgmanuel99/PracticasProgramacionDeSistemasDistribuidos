package PracticasDistribuidos.practica1Distribuidos.ejercicio2;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client2 {
    public static final String version = "1.0";

    private Socket socket;
    private ObjectOutputStream os;
    private ObjectInputStream is;

    public static void main(String[] args) {
        new Client2();
    }

    public void init(){

    }

    public Client2(){
        this.init();
    }
}
