package unit

import app.MiningFramework
import arguments.Arguments
import arguments.InputParser
import com.google.inject.Guice
import com.google.inject.Injector
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses
import project.Project
import util.FileManager

@RunWith(Suite.class)
@SuiteClasses([TextualDiffParserTest.class, DiffJParserTest.class, MethodModifiedLinesMatcherTest.class])
public class TestSuite {
}