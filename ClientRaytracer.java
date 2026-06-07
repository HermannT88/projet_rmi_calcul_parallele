import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.NotBoundException;
import java.time.Instant;
import java.time.Duration;

import raytracer.Disp;
import raytracer.Image;

public class ClientRaytracer {

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
            DispatcherInterface dispatcher = (DispatcherInterface) reg.lookup("Dispatcher");

            System.out.println("Demande : " + fichier_description + " (" + largeur + "x" + hauteur + ")");

            Instant debut = Instant.now();

            Thread afficheur = new Thread(() -> {
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
