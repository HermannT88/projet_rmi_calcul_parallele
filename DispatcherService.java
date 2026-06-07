import java.rmi.RemoteException;
import java.awt.Color;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;

import raytracer.Image;

public class DispatcherService implements DispatcherInterface {

    private Image imageEnCours;

    private Task[] toutesLesTaches;
    private int prochainBloc;
    private int blocsRestants;

    public DispatcherService() {
    }

    @Override
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
}
