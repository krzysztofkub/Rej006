import javafx.beans.property.*;
import javafx.fxml.FXML;

public class Rej006ViewModel {

    final public BooleanProperty isOpenProperty;

    final public ObjectProperty settingsProperty;

    final public StringProperty nameProperty;

    final public IntegerProperty idProperty;

    final public StringProperty firmwareProperty;

    final public BooleanProperty isOnlineProperty;

    final public IntegerProperty registrationCountProperty;

    final public IntegerProperty capacityProperty;

    final public IntegerProperty batteryProperty;

    final public StringProperty statusProperty;

    final public IntegerProperty syncCodeProperty;

    public Rej006ViewModel() {
        isOpenProperty = new SimpleBooleanProperty(this, "is open", false);
        settingsProperty = new SimpleObjectProperty(this, "offline settings");
        nameProperty = new SimpleStringProperty(this,"name");
        idProperty = new SimpleIntegerProperty(this, "ID", -1);
        firmwareProperty = new SimpleStringProperty(this,"firmware");
        isOnlineProperty = new SimpleBooleanProperty(this, "is online", false);
        registrationCountProperty = new SimpleIntegerProperty(this, "registration count", 0);
        capacityProperty = new SimpleIntegerProperty(this, "flash capacity", 0);
        batteryProperty = new SimpleIntegerProperty(this, "battery state", 0);
        statusProperty = new SimpleStringProperty(this,"status");
        syncCodeProperty = new SimpleIntegerProperty(this, "sync code");
    }


}
