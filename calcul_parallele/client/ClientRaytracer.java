package client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import raytracer.Disp;
import raytracer.Image;
import service.ServiceInterface;
import noeud_calcul.ComputeNode;

/**
 * Client du système de calcul parallèle distribué.
 *
 * Fonctionnement :
 * 1. Se connecte au service central et récupère la liste des nœuds disponibles.
 * 2. Découpe l'image en blocs (grille 10x10).
 * 3. Distribue les blocs aux nœuds en parallèle (un Thread par nœud).
 * 4. Chaque thread pioche des blocs dans la file de travail et appelle
 * calculerBloc() sur le nœud qui lui est attribué.
 * 5. Assemble les résultats et affiche l'image finale.
 */
public class ClientRaytracer {

    // File de travail partagée entre les threads (accès synchronisé)
    private final List<Task> fileTravail = new ArrayList<>();
    // Image résultat finale
    private Image imageFinale;
    // Compteur de blocs encore à recevoir
    private int blocsRestants;

    /**
     * Récupère le prochain bloc à traiter (thread-safe).
     * Retourne null si tout le travail a été distribué.
     */
    private synchronized Task prendreProchainBloc() {
        if (fileTravail.isEmpty())
            return null;
        return fileTravail.remove(0);
    }

    /**
     * Reçoit le résultat d'un bloc calculé et l'intègre dans l'image finale
     * (thread-safe).
     */
    private synchronized void recevoirResultat(Task t, Image bloc) {
        for (int i = 0; i < t.w; i++) {
            for (int j = 0; j < t.h; j++) {
                imageFinale.setPixel(t.x + i, t.y + j, bloc.getPixel(i, j));
            }
        }
        blocsRestants--;
        if (blocsRestants == 0) {
            notifyAll(); // Réveille le thread principal
        }
    }

    /**
     * Lance le calcul distribué :
     * - Récupère la liste des nœuds depuis le service central
     * - Prépare la file de blocs à calculer
     * - Lance un thread par nœud pour distribuer le travail
     * - Attend que tous les blocs soient calculés
     */
    public Image distribuerCalcul(ServiceInterface service, Disp disp, String nomFichier, int largeurTotale,
            int hauteurTotale)
            throws RemoteException, InterruptedException {

        // Récupération des nœuds disponibles
        List<ComputeNode> noeuds = service.getListeNoeuds();
        if (noeuds.isEmpty()) {
            System.out.println("Aucun nœud disponible !");
            return null;
        }
        System.out.println(noeuds.size() + " nœuds disponibles.");

        // Préparation de l'image résultat
        imageFinale = new Image(largeurTotale, hauteurTotale);

        // Découpage en grille de blocs
        int nbColonnes = 10;
        int nbLignes = 10;
        int lBloc = largeurTotale / nbColonnes;
        int hBloc = hauteurTotale / nbLignes;

        int id = 0;
        for (int lig = 0; lig < nbLignes; lig++) {
            for (int col = 0; col < nbColonnes; col++) {
                int x = col * lBloc;
                int y = lig * hBloc;
                int w = (col == nbColonnes - 1) ? (largeurTotale - x) : lBloc;
                int h = (lig == nbLignes - 1) ? (hauteurTotale - y) : hBloc;
                fileTravail.add(new Task(id++, nomFichier, x, y, w, h, largeurTotale, hauteurTotale));
            }
        }

        blocsRestants = fileTravail.size();
        System.out.println(blocsRestants + " blocs à calculer.");

        // Lancement d'un thread de travail par nœud
        List<Thread> threads = new ArrayList<>();
        for (ComputeNode noeud : noeuds) {
            Thread t = new Thread(() -> {
                Task tache;
                while ((tache = prendreProchainBloc()) != null) {
                    try {
                        System.out.println("Envoi bloc " + tache.id + " au noeud...");
                        Image bloc = noeud.calculerBloc(
                                tache.nomFichier,
                                tache.x, tache.y, tache.w, tache.h,
                                tache.largeurTotale, tache.hauteurTotale);
                        // Affichage immédiat du bloc dès réception
                        disp.setImage(bloc, tache.x, tache.y);
                        recevoirResultat(tache, bloc);
                        System.out.println("Bloc " + tache.id + " integre.");
                    } catch (RemoteException e) {
                        System.err.println("Erreur sur bloc " + tache.id + " : " + e.getMessage());
                        // Remettre le bloc dans la file pour un autre nœud
                        synchronized (this) {
                            fileTravail.add(tache);
                        }
                    }
                }
            });
            threads.add(t);
            t.start();
        }

        // Attente de la fin de tous les calculs
        synchronized (this) {
            while (blocsRestants > 0) {
                wait();
            }
        }

        // Attente propre de la fin des threads
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
            }
        }

        System.out.println("Calcul distribué terminé !");
        return imageFinale;
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage : java ClientRaytracer <fichier_scene> <largeur> <hauteur> [adresse_service]");
            return;
        }

        String nomFichier = args[0];
        int largeur = Integer.parseInt(args[1]);
        int hauteur = Integer.parseInt(args[2]);
        String adresse = args.length > 3 ? args[3] : "localhost";

        try {
            Disp disp = new Disp("Raytracer Distribué", largeur, hauteur);

            // Connexion au service central
            Registry reg = LocateRegistry.getRegistry(adresse, 1099);
            ServiceInterface service = (ServiceInterface) reg.lookup("ServiceCentral");
            System.out.println("Connecté au service central sur " + adresse);

            ClientRaytracer client = new ClientRaytracer();

            System.out.println("Lancement du calcul : " + nomFichier + " (" + largeur + "x" + hauteur + ")");
            Instant debut = Instant.now();

            Image imageResultat = client.distribuerCalcul(service, disp, nomFichier, largeur, hauteur);

            Instant fin = Instant.now();
            long duree = Duration.between(debut, fin).toMillis();
            System.out.println("Image calculee en : " + duree + " ms");

            if (imageResultat != null) {
                disp.setImage(imageResultat, 0, 0);
            }

        } catch (NotBoundException e) {
            System.out.println("Service central non trouvé dans l'annuaire.");
        } catch (RemoteException e) {
            System.out.println("Erreur de connexion RMI : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
