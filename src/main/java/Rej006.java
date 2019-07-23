import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


public class Rej006 {

    public Rej006ViewModel getViewModel() {
        return m_vm;
    }

    //region Property Changed Listener Stuff

    /**
     * All device properties
     */
    public enum Property {
        IS_OPEN,
        ERROR,
        OFFLINE_SETTINGS,
        NAME,
        ID,
        FIRMWARE,
        FLASH_REGISTRATION_COUNT,
        FLASH_CAPACITY,
        BATTERY_STATE,
        STATUS,
        SYNC_CODE;
    }

    /**
     * Every object that needs to listen to request responses from the device
     * should implement this listener and register itself for listening
     */
    public interface PropertyChangedListener {
        void onPropertyChanged(Property property, Object value);
    }

    public void addPropertyChangeListener(PropertyChangedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangedListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    private void notifyOfPropertyChanged(Rej006.Property property, Object value) {
        listeners.stream().forEach(l -> l.onPropertyChanged(property, value));
    }

    private List<PropertyChangedListener> listeners = new ArrayList<>();

    //endregion


    //region Private Variables

    private SerialPort m_port;

    private Rej006ViewModel m_vm;

    private ExecutorService m_executor;

    //endregion


    public Rej006(String portName) {
        m_executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread t = Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
        });

        m_port = SerialPort.getCommPort(portName);
        m_port.setBaudRate(115200);
        m_port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        m_port.setNumDataBits(8);
        m_port.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        m_port.setParity(SerialPort.NO_PARITY);

        m_vm = new Rej006ViewModel();
    }

    public void open() {
        Runnable task = () -> {
            if (m_port != null) {
                Debugger.log("[REJ006] Opening port: " + m_port.getSystemPortName());

                m_port.openPort();

                Debugger.log("[REJ006] Port: " + m_port.getSystemPortName() + " is opened: " + m_port.isOpen());

                m_vm.isOpenProperty.setValue(m_port.isOpen());
            }
        };
        m_executor.submit(task);
    }

    public void close() {
        //TODO - remove tasks from queue?
        Runnable task = () -> {
            if (m_port != null) {
                Debugger.log("[REJ006] Closing port: " + m_port.getSystemPortName());

                m_port.closePort();

                Debugger.log("[REJ006] Port: " + m_port.getSystemPortName() + " is closed: " + !m_port.isOpen());

                notifyOfPropertyChanged(Property.IS_OPEN, m_port.isOpen());
            }
        };
        m_executor.submit(task);
    }

    void requestID() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                byte[] buffer = new byte[32];
                buffer[0] = 2;

                int numBytesWritten = writeBuffer(buffer, 0, 1);

                Thread.sleep(333);

                int numBytesRead = readBuffer(buffer, 0, buffer.length);

                if (numBytesRead <= 0) {
                    notifyOfPropertyChanged(Property.ERROR, "Request for ID could not be send to the device");
                    close();
                    return null;
                }

                String name = new String(buffer, 0, numBytesRead - 1, "ASCII");
                String id = new String(buffer, numBytesRead - 1, 1, "ASCII");

                Debugger.log("[REJ006] Name: " + name + ", id: " + id);

                Platform.runLater(() -> {
                    m_vm.nameProperty.set(name);
                    m_vm.idProperty.set(Integer.parseInt(id));
                });

                return null;
            }
        };

        m_executor.submit(task);
    }

    public void requestFirmware() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                byte[] buffer = new byte[32];
                buffer[0] = 4;

                int numBytesWritten = writeBuffer(buffer, 0, 1);

                Thread.sleep(333);

                int numBytesRead = readBuffer(buffer, 0, buffer.length);

                if (numBytesRead <= 0) {
                    notifyOfPropertyChanged(Property.ERROR, "Request for FIRMWARE could not be send to the device");
                    close();
                    return null;
                }

                String firmware = new String(buffer, 0, numBytesRead, "ASCII");

                Debugger.log("[REJ006] Firmware: " + firmware);

                notifyOfPropertyChanged(Property.FIRMWARE, firmware);

                return null;
            }
        };

        m_executor.submit(task);
    }


    private int writeBuffer(byte[] buffer, int offset, int length) throws Exception {
        if (m_port == null || !m_port.isOpen()) {
            return -1;
        }

        if (offset >= buffer.length) {
            return -1;
        }

        if (offset + length >= buffer.length || length < 0) {
            length = buffer.length - offset;
        }

        // Use stream to force flush after every byte so device has time to process
        int numBytesWritten = 0;
        DataOutputStream out = new DataOutputStream(m_port.getOutputStream());
        for (int i = offset; i < offset + length; i++) {
            out.write(buffer[i]);
            out.flush();
            numBytesWritten++;
            Thread.sleep(100);
        }
        out.close();

        return numBytesWritten;
    }

    private int readBuffer(byte[] buffer, int offset, int length) throws Exception {
        if (!m_port.isOpen() || m_port == null) {
            return -1;
        }

        if (offset >= buffer.length) {
            return -1;
        }

        if (offset + length > buffer.length || length < 0) {
            length = buffer.length - offset;
        }

        // No need to use streams while reading
        int numBytesRead = 0;
        int numBytesAvailable = 0;
        while ((numBytesAvailable = m_port.bytesAvailable()) > 0 && numBytesRead < length) {
            int numBytesToRead = Math.min(numBytesAvailable, length - numBytesRead);
            numBytesRead += m_port.readBytes(buffer, numBytesToRead, numBytesRead);
            Thread.sleep(100);
        }

        return numBytesRead;
    }

}


