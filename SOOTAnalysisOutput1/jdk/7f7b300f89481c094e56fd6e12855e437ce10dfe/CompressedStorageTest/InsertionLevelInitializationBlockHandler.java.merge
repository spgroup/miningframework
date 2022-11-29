import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public class CompressedStorageTest {

    private static Method setDomainLengthM;

    private static Field compressedStorageFld;

    private static int DOMAIN_PATTERN;

    private static int PROPLIST_PATTERN;

    private static int PROPVAL_PATTERN;

    private static Method setDomainPattern;

    private static Method setPropertyListPattern;

    private static Method setPropertyValuePattern;

    static {
        try {
            Class<?> clz = ObjectName.class;
            setDomainLengthM = clz.getDeclaredMethod("setDomainLength", int.class);
            setDomainLengthM.setAccessible(true);
            compressedStorageFld = clz.getDeclaredField("_compressed_storage");
            compressedStorageFld.setAccessible(true);
            setDomainPattern = clz.getDeclaredMethod("setDomainPattern", boolean.class);
            setDomainPattern.setAccessible(true);
            setPropertyListPattern = clz.getDeclaredMethod("setPropertyListPattern", boolean.class);
            setPropertyListPattern.setAccessible(true);
            setPropertyValuePattern = clz.getDeclaredMethod("setPropertyValuePattern", boolean.class);
            setPropertyValuePattern.setAccessible(true);
            DOMAIN_PATTERN = getStaticIntFld("DOMAIN_PATTERN");
            PROPLIST_PATTERN = getStaticIntFld("PROPLIST_PATTERN");
            PROPVAL_PATTERN = getStaticIntFld("PROPVAL_PATTERN");
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public static void main(String[] args) throws Exception {
        testZeroLength();
        testNegativeLength();
        testMaxLength();
        testSetDomainPattern();
        testSetPropertyListPattern();
        testSetPropertyValuePattern();
    }

    private static ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("domain", "key", "value");
    }

    private static void testZeroLength() throws Exception {
        setDomainNameLength(0);
    }

    private static void testNegativeLength() throws Exception {
        try {
            setDomainNameLength(-1);
        } catch (MalformedObjectNameException e) {
            return;
        }
        fail("Allowing negative domain name length");
    }

    private static void testMaxLength() throws Exception {
        try {
            setDomainNameLength(Integer.MAX_VALUE / 4 + 1);
        } catch (MalformedObjectNameException e) {
            return;
        }
        fail("Maximum domain name length is not respected");
    }

    private static void testSetDomainPattern() throws Exception {
        ObjectName on = getObjectName();
        checkMask(DOMAIN_PATTERN, setDomainPattern, on);
    }

    private static void testSetPropertyListPattern() throws Exception {
        ObjectName on = getObjectName();
        checkMask(PROPLIST_PATTERN, setPropertyListPattern, on);
    }

    private static void testSetPropertyValuePattern() throws Exception {
        ObjectName on = getObjectName();
        checkMask(PROPVAL_PATTERN, setPropertyValuePattern, on);
    }

    private static void setDomainNameLength(int len) throws MalformedObjectNameException {
        try {
            setDomainLengthM.invoke(getObjectName(), len);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof MalformedObjectNameException) {
                throw (MalformedObjectNameException) cause;
            }
            throw new Error(cause);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new Error(e);
        }
    }

    private static void checkMask(int mask, Method setter, ObjectName on) throws Exception {
        int valBefore = compressedStorageFld.getInt(on);
        setter.invoke(on, true);
        int valAfter = compressedStorageFld.getInt(on);
        checkMask(mask, valAfter ^ valBefore);
        valBefore = valAfter;
        setter.invoke(on, false);
        valAfter = compressedStorageFld.getInt(on);
        checkMask(mask, valAfter ^ valBefore);
    }

    private static void checkMask(int mask, int val) {
        if (val != 0 && val != mask) {
            fail("Invalid mask: expecting '" + Integer.toBinaryString(mask) + "' , received '" + Integer.toBinaryString(val) + "'");
        }
    }

    private static int getStaticIntFld(String name) throws Exception {
        Field fld = ObjectName.class.getDeclaredField(name);
        fld.setAccessible(true);
        return fld.getInt(null);
    }

    private static void fail(String msg) {
        throw new Error(msg);
    }
}