package client;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import service.ServiceCentral;
import service.ServiceInterface;

public class LancerService {
  public static void main(String args[]) {
    try {
      System.out.println("Lancement du Service Central...");
      ServiceCentral nom = new ServiceCentral();
      ServiceInterface rd = (ServiceInterface) UnicastRemoteObject.exportObject(nom, 0);

      Registry reg = LocateRegistry.createRegistry(1099);
      reg.rebind("ServiceCentral", rd);

      System.out.println("Service Central enregistré sur le port 1099. En attente de noeuds...");
    } catch (RemoteException e) {
      System.out.println("Erreur RMI : " + e.getMessage());
    }
  }
}
