package org.graalvm.compiler.core.test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import org.junit.Test;
import org.graalvm.compiler.nodes.DeoptimizeNode;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.StructuredGraph.AllowAssumptions;

public final class MethodHandleEagerResolution extends GraalCompilerTest {

    private static final MethodHandle FIELD_HANDLE;

    static {
        Field field;
        try {
            field = String.class.getDeclaredField("value");
        } catch (NoSuchFieldException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        field.setAccessible(true);
        try {
            FIELD_HANDLE = MethodHandles.lookup().unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("unable to initialize field handle", e);
        }
    }

    public static char[] getBackingCharArray(String str) {
        try {
            return (char[]) FIELD_HANDLE.invokeExact(str);
        } catch (Throwable e) {
            throw new IllegalStateException();
        }
    }

    @Test
    public void testFieldInvokeExact() {
        StructuredGraph graph = parseEager("getBackingCharArray", AllowAssumptions.NO);
        assertTrue(graph.getNodes().filter(DeoptimizeNode.class).isEmpty());
    }
}
