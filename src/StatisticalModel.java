import javafx.beans.property.*;

public class StatisticalModel {

    private final StringProperty name;
    private final StringProperty type;
    private final StringProperty outliers;
    private final StringProperty missing;
    private final StringProperty min;
    private final StringProperty max;
    private final StringProperty mean;
    private final StringProperty q1;
    private final StringProperty q2;
    private final StringProperty q3;
    private final StringProperty modes;
    private final StringProperty range;
    private final StringProperty midrange;

    public StatisticalModel(String name, String type, String min, String max, String mean, String q1, String q2, String q3, String modes, String range, String midrange, String outliers, String missing) {
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.outliers = new SimpleStringProperty(outliers);
        this.missing = new SimpleStringProperty(missing);
        this.min = new SimpleStringProperty(min);
        this.max = new SimpleStringProperty(max);
        this.mean = new SimpleStringProperty(mean);
        this.q1 = new SimpleStringProperty(q1);
        this.q2 = new SimpleStringProperty(q2);
        this.q3 = new SimpleStringProperty(q3);
        this.modes = new SimpleStringProperty(modes);
        this.range = new SimpleStringProperty(range);
        this.midrange = new SimpleStringProperty(midrange);
    }


    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty typeProperty() {
        return type;
    }

    public StringProperty outliersProperty() {
        return outliers;
    }

    public StringProperty missingProperty() {
        return missing;
    }

    public StringProperty minProperty() {
        return min;
    }

    public StringProperty maxProperty() {
        return max;
    }

    public StringProperty meanProperty() {
        return mean;
    }

    public StringProperty Q1Property() {
        return q1;
    }

    public StringProperty Q2Property() {
        return q2;
    }

    public StringProperty Q3Property() {
        return q3;
    }

    public StringProperty modesProperty() {
        return modes;
    }

    public StringProperty rangeProperty() {
        return range;
    }

    public StringProperty midrangeProperty() {
        return midrange;
    }
}
