package org.antlr.v4.test.runtime.swift;

import org.antlr.v4.Tool;
import org.antlr.v4.test.runtime.ErrorQueue;
import org.antlr.v4.test.runtime.RuntimeTestSupport;
import org.antlr.v4.test.runtime.StreamVacuum;
import org.stringtemplate.v4.ST;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.antlr.v4.test.runtime.BaseRuntimeTest.antlrOnString;
import static org.antlr.v4.test.runtime.BaseRuntimeTest.writeFile;
import static org.junit.Assert.assertTrue;

public class BaseSwiftTest implements RuntimeTestSupport {

    private static final String BASE_TEST_DIR;

    private static String ANTLR_FRAMEWORK_DIR;

    static {
        String baseTestDir = System.getProperty("antlr-swift-test-dir");
        if (baseTestDir == null || baseTestDir.isEmpty()) {
            baseTestDir = System.getProperty("java.io.tmpdir");
        }
        if (!new File(baseTestDir).isDirectory()) {
            throw new UnsupportedOperationException("The specified base test directory does not exist: " + baseTestDir);
        }
        BASE_TEST_DIR = baseTestDir;
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final URL swiftRuntime = loader.getResource("Swift/Sources/Antlr4");
        if (swiftRuntime == null) {
            throw new RuntimeException("Swift runtime file not found at:" + swiftRuntime.getPath());
        }
        String swiftRuntimePath = swiftRuntime.getPath();
        try {
            String commandLine = "find " + swiftRuntimePath + "/ -iname *.swift -not -name merge.swift -exec cat {} ;";
            ProcessBuilder builder = new ProcessBuilder(commandLine.split(" "));
            builder.redirectError(ProcessBuilder.Redirect.INHERIT);
            Process p = builder.start();
            StreamVacuum stdoutVacuum = new StreamVacuum(p.getInputStream());
            stdoutVacuum.start();
            p.waitFor();
            stdoutVacuum.join();
            String antlrSwift = stdoutVacuum.toString();
            ANTLR_FRAMEWORK_DIR = new File(BASE_TEST_DIR, "Antlr4").getAbsolutePath();
            mkdir(ANTLR_FRAMEWORK_DIR);
            writeFile(ANTLR_FRAMEWORK_DIR, "Antlr4.swift", antlrSwift);
            buildAntlr4Framework();
            String argsString;
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                eraseAntlrFrameWorkDir();
            }
        });
    }

    private static void eraseFilesIn(String dirName) {
        if (dirName == null) {
            return;
        }
        File dir = new File(dirName);
        String[] files = dir.list();
        if (files != null)
            for (String file : files) {
                new File(dirName + "/" + file).delete();
            }
    }

    private static void eraseAntlrFrameWorkDir() {
        File frameworkdir = new File(ANTLR_FRAMEWORK_DIR);
        if (frameworkdir.exists()) {
            eraseFilesIn(ANTLR_FRAMEWORK_DIR);
            frameworkdir.delete();
        }
    }

    private static boolean buildAntlr4Framework() throws Exception {
        String argsString = "xcrun -sdk macosx swiftc -emit-library -emit-module Antlr4.swift -module-name Antlr4 -module-link-name Antlr4 -Xlinker -install_name -Xlinker " + ANTLR_FRAMEWORK_DIR + "/libAntlr4.dylib ";
        return runProcess(argsString, ANTLR_FRAMEWORK_DIR);
    }

    public String tmpdir = null;

    private StringBuilder antlrToolErrors;

    protected String stderrDuringParse;

    @Override
    public void testSetUp() throws Exception {
        String propName = "antlr-swift-test-dir";
        String prop = System.getProperty(propName);
        if (prop != null && prop.length() > 0) {
            tmpdir = prop;
        } else {
            tmpdir = new File(System.getProperty("java.io.tmpdir"), getClass().getSimpleName() + "-" + Thread.currentThread().getName() + "-" + System.currentTimeMillis()).getAbsolutePath();
        }
        antlrToolErrors = new StringBuilder();
    }

    @Override
    public void testTearDown() throws Exception {
    }

    @Override
    public void eraseTempDir() {
    }

    @Override
    public String getTmpDir() {
        return tmpdir;
    }

    @Override
    public String getStdout() {
        return null;
    }

    @Override
    public String getParseErrors() {
        return stderrDuringParse;
    }

    @Override
    public String getANTLRToolErrors() {
        if (antlrToolErrors.length() == 0) {
            return null;
        }
        return antlrToolErrors.toString();
    }

    @Override
    public String execLexer(String grammarFileName, String grammarStr, String lexerName, String input, boolean showDFA) {
        boolean success = rawGenerateRecognizer(grammarFileName, grammarStr, null, lexerName);
        assertTrue(success);
        writeFile(tmpdir, "input", input);
        writeLexerTestFile(lexerName, showDFA);
        addSourceFiles("main.swift");
        compile();
        String output = execTest();
        return output;
    }

    private String execTest() {
        try {
            String exec = tmpdir + "/" + EXEC_NAME;
            String[] args = new String[] { exec, "input" };
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.directory(new File(tmpdir));
            Process p = pb.start();
            StreamVacuum stdoutVacuum = new StreamVacuum(p.getInputStream());
            StreamVacuum stderrVacuum = new StreamVacuum(p.getErrorStream());
            stdoutVacuum.start();
            stderrVacuum.start();
            p.waitFor();
            stdoutVacuum.join();
            stderrVacuum.join();
            String output = stdoutVacuum.toString();
            if (output.length() == 0) {
                output = null;
            }
            if (stderrVacuum.toString().length() > 0) {
                this.stderrDuringParse = stderrVacuum.toString();
            }
            return output;
        } catch (Exception e) {
            System.err.println("can't exec recognizer");
            e.printStackTrace(System.err);
        }
        return null;
    }

    private Set<String> sourceFiles = new HashSet<String>();

    private void addSourceFiles(String... files) {
        Collections.addAll(this.sourceFiles, files);
    }

    public boolean compile() {
        try {
            return buildProject();
        } catch (Exception e) {
            return false;
        }
    }

    private static final String EXEC_NAME = "Test";

    private boolean buildProject() throws Exception {
        String fileList = sourceFiles.toString().replace("[", "").replace("]", "").replace(", ", " ");
        String argsString = "xcrun -sdk macosx swiftc " + fileList + " -o " + EXEC_NAME + " -I " + ANTLR_FRAMEWORK_DIR + " -L " + ANTLR_FRAMEWORK_DIR + " -module-link-name Antlr4 -suppress-warnings";
        return runProcess(argsString, tmpdir);
    }

    private static boolean runProcess(String argsString, String execPath) throws IOException, InterruptedException {
        String[] args = argsString.split(" ");
        Process process = Runtime.getRuntime().exec(args, null, new File(execPath));
        StreamVacuum stdoutVacuum = new StreamVacuum(process.getInputStream());
        StreamVacuum stderrVacuum = new StreamVacuum(process.getErrorStream());
        stdoutVacuum.start();
        stderrVacuum.start();
        process.waitFor();
        stdoutVacuum.join();
        stderrVacuum.join();
        if (stderrVacuum.toString().length() > 0) {
            System.err.println("buildProject stderrVacuum: " + stderrVacuum);
        }
        return process.exitValue() == 0;
    }

    @Override
    public String execParser(String grammarFileName, String grammarStr, String parserName, String lexerName, String listenerName, String visitorName, String startRuleName, String input, boolean showDiagnosticErrors) {
        return execParser(grammarFileName, grammarStr, parserName, lexerName, startRuleName, input, showDiagnosticErrors, false);
    }

    protected String execParser(String grammarFileName, String grammarStr, String parserName, String lexerName, String startRuleName, String input, boolean debug, boolean profile) {
        boolean success = rawGenerateRecognizer(grammarFileName, grammarStr, parserName, lexerName, "-visitor");
        assertTrue(success);
        writeFile(tmpdir, "input", input);
        return rawExecRecognizer(parserName, lexerName, startRuleName, debug, profile);
    }

    protected String rawExecRecognizer(String parserName, String lexerName, String parserStartRuleName, boolean debug, boolean profile) {
        this.stderrDuringParse = null;
        if (parserName == null) {
            writeLexerTestFile(lexerName, false);
        } else {
            writeParserTestFile(parserName, lexerName, parserStartRuleName, debug, profile);
        }
        addSourceFiles("main.swift");
        return execRecognizer();
    }

    public String execRecognizer() {
        compile();
        return execTest();
    }

    protected void writeParserTestFile(String parserName, String lexerName, String parserStartRuleName, boolean debug, boolean profile) {
        ST outputFileST = new ST("import Antlr4\n" + "import Foundation\n" + "setbuf(__stdoutp, nil)\n" + "class TreeShapeListener: ParseTreeListener{\n" + "    func visitTerminal(_ node: TerminalNode){ }\n" + "    func visitErrorNode(_ node: ErrorNode){ }\n" + "    func enterEveryRule(_ ctx: ParserRuleContext) throws { }\n" + "    func exitEveryRule(_ ctx: ParserRuleContext) throws {\n" + "        for i in 0..\\<ctx.getChildCount() {\n" + "            let parent = ctx.getChild(i)?.getParent()\n" + "            if (!(parent is RuleNode) || (parent as! RuleNode ).getRuleContext() !== ctx) {\n" + "                throw ANTLRError.illegalState(msg: \"Invalid parse tree shape detected.\")\n" + "            }\n" + "        }\n" + "    }\n" + "}\n" + "\n" + "do {\n" + "let args = CommandLine.arguments\n" + "let input = ANTLRFileStream(args[1])\n" + "let lex = <lexerName>(input)\n" + "let tokens = CommonTokenStream(lex)\n" + "<createParser>\n" + "parser.setBuildParseTree(true)\n" + "<profile>\n" + "let tree = try parser.<parserStartRuleName>()\n" + "<if(profile)>print(profiler.getDecisionInfo().description)<endif>\n" + "try ParseTreeWalker.DEFAULT.walk(TreeShapeListener(), tree)\n" + "}catch ANTLRException.cannotInvokeStartRule {\n" + "    print(\"error occur: cannotInvokeStartRule\")\n" + "}catch ANTLRException.recognition(let e )   {\n" + "    print(\"error occur\\(e)\")\n" + "}catch {\n" + "    print(\"error occur\")\n" + "}\n");
        ST createParserST = new ST("       let parser = try <parserName>(tokens)\n");
        if (debug) {
            createParserST = new ST("        let parser = try <parserName>(tokens)\n" + "        parser.addErrorListener(DiagnosticErrorListener())\n");
        }
        if (profile) {
            outputFileST.add("profile", "let profiler = ProfilingATNSimulator(parser)\n" + "parser.setInterpreter(profiler)");
        } else {
            outputFileST.add("profile", new ArrayList<Object>());
        }
        outputFileST.add("createParser", createParserST);
        outputFileST.add("parserName", parserName);
        outputFileST.add("lexerName", lexerName);
        outputFileST.add("parserStartRuleName", parserStartRuleName);
        writeFile(tmpdir, "main.swift", outputFileST.render());
    }

    protected void writeLexerTestFile(String lexerName, boolean showDFA) {
        ST outputFileST = new ST("import Antlr4\n" + "import Foundation\n" + "setbuf(__stdoutp, nil)\n" + "let args = CommandLine.arguments\n" + "let input = ANTLRFileStream(args[1])\n" + "let lex = <lexerName>(input)\n" + "let tokens = CommonTokenStream(lex)\n" + "do {\n" + "	try tokens.fill()\n" + "} catch ANTLRException.cannotInvokeStartRule {\n" + "	print(\"error occur: cannotInvokeStartRule\")\n" + "} catch ANTLRException.recognition(let e )   {\n" + "	print(\"error occur\\(e)\")\n" + "} catch {\n" + "	print(\"error occur\")\n" + "}\n" + "for t in tokens.getTokens() {\n" + "	print(t)\n" + "}\n" + (showDFA ? "print(lex.getInterpreter().getDFA(Lexer.DEFAULT_MODE).toLexerString(), terminator: \"\" )\n" : ""));
        outputFileST.add("lexerName", lexerName);
        writeFile(tmpdir, "main.swift", outputFileST.render());
    }

    private boolean rawGenerateRecognizer(String grammarFileName, String grammarStr, String parserName, String lexerName, String... extraOptions) {
        return rawGenerateRecognizer(grammarFileName, grammarStr, parserName, lexerName, false, extraOptions);
    }

    private boolean rawGenerateRecognizer(String grammarFileName, String grammarStr, String parserName, String lexerName, boolean defaultListener, String... extraOptions) {
        ErrorQueue equeue = antlrOnString(getTmpDir(), "Swift", grammarFileName, grammarStr, defaultListener, extraOptions);
        if (!equeue.errors.isEmpty()) {
            return false;
        }
        List<String> files = new ArrayList<String>();
        if (lexerName != null) {
            files.add(lexerName + ".swift");
            files.add(lexerName + "ATN.swift");
        }
        if (parserName != null) {
            files.add(parserName + ".swift");
            files.add(parserName + "ATN.swift");
            Set<String> optionsSet = new HashSet<String>(Arrays.asList(extraOptions));
            String grammarName = grammarFileName.substring(0, grammarFileName.lastIndexOf('.'));
            if (!optionsSet.contains("-no-listener")) {
                files.add(grammarName + "Listener.swift");
                files.add(grammarName + "BaseListener.swift");
            }
            if (optionsSet.contains("-visitor")) {
                files.add(grammarName + "Visitor.swift");
                files.add(grammarName + "BaseVisitor.swift");
            }
        }
        addSourceFiles(files.toArray(new String[files.size()]));
        return true;
    }

    protected static void mkdir(String dir) {
        File f = new File(dir);
        f.mkdirs();
    }

    protected Tool newTool(String[] args) {
        return new Tool(args);
    }

    protected Tool newTool() {
        return new Tool(new String[] { "-o", tmpdir });
    }
}
