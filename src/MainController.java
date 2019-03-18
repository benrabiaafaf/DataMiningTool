import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    private static final String data_sources_path = "./data/";
    private static final String data_source_extension = ".arff";

    @FXML
    private ChoiceBox data_source_box;

    @FXML
    private Button normalization_btn;

    @FXML
    private Button symmeterie_btn;

    @FXML
    private Button bigger_attributes_btn;

    @FXML
    private Button bigger_data_btn;

    @FXML
    private Button apriori_btn;

    @FXML
    private Button knn_btn;

    @FXML
    private Button dbscan_btn;

    @FXML
    private Label relation_name_label;

    @FXML
    private Label nb_instances_label;

    @FXML
    private Label nb_attributes_label;

    @FXML
    private TableView attributes_table;

    @FXML
    private TableView data_table;

    @FXML
    private Pane boxplot_pane;

    @FXML
    private BarChart histo_chart;

    private DataSource dataSource;

    public static Instances normal_instances;
    public static Instances normalized_instances;

    private ArrayList<AttributeStatistics> attribute_statistics;
    private ArrayList<StatisticalModel> attribute_models;

    private int nb_bins = 5;
    private int max_bins = 5;

    @FXML
    private void initialize() {

        data_source_box.getSelectionModel().select(-1);

        fill_data_source_box();

        build_attribute_table(attributes_table);

        data_source_box.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (data_source_box.getValue() != null) {
                clear_components();
                dispalay_instances();
            }
        });

        attributes_table.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if ((Integer) newValue >= 0 && normal_instances.attribute((Integer) newValue).isDate())
                    histo_chart.getData().clear();
                else
                    plot_histogram((Integer) newValue);
            }
        });

        normalization_btn.setOnAction(event -> {
            if (normalized_instances != null) {
                TableView tableView = new TableView();
                fill_data_table(tableView, normalized_instances,null);
                tableView.getSelectionModel().select(data_table.getSelectionModel().getSelectedIndex());
                tableView.scrollTo(data_table.getSelectionModel().getSelectedIndex());
                Stage stage = new Stage();
                stage.setTitle(normal_instances.relationName() + " attributes details");
                stage.setScene(new Scene(tableView));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(normalization_btn.getScene().getWindow());
                stage.showAndWait();
            }
        });

        bigger_attributes_btn.setOnAction(event -> {
            if (normal_instances != null) {
                TableView tableView = new TableView();
                build_attribute_table(tableView);
                fill_attributes_table(tableView);
                tableView.getSelectionModel().select(attributes_table.getSelectionModel().getSelectedIndex());
                tableView.scrollTo(attributes_table.getSelectionModel().getSelectedIndex());
                Stage stage = new Stage();
                stage.setTitle(normal_instances.relationName() + " attributes details");
                stage.setScene(new Scene(tableView));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(normalization_btn.getScene().getWindow());
                stage.showAndWait();
            }
        });

        bigger_data_btn.setOnAction(event -> {
            if (normal_instances != null) {
                TableView tableView = new TableView();
                fill_data_table(tableView, normal_instances,null);
                tableView.getSelectionModel().select(data_table.getSelectionModel().getSelectedIndex());
                tableView.scrollTo(data_table.getSelectionModel().getSelectedIndex());
                Stage stage = new Stage();
                stage.setTitle(normal_instances.relationName());
                stage.setScene(new Scene(tableView));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(normalization_btn.getScene().getWindow());
                stage.showAndWait();
            }
        });

        symmeterie_btn.setOnAction(event -> {
            int index = attributes_table.getSelectionModel().getSelectedIndex();
            if (normal_instances != null && normal_instances.attribute(index).isNumeric()) {
                TextArea textArea = new TextArea();
                StringBuilder sb = new StringBuilder(attribute_statistics.get(index).symmetery());
                textArea.editableProperty().setValue(false);
                textArea.setText(sb.toString());
                Stage stage = new Stage();
                stage.setTitle(normal_instances.relationName()+" symmetery");
                stage.setScene(new Scene(textArea));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(normalization_btn.getScene().getWindow());
                stage.showAndWait();
            }
        });

        apriori_btn.setOnAction(event -> {
            if (normal_instances != null){
                try {
                    Stage stage = new Stage();
                    stage.setTitle(normal_instances.relationName()+" - Apriori");
                    FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("AprioriGUI.fxml"));
                    Parent root = loader.load();
                    stage.setScene(new Scene(root));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.initOwner(apriori_btn.getScene().getWindow());
                    stage.showAndWait();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });

        knn_btn.setOnAction(event -> {
            try {
                if(normal_instances != null){
                    Stage stage = new Stage();
                    stage.setTitle(normal_instances.relationName()+" - KNN");
                    FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("KNNGUI.fxml"));
                    Parent root = loader.load();
                    stage.setScene(new Scene(root));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.initOwner(knn_btn.getScene().getWindow());
                    stage.showAndWait();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        dbscan_btn.setOnAction(event -> {
            try {
                if(normal_instances != null){
                    Stage stage = new Stage();
                    stage.setTitle(normal_instances.relationName()+" - DBSCAN");
                    FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("DBSCANGUI.fxml"));
                    Parent root = loader.load();
                    stage.setScene(new Scene(root));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.initOwner(dbscan_btn.getScene().getWindow());
                    stage.showAndWait();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void dispalay_instances() {

        String choosen_data_source = data_source_box.getSelectionModel().getSelectedItem().toString();
        String data_source_path = data_sources_path + choosen_data_source + data_source_extension;
        try {
            dataSource = new DataSource(data_source_path);
            normal_instances = dataSource.getDataSet();
            normalized_instances = dataSource.getDataSet();
            normal_instances.setClassIndex(normal_instances.numAttributes() - 1);
            normalized_instances.setClassIndex(normal_instances.numAttributes() - 1);
            relation_name_label.setText(normal_instances.relationName());
            nb_instances_label.setText(String.valueOf(normal_instances.numInstances()));
            nb_attributes_label.setText(String.valueOf(normal_instances.numAttributes()));
            build_attributes_statistics_and_models();
            fill_attributes_table(attributes_table);
            fill_data_table(data_table, normal_instances,null);
            Platform.runLater(() -> {
                plot_boxplots();
            });
            attributes_table.getSelectionModel().select(0);
            data_table.getSelectionModel().select(0);
            attributes_table.scrollTo(0);
            data_table.scrollTo(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fill_data_source_box() {
        File folder = new File(data_sources_path);
        File[] files = folder.listFiles();
        for (File file : files)
            data_source_box.getItems().add(file.getName().substring(0, file.getName().lastIndexOf(".")));
        return;
    }

    private void clear_components() {
        nb_attributes_label.setText("");
        nb_instances_label.setText("");
        relation_name_label.setText("");
        data_table.getColumns().clear();
        data_table.getItems().clear();
        attributes_table.getItems().clear();
        boxplot_pane.getChildren().clear();
    }

    private void fill_attributes_table(TableView tableView) {
        tableView.getItems().addAll(attribute_models);
        tableView.getSelectionModel().select(0);
    }

    public static void fill_data_table(TableView tableView, Instances instances, List<Integer> idx ) {

        TableColumn<Instance, String> column;
        for (int i = 0; i < instances.numAttributes(); i++) {
            final int j = i;
            column = new TableColumn<>(instances.attribute(i).name());
            column.setSortable(false);
            column.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().toString(j)));
            tableView.getColumns().add(column);
        }

        ObservableList<Instance> instances_list = FXCollections.observableArrayList();
        if(idx == null){
            for (int i = 0; i < instances.numInstances(); i++) {
                instances_list.add(instances.get(i));
            }
        }else {
            for (int i :idx) {
                instances_list.add(instances.get(i));
            }
        }
        tableView.getItems().addAll(instances_list);
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);



    }

    private void plot_boxplots() {

        SwingNode chartSwingNode = new SwingNode();
        boxplot_pane.getChildren().add(chartSwingNode);

        Platform.runLater(() -> {

            BoxPlot boxPlot = new BoxPlot(normal_instances);

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    chartSwingNode.setContent(boxPlot.getChart());
                }
            });
        });
        //BoxPlot boxPlot = new BoxPlot(normal_instances);
        //chartSwingNode.setContent(boxPlot.getChart());
    }

    private void plot_histogram(int att_indx) {
        if (att_indx >= 0 && att_indx < normal_instances.numAttributes() && !normal_instances.attribute(att_indx).isDate()) {
            histo_chart.getData().clear();
            histo_chart.setTitle("Histogram");
            histo_chart.getXAxis().setLabel("Value / Categorie");
            histo_chart.getYAxis().setLabel("Frequency");

            XYChart.Series series = new XYChart.Series();
            XYChart.Data data;
            series.setName(attribute_statistics.get(att_indx).getName());

            ArrayList labels = new ArrayList();
            // a trick to justify labels in their positions
            ((CategoryAxis) (histo_chart.getXAxis())).getCategories().clear();

            int nb = attribute_statistics.get(att_indx).getFrequencies().size();

            if (normal_instances.attribute(att_indx).isNumeric() && nb > max_bins) {

                histo_chart.getXAxis().setTickLabelRotation(0);


                double min, max, n;
                ArrayList<Double> limits = new ArrayList<>();//x+1 item

                min = Double.parseDouble(attribute_statistics.get(att_indx).getMin());
                max = Double.parseDouble(attribute_statistics.get(att_indx).getMax());
                n = (max - min) / (nb_bins);
                limits.add(min);
                for (int i = 1; i < nb_bins; i++) {
                    limits.add(Double.parseDouble(String.format("%.3f", min + i * n).replace(",", ".")));
                }
                limits.add(max);

                ArrayList<String> bins = new ArrayList<>();
                for (int i = 0; i < nb_bins; i++) {
                    bins.add(limits.get(i) + " - " + limits.get(i + 1));
                    labels.add(bins.get(i));
                }

                // frequencies is a tree map (ordered by keys) ascending order
                double key;
                HashMap<String, Integer> v = new HashMap<>();
                for (Map.Entry pair : attribute_statistics.get(att_indx).getFrequencies().entrySet()) {

                    key = (double) pair.getKey();
                    if (key == limits.get(nb_bins)) { // key == max
                        if (v.containsKey(bins.get(nb_bins - 1))) {
                            v.put(bins.get(nb_bins - 1), v.get(bins.get(nb_bins-1)) + (int) pair.getValue());
                        } else {
                            v.put(bins.get(nb_bins - 1), (int) pair.getValue());
                        }
                    } else {
                        for (int i = 0; i < nb_bins; i++) {
                            if (key >= limits.get(i) && key < limits.get(i + 1)) {
                                if (v.containsKey(bins.get(i))) {
                                    v.put(bins.get(i), v.get(bins.get(i)) + (int) pair.getValue());
                                } else {
                                    v.put(bins.get(i), (int) pair.getValue());
                                }
                            }
                        }
                    }
                }
                int val;
                for (int i = 0; i < nb_bins; i++) {
                    val = 0;
                    if (v.containsKey(bins.get(i)))
                        val = v.get(bins.get(i));

                    data= new XYChart.Data(bins.get(i),val);
                    add_bar_label(data, val);
                    series.getData().add(data);
                }
            } else {
                histo_chart.getXAxis().setTickLabelRotation(90);

                for (Map.Entry pair : attribute_statistics.get(att_indx).getFrequencies().entrySet()) {

                    data = new XYChart.Data<>(String.valueOf(pair.getKey()), pair.getValue());

                    add_bar_label(data, (Integer) pair.getValue());

                    series.getData().add(data);
                    labels.add(String.valueOf(pair.getKey()));
                }
            }
            ((CategoryAxis) (histo_chart.getXAxis())).setCategories(FXCollections.observableArrayList(labels));

            histo_chart.getData().add(series);
        }
    }

    private void build_attributes_statistics_and_models() {
        attribute_statistics = new ArrayList<>();
        attribute_models = new ArrayList<>();
        for (int att_indx = 0; att_indx < normal_instances.numAttributes(); att_indx++) {
            AttributeStatistics s = new AttributeStatistics(normal_instances, normalized_instances, att_indx);
            attribute_statistics.add(s);
            attribute_models.add(new StatisticalModel(
                    s.getName(),
                    s.getType(),
                    s.getMin(),
                    s.getMax(),
                    s.getMean(),
                    s.getQ1(),
                    s.getQ2(),
                    s.getQ3(),
                    s.getModes(),
                    s.getRange(),
                    s.getMidrange(),
                    s.getOutliers(),
                    s.getMissing()
            ));
        }
    }

    private void build_attribute_table(TableView tableView) {

        TableColumn<StatisticalModel, String> name_column = new TableColumn<>("Name");
        name_column.setSortable(false);
        name_column.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        tableView.getColumns().add(name_column);

        TableColumn<StatisticalModel, String> type_column = new TableColumn<>("Type");
        type_column.setSortable(false);
        type_column.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        tableView.getColumns().add(type_column);

        TableColumn<StatisticalModel, String> min_column = new TableColumn<>("Min");
        min_column.setSortable(false);
        min_column.setCellValueFactory(cellData -> cellData.getValue().minProperty());
        tableView.getColumns().add(min_column);

        TableColumn<StatisticalModel, String> max_column = new TableColumn<>("Max");
        max_column.setSortable(false);
        max_column.setCellValueFactory(cellData -> cellData.getValue().maxProperty());
        tableView.getColumns().add(max_column);

        TableColumn<StatisticalModel, String> mean_column = new TableColumn<>("Mean");
        mean_column.setSortable(false);
        mean_column.setCellValueFactory(cellData -> cellData.getValue().meanProperty());
        tableView.getColumns().add(mean_column);

        TableColumn<StatisticalModel, String> q1_column = new TableColumn<>("Q1");
        q1_column.setSortable(false);
        q1_column.setCellValueFactory(cellData -> cellData.getValue().Q1Property());
        tableView.getColumns().add(q1_column);

        TableColumn<StatisticalModel, String> q2_column = new TableColumn<>("Q2");
        q2_column.setSortable(false);
        q2_column.setCellValueFactory(cellData -> cellData.getValue().Q2Property());
        tableView.getColumns().add(q2_column);

        TableColumn<StatisticalModel, String> q3_column = new TableColumn<>("Q3");
        q3_column.setSortable(false);
        q3_column.setCellValueFactory(cellData -> cellData.getValue().Q3Property());
        tableView.getColumns().add(q3_column);

        TableColumn<StatisticalModel, String> modes_column = new TableColumn<>("Modes");
        modes_column.setSortable(false);
        modes_column.setCellValueFactory(cellData -> cellData.getValue().modesProperty());
        tableView.getColumns().add(modes_column);

        TableColumn<StatisticalModel, String> range_column = new TableColumn<>("Range");
        range_column.setSortable(false);
        range_column.setCellValueFactory(cellData -> cellData.getValue().rangeProperty());
        tableView.getColumns().add(range_column);

        TableColumn<StatisticalModel, String> midrange_column = new TableColumn<>("Midrange");
        midrange_column.setSortable(false);
        midrange_column.setCellValueFactory(cellData -> cellData.getValue().midrangeProperty());
        tableView.getColumns().add(midrange_column);

        TableColumn<StatisticalModel, String> outliers_column = new TableColumn<>("# Outliers");
        outliers_column.setSortable(false);
        outliers_column.setCellValueFactory(cellData -> cellData.getValue().outliersProperty());
        tableView.getColumns().add(outliers_column);

        TableColumn<StatisticalModel, String> missing_column = new TableColumn<>("#Missings");
        missing_column.setSortable(false);
        missing_column.setCellValueFactory(cellData -> cellData.getValue().missingProperty());
        tableView.getColumns().add(missing_column);

    }

    private void add_bar_label(XYChart.Data data, int value) {
        StackPane node = new StackPane();
        Label label = new Label(String.valueOf(value));
        label.setRotate(0);
        label.setTextFill(Color.DARKBLUE);
        label.setFont(new Font("Arial", 15));
        Group group = new Group(label);
        StackPane.setAlignment(group, Pos.BOTTOM_CENTER);
        StackPane.setMargin(group, new Insets(10, 0, 0, 0));
        node.getChildren().add(group);
        data.setNode(node);

    }

}
