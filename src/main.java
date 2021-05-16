import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.lang.*;
import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

public class main {
    private static BufferedReader bufferedR;
    private static Bin bin;
    private static HashMap<Integer, Bin> binList = new HashMap<Integer, Bin>();
    private static boolean initDonné = false;
    private static int tailleBin = 0; // En attendant comme j'ai pas la classe
    private static int nbItems = 0; // tu changeras avec la classe
    private static ArrayList<Integer> itemList = new ArrayList<>();
    private static int borneInferieur = 0;
    private static int timeLimit = 800000;

    private static void reset() {
        tailleBin = 0;
        nbItems = 0;
        itemList = new ArrayList<>();
        initDonné = false;
        binList = new HashMap<Integer, Bin>();
    }

    public static void lireFichier(String fichier) {
        try {
            reset();
            bufferedR = new BufferedReader(new InputStreamReader(new FileInputStream(fichier), "utf-8"));
            String line;
            String[] mots = null;
            while ((line = bufferedR.readLine()) != null) {
                mots = line.split(" ");
                if (!initDonné) {
                    tailleBin = Integer.parseInt(mots[0]);
                    System.out.println("La taille d'un bin est de " + tailleBin);
                    nbItems = Integer.parseInt(mots[1]);
                    System.out.println("Il y a " + nbItems + " items dans le fichier");
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

    public static void calculBorneInferieure(){
        int sommeTailleItems = itemList.stream().collect(Collectors.summingInt(Integer::intValue));
        borneInferieur = sommeTailleItems / tailleBin;
        if (sommeTailleItems % tailleBin != 0) {
            borneInferieur++;
        }
    }

    public static void firstFitDecreasing() {
        int compteur = 1;
        boolean isNewBin = true;
        // Tri selon ordre croissant
        itemList = (ArrayList<Integer>) itemList.stream().sorted().collect(Collectors.toList());
        // Inverse la liste
        Collections.reverse(itemList);
        for (int i = 0; i < itemList.size(); i++) {
            isNewBin = true;
            // Si c'est le premier element on initalise la hashmap
            if (binList.size() == 0) {
                Bin binTmp = new Bin(tailleBin);
                binTmp.setItemList(new ArrayList<>());
                binList.put(compteur, binTmp);
            }
            // si l'item peut rentrer dans le bin on le met
            for(Map.Entry<Integer, Bin> mapentry : binList.entrySet()) {

                if (mapentry.getValue().getItemList().stream().collect(Collectors.summingInt(Integer::intValue)) + itemList.get(i) <= tailleBin) {
                    mapentry.getValue().getItemList().add(itemList.get(i));
                    isNewBin = false;
                    break;
                }
            }
            if(isNewBin) { // sinon on crée un autre bin et on incremente compteur pour pointer sur ce nouveau bin
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
            System.out.println("----------------------");
        }
    }

    public static void linearSolver() {
        String str;
        MPSolver solver;
        Loader.loadNativeLibraries();
        solver = new MPSolver("BinPacking1d", MPSolver.OptimizationProblemType.SAT_INTEGER_PROGRAMMING);
        //solver.setTimeLimit(timeLimit);

        System.out.println("Création des variables ... ");
        MPVariable[][] x = new MPVariable[nbItems][nbItems];
        //x : la variable booléenne
        //Vrai si l'item i est dans le bin j
        //Faux sinon
        for(int i = 0; i < nbItems; i++){
            for(int j = 0; j < nbItems; j++){
                str = "X[" + i + "," + j + "]";
                x[i][j] = solver.makeBoolVar(str);
            }
        }
        MPVariable[] y = new MPVariable[nbItems];
        for (int j = 0; j < nbItems; j++){
            str = "Y" + j;
            y[j] = solver.makeBoolVar(str);
        }
        System.out.println("#" + solver.numVariables());

        // constraints
        System.out.println("Création des contraintes ... ");
        MPConstraint[] constraint1 = new MPConstraint[nbItems];
        for (int i = 0; i < nbItems; i++){
            constraint1[i] = solver.makeConstraint(1,1);
            for(int j = 0; j < nbItems; j++){
                constraint1[i].setCoefficient(x[i][j],1);
            }
        }
        MPConstraint[] constraint2 = new MPConstraint[nbItems];
        for(int j = 0; j < nbItems; j++){
            constraint2[j] = solver.makeConstraint(-tailleBin, 0);
            for (int i = 0; i < nbItems; i++){
                constraint2[j].setCoefficient(x[i][j], itemList.get(i));
                constraint2[j].setCoefficient(y[j], -tailleBin);
            }
        }
        System.out.println("#" + solver.numConstraints());


        MPObjective objective = solver.objective();
        for (int i = 0; i < nbItems; i++){
            objective.setCoefficient(y[i],1);
        }
        objective.setMinimization();

        long begin = System.currentTimeMillis();
        MPSolver.ResultStatus statut = solver.solve();
        long time = System.currentTimeMillis() - begin;
        if(statut == MPSolver.ResultStatus.OPTIMAL){
            System.out.println("Solution optimale trouvée " + (int)objective.value());
            System.out.println("Trouvé en : " + time + " millisecondes");
        }
    }

    public static void testAllFilesWithFirstFitDecreasing(){
        ArrayList<String> nomFichierList = new ArrayList<>();
        nomFichierList.add("00");
        nomFichierList.add("01");
        nomFichierList.add("02");
        nomFichierList.add("03");
        nomFichierList.add("04");
        nomFichierList.add("05");
        nomFichierList.add("06");
        nomFichierList.add("11");
        nomFichierList.add("12");
        nomFichierList.add("13");
        nomFichierList.add("14");
        nomFichierList.add("21");
        nomFichierList.add("31");
        for (int i = 0; i < nomFichierList.size(); i++) {
            System.out.println("Test avec FirstFitDecreasing pour le fichier : binpack1d_" + nomFichierList.get(i));
            lireFichier("src/data/binpack1d_" + nomFichierList.get(i) + ".txt");
            firstFitDecreasing();
            System.out.println("----------------------");
            System.out.println("Nombre de bin : " + binList.size());
            calculBorneInferieure();
            System.out.println("Borne inférieure : " + borneInferieur);
            System.out.println("----------------------");
            afficherBin();
            System.out.println("----------------------");
        }
    }


    public static void main(String[] args) {
        testAllFilesWithFirstFitDecreasing();
//        System.out.println("Quel fichier voulez vous tester ?");
//        Scanner sc = new Scanner(System.in);
//        String fichier = sc.nextLine();
//        lireFichier("src/data/" + fichier + ".txt");
//        firstFitDecreasing();
//        System.out.println("Nombre de bin : " + binList.size());
//        calculBorneInferieure();
//        System.out.println("Borne inférieure : " + borneInferieur);
//        //afficherBin();
//        lireFichier("src/data/binpack1d_01.txt");
//        linearSolver();

    }
}
