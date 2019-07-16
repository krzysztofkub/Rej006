package januszsoft;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.util.List;
import java.util.Random;

public class DataWorker extends Service<Void> {

    TextArea values;

    public DataWorker(TextArea values) {
        this.values = values;
    }

    @Override
    protected Task createTask() {
        return new Task() {
            Random random = new Random();

            @Override
            protected Object call() throws Exception {
                while (true) {
                    Thread.sleep(1000);
                    int randomNumber = random.nextInt(50);
                    values.setText(" " + values.getText() + randomNumber);
                    Thread.sleep(1000);
                }
            }
        };
    }
}
