package controllers;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortPacketListener;
import com.sun.javafx.scene.layout.region.Margins;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import viewmodels.OnlineRegistrationViewModel;

import javax.xml.bind.DatatypeConverter;
import java.awt.event.ActionEvent;
import java.io.DataOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OnlineRegistrationController {

    //region FXML controls
    @FXML
    private LineChart chart;

    @FXML
    private Button btnStart;

    @FXML
    private Button btnStop;

    @FXML
    private HBox hbSettingsControls;

    @FXML
    private ComboBox<Integer> cbFrequency;

    @FXML
    private ComboBox<Integer> cbAccelerometer;

    @FXML
    private ComboBox<Integer> cbGyroscope;

    @FXML
    private CheckBox chkAX;

    @FXML
    private CheckBox chkAY;

    @FXML
    private CheckBox chkAZ;

    @FXML
    private CheckBox chkPitch;

    @FXML
    private CheckBox chkRoll;

    @FXML
    private CheckBox chkYaw;
    //endregion

    //region Private Fields

    private OnlineRegistrationViewModel m_vm;

    /**
     * only for testing,
     * TODO replace somehow by Rej006 (or something else) later
     */
    private SerialPort m_port;

    private XYChart.Series[] m_series;

    private double m_time;
    //endregion

    @FXML
    public void initialize() {
        //region Bind to/from VM
        m_vm = new OnlineRegistrationViewModel();

        m_vm.frequencyProperty.bind(cbFrequency.valueProperty());
        m_vm.accelerometerProperty.bind(cbAccelerometer.valueProperty());
        m_vm.gyroscopeProperty.bind(cbGyroscope.valueProperty());

        m_vm.axProperty.bind(chkAX.selectedProperty());
        m_vm.ayProperty.bind(chkAY.selectedProperty());
        m_vm.azProperty.bind(chkAZ.selectedProperty());
        m_vm.rollProperty.bind(chkRoll.selectedProperty());
        m_vm.pitchProperty.bind(chkPitch.selectedProperty());
        m_vm.yawProperty.bind(chkYaw.selectedProperty());

        btnStart.disableProperty().bind(m_vm.isRunningProperty);
        btnStop.disableProperty().bind(m_vm.isRunningProperty.not());

        hbSettingsControls.disableProperty().bind(m_vm.isRunningProperty);
        //endregion

        //region Fill combo boxes
        cbFrequency.getItems().add(20);
        cbFrequency.getItems().add(50);
        cbFrequency.getItems().add(100);
        cbFrequency.getItems().add(200);
        cbFrequency.getItems().add(400);
        cbFrequency.getItems().add(500);
        cbFrequency.getItems().add(700);
        cbFrequency.getItems().add(1000);
        cbFrequency.getSelectionModel().select(0);

        cbAccelerometer.getItems().add(6);
        cbAccelerometer.getItems().add(12);
        cbAccelerometer.getItems().add(24);
        cbAccelerometer.getSelectionModel().select(0);

        cbGyroscope.getItems().add(250);
        cbGyroscope.getItems().add(500);
        cbGyroscope.getItems().add(2500);
        cbGyroscope.getSelectionModel().select(0);

        chkAX.setSelected(true);
        chkAY.setSelected(true);
        chkAZ.setSelected(true);
        chkRoll.setSelected(true);
        chkPitch.setSelected(true);
        chkYaw.setSelected(true);
        //endregion
    }

    /**
     * Checks if what is selected on UI is valid (see table page 17 manual)
     *
     * @return true if settings can be send safely to the device
     */
    private boolean areSettingsValid() {
        int cc = m_vm.channelCountProperty.get();
        int freq = m_vm.frequencyProperty.get();

        // No channels selected
        if (cc == 0) {
            return false;
        }
        // Too many channels per freq
        if (cc > 4 && freq >= 700) {
            return false;
        }
        if (cc > 2 && freq >= 1000) {
            return false;
        }

        return true;
    }

    //region FXML action handlers

    @FXML
    public void onStart() throws Exception {

        // Break if settings are invalid
        if (!areSettingsValid()) {
            new Alert(Alert.AlertType.ERROR, "Settings are not valid", ButtonType.CLOSE).show();
            return;
        }

        int[] settings = getSettings();

        m_port = SerialPort.getCommPort("COM4");
        m_port.setBaudRate(115200);
        m_port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        m_port.setNumDataBits(8);
        m_port.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        m_port.setParity(SerialPort.NO_PARITY);
        m_port.openPort(0, 1, 1024);

        Thread.sleep(333);

        chart.getData().clear();
        m_series = new XYChart.Series[m_vm.channelCountProperty.get()];
        for (int i = 0; i < m_series.length; i++) {
            m_series[i] = new XYChart.Series();
        }
        chart.getData().addAll(m_series);
        m_time = 0;

        final int sampleSize = m_vm.channelCountProperty.get() * 2 + 2;
        m_port.addDataListener(new SerialPortPacketListener() {
            final int numOfSamples = 3;

            /**
             * How many samples read
             * @return
             */
            @Override
            public int getPacketSize() {
                return numOfSamples * sampleSize;
            }

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
            }

            @Override
            public void serialEvent(SerialPortEvent serialPortEvent) {
                byte[] received = serialPortEvent.getReceivedData();
                System.out.println("Received: " + DatatypeConverter.printHexBinary(received));
                processSamples(received, numOfSamples);
            }
        });

        DataOutputStream out = new DataOutputStream(m_port.getOutputStream());
        for (int i = 0; i < settings.length; i++) {
            int byteToWrite = settings[i];
            System.out.println("Writing: " + Integer.toHexString(byteToWrite));
            out.write(byteToWrite);
            out.flush();
            //m_port.writeBytes(new byte[]{(byte) byteToWrite}, 1, 0);
            System.out.println("Written: " + Integer.toHexString(byteToWrite) + "\n");
            Thread.sleep(50);
        }
        out.close();

        m_vm.isRunningProperty.set(true);

    }

    @FXML
    public void onStop() {
        m_vm.isRunningProperty.set(false);
        m_port.writeBytes(new byte[]{0x00}, 1, 0);
        m_port.closePort();
    }
    //endregion

    private int[] getSettings() {

        int numSettingsBytes = 1 // Toggle online byte
                + 1 // Frequency + channel count encoding byte
                + m_vm.channelCountProperty.get() // Each channel byte
                + 1 // Accelerometer + gyroscope encoding byte
                + 1 // Start registration byte
                ;

        int[] settings = new int[numSettingsBytes];

        settings[0] = 0x05;

        int freqBytePart = (cbFrequency.getSelectionModel().getSelectedIndex() & 0x07) << 2;
        int ccBytePart = (m_vm.channelCountProperty.get() - 1) << 5;
        settings[1] = (freqBytePart | ccBytePart | 0x02);

        int idx = 2;
        if (m_vm.axProperty.get()) {
            settings[idx++] = 0x80;
        }
        if (m_vm.ayProperty.get()) {
            settings[idx++] = 0x81;
        }
        if (m_vm.azProperty.get()) {
            settings[idx++] = 0x82;
        }
        if (m_vm.rollProperty.get()) {
            settings[idx++] = 0x83;
        }
        if (m_vm.pitchProperty.get()) {
            settings[idx++] = 0x84;
        }
        if (m_vm.yawProperty.get()) {
            settings[idx++] = 0x85;
        }

        int accBytePart = cbAccelerometer.getSelectionModel().getSelectedIndex();
        int gyroBytePart = cbGyroscope.getSelectionModel().getSelectedIndex() << 2;

        settings[idx++] = (accBytePart | gyroBytePart);
        settings[idx] = 0xda;

        return settings;
    }

    private void processSamples(byte[] data, int numOfSamples) {

        int i = 0;
        for (int sample = 0; sample < numOfSamples; sample++, i += 2) {

            for (int channel = 0; channel < m_vm.channelCountProperty.get(); channel++, i += 2) {

                System.out.println("Data from sample #" + sample + " for channel: " + channel + ": " + DatatypeConverter.printHexBinary(new byte[]{data[i], data[i + 1]}));

                int lo = data[i];
                int hi = data[i + 1];

                int value = hi * 256 + lo;

                XYChart.Data point = new XYChart.Data(m_time, value);
                m_series[channel].getData().add(point);
            }

            m_time += 1.0 / m_vm.frequencyProperty.get();
        }
    }
}

