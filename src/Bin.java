import java.util.ArrayList;

public class Bin {
    private int taille;
    private ArrayList<Item> itemList = new ArrayList<>();

    public Bin(int taille) {
        this.taille = taille;
    }

    public Bin(int taille, ArrayList<Item> itemList) {
        this.taille = taille;
        this.itemList = itemList;
    }

    public int getTaille() {
        return taille;
    }

    public void setTaille(int taille) {
        this.taille = taille;
    }

    public ArrayList<Item> getItemList() {
        return itemList;
    }

    public void setItemList(ArrayList<Item> itemList) {
        this.itemList = itemList;
    }
}
