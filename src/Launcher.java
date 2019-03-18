import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Launcher extends Application {

    public static Stage stage;
    public static Parent root;
    public static FXMLLoader loader;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        stage.setTitle("");
        showMainView();

    }

    private void showMainView() throws IOException {
        loader = new FXMLLoader(Launcher.class.getResource("MainView.fxml"));
        root = loader.load();
        stage.setResizable(false);
        stage.setScene(new Scene(root, 600+800, 800));
        stage.show();
    }

}
