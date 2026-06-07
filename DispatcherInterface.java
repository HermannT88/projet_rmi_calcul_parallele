import java.rmi.Remote;
import java.rmi.RemoteException;
import raytracer.Image;

public interface DispatcherInterface extends Remote {

    Image distribuerCalcul(String nomFichier, int largeurTotale, int hauteurTotale) throws RemoteException;

    Task demanderTravail() throws RemoteException;

    void renvoyerResultat(TaskResult resultat) throws RemoteException;

}
