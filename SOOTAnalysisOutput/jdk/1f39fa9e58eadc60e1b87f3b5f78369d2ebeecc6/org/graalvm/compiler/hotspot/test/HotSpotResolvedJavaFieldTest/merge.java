package org.graalvm.compiler.hotspot.test;

import static java.lang.reflect.Modifier.FINAL;
import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.PROTECTED;
import static java.lang.reflect.Modifier.PUBLIC;
import static java.lang.reflect.Modifier.STATIC;
import static java.lang.reflect.Modifier.TRANSIENT;
import static java.lang.reflect.Modifier.VOLATILE;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.Assert;
import org.junit.Test;
import org.graalvm.compiler.core.common.util.Util;
import org.graalvm.compiler.hotspot.GraalHotSpotVMConfig;
import jdk.vm.ci.hotspot.HotSpotResolvedJavaField;
import jdk.vm.ci.hotspot.HotSpotResolvedObjectType;
import jdk.vm.ci.meta.JavaType;
import jdk.vm.ci.meta.ResolvedJavaField;

public class HotSpotResolvedJavaFieldTest extends HotSpotGraalCompilerTest {

    private static final Class<?>[] classesWithInternalFields = { Class.class, ClassLoader.class };

    private static final Method createFieldMethod;

    private static final Field indexField;

    static {
        Method m = null;
        Field f = null;
        try {
            Class<?> typeImpl = Class.forName("jdk.vm.ci.hotspot.HotSpotResolvedObjectTypeImpl");
            m = typeImpl.getDeclaredMethod("createField", JavaType.class, long.class, int.class, int.class);
            Util.setAccessible(m, true);
            Class<?> fieldImpl = Class.forName("jdk.vm.ci.hotspot.HotSpotResolvedJavaFieldImpl");
            f = fieldImpl.getDeclaredField("index");
            Util.setAccessible(f, true);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        createFieldMethod = m;
        indexField = f;
    }

    private int jvmFieldModifiers() {
        GraalHotSpotVMConfig config = runtime().getVMConfig();
        int accEnum = config.getConstant("JVM_ACC_ENUM", Integer.class, 0x4000);
        int accSynthetic = config.getConstant("JVM_ACC_SYNTHETIC", Integer.class, 0x1000);
        return PUBLIC | PRIVATE | PROTECTED | STATIC | FINAL | VOLATILE | TRANSIENT | accEnum | accSynthetic;
    }

    @Test
    public void testModifiersForInternal() {
        for (Class<?> c : classesWithInternalFields) {
            HotSpotResolvedObjectType type = HotSpotResolvedObjectType.fromObjectClass(c);
            for (ResolvedJavaField field : type.getInstanceFields(false)) {
                if (field.isInternal()) {
                    Assert.assertEquals(0, ~jvmFieldModifiers() & field.getModifiers());
                }
            }
        }
    }

    @Test
    public void testEquivalenceForInternalFields() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        for (Class<?> c : classesWithInternalFields) {
            HotSpotResolvedObjectType type = HotSpotResolvedObjectType.fromObjectClass(c);
            for (ResolvedJavaField field : type.getInstanceFields(false)) {
                if (field.isInternal()) {
                    HotSpotResolvedJavaField expected = (HotSpotResolvedJavaField) field;
                    int index = indexField.getInt(expected);
                    ResolvedJavaField actual = (ResolvedJavaField) createFieldMethod.invoke(type, expected.getType(), expected.offset(), expected.getModifiers(), index);
                    Assert.assertEquals(expected, actual);
                }
            }
        }
    }

    @Test
    public void testIsInObject() {
        for (Field f : String.class.getDeclaredFields()) {
            HotSpotResolvedJavaField rf = (HotSpotResolvedJavaField) getMetaAccess().lookupJavaField(f);
            Assert.assertEquals(rf.toString(), rf.isInObject("a string"), !rf.isStatic());
        }
    }
}
