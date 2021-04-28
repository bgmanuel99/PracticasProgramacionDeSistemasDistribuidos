package filter;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import FilterApp.Filter;
import FilterApp.FilterHelper;
import protocol.*;

public class FilterProxy {
	static Filter filterImpl;

	  public static void main(String args[])
	    {
	      try{
	        // create and initialize the ORB
	        ORB orb = ORB.init(args, null);

	        // get the root naming context
	        org.omg.CORBA.Object objRef = 
	            orb.resolve_initial_references("NameService");
	        // Use NamingContextExt instead of NamingContext. This is 
	        // part of the Interoperable naming Service.  
	        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
	        
	        
	        
	        // resolve the Object Reference in Naming
	        String name = "Filter";
	        filterImpl = FilterHelper.narrow(ncRef.resolve_str(name));
	        System.out.println("Obtained a handle on server object: " + filterImpl);
	        System.out.println("Proxy Filter ready and waiting");
	        ServerSocket listen = new ServerSocket(8000);
	        while(true) {
	        	Socket socket=listen.accept();
	        	ObjectOutputStream osClient = new ObjectOutputStream(socket.getOutputStream());
	        	ObjectInputStream isClient = new ObjectInputStream(socket.getInputStream());
	        	
	        	Request r = (Request) isClient.readObject();
	        	if(r.getType().equals("CONTROL_REQUEST")) {
	        		ControlRequest cr = (ControlRequest) r;
	        		if(cr.getSubtype().equals("OP_FILTER")) {
	        			ControlResponse crs = new ControlResponse("OP_FILTER_OK");
	        			crs.getArgs().add(filterImpl.getFilterImage(cr.getArgs().get(0).toString(),Integer.valueOf(cr.getArgs().get(1).toString())));
	        			osClient.writeObject(crs);
	        		}else if(cr.getSubtype().equals("OP_CLOSE")) {
	        			filterImpl.shutdown();
	        			osClient.close();
	    	        	osClient = null;
	    	        	isClient.close();
	    	        	isClient = null;
	    	        	socket.close();
	    	        	socket = null;
	        			break; 
	        		}
	        	}
	        	
	        	osClient.close();
	        	osClient = null;
	        	isClient.close();
	        	isClient = null;
	        	socket.close();
	        	socket = null;
	        } 
	      }catch (Exception e) {
	        System.out.println("ERROR : " + e) ;
	        e.printStackTrace(System.out);
	        }
	    }

}
