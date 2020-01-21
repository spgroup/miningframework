package test.unit

import org.junit.Test
import static test.Assert.assertEquals
import org.junit.BeforeClass

import services.modifiedLinesCollector.DiffJParser
import services.modifiedLinesCollector.ModifiedLine

public class DiffJParserTest {

    private diffJParser = new DiffJParser()

    @Test
    public void withEmptyArrayShouldReturnEmptySet() {
        assertEquals(diffJParser.parse([]).size(), 0)
    }

    @Test
    public void withOneAddedLine() {
        def result = diffJParser.parse([
            "Base.java <=> Change.java",
            "4a4 code added in method()",
            "<      return 1;",
            "---",
            ">      int i = 0;"
        ]);


        def it = result.iterator();

        assertEquals(result.size(), 1, "Should have one modified line");
        def modLine = it.next();

        validateModifiedLineParameters(modLine, new ModifiedLine(4, "int i = 0;", ModifiedLine.ModificationType.Added))
    }

    @Test
    public void withMultipleAddedLinesInTheSamePlace() {
        def result = diffJParser.parse([
            "Base.java <=> Change.java",
            "4a4,6 code added in method()",
            "<      return 1;",
            "---",
            ">         int x = 0;",
            ">         int y = 0;",
            ">         int z = 0;"
        ]);

        def it = result.iterator()
        assertEquals(result.size(), 3, "Should have 3 modified lines")

        validateModifiedLineParameters(it.next(), new ModifiedLine(4, "int x = 0;", ModifiedLine.ModificationType.Added));
        validateModifiedLineParameters(it.next(), new ModifiedLine(5, "int y = 0;", ModifiedLine.ModificationType.Added));
        validateModifiedLineParameters(it.next(), new ModifiedLine(6, "int z = 0;", ModifiedLine.ModificationType.Added));
    }

    @Test
    public void withMultipleAddedLinesInDifferentPlaces() {
        def result = diffJParser.parse([
            "Base.java <=> Change.java",
            "4a4,5 code added in method()",
            "<      int c = 0;",
            "---",
            ">         int a = 0;",
            ">         int b = 0;",
            "5a7,8 code added in method()",
            "<      }",
            "---",
            ">      int d = 0;",
            ">      int e = 0;"
        ]);

        def it = result.iterator()
        assertEquals(result.size(), 4, "Should have 4 modified lines");
        
        validateModifiedLineParameters(it.next(), new ModifiedLine(4, "int a = 0;", ModifiedLine.ModificationType.Added));
        validateModifiedLineParameters(it.next(), new ModifiedLine(5, "int b = 0;", ModifiedLine.ModificationType.Added));
        validateModifiedLineParameters(it.next(), new ModifiedLine(7, "int d = 0;", ModifiedLine.ModificationType.Added));
        validateModifiedLineParameters(it.next(), new ModifiedLine(8, "int e = 0;", ModifiedLine.ModificationType.Added));
    }

    private void validateModifiedLineParameters(ModifiedLine actual, ModifiedLine expected) {
        assertEquals(actual.getNumber(), expected.getNumber(), "Expected line number to be ${expected.getNumber()}");
        assertEquals(actual.getContent(), expected.getContent(), "Expected line content to be ${expected.getContent()}");
        assertEquals(actual.getType(), expected.getType(), "Expected type to be ${expected.getType()}");
    }
}