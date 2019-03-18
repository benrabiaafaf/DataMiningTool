import weka.core.Instances;
import java.util.*;

public class Apriori {

    private ArrayList<ArrayList<String>> transactions;
    private ArrayList<Object> frequent;
    private ArrayList<Object> rules;
    private int nb_c;
    private Instances instances;

    private int min_sup;
    private float min_confidence;

    public Apriori(Instances instances, int min_sup, float min_confidence) {

        this.instances = instances;
        this.min_sup = min_sup;
        this.min_confidence = min_confidence;

        this.frequent = new ArrayList<>();
        this.rules = new ArrayList<>();

        process();

    }

    private void process(){

        read_transactions();
        
        ArrayList<ArrayList<String>> C = generate_first_itemsets();
        
        this.nb_c = 1;
        for (ArrayList<String> item: C) {
            this.frequent.add(item);
            this.frequent.add(count_support(item));

        }

        while (true){
            System.out.println("C"+this.nb_c+" : "+C.size());
            C = generate_itemsets(C);
            if(C.size() == 0)
                break;
            this.nb_c++;
            for (ArrayList<String> item: C) {
                this.frequent.add(item);
                this.frequent.add(count_support(item));
                ArrayList<Object> rules = generate_rules(item);
                this.rules.addAll(rules);
            }
        }
    }

    private ArrayList<ArrayList<String>> generate_itemsets(ArrayList<ArrayList<String>> itemsets){

        ArrayList<ArrayList<String>> generated_itemsets = new ArrayList<>();
        ArrayList<String> generated_itemset;
        int size = itemsets.get(0).size()+1;
        Set set ;
        ArrayList<String> array;
        for (int i = 0 ; i < itemsets.size() ; i++){
            for (int j = i; j < itemsets.size() ; j++) {
                array = new ArrayList<>(itemsets.get(i));
                array.addAll(itemsets.get(j));
                set = new HashSet(array);
                generated_itemset = new ArrayList<>(set);
                if (generated_itemset.size() == size && !generated_itemsets.contains(generated_itemset)) {

                    boolean bool = true;
                    for (ArrayList<String> subset : generate_subsets(generated_itemset)) {
                        if (count_support(subset) < this.min_sup) {
                            bool = false;
                            break;
                        }
                    }
                    if (bool == true)
                        generated_itemsets.add(generated_itemset);

                }
            }
        }

        return generated_itemsets;
    }

    private void read_transactions(){

        this.transactions = new ArrayList<>();
        String s ;
        int i = 0;
        while (i < this.instances.numInstances() && i <100){
            s = instances.instance(i).toString();
            ArrayList<String> list = new ArrayList<>(Arrays.asList(s.split(",")));
            Collections.sort(list);
            this.transactions.add(list);
            i++;

        }

    }

    private int count_support(ArrayList<String> itemset){
        int cpt = 0;
        boolean bool ;
        for (ArrayList<String> transaction : this.transactions){
            bool = true;
            for (String item: itemset){
                if(!transaction.contains(item)){
                    bool = false;
                    break;
                }
            }
            if (bool)
                cpt++;
        }
        return cpt;
    }

    private ArrayList<ArrayList<String>> generate_first_itemsets(){

        HashMap<String,Integer> map = new HashMap<>();
        for (ArrayList<String> transaction : this.transactions){
            for (String item : transaction){
                if (map.containsKey(item))
                    map.put(item,map.get(item)+1);
                else
                    map.put(item,1);
            }
        }

        ArrayList<ArrayList<String>> list = new ArrayList<>();
        for (Map.Entry entry : map.entrySet()){
            if ((Integer) entry.getValue() >= this.min_sup) {
                list.add(new ArrayList(Arrays.asList(entry.getKey())));
            }
        }
        return list ;
    }

    private ArrayList<ArrayList<String>> generate_subsets(ArrayList<String> items){

        ArrayList<ArrayList<String>> subsets = new ArrayList<>();
        for (long i = 1; i < Math.pow(2, items.size()); i++ ) {
            ArrayList<String> itemset = new ArrayList<>();
            for ( int j = 0; j < items.size(); j++ ) {
                if ( (i & (long) Math.pow(2, j)) > 0 ) {
                    itemset.add(items.get(j));
                }
                subsets.add(itemset);
            }
        }
        Set<ArrayList<String>> set = new HashSet<>(subsets);
        subsets.clear();
        subsets.addAll(set);
        Collections.sort(subsets, new Comparator<ArrayList<String>>() {
            @Override
            public int compare(ArrayList<String> o1, ArrayList<String> o2) {
                return o1.size()-o2.size();
            }
        });
        return subsets;
    }

    private ArrayList<Object> generate_rules(ArrayList<String> itemset){

        ArrayList<Object> rules = new ArrayList<>();
        ArrayList<ArrayList<String>> subsets = generate_subsets(itemset);
        ArrayList<String> conclusion;
        float confidence;

        for (ArrayList<String> subset : subsets){

            conclusion = (ArrayList<String>) itemset.clone();
            conclusion.removeAll(subset);
            if (conclusion.size() == 0)
                continue;

            confidence = ((float)count_support(itemset)/count_support(subset))*100;
            if (confidence >= this.min_confidence){
                rules.add(subset);
                rules.add(conclusion);
                rules.add(confidence);
            }
        }

        return rules;
    }

    public List<List<Object>> getRules(){
        List<List<Object>> list = new ArrayList<>();
        int i =0;
        while (i < this.rules.size()){
            list.add(Arrays.asList(this.rules.get(i), this.rules.get(i + 1),this.rules.get(i+2)));
            i+=3;
        }

        return  list;
    }

    public List<List<Object>> getFrequent(){

        List<List<Object>> list = new ArrayList<>();
        int i =0;
        while (i < this.frequent.size()){
            list.add(Arrays.asList(this.frequent.get(i), this.frequent.get(i + 1)));
            i+=2;
        }
        return  list;
    }

    public List<List<Object>> getFrequent(int len){
        int i = 0;
        ArrayList<Object> temp = new ArrayList<>();
        while ( i < this.frequent.size()){
            if(((ArrayList<String>)this.frequent.get(i)).size() == len){
                temp.add(this.frequent.get(i));
                temp.add(this.frequent.get(i+1));
            }
            i +=2;
        }
        List<List<Object>> list = new ArrayList<>();
        i =0;
        while (i < temp.size()){
            list.add(Arrays.asList(temp.get(i), temp.get(i + 1)));
            i+=2;
        }
        return  list;
    }

    public int getNb_c(){return this.nb_c;}
}

