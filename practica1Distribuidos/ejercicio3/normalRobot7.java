package ejercicio3;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import protocol.ControlRequest;
import protocol.ControlResponse;
import protocol.Request;

public class normalRobot7 {

	public static void main(String[] args) {
		new mainRobot7();

	}

}


class mainRobot7{
	
	private Socket in,  robotRight;
	private ObjectOutputStream osLeft, osRight;
	private ObjectInputStream isLeft, isRight;
	private int node = 7;
	public mainRobot7() {
		while(true) {
			try {
				//esperando una llamada entrante
				ServerSocket listen = new ServerSocket(4007);
				System.out.println("Robot7 waiting for instructionss");
				this.in = listen.accept();
				System.out.println("Connection accepted from: "+this.in.toString());
				this.isLeft = new ObjectInputStream(this.in.getInputStream());
				this.osLeft = new ObjectOutputStream(this.in.getOutputStream());
				//Esperamos el comando
				Request r = (Request) this.isLeft.readObject();
				
				if(r.getType().equals("CONTROL_REQUEST")) {
					ControlRequest cr = (ControlRequest) r;
					
					if(cr.getSubtype().equals("OP_MOVE")) {
						this.doConnect();
						this.osRight.writeObject(cr);
						//esperamos la confirmacion
					}else if(cr.getSubtype().equals("OP_STATUS")) {
						this.doConnect();
						this.doStatus(cr);
					}
					
				}
				this.doDisconnect();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	public void doConnect() throws UnknownHostException, IOException {
		this.robotRight = new Socket("localhost",4008);
		this.osRight = new ObjectOutputStream(this.robotRight.getOutputStream());
		this.isRight = new ObjectInputStream(this.robotRight.getInputStream());
	}
	public void doStatus(ControlRequest cr) throws IOException {
		try {
			this.osRight.writeObject(cr);
			
			ControlResponse crs =(ControlResponse) this.isRight.readObject();
			this.osLeft.writeObject(crs);
			
		} catch (IOException | ClassNotFoundException e) {
			ControlResponse crs = new ControlResponse("STATUS_NOK");
			crs.getArgs().add("The node "+ this.node+1+"is offline");
			this.osLeft.writeObject(crs);
		}
	}
	
	public void doDisconnect() {
		try {
			if(this.in !=null) {
				this.in.close();
				this.in = null;
				this.osRight.close();
				this.osRight=null;
				this.osLeft.close();
				this.osLeft=null;
				this.isLeft.close();
				this.isLeft=null;
				this.isRight.close();
				this.isRight=null;
				this.robotRight.close();
				this.robotRight = null;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	
}

class threadRobot7 extends Thread{
	private mainRobot7 maRobot;
}