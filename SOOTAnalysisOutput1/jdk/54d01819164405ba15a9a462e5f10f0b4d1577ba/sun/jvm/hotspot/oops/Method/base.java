package sun.jvm.hotspot.oops;

import java.io.PrintStream;
import java.util.Observable;
import java.util.Observer;
import sun.jvm.hotspot.code.NMethod;
import sun.jvm.hotspot.debugger.Address;
import sun.jvm.hotspot.interpreter.OopMapCacheEntry;
import sun.jvm.hotspot.runtime.SignatureConverter;
import sun.jvm.hotspot.runtime.VM;
import sun.jvm.hotspot.runtime.VMObjectFactory;
import sun.jvm.hotspot.types.AddressField;
import sun.jvm.hotspot.types.Type;
import sun.jvm.hotspot.types.TypeDataBase;
import sun.jvm.hotspot.types.WrongTypeException;
import sun.jvm.hotspot.utilities.Assert;

public class Method extends Metadata {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        type = db.lookupType("Method");
        constMethod = type.getAddressField("_constMethod");
        methodData = type.getAddressField("_method_data");
        methodCounters = type.getAddressField("_method_counters");
        accessFlags = new CIntField(type.getCIntegerField("_access_flags"), 0);
        code = type.getAddressField("_code");
        vtableIndex = new CIntField(type.getCIntegerField("_vtable_index"), 0);
        objectInitializerName = null;
        classInitializerName = null;
    }

    public Method(Address addr) {
        super(addr);
    }

    public boolean isMethod() {
        return true;
    }

    private static Type type;

    private static AddressField constMethod;

    private static AddressField methodData;

    private static AddressField methodCounters;

    private static CIntField accessFlags;

    private static CIntField vtableIndex;

    private static AddressField code;

    private static Symbol objectInitializerName;

    private static Symbol classInitializerName;

    private static Symbol objectInitializerName() {
        if (objectInitializerName == null) {
            objectInitializerName = VM.getVM().getSymbolTable().probe("<init>");
        }
        return objectInitializerName;
    }

    private static Symbol classInitializerName() {
        if (classInitializerName == null) {
            classInitializerName = VM.getVM().getSymbolTable().probe("<clinit>");
        }
        return classInitializerName;
    }

    public ConstMethod getConstMethod() {
        Address addr = constMethod.getValue(getAddress());
        return (ConstMethod) VMObjectFactory.newObject(ConstMethod.class, addr);
    }

    public ConstantPool getConstants() {
        return getConstMethod().getConstants();
    }

    public MethodData getMethodData() {
        Address addr = methodData.getValue(getAddress());
        return (MethodData) VMObjectFactory.newObject(MethodData.class, addr);
    }

    public MethodCounters getMethodCounters() {
        Address addr = methodCounters.getValue(getAddress());
        return (MethodCounters) VMObjectFactory.newObject(MethodCounters.class, addr);
    }

    public long getMaxStack() {
        return getConstMethod().getMaxStack();
    }

    public long getMaxLocals() {
        return getConstMethod().getMaxLocals();
    }

    public long getSizeOfParameters() {
        return getConstMethod().getSizeOfParameters();
    }

    public long getNameIndex() {
        return getConstMethod().getNameIndex();
    }

    public long getSignatureIndex() {
        return getConstMethod().getSignatureIndex();
    }

    public long getGenericSignatureIndex() {
        return getConstMethod().getGenericSignatureIndex();
    }

    public long getAccessFlags() {
        return accessFlags.getValue(this);
    }

    public long getCodeSize() {
        return getConstMethod().getCodeSize();
    }

    public long getVtableIndex() {
        return vtableIndex.getValue(this);
    }

    public long getInvocationCount() {
        MethodCounters mc = getMethodCounters();
        return mc == null ? 0 : mc.getInvocationCounter();
    }

    public long getBackedgeCount() {
        MethodCounters mc = getMethodCounters();
        return mc == null ? 0 : mc.getBackedgeCounter();
    }

    public NMethod getNativeMethod() {
        Address addr = code.getValue(getAddress());
        return (NMethod) VMObjectFactory.newObject(NMethod.class, addr);
    }

    public AccessFlags getAccessFlagsObj() {
        return new AccessFlags(getAccessFlags());
    }

    public int getBytecodeOrBPAt(int bci) {
        return getConstMethod().getBytecodeOrBPAt(bci);
    }

    public int getOrigBytecodeAt(int bci) {
        BreakpointInfo bp = getMethodHolder().getBreakpoints();
        for (; bp != null; bp = bp.getNext()) {
            if (bp.match(this, bci)) {
                return bp.getOrigBytecode();
            }
        }
        System.err.println("Requested bci " + bci);
        for (; bp != null; bp = bp.getNext()) {
            System.err.println("Breakpoint at bci " + bp.getBCI() + ", bytecode " + bp.getOrigBytecode());
        }
        Assert.that(false, "Should not reach here");
        return -1;
    }

    public byte getBytecodeByteArg(int bci) {
        return getConstMethod().getBytecodeByteArg(bci);
    }

    public short getBytecodeShortArg(int bci) {
        return getConstMethod().getBytecodeShortArg(bci);
    }

    public short getNativeShortArg(int bci) {
        return getConstMethod().getNativeShortArg(bci);
    }

    public int getBytecodeIntArg(int bci) {
        return getConstMethod().getBytecodeIntArg(bci);
    }

    public int getNativeIntArg(int bci) {
        return getConstMethod().getNativeIntArg(bci);
    }

    public byte[] getByteCode() {
        return getConstMethod().getByteCode();
    }

    public Symbol getName() {
        return getConstants().getSymbolAt(getNameIndex());
    }

    public Symbol getSignature() {
        return getConstants().getSymbolAt(getSignatureIndex());
    }

    public Symbol getGenericSignature() {
        long index = getGenericSignatureIndex();
        return (index != 0L) ? getConstants().getSymbolAt(index) : null;
    }

    public InstanceKlass getMethodHolder() {
        return getConstants().getPoolHolder();
    }

    public boolean isPublic() {
        return getAccessFlagsObj().isPublic();
    }

    public boolean isPrivate() {
        return getAccessFlagsObj().isPrivate();
    }

    public boolean isProtected() {
        return getAccessFlagsObj().isProtected();
    }

    public boolean isPackagePrivate() {
        AccessFlags af = getAccessFlagsObj();
        return (!af.isPublic() && !af.isPrivate() && !af.isProtected());
    }

    public boolean isStatic() {
        return getAccessFlagsObj().isStatic();
    }

    public boolean isFinal() {
        return getAccessFlagsObj().isFinal();
    }

    public boolean isSynchronized() {
        return getAccessFlagsObj().isSynchronized();
    }

    public boolean isBridge() {
        return getAccessFlagsObj().isBridge();
    }

    public boolean isVarArgs() {
        return getAccessFlagsObj().isVarArgs();
    }

    public boolean isNative() {
        return getAccessFlagsObj().isNative();
    }

    public boolean isAbstract() {
        return getAccessFlagsObj().isAbstract();
    }

    public boolean isStrict() {
        return getAccessFlagsObj().isStrict();
    }

    public boolean isSynthetic() {
        return getAccessFlagsObj().isSynthetic();
    }

    public boolean isConstructor() {
        return (!isStatic()) && getName().equals(objectInitializerName());
    }

    public boolean isStaticInitializer() {
        return isStatic() && getName().equals(classInitializerName());
    }

    public boolean isObsolete() {
        return getAccessFlagsObj().isObsolete();
    }

    public OopMapCacheEntry getMaskFor(int bci) {
        OopMapCacheEntry entry = new OopMapCacheEntry();
        entry.fill(this, bci);
        return entry;
    }

    public long getSize() {
        return type.getSize() + (isNative() ? 2 : 0);
    }

    public void printValueOn(PrintStream tty) {
        tty.print("Method " + getName().asString() + getSignature().asString() + "@" + getAddress());
    }

    public void iterateFields(MetadataVisitor visitor) {
        visitor.doCInt(accessFlags, true);
    }

    public boolean hasLineNumberTable() {
        return getConstMethod().hasLineNumberTable();
    }

    public int getLineNumberFromBCI(int bci) {
        return getConstMethod().getLineNumberFromBCI(bci);
    }

    public LineNumberTableElement[] getLineNumberTable() {
        return getConstMethod().getLineNumberTable();
    }

    public boolean hasLocalVariableTable() {
        return getConstMethod().hasLocalVariableTable();
    }

    public LocalVariableTableElement[] getLocalVariableTable() {
        return getConstMethod().getLocalVariableTable();
    }

    public Symbol getLocalVariableName(int bci, int slot) {
        if (!hasLocalVariableTable()) {
            return null;
        }
        LocalVariableTableElement[] locals = getLocalVariableTable();
        for (int l = 0; l < locals.length; l++) {
            LocalVariableTableElement local = locals[l];
            if ((bci >= local.getStartBCI()) && (bci < (local.getStartBCI() + local.getLength())) && slot == local.getSlot()) {
                return getConstants().getSymbolAt(local.getNameCPIndex());
            }
        }
        return null;
    }

    public boolean hasExceptionTable() {
        return getConstMethod().hasExceptionTable();
    }

    public ExceptionTableElement[] getExceptionTable() {
        return getConstMethod().getExceptionTable();
    }

    public boolean hasCheckedExceptions() {
        return getConstMethod().hasCheckedExceptions();
    }

    public CheckedExceptionElement[] getCheckedExceptions() {
        return getConstMethod().getCheckedExceptions();
    }

    public String externalNameAndSignature() {
        final StringBuffer buf = new StringBuffer();
        buf.append(getMethodHolder().getName().asString());
        buf.append(".");
        buf.append(getName().asString());
        buf.append("(");
        new SignatureConverter(getSignature(), buf).iterateParameters();
        buf.append(")");
        return buf.toString().replace('/', '.');
    }

    public void dumpReplayData(PrintStream out) {
        NMethod nm = getNativeMethod();
        int code_size = 0;
        if (nm != null) {
            code_size = (int) nm.codeEnd().minus(nm.getVerifiedEntryPoint());
        }
        Klass holder = getMethodHolder();
        out.println("ciMethod " + nameAsAscii() + " " + getInvocationCount() + " " + getBackedgeCount() + " " + interpreterInvocationCount() + " " + interpreterThrowoutCount() + " " + code_size);
    }

    public int interpreterThrowoutCount() {
        return getMethodCounters().interpreterThrowoutCount();
    }

    public int interpreterInvocationCount() {
        return getMethodCounters().interpreterInvocationCount();
    }

    public String nameAsAscii() {
        return getMethodHolder().getName().asString() + " " + OopUtilities.escapeString(getName().asString()) + " " + getSignature().asString();
    }
}
