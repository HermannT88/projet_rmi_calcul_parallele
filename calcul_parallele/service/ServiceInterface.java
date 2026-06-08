package service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import noeud_calcul.ComputeNode;

public interface ServiceInterface extends Remote {

    /** Enregistre un nœud de calcul auprès du service central. */
    void enregistrerNoeud(ComputeNode noeud) throws RemoteException;

    /** Retire un nœud de calcul de la liste. */
    void supprimerNoeud(ComputeNode noeud) throws RemoteException;

    /** Retourne la liste des nœuds disponibles pour que le client distribue le travail. */
    List<ComputeNode> getListeNoeuds() throws RemoteException;
}
