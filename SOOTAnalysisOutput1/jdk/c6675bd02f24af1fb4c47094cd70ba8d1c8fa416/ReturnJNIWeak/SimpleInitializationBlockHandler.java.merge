public final class ReturnJNIWeak {

    static {
        System.loadLibrary("ReturnJNIWeak");
    }

    private static final class TestObject {

        public final int value;

        public TestObject(int value) {
            this.value = value;
        }
    }

    private static volatile TestObject testObject = null;

    private static native void registerObject(Object o);

    private static native void unregisterObject();

    private static native Object getObject();

    private static void remember(int value) {
        TestObject o = new TestObject(value);
        registerObject(o);
        testObject = o;
    }

    private static void forget() {
        unregisterObject();
        testObject = null;
    }

    private static void checkValue(int value) throws Exception {
        Object o = getObject();
        if (o == null) {
            throw new RuntimeException("Weak reference unexpectedly null");
        }
        TestObject t = (TestObject) o;
        if (t.value != value) {
            throw new RuntimeException("Incorrect value");
        }
    }

    private static void testSanity() throws Exception {
        System.out.println("running testSanity");
        int value = 5;
        try {
            remember(value);
            checkValue(value);
        } finally {
            forget();
        }
    }

    private static void testSurvival() throws Exception {
        System.out.println("running testSurvival");
        int value = 10;
        try {
            remember(value);
            checkValue(value);
            System.gc();
            checkValue(value);
        } finally {
            forget();
        }
    }

    private static void testClear() throws Exception {
        System.out.println("running testClear");
        int value = 15;
        try {
            remember(value);
            checkValue(value);
            checkValue(value);
            testObject = null;
            System.gc();
            Object recorded = getObject();
            if (recorded != null) {
                throw new RuntimeException("expected clear");
            }
        } finally {
            forget();
        }
    }

    public static void main(String[] args) throws Exception {
        testSanity();
        testSurvival();
        testClear();
    }
}