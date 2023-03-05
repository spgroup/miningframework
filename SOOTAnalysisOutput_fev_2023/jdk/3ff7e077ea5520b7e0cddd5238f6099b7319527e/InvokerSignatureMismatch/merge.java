package compiler.jsr292;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandleHelper;
import jdk.internal.vm.annotation.ForceInline;

public class InvokerSignatureMismatch {

    static final MethodHandle INT_MH;

    static {
        MethodHandle mhI = null;
        try {
            mhI = MethodHandles.lookup().findStatic(InvokerSignatureMismatch.class, "bodyI", MethodType.methodType(void.class, int.class));
        } catch (Throwable e) {
        }
        INT_MH = mhI;
    }

    public static void main(String[] args) throws Throwable {
        for (int i = 0; i < 50_000; i++) {
            mainLink(i);
            mainInvoke(i);
        }
    }

    static void mainLink(int i) throws Throwable {
        Object name = MethodHandleHelper.internalMemberName(INT_MH);
        MethodHandleHelper.linkToStatic(INT_MH, (float) i, name);
    }

    static void mainInvoke(int i) throws Throwable {
        MethodHandleHelper.invokeBasicV(INT_MH, (float) i);
    }

    static int cnt = 0;

    static void bodyI(int x) {
        if ((x & 1023) == 0) {
            ++cnt;
        }
    }
}
