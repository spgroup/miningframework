package org.graalvm.compiler.lir;

import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.COMPOSITE;
import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.CONST;
import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.HINT;
import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.ILLEGAL;
import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.OUTGOING;
import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.REG;
import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.STACK;
import static org.graalvm.compiler.lir.LIRInstruction.OperandFlag.UNINITIALIZED;
import static org.graalvm.compiler.lir.LIRInstruction.OperandMode.ALIVE;
import static org.graalvm.compiler.lir.LIRInstruction.OperandMode.DEF;
import static org.graalvm.compiler.lir.LIRInstruction.OperandMode.TEMP;
import static org.graalvm.compiler.lir.LIRValueUtil.isVirtualStackSlot;
import static jdk.vm.ci.code.ValueUtil.isStackSlot;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import org.graalvm.compiler.debug.Debug;
import org.graalvm.compiler.debug.DebugCounter;
import org.graalvm.compiler.graph.NodeSourcePosition;
import org.graalvm.compiler.lir.asm.CompilationResultBuilder;
import jdk.vm.ci.code.RegisterValue;
import jdk.vm.ci.code.StackSlot;
import jdk.vm.ci.meta.JavaConstant;
import jdk.vm.ci.meta.Value;

public abstract class LIRInstruction {

    public enum OperandMode {

        USE, ALIVE, TEMP, DEF
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface Use {

        OperandFlag[] value() default OperandFlag.REG;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface Alive {

        OperandFlag[] value() default OperandFlag.REG;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface Temp {

        OperandFlag[] value() default OperandFlag.REG;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface Def {

        OperandFlag[] value() default OperandFlag.REG;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public static @interface State {
    }

    public enum OperandFlag {

        REG,
        STACK,
        COMPOSITE,
        CONST,
        ILLEGAL,
        HINT,
        UNINITIALIZED,
        OUTGOING
    }

    protected static final EnumMap<OperandMode, EnumSet<OperandFlag>> ALLOWED_FLAGS;

    static {
        ALLOWED_FLAGS = new EnumMap<>(OperandMode.class);
        ALLOWED_FLAGS.put(OperandMode.USE, EnumSet.of(REG, STACK, COMPOSITE, CONST, ILLEGAL, HINT, UNINITIALIZED));
        ALLOWED_FLAGS.put(ALIVE, EnumSet.of(REG, STACK, COMPOSITE, CONST, ILLEGAL, HINT, UNINITIALIZED, OUTGOING));
        ALLOWED_FLAGS.put(TEMP, EnumSet.of(REG, STACK, COMPOSITE, ILLEGAL, HINT));
        ALLOWED_FLAGS.put(DEF, EnumSet.of(REG, STACK, COMPOSITE, ILLEGAL, HINT));
    }

    protected static final EnumSet<OperandFlag> ADDRESS_FLAGS = EnumSet.of(REG, ILLEGAL);

    private final LIRInstructionClass<?> instructionClass;

    private int id;

    private NodeSourcePosition position;

    private static final DebugCounter LIR_NODE_COUNT = Debug.counter("LIRNodes");

    public LIRInstruction(LIRInstructionClass<? extends LIRInstruction> c) {
        LIR_NODE_COUNT.increment();
        instructionClass = c;
        assert c.getClazz() == this.getClass();
        id = -1;
    }

    public abstract void emitCode(CompilationResultBuilder crb);

    public final int id() {
        return id;
    }

    public final void setId(int id) {
        this.id = id;
    }

    public final NodeSourcePosition getPosition() {
        return position;
    }

    public final void setPosition(NodeSourcePosition position) {
        this.position = position;
    }

    public final String name() {
        return instructionClass.getOpcode(this);
    }

    public final boolean hasOperands() {
        return instructionClass.hasOperands() || hasState() || destroysCallerSavedRegisters();
    }

    public final boolean hasState() {
        return instructionClass.hasState(this);
    }

    public boolean destroysCallerSavedRegisters() {
        return false;
    }

    public final void forEachInput(InstructionValueProcedure proc) {
        instructionClass.forEachUse(this, proc);
    }

    public final void forEachAlive(InstructionValueProcedure proc) {
        instructionClass.forEachAlive(this, proc);
    }

    public final void forEachTemp(InstructionValueProcedure proc) {
        instructionClass.forEachTemp(this, proc);
    }

    public final void forEachOutput(InstructionValueProcedure proc) {
        instructionClass.forEachDef(this, proc);
    }

    public final void forEachState(InstructionValueProcedure proc) {
        instructionClass.forEachState(this, proc);
    }

    public final void forEachInput(ValueProcedure proc) {
        instructionClass.forEachUse(this, proc);
    }

    public final void forEachAlive(ValueProcedure proc) {
        instructionClass.forEachAlive(this, proc);
    }

    public final void forEachTemp(ValueProcedure proc) {
        instructionClass.forEachTemp(this, proc);
    }

    public final void forEachOutput(ValueProcedure proc) {
        instructionClass.forEachDef(this, proc);
    }

    public final void forEachState(ValueProcedure proc) {
        instructionClass.forEachState(this, proc);
    }

    public final void forEachState(InstructionStateProcedure proc) {
        instructionClass.forEachState(this, proc);
    }

    public final void forEachState(StateProcedure proc) {
        instructionClass.forEachState(this, proc);
    }

    public final void visitEachInput(InstructionValueConsumer proc) {
        instructionClass.visitEachUse(this, proc);
    }

    public final void visitEachAlive(InstructionValueConsumer proc) {
        instructionClass.visitEachAlive(this, proc);
    }

    public final void visitEachTemp(InstructionValueConsumer proc) {
        instructionClass.visitEachTemp(this, proc);
    }

    public final void visitEachOutput(InstructionValueConsumer proc) {
        instructionClass.visitEachDef(this, proc);
    }

    public final void visitEachState(InstructionValueConsumer proc) {
        instructionClass.visitEachState(this, proc);
    }

    public final void visitEachInput(ValueConsumer proc) {
        instructionClass.visitEachUse(this, proc);
    }

    public final void visitEachAlive(ValueConsumer proc) {
        instructionClass.visitEachAlive(this, proc);
    }

    public final void visitEachTemp(ValueConsumer proc) {
        instructionClass.visitEachTemp(this, proc);
    }

    public final void visitEachOutput(ValueConsumer proc) {
        instructionClass.visitEachDef(this, proc);
    }

    public final void visitEachState(ValueConsumer proc) {
        instructionClass.visitEachState(this, proc);
    }

    @SuppressWarnings("unused")
    public final Value forEachRegisterHint(Value value, OperandMode mode, InstructionValueProcedure proc) {
        return instructionClass.forEachRegisterHint(this, mode, proc);
    }

    @SuppressWarnings("unused")
    public final Value forEachRegisterHint(Value value, OperandMode mode, ValueProcedure proc) {
        return instructionClass.forEachRegisterHint(this, mode, proc);
    }

    protected static Value[] addStackSlotsToTemporaries(Value[] parameters, Value[] temporaries) {
        int extraTemps = 0;
        for (Value p : parameters) {
            if (isStackSlot(p)) {
                extraTemps++;
            }
            assert !isVirtualStackSlot(p) : "only real stack slots in calling convention";
        }
        if (extraTemps != 0) {
            int index = temporaries.length;
            Value[] newTemporaries = Arrays.copyOf(temporaries, temporaries.length + extraTemps);
            for (Value p : parameters) {
                if (isStackSlot(p)) {
                    newTemporaries[index++] = p;
                }
            }
            return newTemporaries;
        }
        return temporaries;
    }

    public void verify() {
    }

    public final String toStringWithIdPrefix() {
        if (id != -1) {
            return String.format("%4d %s", id, toString());
        }
        return "     " + toString();
    }

    @Override
    public String toString() {
        return instructionClass.toString(this);
    }

    public LIRInstructionClass<?> getLIRInstructionClass() {
        return instructionClass;
    }
}