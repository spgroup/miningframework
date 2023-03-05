package org.openjdk.tests.java.util.stream;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.TreeSet;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import static java.util.stream.LambdaTestHelpers.*;
import static org.testng.Assert.*;

@Test
public class ConcatTest {

    private static Object[][] cases;

    static {
        List<Integer> part1 = Arrays.asList(5, 3, 4, 1, 2, 6, 2, 4);
        List<Integer> part2 = Arrays.asList(8, 8, 6, 6, 9, 7, 10, 9);
        List<Integer> p1p2 = Arrays.asList(5, 3, 4, 1, 2, 6, 2, 4, 8, 8, 6, 6, 9, 7, 10, 9);
        List<Integer> p2p1 = Arrays.asList(8, 8, 6, 6, 9, 7, 10, 9, 5, 3, 4, 1, 2, 6, 2, 4);
        List<Integer> empty = new LinkedList<>();
        assertTrue(empty.isEmpty());
        LinkedHashSet<Integer> distinctP1 = new LinkedHashSet<>(part1);
        LinkedHashSet<Integer> distinctP2 = new LinkedHashSet<>(part2);
        TreeSet<Integer> sortedP1 = new TreeSet<>(part1);
        TreeSet<Integer> sortedP2 = new TreeSet<>(part2);
        cases = new Object[][] { { "regular", part1, part2, p1p2 }, { "reverse regular", part2, part1, p2p1 }, { "front distinct", distinctP1, part2, Arrays.asList(5, 3, 4, 1, 2, 6, 8, 8, 6, 6, 9, 7, 10, 9) }, { "back distinct", part1, distinctP2, Arrays.asList(5, 3, 4, 1, 2, 6, 2, 4, 8, 6, 9, 7, 10) }, { "both distinct", distinctP1, distinctP2, Arrays.asList(5, 3, 4, 1, 2, 6, 8, 6, 9, 7, 10) }, { "front sorted", sortedP1, part2, Arrays.asList(1, 2, 3, 4, 5, 6, 8, 8, 6, 6, 9, 7, 10, 9) }, { "back sorted", part1, sortedP2, Arrays.asList(5, 3, 4, 1, 2, 6, 2, 4, 6, 7, 8, 9, 10) }, { "both sorted", sortedP1, sortedP2, Arrays.asList(1, 2, 3, 4, 5, 6, 6, 7, 8, 9, 10) }, { "reverse both sorted", sortedP2, sortedP1, Arrays.asList(6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6) }, { "empty something", empty, part1, part1 }, { "something empty", part1, empty, part1 }, { "empty empty", empty, empty, empty } };
    }

    @DataProvider(name = "cases")
    private static Object[][] getCases() {
        return cases;
    }

    @Factory(dataProvider = "cases")
    public static Object[] createTests(String scenario, Collection<Integer> c1, Collection<Integer> c2, Collection<Integer> expected) {
        return new Object[] { new ConcatTest(scenario, c1, c2, expected) };
    }

    protected final String scenario;

    protected final Collection<Integer> c1;

    protected final Collection<Integer> c2;

    protected final Collection<Integer> expected;

    public ConcatTest(String scenario, Collection<Integer> c1, Collection<Integer> c2, Collection<Integer> expected) {
        this.scenario = scenario;
        this.c1 = c1;
        this.c2 = c2;
        this.expected = expected;
        Stream<Integer> s1s = c1.stream();
        Stream<Integer> s2s = c2.stream();
        Stream<Integer> s1p = c1.parallelStream();
        Stream<Integer> s2p = c2.parallelStream();
        assertTrue(s1p.isParallel());
        assertTrue(s2p.isParallel());
        assertFalse(s1s.isParallel());
        assertFalse(s2s.isParallel());
        assertTrue(s1s.spliterator().hasCharacteristics(Spliterator.ORDERED));
        assertTrue(s1p.spliterator().hasCharacteristics(Spliterator.ORDERED));
        assertTrue(s2s.spliterator().hasCharacteristics(Spliterator.ORDERED));
        assertTrue(s2p.spliterator().hasCharacteristics(Spliterator.ORDERED));
    }

    private <T> void assertConcatContent(Spliterator<T> sp, boolean ordered, Spliterator<T> expected) {
        assertFalse(sp.hasCharacteristics(Spliterator.DISTINCT), scenario);
        assertFalse(sp.hasCharacteristics(Spliterator.SORTED), scenario);
        assertEquals(sp.hasCharacteristics(Spliterator.ORDERED), ordered, scenario);
        if (ordered) {
            assertEquals(toBoxedList(sp), toBoxedList(expected), scenario);
        } else {
            assertEquals(toBoxedMultiset(sp), toBoxedMultiset(expected), scenario);
        }
    }

    private void assertRefConcat(Stream<Integer> s1, Stream<Integer> s2, boolean parallel, boolean ordered) {
        Stream<Integer> result = Stream.concat(s1, s2);
        assertEquals(result.isParallel(), parallel);
        assertConcatContent(result.spliterator(), ordered, expected.spliterator());
    }

    private void assertIntConcat(Stream<Integer> s1, Stream<Integer> s2, boolean parallel, boolean ordered) {
        IntStream result = IntStream.concat(s1.mapToInt(Integer::intValue), s2.mapToInt(Integer::intValue));
        assertEquals(result.isParallel(), parallel);
        assertConcatContent(result.spliterator(), ordered, expected.stream().mapToInt(Integer::intValue).spliterator());
    }

    private void assertLongConcat(Stream<Integer> s1, Stream<Integer> s2, boolean parallel, boolean ordered) {
        LongStream result = LongStream.concat(s1.mapToLong(Integer::longValue), s2.mapToLong(Integer::longValue));
        assertEquals(result.isParallel(), parallel);
        assertConcatContent(result.spliterator(), ordered, expected.stream().mapToLong(Integer::longValue).spliterator());
    }

    private void assertDoubleConcat(Stream<Integer> s1, Stream<Integer> s2, boolean parallel, boolean ordered) {
        DoubleStream result = DoubleStream.concat(s1.mapToDouble(Integer::doubleValue), s2.mapToDouble(Integer::doubleValue));
        assertEquals(result.isParallel(), parallel);
        assertConcatContent(result.spliterator(), ordered, expected.stream().mapToDouble(Integer::doubleValue).spliterator());
    }

    public void testRefConcat() {
        assertRefConcat(c1.stream(), c2.stream(), false, true);
        assertRefConcat(c1.parallelStream(), c2.parallelStream(), true, true);
        assertRefConcat(c1.stream(), c2.parallelStream(), true, true);
        assertRefConcat(c1.parallelStream(), c2.stream(), true, true);
        assertRefConcat(c1.stream().unordered(), c2.stream(), false, false);
        assertRefConcat(c1.stream(), c2.stream().unordered(), false, false);
        assertRefConcat(c1.parallelStream().unordered(), c2.stream().unordered(), true, false);
    }

    public void testIntConcat() {
        assertIntConcat(c1.stream(), c2.stream(), false, true);
        assertIntConcat(c1.parallelStream(), c2.parallelStream(), true, true);
        assertIntConcat(c1.stream(), c2.parallelStream(), true, true);
        assertIntConcat(c1.parallelStream(), c2.stream(), true, true);
        assertIntConcat(c1.stream().unordered(), c2.stream(), false, false);
        assertIntConcat(c1.stream(), c2.stream().unordered(), false, false);
        assertIntConcat(c1.parallelStream().unordered(), c2.stream().unordered(), true, false);
    }

    public void testLongConcat() {
        assertLongConcat(c1.stream(), c2.stream(), false, true);
        assertLongConcat(c1.parallelStream(), c2.parallelStream(), true, true);
        assertLongConcat(c1.stream(), c2.parallelStream(), true, true);
        assertLongConcat(c1.parallelStream(), c2.stream(), true, true);
        assertLongConcat(c1.stream().unordered(), c2.stream(), false, false);
        assertLongConcat(c1.stream(), c2.stream().unordered(), false, false);
        assertLongConcat(c1.parallelStream().unordered(), c2.stream().unordered(), true, false);
    }

    public void testDoubleConcat() {
        assertDoubleConcat(c1.stream(), c2.stream(), false, true);
        assertDoubleConcat(c1.parallelStream(), c2.parallelStream(), true, true);
        assertDoubleConcat(c1.stream(), c2.parallelStream(), true, true);
        assertDoubleConcat(c1.parallelStream(), c2.stream(), true, true);
        assertDoubleConcat(c1.stream().unordered(), c2.stream(), false, false);
        assertDoubleConcat(c1.stream(), c2.stream().unordered(), false, false);
        assertDoubleConcat(c1.parallelStream().unordered(), c2.stream().unordered(), true, false);
    }
}
