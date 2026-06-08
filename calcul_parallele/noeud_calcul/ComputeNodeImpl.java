package noeud_calcul;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.time.Instant;
import java.time.Duration;
import raytracer.Image;
import raytracer.Scene;
import service.ServiceInterface;

public class ComputeNodeImpl implements ComputeNode {

    private String nom;

    public ComputeNodeImpl(String nom) {
        this.nom = nom;
    }

    @Override
    public Image calculerBloc(String fichier_description, int x, int y, int w, int h, int largeurTotale,
            int hauteurTotale) throws RemoteException {
        System.out.println(nom + " : Tâche reçue : bloc de " + w + "x" + h + " en (" + x + "," + y + ")");
        Instant debut = Instant.now();

        // Calcul local du bloc
        Scene scene = new Scene(fichier_description, largeurTotale, hauteurTotale);
        Image bloc = scene.compute(x, y, w, h);

        Instant fin = Instant.now();
        long duree = Duration.between(debut, fin).toMillis();
        System.out.println(nom + " : Tâche complétée en " + duree + " ms.");

        // Retourne le résultat
        return bloc;
    }

    public static void demarrerNoeud(String nomNoeud) {
        try {
            ComputeNodeImpl node = new ComputeNodeImpl(nomNoeud);
            // Exportation noeud pour qu'il devienne un serveur RMI accessible
            ComputeNode stub = (ComputeNode) UnicastRemoteObject.exportObject(node, 0);

            // Connexion à l'annuaire RMI
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            ServiceInterface serviceCentral = (ServiceInterface) registry.lookup("ServiceCentral");

            // Enregistrement du noeud dans la liste du service central
            serviceCentral.enregistrerNoeud(stub);
            System.out.println(nomNoeud + " : Enregistré auprès du service central.");

            // Le thread reste actif pour continuer à répondre aux appels RMI
            synchronized (node) {
                node.wait();
            }
        } catch (InterruptedException e) {
            System.out.println(nomNoeud + " : Arrêt du nœud.");
        } catch (Exception e) {
            System.err.println(nomNoeud + " : Erreur d'initialisation : " + e.toString());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String nom = args.length > 0 ? args[0] : "Noeud-1";
        demarrerNoeud(nom);
    }
}