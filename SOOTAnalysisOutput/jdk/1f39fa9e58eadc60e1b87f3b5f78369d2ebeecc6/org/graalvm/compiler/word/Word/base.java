package org.graalvm.compiler.word;

import static org.graalvm.compiler.word.UnsafeAccess.UNSAFE;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.graalvm.compiler.core.common.LocationIdentity;
import org.graalvm.compiler.core.common.calc.Condition;
import org.graalvm.compiler.core.common.calc.UnsignedMath;
import org.graalvm.compiler.debug.GraalError;
import org.graalvm.compiler.nodes.ValueNode;
import org.graalvm.compiler.nodes.calc.AddNode;
import org.graalvm.compiler.nodes.calc.AndNode;
import org.graalvm.compiler.nodes.calc.LeftShiftNode;
import org.graalvm.compiler.nodes.calc.MulNode;
import org.graalvm.compiler.nodes.calc.OrNode;
import org.graalvm.compiler.nodes.calc.RightShiftNode;
import org.graalvm.compiler.nodes.calc.SignedDivNode;
import org.graalvm.compiler.nodes.calc.SignedRemNode;
import org.graalvm.compiler.nodes.calc.SubNode;
import org.graalvm.compiler.nodes.calc.UnsignedDivNode;
import org.graalvm.compiler.nodes.calc.UnsignedRemNode;
import org.graalvm.compiler.nodes.calc.UnsignedRightShiftNode;
import org.graalvm.compiler.nodes.calc.XorNode;
import org.graalvm.compiler.nodes.memory.HeapAccess.BarrierType;
import org.graalvm.compiler.nodes.memory.address.AddressNode.Address;

public abstract class Word implements Signed, Unsigned, Pointer {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Operation {

        Class<? extends ValueNode> node() default ValueNode.class;

        boolean rightOperandIsInt() default false;

        Opcode opcode() default Opcode.NODE_CLASS;

        Condition condition() default Condition.EQ;
    }

    public enum Opcode {

        NODE_CLASS,
        COMPARISON,
        NOT,
        READ_POINTER,
        READ_OBJECT,
        READ_BARRIERED,
        READ_HEAP,
        WRITE_POINTER,
        WRITE_OBJECT,
        WRITE_BARRIERED,
        INITIALIZE,
        ZERO,
        FROM_UNSIGNED,
        FROM_SIGNED,
        FROM_ADDRESS,
        OBJECT_TO_TRACKED,
        OBJECT_TO_UNTRACKED,
        TO_OBJECT,
        TO_RAW_VALUE
    }

    private static Word box(long val) {
        return HostedWord.boxLong(val);
    }

    protected abstract long unbox();

    private static Word intParam(int val) {
        return box(val);
    }

    @Operation(opcode = Opcode.ZERO)
    public static Word zero() {
        return box(0L);
    }

    @Operation(opcode = Opcode.FROM_UNSIGNED)
    public static Word unsigned(long val) {
        return box(val);
    }

    @Operation(opcode = Opcode.FROM_UNSIGNED)
    @SuppressWarnings("unchecked")
    public static <T extends PointerBase> T pointer(long val) {
        return (T) box(val);
    }

    @Operation(opcode = Opcode.FROM_UNSIGNED)
    public static Word unsigned(int val) {
        return box(val & 0xffffffffL);
    }

    @Operation(opcode = Opcode.FROM_SIGNED)
    public static Word signed(long val) {
        return box(val);
    }

    @Operation(opcode = Opcode.FROM_SIGNED)
    public static Word signed(int val) {
        return box(val);
    }

    @Override
    @Operation(opcode = Opcode.TO_RAW_VALUE)
    public long rawValue() {
        return unbox();
    }

    @Operation(opcode = Opcode.OBJECT_TO_TRACKED)
    public static native Pointer objectToTrackedPointer(Object val);

    @Operation(opcode = Opcode.OBJECT_TO_UNTRACKED)
    public static native Pointer objectToUntrackedPointer(Object val);

    @Operation(opcode = Opcode.FROM_ADDRESS)
    public static native Pointer fromAddress(Address address);

    @Override
    @Operation(opcode = Opcode.TO_OBJECT)
    public native Object toObject();

    @Override
    @Operation(node = AddNode.class)
    public Word add(Signed val) {
        return add((Word) val);
    }

    @Override
    @Operation(node = AddNode.class)
    public Word add(Unsigned val) {
        return add((Word) val);
    }

    @Override
    @Operation(node = AddNode.class)
    public Word add(int val) {
        return add(intParam(val));
    }

    @Operation(node = AddNode.class)
    public Word add(Word val) {
        return box(unbox() + val.unbox());
    }

    @Override
    @Operation(node = SubNode.class)
    public Word subtract(Signed val) {
        return subtract((Word) val);
    }

    @Override
    @Operation(node = SubNode.class)
    public Word subtract(Unsigned val) {
        return subtract((Word) val);
    }

    @Override
    @Operation(node = SubNode.class)
    public Word subtract(int val) {
        return subtract(intParam(val));
    }

    @Operation(node = SubNode.class)
    public Word subtract(Word val) {
        return box(unbox() - val.unbox());
    }

    @Override
    @Operation(node = MulNode.class)
    public Word multiply(Signed val) {
        return multiply((Word) val);
    }

    @Override
    @Operation(node = MulNode.class)
    public Word multiply(Unsigned val) {
        return multiply((Word) val);
    }

    @Override
    @Operation(node = MulNode.class)
    public Word multiply(int val) {
        return multiply(intParam(val));
    }

    @Operation(node = MulNode.class)
    public Word multiply(Word val) {
        return box(unbox() * val.unbox());
    }

    @Override
    @Operation(node = SignedDivNode.class)
    public Word signedDivide(Signed val) {
        return signedDivide((Word) val);
    }

    @Override
    @Operation(node = SignedDivNode.class)
    public Word signedDivide(int val) {
        return signedDivide(intParam(val));
    }

    @Operation(node = SignedDivNode.class)
    public Word signedDivide(Word val) {
        return box(unbox() / val.unbox());
    }

    @Override
    @Operation(node = UnsignedDivNode.class)
    public Word unsignedDivide(Unsigned val) {
        return unsignedDivide((Word) val);
    }

    @Override
    @Operation(node = UnsignedDivNode.class)
    public Word unsignedDivide(int val) {
        return signedDivide(intParam(val));
    }

    @Operation(node = UnsignedDivNode.class)
    public Word unsignedDivide(Word val) {
        return box(Long.divideUnsigned(unbox(), val.unbox()));
    }

    @Override
    @Operation(node = SignedRemNode.class)
    public Word signedRemainder(Signed val) {
        return signedRemainder((Word) val);
    }

    @Override
    @Operation(node = SignedRemNode.class)
    public Word signedRemainder(int val) {
        return signedRemainder(intParam(val));
    }

    @Operation(node = SignedRemNode.class)
    public Word signedRemainder(Word val) {
        return box(unbox() % val.unbox());
    }

    @Override
    @Operation(node = UnsignedRemNode.class)
    public Word unsignedRemainder(Unsigned val) {
        return unsignedRemainder((Word) val);
    }

    @Override
    @Operation(node = UnsignedRemNode.class)
    public Word unsignedRemainder(int val) {
        return signedRemainder(intParam(val));
    }

    @Operation(node = UnsignedRemNode.class)
    public Word unsignedRemainder(Word val) {
        return box(Long.remainderUnsigned(unbox(), val.unbox()));
    }

    @Override
    @Operation(node = LeftShiftNode.class, rightOperandIsInt = true)
    public Word shiftLeft(Unsigned val) {
        return shiftLeft((Word) val);
    }

    @Override
    @Operation(node = LeftShiftNode.class, rightOperandIsInt = true)
    public Word shiftLeft(int val) {
        return shiftLeft(intParam(val));
    }

    @Operation(node = LeftShiftNode.class, rightOperandIsInt = true)
    public Word shiftLeft(Word val) {
        return box(unbox() << val.unbox());
    }

    @Override
    @Operation(node = RightShiftNode.class, rightOperandIsInt = true)
    public Word signedShiftRight(Unsigned val) {
        return signedShiftRight((Word) val);
    }

    @Override
    @Operation(node = RightShiftNode.class, rightOperandIsInt = true)
    public Word signedShiftRight(int val) {
        return signedShiftRight(intParam(val));
    }

    @Operation(node = RightShiftNode.class, rightOperandIsInt = true)
    public Word signedShiftRight(Word val) {
        return box(unbox() >> val.unbox());
    }

    @Override
    @Operation(node = UnsignedRightShiftNode.class, rightOperandIsInt = true)
    public Word unsignedShiftRight(Unsigned val) {
        return unsignedShiftRight((Word) val);
    }

    @Override
    @Operation(node = UnsignedRightShiftNode.class, rightOperandIsInt = true)
    public Word unsignedShiftRight(int val) {
        return unsignedShiftRight(intParam(val));
    }

    @Operation(node = UnsignedRightShiftNode.class, rightOperandIsInt = true)
    public Word unsignedShiftRight(Word val) {
        return box(unbox() >>> val.unbox());
    }

    @Override
    @Operation(node = AndNode.class)
    public Word and(Signed val) {
        return and((Word) val);
    }

    @Override
    @Operation(node = AndNode.class)
    public Word and(Unsigned val) {
        return and((Word) val);
    }

    @Override
    @Operation(node = AndNode.class)
    public Word and(int val) {
        return and(intParam(val));
    }

    @Operation(node = AndNode.class)
    public Word and(Word val) {
        return box(unbox() & val.unbox());
    }

    @Override
    @Operation(node = OrNode.class)
    public Word or(Signed val) {
        return or((Word) val);
    }

    @Override
    @Operation(node = OrNode.class)
    public Word or(Unsigned val) {
        return or((Word) val);
    }

    @Override
    @Operation(node = OrNode.class)
    public Word or(int val) {
        return or(intParam(val));
    }

    @Operation(node = OrNode.class)
    public Word or(Word val) {
        return box(unbox() | val.unbox());
    }

    @Override
    @Operation(node = XorNode.class)
    public Word xor(Signed val) {
        return xor((Word) val);
    }

    @Override
    @Operation(node = XorNode.class)
    public Word xor(Unsigned val) {
        return xor((Word) val);
    }

    @Override
    @Operation(node = XorNode.class)
    public Word xor(int val) {
        return xor(intParam(val));
    }

    @Operation(node = XorNode.class)
    public Word xor(Word val) {
        return box(unbox() ^ val.unbox());
    }

    @Override
    @Operation(opcode = Opcode.NOT)
    public Word not() {
        return box(~unbox());
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.EQ)
    public boolean equal(ComparableWord val) {
        return equal((Word) val);
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.EQ)
    public boolean equal(Signed val) {
        return equal((Word) val);
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.EQ)
    public boolean equal(Unsigned val) {
        return equal((Word) val);
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.EQ)
    public boolean equal(int val) {
        return equal(intParam(val));
    }

    @Operation(opcode = Opcode.COMPARISON, condition = Condition.EQ)
    public boolean equal(Word val) {
        return unbox() == val.unbox();
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.NE)
    public boolean notEqual(ComparableWord val) {
        return notEqual((Word) val);
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.NE)
    public boolean notEqual(Signed val) {
        return notEqual((Word) val);
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.NE)
    public boolean notEqual(Unsigned val) {
        return notEqual((Word) val);
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.NE)
    public boolean notEqual(int val) {
        return notEqual(intParam(val));
    }

    @Operation(opcode = Opcode.COMPARISON, condition = Condition.NE)
    public boolean notEqual(Word val) {
        return unbox() != val.unbox();
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.LT)
    public boolean lessThan(Signed val) {
        return lessThan((Word) val);
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.LT)
    public boolean lessThan(int val) {
        return lessThan(intParam(val));
    }

    @Operation(opcode = Opcode.COMPARISON, condition = Condition.LT)
    public boolean lessThan(Word val) {
        return unbox() < val.unbox();
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.LE)
    public boolean lessOrEqual(Signed val) {
        return lessOrEqual((Word) val);
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.LE)
    public boolean lessOrEqual(int val) {
        return lessOrEqual(intParam(val));
    }

    @Operation(opcode = Opcode.COMPARISON, condition = Condition.LE)
    public boolean lessOrEqual(Word val) {
        return unbox() <= val.unbox();
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.GT)
    public boolean greaterThan(Signed val) {
        return greaterThan((Word) val);
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.GT)
    public boolean greaterThan(int val) {
        return greaterThan(intParam(val));
    }

    @Operation(opcode = Opcode.COMPARISON, condition = Condition.GT)
    public boolean greaterThan(Word val) {
        return unbox() > val.unbox();
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.GE)
    public boolean greaterOrEqual(Signed val) {
        return greaterOrEqual((Word) val);
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.GE)
    public boolean greaterOrEqual(int val) {
        return greaterOrEqual(intParam(val));
    }

    @Operation(opcode = Opcode.COMPARISON, condition = Condition.GE)
    public boolean greaterOrEqual(Word val) {
        return unbox() >= val.unbox();
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.BT)
    public boolean belowThan(Unsigned val) {
        return belowThan((Word) val);
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.BT)
    public boolean belowThan(int val) {
        return belowThan(intParam(val));
    }

    @Operation(opcode = Opcode.COMPARISON, condition = Condition.BT)
    public boolean belowThan(Word val) {
        return UnsignedMath.belowThan(unbox(), val.unbox());
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.BE)
    public boolean belowOrEqual(Unsigned val) {
        return belowOrEqual((Word) val);
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.BE)
    public boolean belowOrEqual(int val) {
        return belowOrEqual(intParam(val));
    }

    @Operation(opcode = Opcode.COMPARISON, condition = Condition.BE)
    public boolean belowOrEqual(Word val) {
        return UnsignedMath.belowOrEqual(unbox(), val.unbox());
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.AT)
    public boolean aboveThan(Unsigned val) {
        return aboveThan((Word) val);
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.AT)
    public boolean aboveThan(int val) {
        return aboveThan(intParam(val));
    }

    @Operation(opcode = Opcode.COMPARISON, condition = Condition.AT)
    public boolean aboveThan(Word val) {
        return UnsignedMath.aboveThan(unbox(), val.unbox());
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.AE)
    public boolean aboveOrEqual(Unsigned val) {
        return aboveOrEqual((Word) val);
    }

    @Override
    @Operation(opcode = Opcode.COMPARISON, condition = Condition.AE)
    public boolean aboveOrEqual(int val) {
        return aboveOrEqual(intParam(val));
    }

    @Operation(opcode = Opcode.COMPARISON, condition = Condition.AE)
    public boolean aboveOrEqual(Word val) {
        return UnsignedMath.aboveOrEqual(unbox(), val.unbox());
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public byte readByte(WordBase offset, LocationIdentity locationIdentity) {
        return UNSAFE.getByte(add((Word) offset).unbox());
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public char readChar(WordBase offset, LocationIdentity locationIdentity) {
        return UNSAFE.getChar(add((Word) offset).unbox());
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public short readShort(WordBase offset, LocationIdentity locationIdentity) {
        return UNSAFE.getShort(add((Word) offset).unbox());
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public int readInt(WordBase offset, LocationIdentity locationIdentity) {
        return UNSAFE.getInt(add((Word) offset).unbox());
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public long readLong(WordBase offset, LocationIdentity locationIdentity) {
        return UNSAFE.getLong(add((Word) offset).unbox());
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public float readFloat(WordBase offset, LocationIdentity locationIdentity) {
        return UNSAFE.getFloat(add((Word) offset).unbox());
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public double readDouble(WordBase offset, LocationIdentity locationIdentity) {
        return UNSAFE.getDouble(add((Word) offset).unbox());
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public Word readWord(WordBase offset, LocationIdentity locationIdentity) {
        return box(UNSAFE.getAddress(add((Word) offset).unbox()));
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public native Object readObject(WordBase offset, LocationIdentity locationIdentity);

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public byte readByte(int offset, LocationIdentity locationIdentity) {
        return readByte(signed(offset), locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public char readChar(int offset, LocationIdentity locationIdentity) {
        return readChar(signed(offset), locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public short readShort(int offset, LocationIdentity locationIdentity) {
        return readShort(signed(offset), locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public int readInt(int offset, LocationIdentity locationIdentity) {
        return readInt(signed(offset), locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public long readLong(int offset, LocationIdentity locationIdentity) {
        return readLong(signed(offset), locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public float readFloat(int offset, LocationIdentity locationIdentity) {
        return readFloat(signed(offset), locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public double readDouble(int offset, LocationIdentity locationIdentity) {
        return readDouble(signed(offset), locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public Word readWord(int offset, LocationIdentity locationIdentity) {
        return readWord(signed(offset), locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public Object readObject(int offset, LocationIdentity locationIdentity) {
        return readObject(signed(offset), locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeByte(WordBase offset, byte val, LocationIdentity locationIdentity) {
        UNSAFE.putByte(add((Word) offset).unbox(), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeChar(WordBase offset, char val, LocationIdentity locationIdentity) {
        UNSAFE.putChar(add((Word) offset).unbox(), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeShort(WordBase offset, short val, LocationIdentity locationIdentity) {
        UNSAFE.putShort(add((Word) offset).unbox(), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeInt(WordBase offset, int val, LocationIdentity locationIdentity) {
        UNSAFE.putInt(add((Word) offset).unbox(), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeLong(WordBase offset, long val, LocationIdentity locationIdentity) {
        UNSAFE.putLong(add((Word) offset).unbox(), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeFloat(WordBase offset, float val, LocationIdentity locationIdentity) {
        UNSAFE.putFloat(add((Word) offset).unbox(), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeDouble(WordBase offset, double val, LocationIdentity locationIdentity) {
        UNSAFE.putDouble(add((Word) offset).unbox(), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeWord(WordBase offset, WordBase val, LocationIdentity locationIdentity) {
        UNSAFE.putAddress(add((Word) offset).unbox(), ((Word) val).unbox());
    }

    @Override
    @Operation(opcode = Opcode.INITIALIZE)
    public void initializeLong(WordBase offset, long val, LocationIdentity locationIdentity) {
        UNSAFE.putLong(add((Word) offset).unbox(), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public native void writeObject(WordBase offset, Object val, LocationIdentity locationIdentity);

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeByte(int offset, byte val, LocationIdentity locationIdentity) {
        writeByte(signed(offset), val, locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeChar(int offset, char val, LocationIdentity locationIdentity) {
        writeChar(signed(offset), val, locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeShort(int offset, short val, LocationIdentity locationIdentity) {
        writeShort(signed(offset), val, locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeInt(int offset, int val, LocationIdentity locationIdentity) {
        writeInt(signed(offset), val, locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeLong(int offset, long val, LocationIdentity locationIdentity) {
        writeLong(signed(offset), val, locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeFloat(int offset, float val, LocationIdentity locationIdentity) {
        writeFloat(signed(offset), val, locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeDouble(int offset, double val, LocationIdentity locationIdentity) {
        writeDouble(signed(offset), val, locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeWord(int offset, WordBase val, LocationIdentity locationIdentity) {
        writeWord(signed(offset), val, locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.INITIALIZE)
    public void initializeLong(int offset, long val, LocationIdentity locationIdentity) {
        initializeLong(signed(offset), val, locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeObject(int offset, Object val, LocationIdentity locationIdentity) {
        writeObject(signed(offset), val, locationIdentity);
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public byte readByte(WordBase offset) {
        return UNSAFE.getByte(add((Word) offset).unbox());
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public char readChar(WordBase offset) {
        return UNSAFE.getChar(add((Word) offset).unbox());
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public short readShort(WordBase offset) {
        return UNSAFE.getShort(add((Word) offset).unbox());
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public int readInt(WordBase offset) {
        return UNSAFE.getInt(add((Word) offset).unbox());
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public long readLong(WordBase offset) {
        return UNSAFE.getLong(add((Word) offset).unbox());
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public float readFloat(WordBase offset) {
        return UNSAFE.getFloat(add((Word) offset).unbox());
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public double readDouble(WordBase offset) {
        return UNSAFE.getDouble(add((Word) offset).unbox());
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public Word readWord(WordBase offset) {
        return box(UNSAFE.getAddress(add((Word) offset).unbox()));
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public native Object readObject(WordBase offset);

    @Override
    @Operation(opcode = Opcode.READ_HEAP)
    public native Object readObject(WordBase offset, BarrierType barrierType);

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public byte readByte(int offset) {
        return readByte(signed(offset));
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public char readChar(int offset) {
        return readChar(signed(offset));
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public short readShort(int offset) {
        return readShort(signed(offset));
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public int readInt(int offset) {
        return readInt(signed(offset));
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public long readLong(int offset) {
        return readLong(signed(offset));
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public float readFloat(int offset) {
        return readFloat(signed(offset));
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public double readDouble(int offset) {
        return readDouble(signed(offset));
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public Word readWord(int offset) {
        return readWord(signed(offset));
    }

    @Override
    @Operation(opcode = Opcode.READ_POINTER)
    public Object readObject(int offset) {
        return readObject(signed(offset));
    }

    @Override
    @Operation(opcode = Opcode.READ_HEAP)
    public Object readObject(int offset, BarrierType barrierType) {
        return readObject(signed(offset), barrierType);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeByte(WordBase offset, byte val) {
        UNSAFE.putByte(add((Word) offset).unbox(), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeChar(WordBase offset, char val) {
        UNSAFE.putChar(add((Word) offset).unbox(), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeShort(WordBase offset, short val) {
        UNSAFE.putShort(add((Word) offset).unbox(), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeInt(WordBase offset, int val) {
        UNSAFE.putInt(add((Word) offset).unbox(), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeLong(WordBase offset, long val) {
        UNSAFE.putLong(add((Word) offset).unbox(), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeFloat(WordBase offset, float val) {
        UNSAFE.putFloat(add((Word) offset).unbox(), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeDouble(WordBase offset, double val) {
        UNSAFE.putDouble(add((Word) offset).unbox(), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeWord(WordBase offset, WordBase val) {
        UNSAFE.putAddress(add((Word) offset).unbox(), ((Word) val).unbox());
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public native void writeObject(WordBase offset, Object val);

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeByte(int offset, byte val) {
        writeByte(signed(offset), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeChar(int offset, char val) {
        writeChar(signed(offset), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeShort(int offset, short val) {
        writeShort(signed(offset), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeInt(int offset, int val) {
        writeInt(signed(offset), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeLong(int offset, long val) {
        writeLong(signed(offset), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeFloat(int offset, float val) {
        writeFloat(signed(offset), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeDouble(int offset, double val) {
        writeDouble(signed(offset), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeWord(int offset, WordBase val) {
        writeWord(signed(offset), val);
    }

    @Override
    @Operation(opcode = Opcode.WRITE_POINTER)
    public void writeObject(int offset, Object val) {
        writeObject(signed(offset), val);
    }

    @Override
    public final boolean equals(Object obj) {
        throw GraalError.shouldNotReachHere("equals must not be called on words");
    }

    @Override
    public final int hashCode() {
        throw GraalError.shouldNotReachHere("hashCode must not be called on words");
    }

    @Override
    public String toString() {
        throw GraalError.shouldNotReachHere("toString must not be called on words");
    }
}

final class HostedWord extends Word {

    private static final int SMALL_FROM = -1;

    private static final int SMALL_TO = 100;

    private static final HostedWord[] smallCache = new HostedWord[SMALL_TO - SMALL_FROM + 1];

    static {
        for (int i = SMALL_FROM; i <= SMALL_TO; i++) {
            smallCache[i - SMALL_FROM] = new HostedWord(i);
        }
    }

    private final long rawValue;

    private HostedWord(long rawValue) {
        this.rawValue = rawValue;
    }

    protected static Word boxLong(long val) {
        if (val >= SMALL_FROM && val <= SMALL_TO) {
            return smallCache[(int) val - SMALL_FROM];
        }
        return new HostedWord(val);
    }

    @Override
    protected long unbox() {
        return rawValue;
    }

    @Override
    public String toString() {
        return "Word<" + rawValue + ">";
    }
}
