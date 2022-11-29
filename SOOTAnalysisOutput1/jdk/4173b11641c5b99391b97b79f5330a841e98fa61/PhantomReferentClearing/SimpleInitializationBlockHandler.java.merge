import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;

public class PhantomReferentClearing {

    private static final long ENQUEUE_TIMEOUT = 1000;

    private static final ReferenceQueue<Object> Q1 = new ReferenceQueue<>();

    private static final ReferenceQueue<Object> Q2 = new ReferenceQueue<>();

    private static volatile Object O2 = new Object();

    private static volatile List<Object> O1 = new ArrayList<>();

    static {
        O1.add(O2);
    }

    private static final PhantomReference<Object> P1 = new PhantomReference<>(O1, Q1);

    private static final PhantomReference<Object> P2 = new PhantomReference<>(O2, Q2);

    public static void main(String[] args) throws InterruptedException {
        System.gc();
        if (Q1.remove(ENQUEUE_TIMEOUT) != null) {
            throw new RuntimeException("P1 already notified");
        } else if (Q2.poll() != null) {
            throw new RuntimeException("P2 already notified");
        }
        O1 = null;
        System.gc();
        if (Q1.remove(ENQUEUE_TIMEOUT) == null) {
            throw new RuntimeException("P1 not notified by O1 deletion");
        } else if (Q2.remove(ENQUEUE_TIMEOUT) != null) {
            throw new RuntimeException("P2 notified by O1 deletion.");
        }
        O2 = null;
        System.gc();
        if (Q2.remove(ENQUEUE_TIMEOUT) == null) {
            throw new RuntimeException("P2 not notified by O2 deletion");
        }
    }
}