import weka.core.Instance;
import weka.core.Instances;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

public class KNN {

    private int K;
    private Instances instances;
    private HashMap<Instance,String> predicats;
    private int well_predicted, bad_predicted;
    private List<Integer> test_indexes;
    private List<Integer> train_indexes;

    public KNN(Instances instances) {
        this.K = 0;
        this.instances = instances;
        this.predicats = new HashMap<>();

        int start = 0;
        int end = this.instances.numInstances()-1;
        int split_idx = this.instances.numInstances()*2/3;

        List<Integer> range = IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());
        Collections.shuffle(range);

        List<Integer> train_indexes = range.subList(0,split_idx);
        List<Integer> test_indexes = range.subList(split_idx+1,end);

        this.test_indexes = test_indexes;
        this.train_indexes = train_indexes;

    }

    public void set_K(int k){
        this.K = k;
        this.well_predicted = 0;
        this.bad_predicted = 0;
        process();
    }

    private void process(){

        LinkedList<Instance> k_voisins ;

        for (int i :test_indexes){

            k_voisins = new LinkedList<>();

            for (int j : train_indexes){
                insert(this.instances.get(i),this.instances.get(j),k_voisins);
                j++;
            }

            HashMap<String,Integer> count = new HashMap<>();
            String classs;
            for (Instance instance: k_voisins){
                if (instances.classAttribute().isNumeric())
                    classs = String.valueOf(instance.value(this.instances.classIndex()));
                else
                    classs = instance.stringValue(this.instances.classIndex());

                if (count.containsKey(classs)) {
                    count.put(classs, count.get(classs) + 1);
                }else{
                    count.put(classs,1);
                }

            }
            count = count
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2, LinkedHashMap::new));
            classs = count.entrySet().iterator().next().getKey();

            this.predicats.put(this.instances.get(i),classs);
             String temp ;
            if (instances.classAttribute().isNumeric())
                temp = String.valueOf(this.instances.get(i).value(this.instances.classIndex()));
            else
                temp = this.instances.get(i).stringValue(this.instances.classIndex());

            //System.out.println(classs+" : "+temp);

            if( classs.equals(temp))
                well_predicted++;
            else
                bad_predicted++;

            i++;
        }
    }

    private void insert(Instance x,Instance y, LinkedList<Instance> items){
        //ordre croissant des dissimilaritées
        if(items.size() == 0){
            items.add(y);
            return;
        }

        double d = disimilarity(x, y);
        double v = 0;
        int i;
        for (i = 0; i< items.size();i++){
            v = disimilarity(x,items.get(i));
            if ( d <= v)
                break;
        }
        if(items.size() ==  this.K && d < v){
            items.add(i,y);
            items.removeLast();
            return;
        }
        if( items.size() != this.K){
            items.add(i,y);
            return;
        }

    }

    private double disimilarity(Instance x, Instance y){
        String s="";
        for (int att_idx = 1; att_idx < this.instances.numAttributes()-1; att_idx++){
            if(instances.attribute(att_idx).type() != instances.attribute(att_idx-1).type()) {
                s = "mixed";
                break;
            }
        }
        double p = 0 , d = 0, m =0;
        if(s =="mixed"){
            for (int att_idx = 0; att_idx < this.instances.numAttributes()-1; att_idx++){
                if (x.isMissing(att_idx) || y.isMissing(att_idx))
                    continue;
                p++;
                if (this.instances.attribute(att_idx).isNumeric()){
                    double min = this.instances.attributeStats(att_idx).numericStats.min;
                    double max = this.instances.attributeStats(att_idx).numericStats.max;
                    d +=(Math.abs(x.value(att_idx)-y.value(att_idx)))/(max-min);
                }else if(this.instances.attribute(att_idx).isNominal()){
                    if (x.stringValue(att_idx)== y.stringValue(att_idx)){
                        d += 1;
                    }
                }
            }
        }else {
            for (int att_idx = 0; att_idx < this.instances.numAttributes()-1; att_idx++){
                if (x.isMissing(att_idx) || y.isMissing(att_idx))
                    continue;
                p++;
                if (this.instances.attribute(att_idx).isNumeric()){
                    d = Math.pow(x.value(att_idx)-y.value(att_idx),2);
                }else if(this.instances.attribute(att_idx).isNominal()){
                    if (x.stringValue(att_idx)== y.stringValue(att_idx)){
                        m++;
                    }
                }
            }
        }
        if(s=="mixed")
            return d/p;
        else if (this.instances.attribute(0).isNumeric())
            return Math.sqrt(d);
        else if (this.instances.attribute(0).isNominal())
            return (p-m)/p;
        return 0;
    }

    public HashMap<Instance, String> getPredicats() {
        return predicats;
    }

    public int getWell_predicted() {
        return well_predicted;
    }

    public int getBad_predicted() {
        return bad_predicted;
    }

    public int getK() {
        return K;
    }

    public List<Integer> get_test_indexes() {
        return test_indexes;
    }

    public int get_testing_size(){
        return this.test_indexes.size();
    }
}
