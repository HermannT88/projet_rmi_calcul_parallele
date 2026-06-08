package service;
import java.rmi.RemoteException;
import java.awt.Color;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;

import ComputeNodeImpl;
import raytracer.Image;
import client.ClientRaytracer;


public class ServiceCentral implements ServiceInterface {

    private ArrayList<ComputeNodeImpl> listeNoeuds = new ArrayList<>();
    private ArrayList<ClientRaytracer> listeClients = new ArrayList<>();

    public ServiceCentral() {
        this.listeNoeuds = new ArrayList<>();
        this.listeClients = new ArrayList<>();
    }
    
    @Override
    public void enregistrerNoeud(ComputeNodeImpl noeud) throws RemoteException {
        this.listeNoeuds.add(noeud);
    }

    @Override
    public void supprimerNoeud(ComputeNodeImpl noeud) throws RemoteException {
        this.listeNoeuds.remove(noeud);
    }

    @Override
    public void enregistrerClient(ClientRaytracer client) throws RemoteException {
        this.listeClients.add(client);
    }

    @Override
    public ArrayList<ComputeNodeImpl> getListeNoeuds() {
        return this.listeNoeuds;
    }


}
