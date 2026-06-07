import java.io.Serializable;

public class Task implements Serializable {
    public int id;
    public String nomFichier;
    public int x, y, w, h;
    public int largeurTotale, hauteurTotale;

    public Task(int id, String nomFichier, int x, int y, int w, int h, int largeurTotale, int hauteurTotale) {
        this.id = id;
        this.nomFichier = nomFichier;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.largeurTotale = largeurTotale;
        this.hauteurTotale = hauteurTotale;
    }
}
