import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class LancerService {
  public static void main(String args[]) {
    try {
      System.out.println("Lancement du Dispatcher...");
      ServiceInterface nom = new ServiceCentral();
      ServiceInterface rd = (ServiceInterface) UnicastRemoteObject.exportObject(nom, 0); // 0 pour que OS donne le port

      Registry reg = LocateRegistry.getRegistry(1099); // 1099 port par defaut des annuaire (modifiable)
      reg.rebind("ServiceCentral", rd); // donner un nom dans l annuaire pour le service

      System.out.println("Service enregistré");
    } catch (RemoteException e) {
      System.out.println("Erreur RMI : " + e.getMessage());
      System.out.println("L'annuaire RMI (rmiregistry) est-il bien lancé sur le port 1099 ?");
    }
  }
}
