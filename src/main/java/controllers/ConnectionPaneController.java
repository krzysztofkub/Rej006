package controllers;

import com.fazecast.jSerialComm.SerialPort;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;

public class ConnectionPaneController {

    //region FXML controls
    @FXML
    private ComboBox<String> cbPorts;

    @FXML
    private Button btnOpen;

    @FXML
    private Button btnClose;

    @FXML
    private Button btnRefresh;

    @FXML
    private CheckBox chkConnection;

    @FXML
    private ProgressIndicator progIsBusy;
    //endregion

    private BooleanProperty isBusy = new SimpleBooleanProperty(this, "isBusy", false);

    @FXML
    public void initialize() {

        // Disable controls to open, refresh and select ports if
        // port is open OR is busy
        btnOpen.disableProperty().bind(chkConnection.selectedProperty().or(isBusy));
        btnRefresh.disableProperty().bind(chkConnection.selectedProperty().or(isBusy));
        cbPorts.disableProperty().bind(chkConnection.selectedProperty().or(isBusy));

        // Disable button to close port if no port is opened OR is busy
        btnClose.disableProperty().bind(chkConnection.selectedProperty().not().or(isBusy));
    }

    //region FXML action callbacks
    @FXML
    void onClose(ActionEvent event) {

    }

    @FXML
    void onOpen(ActionEvent event) {

    }

    @FXML
    void onRefresh(ActionEvent event) {
        isBusy.set(true);

        // Just re-insert all the available ports (system names)
        cbPorts.getItems().clear();

        SerialPort[] ports = SerialPort.getCommPorts();
        for(SerialPort sp : ports) {
            cbPorts.getItems().add(sp.getSystemPortName());
        }

        isBusy.set(false);
    }
    //endregion
}
