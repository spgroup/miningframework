package test

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

import test.unit.TestSuite
import test.integration.TestSuite

@RunWith(Suite.class)
@SuiteClasses([test.unit.TestSuite.class, test.integration.TestSuite.class])
public class TestSuite {}