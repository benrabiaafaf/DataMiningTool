import weka.core.Instance;
import weka.core.Instances;
import java.util.*;
import java.util.stream.Collectors;

public class DBSCAN {

    private double eps;
    private int min_pts;
    private Instances instances;
    private ArrayList<Instance> inviseted;
    private List<List<Instance>> clusters;
    private List<Instance> noise;
    private List<List<Object>> gravity_centers;
    private double w;
    private double t;
    private List<Object> Gl;


    public DBSCAN(Instances instances,double eps, int min_pts) {
        this.eps = eps;
        this.min_pts = min_pts;
        this.instances = instances;
        this.clusters = new ArrayList<>();
        this.noise = new ArrayList<>();
        this.gravity_centers = new ArrayList<>();
        this.w = 0;
        this.t = 0;
        this.Gl = new ArrayList<>();

        process();
        gravity_center();
        global_gravity();
        inertie_intraclasse();
        inertie_interclasse();
    }

    private void process(){

        this.inviseted =  new ArrayList<>();
        for (Instance instance: this.instances)
            inviseted.add(instance);
        Collections.shuffle(this.inviseted);

        Instance p;

        Collections.shuffle(this.inviseted);

        while (true){
            if (inviseted.isEmpty())
                break;
            p = inviseted.remove(0);
            List<Instance> neighborhood_1 = eps_neighborhood(p);

            if( neighborhood_1.size() >= min_pts){

                List<Instance> cluster = new ArrayList<>();
                cluster.add(p);

                while (!neighborhood_1.isEmpty()){
                    Instance pp = neighborhood_1.remove(0);
                    if (inviseted.isEmpty())
                        break;
                    if (inviseted.contains(pp)){

                        inviseted.remove(pp);
                        List<Instance> neighborhood_2 = eps_neighborhood(p);

                        if( neighborhood_2.size() >= min_pts){
                            neighborhood_1.addAll(neighborhood_2);
                        }
                    }
                    if( ! in_clusters(pp)&& !cluster.contains(pp)){
                        cluster.add(pp);
                    }
                }
                this.clusters.add(cluster);
            }else {
                this.noise.add(p);
            }
        }

        List<List<Instance>> r = new ArrayList<>();
        for(List<Instance> cluster: this.clusters){
           if (cluster.size() < min_pts){
                noise.addAll(cluster);
                r.add(cluster);
           }
        }
        clusters.removeAll(r);

    }

    private List<Instance> eps_neighborhood(Instance p){
        List<Instance> neigboors = new ArrayList<>();
        for (Instance instance: this.instances){
            if (instance != p){
                double v = disimilarity(p, instance);
                if (v < this.eps)
                    neigboors.add(instance);
            }
        }
        return neigboors;
    }

    private boolean in_clusters(Instance p){
        for (List<Instance> cluster:this.clusters){
            if (cluster.contains(p))
                return true;
        }
        return false;
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
            //pour chaque attribut
            for (int att_idx = 0; att_idx < this.instances.numAttributes()-1; att_idx++){
                // si la valeur est manquente
                if (x.isMissing(att_idx) || y.isMissing(att_idx))
                    continue;
                //compter le nombre d'attribut non manquants
                p++;
                // si l'attruibut est numerique
                if (this.instances.attribute(att_idx).isNumeric()){
                    double min = this.instances.attributeStats(att_idx).numericStats.min;
                    double max = this.instances.attributeStats(att_idx).numericStats.max;
                    d +=(Math.abs(x.value(att_idx)-y.value(att_idx)))/(max-min);

                    //si l'attribut est nominal
                }else if(this.instances.attribute(att_idx).isNominal()){
                    if (x.stringValue(att_idx)== y.stringValue(att_idx)){
                        d += 1;
                    }
                }
            }
        }else {
            // si les types d'attributs ne sont pas mixte
            for (int att_idx = 0; att_idx < this.instances.numAttributes()-1; att_idx++){
                if (x.isMissing(att_idx) || y.isMissing(att_idx))
                    continue;
                p++;
                if (this.instances.attribute(att_idx).isNumeric()){
                    d = Math.pow(x.value(att_idx)-y.value(att_idx),2);
                }else if(this.instances.attribute(att_idx).isNominal()){
                    if (x.stringValue(att_idx)== y.stringValue(att_idx)){
                        // m est le nombre d'attributs communs
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

    private double disimilarity(Instance x, List<Object> y){
        String s="";
        for (int att_idx = 1; att_idx < this.instances.numAttributes()-1; att_idx++){
            if(instances.attribute(att_idx).type() != instances.attribute(att_idx-1).type()) {
                s = "mixed";
                break;
            }
        }
        double p = 0 , d = 0, m =0;
        if(s =="mixed"){
            //pour chaque attribut
            for (int att_idx = 0; att_idx < this.instances.numAttributes()-1; att_idx++){
                // si la valeur est manquente
                if (x.isMissing(att_idx) || y.get(att_idx) == null)
                    continue;
                //compter le nombre d'attribut non manquants
                p++;
                // si l'attruibut est numerique
                if (this.instances.attribute(att_idx).isNumeric()){
                    double min = this.instances.attributeStats(att_idx).numericStats.min;
                    double max = this.instances.attributeStats(att_idx).numericStats.max;
                    d +=(Math.abs(x.value(att_idx)-(Double)y.get(att_idx)))/(max-min);

                    //si l'attribut est nominal
                }else if(this.instances.attribute(att_idx).isNominal()){
                    if (x.stringValue(att_idx)== y.get(att_idx).toString()){
                        d += 1;
                    }
                }
            }
        }else {
            // si les types d'attributs ne sont pas mixte
            for (int att_idx = 0; att_idx < this.instances.numAttributes()-1; att_idx++){
                if (x.isMissing(att_idx) || y.get(att_idx)== null)
                    continue;
                p++;
                if (this.instances.attribute(att_idx).isNumeric()){
                    d = Math.pow(x.value(att_idx)-(Double)y.get(att_idx),2);
                }else if(this.instances.attribute(att_idx).isNominal()){
                    if (x.stringValue(att_idx)== y.get(att_idx).toString()){
                        // m est le nombre d'attributs communs
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

    private double disimilarity(List<Object> x, List<Object> y){
        String s="";
        for (int att_idx = 1; att_idx < this.instances.numAttributes()-1; att_idx++){
            if(instances.attribute(att_idx).type() != instances.attribute(att_idx-1).type()) {
                s = "mixed";
                break;
            }
        }
        double p = 0 , d = 0, m =0;
        if(s =="mixed"){
            //pour chaque attribut
            for (int att_idx = 0; att_idx < this.instances.numAttributes()-1; att_idx++){
                // si la valeur est manquente
                if (x.get(att_idx) == null || y.get(att_idx) == null)
                    continue;
                //compter le nombre d'attribut non manquants
                p++;
                // si l'attruibut est numerique
                if (this.instances.attribute(att_idx).isNumeric()){
                    double min = this.instances.attributeStats(att_idx).numericStats.min;
                    double max = this.instances.attributeStats(att_idx).numericStats.max;
                    d += Math.abs((Double)x.get(att_idx)-(Double)y.get(att_idx))/(max-min);
                    //si l'attribut est nominal
                }else if(this.instances.attribute(att_idx).isNominal()){
                    if (x.get(att_idx).toString()== y.get(att_idx).toString()){
                        d += 1;
                    }
                }
            }
        }else {
            // si les types d'attributs ne sont pas mixte
            for (int att_idx = 0; att_idx < this.instances.numAttributes()-1; att_idx++){
                if (x.get(att_idx) == null || y.get(att_idx)== null)
                    continue;
                p++;
                if (this.instances.attribute(att_idx).isNumeric()){
                    d = Math.pow((Double)x.get(att_idx)-(Double)y.get(att_idx),2);
                }else if(this.instances.attribute(att_idx).isNominal()){
                    if (x.get(att_idx).toString() == y.get(att_idx).toString()){
                        // m est le nombre d'attributs communs
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

    private void gravity_center(){
        int nbl;
        List<Object> items ;
        List<String> nominals;
        List<Object> g ;
        Double v;
        String s;
        int nvars = instances.numAttributes()-1;
        for (List<Instance> cluster : this.clusters){
            nbl = cluster.size();
            items = new ArrayList<>();
            for (Instance instance: cluster){
                for(int i = 0; i < nvars ; i++){
                    if (instance.attribute(i).isNumeric() && !instance.isMissing(i)){
                        if (items.size() == nvars ){
                            v = (Double) items.remove(i) + instance.value(i);
                            items.add(i,v);
                        }else {
                            v = instance.value(i);
                            items.add(v);
                        }
                    }else if(instance.attribute(i).isNominal() && !instance.isMissing(i)){
                        if (items.size() == nvars ){
                            s = instance.stringValue(i);
                            nominals = (List<String>) items.remove(i);
                            nominals.add(s);
                            items.add(i,nominals);
                        }else {
                            s = instance.stringValue(i);
                            nominals = new ArrayList<>();
                            nominals.add(s);
                            items.add(i,nominals);
                        }
                    }
                }
            }
            g = new ArrayList<>();
            for (int i = 0; i < nvars ; i++){
                if (instances.attribute(i).isNumeric()){
                    v = (Double) items.get(i)/nbl;
                    g.add(i,v);
                }else if(instances.attribute(i).isNominal()){
                    nominals = (List<String>) items.get(i);
                    s = nominals.stream()
                                .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
                                .entrySet()
                                .stream()
                                .max(Comparator.comparing(Map.Entry::getValue))
                                .get()
                                .getKey();
                    g.add(i,s);
                }
            }
            gravity_centers.add(g);
        }
    }

    private void inertie_intraclasse(){
        int l = 0;
        List<Object> g;
        for(List<Instance> cluster: this.clusters){
            g = this.gravity_centers.get(l);
            l++;
            for (Instance instance: cluster){
                w+= Math.pow(disimilarity(instance,g),2);
            }
        }
    }

    private void inertie_interclasse(){
        for(List<Object> Gi: this.gravity_centers){
            t += Math.pow(disimilarity(Gi,Gl),2);
        }
    }

    private void global_gravity(){

        int nbl = this.instances.numInstances();
        int nvars = this.instances.numAttributes()-1;
        List<Object> items = new ArrayList<>();
        List<String> nominals;
        List<Object> g ;
        Double v;
        String s;
        for (Instance instance: instances){
            for(int i = 0; i < nvars ; i++){
                if (instance.attribute(i).isNumeric() && !instance.isMissing(i)){
                    if (items.size() == nvars ){
                        v = (Double) items.remove(i) + instance.value(i);
                        items.add(i,v);
                    }else {
                        v = instance.value(i);
                        items.add(v);
                    }
                }else if(instance.attribute(i).isNominal() && !instance.isMissing(i)){
                    if (items.size() == nvars ){
                        s = instance.stringValue(i);
                        nominals = (List<String>) items.remove(i);
                        nominals.add(s);
                        items.add(i,nominals);
                    }else {
                        s = instance.stringValue(i);
                        nominals = new ArrayList<>();
                        nominals.add(s);
                        items.add(i,nominals);
                    }
                }
            }
        }
        for (int i = 0; i < nvars ; i++){
            if (instances.attribute(i).isNumeric()){
                v = (Double) items.get(i)/nbl;
                Gl.add(i,v);
            }else if(instances.attribute(i).isNominal()){
                nominals = (List<String>) items.get(i);
                s = nominals.stream()
                        .collect(Collectors.groupingBy(w -> w, Collectors.counting()))
                        .entrySet()
                        .stream()
                        .max(Comparator.comparing(Map.Entry::getValue))
                        .get()
                        .getKey();
                Gl.add(i,s);
            }
        }
    }

    public List<List<Instance>> getClusters() {
        return clusters;
    }

    public List<Instance> getNoise() {
        System.out.println(noise.size());
        return noise;
    }

    public List<List<Object>> getGravity_centers() {
        return gravity_centers;
    }

    public double getW() {
        return w;
    }

    public double getT() {
        return t;
    }

    public List<Object> getGl() {
        return Gl;
    }
}
