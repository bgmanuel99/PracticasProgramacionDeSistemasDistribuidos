package ejercicio3;

import java.io.IOException;
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
	private Console console;
	
	
	public mainRobot() {
		this.console = new Console("2");
		String cmd = this.console.getCommand();
		
		while(!cmd.equals("end")) {
			try {
				
				
				if(cmd.equals("move")) {
					//move
					this.doMove();
				}else if(cmd.equals("rotate")){
					
				}else if(cmd.equals("stop")) {
					
				}else if(cmd.equals("status")) {
					this.doStatus();
				}
				
				
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			this.doDisconnect();
			cmd =this.console.getCommand();
		}

		
	}
	
	
	public void doStatus() throws IOException, ClassNotFoundException {
		
		this.robotRight = new Socket("localhost",4001);
		this.osRight = new ObjectOutputStream(this.robotRight.getOutputStream());
		this.isRight = new ObjectInputStream(this.robotRight.getInputStream());
		ControlRequest cr = new ControlRequest("OP_STATUS");
		this.osRight.writeObject(cr);
		ControlResponse crs = (ControlResponse) this.isRight.readObject();
		System.out.println(crs.getSubtype());
		if(crs.getSubtype().equals("STATUS_OK")) {
			this.console.writeMessage("All nodes are online");
		}else {
			this.console.writeMessage("Some node is offline");

		}
	}
	
	public void doMove() throws IOException, ClassNotFoundException {
		ServerSocket listen = new ServerSocket(4000);
		ControlRequest cr = new ControlRequest("OP_MOVE");
		this.robotRight = new Socket("localhost",4001);
		this.osRight = new ObjectOutputStream(this.robotRight.getOutputStream());
		this.isRight = new ObjectInputStream(this.robotRight.getInputStream());
		this.osRight.writeObject(cr);
		//Enviamos y esperamos respueste
		ControlResponse crs = (ControlResponse) this.isRight.readObject();
		if(crs.getSubtype().equals("OP_OK")) {
			this.console.writeMessage("Shipment received");
		}
		//System.out.println("esperando el ultimo nodo del anillo");
		this.in=listen.accept();
		this.osConsole=new ObjectOutputStream(this.in.getOutputStream());
		this.isConsole=new ObjectInputStream(this.in.getInputStream());
		
		Request r = (Request)this.isConsole.readObject();
		if(r.getType().equals("CONTROL_REQUEST")) {
			ControlRequest crs_ok = (ControlRequest) r;
			if(crs_ok.getSubtype().equals("OP_MOVE")) {
				this.console.writeMessage("All movements completed");
			}
		}
		listen.close();
	}
	
	public void doDisconnect() {
		if(this.in!=null&&this.robotRight!=null) {
			try {
				this.isConsole.close();
				this.isConsole=null;
				this.isRight.close();
				this.isRight=null;
				this.osConsole.close();
				this.osConsole=null;
				this.osRight.close();
				this.osRight=null;
				this.in.close();
				this.in=null;
				this.robotRight.close();
				this.robotRight=null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	
}

class threadRobot extends Thread{
	private mainRobot maRobot;
}
