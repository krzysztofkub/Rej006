public class Debugger {
    public static boolean isEnabled = true;

    public static void log(Object o) {
        if (isEnabled) {
            System.out.println(o.toString());
        }
    }
}
