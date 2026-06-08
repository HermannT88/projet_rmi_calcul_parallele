import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import raytracer.Image;
import noeud_calcul.ComputeNodeImpl;

public interface ServiceInterface extends Remote {

    void enregistrerNoeud(ComputeNodeImpl noeud) throws RemoteException;

    void supprimerNoeud(ComputeNodeImpl noeud) throws RemoteException;

    void enregistrerClient(ClientRaytracer client) throws RemoteException;

    ArrayList<ComputeNodeImpl> getListeNoeuds();

}
