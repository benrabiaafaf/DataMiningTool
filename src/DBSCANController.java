import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import weka.core.Instance;
import weka.core.Instances;
import java.util.ArrayList;
import java.util.List;

public class DBSCANController {

    @FXML
    private Button run_btn;

    @FXML
    private Label label;

    @FXML
    private Spinner eps_spinner,minpts_spinner;

    @FXML
    private javafx.scene.control.TableView table_clusters, table_centers;

    private Instances instances;

    private List<List<Instance>> clusters;

    @FXML
    private void initialize(){
        instances = MainController.normalized_instances;
        minpts_spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 2,1));
        eps_spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.01,5,0.2,0.01));

        create_table_clusters();
        create_table_centers();


        run_btn.setOnAction(event -> {
            double eps = (double) eps_spinner.getValue();
            int min_pts = (int) minpts_spinner.getValue();
            DBSCAN dbscan = new DBSCAN(instances,eps,min_pts);
            List<List<Object>> gravity_centers = dbscan.getGravity_centers();
            gravity_centers.add(0,dbscan.getGl());

            table_centers.getItems().clear();
            table_centers.getItems().addAll(gravity_centers);

            table_clusters.getItems().clear();
            clusters = dbscan.getClusters();
            int i = 0;
            for (List<Instance> cluster: clusters){
                ArrayList<Object> l = new ArrayList<>();
                l.add("C"+(i+1));
                l.add(cluster.size());
                i++;
                table_clusters.getItems().add(l);
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Clusters number : "+clusters.size()+"\n");
            sb.append("Noise : "+(dbscan.getNoise().size()/(double)instances.numInstances()*100)+" % \n");
            sb.append("Inertie Intraclasse (W) : "+dbscan.getW()+"\n");
            sb.append("Inertie Interclasse (T) : "+dbscan.getT()+"\n");
            label.setText(sb.toString());


        });

    }

    private void create_table_centers() {
        for (int i = 0; i < instances.numAttributes()-1;i++){
            TableColumn<List<Object>, String> column = new TableColumn<>(instances.attribute(i).name());
            int finalI = i;
            column.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get(finalI).toString())));
            table_centers.getColumns().add(column);
        }
        table_centers.setColumnResizePolicy(javafx.scene.control.TableView.UNCONSTRAINED_RESIZE_POLICY);

    }

    private void create_table_clusters() {

        TableColumn<List<String>, String> column_1 = new TableColumn<>("Cluster");
        TableColumn<List<String>, String> column_2 = new TableColumn<>("Size");

        column_1.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get(0))));
        column_2.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get(1))));

        table_clusters.getColumns().addAll(column_1, column_2);

        table_clusters.setColumnResizePolicy(javafx.scene.control.TableView.UNCONSTRAINED_RESIZE_POLICY);

        table_clusters.setRowFactory( tv -> {
            TableRow<List<String>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty() ) {
                    TableView tableView = new TableView();
                    TableColumn<Instance, String> column;
                    for (int i = 0; i < instances.numAttributes(); i++) {
                        final int j = i;
                        column = new TableColumn<>(instances.attribute(i).name());
                        column.setSortable(false);
                        column.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().toString(j)));
                        tableView.getColumns().add(column);
                    }
                    tableView.getItems().addAll(clusters.get(row.getIndex()));
                    tableView.setColumnResizePolicy(javafx.scene.control.TableView.UNCONSTRAINED_RESIZE_POLICY);
                    Stage stage = new Stage();
                    stage.setTitle("Cluster "+ (row.getIndex()+1));
                    stage.setScene(new Scene(tableView));
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.initOwner(table_clusters.getScene().getWindow());
                    stage.showAndWait();
                }
            });
            return row ;
        });
    }
}
