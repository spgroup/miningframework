package com.oracle.svm.core;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.graalvm.compiler.word.Word;
import org.graalvm.nativeimage.Feature;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.Platform;
import org.graalvm.nativeimage.Platforms;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CCharPointerPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;
import org.graalvm.nativeimage.c.type.CTypeConversion.CCharPointerHolder;
import org.graalvm.word.Pointer;
import org.graalvm.word.UnsignedWord;
import org.graalvm.word.WordFactory;
import com.oracle.svm.core.amd64.AMD64CPUFeatureAccess;
import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.c.function.CEntryPointOptions;
import com.oracle.svm.core.c.function.CEntryPointSetup.EnterCreateIsolatePrologue;
import com.oracle.svm.core.jdk.RuntimeFeature;
import com.oracle.svm.core.jdk.RuntimeSupport;
import com.oracle.svm.core.option.RuntimeOptionParser;
import com.oracle.svm.core.thread.JavaThreads;
import com.oracle.svm.core.util.Counter;
import com.oracle.svm.core.util.VMError;
import jdk.vm.ci.code.Architecture;

public class JavaMainWrapper {

    private static int argc;

    private static CCharPointerPointer argv;

    static {
        Word.ensureInitialized();
    }

    private static UnsignedWord argvLength = WordFactory.zero();

    private static String[] mainArgs;

    public static class JavaMainSupport {

        final MethodHandle javaMainHandle;

        final String javaMainClassName;

        @Platforms(Platform.HOSTED_ONLY.class)
        public JavaMainSupport(Method javaMainMethod) throws IllegalAccessException {
            this.javaMainHandle = MethodHandles.lookup().unreflect(javaMainMethod);
            this.javaMainClassName = javaMainMethod.getDeclaringClass().getName();
        }

        public String getJavaCommand() {
            if (mainArgs != null) {
                StringBuilder commandLine = new StringBuilder(javaMainClassName);
                for (String arg : mainArgs) {
                    commandLine.append(' ');
                    commandLine.append(arg);
                }
                return commandLine.toString();
            }
            return null;
        }

        public List<String> getInputArguments() {
            if (argv.isNonNull() && argc > 0) {
                String[] unmodifiedArgs = SubstrateUtil.getArgs(argc, argv);
                List<String> inputArgs = new ArrayList<>(Arrays.asList(unmodifiedArgs));
                if (mainArgs != null) {
                    inputArgs.removeAll(Arrays.asList(mainArgs));
                }
                return Collections.unmodifiableList(inputArgs);
            }
            return Collections.emptyList();
        }
    }

    private static final Thread preallocatedThread;

    static {
        preallocatedThread = new Thread("main");
        preallocatedThread.setDaemon(false);
    }

    @CEntryPoint
    @CEntryPointOptions(prologue = EnterCreateIsolatePrologue.class, include = CEntryPointOptions.NotIncludedAutomatically.class)
    public static int run(int paramArgc, CCharPointerPointer paramArgv) throws Exception {
        JavaThreads.singleton().assignJavaThread(preallocatedThread, true);
        JavaMainWrapper.argc = paramArgc;
        JavaMainWrapper.argv = paramArgv;
        Architecture imageArchitecture = ImageSingletons.lookup(SubstrateTargetDescription.class).arch;
        AMD64CPUFeatureAccess.verifyHostSupportsArchitecture(imageArchitecture);
        String[] args = SubstrateUtil.getArgs(paramArgc, paramArgv);
        args = RuntimeOptionParser.parseAndConsumeAllOptions(args);
        mainArgs = args;
        int exitCode;
        try {
            if (SubstrateOptions.ParseRuntimeOptions.getValue()) {
                RuntimeSupport.getRuntimeSupport().executeStartupHooks();
            }
            ImageSingletons.lookup(JavaMainSupport.class).javaMainHandle.invokeExact(args);
            exitCode = 0;
        } catch (Throwable ex) {
            JavaThreads.dispatchUncaughtException(Thread.currentThread(), ex);
            exitCode = 1;
        } finally {
            JavaThreads.singleton().joinAllNonDaemons();
            RuntimeSupport.getRuntimeSupport().shutdown();
            Counter.logValues();
        }
        return exitCode;
    }

    public static long getCRuntimeArgumentBlockLength() {
        VMError.guarantee(argv.notEqual(WordFactory.zero()) && argc > 0, "Requires JavaMainWrapper.run(int, CCharPointerPointer) entry point!");
        CCharPointer firstArgPos = argv.read(0);
        if (argvLength.equal(WordFactory.zero())) {
            CCharPointer lastArgPos = argv.read(argc - 1);
            UnsignedWord lastArgLength = SubstrateUtil.strlen(lastArgPos);
            argvLength = WordFactory.unsigned(lastArgPos.rawValue()).add(lastArgLength).subtract(WordFactory.unsigned(firstArgPos.rawValue()));
        }
        return argvLength.rawValue();
    }

    public static boolean setCRuntimeArgument0(String arg0) {
        boolean arg0truncation = false;
        try (CCharPointerHolder arg0Pin = CTypeConversion.toCString(arg0)) {
            CCharPointer arg0Pointer = arg0Pin.get();
            UnsignedWord arg0Length = SubstrateUtil.strlen(arg0Pointer);
            UnsignedWord origLength = WordFactory.unsigned(getCRuntimeArgumentBlockLength());
            UnsignedWord newArgLength = origLength;
            if (arg0Length.add(1).belowThan(origLength)) {
                newArgLength = arg0Length.add(1);
            }
            arg0truncation = arg0Length.aboveThan(origLength);
            CCharPointer firstArgPos = argv.read(0);
            MemoryUtil.copyConjointMemoryAtomic(WordFactory.pointer(arg0Pointer.rawValue()), WordFactory.pointer(firstArgPos.rawValue()), newArgLength);
            MemoryUtil.fillToMemoryAtomic((Pointer) WordFactory.unsigned(firstArgPos.rawValue()).add(newArgLength), origLength.subtract(newArgLength), (byte) 0);
        }
        return arg0truncation;
    }

    @AutomaticFeature
    public static class ExposeCRuntimeArgumentBlockFeature implements Feature {

        @Override
        public List<Class<? extends Feature>> getRequiredFeatures() {
            return Arrays.asList(RuntimeFeature.class);
        }

        @Override
        public void afterRegistration(AfterRegistrationAccess access) {
            RuntimeSupport rs = RuntimeSupport.getRuntimeSupport();
            rs.addCommandPlugin(new GetCRuntimeArgumentBlockLengthCommand());
            rs.addCommandPlugin(new SetCRuntimeArgument0Command());
        }
    }

    private static class GetCRuntimeArgumentBlockLengthCommand implements CompilerCommandPlugin {

        @Override
        public String name() {
            return "com.oracle.svm.core.JavaMainWrapper.getCRuntimeArgumentBlockLength()long";
        }

        @Override
        public Object apply(Object[] args) {
            return getCRuntimeArgumentBlockLength();
        }
    }

    private static class SetCRuntimeArgument0Command implements CompilerCommandPlugin {

        @Override
        public String name() {
            return "com.oracle.svm.core.JavaMainWrapper.setCRuntimeArgument0(String)boolean";
        }

        @Override
        public Object apply(Object[] args) {
            return setCRuntimeArgument0((String) args[0]);
        }
    }
}
