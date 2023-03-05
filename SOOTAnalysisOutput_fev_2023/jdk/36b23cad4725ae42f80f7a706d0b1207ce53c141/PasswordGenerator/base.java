import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class PasswordGenerator {

    private static void usage() {
        System.out.println("Usage: PasswordGenerator LENGTH");
        System.out.println("Password Generator produces password of desired LENGTH.");
    }

    private static final List<Integer> PASSWORD_CHARS = new ArrayList<>();

    static {
        IntStream.rangeClosed('0', '9').forEach(PASSWORD_CHARS::add);
        IntStream.rangeClosed('A', 'Z').forEach(PASSWORD_CHARS::add);
        IntStream.rangeClosed('a', 'z').forEach(PASSWORD_CHARS::add);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            usage();
            return;
        }
        long passwordLength;
        try {
            passwordLength = Long.parseLong(args[0]);
            if (passwordLength < 1) {
                printMessageAndUsage("Length has to be positive");
                return;
            }
        } catch (NumberFormatException ex) {
            printMessageAndUsage("Unexpected number format" + args[0]);
            return;
        }
        new SecureRandom().ints(passwordLength, 0, PASSWORD_CHARS.size()).map(PASSWORD_CHARS::get).forEach(i -> System.out.print((char) i));
    }

    private static void printMessageAndUsage(String message) {
        System.err.println(message);
        usage();
    }
}
