package service;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.List;
import noeud_calcul.ComputeNode;

public class ServiceCentral implements ServiceInterface {

    private final List<ComputeNode> listeNoeuds = new ArrayList<>();

    public ServiceCentral() {
    }

    @Override
    public synchronized void enregistrerNoeud(ComputeNode noeud) throws RemoteException {
        try {
            System.out.println("Nouveau nœud enregistré depuis : " + RemoteServer.getClientHost());
        } catch (ServerNotActiveException e) {
        }
        listeNoeuds.add(noeud);
        System.out.println("Nombre de nœuds disponibles : " + listeNoeuds.size());
    }

    @Override
    public synchronized void supprimerNoeud(ComputeNode noeud) throws RemoteException {
        listeNoeuds.remove(noeud);
        System.out.println("Nœud retiré. Nœuds restants : " + listeNoeuds.size());
    }

    @Override
    public synchronized List<ComputeNode> getListeNoeuds() throws RemoteException {
        return new ArrayList<>(listeNoeuds);
    }
}
