package org.graalvm.compiler.hotspot;

import static jdk.vm.ci.common.InitTimer.timer;
import static org.graalvm.compiler.hotspot.HotSpotGraalOptionValues.GRAAL_OPTION_PROPERTY_PREFIX;
import java.io.PrintStream;
import org.graalvm.compiler.debug.MethodFilter;
import org.graalvm.compiler.options.Option;
import org.graalvm.compiler.options.OptionKey;
import org.graalvm.compiler.options.OptionType;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.options.OptionsParser;
import org.graalvm.compiler.phases.tiers.CompilerConfiguration;
import jdk.vm.ci.common.InitTimer;
import jdk.vm.ci.hotspot.HotSpotJVMCICompilerFactory;
import jdk.vm.ci.hotspot.HotSpotJVMCIRuntime;
import jdk.vm.ci.hotspot.HotSpotSignature;
import jdk.vm.ci.runtime.JVMCIRuntime;

public final class HotSpotGraalCompilerFactory extends HotSpotJVMCICompilerFactory {

    private static MethodFilter[] graalCompileOnlyFilter;

    private static boolean compileGraalWithC1Only;

    private final HotSpotGraalJVMCIServiceLocator locator;

    HotSpotGraalCompilerFactory(HotSpotGraalJVMCIServiceLocator locator) {
        this.locator = locator;
    }

    @Override
    public String getCompilerName() {
        return "graal";
    }

    private OptionValues options;

    @Override
    public void onSelection() {
        JVMCIVersionCheck.check(false);
        assert options == null : "cannot select " + getClass() + " service more than once";
        options = HotSpotGraalOptionValues.HOTSPOT_OPTIONS;
        initializeGraalCompilePolicyFields(options);
        adjustCompilationLevelInternal(Object.class, "hashCode", "()I", CompilationLevel.FullOptimization);
        adjustCompilationLevelInternal(Object.class, "hashCode", "()I", CompilationLevel.Simple);
    }

    private static void initializeGraalCompilePolicyFields(OptionValues options) {
        compileGraalWithC1Only = Options.CompileGraalWithC1Only.getValue(options);
        String optionValue = Options.GraalCompileOnly.getValue(options);
        if (optionValue != null) {
            MethodFilter[] filter = MethodFilter.parse(optionValue);
            if (filter.length == 0) {
                filter = null;
            }
            graalCompileOnlyFilter = filter;
        }
    }

    @Override
    public void printProperties(PrintStream out) {
        out.println("[Graal properties]");
        options.printHelp(OptionsParser.getOptionsLoader(), out, GRAAL_OPTION_PROPERTY_PREFIX);
    }

    static class Options {

        @Option(help = "In tiered mode compile Graal and JVMCI using optimized first tier code.", type = OptionType.Expert)
        public static final OptionKey<Boolean> CompileGraalWithC1Only = new OptionKey<>(true);

        @Option(help = "A method filter selecting what should be compiled by Graal.  All other requests will be reduced to CompilationLevel.Simple.", type = OptionType.Expert)
        public static final OptionKey<String> GraalCompileOnly = new OptionKey<>(null);
    }

    @Override
    public HotSpotGraalCompiler createCompiler(JVMCIRuntime runtime) {
        HotSpotGraalCompiler compiler = createCompiler(runtime, options, CompilerConfigurationFactory.selectFactory(null, options));
        locator.onCompilerCreation(compiler);
        return compiler;
    }

    @SuppressWarnings("try")
    public static HotSpotGraalCompiler createCompiler(JVMCIRuntime runtime, OptionValues options, CompilerConfigurationFactory compilerConfigurationFactory) {
        HotSpotJVMCIRuntime jvmciRuntime = (HotSpotJVMCIRuntime) runtime;
        try (InitTimer t = timer("HotSpotGraalRuntime.<init>")) {
            HotSpotGraalRuntime graalRuntime = new HotSpotGraalRuntime(jvmciRuntime, compilerConfigurationFactory, options);
            return new HotSpotGraalCompiler(jvmciRuntime, graalRuntime, graalRuntime.getOptions());
        }
    }

    @Override
    public CompilationLevelAdjustment getCompilationLevelAdjustment() {
        if (graalCompileOnlyFilter != null) {
            return CompilationLevelAdjustment.ByFullSignature;
        }
        if (compileGraalWithC1Only) {
            return CompilationLevelAdjustment.ByHolder;
        }
        return CompilationLevelAdjustment.None;
    }

    @Override
    public CompilationLevel adjustCompilationLevel(Class<?> declaringClass, String name, String signature, boolean isOsr, CompilationLevel level) {
        return adjustCompilationLevelInternal(declaringClass, name, signature, level);
    }

    static {
        assert jdk.vm.ci.services.Services.class.getName().equals("jdk.vm.ci.services.Services");
        assert HotSpotGraalCompilerFactory.class.getName().equals("org.graalvm.compiler.hotspot.HotSpotGraalCompilerFactory");
    }

    private static CompilationLevel adjustCompilationLevelInternal(Class<?> declaringClass, String name, String signature, CompilationLevel level) {
        if (compileGraalWithC1Only) {
            if (level.ordinal() > CompilationLevel.Simple.ordinal()) {
                String declaringClassName = declaringClass.getName();
                if (declaringClassName.startsWith("jdk.vm.ci") || declaringClassName.startsWith("org.graalvm") || declaringClassName.startsWith("com.oracle.graal")) {
                    return CompilationLevel.Simple;
                }
            }
        }
        return checkGraalCompileOnlyFilter(declaringClass.getName(), name, signature, level);
    }

    public static CompilationLevel checkGraalCompileOnlyFilter(String declaringClassName, String name, String signature, CompilationLevel level) {
        if (graalCompileOnlyFilter != null) {
            if (level == CompilationLevel.FullOptimization) {
                HotSpotSignature sig = null;
                for (MethodFilter filter : graalCompileOnlyFilter) {
                    if (filter.hasSignature() && sig == null) {
                        sig = new HotSpotSignature(HotSpotJVMCIRuntime.runtime(), signature);
                    }
                    if (filter.matches(declaringClassName, name, sig)) {
                        return level;
                    }
                }
                return CompilationLevel.Simple;
            }
        }
        return level;
    }
}
