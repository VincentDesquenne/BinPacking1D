import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class main {
    private static BufferedReader bufferedR;
    private static Bin bin;
    private static HashMap<Integer, Bin> binList = new HashMap<Integer, Bin>();
    private static boolean initDonné = false;
    private static int tailleBin = 0; // En attendant comme j'ai pas la classe
    private static int nbItems = 0; // tu changeras avec la classe
    private static ArrayList<Integer> itemList = new ArrayList<>();

    public static void lireFichier(String fichier) {
        try {
            bufferedR = new BufferedReader(new InputStreamReader(new FileInputStream(fichier), "utf-8"));
            String line;
            String[] mots = null;
            while ((line = bufferedR.readLine()) != null) {
                mots = line.split(" ");
                if (!initDonné) {
                    tailleBin = Integer.parseInt(mots[0]);
                    nbItems = Integer.parseInt(mots[1]);
                    initDonné = true;
                    line = bufferedR.readLine();
                    mots = line.split(" ");
                }

                for (String wrd : mots) {
                    if (wrd.matches("[+-]?\\d*(\\.\\d+)?")) {
                        itemList.add(Integer.parseInt(wrd));
                    }
                }
            }
        } catch (

                FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void firstFitDecreasing() {
        int compteur = 1;
        // Tri selon ordre croissant
        itemList = (ArrayList<Integer>) itemList.stream().sorted().collect(Collectors.toList());
        // Inverse la liste
        Collections.reverse(itemList);
        for (int i = 0; i < itemList.size(); i++) {
            // Si c'est le premier element on initalise la hashmap
            if (binList.size() == 0) {
                Bin binTmp = new Bin(tailleBin);
                binTmp.setItemList(new ArrayList<>());
                binList.put(compteur, binTmp);
            }
            // si l'item peut rentrer dans le bin on le met
            if (binList.get(compteur).getItemList().stream().collect(Collectors.summingInt(Integer::intValue)) + itemList.get(i) <= tailleBin) {
                    binList.get(compteur).getItemList().add(itemList.get(i));
            } else { // sinon on crée un autre bin et on incremente compteur pour pointer sur ce nouveau bin
                compteur++;
                Bin newBin = new Bin(tailleBin);
                newBin.setItemList(new ArrayList<>());
                newBin.getItemList().add(itemList.get(i));
                binList.put(compteur, newBin);
            }
        }
    }

    public static void afficherBin() {
        int compteur = 0;
        for (Map.Entry<Integer, Bin> mapentry : binList.entrySet()) {
            for (int i = 0; i < mapentry.getValue().getItemList().size(); i++) {
                System.out.println("ITEM : " + mapentry.getValue().getItemList().get(i));
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Quel fichier voulez vous tester ?");
        Scanner sc = new Scanner(System.in);
        String fichier = sc.nextLine();
        lireFichier("src/data/" + fichier + ".txt");
        firstFitDecreasing();
        System.out.println("Nombre de bin : " + binList.size());
        afficherBin();

    }
}
