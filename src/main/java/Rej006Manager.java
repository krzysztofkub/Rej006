import java.util.ArrayList;
import java.util.List;

public final class Rej006Manager {

    //region Private fields

    /**
     * Holds all REJ006 Devices
     */
    private List<Rej006> m_devices;

    //endregion

    //region Public Methods

    /**
     * Returns device of provided id.
     *
     * @param id of the device to retrieve
     * @return Rej006 instance or null
     */
    public synchronized Rej006 get(int id) {
        Rej006 result = m_devices
                .stream()
                // TODO apply proper filtering
                //.filter(d -> d.getId() == id)
                .findFirst()
                .orElse(null);

        return result;
    }

    /**
     * Adds device to the available devices list.
     *
     * @param device Device to add
     * @return if device was added (false = was already in the list)
     */
    public synchronized boolean add(Rej006 device) {
        boolean isFound = m_devices.stream().anyMatch(d -> d == device);
        if (!isFound) {
            m_devices.add(device);
        }
        return !isFound;
    }

    //endregion

    //region Singleton

    private Rej006Manager() {
        if (__Holder.INSTANCE != null) {
            throw new IllegalStateException("Rej006Manager already constructed");
        }

        m_devices = new ArrayList<>();
    }

    public static Rej006Manager getInstance() {
        return __Holder.INSTANCE;
    }

    private static class __Holder {
        private static final Rej006Manager INSTANCE = new Rej006Manager();
    }

    //endregion

}
