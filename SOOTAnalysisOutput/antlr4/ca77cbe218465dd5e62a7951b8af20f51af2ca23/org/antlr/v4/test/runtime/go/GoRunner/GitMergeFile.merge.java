package org.antlr.v4.test.runtime.go;

import org.antlr.v4.test.runtime.*;
import org.antlr.v4.test.runtime.states.CompiledState;
import org.antlr.v4.test.runtime.states.GeneratedState;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import static org.antlr.v4.test.runtime.FileUtils.replaceInFile;

public class GoRunner extends RuntimeRunner {

    @Override
    public String getLanguage() {
        return "Go";
    }

    @Override
    public String getLexerSuffix() {
        return "_lexer";
    }

    @Override
    public String getParserSuffix() {
        return "_parser";
    }

    @Override
    public String getBaseListenerSuffix() {
        return "_base_listener";
    }

    @Override
    public String getListenerSuffix() {
        return "_listener";
    }

    @Override
    public String getBaseVisitorSuffix() {
        return "_base_visitor";
    }

    @Override
    public String getVisitorSuffix() {
        return "_visitor";
    }

    @Override
    protected String grammarNameToFileName(String grammarName) {
        return grammarName.toLowerCase();
    }

    @Override
    public String[] getExtraRunArgs() {
        return new String[] { "run" };
    }

    private final static String antlrTestPackageName = "antlr";

    private static final String goModFileName = "go.mod";

    private static final String GoRuntimeImportPath = "github.com/antlr/antlr4/runtime/Go/antlr";

    private static final Path packageBase;

    private static final String packageBaseString;

    private static String goModContent = null;

    private final static Map<String, String> environment;

    static {
        packageBase = Paths.get(cacheDirectory, "Go");
        packageBaseString = packageBase.toString();
        environment = new HashMap<>();
        environment.put("GOWORK", "off");
    }

    @Override
    protected String grammarParseRuleToRecognizerName(String startRuleName) {
        if (startRuleName == null || startRuleName.length() == 0) {
            return null;
        }
        return startRuleName.substring(0, 1).toUpperCase() + startRuleName.substring(1);
    }

    @Override
    protected void initRuntime() throws Exception {
        Path packageDir = Paths.get(packageBaseString, "src", antlrTestPackageName);
        Path runtimeBase = Paths.get(getRuntimePath("Go"), "antlr");
    }

    static class GoFileFilter implements FilenameFilter {

        public final static GoFileFilter Instance = new GoFileFilter();

        public boolean accept(File dir, String name) {
            return name.endsWith(".go");
        }
    }

    @Override
    protected CompiledState compile(RunOptions runOptions, GeneratedState generatedState) {
        List<GeneratedFile> generatedFiles = generatedState.generatedFiles;
        String tempDirPath = getTempDirPath();
        Path runtimeFiles = Paths.get(getRuntimePath("Go"), "antlr");
        File generatedFir = new File(tempDirPath, "parser");
        if (!generatedFir.mkdir()) {
            return new CompiledState(generatedState, new Exception("can't make dir " + generatedFir));
        }
        for (GeneratedFile generatedFile : generatedFiles) {
            try {
                Path originalFile = Paths.get(tempDirPath, generatedFile.name);
                Files.move(originalFile, Paths.get(tempDirPath, "parser", generatedFile.name));
            } catch (IOException e) {
                return new CompiledState(generatedState, e);
            }
        }
        ProcessorResult pr = null;
        try {
            pr = Processor.run(new String[] { getRuntimeToolPath(), "mod", "init", "test" }, tempDirPath, environment);
            pr = Processor.run(new String[] { getRuntimeToolPath(), "mod", "edit", "-replace=" + GoRuntimeImportPath + "=" + runtimeFiles.toString() }, tempDirPath, environment);
            pr = Processor.run(new String[] { getRuntimeToolPath(), "mod", "tidy" }, tempDirPath, environment);
        } catch (InterruptedException | IOException e) {
            System.out.println("Output:");
            System.out.println(pr.output);
            System.out.println("Errors:");
            System.out.println(pr.errors);
            throw new RuntimeException(e);
        }
        return new CompiledState(generatedState, null);
    }

    @Override
    public Map<String, String> getExecEnvironment() {
        return environment;
    }
}
