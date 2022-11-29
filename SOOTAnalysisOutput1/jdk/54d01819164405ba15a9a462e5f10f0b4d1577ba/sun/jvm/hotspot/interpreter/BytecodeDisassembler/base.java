package sun.jvm.hotspot.interpreter;

import java.util.*;
import java.lang.reflect.Constructor;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.utilities.*;

public class BytecodeDisassembler {

    private Method method;

    private static Map bytecode2Class = new HashMap();

    private static void addBytecodeClass(int bytecode, Class clazz) {
        bytecode2Class.put(new Integer(bytecode), clazz);
    }

    private static Class getBytecodeClass(int bytecode) {
        return (Class) bytecode2Class.get(new Integer(bytecode));
    }

    static {
        addBytecodeClass(Bytecodes._anewarray, BytecodeANewArray.class);
        addBytecodeClass(Bytecodes._bipush, BytecodeBipush.class);
        addBytecodeClass(Bytecodes._checkcast, BytecodeCheckCast.class);
        addBytecodeClass(Bytecodes._getfield, BytecodeGetField.class);
        addBytecodeClass(Bytecodes._getstatic, BytecodeGetStatic.class);
        addBytecodeClass(Bytecodes._goto, BytecodeGoto.class);
        addBytecodeClass(Bytecodes._goto_w, BytecodeGotoW.class);
        addBytecodeClass(Bytecodes._ifeq, BytecodeIf.class);
        addBytecodeClass(Bytecodes._ifne, BytecodeIf.class);
        addBytecodeClass(Bytecodes._iflt, BytecodeIf.class);
        addBytecodeClass(Bytecodes._ifge, BytecodeIf.class);
        addBytecodeClass(Bytecodes._ifgt, BytecodeIf.class);
        addBytecodeClass(Bytecodes._ifle, BytecodeIf.class);
        addBytecodeClass(Bytecodes._if_icmpeq, BytecodeIf.class);
        addBytecodeClass(Bytecodes._if_icmpne, BytecodeIf.class);
        addBytecodeClass(Bytecodes._if_icmplt, BytecodeIf.class);
        addBytecodeClass(Bytecodes._if_icmpge, BytecodeIf.class);
        addBytecodeClass(Bytecodes._if_icmpgt, BytecodeIf.class);
        addBytecodeClass(Bytecodes._if_icmple, BytecodeIf.class);
        addBytecodeClass(Bytecodes._if_acmpeq, BytecodeIf.class);
        addBytecodeClass(Bytecodes._if_acmpne, BytecodeIf.class);
        addBytecodeClass(Bytecodes._ifnull, BytecodeIf.class);
        addBytecodeClass(Bytecodes._ifnonnull, BytecodeIf.class);
        addBytecodeClass(Bytecodes._iinc, BytecodeIinc.class);
        addBytecodeClass(Bytecodes._instanceof, BytecodeInstanceOf.class);
        addBytecodeClass(Bytecodes._invokevirtual, BytecodeInvoke.class);
        addBytecodeClass(Bytecodes._invokestatic, BytecodeInvoke.class);
        addBytecodeClass(Bytecodes._invokespecial, BytecodeInvoke.class);
        addBytecodeClass(Bytecodes._invokeinterface, BytecodeInvoke.class);
        addBytecodeClass(Bytecodes._invokedynamic, BytecodeInvoke.class);
        addBytecodeClass(Bytecodes._jsr, BytecodeJsr.class);
        addBytecodeClass(Bytecodes._jsr_w, BytecodeJsrW.class);
        addBytecodeClass(Bytecodes._iload, BytecodeLoad.class);
        addBytecodeClass(Bytecodes._lload, BytecodeLoad.class);
        addBytecodeClass(Bytecodes._fload, BytecodeLoad.class);
        addBytecodeClass(Bytecodes._dload, BytecodeLoad.class);
        addBytecodeClass(Bytecodes._aload, BytecodeLoad.class);
        addBytecodeClass(Bytecodes._ldc, BytecodeLoadConstant.class);
        addBytecodeClass(Bytecodes._ldc_w, BytecodeLoadConstant.class);
        addBytecodeClass(Bytecodes._ldc2_w, BytecodeLoadConstant.class);
        addBytecodeClass(Bytecodes._lookupswitch, BytecodeLookupswitch.class);
        addBytecodeClass(Bytecodes._multianewarray, BytecodeMultiANewArray.class);
        addBytecodeClass(Bytecodes._new, BytecodeNew.class);
        addBytecodeClass(Bytecodes._newarray, BytecodeNewArray.class);
        addBytecodeClass(Bytecodes._putfield, BytecodePutField.class);
        addBytecodeClass(Bytecodes._putstatic, BytecodePutStatic.class);
        addBytecodeClass(Bytecodes._ret, BytecodeRet.class);
        addBytecodeClass(Bytecodes._sipush, BytecodeSipush.class);
        addBytecodeClass(Bytecodes._istore, BytecodeStore.class);
        addBytecodeClass(Bytecodes._lstore, BytecodeStore.class);
        addBytecodeClass(Bytecodes._fstore, BytecodeStore.class);
        addBytecodeClass(Bytecodes._dstore, BytecodeStore.class);
        addBytecodeClass(Bytecodes._astore, BytecodeStore.class);
        addBytecodeClass(Bytecodes._tableswitch, BytecodeTableswitch.class);
    }

    public BytecodeDisassembler(Method method) {
        this.method = method;
    }

    public Method getMethod() {
        return method;
    }

    public void decode(BytecodeVisitor visitor) {
        visitor.prologue(method);
        BytecodeStream stream = new BytecodeStream(method);
        int javacode = Bytecodes._illegal;
        while ((javacode = stream.next()) != Bytecodes._illegal) {
            int bci = stream.bci();
            int hotspotcode = method.getBytecodeOrBPAt(bci);
            Class clazz = getBytecodeClass(javacode);
            if (clazz == null) {
                clazz = getBytecodeClass(hotspotcode);
                if (clazz == null) {
                    clazz = Bytecode.class;
                }
            }
            Constructor cstr = null;
            try {
                cstr = clazz.getDeclaredConstructor(new Class[] { Method.class, Integer.TYPE });
            } catch (NoSuchMethodException nomethod) {
                if (Assert.ASSERTS_ENABLED) {
                    Assert.that(false, "Bytecode class without proper constructor!");
                }
            }
            Bytecode bytecodeObj = null;
            try {
                bytecodeObj = (Bytecode) cstr.newInstance(new Object[] { method, new Integer(bci) });
            } catch (Exception exp) {
                if (Assert.ASSERTS_ENABLED) {
                    Assert.that(false, "Bytecode instance of class " + clazz.getName() + " can not be created!");
                }
            }
            if (stream.isWide()) {
                visitor.visit(new Bytecode(method, bci - 1));
            }
            try {
                visitor.visit(bytecodeObj);
            } catch (ClassCastException castfail) {
                castfail.printStackTrace();
                System.err.println(method.getAddress() + " " + bci);
            }
        }
        visitor.epilogue();
    }
}
