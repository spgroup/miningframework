public class TestJNI {

    static {
        System.loadLibrary("TestJNI");
    }

    public static native void doSomething(int val);

    public static void main(String[] args) {
        int intArg = 43;
        if (args.length > 0) {
            try {
                intArg = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("arg " + args[0] + " must be an integer");
                System.exit(1);
            }
        }
        TestJNI.doSomething(intArg);
    }
}