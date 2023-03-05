import sun.hotspot.WhiteBox;
import java.lang.ref.Reference;

public final class TestJNIWeakG1 {

    static {
        System.loadLibrary("TestJNIWeakG1");
    }

    private static final WhiteBox WB = WhiteBox.getWhiteBox();

    private static final class TestObject {

        public final int value;

        public TestObject(int value) {
            this.value = value;
        }
    }

    private volatile TestObject testObject = null;

    private static native void registerObject(Object o);

    private static native void unregisterObject();

    private static native Object getReturnedWeak();

    private static native Object getResolvedWeak();

    private boolean resolve = true;

    TestJNIWeakG1(boolean resolve) {
        this.resolve = resolve;
    }

    private Object getObject() {
        if (resolve) {
            return getResolvedWeak();
        } else {
            return getReturnedWeak();
        }
    }

    private void remember(int value) {
        TestObject o = new TestObject(value);
        registerObject(o);
        testObject = o;
    }

    private void forget() {
        unregisterObject();
        testObject = null;
    }

    private void gcUntilOld(Object o) {
        while (!WB.isObjectInOldGen(o)) {
            WB.youngGC();
        }
    }

    private void checkValue(int value) throws Exception {
        Object o = getObject();
        if (o == null) {
            throw new RuntimeException("Weak reference unexpectedly null");
        }
        TestObject t = (TestObject) o;
        if (t.value != value) {
            throw new RuntimeException("Incorrect value");
        }
    }

    private void checkSanity() throws Exception {
        System.out.println("running checkSanity");
        try {
            WB.requestConcurrentGCPhase("IDLE");
            int value = 5;
            try {
                remember(value);
                checkValue(value);
            } finally {
                forget();
            }
        } finally {
            WB.requestConcurrentGCPhase("ANY");
        }
    }

    private void checkSurvival() throws Exception {
        System.out.println("running checkSurvival");
        try {
            int value = 10;
            try {
                remember(value);
                checkValue(value);
                gcUntilOld(testObject);
                WB.requestConcurrentGCPhase("CONCURRENT_CYCLE");
                WB.requestConcurrentGCPhase("IDLE");
                checkValue(value);
            } finally {
                forget();
            }
        } finally {
            WB.requestConcurrentGCPhase("ANY");
        }
    }

    private void checkClear() throws Exception {
        System.out.println("running checkClear");
        try {
            int value = 15;
            try {
                remember(value);
                checkValue(value);
                gcUntilOld(testObject);
                WB.requestConcurrentGCPhase("CONCURRENT_CYCLE");
                WB.requestConcurrentGCPhase("IDLE");
                checkValue(value);
                testObject = null;
                WB.requestConcurrentGCPhase("CONCURRENT_CYCLE");
                WB.requestConcurrentGCPhase("IDLE");
                Object recorded = getObject();
                if (recorded != null) {
                    throw new RuntimeException("expected clear");
                }
            } finally {
                forget();
            }
        } finally {
            WB.requestConcurrentGCPhase("ANY");
        }
    }

    private void checkShouldNotClear() throws Exception {
        System.out.println("running checkShouldNotClear");
        try {
            int value = 20;
            try {
                remember(value);
                checkValue(value);
                gcUntilOld(testObject);
                WB.requestConcurrentGCPhase("IDLE");
                checkValue(value);
                testObject = null;
                WB.requestConcurrentGCPhase("BEFORE_REMARK");
                Object recovered = getObject();
                if (recovered == null) {
                    throw new RuntimeException("unexpected clear during mark");
                }
                WB.requestConcurrentGCPhase("IDLE");
                if (getObject() == null) {
                    throw new RuntimeException("cleared jweak for live object");
                }
                Reference.reachabilityFence(recovered);
            } finally {
                forget();
            }
        } finally {
            WB.requestConcurrentGCPhase("ANY");
        }
    }

    private void check() throws Exception {
        checkSanity();
        checkSurvival();
        checkClear();
        checkShouldNotClear();
        System.out.println("Check passed");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Check with jweak resolved");
        new TestJNIWeakG1(true).check();
        System.out.println("Check with jweak returned");
        new TestJNIWeakG1(false).check();
    }
}
