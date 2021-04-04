package ejercicio3;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import protocol.*;

public class SpecialNode {

	public static void main(String[] args) {
		new mainRobot();

	}
	

}
class mainRobot{
	
	private Socket in,  robotRight;
	private ObjectOutputStream osConsole, osRight;
	private ObjectInputStream isConsole, isRight;

	public mainRobot() {
		while(true) {
			try {
				//esperando una llamada entrante
				ServerSocket listen = new ServerSocket(4000);
				System.out.println("RobotEspecial esperando instrucciones");
				this.in = listen.accept();
				System.out.println("Connexion acceptada desde la console: "+this.in.toString());
				this.isConsole = new ObjectInputStream(this.in.getInputStream());
				this.osConsole = new ObjectOutputStream(this.in.getOutputStream());
				//Esperamos el comando
				Request r = (Request) this.isConsole.readObject();
				if(r.getType().equals("CONTROL_REQUEST")) {
					ControlRequest cr = (ControlRequest) r;
					System.out.println(cr.getSubtype());
					//Hemos recibido la orden desde el cliente
					this.robotRight = new Socket("localhost",4001);
					this.osRight = new ObjectOutputStream(this.robotRight.getOutputStream());
					this.isRight = new ObjectInputStream(this.robotRight.getInputStream());
					this.osRight.writeObject(cr);
					
					//Esperando respuesta de confirmacion de movimiento del siguiente robot
					System.out.println("Esperando respuesta...");
					this.isRight.readObject();
					ControlResponse crs = new ControlResponse("MOVE_DONE");
					crs.getArgs().add("Move done");
					this.osConsole.writeObject(crs);
					System.out.println("Enviando respuesta");
					//Hemos transmitido la operacion al siguiente robot del anillo
					//Cerramos conexiones de nuevo y esperamos nuevas ordenes;
					if(this.in !=null) {
						this.in.close();
						this.in = null;
						this.osRight.close();
						this.osRight=null;
						this.isRight.close();
						this.isRight = null;
						this.isConsole.close();
						this.isConsole=null;
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

class threadRobot extends Thread{
	private mainRobot maRobot;
}
