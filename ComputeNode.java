import java.rmi.Remote;
import java.rmi.RemoteException;
import raytracer.Image;

public interface ComputeNode extends Remote {
    Image calculerBloc(String fichier_description, int x, int y, int w, int h, int largeurTotale, int hauteurTotale)
            throws RemoteException;
}
