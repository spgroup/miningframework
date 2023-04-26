package org.graalvm.launcher;

import static java.lang.Integer.max;
import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.graalvm.nativeimage.RuntimeOptions;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.options.OptionCategory;
import org.graalvm.options.OptionDescriptor;
import org.graalvm.options.OptionDescriptors;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Instrument;
import org.graalvm.polyglot.Language;

public abstract class Launcher {

    private static final boolean STATIC_VERBOSE = Boolean.getBoolean("org.graalvm.launcher.verbose");

    static final boolean IS_AOT = Boolean.getBoolean("com.oracle.graalvm.isaot") || Boolean.getBoolean("com.oracle.truffle.aot");

    private Engine tempEngine;

<<<<<<< MINE
public
=======
>>>>>>> YOURS
 enum VMType {

        Native, JVM
    }

    final Native nativeAccess;

    private final boolean verbose;

    private boolean help;

    private boolean helpDebug;

    private boolean helpExpert;

    private boolean helpTools;

    private boolean helpLanguages;

    private boolean seenPolyglot;

    private VersionAction versionAction = VersionAction.None;

    protected enum VersionAction {

        None, PrintAndExit, PrintAndContinue
    }

    Launcher() {
        verbose = STATIC_VERBOSE || Boolean.valueOf(System.getenv("VERBOSE_GRAALVM_LAUNCHERS"));
        if (IS_AOT) {
            nativeAccess = new Native();
        } else {
            nativeAccess = null;
        }
    }

    final boolean isPolyglot() {
        return seenPolyglot;
    }

    final void setPolyglot(boolean polyglot) {
        seenPolyglot = polyglot;
    }

    private Engine getTempEngine() {
        if (tempEngine == null) {
            tempEngine = Engine.create();
        }
        return tempEngine;
    }

    static void handleAbortException(AbortException e) {
        if (e.getMessage() != null) {
            System.err.println("ERROR: " + e.getMessage());
        }
        if (e.getCause() != null) {
            e.printStackTrace();
        }
        System.exit(e.getExitCode());
    }

    protected void setVersionAction(VersionAction versionAction) {
        this.versionAction = versionAction;
    }

    protected static class AbortException extends RuntimeException {

        static final long serialVersionUID = 4681646279864737876L;

        private final int exitCode;

        AbortException(String message, int exitCode) {
            super(message);
            this.exitCode = exitCode;
        }

        AbortException(Throwable cause, int exitCode) {
            super(cause);
            this.exitCode = exitCode;
        }

        int getExitCode() {
            return exitCode;
        }

        @SuppressWarnings("sync-override")
        @Override
        public final Throwable fillInStackTrace() {
            return null;
        }
    }

    protected final AbortException exit() {
        return exit(0);
    }

    protected final AbortException exit(int exitCode) {
        return abort((String) null, exitCode);
    }

    protected final AbortException abort(String message) {
        return abort(message, 1);
    }

    @SuppressWarnings("static-method")
    protected final AbortException abort(String message, int exitCode) {
        throw new AbortException(message, exitCode);
    }

    protected final AbortException abort(Throwable t) {
        return abort(t, 255);
    }

    protected final AbortException abort(Throwable t, int exitCode) {
        if (t.getCause() instanceof IOException && t.getClass() == RuntimeException.class) {
            String message = t.getMessage();
            if (message != null && !message.startsWith(t.getCause().getClass().getName() + ": ")) {
                System.err.println(message);
            }
            throw abort((IOException) t.getCause(), exitCode);
        }
        throw new AbortException(t, exitCode);
    }

    protected final AbortException abort(IOException e) {
        return abort(e, 74);
    }

    protected final AbortException abort(IOException e, int exitCode) {
        String message = e.getMessage();
        if (message != null) {
            if (e instanceof NoSuchFileException) {
                throw abort("Not such file: " + message, exitCode);
            } else if (e instanceof AccessDeniedException) {
                throw abort("Access denied: " + message, exitCode);
            } else {
                throw abort(message + " (" + e.getClass().getSimpleName() + ")", exitCode);
            }
        }
        throw abort((Throwable) e, exitCode);
    }

    protected final AbortException abortInvalidArgument(String argument, String message) {
        return abortInvalidArgument(argument, message, 2);
    }

    protected final AbortException abortInvalidArgument(String argument, String message, int exitCode) {
        Set<String> allArguments = collectAllArguments();
        int equalIndex;
        String testString = argument;
        if ((equalIndex = argument.indexOf('=')) != -1) {
            testString = argument.substring(0, equalIndex);
        }
        List<String> matches = fuzzyMatch(allArguments, testString, 0.7f);
        if (matches.isEmpty()) {
            matches = fuzzyMatch(allArguments, testString, 0.5f);
        }
        StringBuilder sb = new StringBuilder();
        if (message != null) {
            sb.append(message);
        }
        if (!matches.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(System.lineSeparator());
            }
            sb.append("Did you mean one of the following arguments?").append(System.lineSeparator());
            Iterator<String> iterator = matches.iterator();
            while (true) {
                String match = iterator.next();
                sb.append("  ").append(match);
                if (iterator.hasNext()) {
                    sb.append(System.lineSeparator());
                } else {
                    break;
                }
            }
        }
        if (sb.length() > 0) {
            throw abort(sb.toString(), exitCode);
        } else {
            throw exit(exitCode);
        }
    }

    protected abstract void printHelp(OptionCategory maxCategory);

    protected abstract void printVersion();

    protected abstract void collectArguments(Set<String> options);

    private String executableName(String basename) {
        switch(OS.current) {
            case Linux:
            case Darwin:
            case Solaris:
                return basename;
            default:
                throw abort("executableName: OS not supported: " + OS.current);
        }
    }

    protected static void printPolyglotVersions() {
        Engine engine = Engine.create();
        System.out.println("GraalVM Polyglot Engine Version " + engine.getVersion());
        printLanguages(engine, true);
        printInstruments(engine, true);
    }

    private static final Path FORCE_GRAAL_HOME;

    static {
        String forcedHome = System.getProperty("org.graalvm.launcher.home");
        if (forcedHome != null) {
            FORCE_GRAAL_HOME = Paths.get(forcedHome);
        } else {
            FORCE_GRAAL_HOME = null;
        }
    }

    protected String getMainClass() {
        return this.getClass().getName();
    }

    protected static boolean isAOT() {
        return IS_AOT;
    }

    private boolean isVerbose() {
        return verbose;
    }

    @SuppressWarnings("fallthrough")
    final boolean runPolyglotAction() {
        OptionCategory helpCategory = helpDebug ? OptionCategory.DEBUG : (helpExpert ? OptionCategory.EXPERT : OptionCategory.USER);
        switch(versionAction) {
            case PrintAndContinue:
                printVersion();
            case None:
                break;
            case PrintAndExit:
                printVersion();
                return true;
        }
        boolean printDefaultHelp = help || ((helpExpert || helpDebug) && !helpTools && !helpLanguages);
        if (printDefaultHelp) {
            printHelp(helpCategory);
            System.out.println();
            System.out.println("Runtime Options:");
            printOption("--polyglot", "Run with all other guest languages accessible.");
            printOption("--native", "Run using the native launcher with limited Java access (default).");
            printOption("--native.[option]", "Pass options to the native image; for example, '--native.Xmx1G'. To see available options, use '--native.help'.");
            printOption("--jvm", "Run on the Java Virtual Machine with Java access.");
            printOption("--jvm.[option]", "Pass options to the JVM; for example, '--jvm.classpath=myapp.jar'. To see available options. use '--jvm.help'.");
            printOption("--help", "Print this help message.");
            printOption("--help:languages", "Print options for all installed languages.");
            printOption("--help:tools", "Print options for all installed tools.");
            printOption("--help:expert", "Print additional options for experts.");
            if (helpExpert || helpDebug) {
                printOption("--help:debug", "Print additional options for debugging.");
            }
            printOption("--version", "Print version information and exit.");
            printOption("--show-version", "Print version information and continue execution.");
            List<PrintableOption> engineOptions = new ArrayList<>();
            for (OptionDescriptor descriptor : getTempEngine().getOptions()) {
                if (!descriptor.getName().startsWith("engine.") && !descriptor.getName().startsWith("compiler.")) {
                    continue;
                }
                if (descriptor.getCategory().ordinal() == helpCategory.ordinal()) {
                    engineOptions.add(asPrintableOption(descriptor));
                }
            }
            if (!engineOptions.isEmpty()) {
                printOptions(engineOptions, "Engine options:", 2);
            }
        }
        if (helpLanguages) {
            printLanguageOptions(getTempEngine(), helpCategory);
        }
        if (helpTools) {
            printInstrumentOptions(getTempEngine(), helpCategory);
        }
        if (printDefaultHelp || helpLanguages || helpTools) {
            System.out.println("\nSee http://www.graalvm.org for more information.");
            return true;
        }
        return false;
    }

    private static void printInstrumentOptions(Engine engine, OptionCategory optionCategory) {
        Map<Instrument, List<PrintableOption>> instrumentsOptions = new HashMap<>();
        List<Instrument> instruments = sortedInstruments(engine);
        for (Instrument instrument : instruments) {
            List<PrintableOption> options = new ArrayList<>();
            for (OptionDescriptor descriptor : instrument.getOptions()) {
                if (descriptor.getCategory().ordinal() == optionCategory.ordinal()) {
                    options.add(asPrintableOption(descriptor));
                }
            }
            if (!options.isEmpty()) {
                instrumentsOptions.put(instrument, options);
            }
        }
        if (!instrumentsOptions.isEmpty()) {
            System.out.println();
            System.out.println("Tool options:");
            for (Instrument instrument : instruments) {
                List<PrintableOption> options = instrumentsOptions.get(instrument);
                if (options != null) {
                    printOptions(options, "  " + instrument.getName() + ":", 4);
                }
            }
        }
    }

    private static void printLanguageOptions(Engine engine, OptionCategory optionCategory) {
        Map<Language, List<PrintableOption>> languagesOptions = new HashMap<>();
        List<Language> languages = sortedLanguages(engine);
        for (Language language : languages) {
            List<PrintableOption> options = new ArrayList<>();
            for (OptionDescriptor descriptor : language.getOptions()) {
                if (descriptor.getCategory().ordinal() == optionCategory.ordinal()) {
                    options.add(asPrintableOption(descriptor));
                }
            }
            if (!options.isEmpty()) {
                languagesOptions.put(language, options);
            }
        }
        if (!languagesOptions.isEmpty()) {
            System.out.println();
            System.out.println("Language Options:");
            for (Language language : languages) {
                List<PrintableOption> options = languagesOptions.get(language);
                if (options != null) {
                    printOptions(options, "  " + language.getName() + ":", 4);
                }
            }
        }
    }

    boolean parsePolyglotOption(String defaultOptionPrefix, Map<String, String> options, String arg) {
        switch(arg) {
            case "--help":
                help = true;
                return true;
            case "--help:debug":
                helpDebug = true;
                return true;
            case "--help:expert":
                helpExpert = true;
                return true;
            case "--help:tools":
                helpTools = true;
                return true;
            case "--help:languages":
                helpLanguages = true;
                return true;
            case "--version":
                versionAction = VersionAction.PrintAndExit;
                return true;
            case "--show-version":
                versionAction = VersionAction.PrintAndContinue;
                return true;
            case "--polyglot":
                seenPolyglot = true;
                return true;
            default:
                if ((arg.startsWith("--jvm.") && arg.length() > "--jvm.".length()) || arg.equals("--jvm")) {
                    if (isAOT()) {
                        throw abort("should not reach here: jvm option failed to switch to JVM");
                    }
                    return true;
                } else if ((arg.startsWith("--native.") && arg.length() > "--native.".length()) || arg.equals("--native")) {
                    if (!isAOT()) {
                        throw abort("native options are not supported on the JVM");
                    }
                    return true;
                }
                if (arg.length() <= 2 || !arg.startsWith("--")) {
                    return false;
                }
                int eqIdx = arg.indexOf('=');
                String key;
                String value;
                if (eqIdx < 0) {
                    key = arg.substring(2);
                    value = null;
                } else {
                    key = arg.substring(2, eqIdx);
                    value = arg.substring(eqIdx + 1);
                }
                if (value == null) {
                    value = "true";
                }
                int index = key.indexOf('.');
                String group = key;
                if (index >= 0) {
                    group = group.substring(0, index);
                }
                OptionDescriptor descriptor = findPolyglotOptionDescriptor(group, key);
                if (descriptor == null) {
                    if (defaultOptionPrefix != null) {
                        descriptor = findPolyglotOptionDescriptor(defaultOptionPrefix, defaultOptionPrefix + "." + key);
                    }
                    if (descriptor == null) {
                        return false;
                    }
                }
                try {
                    descriptor.getKey().getType().convert(value);
                } catch (IllegalArgumentException e) {
                    throw abort(String.format("Invalid argument %s specified. %s'", arg, e.getMessage()));
                }
                options.put(key, value);
                return true;
        }
    }

    private OptionDescriptor findPolyglotOptionDescriptor(String group, String key) {
        OptionDescriptors descriptors = null;
        switch(group) {
            case "compiler":
            case "engine":
                descriptors = getTempEngine().getOptions();
                break;
            default:
                Engine engine = getTempEngine();
                if (engine.getLanguages().containsKey(group)) {
                    descriptors = engine.getLanguages().get(group).getOptions();
                } else if (engine.getInstruments().containsKey(group)) {
                    descriptors = engine.getInstruments().get(group).getOptions();
                }
                break;
        }
        if (descriptors == null) {
            return null;
        }
        return descriptors.get(key);
    }

    private Set<String> collectAllArguments() {
        Engine engine = getTempEngine();
        Set<String> options = new LinkedHashSet<>();
        collectArguments(options);
        options.add("--polylgot");
        options.add("--native");
        options.add("--jvm");
        options.add("--help");
        options.add("--help:languages");
        options.add("--help:tools");
        options.add("--help:expert");
        options.add("--version");
        options.add("--show-version");
        if (helpExpert || helpDebug) {
            options.add("--help:debug");
        }
        addOptions(engine.getOptions(), options);
        for (Language language : engine.getLanguages().values()) {
            addOptions(language.getOptions(), options);
        }
        for (Instrument instrument : engine.getInstruments().values()) {
            addOptions(instrument.getOptions(), options);
        }
        return options;
    }

    private static void addOptions(OptionDescriptors descriptors, Set<String> target) {
        for (OptionDescriptor descriptor : descriptors) {
            target.add("--" + descriptor.getName());
        }
    }

    private static List<String> fuzzyMatch(Set<String> arguments, String argument, float threshold) {
        List<String> matches = new ArrayList<>();
        for (String arg : arguments) {
            float score = stringSimilarity(arg, argument);
            if (score >= threshold) {
                matches.add(arg);
            }
        }
        return matches;
    }

    private static float stringSimilarity(String str1, String str2) {
        int hit = 0;
        for (int i = 0; i < str1.length() - 1; ++i) {
            for (int j = 0; j < str2.length() - 1; ++j) {
                if ((str1.charAt(i) == str2.charAt(j)) && (str1.charAt(i + 1) == str2.charAt(j + 1))) {
                    ++hit;
                    break;
                }
            }
        }
        return 2.0f * hit / (str1.length() + str2.length());
    }

    static List<Language> sortedLanguages(Engine engine) {
        List<Language> languages = new ArrayList<>(engine.getLanguages().values());
        languages.sort(Comparator.comparing(Language::getId));
        return languages;
    }

    static List<Instrument> sortedInstruments(Engine engine) {
        List<Instrument> instruments = new ArrayList<>();
        for (Instrument instrument : engine.getInstruments().values()) {
            if (!instrument.getOptions().iterator().hasNext()) {
                continue;
            }
            instruments.add(instrument);
        }
        instruments.sort(Comparator.comparing(Instrument::getId));
        return instruments;
    }

    static void printOption(OptionCategory optionCategory, OptionDescriptor descriptor) {
        if (descriptor.getCategory().ordinal() == optionCategory.ordinal()) {
            printOption(asPrintableOption(descriptor));
        }
    }

    private static PrintableOption asPrintableOption(OptionDescriptor descriptor) {
        StringBuilder key = new StringBuilder("--");
        key.append(descriptor.getName());
        Object defaultValue = descriptor.getKey().getDefaultValue();
        if (defaultValue instanceof Boolean && defaultValue == Boolean.FALSE) {
        } else {
            key.append("=<");
            key.append(descriptor.getKey().getType().getName());
            key.append(">");
        }
        return new PrintableOption(key.toString(), descriptor.getHelp());
    }

    static void printOption(String option, String description) {
        printOption(option, description, 2);
    }

    private static void printOption(String option, String description, int indentation) {
        StringBuilder indent = new StringBuilder(indentation);
        for (int i = 0; i < indentation; i++) {
            indent.append(' ');
        }
        String desc = description != null ? description : "";
        String[] descLines = desc.split(System.lineSeparator());
        if (option.length() >= 45 && description != null) {
            System.out.println(String.format("%s%s%n%s%-45s%s", indent, option, indent, "", descLines[0]));
        } else {
            System.out.println(String.format("%s%-45s%s", indent, option, descLines[0]));
        }
        for (int i = 1; i < descLines.length; i++) {
            System.out.println(String.format("%s%-45s%s", indent, "", descLines[i]));
        }
    }

    private static void printOption(PrintableOption option) {
        printOption(option, 2);
    }

    private static void printOption(PrintableOption option, int indentation) {
        printOption(option.option, option.description, indentation);
    }

    private static final class PrintableOption implements Comparable<PrintableOption> {

        final String option;

        final String description;

        private PrintableOption(String option, String description) {
            this.option = option;
            this.description = description;
        }

        @Override
        public int compareTo(PrintableOption o) {
            return this.option.compareTo(o.option);
        }
    }

    private static void printOptions(List<PrintableOption> options, String title, int indentation) {
        Collections.sort(options);
        System.out.println(title);
        for (PrintableOption option : options) {
            printOption(option, indentation);
        }
    }

    enum OS {

        Darwin, Linux, Solaris;

        private static OS findCurrent() {
            final String name = System.getProperty("os.name");
            if (name.equals("Linux")) {
                return Linux;
            }
            if (name.equals("SunOS")) {
                return Solaris;
            }
            if (name.equals("Mac OS X") || name.equals("Darwin")) {
                return Darwin;
            }
            throw new IllegalArgumentException("unknown OS: " + name);
        }

        private static final OS current = findCurrent();

        public static OS getCurrent() {
            return current;
        }
    }

    private static void serializePolyglotOptions(Map<String, String> polyglotOptions, List<String> args) {
        if (polyglotOptions == null) {
            return;
        }
        for (Entry<String, String> entry : polyglotOptions.entrySet()) {
            args.add("--" + entry.getKey() + '=' + entry.getValue());
        }
    }

    private static void printLanguages(Engine engine, boolean printWhenEmpty) {
        if (engine.getLanguages().isEmpty()) {
            if (printWhenEmpty) {
                System.out.println("  Installed Languages: none");
            }
        } else {
            System.out.println("  Installed Languages:");
            List<Language> languages = new ArrayList<>(engine.getLanguages().size());
            int nameLength = 0;
            for (Language language : engine.getLanguages().values()) {
                languages.add(language);
                nameLength = max(nameLength, language.getName().length());
            }
            languages.sort(Comparator.comparing(Language::getId));
            String langFormat = "    %-" + nameLength + "s%s version %s%n";
            for (Language language : languages) {
                String host;
                host = "";
                String version = language.getVersion();
                if (version == null || version.length() == 0) {
                    version = "";
                }
                System.out.printf(langFormat, language.getName().isEmpty() ? "Unnamed" : language.getName(), host, version);
            }
        }
    }

    private static void printInstruments(Engine engine, boolean printWhenEmpty) {
        if (engine.getInstruments().isEmpty()) {
            if (printWhenEmpty) {
                System.out.println("  Installed Tools: none");
            }
        } else {
            System.out.println("  Installed Tools:");
            List<Instrument> instruments = sortedInstruments(engine);
            int nameLength = 0;
            for (Instrument instrument : instruments) {
                nameLength = max(nameLength, instrument.getName().length());
            }
            String instrumentFormat = "    %-" + nameLength + "s version %s%n";
            for (Instrument instrument : instruments) {
                String version = instrument.getVersion();
                if (version == null || version.length() == 0) {
                    version = "";
                }
                System.out.printf(instrumentFormat, instrument.getName().isEmpty() ? instrument.getId() : instrument.getName(), version);
            }
        }
    }

    private static final String CLASSPATH = System.getProperty("org.graalvm.launcher.classpath");

    private static final String ALT_GRAALVM_VERSION_PROPERTY = "graalvm.version";

    private static final String GRAALVM_VERSION_PROPERTY = "org.graalvm.version";

    private static final String GRAALVM_VERSION;

    static {
        String version = System.getProperty(GRAALVM_VERSION_PROPERTY);
        String altVersion = System.getProperty(ALT_GRAALVM_VERSION_PROPERTY);
        if (version != null && altVersion == null) {
            GRAALVM_VERSION = version;
        } else if (altVersion != null && version == null) {
            GRAALVM_VERSION = altVersion;
        } else if (version != null && version.equals(altVersion)) {
            GRAALVM_VERSION = version;
        } else if (isAOT()) {
            throw new RuntimeException("Could not find GraalVM version: " + GRAALVM_VERSION_PROPERTY + "='" + version + "' " + ALT_GRAALVM_VERSION_PROPERTY + "='" + altVersion + "'");
        } else {
            GRAALVM_VERSION = null;
        }
    }

    class Native {

        void maybeExec(List<String> args, boolean isPolyglot, Map<String, String> polyglotOptions, VMType defaultVmType) {
            assert isAOT();
            VMType vmType = null;
            boolean polyglot = false;
            List<String> jvmArgs = new ArrayList<>();
            List<String> remainingArgs = new ArrayList<>(args.size());
            Iterator<Entry<String, String>> polyglotOptionsIterator = polyglotOptions.entrySet().iterator();
            while (polyglotOptionsIterator.hasNext()) {
                Map.Entry<String, String> entry = polyglotOptionsIterator.next();
                if (entry.getKey().startsWith("jvm.")) {
                    jvmArgs.add('-' + entry.getKey().substring(4));
                    if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                        jvmArgs.add(entry.getValue());
                    }
                    vmType = VMType.JVM;
                    polyglotOptionsIterator.remove();
                }
            }
            Iterator<String> iterator = args.iterator();
            while (iterator.hasNext()) {
                String arg = iterator.next();
                if ((arg.startsWith("--jvm.") && arg.length() > "--jvm.".length()) || arg.equals("--jvm")) {
                    if (vmType == VMType.Native) {
                        throw abort("`--jvm` and `--native` options can not be used together.");
                    }
                    if (arg.equals("--jvm.help")) {
                        printJvmHelp();
                        throw exit();
                    }
                    vmType = VMType.JVM;
                    if (arg.startsWith("--jvm.")) {
                        String jvmArg = arg.substring("--jvm.".length());
                        if (jvmArg.equals("classpath")) {
                            throw abort("--jvm.classpath argument must be of the form --jvm.classpath=<classpath>, not two separate arguments");
                        }
                        if (jvmArg.equals("cp")) {
                            throw abort("--jvm.cp argument must be of the form --jvm.cp=<classpath>, not two separate arguments");
                        }
                        if (jvmArg.startsWith("classpath=") || jvmArg.startsWith("cp=")) {
                            int eqIndex = jvmArg.indexOf('=');
                            jvmArgs.add('-' + jvmArg.substring(0, eqIndex));
                            jvmArgs.add(jvmArg.substring(eqIndex + 1));
                        } else {
                            jvmArgs.add('-' + jvmArg);
                        }
                    }
                    iterator.remove();
                } else if ((arg.startsWith("--native.") && arg.length() > "--native.".length()) || arg.equals("--native")) {
                    if (vmType == VMType.JVM) {
                        throw abort("`--jvm` and `--native` options can not be used together.");
                    }
                    vmType = VMType.Native;
                    if (arg.equals("--native.help")) {
                        printNativeHelp();
                        throw exit();
                    }
                    if (arg.startsWith("--native.")) {
                        int eqIdx = arg.indexOf('=');
                        String key;
                        String value;
                        if (eqIdx < 0) {
                            key = arg.substring("--native.".length());
                            value = null;
                        } else {
                            key = arg.substring("--native.".length(), eqIdx);
                            value = arg.substring(eqIdx + 1);
                        }
                        if (value == null) {
                            value = "true";
                        }
                        OptionDescriptor descriptor = RuntimeOptions.getOptions().get(key);
                        if (descriptor == null) {
                            throw abort("Unknown native option: " + key);
                        }
                        RuntimeOptions.set(key, descriptor.getKey().getType().convert(value));
                    }
                    iterator.remove();
                } else if (arg.equals("--polyglot")) {
                    polyglot = true;
                } else {
                    remainingArgs.add(arg);
                }
            }
            if (vmType == null) {
                vmType = defaultVmType;
            }
            if (vmType == VMType.JVM) {
                if (!isPolyglot && polyglot) {
                    remainingArgs.add(0, "--polyglot");
                }
                execJVM(jvmArgs, remainingArgs, polyglotOptions);
            } else if (!isPolyglot && polyglot) {
                assert jvmArgs.isEmpty();
                execNativePolyglot(remainingArgs, polyglotOptions);
            }
        }

        private void printJvmHelp() {
            System.out.print("JVM options:");
            printOption("--jvm.classpath <...>", "A " + File.pathSeparator + " separated list of classpath entries that will be added to the JVM's classpath");
            printOption("--jvm.D<name>=<value>", "Set a system property");
            printOption("--jvm.esa", "Enable system assertions");
            printOption("--jvm.ea[:<packagename>...|:<classname>]", "Enable assertions with specified granularity");
            printOption("--jvm.agentlib:<libname>[=<options>]", "Load native agent library <libname>");
            printOption("--jvm.agentpath:<pathname>[=<options>]", "Load native agent library by full pathname");
            printOption("--jvm.javaagent:<jarpath>[=<options>]", "Load Java programming language agent");
            printOption("--jvm.Xbootclasspath/a:<...>", "A " + File.pathSeparator + " separated list of classpath entries that will be added to the JVM's boot classpath");
            printOption("--jvm.Xmx<size>", "Set maximum Java heap size");
            printOption("--jvm.Xms<size>", "Set initial Java heap size");
            printOption("--jvm.Xss<size>", "Set java thread stack size");
        }

        private void printNativeHelp() {
            System.out.println("Native VM options:");
            OptionDescriptors options = RuntimeOptions.getOptions();
            SortedMap<String, OptionDescriptor> sortedOptions = new TreeMap<>();
            for (OptionDescriptor descriptor : options) {
                sortedOptions.put(descriptor.getName(), descriptor);
            }
            for (Entry<String, OptionDescriptor> entry : sortedOptions.entrySet()) {
                printOption("--native." + entry.getKey() + "=<value>", entry.getValue().getHelp());
            }
        }

        private void execNativePolyglot(List<String> args, Map<String, String> polyglotOptions) {
            List<String> command = new ArrayList<>(args.size() + (polyglotOptions == null ? 0 : polyglotOptions.size()) + 2);
            Path executable = getGraalVMBinaryPath("polyglot");
            serializePolyglotOptions(polyglotOptions, command);
            command.add("--use-launcher");
            command.add(getMainClass());
            command.addAll(args);
            exec(executable, command);
        }

        private void execJVM(List<String> jvmArgs, List<String> args, Map<String, String> polyglotOptions) {
            List<String> command = new ArrayList<>(jvmArgs.size() + args.size() + (polyglotOptions == null ? 0 : polyglotOptions.size()) + 4);
            Path executable = getGraalVMBinaryPath("java");
            String classpath = getClasspath(jvmArgs);
            if (classpath != null) {
                command.add("-classpath");
                command.add(classpath);
            }
            command.addAll(jvmArgs);
            command.add(getMainClass());
            serializePolyglotOptions(polyglotOptions, command);
            command.addAll(args);
            exec(executable, command);
        }

        private String getClasspath(List<String> jvmArgs) {
            assert isAOT();
            assert CLASSPATH != null;
            StringBuilder sb = new StringBuilder();
            Path graalVMHome = getGraalVMHome();
            for (String entry : CLASSPATH.split(File.pathSeparator)) {
                Path resolved = graalVMHome.resolve(entry);
                if (isVerbose() && !Files.exists(resolved)) {
                    System.err.println(String.format("Warning: %s does not exit", resolved));
                }
                sb.append(resolved);
                sb.append(File.pathSeparatorChar);
            }
            String classpathFromArgs = null;
            Iterator<String> iterator = jvmArgs.iterator();
            while (iterator.hasNext()) {
                String jvmArg = iterator.next();
                if (jvmArg.equals("-cp") || jvmArg.equals("-classpath")) {
                    if (iterator.hasNext()) {
                        iterator.remove();
                        classpathFromArgs = iterator.next();
                        iterator.remove();
                    }
                }
                if (jvmArg.startsWith("-Djava.class.path=")) {
                    iterator.remove();
                    classpathFromArgs = jvmArg.substring("-Djava.class.path=".length());
                }
            }
            if (classpathFromArgs != null) {
                sb.append(classpathFromArgs);
                sb.append(File.pathSeparatorChar);
            }
            if (sb.length() == 0) {
                return null;
            }
            return sb.substring(0, sb.length() - 1);
        }

        void setGraalVMProperties() {
            assert GRAALVM_VERSION != null;
            System.setProperty(GRAALVM_VERSION_PROPERTY, GRAALVM_VERSION);
            System.setProperty(ALT_GRAALVM_VERSION_PROPERTY, GRAALVM_VERSION);
            String home = getGraalVMHome().toString();
            System.setProperty("graalvm.home", home);
            System.setProperty("org.graalvm.home", home);
        }

        private Path getGraalVMBinaryPath(String binaryName) {
            String executableName = executableName(binaryName);
            Path siblingBinary = getCurrentExecutablePath().resolveSibling(executableName);
            if (Files.exists(siblingBinary)) {
                return siblingBinary;
            }
            Path graalVMHome = getGraalVMHome();
            Path jdkBin = graalVMHome.resolve("bin").resolve(executableName);
            if (Files.exists(jdkBin)) {
                return jdkBin;
            }
            return graalVMHome.resolve("jre").resolve("bin").resolve(executableName);
        }

        private Path getCurrentExecutablePath() {
            return Paths.get((String) Compiler.command(new String[] { "com.oracle.svm.core.posix.GetExecutableName" }));
        }

        Path getGraalVMHome() {
            if (FORCE_GRAAL_HOME != null) {
                return FORCE_GRAAL_HOME;
            }
            assert isAOT();
            Path executable = getCurrentExecutablePath();
            Path bin = executable.getParent();
            assert bin.getFileName().toString().equals("bin");
            Path jreOrJdk = bin.getParent();
            Path home;
            if (jreOrJdk.getFileName().toString().equals("jre")) {
                home = jreOrJdk.getParent();
            } else {
                home = jreOrJdk;
            }
            return home;
        }

        private void exec(Path executable, List<String> command) {
            assert isAOT();
            if (isVerbose()) {
                System.out.println(String.format("exec(%s, %s)", executable, command));
            }
            String[] argv = new String[command.size() + 1];
            int i = 0;
            argv[i++] = executable.getFileName().toString();
            for (String arg : command) {
                argv[i++] = arg;
            }
            if (execv(executable.toString(), argv) != 0) {
                int errno = NativeInterface.errno();
                throw abort(String.format("exec(%s, %s) failed: %s", executable, command, CTypeConversion.toJavaString(NativeInterface.strerror(errno))));
            }
        }

        private int execv(String executable, String[] argv) {
            try (CTypeConversion.CCharPointerHolder pathHolder = CTypeConversion.toCString(executable);
                CTypeConversion.CCharPointerPointerHolder argvHolder = CTypeConversion.toCStrings(argv)) {
                return NativeInterface.execv(pathHolder.get(), argvHolder.get());
            }
        }
    }
}