import java.rmi.RemoteException;
import java.awt.Color;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;

import raytracer.Image;


public class SerivceCentral implements ServiceInterface {

    private ArrayList<ComputeNodeImpl> listeNoeuds = new ArrayList<>();

    public SerivceCentral() {
    }
    
}
