package januszsoft;


import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;




public class MainWindowController {

    @FXML
    Button btnConnect;
    @FXML
    TextArea values;
    @FXML
    TextArea title;
    private Service<Void> dataWorker;


    @FXML
    public void onBtnConnectClicked() {

        title.setText("Nawiązane połączenie, następuje przesyłanie danych:");
        dataWorker = new DataWorker(values);
        dataWorker.start();
    }
}
