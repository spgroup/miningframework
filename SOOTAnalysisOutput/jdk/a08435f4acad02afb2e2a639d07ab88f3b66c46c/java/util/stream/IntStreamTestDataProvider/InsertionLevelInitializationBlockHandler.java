package java.util.stream;

import org.testng.annotations.DataProvider;
import java.util.*;
import java.util.Spliterators;
import java.util.function.Supplier;

public class IntStreamTestDataProvider {

    private static final int[] to0 = new int[0];

    private static final int[] to1 = new int[1];

    private static final int[] to10 = new int[10];

    private static final int[] to100 = new int[100];

    private static final int[] to1000 = new int[1000];

    private static final int[] reversed = new int[100];

    private static final int[] ones = new int[100];

    private static final int[] twice = new int[200];

    private static final int[] pseudoRandom;

    private static final Object[][] testData;

    private static final Object[][] spliteratorTestData;

    static {
        int[][] arrays = { to0, to1, to10, to100, to1000 };
        for (int[] arr : arrays) {
            for (int i = 0; i < arr.length; i++) {
                arr[i] = i;
            }
        }
        for (int i = 0; i < reversed.length; i++) {
            reversed[i] = reversed.length - i;
        }
        for (int i = 0; i < ones.length; i++) {
            ones[i] = 1;
        }
        System.arraycopy(to100, 0, twice, 0, to100.length);
        System.arraycopy(to100, 0, twice, to100.length, to100.length);
        pseudoRandom = new int[LambdaTestHelpers.LONG_STRING.length()];
        for (int i = 0; i < LambdaTestHelpers.LONG_STRING.length(); i++) {
            pseudoRandom[i] = (int) LambdaTestHelpers.LONG_STRING.charAt(i);
        }
    }

    static final Object[][] arrays = { { "empty", to0 }, { "0..1", to1 }, { "0..10", to10 }, { "0..100", to100 }, { "0..1000", to1000 }, { "100x[1]", ones }, { "2x[0..100]", twice }, { "reverse 0..100", reversed }, { "pseudorandom", pseudoRandom } };

    static {
        {
            List<Object[]> list = new ArrayList<>();
            for (Object[] data : arrays) {
                final Object name = data[0];
                final int[] ints = (int[]) data[1];
                list.add(new Object[] { "array:" + name, TestData.Factory.ofArray("array:" + name, ints) });
                SpinedBuffer.OfInt isl = new SpinedBuffer.OfInt();
                for (int i : ints) {
                    isl.accept(i);
                }
                list.add(new Object[] { "SpinedList:" + name, TestData.Factory.ofSpinedBuffer("SpinedList:" + name, isl) });
                list.add(streamDataDescr("IntStream.intRange(0,l): " + ints.length, () -> IntStream.range(0, ints.length)));
                list.add(streamDataDescr("IntStream.rangeClosed(0,l): " + ints.length, () -> IntStream.rangeClosed(0, ints.length)));
            }
            testData = list.toArray(new Object[0][]);
        }
        {
            List<Object[]> spliterators = new ArrayList<>();
            for (Object[] data : arrays) {
                final Object name = data[0];
                final int[] ints = (int[]) data[1];
                SpinedBuffer.OfInt isl = new SpinedBuffer.OfInt();
                for (int i : ints) {
                    isl.accept(i);
                }
                spliterators.add(splitDescr("Arrays.s(array):" + name, () -> Arrays.spliterator(ints)));
                spliterators.add(splitDescr("Arrays.s(array,o,l):" + name, () -> Arrays.spliterator(ints, 0, ints.length / 2)));
                spliterators.add(splitDescr("SpinedBuffer.s():" + name, () -> isl.spliterator()));
                spliterators.add(splitDescr("Primitives.s(SpinedBuffer.iterator(), size):" + name, () -> Spliterators.spliterator(isl.iterator(), ints.length, 0)));
                spliterators.add(splitDescr("Primitives.s(SpinedBuffer.iterator()):" + name, () -> Spliterators.spliteratorUnknownSize(isl.iterator(), 0)));
                spliterators.add(splitDescr("IntStream.intRange(0,l):" + name, () -> IntStream.range(0, ints.length).spliterator()));
                spliterators.add(splitDescr("IntStream.intRangeClosed(0,l):" + name, () -> IntStream.rangeClosed(0, ints.length).spliterator()));
            }
            spliteratorTestData = spliterators.toArray(new Object[0][]);
        }
    }

    static <T> Object[] streamDataDescr(String description, Supplier<IntStream> s) {
        return new Object[] { description, TestData.Factory.ofIntSupplier(description, s) };
    }

    static <T> Object[] splitDescr(String description, Supplier<Spliterator.OfInt> s) {
        return new Object[] { description, s };
    }

    @DataProvider(name = "IntStreamTestData")
    public static Object[][] makeIntStreamTestData() {
        return testData;
    }

    @DataProvider(name = "IntSpliterator")
    public static Object[][] spliteratorProvider() {
        return spliteratorTestData;
    }
}