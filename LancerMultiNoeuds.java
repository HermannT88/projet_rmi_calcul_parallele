public class LancerMultiNoeuds {
    public static void main(String[] args) {
        int nbNoeuds = 4; // Nombre de noeuds à lancer en parallèle

        if (args.length > 0) {
            try {
                nbNoeuds = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Argument invalide. Utilisation de la valeur par défaut : " + nbNoeuds);
            }
        }

        System.out.println("Lancement de " + nbNoeuds + " noeuds de calcul...");

        for (int i = 1; i <= nbNoeuds; i++) {
            final String nomNoeud = "Noeud-" + i;
            new Thread(() -> {
                ComputeNodeImpl.demarrerNoeud(nomNoeud);
            }).start();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
