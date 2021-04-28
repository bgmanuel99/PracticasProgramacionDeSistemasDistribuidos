package filter;
import FilterApp.*;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;


import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.omg.CORBA.*;

public class FilterClient
{
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
        String path = filterImpl.getFilterImage("C:/Users/fer27/OneDrive/Escritorio/Java_Corba/corba/img/monaLisa.jpg");
        System.out.println(path);
        filterImpl.shutdown();

      }catch (Exception e) {
        System.out.println("ERROR : " + e) ;
        e.printStackTrace(System.out);
        }
    }

}