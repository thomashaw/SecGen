// A minimal long-running Java service. Its only job is to stay alive so that a
// JVM is running with the JDWP debug agent attached. The main thread spends
// almost all of its time in Thread.sleep(), which keeps a thread in a sleeping
// state and makes it wake to execute bytecode once a second. A debugger that
// attaches over JDWP relies on finding such a thread.
public class JdwpService {
    public static void main(String[] args) throws InterruptedException {
        long ticks = 0;
        while (true) {
            ticks++;
            Thread.sleep(1000);
        }
    }
}
