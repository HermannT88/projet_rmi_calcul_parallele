import java.time.Instant;
import java.util.ArrayList;
import java.time.Duration;

import raytracer.Disp;
import raytracer.Scene;
import raytracer.Image;

public class LancerRaytracer {

    public static String aide = "Raytracer : synthèse d'image par lancé de rayons (https://en.wikipedia.org/wiki/Ray_tracing_(graphics))\n\nUsage : java LancerRaytracer [fichier-scène] [largeur] [hauteur]\n\tfichier-scène : la description de la scène (par défaut simple.txt)\n\tlargeur : largeur de l'image calculée (par défaut 512)\n\thauteur : hauteur de l'image calculée (par défaut 512)\n";
     
    public static void main(String args[]){

        // Le fichier de description de la scène si pas fournie
        String fichier_description="simple.txt";

        // largeur et hauteur par défaut de l'image à reconstruire
        int largeur = 512, hauteur = 512;
        
        if(args.length > 0){
            fichier_description = args[0];
            if(args.length > 1){
                largeur = Integer.parseInt(args[1]);
                if(args.length > 2)
                    hauteur = Integer.parseInt(args[2]);
            }
        }else{
            System.out.println(aide);
        }
        
   
        // création d'une fenêtre 
        Disp disp = new Disp("Raytracer", largeur, hauteur);
        
        // Initialisation d'une scène depuis le modèle 
        Scene scene = new Scene(fichier_description, largeur, hauteur);
        
        // Calcul de l'image de la scène les paramètres : 
        // - x0 et y0 : correspondant au coin haut à gauche
        // - l et h : hauteur et largeur de l'image calculée
        // Ici on calcule toute l'image (0,0) -> (largeur, hauteur)
        
        int x0 = 0, y0 = 0;
        int l = largeur, h = hauteur;
                
        // Chronométrage du temps de calcul
        System.out.println("Calcul de l'image :\n - Coordonnées : "+x0+","+y0+"\n - Taille "+ largeur + "x" + hauteur);
        int nbColonnes = 10;
        int nbLignes = 10;
        int lBloc = l / nbColonnes;
        int hBloc = h / nbLignes;
        int x = 1;
        
        Instant debut = Instant.now();

        for(int i = 0; i < 100; i++) {
            
            if (i % 2 == 0) {
                
                int col = i % nbColonnes;
                int lig = i / nbColonnes;
                
                int xActuel = col * lBloc;
                int yActuel = lig * hBloc;
                
                Image image = scene.compute(xActuel, yActuel, lBloc, hBloc);
                
                disp.setImage(image, xActuel, yActuel);
                
            }
        }
        //Image image = scene.compute(x0, y0, l/2, h/2);
        //Image image2 = scene.compute(l/2, h/2, l/2, h/2);

        
        Instant fin = Instant.now();

        long duree = Duration.between(debut, fin).toMillis();
        
        System.out.println("Image calculée en :"+duree+" ms");
        
        // Affichage de l'image calculée
        //disp.setImage(image2, l/2, h/2);
    }	
}
