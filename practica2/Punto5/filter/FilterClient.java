package filter;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

import com.sun.xml.internal.ws.api.pipe.NextAction;

import protocol.*;

public class FilterClient
{
	
	public static Socket socket;
	public static ObjectOutputStream os;
	public static ObjectInputStream is;
	
	public static void doConnect() {
		try {
			if(socket==null) {
				socket = new Socket("localhost",8000);
				os = new ObjectOutputStream(socket.getOutputStream());
				is = new ObjectInputStream(socket.getInputStream());
			}
		} catch (Exception e) {
			System.out.println("doConnect " + e.getMessage());
		}
	}
	
	public static void doDisconnect() {
		try {
			if(socket!=null) {
				os.close();
				os = null;
				is.close();
				is = null;
				socket.close();
				socket = null;
			}
		} catch (Exception e) {
			System.out.println("doDisconnect " + e.getMessage());

		}
	}
	
	
  public static void main(String[] args) {
	try {
		String[] filters= {"lightfilter","grayfilter","oppositefilter","flipedfilter","darkfilter"};
		Scanner scanner = new Scanner(System.in);
        System.out.print("Do you wanto to filter an image? yes|close: ");
		String cmd = scanner.nextLine();
		
		while(!cmd.equals("close")) {
			
			
			System.out.println("Choose the filters:");
			System.out.println("Available filters :");
			for(int i = 0; i< filters.length;i++) {
				System.out.println((i+1) + ". "+ filters[i]);
			}
			System.out.println((filters.length+1)+". close");
			int filter = 0;
			Vector<Integer> filtersIndex = new Vector<Integer>();
			while(filter!=(filters.length+1)) {
				filter = Integer.valueOf(scanner.nextLine());
				if(filter!=6) {
					if(!filtersIndex.contains(filter)) {
						filtersIndex.add(filter-1);
					}else {
						System.out.println("Roses are red, Violets are blue, and this filter is not due ");
					}
				}else break;
			}
			
			System.out.println("Starting filtering...");
			String path = "C:/Users/fer27/OneDrive/Escritorio/Java_Corba/corba/img/monaLisa.jpg";
			for(int i = 0; i < filtersIndex.size();i++) {
				doConnect();
				ControlRequest cr = new ControlRequest("OP_FILTER");
				cr.getArgs().add(path);
				cr.getArgs().add(filtersIndex.get(i));
				os.writeObject(cr);
				ControlResponse crs = (ControlResponse) is.readObject();
				path = crs.getArgs().get(0).toString();
				doDisconnect();
			}
			System.out.println("Path: "+path);
			System.out.println("Do you want to make another filter? yes|close");
			cmd = scanner.nextLine();
		}
		
		doConnect();
		ControlRequest cr = new ControlRequest("OP_CLOSE");
		os.writeObject(cr);
		doDisconnect();
	} catch (Exception e) {
		System.out.println(e.getMessage());
}
}

}