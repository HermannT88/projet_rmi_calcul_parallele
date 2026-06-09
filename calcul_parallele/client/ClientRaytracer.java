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
 * 1. Demande au service le nombre de nœuds pour dimensionner les threads.
 * 2. Découpe l'image en blocs (grille 10x10).
 * 3. Lance autant de threads que de nœuds disponibles.
 * 4. Chaque thread demande UN nœud au service via obtenirNoeud(), puis
 *    appelle calculerBloc() directement sur ce nœud (parallélisme préservé).
 * 5. Si le nœud est mort (RemoteException sur calculerBloc) :
 *    - Le client prévient le service (supprimerNoeud)
 *    - Le bloc est remis dans la file
 *    - Le thread s'arrête (un thread = un nœud)
 * 6. Assemble les résultats et affiche l'image finale.
 */
public class ClientRaytracer {

    private final List<Task> fileTravail = new ArrayList<>();
    private Image imageFinale;
    private int blocsRestants;

    private synchronized Task prendreProchainBloc() {
        if (fileTravail.isEmpty())
            return null;
        return fileTravail.remove(0);
    }

    private synchronized void remettreBloc(Task t) {
        fileTravail.add(0, t);
    }

    private synchronized void recevoirResultat(Task t, Image bloc) {
        for (int i = 0; i < t.w; i++) {
            for (int j = 0; j < t.h; j++) {
                imageFinale.setPixel(t.x + i, t.y + j, bloc.getPixel(i, j));
            }
        }
        blocsRestants--;
        if (blocsRestants == 0) {
            notifyAll();
        }
    }

    public Image distribuerCalcul(ServiceInterface service, Disp disp, String nomFichier,
            int largeurTotale, int hauteurTotale)
            throws RemoteException, InterruptedException {

        int nbNoeuds = service.getNombreNoeuds();
        if (nbNoeuds == 0) {
            System.out.println("Aucun nœud disponible !");
            return null;
        }
        System.out.println(nbNoeuds + " nœuds disponibles.");

        imageFinale = new Image(largeurTotale, hauteurTotale);

        // Découpage en grille 10x10
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

        // 1 thread par nœud disponible au départ
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < nbNoeuds; i++) {
            Thread t = new Thread(() -> {
                // Chaque thread demande son nœud au service
                ComputeNode monNoeud;
                try {
                    monNoeud = service.obtenirNoeud();
                } catch (RemoteException e) {
                    System.err.println("Impossible d'obtenir un nœud : " + e.getMessage());
                    return;
                }
                if (monNoeud == null) {
                    System.err.println("Le service n'a plus de nœud à donner.");
                    return;
                }

                Task tache;
                while ((tache = prendreProchainBloc()) != null) {
                    try {
                        System.out.println("Envoi bloc " + tache.id + " au noeud...");
                        // Appel direct sur le nœud → vrai parallélisme
                        Image bloc = monNoeud.calculerBloc(
                                tache.nomFichier,
                                tache.x, tache.y, tache.w, tache.h,
                                tache.largeurTotale, tache.hauteurTotale);
                        disp.setImage(bloc, tache.x, tache.y);
                        recevoirResultat(tache, bloc);
                        System.out.println("Bloc " + tache.id + " integre.");
                    } catch (RemoteException e) {
                        // Le nœud est mort : on le signale au service et on remet le bloc
                        System.err.println("Nœud mort sur bloc " + tache.id + ", retrait du service.");
                        try {
                            service.supprimerNoeud(monNoeud);
                        } catch (RemoteException ignored) {
                        }
                        remettreBloc(tache);
                        // Ce thread s'arrête : son nœud est mort
                        return;
                    }
                }
            });
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
            }
        }

        if (blocsRestants > 0) {
            System.err.println("ATTENTION : " + blocsRestants + " bloc(s) non calculé(s) (tous les nœuds sont morts).");
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
