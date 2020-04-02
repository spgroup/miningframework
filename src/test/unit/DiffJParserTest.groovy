package unit

import org.junit.Test
import static util.Assert.assertEquals

import services.dataCollectors.modifiedLinesCollector.DiffJParser

public class DiffJParserTest {

    private def methodsParser = new DiffJParser();

    @Test
    public void withOneModifiedMethodWithOneAddedLine() {
        def result = methodsParser.parse([
            "add-line/Left.java <=> add-line/Right.java",
            "4a4: code added in method()"   
        ])

        assertEquals(result.size(), 1);
        assertEquals(result.get("method()"), [4])
    }

    @Test
    public void withOneModifiedMethodWithMultipleAddedLines() {
        def result = methodsParser.parse([
            "multiple-added-lines/Left.java <=> multiple-added-lines/Right.java",
            "4a4,6: code added in method()"
        ])

        assertEquals(result.size(), 1);
        assertEquals(result.get("method()"), [4, 5, 6]);
    }

    @Test
    public void withOneModifiedMethodWithMultipleAddedLinesInDifferentPlaces() {
        def result = methodsParser.parse([
            "multiple-added-lines-different-places/Left.java <=> multiple-added-lines-different-places/Right.java",
            "4a4,5: code added in method()",
            "5a7,8: code added in method()"
        ])

        assertEquals(result.size(), 1);
        assertEquals(result.get("method()"), [4, 5, 7, 8]);
    }

    @Test
    public void withTwoModifiedMethodsWithMultipleLinesInDifferentPlaces() {
        def result = methodsParser.parse([
            "multiple-added-lines-different-methods/Left.java <=> multiple-added-lines-different-methods/Right.java",
            "4a4,5: code added in methodOne()",
            "5a7,8: code added in methodOne()",
            "8a12: code added in methodTwo()",
            "9a14: code added in methodTwo()"
        ])

        assertEquals(result.size(), 2);
        assertEquals(result.get("methodOne()"), [4, 5, 7, 8]);
        assertEquals(result.get("methodTwo()"), [12, 14]);
    }

    @Test
    public void withOneModifiedMethodWithOneChangedLine() {
        def result = methodsParser.parse([
            "change-expression/Left.java <=> change-expression/Right.java",
            "4c4: code changed in method()"
        ]);

        assertEquals(result.size(), 1);
        assertEquals(result.get("method()"), [4])        
    }

    @Test
    public void withAMethodWithMultipleArguments() {
        def result = methodsParser.parse([
            "change-expression/Left.java <=> change-expression/Right.java",
            "4a4: code changed in method(int, int)"
        ])

        assertEquals(result.size(), 1);
        assertEquals(result.get("method(int, int)"), [4]);
    }

    @Test
    public void withOneModifiedMethodAndOneFullyRemovedLine() {
        def result = methodsParser.parse([
           "remove-full-line/Right.java remove-full-line/Left.java <=> remove-full-line/Right.java",
            "4d5: code removed in method()"
        ])

        assertEquals(result.size(), 1);
        assertEquals(result.get("method()"), [5])
    }

    @Test
    public void withMultipleTypesOfChanges() {
        def result = methodsParser.parse([
            "different-types-of-changes/Left.java <=> different-types-of-changes/Right.java",
            "4d4: code removed in method()",
            "7c6: code changed in method()",
            "8c7: code changed in method()",
            "9c8: code changed in method()",
            "11d10: code removed in method()"
        ])

        assertEquals(result.size(), 1);
        assertEquals(result.get("method()"), [4, 6, 7,8, 10]);
    }
}