import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;
import jdk.jshell.JShell;
import org.testng.annotations.Test;

@Test
public class MergedTabShiftTabTest extends UITesting {

    public void testCommand() throws Exception {
        doRunTest((inputSink, out) -> {
            inputSink.write("1\n");
            waitOutput(out, "\u0005");
            inputSink.write("/\011");
            waitOutput(out, ".*/edit.*/list.*\n\n" + Pattern.quote(getResource("jshell.console.see.synopsis")) + "\n\r\u0005/");
            inputSink.write("\011");
            waitOutput(out, ".*\n/edit\n" + Pattern.quote(getResource("help.edit.summary")) + "\n.*\n/list\n" + Pattern.quote(getResource("help.list.summary")) + ".*\n\n" + Pattern.quote(getResource("jshell.console.see.full.documentation")) + "\n\r\u0005/");
            inputSink.write("\011");
            waitOutput(out, "/!\n" + Pattern.quote(getResource("help.bang")) + "\n" + "\n" + Pattern.quote(getResource("jshell.console.see.next.command.doc")) + "\n" + "\r\u0005/");
            inputSink.write("\011");
            waitOutput(out, "/-<n>\n" + Pattern.quote(getResource("help.previous")) + "\n" + "\n" + Pattern.quote(getResource("jshell.console.see.next.command.doc")) + "\n" + "\r\u0005/");
            inputSink.write("lis\011");
            waitOutput(out, "list $");
            inputSink.write("\011");
            waitOutput(out, ".*-all.*" + "\n\n" + Pattern.quote(getResource("jshell.console.see.synopsis")) + "\n\r\u0005/");
            inputSink.write("\011");
            waitOutput(out, Pattern.quote(getResource("help.list.summary")) + "\n\n" + Pattern.quote(getResource("jshell.console.see.full.documentation")) + "\n\r\u0005/list ");
            inputSink.write("\011");
            waitOutput(out, Pattern.quote(getResource("help.list").replaceAll("\t", "    ")));
            inputSink.write("\u0003/env \011");
            waitOutput(out, "\u0005/env -\n" + "-add-exports    -add-modules    -class-path     -module-path    \n" + "\r\u0005/env -");
            inputSink.write("\011");
            waitOutput(out, "-add-exports    -add-modules    -class-path     -module-path    \n" + "\n" + Pattern.quote(getResource("jshell.console.see.synopsis")) + "\n" + "\r\u0005/env -");
            inputSink.write("\011");
            waitOutput(out, Pattern.quote(getResource("help.env.summary")) + "\n\n" + Pattern.quote(getResource("jshell.console.see.full.documentation")) + "\n" + "\r\u0005/env -");
            inputSink.write("\011");
            waitOutput(out, Pattern.quote(getResource("help.env").replaceAll("\t", "    ")) + "\n" + "\r\u0005/env -");
            inputSink.write("\011");
            waitOutput(out, "-add-exports    -add-modules    -class-path     -module-path    \n" + "\n" + Pattern.quote(getResource("jshell.console.see.synopsis")) + "\n" + "\r\u0005/env -");
            inputSink.write("\u0003/exit \011");
            waitOutput(out, Pattern.quote(getResource("help.exit.summary")) + "\n\n" + Pattern.quote(getResource("jshell.console.see.full.documentation")) + "\n\r\u0005/exit ");
            inputSink.write("\011");
            waitOutput(out, Pattern.quote(getResource("help.exit")) + "\n" + "\r\u0005/exit ");
            inputSink.write("\011");
            waitOutput(out, Pattern.quote(getResource("help.exit.summary")) + "\n\n" + Pattern.quote(getResource("jshell.console.see.full.documentation")) + "\n\r\u0005/exit ");
            inputSink.write("\u0003/doesnotexist\011");
            waitOutput(out, "\u0005/doesnotexist\n" + Pattern.quote(getResource("jshell.console.no.such.command")) + "\n" + "\n" + "\r\u0005/doesnotexist");
        });
    }

    public void testExpression() throws Exception {
        Path classes = prepareZip();
        doRunTest((inputSink, out) -> {
            inputSink.write("/env -class-path " + classes.toString() + "\n");
            waitOutput(out, Pattern.quote(getResource("jshell.msg.set.restore")) + "\n\u0005");
            inputSink.write("import jshelltest.*;\n");
            waitOutput(out, "\n\u0005");
            inputSink.write("\011");
            waitOutput(out, getMessage("jshell.console.completion.all.completions.number", "[0-9]+"));
            inputSink.write("\011");
            waitOutput(out, ".*String.*StringBuilder.*\n\r\u0005");
            inputSink.write("new JShellTes\011");
            waitOutput(out, "t\nJShellTest\\(      JShellTestAux\\(   \n\r\u0005new JShellTest");
            inputSink.write("\011");
            waitOutput(out, "JShellTest\\(      JShellTestAux\\(   \n" + "\n" + Pattern.quote(getResource("jshell.console.completion.current.signatures")) + "\n" + "jshelltest.JShellTest\n" + "\n" + Pattern.quote(getResource("jshell.console.see.documentation")) + "\n" + "\r\u0005new JShellTest");
            inputSink.write("\011");
            waitOutput(out, "jshelltest.JShellTest\n" + "JShellTest 0\n" + "\r\u0005new JShellTest");
            inputSink.write("\011");
            waitOutput(out, "JShellTest\\(      JShellTestAux\\(   \n" + "\n" + Pattern.quote(getResource("jshell.console.completion.current.signatures")) + "\n" + "jshelltest.JShellTest\n" + "\n" + Pattern.quote(getResource("jshell.console.see.documentation")) + "\n" + "\r\u0005new JShellTest");
            inputSink.write("(\011");
            waitOutput(out, "\\(\n" + Pattern.quote(getResource("jshell.console.completion.current.signatures")) + "\n" + "JShellTest\\(String str\\)\n" + "JShellTest\\(String str, int i\\)\n" + "\n" + Pattern.quote(getResource("jshell.console.see.documentation")) + "\n" + "\r\u0005new JShellTest\\(");
            inputSink.write("\011");
            waitOutput(out, "JShellTest\\(String str\\)\n" + "JShellTest 1\n" + "1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n" + "\n" + Pattern.quote(getResource("jshell.console.see.next.page")) + "\n" + "\r\u0005new JShellTest\\(");
            inputSink.write("\011");
            waitOutput(out, "1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n" + "\n" + Pattern.quote(getResource("jshell.console.see.next.javadoc")) + "\n" + "\r\u0005new JShellTest\\(");
            inputSink.write("\011");
            waitOutput(out, "JShellTest\\(String str, int i\\)\n" + "JShellTest 2\n" + "\n" + getMessage("jshell.console.completion.all.completions.number", "[0-9]+") + "\n" + "\r\u0005new JShellTest\\(");
            inputSink.write("\011");
            waitOutput(out, ".*String.*StringBuilder.*\n\r\u0005new JShellTest\\(");
            inputSink.write("\u0003String str = \"\";\nnew JShellTest(");
            waitOutput(out, "\u0005new JShellTest\\(");
            inputSink.write("\011");
            waitOutput(out, "\n" + "str   \n" + "\n" + Pattern.quote(getResource("jshell.console.completion.current.signatures")) + "\n" + "JShellTest\\(String str\\)\n" + "JShellTest\\(String str, int i\\)\n" + "\n" + Pattern.quote(getResource("jshell.console.see.documentation")) + "\n" + "\r\u0005new JShellTest\\(");
            inputSink.write("\011");
            waitOutput(out, "JShellTest\\(String str\\)\n" + "JShellTest 1\n" + "1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n" + "\n" + Pattern.quote(getResource("jshell.console.see.next.page")) + "\n" + "\r\u0005new JShellTest\\(");
            inputSink.write("\011");
            waitOutput(out, "1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n1\n" + "\n" + Pattern.quote(getResource("jshell.console.see.next.javadoc")) + "\n" + "\r\u0005new JShellTest\\(");
            inputSink.write("\011");
            waitOutput(out, "JShellTest\\(String str, int i\\)\n" + "JShellTest 2\n" + "\n" + getMessage("jshell.console.completion.all.completions.number", "[0-9]+") + "\n" + "\r\u0005new JShellTest\\(");
            inputSink.write("\011");
            waitOutput(out, ".*String.*StringBuilder.*\n\r\u0005new JShellTest\\(");
            inputSink.write("\u0003JShellTest t = new JShellTest\011");
            waitOutput(out, "\u0005JShellTest t = new JShellTest\n" + "JShellTest\\(   \n" + "\n" + Pattern.quote(getResource("jshell.console.completion.current.signatures")) + "\n" + "jshelltest.JShellTest\n" + "\n" + Pattern.quote(getResource("jshell.console.completion.all.completions")) + "\n" + "\r\u0005JShellTest t = new JShellTest");
            inputSink.write("\011");
            waitOutput(out, "JShellTest\\(      JShellTestAux\\(   \n" + "\n" + Pattern.quote(getResource("jshell.console.see.documentation")) + "\n" + "\r\u0005JShellTest t = new JShellTest");
            inputSink.write("\u0003JShellTest t = new \011");
            waitOutput(out, "\u0005JShellTest t = new \n" + "JShellTest\\(   \n" + "\n" + getMessage("jshell.console.completion.all.completions.number", "[0-9]+") + "\n" + "\r\u0005JShellTest t = new ");
            inputSink.write("\011");
            waitOutput(out, ".*String.*StringBuilder.*\n\r\u0005JShellTest t = new ");
            inputSink.write("\u0003class JShelX{}\n");
            inputSink.write("new JShel\011");
            waitOutput(out, "\u0005new JShel\n" + "JShelX\\(\\)         JShellTest\\(      JShellTestAux\\(   \n" + "\r\u0005new JShel");
            inputSink.write("\u0003new Stringbuil\011");
            waitOutput(out, "\u0005new Stringbuil\u0007");
        });
    }

    private Path prepareZip() {
        String clazz1 = "package jshelltest;\n" + "/**JShellTest 0" + " */\n" + "public class JShellTest {\n" + "    /**JShellTest 1\n" + "     * <p>1 <p>1 <p>1 <p>1 <p>1 <p>1 <p>1 <p>1 <p>1 <p>1\n" + "     * <p>1 <p>1 <p>1 <p>1 <p>1 <p>1 <p>1 <p>1 <p>1 <p>1\n" + "     * <p>1 <p>1 <p>1 <p>1 <p>1 <p>1 <p>1 <p>1 <p>1 <p>1\n" + "     */\n" + "    public JShellTest(String str) {}\n" + "    /**JShellTest 2" + "     */\n" + "    public JShellTest(String str, int i) {}\n" + "}\n";
        String clazz2 = "package jshelltest;\n" + "/**JShellTestAux 0" + " */\n" + "public class JShellTestAux {\n" + "    /**JShellTest 1" + "     */\n" + "    public JShellTestAux(String str) { }\n" + "    /**JShellTest 2" + "     */\n" + "    public JShellTestAux(String str, int i) { }\n" + "}\n";
        Path srcZip = Paths.get("src.zip");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(srcZip))) {
            out.putNextEntry(new JarEntry("jshelltest/JShellTest.java"));
            out.write(clazz1.getBytes());
            out.putNextEntry(new JarEntry("jshelltest/JShellTestAux.java"));
            out.write(clazz2.getBytes());
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        compiler.compile(clazz1, clazz2);
        try {
            Field availableSources = Class.forName("jdk.jshell.SourceCodeAnalysisImpl").getDeclaredField("availableSourcesOverride");
            availableSources.setAccessible(true);
            availableSources.set(null, Arrays.asList(srcZip));
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException | ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
        return compiler.getClassDir();
    }

    private final Compiler compiler = new Compiler();

    private final ResourceBundle resources;

    {
        resources = ResourceBundle.getBundle("jdk.internal.jshell.tool.resources.l10n", Locale.US, JShell.class.getModule());
    }

    private String getResource(String key) {
        return resources.getString(key);
    }

    private String getMessage(String key, Object... args) {
        return MessageFormat.format(resources.getString(key), args);
    }
}