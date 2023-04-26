package org.codehaus.groovy.tools.groovydoc;

import groovy.util.CharsetToolkit;
import org.apache.tools.ant.BuildFileTest;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.util.List;

public class GroovyDocTest extends BuildFileTest {

    private static final String SRC_TESTFILES = "src/test/resources/groovydoc/";

    private File tmpDir;

    public GroovyDocTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        configureProject(SRC_TESTFILES + "groovyDocTests.xml");
        tmpDir = new File(getProject().getProperty("tmpdir"));
    }

    public void testCustomClassTemplate() throws Exception {
        executeTarget("testCustomClassTemplate");
        final File testfilesPackageDir = new File(tmpDir, "org/codehaus/groovy/tools/groovydoc/testfiles");
        System.err.println("testfilesPackageDir = " + testfilesPackageDir);
        final String[] list = testfilesPackageDir.list(new FilenameFilter() {

            public boolean accept(File file, String name) {
                return name.equals("DocumentedClass.html");
            }
        });
        assertNotNull("Dir not found: " + testfilesPackageDir.getAbsolutePath(), list);
        assertEquals(1, list.length);
        File documentedClassHtmlDoc = new File(testfilesPackageDir, list[0]);
        List<String> lines = ResourceGroovyMethods.readLines(documentedClassHtmlDoc);
        assertTrue("\"<title>DocumentedClass</title>\" not in: " + lines, lines.contains("<title>DocumentedClass</title>"));
        assertTrue("\"This is a custom class template.\" not in: " + lines, lines.contains("This is a custom class template."));
    }

    public void testFileEncoding() throws Exception {
        executeTarget("testFileEncoding");
        final File testfilesPackageDir = new File(tmpDir, "org/codehaus/groovy/tools/groovydoc/testfiles");
        System.err.println("testfilesPackageDir = " + testfilesPackageDir);
        final String[] list = testfilesPackageDir.list(new FilenameFilter() {

            public boolean accept(File file, String name) {
                return name.equals("DocumentedClass.html");
            }
        });
        File documentedClassHtmlDoc = new File(testfilesPackageDir, list[0]);
        CharsetToolkit charsetToolkit = new CharsetToolkit(documentedClassHtmlDoc);
        assertEquals("The generated groovydoc must be in 'UTF-16LE' file encoding.'", Charset.forName("UTF-16LE"), charsetToolkit.getCharset());
    }
}
