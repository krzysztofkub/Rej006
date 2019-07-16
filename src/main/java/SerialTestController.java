import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SerialTestController {

    private SerialPort m_port;

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
        onLog("Connecting to port: " + portName);

        try {
            m_port = SerialPort.getCommPort(portName);

            m_port.setBaudRate(115200);
            m_port.setNumStopBits(SerialPort.ONE_STOP_BIT);
            m_port.setNumDataBits(8);
            m_port.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
            m_port.setParity(SerialPort.NO_PARITY);

            boolean isOpen = m_port.openPort(0);

            if (isOpen) {
                btnDisconnect.setDisable(false);
                btnConnect.setDisable(true);
                cbPorts.setDisable(true);
                btnRefresh.setDisable(true);
                txtCommand.setDisable(false);
                btnSend.setDisable(false);
                onLog("Connected on port: " + portName);
            } else {
                onLog("Could not open port: " + portName);
            }
        } catch (SerialPortInvalidPortException|NullPointerException ex) {
            onError("Error while connecting to port: " + portName, ex);
        }
    }

    @FXML
    public void onDisconnect() {

        // TODO cancel any current transmission

        onLog("Closing port: " + m_port.getSystemPortName());
        if (m_port != null) {
            m_port.closePort();
        }

        btnDisconnect.setDisable(true);
        btnConnect.setDisable(false);
        cbPorts.setDisable(false);
        btnRefresh.setDisable(false);
        txtCommand.setDisable(true);
        btnSend.setDisable(true);
    }

    @FXML
    public void onSend() {
        String hexString = txtCommand.getText();
        String[] hexBytes = hexString.split("\\s+");

        byte[] bytes = new byte[hexBytes.length];
        try {
            for(int i=0;i<hexBytes.length; i++) {
                String hexByte = hexBytes[i];
                char low = hexByte.charAt(1);
                char hi = hexByte.charAt(0);

                int value = Character.digit(low, 16) + Character.digit(hi, 16) << 4;
                bytes[i] = (byte)(value &0xff);
            }
        }
        catch (IndexOutOfBoundsException ex) {
            onError("Command parse error", ex);
            return;
        }

        onLog("Writing bytes: " + hexString.replaceAll("\\s+", " "));
        m_port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 33, 33);
        int numBytesWritten = 0;

        for(int i=0; i<bytes.length; i++) {
            m_port.writeBytes(bytes, 1, i);
            try {
                Thread.sleep(33);
            }
            catch (InterruptedException ex) {
                onError("Write timeout cancelled", ex);
                return;
            }
        }

        onLog("Written " + numBytesWritten + " bytes");
    }


    private String timestamp() {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
    }

    private void onLog(String message) {
        txtLog.appendText(timestamp() + " - " + message + "\n");
    }

    private void onDataReceived() {

    }

    private void onDataSend() {

    }

    private void onError(String message, Exception ex) {
        onLog("[ERROR] " + message + ", reason: " + ex.getMessage());
    }
}
