public class AllowedFunctions {

    static {
        try {
            System.loadLibrary("AllowedFunctions");
        } catch (UnsatisfiedLinkError ule) {
            System.err.println("Could not load AllowedFunctions library");
            System.err.println("java.library.path: " + System.getProperty("java.library.path"));
            throw ule;
        }
    }

    native static int check();

    public static void main(String[] args) {
        int status = check();
        if (status != 0) {
            throw new RuntimeException("Non-zero status returned from the agent: " + status);
        }
    }
}