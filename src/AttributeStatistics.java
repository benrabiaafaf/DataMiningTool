import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.*;

import static java.util.stream.Collectors.toMap;

public class AttributeStatistics {

    private Instances instances;
    private Instances normalized_instances;
    private int att_indx;
    private String name;
    private String type;
    private Double q1;
    private Double q2;
    private Double q3;
    private Double mean;
    private Double min;
    private Double max;
    private Double range;
    private Double midrange;
    private int missing;
    private int outliers;
    private TreeMap<Object, Integer> frequencies;
    private ArrayList<Object> modes;
    private int max_frequency;
    private int total;
    private double min_step;
    private boolean first_build_frequencie = false;

    public AttributeStatistics(Instances instances, Instances normalized_instances, int att_indx) {

        this.instances = instances;
        this.normalized_instances = normalized_instances;
        this.att_indx = att_indx;
        this.name = instances.attribute(att_indx).name();
        this.type = Attribute.typeToString(instances.attribute(att_indx));
        this.modes = new ArrayList<>();
        this.frequencies = new TreeMap<>();
        this.missing = 0;
        this.outliers = 0;

        if (!instances.attribute(att_indx).isDate()) {

            buildFrequencies();
            calculateModes();
            //replaceClassMissingValues();
            replaceAtributeMissingValues();
            buildFrequencies();
            calculateModes();
            calculateMin();
            calculateMax();
            calculateMean();
            calculateQ1();
            calculateQ2();
            calculateQ3();
            caclculateRange();
            calculateMiderange();
            detectOutliers();
            //calculateMinStep();
            min_max_normalization();

        }
    }

    private void buildFrequencies() {

        frequencies.clear();
        Object value;
        total = 0;
        max_frequency = 0;
        for (Instance instance : instances) {
            //if (!instance.isMissing(att_indx)) {
            if (!isMissing(instance,att_indx)) {
                if (instances.attribute(att_indx).isNumeric()) {
                    /*** NUMERIC **/
                    value = instance.value(att_indx);
                } else {
                    /*** NOMINAL **/
                    value = instance.stringValue(att_indx).toLowerCase();
                }
                int f;
                if (frequencies.containsKey(value)) {
                    f = frequencies.get(value) + 1;
                } else {
                    f = 1;
                }
                frequencies.put(value, f);
                max_frequency = Math.max(max_frequency, f);
                total++;
            } else {
                if (first_build_frequencie == false)
                    missing++;
            }
        }
        first_build_frequencie = true;
    }

    private void detectOutliers() {

        if (instances.attribute(att_indx).isNumeric()) {


            for (Instance instance : instances) {
                if (!isMissing(instance,att_indx) && (instance.value(att_indx) > q3 + 1.5 *(q3-q1) || instance.value(att_indx) < q1 - 1.5 *(q3-q1))) {
                    outliers++;
                    //System.out.println("\tOutlier : " + instance.value(att_indx));
                }
            }
        }
    }

    private void calculateQ1() {
        if (instances.attribute(att_indx).isNumeric() && !frequencies.isEmpty()) {
            int position = Math.round(total / 4);
            int count = 0;
            for (Object key : frequencies.keySet()) {
                count += frequencies.get(key);
                if (count >= position) {
                    this.q1 = (Double) key;
                    break;
                }
            }
        }
    }

    private void calculateQ2() {
        if (instances.attribute(att_indx).isNumeric() && !frequencies.isEmpty()) {
            int position = Math.round(total / 2);
            int count = 0;
            for (Object key : frequencies.keySet()) {
                count += frequencies.get(key);
                if (count >= position) {
                    this.q2 = (Double) key;
                    break;
                }
            }
        }
    }

    private void calculateQ3() {
        if (instances.attribute(att_indx).isNumeric() && !frequencies.isEmpty()) {
            int position = Math.round(total * 3 / 4);
            int count = 0;
            for (Object key : frequencies.keySet()) {
                count += frequencies.get(key);
                if (count >= position) {
                    this.q3 = (Double) key;
                    break;
                }
            }
        }
    }

    private void calculateMean() {
        if (instances.attribute(att_indx).isNumeric() && !frequencies.isEmpty()) {
            double sum = 0;
            for (Object key : frequencies.keySet()) {
                sum += (double) key * frequencies.get(key);
            }
            this.mean = (sum / total);
        }
    }

    private void calculateMin() {
        if (instances.attribute(att_indx).isNumeric() && !frequencies.isEmpty()) {
            min = (Double) frequencies.firstEntry().getKey();
        }
    }

    private void calculateMax() {
        if (instances.attribute(att_indx).isNumeric() && !frequencies.isEmpty()) {
            max = (Double) frequencies.lastEntry().getKey();
        }
    }

    private void calculateModes() {
        modes = new ArrayList<>();
        if (!frequencies.isEmpty()) {

            for (Object key : frequencies.keySet()) {
                if (frequencies.get(key) == this.max_frequency)
                    modes.add(key);
            }
        }
    }

    private void caclculateRange() {
        if (min != null && max != null)
            this.range = max - min;
    }

    private void calculateMiderange() {
        if (min != null && max != null)
            this.midrange = (max + min) / 2;
    }

    private void replaceClassMissingValues() {

        if (att_indx == instances.numAttributes() - 1) {
            if (modes != null && !modes.isEmpty()) {
                for (Instance instance : instances) {
                    if (isMissing(instance,att_indx)) {
                        if (instances.attribute(att_indx).isNumeric())
                            instance.attribute(att_indx).setWeight((Double) modes.get(0));
                        else
                            instance.attribute(att_indx).setStringValue(String.valueOf(modes.get(0)));
                    }
                }
            }
        }
    }

    private void replaceAtributeMissingValues() {

        // list : key is class_value and item is a list of frequencies of different attribute_values
        HashMap<Object, HashMap<Object, Integer>> list = new HashMap<>();
        Object att_v, class_v;
        HashMap<Object, Integer> tree;
        ArrayList<Instance> missing_instance = new ArrayList<>();

        if (att_indx != instances.classIndex() && !frequencies.isEmpty()) {

            // in this loop we are going to build structures to do some statistics
            for (Instance instance : instances) {

                if (!isMissing(instance,att_indx)) {

                    if (instance.attribute(att_indx).isNumeric()) {
                        att_v = instance.value(att_indx);
                    } else {
                        // is nominal
                        att_v = instance.stringValue(att_indx);
                    }

                    if (instances.classAttribute().isNumeric()) {
                        class_v = instance.classValue();
                    } else {
                        class_v = instance.stringValue(instance.classIndex());
                    }

                    if (list.containsKey(class_v)) {
                        tree = list.get(class_v);
                        if (tree.containsKey(att_v)) {
                            tree.put(att_v, tree.get(att_v) + 1);
                        } else {
                            tree.put(att_v, 1);
                        }
                    } else {
                        tree = new HashMap<>();
                        tree.put(att_v, 1);
                        list.put(class_v, tree);
                    }

                } else {
                    // if attribute value is missing lets keep the instance in a list to be modified after these statistics
                    missing_instance.add(instance);
                }
            }
            // lets replace the missing values here
            for (Instance instance : missing_instance) {

                //get the missing value
                //get it's class value
                if (instance.classAttribute().isNumeric()) {
                    class_v = instance.classValue();
                } else {
                    class_v = instance.stringValue(instance.classIndex());
                }

                if (list.containsKey(class_v)) {
                    tree = list.get(class_v);

                    if (instances.attribute(att_indx).isNominal()) {
                        // if the attribute is nominal replace with the mode for the same class

                        // sort the values for the class by freq
                        // tree is <value, freq>
                        tree = tree.entrySet()
                                .stream()
                                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

                        att_v = tree.keySet().stream().findFirst().get();
                        instance.setValue(att_indx, (String) att_v);

                    } else {
                        // if attribute is numeric calculate the mean for the same class
                        double s, f;
                        s = 0;
                        f = 0;
                        for (Map.Entry<Object, Integer> entry : tree.entrySet()) {
                            s += (double) entry.getKey() * entry.getValue();
                            f += entry.getValue();
                        }
                        s = s / f;
                        s = Double.parseDouble(String.format("%.2f", s).replace(",", "."));
                        instance.setValue(att_indx, s);
                    }
                }else{
                    instance.setValue(att_indx, String.valueOf(modes.get(0)));
                }
            }

        }

    }

    public String getQ1() {
        if (q1 == null)
            return "";
        else
            return String.valueOf(q1);
    }

    public String getQ2() {
        if (q2 == null)
            return "";
        else
            return String.valueOf(q2);
    }

    public String getQ3() {
        if (q3 == null)
            return "";
        else
            return String.valueOf(q3);
    }

    public String getMean() {
        if (mean == null)
            return "";
        else
            return String.format("%.2f", mean).replace(",", ".");
    }

    public String getMin() {

        if (min == null)
            return "";
        else
            return String.valueOf(min);
    }

    public String getMax() {
        if (max == null)
            return "";
        else
            return String.valueOf(max);
    }

    public String getRange() {
        if (range == null)
            return "";
        else
            return String.format("%.2f", range).replace(",", ".");
    }

    public String getMidrange() {
        if (midrange == null)
            return "";
        else
            return String.valueOf(midrange);
    }

    public String getModes() {
        StringBuilder s = new StringBuilder();
        if (!modes.isEmpty()) {
            for (Object o : modes)
                s.append(o + "\n");
        }
        return s.toString();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getMissing() {
        return String.valueOf(missing);
    }

    public String getOutliers() {
        if (instances.attribute(att_indx).isNumeric())
            return String.valueOf(outliers);
        return "";
    }

    public TreeMap<Object, Integer> getFrequencies() {
        return frequencies;
    }

    @Override
    public String toString() {
        return "################\n" + instances.attribute(att_indx).name()
                + "\nfrequencies :" + frequencies
                + "\n min : " + min
                + "\n max : " + max
                + "\n mean : " + mean
                + "\n Q1 : " + q1
                + "\n Q2 : " + q2
                + "\n Q3 : " + q3
                + "\n modes : " + modes
                + "\n total : " + total
                + "\n max_f : " + max_frequency;

    }

    public void min_max_normalization() {
        // we read from instance and change into normalized_instances
        double v, nv;
        if( missing != instances.numInstances()) {  // verifies if not all values are missing

            for (int i = 0; i < instances.numInstances(); i++) {

                if (instances.attribute(att_indx).isNumeric()) {
                    if( min != max){
                        v = instances.instance(i).value(att_indx);
                        nv = (v - min) / (max - min);
                    }else {
                        nv = 1;
                    }
                    normalized_instances.instance(i).setValue(att_indx, nv);

                }
                if (instances.attribute(att_indx).isNominal() && isMissing(normalized_instances.instance(i), att_indx)) {

                    normalized_instances.instance(i).setValue(att_indx, instances.instance(i).stringValue(att_indx));
                }
            }
        }
    }

    public String symmetery() {
        StringBuilder sb = new StringBuilder();
        if (instances.attribute(att_indx).isNumeric() && mean != null && q2 != null && modes.size() > 0) {

            sb.append(name + " : ");
            if (modes.size() > 1)
                sb.append("\n\tMultimodal attribute");
            else {
                //unimodal
                sb.append("\n\tMean = " + getMean());
                sb.append("\n\tMedian = " + getQ2());
                sb.append("\n\tMode = " + modes.get(0));
                if (Double.compare(Double.valueOf(getMean()), Double.valueOf(getQ2())) == 0 && Double.compare(Double.valueOf(getQ2()), (Double) modes.get(0)) == 0)
                    sb.append("\n\tSemmetrical attribute");
                else if ((Double.valueOf(getMean())-(double) modes.get(0)) ==  3*(Double.valueOf(getMean()) - Double.valueOf(getQ2())))
                    sb.append("\n\tModeratly skewed");
                else if (Double.compare(Double.valueOf(getMean()), Double.valueOf(getQ2())) > 0 && Double.compare(Double.valueOf(getQ2()), (Double) modes.get(0)) > 0)
                    sb.append("\n\tPositively skewed");
                else if (Double.compare(Double.valueOf(getMean()), Double.valueOf(getQ2())) < 0 && Double.compare(Double.valueOf(getQ2()), (Double) modes.get(0)) < 0)
                    sb.append("\n\tNegatively skewed");
                else
                    sb.append("\n\tAsymmetrical attribute");
            }
            sb.append("\n\n");
        }
        return sb.toString();
    }

    private void calculateMinStep() {
        // it adds values that have no occurance so that they can be shown in the histogram when max_bean has a great value
        if (instances.attribute(att_indx).isNumeric() && !frequencies.isEmpty()) {
            min_step = max - min;
            Object[] entries = frequencies.entrySet().toArray();

            for (int i = 0; i + 1 < entries.length; i++) {
                Double v1 = (Double) ((Map.Entry) entries[i]).getKey();
                Double v2 = (Double) ((Map.Entry) entries[i + 1]).getKey();
                min_step = Math.min(min_step, v2 - v1);
            }

            for (double v = min + min_step; v <= max; v += min_step) {
                if (!frequencies.containsKey(v))
                    frequencies.put(v, 0);
            }
        }
    }

    private boolean isMissing(Instance instance, int att_indx){
        if (instance.attribute(att_indx).isNominal())
            return  instance.isMissing(att_indx);
            //return instance.stringValue(att_indx).equals("none") || instance.stringValue(att_indx).equals("?") || instance.stringValue(att_indx).equals("");
        else
            return instance.isMissing(att_indx);
    }

}
