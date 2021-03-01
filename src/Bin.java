import java.util.ArrayList;
import java.util.stream.Collectors;

public class Bin {
    private int taille;
    private ArrayList<Integer> itemList = new ArrayList<>();

    public Bin(int taille) {
        this.taille = taille;
    }

    public Bin(int taille, ArrayList<Integer> itemList) {
        this.taille = taille;
        this.itemList = itemList;
    }

    public int getTaille() {
        return taille;
    }

    public void setTaille(int taille) {
        this.taille = taille;
    }

    public ArrayList<Integer> getItemList() {
        return itemList;
    }

    public void setItemList(ArrayList<Integer> itemList) {
        this.itemList = itemList;
    }

}
