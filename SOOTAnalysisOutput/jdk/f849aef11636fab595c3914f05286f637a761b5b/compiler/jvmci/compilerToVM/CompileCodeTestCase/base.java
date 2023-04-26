package compiler.jvmci.compilerToVM;

import compiler.testlibrary.CompilerUtils;
import jdk.test.lib.Utils;
import sun.hotspot.WhiteBox;
import sun.hotspot.code.NMethod;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CompileCodeTestCase {

    public static final Map<Class<?>, Object> RECEIVERS;

    private static final WhiteBox WB = WhiteBox.getWhiteBox();

    private static final int COMP_LEVEL;

    static {
        int[] levels = CompilerUtils.getAvailableCompilationLevels();
        if (levels.length == 0) {
            throw new Error("TESTBUG: no compilers available");
        }
        COMP_LEVEL = levels[levels.length - 1];
    }

    private static final Class<?>[] CLASSES = { Interface.class, Dummy.class, DummyEx.class };

    public final Executable executable;

    public final int bci;

    private final boolean isOsr;

    public CompileCodeTestCase(Executable executable, int bci) {
        this.executable = executable;
        this.bci = bci;
        isOsr = bci >= 0;
    }

    public NMethod compile() {
        return compile(COMP_LEVEL);
    }

    public NMethod compile(int level) {
        boolean enqueued = WB.enqueueMethodForCompilation(executable, level, bci);
        if (!enqueued) {
            throw new Error(String.format("%s can't be enqueued for %scompilation on level %d", executable, bci >= 0 ? "osr-" : "", level));
        }
        Utils.waitForCondition(() -> WB.isMethodCompiled(executable, isOsr));
        return NMethod.get(executable, isOsr);
    }

    public static List<CompileCodeTestCase> generate(int bci) {
        ArrayList<CompileCodeTestCase> result = new ArrayList<>();
        for (Class<?> aClass : CLASSES) {
            for (Executable m : aClass.getDeclaredConstructors()) {
                result.add(new CompileCodeTestCase(m, bci));
            }
            Arrays.stream(aClass.getDeclaredMethods()).filter(m -> !Modifier.isAbstract(m.getModifiers())).filter(m -> !Modifier.isNative(m.getModifiers())).map(m -> new CompileCodeTestCase(m, bci)).forEach(result::add);
        }
        return result;
    }

    public NMethod toNMethod() {
        return NMethod.get(executable, isOsr);
    }

    @Override
    public String toString() {
        return "CompileCodeTestCase{" + "executable=" + executable + ", bci=" + bci + '}';
    }

    public void deoptimize() {
        WB.deoptimizeMethod(executable, isOsr);
    }

    public NMethod deoptimizeAndCompile() {
        deoptimize();
        return compile();
    }

    private static interface Interface {

        Interface interfaceMethod();

        default Long defaultOverriddenMethod(Interface[] array) {
            return array == null ? 0L : array.length;
        }

        default int defaultMethod(Object o) {
            return o != null ? o.hashCode() : 0;
        }
    }

    private static abstract class Dummy implements Interface {

        protected Dummy() {
        }

        private static void staticMethod() {
        }

        Dummy instanceMethod(int i) {
            return null;
        }

        abstract Object abstractMethod(double d);

        @Override
        public Long defaultOverriddenMethod(Interface[] array) {
            return 0L;
        }
    }

    public static class DummyEx extends Dummy {

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        public DummyEx() {
        }

        protected Dummy instanceMethod(int i) {
            if (i == 0) {
                return this;
            }
            return null;
        }

        @Override
        Object abstractMethod(double d) {
            return this;
        }

        @Override
        public Interface interfaceMethod() {
            return null;
        }
    }

    static {
        Map<Class<?>, Object> map = new HashMap<>();
        ;
        map.put(CompileCodeTestCase.DummyEx.class, new CompileCodeTestCase.DummyEx());
        map.put(CompileCodeTestCase.Dummy.class, new CompileCodeTestCase.Dummy() {

            @Override
            public CompileCodeTestCase.Interface interfaceMethod() {
                throw new AbstractMethodError();
            }

            @Override
            Object abstractMethod(double d) {
                throw new AbstractMethodError();
            }
        });
        map.put(CompileCodeTestCase.Interface.class, new CompileCodeTestCase.Interface() {

            @Override
            public CompileCodeTestCase.Interface interfaceMethod() {
                throw new AbstractMethodError();
            }
        });
        RECEIVERS = Collections.unmodifiableMap(map);
    }
}
