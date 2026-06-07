import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.File;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.Duration;

import raytracer.Image;
import raytracer.Scene;

public class ComputeNodeImpl {

    public static void main(String[] args) {
        demarrerNoeud("Noeud-1");
    }

    public static void demarrerNoeud(String nomNoeud) {
        try {
            // Recherche du Dispatcher
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            DispatcherInterface dispatcher = (DispatcherInterface) registry.lookup("Dispatcher");
            System.out.println(nomNoeud + " : Connecté");

            // Boucle infinie pour demander du travail
            while (true) {
                Task t = dispatcher.demanderTravail();

                if (t == null) {
                    Thread.sleep(1000);
                } else {
                    System.out.println(nomNoeud + " : Tâche " + t.id + " reçue : bloc de " + t.w + "x" + t.h + " en ("
                            + t.x + "," + t.y + ")");

                    Instant debut = Instant.now();

                    Scene scene = new Scene(t.nomFichier, t.largeurTotale, t.hauteurTotale);
                    Image bloc = scene.compute(t.x, t.y, t.w, t.h);

                    // 3. Renvoyer le résultat
                    TaskResult resultat = new TaskResult(t.id, bloc);
                    dispatcher.renvoyerResultat(resultat);

                    Instant fin = Instant.now();
                    long duree = Duration.between(debut, fin).toMillis();
                    System.out.println(nomNoeud + " : Tâche " + t.id + " : " + duree + " ms");
                }
            }
        } catch (Exception e) {
            System.err.println(nomNoeud + " : Erreur dans le noeud de calcul : " + e.toString());
            e.printStackTrace();
        }
    }
}
