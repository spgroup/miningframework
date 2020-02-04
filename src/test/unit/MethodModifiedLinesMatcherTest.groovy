package test.unit

import org.junit.Test
import static test.Assert.assertEquals
import org.junit.BeforeClass

import services.modifiedLinesCollector.MethodModifiedLinesMatcher
import services.modifiedLinesCollector.ModifiedLine
import services.modifiedLinesCollector.ModifiedMethod

public class MethodModifiedLinesMatcherTest {

    private MethodModifiedLinesMatcher methodLinesMatcher = new MethodModifiedLinesMatcher();


    @Test
    public void withOneMethodAndOneModifiedLine() {

        def methodsMap = new HashMap<String, int[]>();
        
        methodsMap.put("method()", [4]);

        def addedLine = new ModifiedLine(4, "int i = 0;", ModifiedLine.ModificationType.Added);
        def linesList = [addedLine];

        def result = methodLinesMatcher.matchModifiedMethodsAndLines(
            methodsMap,
            linesList
        );

        assertEquals(result.size(), 1);
        def method = result.iterator().next();

        assertEquals(method.getSignature(), "method()");
        
        assertEquals(method.getLines().size(), 1);
        def line = method.getLines().iterator().next();

        assertEquals(line, addedLine);
    }

    @Test
    public void withOneMethodAndMultipleLines() {
        def methodsMap = new HashMap<String, int[]>();
        
        methodsMap.put("method()", [4, 6, 7]);

        def addedLineOne = new ModifiedLine(4, "int i = 0;", ModifiedLine.ModificationType.Added);
        def addedLineTwo = new ModifiedLine(6, "int j = 0;", ModifiedLine.ModificationType.Changed);
        def addedLineThree = new ModifiedLine(7, "int w = 0;", ModifiedLine.ModificationType.Added);

        def linesList = [addedLineOne, addedLineTwo, addedLineThree];

        def result = methodLinesMatcher.matchModifiedMethodsAndLines(
            methodsMap,
            linesList
        );

        assertEquals(result.size(), 1);
        def method = result.iterator().next();

        assertEquals(method.getSignature(), "method()");
        
        assertEquals(method.getLines().size(), 3);
        def it = method.getLines().iterator();
        assertModifiedLineEquals(it.next(), addedLineTwo);
        assertModifiedLineEquals(it.next(), addedLineOne);
        assertModifiedLineEquals(it.next(), addedLineThree);
    };

    @Test
    public void withMultipleMethodsAndMultipleLines() {
        def methodsMap = new HashMap<String, int[]>();
        
        methodsMap.put("method()", [4, 6]);
        methodsMap.put("methodTwo()", [7])

        def addedLineOne = new ModifiedLine(4, "int i = 0;", ModifiedLine.ModificationType.Added);
        def addedLineTwo = new ModifiedLine(6, "int j = 0;", ModifiedLine.ModificationType.Changed);
        def addedLineThree = new ModifiedLine(7, "int w = 0;", ModifiedLine.ModificationType.Added);

        def linesList = [addedLineOne, addedLineTwo, addedLineThree];

        def result = methodLinesMatcher.matchModifiedMethodsAndLines(
            methodsMap,
            linesList
        );

        assertEquals(result.size(), 2);

        def methodIterator = result.iterator();
        def method = methodIterator.next();

        assertEquals(method.getSignature(), "method()");

        def lineIterator = method.getLines().iterator();
        assertEquals(method.getLines().size(), 2);
        
        def first = lineIterator.next()
        def second = lineIterator.next()

        assertModifiedLineEquals(second, addedLineOne);
        assertModifiedLineEquals(first, addedLineTwo);

        method = methodIterator.next();

        assertEquals(method.getSignature(), "methodTwo()");

        assertEquals(method.getLines().size(), 1);
        lineIterator = method.getLines().iterator();

        assertModifiedLineEquals(lineIterator.next(), addedLineThree);
    }

    private assertModifiedLineEquals(ModifiedLine actual, ModifiedLine expected) {
        assertEquals(actual.getNumber(), expected.getNumber());
        assertEquals(actual.getContent(), expected.getContent());
        assertEquals(actual.getType(), expected.getType());
    }

}