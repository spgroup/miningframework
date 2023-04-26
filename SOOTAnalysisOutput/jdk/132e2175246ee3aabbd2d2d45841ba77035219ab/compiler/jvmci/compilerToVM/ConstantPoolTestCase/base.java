package compiler.jvmci.compilerToVM;

import java.util.HashMap;
import java.util.Map;
import jdk.vm.ci.hotspot.HotSpotResolvedObjectType;
import jdk.internal.misc.SharedSecrets;
import sun.reflect.ConstantPool;

public class ConstantPoolTestCase {

    private final Map<ConstantPoolTestsHelper.ConstantTypes, Validator> typeTests;

    public static interface Validator {

        void validate(jdk.vm.ci.meta.ConstantPool constantPoolCTVM, ConstantPool constantPoolSS, ConstantPoolTestsHelper.DummyClasses dummyClass, int index);
    }

    public ConstantPoolTestCase(Map<ConstantPoolTestsHelper.ConstantTypes, Validator> typeTests) {
        this.typeTests = new HashMap<>();
        this.typeTests.putAll(typeTests);
    }

    private void messageOnFail(Throwable t, ConstantPoolTestsHelper.ConstantTypes cpType, ConstantPoolTestsHelper.DummyClasses dummyClass, int index) {
        ConstantPool constantPoolSS = SharedSecrets.getJavaLangAccess().getConstantPool(dummyClass.klass);
        String msg = String.format("Test for %s constant pool entry of" + " type %s", dummyClass.klass, cpType.name());
        switch(cpType) {
            case CONSTANT_CLASS:
            case CONSTANT_STRING:
            case CONSTANT_METHODTYPE:
                String utf8 = constantPoolSS.getUTF8At((int) dummyClass.cp.get(index).value);
                msg = String.format("%s (%s) failed with %s", msg, utf8, t);
                break;
            case CONSTANT_INTEGER:
                int intValue = constantPoolSS.getIntAt(index);
                msg = String.format("%s (%d) failed with %s", msg, intValue, t);
                break;
            case CONSTANT_LONG:
                long longValue = constantPoolSS.getLongAt(index);
                msg = String.format("%s (%d) failed with %s", msg, longValue, t);
                break;
            case CONSTANT_FLOAT:
                float floatValue = constantPoolSS.getFloatAt(index);
                msg = String.format("%s (%E) failed with %s", msg, floatValue, t);
                break;
            case CONSTANT_DOUBLE:
                double doubleValue = constantPoolSS.getDoubleAt(index);
                msg = String.format("%s (%E) failed with %s", msg, doubleValue, t);
                break;
            case CONSTANT_UTF8:
                String utf8Value = constantPoolSS.getUTF8At(index);
                msg = String.format("%s (%s) failed with %s", msg, utf8Value, t);
                break;
            case CONSTANT_INVOKEDYNAMIC:
                index = ((int[]) dummyClass.cp.get(index).value)[1];
            case CONSTANT_NAMEANDTYPE:
                String name = constantPoolSS.getUTF8At(((int[]) dummyClass.cp.get(index).value)[0]);
                String type = constantPoolSS.getUTF8At(((int[]) dummyClass.cp.get(index).value)[1]);
                msg = String.format("%s (%s:%s) failed with %s", msg, name, type, t);
                break;
            case CONSTANT_METHODHANDLE:
                index = ((int[]) dummyClass.cp.get(index).value)[1];
            case CONSTANT_METHODREF:
            case CONSTANT_INTERFACEMETHODREF:
            case CONSTANT_FIELDREF:
                int classIndex = ((int[]) dummyClass.cp.get(index).value)[0];
                int nameAndTypeIndex = ((int[]) dummyClass.cp.get(index).value)[1];
                String cName = constantPoolSS.getUTF8At((int) dummyClass.cp.get(classIndex).value);
                String mName = constantPoolSS.getUTF8At(((int[]) dummyClass.cp.get(nameAndTypeIndex).value)[0]);
                String mType = constantPoolSS.getUTF8At(((int[]) dummyClass.cp.get(nameAndTypeIndex).value)[1]);
                msg = String.format("%s (%s.%s:%s) failed with %s ", msg, cName, mName, mType, t);
                break;
            default:
                msg = String.format("Test bug: unknown constant type %s ", cpType);
        }
        throw new Error(msg + t.getMessage(), t);
    }

    public void test() {
        for (ConstantPoolTestsHelper.DummyClasses dummyClass : ConstantPoolTestsHelper.DummyClasses.values()) {
            System.out.printf("%nTesting dummy %s%n", dummyClass.klass);
            HotSpotResolvedObjectType holder = HotSpotResolvedObjectType.fromObjectClass(dummyClass.klass);
            jdk.vm.ci.meta.ConstantPool constantPoolCTVM = holder.getConstantPool();
            ConstantPool constantPoolSS = SharedSecrets.getJavaLangAccess().getConstantPool(dummyClass.klass);
            for (Integer i : dummyClass.cp.keySet()) {
                ConstantPoolTestsHelper.ConstantTypes cpType = dummyClass.cp.get(i).type;
                if (!typeTests.keySet().contains(cpType)) {
                    continue;
                }
                try {
                    typeTests.get(cpType).validate(constantPoolCTVM, constantPoolSS, dummyClass, i);
                } catch (Throwable t) {
                    messageOnFail(t, cpType, dummyClass, i);
                }
            }
        }
    }
}
