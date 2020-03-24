package unit

import org.junit.Test
import static util.Assert.assertEquals

import services.modifiedLinesCollector.TextualDiffParser
import services.modifiedLinesCollector.ModifiedLine

public class TextualDiffParserTest {

    private diffParser = new TextualDiffParser()

    @Test
    public void withEmptyArrayShouldReturnEmptySet() {
        assertEquals(diffParser.parse([]).size(), 0)
    }

    @Test
    public void withOneAddedLine() {
        def result = diffParser.parse([
            "4a4",
            ">      int i = 0;"
        ]);


        def it = result.iterator();

        assertEquals(result.size(), 1, "Should have one modified line");
        def modLine = it.next();

        validateModifiedLineParameters(modLine, new ModifiedLine(4, "int i = 0;", ModifiedLine.ModificationType.Added))
    }

    @Test
    public void withMultipleAddedLinesInTheSamePlace() {
        def result = diffParser.parse([
            "4a4,6      ",
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
        def result = diffParser.parse([
            "4a4,5",
            ">         int a = 0;",
            ">         int b = 0;",
            "5a7,8",
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

    /**
        This case should be irrelevant because diff will not consider methods
        but it is a good way to test the parser behaviour with files with multiple differences    
    */ 
    @Test
    public void withMultipleAddedLinesInDifferentMethods() {
        def result = diffParser.parse([
            "3a4,5",
            ">      int x = 0;",
            ">      int y = 0;",
            "7a10",
            ">      int x = 2;",
            "8a12",
            ">      int y = 2;",
            "12a17,18",
            ">      int x = 1;",
            ">      int y = 1;"
        ]);

        def it = result.iterator()
        assertEquals(result.size(), 6, "Should have 6 modified lines");
        validateModifiedLineParameters(it.next(), new ModifiedLine(4, "int x = 0;", ModifiedLine.ModificationType.Added));
        validateModifiedLineParameters(it.next(), new ModifiedLine(5, "int y = 0;", ModifiedLine.ModificationType.Added));
        validateModifiedLineParameters(it.next(), new ModifiedLine(10, "int x = 2;", ModifiedLine.ModificationType.Added));
        validateModifiedLineParameters(it.next(), new ModifiedLine(12, "int y = 2;", ModifiedLine.ModificationType.Added));
        validateModifiedLineParameters(it.next(), new ModifiedLine(17, "int x = 1;", ModifiedLine.ModificationType.Added));
        validateModifiedLineParameters(it.next(), new ModifiedLine(18, "int y = 1;", ModifiedLine.ModificationType.Added));
    }

    @Test
    public void withOneChangedLine() {
        def result = diffParser.parse([
            "4c4",
            "<      int i = 0;",
            "---",
            ">      int i = 1;"
        ]);

        def it = result.iterator();
        assertEquals(result.size(), 1, "Should have one modified line");
        validateModifiedLineParameters(it.next(), new ModifiedLine(4, "int i = 1;", ModifiedLine.ModificationType.Changed));
    }

    @Test
    public void withMultipleChangedLinesInOnePlace() {
        def result = diffParser.parse([
            "4,6c4,6",
            "<      int x = 0;",
            "<      int y = 0;",
            "<      int z = 0;",
            "---",
            ">      int x = 1;",
            ">      int y = 2;",
            ">      int z = 3;"
        ]);

        def it = result.iterator()
        assertEquals(result.size(), 3, "Should have 3 modified lines");
        validateModifiedLineParameters(it.next(), new ModifiedLine(4, "int x = 1;", ModifiedLine.ModificationType.Changed));
        validateModifiedLineParameters(it.next(), new ModifiedLine(5, "int y = 2;", ModifiedLine.ModificationType.Changed));
        validateModifiedLineParameters(it.next(), new ModifiedLine(6, "int z = 3;", ModifiedLine.ModificationType.Changed));
    }

    @Test
    public void withOneRemovedLine() {
        def result = diffParser.parse([
            "4d3",
            "<      int x = 1;"
        ])

        def it = result.iterator();
        assertEquals(result.size(), 1, "Should have 1 modified line");
        validateModifiedLineParameters(it.next(), new ModifiedLine(3, "int x = 1;", ModifiedLine.ModificationType.Removed));
    }

    private void validateModifiedLineParameters(ModifiedLine actual, ModifiedLine expected) {
        assertEquals(actual.getNumber(), expected.getNumber(), "Expected line number to be ${expected.getNumber()}");
        assertEquals(actual.getContent(), expected.getContent(), "Expected line content to be ${expected.getContent()}");
        assertEquals(actual.getType(), expected.getType(), "Expected type to be ${expected.getType()}");
    }
}