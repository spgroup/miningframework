package unit

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

@RunWith(Suite.class)
@SuiteClasses([
        TextualDiffParserTest.class,
        DiffJParserTest.class,
        MethodModifiedLinesMatcherTest.class,
        ModifiedMethodsHelperTest.class,
        JarHelperTest.class
])
public class TestSuite {}