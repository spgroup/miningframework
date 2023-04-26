package java.util.stream;

import org.testng.annotations.DataProvider;
import java.util.*;
import java.util.Spliterators;
import java.util.function.Supplier;

public class StreamTestDataProvider {

    private static final Integer[] to0 = new Integer[0];

    private static final Integer[] to1 = new Integer[1];

    private static final Integer[] to10 = new Integer[10];

    private static final Integer[] to100 = new Integer[100];

    private static final Integer[] to1000 = new Integer[1000];

    private static final Integer[] reversed = new Integer[100];

    private static final Integer[] ones = new Integer[100];

    private static final Integer[] twice = new Integer[200];

    private static final Integer[] pseudoRandom;

    private static final Object[][] testData;

    private static final Object[][] testSmallData;

    private static final Object[][] testMiniData;

    private static final Object[][] withNullTestData;

    private static final Object[][] spliteratorTestData;

    static {
        Integer[][] arrays = { to0, to1, to10, to100, to1000 };
        for (Integer[] arr : arrays) {
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
        pseudoRandom = new Integer[LambdaTestHelpers.LONG_STRING.length()];
        for (int i = 0; i < LambdaTestHelpers.LONG_STRING.length(); i++) {
            pseudoRandom[i] = (int) LambdaTestHelpers.LONG_STRING.charAt(i);
        }
    }

    static final Object[][] arrays = { { "empty", to0 }, { "0..1", to1 }, { "0..10", to10 }, { "0..100", to100 }, { "0..1000", to1000 }, { "100x[1]", ones }, { "2x[0..100]", twice }, { "reverse 0..100", reversed }, { "pseudorandom", pseudoRandom } };

    static {
        {
            List<Object[]> listMini = new ArrayList<>();
            List<Object[]> listSmall = new ArrayList<>();
            List<Object[]> list1000 = new ArrayList<>();
            List<Object[]> list = null;
            for (Object[] data : arrays) {
                final Object name = data[0];
                final Integer[] ints = (Integer[]) data[1];
                final List<Integer> intsAsList = Arrays.asList(ints);
                list = ints.length >= 1000 ? list1000 : (ints.length >= 100 ? listSmall : listMini);
                list.add(arrayDataDescr("array:" + name, ints));
                list.add(collectionDataDescr("ArrayList.asList:" + name, intsAsList));
                list.add(collectionDataDescr("ArrayList:" + name, new ArrayList<>(intsAsList)));
                list.add(streamDataDescr("DelegatingStream(ArrayList):" + name, () -> new ArrayList<>(intsAsList).stream()));
                List<Integer> aList = new ArrayList<>(intsAsList);
                if (LambdaTestMode.isNormalMode()) {
                    list.add(collectionDataDescr("ArrayList.Sublist:" + name, (ints.length) <= 1 ? aList.subList(0, 0) : aList.subList(1, ints.length / 2)));
                }
                list.add(collectionDataDescr("LinkedList:" + name, new LinkedList<>(intsAsList)));
                list.add(collectionDataDescr("HashSet:" + name, new HashSet<>(intsAsList)));
                list.add(collectionDataDescr("LinkedHashSet:" + name, new LinkedHashSet<>(intsAsList)));
                list.add(collectionDataDescr("TreeSet:" + name, new TreeSet<>(intsAsList)));
                SpinedBuffer<Integer> spinedBuffer = new SpinedBuffer<>();
                intsAsList.forEach(spinedBuffer);
                list.add(sbDataDescr("SpinedBuffer:" + name, spinedBuffer));
            }
            testMiniData = listMini.toArray(new Object[0][]);
            listSmall.addAll(listMini);
            testSmallData = listSmall.toArray(new Object[0][]);
            list1000.addAll(listSmall);
            testData = list1000.toArray(new Object[0][]);
        }
        {
            List<Object[]> list = new ArrayList<>();
            int size = 5;
            for (int i = 0; i < (1 << size) - 2; i++) {
                Integer[] content = new Integer[size];
                for (int e = 0; e < size; e++) {
                    content[e] = (i & (1 << e)) > 0 ? e + 1 : null;
                }
                list.add(arrayDataDescr("array:" + i, content));
                list.add(collectionDataDescr("HashSet:" + i, new HashSet<>(Arrays.asList(content))));
            }
            withNullTestData = list.toArray(new Object[0][]);
        }
        {
            List<Object[]> spliterators = new ArrayList<>();
            for (Object[] data : arrays) {
                final Object name = data[0];
                final Integer[] ints = (Integer[]) data[1];
                spliterators.add(splitDescr("Arrays.s(array):" + name, () -> Arrays.spliterator(ints)));
                spliterators.add(splitDescr("arrays.s(array,o,l):" + name, () -> Arrays.spliterator(ints, 0, ints.length / 2)));
                spliterators.add(splitDescr("SpinedBuffer.s():" + name, () -> {
                    SpinedBuffer<Integer> sb = new SpinedBuffer<>();
                    for (Integer i : ints) sb.accept(i);
                    return sb.spliterator();
                }));
                spliterators.add(splitDescr("Iterators.s(Arrays.s(array).iterator(), size):" + name, () -> Spliterators.spliterator(Arrays.asList(ints).iterator(), ints.length, 0)));
                spliterators.add(splitDescr("Iterators.s(Arrays.s(array).iterator()):" + name, () -> Spliterators.spliteratorUnknownSize(Arrays.asList(ints).iterator(), 0)));
            }
            spliteratorTestData = spliterators.toArray(new Object[0][]);
        }
    }

    static <T> Object[] arrayDataDescr(String description, T[] data) {
        return new Object[] { description, TestData.Factory.ofArray(description, data) };
    }

    static <T> Object[] streamDataDescr(String description, Supplier<Stream<T>> supplier) {
        return new Object[] { description, TestData.Factory.ofSupplier(description, supplier) };
    }

    static <T> Object[] collectionDataDescr(String description, Collection<T> data) {
        return new Object[] { description, TestData.Factory.ofCollection(description, data) };
    }

    static <T> Object[] sbDataDescr(String description, SpinedBuffer<T> data) {
        return new Object[] { description, TestData.Factory.ofSpinedBuffer(description, data) };
    }

    static <T> Object[] splitDescr(String description, Supplier<Spliterator<T>> ss) {
        return new Object[] { description, ss };
    }

    @DataProvider(name = "StreamTestData<Integer>")
    public static Object[][] makeStreamTestData() {
        return testData;
    }

    @DataProvider(name = "StreamTestData<Integer>.small")
    public static Object[][] makeSmallStreamTestData() {
        return testSmallData;
    }

    @DataProvider(name = "StreamTestData<Integer>.mini")
    public static Object[][] makeMiniStreamTestData() {
        return testMiniData;
    }

    @DataProvider(name = "withNull:StreamTestData<Integer>")
    public static Object[][] makeStreamWithNullTestData() {
        return withNullTestData;
    }

    @DataProvider(name = "Spliterator<Integer>")
    public static Object[][] spliteratorProvider() {
        return spliteratorTestData;
    }
}
