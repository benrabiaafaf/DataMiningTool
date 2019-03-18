import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class AprioriController {

    @FXML
    private Spinner<Integer> min_support_spinner, min_confidence_spinner;
    @FXML
    private ChoiceBox c_choice;
    @FXML
    private Label nb_rules_label, nb_patterns_label;
    @FXML
    private Button mine_btn;
    @FXML
    private TableView c_table, rules_table;

    private Apriori apriori;

    @FXML
    public void initialize() {

        min_support_spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 100, 3));
        min_confidence_spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 100, 3));

        //init frequent_patterns_table
        TableColumn<List<Object>, String> column_3 = new TableColumn<>("Frequent itemsets");
        TableColumn<List<Object>, String> column_4 = new TableColumn<>("Support");
        column_4.prefWidthProperty().setValue(100);
        column_3.prefWidthProperty().setValue(500);
        c_table.getColumns().addAll(column_3, column_4);
        column_3.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get(0))));
        column_4.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().get(1))));

        //init rules_tables
        TableColumn<List<Object>, Void> column_0 = new TableColumn<>("N°");
        TableColumn<List<Object>, String> column_1 = new TableColumn<>("Rules");
        TableColumn<List<Object>, String> column_2 = new TableColumn<>("Confidence");

        column_0.minWidthProperty().setValue(50);
        column_0.maxWidthProperty().setValue(50);
        column_1.minWidthProperty().setValue(445);
        column_2.minWidthProperty().setValue(100);
        column_2.maxWidthProperty().setValue(100);

        rules_table.getColumns().addAll(column_0, column_1, column_2);
        column_0.setSortable(false);
        column_0.setCellFactory(col -> {
            TableCell<List<Object>, Void> cell = new TableCell<>();
            cell.textProperty().bind(Bindings.createStringBinding(() -> {
                if (cell.isEmpty()) {
                    return null;
                } else {
                    return String.format("%04d", cell.getIndex() + 1);
                }
            }, cell.emptyProperty(), cell.indexProperty()));
            return cell;
        });
        column_1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0).toString() + " -> " + data.getValue().get(1).toString()));
        column_2.setCellValueFactory(data -> new SimpleStringProperty(String.format("%.2f", data.getValue().get(2)) + " % "));

        c_choice.disableProperty().setValue(true);

        mine_btn.setOnAction(event -> {

                    c_choice.getItems().clear();
                    c_choice.disableProperty().setValue(false);
                    rules_table.getItems().clear();
                    c_table.getItems().clear();

                    int min_sup = min_support_spinner.getValue();
                    int min_conf = min_confidence_spinner.getValue();
                    apriori = new Apriori(MainController.normal_instances, min_sup, min_conf);

                    // remplir la liste des C
                    for (int i = 1; i <= apriori.getNb_c(); i++) {
                        c_choice.getItems().add(i);
                    }
                    c_choice.getItems().add("ALL");
                    c_choice.getSelectionModel().selectLast();

                    rules_table.getItems().clear();
                    List<List<Object>> rules = apriori.getRules();
                    rules_table.getItems().addAll(rules);

                    nb_rules_label.setText(rules.size() + " Rules");
                    nb_patterns_label.setText(c_table.getItems().size() + " frequent pattern ");

                }
        );

        c_choice.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.toString().equals("ALL")) {
                    c_table.getItems().clear();
                    c_table.getItems().addAll(apriori.getFrequent());
                    nb_patterns_label.setText(c_table.getItems().size() + " frequent pattern");
                } else {
                    c_table.getItems().clear();
                    int len = Integer.valueOf(String.valueOf(newValue));
                    c_table.getItems().addAll(apriori.getFrequent(len));
                    nb_patterns_label.setText(c_table.getItems().size() + " frequent pattern of length " + newValue);
                }

            }
        });

    }

}
