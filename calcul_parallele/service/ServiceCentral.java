package service;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.List;
import noeud_calcul.ComputeNode;

/**
 * Service Central.
 *
 * Gère la liste des nœuds en interne. Les clients ne voient jamais
 * la liste : ils demandent un nœud via obtenirNoeud() et le service
 * fait tourner un round-robin sur les nœuds disponibles.
 */
public class ServiceCentral implements ServiceInterface {

    private final List<ComputeNode> listeNoeuds = new ArrayList<>();
    private int indexCourant = 0;

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
        int idx = listeNoeuds.indexOf(noeud);
        if (idx >= 0) {
            listeNoeuds.remove(idx);
            // Corrige l'index round-robin pour ne pas sauter un nœud
            if (indexCourant > 0 && indexCourant >= listeNoeuds.size()) {
                indexCourant = 0;
            }
        }
        System.out.println("Nœud retiré. Nœuds restants : " + listeNoeuds.size());
    }

    @Override
    public synchronized int getNombreNoeuds() throws RemoteException {
        return listeNoeuds.size();
    }

    /**
     * Donne le prochain nœud disponible en round-robin.
     * Retourne null si aucun nœud n'est disponible.
     */
    @Override
    public synchronized ComputeNode obtenirNoeud() throws RemoteException {
        if (listeNoeuds.isEmpty()) {
            return null;
        }
        indexCourant = indexCourant % listeNoeuds.size();
        ComputeNode noeud = listeNoeuds.get(indexCourant);
        indexCourant++;
        return noeud;
    }
}
