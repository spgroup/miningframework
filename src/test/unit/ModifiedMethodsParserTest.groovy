package test.unit

import org.junit.Test
import static test.Assert.assertEquals
import org.junit.BeforeClass

import services.modifiedLinesCollector.ModifiedMethodsParser

public class ModifiedMethodsParserTest {

    private def methodsParser = new ModifiedMethodsParser();

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

}