import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

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

    public static void calculBorneInferieure() {
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
            for (Map.Entry<Integer, Bin> mapentry : binList.entrySet()) {

                if (mapentry.getValue().getItemList().stream().collect(Collectors.summingInt(Integer::intValue)) + itemList.get(i) <= tailleBin) {
                    mapentry.getValue().getItemList().add(itemList.get(i));
                    isNewBin = false;
                    break;
                }
            }
            if (isNewBin) { // sinon on crée un autre bin et on incremente compteur pour pointer sur ce nouveau bin
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

    public static void afficherBin2(HashMap<Integer, Bin> binn) {
        int compteur = 0;
        for (Map.Entry<Integer, Bin> mapentry : binn.entrySet()) {
            compteur = 0;
            for (int i = 0; i < mapentry.getValue().getItemList().size(); i++) {
                System.out.println("ITEM : " + mapentry.getValue().getItemList().get(i));
                compteur += mapentry.getValue().getItemList().get(i);
            }
            System.out.println("SOMME RESTANTE : " + (tailleBin - compteur));
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
        for (int i = 0; i < nbItems; i++) {
            for (int j = 0; j < nbItems; j++) {
                str = "X[" + i + "," + j + "]";
                x[i][j] = solver.makeBoolVar(str);
            }
        }
        MPVariable[] y = new MPVariable[nbItems];
        for (int j = 0; j < nbItems; j++) {
            str = "Y" + j;
            y[j] = solver.makeBoolVar(str);
        }
        System.out.println("#" + solver.numVariables());

        // constraints
        System.out.println("Création des contraintes ... ");
        MPConstraint[] constraint1 = new MPConstraint[nbItems];
        for (int i = 0; i < nbItems; i++) {
            constraint1[i] = solver.makeConstraint(1, 1);
            for (int j = 0; j < nbItems; j++) {
                constraint1[i].setCoefficient(x[i][j], 1);
            }
        }
        MPConstraint[] constraint2 = new MPConstraint[nbItems];
        for (int j = 0; j < nbItems; j++) {
            constraint2[j] = solver.makeConstraint(-tailleBin, 0);
            for (int i = 0; i < nbItems; i++) {
                constraint2[j].setCoefficient(x[i][j], itemList.get(i));
                constraint2[j].setCoefficient(y[j], -tailleBin);
            }
        }
        System.out.println("#" + solver.numConstraints());


        MPObjective objective = solver.objective();
        for (int i = 0; i < nbItems; i++) {
            objective.setCoefficient(y[i], 1);
        }
        objective.setMinimization();

        long begin = System.currentTimeMillis();
        MPSolver.ResultStatus statut = solver.solve();
        long time = System.currentTimeMillis() - begin;
        if (statut == MPSolver.ResultStatus.OPTIMAL) {
            System.out.println("Solution optimale trouvée " + (int) objective.value());
            System.out.println("Trouvé en : " + time + " millisecondes");
        }
    }

    public static void testAllFilesWithFirstFitDecreasing() {
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
            //System.out.println("Test avec FirstFitDecreasing pour le fichier : binpack1d_" + nomFichierList.get(i));
            System.out.println("Test avec FirstFitRandom pour le fichier : binpack1d_" + nomFichierList.get(i));
            lireFichier("src/data/binpack1d_" + nomFichierList.get(i) + ".txt");
            firstFitDecreasing();
            //firstFitRandom();
            System.out.println("----------------------");
            System.out.println("Nombre de bin : " + binList.size());
            calculBorneInferieure();
            System.out.println("Borne inférieure : " + borneInferieur);
            System.out.println("----------------------");
            afficherBin();
            System.out.println("----------------------");
            System.out.println(voisinageA());
            System.out.println(voisinageB());
            //randomGenerateA();
            //System.out.println("Générateur aléatoire A : " + binList.size());

        }
    }

    public static void randomGenerateA() {
        int compteur = 1;
        for (int i = 0; i < nbItems; i++) {
            compteur++;
            Bin newBin = new Bin(tailleBin);
            newBin.setItemList(new ArrayList<>());
            newBin.getItemList().add(itemList.get(i));
            binList.put(compteur, newBin);
        }
    }

    public static void firstFitRandom() {
        int compteur = 1;
        boolean isNewBin = true;
        Collections.shuffle(itemList);
        for (int i = 0; i < itemList.size(); i++) {
            isNewBin = true;
            // Si c'est le premier element on initalise la hashmap
            if (binList.size() == 0) {
                Bin binTmp = new Bin(tailleBin);
                binTmp.setItemList(new ArrayList<>());
                binList.put(compteur, binTmp);
            }
            // si l'item peut rentrer dans le bin on le met
            for (Map.Entry<Integer, Bin> mapentry : binList.entrySet()) {

                if (mapentry.getValue().getItemList().stream().collect(Collectors.summingInt(Integer::intValue)) + itemList.get(i) <= tailleBin) {
                    mapentry.getValue().getItemList().add(itemList.get(i));
                    isNewBin = false;
                    break;
                }
            }
            if (isNewBin) { // sinon on crée un autre bin et on incremente compteur pour pointer sur ce nouveau bin
                compteur++;
                Bin newBin = new Bin(tailleBin);
                newBin.setItemList(new ArrayList<>());
                newBin.getItemList().add(itemList.get(i));
                binList.put(compteur, newBin);
            }

        }
    }

    public static String voisinageA() {
        Random r = new Random();
        ArrayList<Integer> listB = new ArrayList<>(binList.keySet());
        int random = r.nextInt(listB.size());
        while (!listB.isEmpty()) {
            for (int i = 0; i < binList.get(listB.get(random)).getItemList().size(); i++) {
                for (int j = 1; j < binList.keySet().size() + 1; j++) {
                    if (binList.get(j).getItemList().stream().collect(Collectors.summingInt(Integer::intValue)) + binList.get(listB.get(random)).getItemList().get(i) <= tailleBin && binList.get(j) != binList.get(listB.get(random))) {
                        binList.get(j).getItemList().add(binList.get(listB.get(random)).getItemList().get(i));
                        binList.get(listB.get(random)).getItemList().remove(binList.get(listB.get(random)).getItemList().get(i));
                        return "L'item " + binList.get(j).getItemList().get(binList.get(j).getItemList().size() - 1) + " du bin " + listB.get(random) + " a été déplacé dans le bin " + j;
                    }
                }
            }
            listB.remove(listB.get(random));
            if (!listB.isEmpty()) {
                random = r.nextInt(listB.size());
            }
        }
        return "Il est impossible de déplacer un item d'un bin dans un autre bin";

    }

    public static String voisinageB() {
        Random r = new Random();
        ArrayList<Integer> listB = new ArrayList<>(binList.keySet());
        int random = r.nextInt(listB.size());
        while (!listB.isEmpty()) {
            for (int i = 0; i < binList.get(listB.get(random)).getItemList().size(); i++) {
                for (int j = 1; j < binList.keySet().size(); j++) {
                    for (int k = 0; k < binList.get(j).getItemList().size(); k++) {
                        if (binList.get(j).getItemList().stream().collect(Collectors.summingInt(Integer::intValue)) + binList.get(listB.get(random)).getItemList().get(i) - binList.get(j).getItemList().get(k) <= tailleBin && binList.get(j) != binList.get(listB.get(random))) {
                            if (binList.get(listB.get(random)).getItemList().stream().collect(Collectors.summingInt(Integer::intValue)) - binList.get(listB.get(random)).getItemList().get(i) + binList.get(j).getItemList().get(k) <= tailleBin && binList.get(listB.get(random)).getItemList().get(i).intValue() != binList.get(j).getItemList().get(k).intValue()) {
                                binList.get(j).getItemList().add(binList.get(listB.get(random)).getItemList().get(i));
                                binList.get(listB.get(random)).getItemList().add(binList.get(j).getItemList().get(k));
                                binList.get(listB.get(random)).getItemList().remove(binList.get(listB.get(random)).getItemList().get(i));
                                binList.get(j).getItemList().remove(k);
                                return "L'item " + binList.get(listB.get(random)).getItemList().get(binList.get(listB.get(random)).getItemList().size() - 1) + " du bin " + j + " a été déplacé dans le bin " + listB.get(random) + "\n" +
                                        "Inversement : l'item " + binList.get(j).getItemList().get(binList.get(j).getItemList().size() - 1) + " du bin " + listB.get(random) + " a été déplacé dans le bin " + j;
                            }
                        }
                    }
                }
            }
            listB.remove(listB.get(random));
            if (!listB.isEmpty()) {
                random = r.nextInt(listB.size());
            }
        }
        return "Il est impossible d'échanger deux items de deux bins différents";

    }

    public static int calculerFitness(HashMap<Integer, Bin> binHashMap) {
        int somme = 0;
        int addition = 0;
        for (Map.Entry<Integer, Bin> mapentry : binHashMap.entrySet()) {
            addition = 0;
            for (int i = 0; i < mapentry.getValue().getItemList().size(); i++) {
                addition += mapentry.getValue().getItemList().get(i);
            }
            somme += addition * addition;
        }
        return somme;
    }

    public static ArrayList<Integer> getRandomElement(ArrayList<Integer> list) {
        ArrayList<Integer> listReturn = new ArrayList<Integer>();
        Random r = new Random();
        int rand = r.nextInt(list.size());
        int item = list.get(rand);
        listReturn.add(rand);
        listReturn.add(item);
        return listReturn;
    }

    public static boolean deplacementItemVersBin(Bin ancienBin, ArrayList<Integer> item, Bin nouveauBin) {
        if (nouveauBin.getItemList().stream().collect(Collectors.summingInt(Integer::intValue)) + item.get(1) <= tailleBin && ancienBin != nouveauBin) {//on verifie que l'item peut intégrer l'autre bin
            int indice1 = item.get(0);
            if (ancienBin.getItemList().size() == 0) {

            }
            ancienBin.getItemList().remove(indice1); //on supprime l'item de l'ancien bin
            nouveauBin.getItemList().add(item.get(1)); //on ajoute l'item dans l'autre bin
            return true;
        }
        return false;
    }

    public static boolean echangeItem(Bin bin1, ArrayList<Integer> item1, Bin bin2, ArrayList<Integer> item2) {
        if (bin1.getItemList().stream().collect(Collectors.summingInt(Integer::intValue)) + item2.get(1) - item1.get(1) <= tailleBin && bin1 != bin2) {
            if (bin2.getItemList().stream().collect(Collectors.summingInt(Integer::intValue)) + item1.get(1) - item2.get(1) <= tailleBin) {
                int indice1 = item1.get(0);
                int indice2 = item2.get(0);
                bin1.getItemList().remove(indice1);
                bin2.getItemList().remove(indice2);
                bin1.getItemList().add(item2.get(1));
                bin2.getItemList().add(item1.get(1));
                return true;
            }
        }
        return false;
    }

    public static HashMap<Integer, Bin> voisin(HashMap<Integer, Bin> listBin) {
        Random r = new Random();
        //int random = r.nextInt(listB.size());
        ArrayList<Integer> listB = new ArrayList<>(listBin.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList()));
        while (true) {
            int indiceBin = getRandomElement(listB).get(1);
            ArrayList<Integer> listItem1 = listBin.get(indiceBin).getItemList();
            ArrayList<Integer> randomItem = getRandomElement(listItem1);
            int indiceBin2 = getRandomElement(listB).get(1);
            ArrayList<Integer> listItem2 = listBin.get(indiceBin2).getItemList();
            ArrayList<Integer> randomItem2 = getRandomElement(listItem2);
            int randomMoveOrChange = r.nextInt(2);
            if (randomMoveOrChange == 0) {
                if (deplacementItemVersBin(listBin.get(indiceBin), randomItem, listBin.get(indiceBin2))) {
                    if (listBin.get(indiceBin).getItemList().isEmpty()) {
                        listBin.remove(indiceBin);
                    }
                    return listBin;
                }
            } else {
                if (echangeItem(listBin.get(indiceBin), randomItem, listBin.get(indiceBin2), randomItem2)) {
                    return listBin;
                }
            }
        }
    }

    public static HashMap<Integer, Bin> algoRecuitSimulé() {
        ArrayList<HashMap<Integer, Bin>> x = new ArrayList<>();
        x.add(binList);
        HashMap<Integer, Bin> xMin = x.get(0);
        ArrayList<Double> temperature = new ArrayList<>();
        temperature.add(10000.0);
        int i = 0;
        ArrayList<Integer> fitness = new ArrayList<>();
        fitness.add(calculerFitness(x.get(i)));
        int fitnessMin = fitness.get(0);

        for (int k = 0; k < 20; k++) {
            for (int l = 1; l < 10; l++) {
                HashMap<Integer, Bin> y = voisin(x.get(i));
                int deltaF = calculerFitness(y) - calculerFitness(x.get(i));
                fitness.add(calculerFitness(y));
                if (deltaF <= 0) {
                    x.add(i + 1, y);
                    if (fitness.get(i + 1) < fitnessMin) {
                        xMin = x.get(i + 1);
                        fitnessMin = fitness.get(i + 1);
                    }

                } else {
                    double p = 0.7;
                    if (p <= Math.exp(-deltaF / temperature.get(k))) {
                        x.add(i + 1, y);
                    } else {
                        x.add(i + 1, x.get(i));
                    }
                }
                i = i + 1;

            }
            temperature.add(k + 1, 0.9 * temperature.get(k));
        }
        System.out.println("FINI");
        afficherBin2(xMin);
        System.out.println("FITNESS INITIAL : " + fitness.get(0));
        System.out.println("FITNESS MIN : " + fitnessMin);
        return xMin;
    }


    public static void main(String[] args) {
//        testAllFilesWithFirstFitDecreasing();
//        System.out.println("Quel fichier voulez vous tester ?");
//        Scanner sc = new Scanner(System.in);
//        String fichier = sc.nextLine();
//        lireFichier("src/data/" + fichier + ".txt");
//        firstFitDecreasing();
//        System.out.println("Nombre de bin : " + binList.size());
//        calculBorneInferieure();
//        System.out.println("Borne inférieure : " + borneInferieur);
//        afficherBin();
//        voisinageA();
//        afficherBin();
        lireFichier("src/data/binpack1d_02.txt");
        firstFitDecreasing();
        afficherBin2(binList);
        algoRecuitSimulé();
//        linearSolver();

    }
}
