package januszsoft;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Rej006 {

    private final String portName;

    private Queue<Long> data;

    public Rej006(String portName) {
        this.portName = portName;
        data = new ConcurrentLinkedQueue<Long>();
    }

    public String getPortName() {
        return portName;
    }

    public void add(Long value) {
        data.add(value);
    }

    public Long poll() {
        return data.poll();
    }
}


