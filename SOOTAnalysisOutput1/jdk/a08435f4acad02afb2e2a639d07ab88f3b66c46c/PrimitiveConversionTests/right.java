import static java.math.BigInteger.ONE;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PrimitiveConversionTests {

    static final List<BigInteger> ALL_BIGINTEGER_CANDIDATES;

    static {
        List<BigInteger> samples = new ArrayList<>();
        for (int exponent : Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 31, 32, 33, 34, 62, 63, 64, 65, 71, 72, 73, 79, 80, 81, 255, 256, 257, 511, 512, 513, Double.MAX_EXPONENT - 1, Double.MAX_EXPONENT, Double.MAX_EXPONENT + 1, 2000, 2001, 2002)) {
            BigInteger x = ONE.shiftLeft(exponent);
            for (BigInteger y : Arrays.asList(x, x.add(ONE), x.subtract(ONE))) {
                samples.add(y);
                samples.add(y.negate());
            }
        }
        Random rng = new Random(1234567);
        for (int i = 0; i < 2000; i++) {
            samples.add(new BigInteger(rng.nextInt(2000), rng));
        }
        ALL_BIGINTEGER_CANDIDATES = Collections.unmodifiableList(samples);
    }

    public static int testDoubleValue() {
        int failures = 0;
        for (BigInteger big : ALL_BIGINTEGER_CANDIDATES) {
            double expected = Double.parseDouble(big.toString());
            double actual = big.doubleValue();
            if (Double.doubleToRawLongBits(expected) != Double.doubleToRawLongBits(actual)) {
                System.out.println(big);
                failures++;
            }
        }
        return failures;
    }

    public static int testFloatValue() {
        int failures = 0;
        for (BigInteger big : ALL_BIGINTEGER_CANDIDATES) {
            float expected = Float.parseFloat(big.toString());
            float actual = big.floatValue();
            if (Float.floatToRawIntBits(expected) != Float.floatToRawIntBits(actual)) {
                System.out.println(big + " " + expected + " " + actual);
                failures++;
            }
        }
        return failures;
    }

    public static void main(String[] args) {
        int failures = testDoubleValue();
        failures += testFloatValue();
        if (failures > 0) {
            throw new RuntimeException("Incurred " + failures + " failures while testing primitive conversions.");
        }
    }
}
