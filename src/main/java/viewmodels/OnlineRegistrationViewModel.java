package viewmodels;

import controllers.OnlineRegistrationController;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class OnlineRegistrationViewModel {

    public IntegerProperty frequencyProperty;
    public IntegerProperty accelerometerProperty;
    public IntegerProperty gyroscopeProperty;

    public BooleanProperty axProperty;
    public BooleanProperty ayProperty;
    public BooleanProperty azProperty;
    public BooleanProperty pitchProperty;
    public BooleanProperty rollProperty;
    public BooleanProperty yawProperty;

    public IntegerProperty channelCountProperty;

    public BooleanProperty isRunningProperty;

    public OnlineRegistrationViewModel() {
        frequencyProperty = new SimpleIntegerProperty(this, "frequency", 100);
        accelerometerProperty = new SimpleIntegerProperty(this, "accelerometer", 6);
        gyroscopeProperty = new SimpleIntegerProperty(this, "gyroscope", 250);

        axProperty = new SimpleBooleanProperty(this, "AX", true);
        ayProperty = new SimpleBooleanProperty(this, "AY", true);
        azProperty = new SimpleBooleanProperty(this, "AZ", true);
        pitchProperty = new SimpleBooleanProperty(this, "Pitch", true);
        rollProperty = new SimpleBooleanProperty(this, "Roll", true);
        yawProperty = new SimpleBooleanProperty(this, "Yaw", true);

        channelCountProperty = new SimpleIntegerProperty(this, "Channel Count", 6);
        ChangeListener channelToggleListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                channelCountProperty.set(channelCountProperty.get() + (newValue ? 1 : -1));
            }
        };
        axProperty.addListener(channelToggleListener);
        ayProperty.addListener(channelToggleListener);
        azProperty.addListener(channelToggleListener);
        pitchProperty.addListener(channelToggleListener);
        rollProperty.addListener(channelToggleListener);
        yawProperty.addListener(channelToggleListener);

        isRunningProperty = new SimpleBooleanProperty(this, "Is Running?", false);
    }
}
