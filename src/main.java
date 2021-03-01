import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

public class main {
    private static BufferedReader bufferedR;
    private static boolean initDonné = false;
    private static int nbBin = 0; // En attendant comme j'ai pas la classe
    private static int nbItems = 0; // tu changeras avec la classe

    public static void lireFichier(String fichier) {
        try {
            bufferedR = new BufferedReader(new InputStreamReader(new FileInputStream(fichier), "utf-8"));
            String line;
            String[] mots = null;
            while ((line = bufferedR.readLine()) != null) {
                mots = line.split(" ");
                if (!initDonné) {
                    nbBin = Integer.parseInt(mots[0]);
                    nbItems = Integer.parseInt(mots[1]);
                    initDonné = true;
                }
                    for (String wrd : mots) {
                        if (wrd.matches("[+-]?\\d*(\\.\\d+)?")) {
                            System.out.println(wrd);
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

    public static void main(String[] args) {
        System.out.println("Quel fichier voulez vous tester ?");
        Scanner sc = new Scanner(System.in);
        String fichier = sc.nextLine();
        lireFichier("src/data/" + fichier + ".txt");
    }
}
