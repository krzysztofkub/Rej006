import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import javax.xml.bind.DatatypeConverter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public class SerialTestController implements  Rej006.PropertyChangedListener {

    @FXML
    private ProgressIndicator busyIndicator;

    @FXML
    private VBox vbParent;

    @FXML
    private Button btnRefresh;

    @FXML
    private Button btnSend;

    @FXML
    private ComboBox cbPorts;

    @FXML
    private TextArea txtLog;

    @FXML
    private TextField txtCommand;

    @FXML
    private TextArea txtConsole;

    @FXML
    private Button btnDisconnect;

    @FXML
    private Button btnConnect;

    private Rej006 m_rej006;

    @FXML
    public void initialize() {
        onRefresh();
    }

    @FXML
    public void onRefresh() {
        onLog("Refreshing available ports");

        SerialPort[] ports = SerialPort.getCommPorts();
        List<String> portNames = Arrays.stream(ports).map(SerialPort::getSystemPortName).collect(Collectors.toList());
        cbPorts.getItems().clear();
        cbPorts.getItems().addAll(portNames);
        cbPorts.getSelectionModel().select(0);
    }

    @FXML
    public void onConnect() {
        String portName = (String) cbPorts.getSelectionModel().getSelectedItem();

        m_rej006 = new Rej006(portName);

        m_rej006.addPropertyChangeListener(this);

        m_rej006.open();

    }

    @FXML
    public void onDisconnect() {
        m_rej006.close();
    }

    @FXML
    public void onSend() {
        m_rej006.requestID();
        m_rej006.requestFirmware();
    }

    @FXML
    public void onClearConsole() {
        txtLog.clear();
        txtConsole.clear();
    }

    private String timestamp() {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
    }

    private void onLog(String message) {
        txtLog.appendText(timestamp() + " - " + message + "\n");
    }

    private void onError(String message, Exception ex) {
        onLog("[ERROR] " + message + ", reason: " + ex.getMessage());
    }


    @Override
    public void onPropertyChanged(Rej006.Property property, Object value) {
        switch (property) {
            case NAME:
                onLog("Name: " + value.toString());
                break;
            case ID:
                onLog("Id: " + value.toString());
                break;
            case FIRMWARE:
                onLog("Firmware: " + value.toString());
                break;
        }
    }
}
