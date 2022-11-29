public class CallWithJNIWeak {

    static {
        System.loadLibrary("CallWithJNIWeak");
    }

    private static native void testJNIFieldAccessors(CallWithJNIWeak o);

    private int i = 1;

    private long j = 2;

    private boolean z = true;

    private char c = 'a';

    private short s = 3;

    private float f = 1.0f;

    private double d = 2.0;

    private Object l;

    private CallWithJNIWeak() {
        this.l = this;
    }

    private native void weakReceiverTest0();

    private void weakReceiverTest() {
        weakReceiverTest0();
    }

    private synchronized void synchonizedWeakReceiverTest() {
        this.notifyAll();
    }

    private static native void runTests(CallWithJNIWeak o);

    public static void main(String[] args) {
        CallWithJNIWeak w = new CallWithJNIWeak();
        for (int i = 0; i < 20000; i++) {
            runTests(w);
        }
    }
}
