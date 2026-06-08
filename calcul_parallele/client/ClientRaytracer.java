import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.NotBoundException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.time.Instant;
import java.time.Duration;

import raytracer.Disp;
import raytracer.Image;

public class ClientRaytracer {

    private Image imageEnCours;

    private Task[] toutesLesTaches;
    private int prochainBloc;
    private int blocsRestants;

    public synchronized Image distribuerCalcul(String nomFichier, int largeurTotale, int hauteurTotale)
            throws RemoteException {
        try {
            System.out.println("Nouvelle demande du client : " + RemoteServer.getClientHost());
        } catch (ServerNotActiveException e) {
        }

        int nbColonnes = 10;
        int nbLignes = 10;
        int nbTotalTaches = nbColonnes * nbLignes;

        this.imageEnCours = new Image(largeurTotale, hauteurTotale);
        for (int x = 0; x < largeurTotale; x++) {
            for (int y = 0; y < hauteurTotale; y++) {
                this.imageEnCours.setPixel(x, y, Color.BLACK);
            }
        }

        this.toutesLesTaches = new Task[nbTotalTaches];
        this.prochainBloc = 0;
        this.blocsRestants = nbTotalTaches;

        int lBloc = largeurTotale / nbColonnes;
        int hBloc = hauteurTotale / nbLignes;

        int id = 0;
        for (int lig = 0; lig < nbLignes; lig++) {
            for (int col = 0; col < nbColonnes; col++) {
                int x = col * lBloc;
                int y = lig * hBloc;
                int w = (col == nbColonnes - 1) ? (largeurTotale - x) : lBloc;
                int h = (lig == nbLignes - 1) ? (hauteurTotale - y) : hBloc;

                Task t = new Task(id, nomFichier, x, y, w, h, largeurTotale, hauteurTotale);
                toutesLesTaches[id] = t;
                id++;
            }
        }

        System.out.println(nbTotalTaches + " tâches prêtes. En attente des noeuds...");

        while (blocsRestants > 0) {
            try {
                wait(); // Attend un notify() de la méthode renvoyerResultat
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Calcul terminé !");
        return imageEnCours;
    }

    @Override
    public synchronized Task demanderTravail() throws RemoteException {
        // S'il reste des blocs à attribuer
        if (toutesLesTaches != null && prochainBloc < toutesLesTaches.length) {
            Task t = toutesLesTaches[prochainBloc];
            prochainBloc++;
            return t;
        }
        return null;
    }

    @Override
    public synchronized void renvoyerResultat(TaskResult resultat) throws RemoteException {
        if (toutesLesTaches == null)
            return;

        Task t = null;
        for (int i = 0; i < toutesLesTaches.length; i++) {
            if (toutesLesTaches[i] != null && toutesLesTaches[i].id == resultat.taskId) {
                t = toutesLesTaches[i];
                // On met à null pour marquer la tâche comme terminée
                toutesLesTaches[i] = null;
                break;
            }
        }

        if (t == null)
            return; // Déjà traitée ou erreur

        try {
            System.out.println("Résultat de tâche " + t.id + " reçu depuis le noeud : " + RemoteServer.getClientHost());
        } catch (ServerNotActiveException e) {
        }

        Image blocImage = resultat.imageBloc;
        for (int i = 0; i < t.w; i++) {
            for (int j = 0; j < t.h; j++) {
                Color c = blocImage.getPixel(i, j);
                imageEnCours.setPixel(t.x + i, t.y + j, c);
            }
        }

        blocsRestants--;

        if (blocsRestants == 0) {
            notifyAll();
        }
    }

    @Override
    public synchronized Image getImageEnCours() throws RemoteException {
        // Renvoie une copie ou l'image en l'état
        return imageEnCours;
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage : java ClientRaytracer <fichier_scene> <largeur> <hauteur>");
            return;
        }

        String fichier_description = args[0];
        int largeur = Integer.parseInt(args[1]);
        int hauteur = Integer.parseInt(args[2]);

        try {
            Disp disp = new Disp("Raytracer Distribué", largeur, hauteur);

            Registry reg = LocateRegistry.getRegistry("localhost", 1099);
            

            ClientRaytracer dispatcher = new ClientRaytracer();

            System.out.println("Demande : " + fichier_description + " (" + largeur + "x" + hauteur + ")");

            Instant debut = Instant.now();

            Thread afficheur = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            Thread.sleep(200);
                            Image img = dispatcher.getImageEnCours();
                            if (img != null) {
                                disp.setImage(img, 0, 0);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } catch (Exception e) {
                        }
                    }
                }
            });
            afficheur.start();

            Image imageFinale = dispatcher.distribuerCalcul(fichier_description, largeur, hauteur);

            afficheur.interrupt();

            Instant fin = Instant.now();
            long duree = Duration.between(debut, fin).toMillis();

            System.out.println("Image calculée en : " + duree + " ms");

            disp.setImage(imageFinale, 0, 0);

        } catch (NotBoundException e) {
            System.out.println("Reference non trouvee dans l'annuaire");
        } catch (RemoteException e) {
            System.out.println("Reference non cree ou erreur de connexion : " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}
