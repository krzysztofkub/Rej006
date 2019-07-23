import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainApplication extends Application {

    public static void main(String[] args) {
        launch(args);

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL url = getClass().getClassLoader().getResource("fxml/online_registration_window.fxml");
        FXMLLoader loader = new FXMLLoader(url);

        Parent root = loader.load();

        Scene scene = new Scene(root);

        primaryStage.setTitle("REJ006");
        primaryStage.setMinWidth(300);
        primaryStage.setMinHeight(200);
        primaryStage.setScene(scene);
        primaryStage.show();


    }
}
