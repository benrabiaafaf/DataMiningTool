import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.List;


public class KNNController {

    @FXML
    private Button predict_btn;

    @FXML
    private Label result_label;

    @FXML
    private Spinner<Integer> k_spinner;

    @FXML
    private TableView table;

    private Instances instances;

    @FXML
    private void initialize() {
        instances = MainController.normal_instances;
        k_spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 5));

        KNN knn = new KNN(instances);
        List<Integer> test_indexes = knn.get_test_indexes();

        MainController.fill_data_table(table,instances, test_indexes);

        TableColumn<Instance,Void> column = new TableColumn<>("Predicted");
        table.getColumns().add(column);


        predict_btn.setOnAction(event -> {
            knn.set_K(k_spinner.getValue());
            HashMap<Instance, String> predicats = knn.getPredicats();

            column.setCellFactory(col -> {
                TableCell<Instance, Void> cell = new TableCell<>();
                cell.textProperty().bind(Bindings.createStringBinding(() -> {
                    if (cell.isEmpty()) {
                        return null;
                    } else {
                        try {
                            String classs = "";
                            if(cell.getIndex() >= 0)
                                classs = predicats.get(instances.get(test_indexes.get(cell.getIndex())));
                            return classs;
                        }catch (ArrayIndexOutOfBoundsException e){

                        }
                        return "";
                    }
                }, cell.emptyProperty(), cell.indexProperty()));
                return cell;
            });

            float x = ((float)knn.getWell_predicted())/knn.get_testing_size()*100;
            float y = ((float)knn.getBad_predicted())/knn.get_testing_size()*100;
            result_label.setText("K = "+knn.getK()+"\n"+x+" % True prediction \n"
                    +y+" % False prediction");

        });
    }
}
