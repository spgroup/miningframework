package com.oracle.svm.test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import sun.misc.Unsafe;

class PureMustBeSafeEarly {

    static int v;

    static {
        v = 1;
        v = 42;
    }
}

class InitializesPureMustBeDelayed {

    static int v;

    static {
        v = PureMustBeSafeEarly.v;
    }
}

class NonPureAccessedFinal {

    static final int v = 1;

    static {
        System.out.println("Must not be called at runtime or compile time.");
        System.exit(1);
    }
}

class PureCallMustBeSafeEarly {

    static int v;

    static {
        v = TestClassInitializationMustBeSafe.pure();
    }
}

class NonPureMustBeDelayed {

    static int v = 1;

    static {
        System.out.println("Analysis should not reach here.");
    }
}

class InitializesNonPureMustBeDelayed {

    static int v = NonPureMustBeDelayed.v;
}

class SystemPropReadMustBeDelayed {

    static int v = 1;

    static {
        System.getProperty("test");
    }
}

class SystemPropWriteMustBeDelayed {

    static int v = 1;

    static {
        System.setProperty("test", "");
    }
}

class StartsAThreadMustBeDelayed {

    static int v = 1;

    static {
        new Thread().start();
    }
}

class CreatesAFileMustBeDelayed {

    static int v = 1;

    static File f = new File("./");
}

class CreatesAnExceptionMustBeDelayed {

    static Exception e;

    static {
        e = new Exception("should fire at runtime");
    }
}

class ThrowsAnExceptionMustBeDelayed {

    static int v = 1;

    static {
        if (PureMustBeSafeEarly.v == 42) {
            throw new RuntimeException("should fire at runtime");
        }
    }
}

interface PureInterfaceMustBeSafeEarly {
}

class PureSubclassMustBeDelayed extends SuperClassMustBeDelayed {

    static int v = 1;
}

class SuperClassMustBeDelayed implements PureInterfaceMustBeSafeEarly {

    static {
        System.out.println("Delaying this class.");
    }
}

interface InterfaceNonPureMustBeDelayed {

    int v = B.v;

    class B {

        static int v = 1;

        static {
            System.out.println("Delaying this class.");
        }
    }
}

interface InterfaceNonPureDefaultMustBeDelayed {

    int v = B.v;

    class B {

        static int v = 1;

        static {
            System.out.println("Delaying this class.");
        }
    }

    default int m() {
        return v;
    }
}

class PureSubclassInheritsDelayedInterfaceMustBeSafeEarly implements InterfaceNonPureMustBeDelayed {

    static int v = 1;
}

class PureSubclassInheritsDelayedDefaultInterfaceMustBeDelayed implements InterfaceNonPureDefaultMustBeDelayed {

    static int v = 1;
}

class ImplicitExceptionInInitializerMustBeDelayed {

    static int a = 10;

    static int b = 0;

    static int res;

    static {
        res = a / b;
    }
}

class PureDependsOnImplicitExceptionMustBeDelayed {

    static int a;

    static {
        a = ImplicitExceptionInInitializerMustBeDelayed.res;
    }
}

class StaticFieldHolderMustBeSafeEarly {

    static int a = 111;

    static void setA(int value) {
        a = value;
    }
}

class StaticFieldModifer1MustBeDelayed {

    static {
        StaticFieldHolderMustBeSafeEarly.a = 222;
    }

    static void triggerInitialization() {
    }
}

class StaticFieldModifer2MustBeDelayed {

    static {
        StaticFieldHolderMustBeSafeEarly.setA(333);
    }

    static void triggerInitialization() {
    }
}

class RecursionInInitializerMustBeSafeLate {

    static int i = compute(200);

    static int compute(int n) {
        if (n <= 1) {
            return 1;
        } else {
            return n + compute(n - 1);
        }
    }
}

class UnsafeAccessMustBeSafeLate {

    static UnsafeAccessMustBeSafeLate value = compute();

    int f01, f02, f03, f04, f05, f06, f07, f08, f09, f10, f11, f12, f13, f14, f15, f16;

    static UnsafeAccessMustBeSafeLate compute() {
        UnsafeAccessMustBeSafeLate result = new UnsafeAccessMustBeSafeLate();
        UnsafeAccess.UNSAFE.putInt(result, 32L, 1234);
        return result;
    }
}

enum EnumMustBeSafeEarly {

    V1(null), V2("Hello"), V3(new Object());

    final Object value;

    EnumMustBeSafeEarly(Object value) {
        this.value = value;
    }

    Object getValue() {
        assert value != null;
        return value;
    }
}

class NativeMethodMustBeDelayed {

    static int i = compute();

    static int compute() {
        if (i < 0) {
            nativeMethod();
        }
        return 42;
    }

    static native void nativeMethod();

    static void foo() {
        assert assertionOnlyCode();
    }

    static boolean assertionOnlyCode() {
        AssertionOnlyClassMustBeUnreachable.reference();
        return false;
    }
}

class AssertionOnlyClassMustBeUnreachable {

    static void reference() {
    }
}

class UnsafeAccess {

    static final Unsafe UNSAFE = initUnsafe();

    private static Unsafe initUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(Unsafe.class);
        } catch (Exception e) {
            throw new RuntimeException("exception while trying to get Unsafe", e);
        }
    }
}

class TestClassInitializationMustBeSafeFeature implements Feature {

    static final Class<?>[] checkedClasses = new Class<?>[] { PureMustBeSafeEarly.class, NonPureMustBeDelayed.class, PureCallMustBeSafeEarly.class, InitializesNonPureMustBeDelayed.class, SystemPropReadMustBeDelayed.class, SystemPropWriteMustBeDelayed.class, StartsAThreadMustBeDelayed.class, CreatesAFileMustBeDelayed.class, CreatesAnExceptionMustBeDelayed.class, ThrowsAnExceptionMustBeDelayed.class, PureInterfaceMustBeSafeEarly.class, PureSubclassMustBeDelayed.class, SuperClassMustBeDelayed.class, InterfaceNonPureMustBeDelayed.class, InterfaceNonPureDefaultMustBeDelayed.class, PureSubclassInheritsDelayedInterfaceMustBeSafeEarly.class, PureSubclassInheritsDelayedDefaultInterfaceMustBeDelayed.class, ImplicitExceptionInInitializerMustBeDelayed.class, PureDependsOnImplicitExceptionMustBeDelayed.class, StaticFieldHolderMustBeSafeEarly.class, StaticFieldModifer1MustBeDelayed.class, StaticFieldModifer2MustBeDelayed.class, RecursionInInitializerMustBeSafeLate.class, UnsafeAccessMustBeSafeLate.class, EnumMustBeSafeEarly.class, NativeMethodMustBeDelayed.class };

    private static void checkClasses(boolean checkSafeEarly, boolean checkSafeLate) {
        System.out.println("=== Checking initialization state of classes: checkSafeEarly=" + checkSafeEarly + ", checkSafeLate=" + checkSafeLate);
        List<String> errors = new ArrayList<>();
        for (Class<?> checkedClass : checkedClasses) {
            boolean nameHasSafeEarly = checkedClass.getName().contains("MustBeSafeEarly");
            boolean nameHasSafeLate = checkedClass.getName().contains("MustBeSafeLate");
            boolean nameHasDelayed = checkedClass.getName().contains("MustBeDelayed");
            if ((nameHasSafeEarly ? 1 : 0) + (nameHasSafeLate ? 1 : 0) + (nameHasDelayed ? 1 : 0) != 1) {
                errors.add(checkedClass.getName() + ": Wrongly named class (nameHasSafeEarly: " + nameHasSafeEarly + ", nameHasSafeLate: " + nameHasSafeLate + ", nameHasDelayed: " + nameHasDelayed);
            } else {
                boolean initialized = !UnsafeAccess.UNSAFE.shouldBeInitialized(checkedClass);
                if (nameHasDelayed && initialized) {
                    errors.add(checkedClass.getName() + ": Check for MustBeDelayed failed");
                }
                if (nameHasSafeEarly && initialized != checkSafeEarly) {
                    errors.add(checkedClass.getName() + ": Check for MustBeSafeEarly failed");
                }
                if (nameHasSafeLate && initialized != checkSafeLate) {
                    errors.add(checkedClass.getName() + ": Check for MustBeSafeLate failed");
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new Error(errors.stream().collect(Collectors.joining(System.lineSeparator())));
        }
    }

    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        RuntimeClassInitialization.initializeAtBuildTime(UnsafeAccess.class);
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        checkClasses(false, false);
    }

    @Override
    public void duringAnalysis(DuringAnalysisAccess access) {
        checkClasses(true, false);
    }

    @Override
    public void afterAnalysis(AfterAnalysisAccess access) {
        if (access.isReachable(AssertionOnlyClassMustBeUnreachable.class)) {
            throw new Error("Assertion check was not constant folded for a class that is initialized at run time. " + "We assume here that the image is built with assertions disabled, which is the case for the gate check.");
        }
    }

    @Override
    public void beforeCompilation(BeforeCompilationAccess access) {
        checkClasses(true, true);
    }

    @Override
    public void afterImageWrite(AfterImageWriteAccess access) {
        checkClasses(true, true);
    }
}

public class TestClassInitializationMustBeSafe {

    static int pure() {
        return transitivelyPure() + 42;
    }

    private static int transitivelyPure() {
        return 42;
    }

    public static void main(String[] args) {
        System.out.println(PureMustBeSafeEarly.v);
        System.out.println(PureCallMustBeSafeEarly.v);
        System.out.println(InitializesPureMustBeDelayed.v);
        System.out.println(NonPureMustBeDelayed.v);
        System.out.println(NonPureAccessedFinal.v);
        System.out.println(InitializesNonPureMustBeDelayed.v);
        System.out.println(SystemPropReadMustBeDelayed.v);
        System.out.println(SystemPropWriteMustBeDelayed.v);
        System.out.println(StartsAThreadMustBeDelayed.v);
        System.out.println(CreatesAFileMustBeDelayed.v);
        System.out.println(PureSubclassMustBeDelayed.v);
        System.out.println(PureSubclassInheritsDelayedInterfaceMustBeSafeEarly.v);
        System.out.println(PureSubclassInheritsDelayedDefaultInterfaceMustBeDelayed.v);
        System.out.println(InterfaceNonPureMustBeDelayed.v);
        try {
            System.out.println(ThrowsAnExceptionMustBeDelayed.v);
        } catch (Throwable t) {
            System.out.println(CreatesAnExceptionMustBeDelayed.e.getMessage());
        }
        try {
            System.out.println(ImplicitExceptionInInitializerMustBeDelayed.res);
            throw new RuntimeException("should not reach here");
        } catch (ExceptionInInitializerError ae) {
            if (!(ae.getCause() instanceof ArithmeticException)) {
                throw new RuntimeException("should not reach here");
            }
        }
        try {
            System.out.println(PureDependsOnImplicitExceptionMustBeDelayed.a);
            throw new RuntimeException("should not reach here");
        } catch (NoClassDefFoundError ae) {
        }
        int a = StaticFieldHolderMustBeSafeEarly.a;
        if (a != 111) {
            throw new RuntimeException("expected 111 but found " + a);
        }
        StaticFieldModifer1MustBeDelayed.triggerInitialization();
        a = StaticFieldHolderMustBeSafeEarly.a;
        if (a != 222) {
            throw new RuntimeException("expected 222 but found " + a);
        }
        StaticFieldModifer2MustBeDelayed.triggerInitialization();
        a = StaticFieldHolderMustBeSafeEarly.a;
        if (a != 333) {
            throw new RuntimeException("expected 333 but found " + a);
        }
        System.out.println(RecursionInInitializerMustBeSafeLate.i);
        UnsafeAccessMustBeSafeLate value = UnsafeAccessMustBeSafeLate.value;
        System.out.println(value.f01);
        System.out.println(value.f02);
        System.out.println(value.f03);
        System.out.println(value.f04);
        System.out.println(value.f05);
        System.out.println(value.f06);
        System.out.println(value.f07);
        System.out.println(value.f08);
        System.out.println(value.f09);
        System.out.println(value.f10);
        System.out.println(value.f11);
        System.out.println(value.f12);
        System.out.println(value.f13);
        System.out.println(value.f14);
        System.out.println(value.f15);
        System.out.println(value.f16);
        for (EnumMustBeSafeEarly e : EnumMustBeSafeEarly.values()) {
            System.out.println(e.getValue());
        }
        System.out.println(NativeMethodMustBeDelayed.i);
        NativeMethodMustBeDelayed.foo();
    }
}
