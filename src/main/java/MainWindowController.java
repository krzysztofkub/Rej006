import com.fazecast.jSerialComm.SerialPort;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class MainWindowController {

    @FXML
    Button btnConnect;

    @FXML
    public void onBtnConnectClicked() {

        System.out.println("DUPA");


        SerialPort sp = SerialPort.getCommPort("COM1");

        sp.setBaudRate(115200);
        sp.setNumDataBits(8);
        sp.setNumStopBits(0);
        sp.setFlowControl(0);

        if (!sp.openPort()) {
            System.out.println("port cannot be opened");
            return;
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            return;
        }

        byte[] request = new byte[]{(byte) 0x02};
        sp.writeBytes(request, 1);

        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            return;
        }

        byte[] response = new byte[32];
        int totalBytesRead = 0;
        int bytesRead = 0;
        do {
            System.out.println(totalBytesRead);
            bytesRead = sp.readBytes(response, response.length - totalBytesRead, totalBytesRead);
            totalBytesRead += bytesRead;
        } while (bytesRead > 0);

        sp.closePort();

        String id = "" + response[totalBytesRead - 1];


    }
}
