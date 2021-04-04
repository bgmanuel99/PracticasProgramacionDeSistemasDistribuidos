package ejercicio3;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import protocol.ControlRequest;
import protocol.ControlResponse;

public class Client {
	public static void main(String[] args) {
		try {
			new Controller();
					
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}

class Controller {
	
	private Console console;
	private Socket socket;
	private ObjectOutputStream os;
	private ObjectInputStream is;
	
	
	public Controller() {
		System.out.println("Iniciando el cliente...");
		// TODO Auto-generated constructor stub
		this.init();
		String cmd = this.console.getCommand();
		while(!cmd.equals("end")) {
			
			try {
				ControlRequest cr = new ControlRequest(cmd);
				this.os.writeObject(cr);
				ControlResponse crs = (ControlResponse) this.is.readObject();
				System.out.println(crs.getArgs().get(0).toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			cmd =this.console.getCommand();
		}
		
		
	}
	public void doConnect() {
		if(this.socket==null) {
			try {
				this.socket = new Socket("localhost",4000);
				this.os = new ObjectOutputStream(this.socket.getOutputStream());
				this.is = new ObjectInputStream(this.socket.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void doDisconnect() {
		try {
			if(this.socket!=null) {
				try {
					this.is.close();
					this.is=null;
					this.os.close();
					this.os=null;
					this.socket.close();
					this.socket = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public void init() {
		this.console = new Console("2");
		try {
			this.socket = new Socket("localhost",4000);
			this.os = new ObjectOutputStream(this.socket.getOutputStream());
			this.is = new ObjectInputStream(this.socket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
