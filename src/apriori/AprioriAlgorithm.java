package apriori;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class AprioriAlgorithm {

    private List<Set<Integer>> frequentItemsets;
    private Map<Set<Integer>, Integer> supportCounts;
    private int numTransactions;
    private double minSup;
    private Map<String, Integer> itemToID;
    private List<String> itemNames;

    public AprioriAlgorithm(String filePath, double minSup) {
        this.minSup = minSup;
        this.numTransactions = 0;
        this.itemToID = new HashMap<>();
        this.itemNames = new ArrayList<>();
        this.frequentItemsets = new ArrayList<>();
        this.supportCounts = new HashMap<>();
        readTransactions(filePath);
    }

    public static void main(String[] args) {
        String filePath = "src/apriori/groceries.csv";
        double minSupport = 0.05;

        AprioriAlgorithm apriori = new AprioriAlgorithm(filePath, minSupport);
        apriori.runApriori();
    }

    private void readTransactions(String filePath) {
        try (BufferedReader data_in = new BufferedReader(new FileReader(filePath))) {
            while (data_in.ready()) {
                String line = data_in.readLine();
                if (!line.trim().isEmpty()) {
                    numTransactions++;
                    processTransaction(line);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processTransaction(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, ",");
        Set<Integer> transactionItems = new HashSet<>();

        while (tokenizer.hasMoreTokens()) {
            String itemName = tokenizer.nextToken().trim();
            if (!itemName.isEmpty()) {
                int itemID = itemToID.computeIfAbsent(itemName, k -> {
                    int id = itemToID.size();
                    itemNames.add(itemName);
                    return id;
                });
                transactionItems.add(itemID);
            }
        }


        for (int item : transactionItems) {
            Set<Integer> itemset = new HashSet<>();
            itemset.add(item);
            supportCounts.merge(itemset, 1, Integer::sum);
        }

        List<Integer> sortedItems = new ArrayList<>(transactionItems);
        Collections.sort(sortedItems);
        generatePairsAndUpdateSupport(sortedItems);
    }

    private void generatePairsAndUpdateSupport(List<Integer> transactionItems) {
        int n = transactionItems.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Set<Integer> pair = new HashSet<>();
                pair.add(transactionItems.get(i));
                pair.add(transactionItems.get(j));
                supportCounts.merge(pair, 1, Integer::sum);
            }
        }
    }

    private void runApriori() {
        int k = 1;

        while (!supportCounts.isEmpty()) {
            frequentItemsets.clear();

            for (Map.Entry<Set<Integer>, Integer> entry : supportCounts.entrySet()) {
                Set<Integer> itemset = entry.getKey();
                int supportCount = entry.getValue();
                double support = (double) supportCount / numTransactions;

                if (support >= minSup) {
                    frequentItemsets.add(itemset);
                }
            }

            displayFrequentItemsets(k);

            if (frequentItemsets.isEmpty()) {
                break;
            }

            supportCounts = generateCandidateItemsets(frequentItemsets, k);
            k++;
        }
    }

    private void displayFrequentItemsets(int k) {

        List<Set<Integer>> sortedFrequentItemsets = new ArrayList<>(frequentItemsets);
        sortedFrequentItemsets.sort((itemset1, itemset2) -> {
            double support1 = getSupport(itemset1);
            double support2 = getSupport(itemset2);
            return Double.compare(support1, support2);
        });

        for (Set<Integer> itemset : sortedFrequentItemsets) {
            List<String> itemNamesList = new ArrayList<>();
            for (int itemId : itemset) {
                itemNamesList.add(itemNames.get(itemId));
            }
            System.out.println(itemNamesList + "\t" + getSupport(itemset));
        }
        System.out.println();
    }


    private double getSupport(Set<Integer> itemset) {
        return (double) supportCounts.getOrDefault(itemset, 0) / numTransactions;
    }

    private Map<Set<Integer>, Integer> generateCandidateItemsets(List<Set<Integer>> prevItemsets, int k) {
        Map<Set<Integer>, Integer> candidateSupportCounts = new HashMap<>();


        Set<Set<Integer>> candidateSet = new HashSet<>();
        for (int i = 0; i < prevItemsets.size(); i++) {
            for (int j = i + 1; j < prevItemsets.size(); j++) {
                Set<Integer> itemset1 = prevItemsets.get(i);
                Set<Integer> itemset2 = prevItemsets.get(j);


                List<Integer> list1 = new ArrayList<>(itemset1);
                List<Integer> list2 = new ArrayList<>(itemset2);
                Collections.sort(list1);
                Collections.sort(list2);

                boolean canJoin = true;
                for (int m = 0; m < k - 1; m++) {
                    if (!list1.get(m).equals(list2.get(m))) {
                        canJoin = false;
                        break;
                    }
                }

                if (canJoin) {
                    Set<Integer> candidateItemset = new HashSet<>(itemset1);
                    candidateItemset.addAll(itemset2);

                    if (candidateItemset.size() == k + 1) {
                        candidateSet.add(candidateItemset);
                    }
                }
            }
        }


        for (Set<Integer> candidate : candidateSet) {
            int supportCount = countSupport(candidate);
            if ((double) supportCount / numTransactions >= minSup) {
                candidateSupportCounts.put(candidate, supportCount);
            }
        }

        return candidateSupportCounts;
    }

    private int countSupport(Set<Integer> itemset) {
        int count = 0;
        for (Set<Integer> transaction : supportCounts.keySet()) {
            if (transaction.containsAll(itemset)) {
                count++;
            }
        }
        return count;
    }
}
