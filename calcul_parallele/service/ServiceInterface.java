package service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import noeud_calcul.ComputeNode;

public interface ServiceInterface extends Remote {

    /** Enregistre un nœud de calcul auprès du service central. */
    void enregistrerNoeud(ComputeNode noeud) throws RemoteException;

    /** Retire un nœud de calcul de la liste. */
    void supprimerNoeud(ComputeNode noeud) throws RemoteException;

    /** Retourne le nombre de nœuds actuellement disponibles. */
    int getNombreNoeuds() throws RemoteException;

    /**
     * Retourne le prochain nœud disponible (round-robin).
     * C'est le service qui choisit quel nœud donner — le client
     * ne connaît jamais la liste complète.
     * Retourne null s'il n'y a plus aucun nœud.
     */
    ComputeNode obtenirNoeud() throws RemoteException;
}
