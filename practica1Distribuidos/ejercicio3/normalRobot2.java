package ejercicio3;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import protocol.ControlRequest;
import protocol.ControlResponse;
import protocol.Request;

public class normalRobot2 {

	public static void main(String[] args) {
		new mainRobot3();

	}

}


class mainRobot3{
	
	private Socket in,  robotRight;
	private ObjectOutputStream osLeft, osRight;
	private ObjectInputStream isLeft, isRight;
	
	public mainRobot3() {
		while(true) {
			try {
				//esperando una llamada entrante
				ServerSocket listen = new ServerSocket(4002);
				System.out.println("Robot2 esperando instrucciones");
				this.in = listen.accept();
				System.out.println("Connexion acceptada desde el robot de la izquierda: "+this.in.toString());
				this.isLeft = new ObjectInputStream(this.in.getInputStream());
				this.osLeft = new ObjectOutputStream(this.in.getOutputStream());
				//Esperamos el comando
				Request r = (Request) this.isLeft.readObject();
				if(r.getType().equals("CONTROL_REQUEST")) {
					ControlRequest cr = (ControlRequest) r;
					System.out.println(cr.getSubtype());
					//Hemos recibido la orden desde el cliente
					//this.robotRight = new Socket("localhost",4001);
					//this.osRight = new ObjectOutputStream(this.robotRight.getOutputStream());
					
					//this.osRight.writeObject(cr);
					//System.out.println("Esperando respuesta...");
					//this.isRight.readObject();
					ControlResponse crs = new ControlResponse("MOVE_DONE");
					crs.getArgs().add("Move done");
					this.osLeft.writeObject(crs);
					System.out.println("Enviando respuesta");
					//Hemos transmitido la operacion al siguiente robot del anillo
					//Cerramos conexiones de nuevo y esperamos nuevas ordenes;
					if(this.in !=null) {
						this.in.close();
						this.in = null;
						//this.osRight.close();
						//this.osRight=null;
						this.osLeft.close();
						this.osLeft=null;
						this.isLeft.close();
						this.isLeft=null;
						this.robotRight.close();
						this.robotRight = null;
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	
}

class threadRobot3 extends Thread{
	private mainRobot maRobot;
}