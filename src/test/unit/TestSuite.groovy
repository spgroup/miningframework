package test.unit

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

@RunWith(Suite.class)
@SuiteClasses([DiffParserTest.class, ModifiedMethodsParserTest.class, MethodModifiedLinesMatcherTest.class])
public class TestSuite {}