public class invoker {

    static {
        System.loadLibrary("invoker");
    }

    public static native int invokeCLR(String sApplication);

    public static void main(String[] args) {
        System.out.println("Hello from JVM!!!");
        if (args.length >= 1) {
            String sApplication = args[0];
            int nResult = invokeCLR(sApplication);
            System.exit(nResult);
        } else
            System.out.println("Usage: java invoker <application>");
    }
}
