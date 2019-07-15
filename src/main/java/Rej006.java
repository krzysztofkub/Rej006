import com.fazecast.jSerialComm.SerialPort;

public class Rej006  {

    SerialPort m_port;

    Thread m_thread;


    String m_id;

    public Rej006(String portName) {
        m_port = SerialPort.getCommPort(portName);
        m_port.openPort();

    }

    void requestID() {

    }




}


