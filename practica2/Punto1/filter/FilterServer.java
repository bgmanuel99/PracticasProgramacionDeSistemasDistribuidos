package filter;
import FilterApp.*;


import java.awt.Graphics;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


class FilterImpl extends FilterPOA{

    private ORB orb;

    public void setORB(ORB orb_val) {
        orb = orb_val; 
      }

    @Override
    public String getFilterImage(String path) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            int color;
            for (int j = 0; j < image.getHeight(); j = j + 2) {
                for (int i = 0; i < image.getWidth(); i++) {
                    color = image.getRGB(i, j);
                    image.setRGB(i, j, color - 150);
                }
            }
            path=path.substring(0, path.lastIndexOf('/')-1);
            path= path + "filter.jpg";
            ImageIO.write(image, "jpg", new File(path)); 
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return path;
    }

    @Override
    public void shutdown() {
        orb.shutdown(false);
    }
    
}

public class FilterServer {

    public static void main(String args[]) {
      try{


        // create and initialize the ORB
        ORB orb = ORB.init(args, null);

        // get reference to rootpoa & activate the POAManager
        POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        rootpoa.the_POAManager().activate();

        // create servant and register it with the ORB
        FilterImpl filterImpl = new FilterImpl();
        filterImpl.setORB(orb); 

        // get object reference from the servant
        org.omg.CORBA.Object ref = rootpoa.servant_to_reference(filterImpl);
        Filter href = FilterHelper.narrow(ref);

        // get the root naming context
        // NameService invokes the name service
        org.omg.CORBA.Object objRef =
            orb.resolve_initial_references("NameService");
 // Use NamingContextExt which is part of the Interoperable
        // Naming Service (INS) specification.
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);  
        // bind the Object Reference in Naming
        String name = "Filter";
        NameComponent path[] = ncRef.to_name( name );
        ncRef.rebind(path, href);
        System.out.println("Filter Server ready and waiting ...");
  
        // wait for invocations from clients
        orb.run();
      } 
          
        catch (Exception e) {
          System.err.println("ERROR: " + e);
        }
            
        System.out.println("HelloServer Exiting ...");
          
    }
  }
