package com.oracle.truffle.espresso.bytecode;

import static com.oracle.truffle.espresso.bytecode.Bytecodes.Flags.ASSOCIATIVE;
import static com.oracle.truffle.espresso.bytecode.Bytecodes.Flags.BRANCH;
import static com.oracle.truffle.espresso.bytecode.Bytecodes.Flags.COMMUTATIVE;
import static com.oracle.truffle.espresso.bytecode.Bytecodes.Flags.FALL_THROUGH;
import static com.oracle.truffle.espresso.bytecode.Bytecodes.Flags.FIELD_READ;
import static com.oracle.truffle.espresso.bytecode.Bytecodes.Flags.FIELD_WRITE;
import static com.oracle.truffle.espresso.bytecode.Bytecodes.Flags.INVOKE;
import static com.oracle.truffle.espresso.bytecode.Bytecodes.Flags.LOAD;
import static com.oracle.truffle.espresso.bytecode.Bytecodes.Flags.QUICKENED;
import static com.oracle.truffle.espresso.bytecode.Bytecodes.Flags.STOP;
import static com.oracle.truffle.espresso.bytecode.Bytecodes.Flags.STORE;
import static com.oracle.truffle.espresso.bytecode.Bytecodes.Flags.TRAP;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;

public final class Bytecodes {

    public static final int NOP = 0;

    public static final int ACONST_NULL = 1;

    public static final int ICONST_M1 = 2;

    public static final int ICONST_0 = 3;

    public static final int ICONST_1 = 4;

    public static final int ICONST_2 = 5;

    public static final int ICONST_3 = 6;

    public static final int ICONST_4 = 7;

    public static final int ICONST_5 = 8;

    public static final int LCONST_0 = 9;

    public static final int LCONST_1 = 10;

    public static final int FCONST_0 = 11;

    public static final int FCONST_1 = 12;

    public static final int FCONST_2 = 13;

    public static final int DCONST_0 = 14;

    public static final int DCONST_1 = 15;

    public static final int BIPUSH = 16;

    public static final int SIPUSH = 17;

    public static final int LDC = 18;

    public static final int LDC_W = 19;

    public static final int LDC2_W = 20;

    public static final int ILOAD = 21;

    public static final int LLOAD = 22;

    public static final int FLOAD = 23;

    public static final int DLOAD = 24;

    public static final int ALOAD = 25;

    public static final int ILOAD_0 = 26;

    public static final int ILOAD_1 = 27;

    public static final int ILOAD_2 = 28;

    public static final int ILOAD_3 = 29;

    public static final int LLOAD_0 = 30;

    public static final int LLOAD_1 = 31;

    public static final int LLOAD_2 = 32;

    public static final int LLOAD_3 = 33;

    public static final int FLOAD_0 = 34;

    public static final int FLOAD_1 = 35;

    public static final int FLOAD_2 = 36;

    public static final int FLOAD_3 = 37;

    public static final int DLOAD_0 = 38;

    public static final int DLOAD_1 = 39;

    public static final int DLOAD_2 = 40;

    public static final int DLOAD_3 = 41;

    public static final int ALOAD_0 = 42;

    public static final int ALOAD_1 = 43;

    public static final int ALOAD_2 = 44;

    public static final int ALOAD_3 = 45;

    public static final int IALOAD = 46;

    public static final int LALOAD = 47;

    public static final int FALOAD = 48;

    public static final int DALOAD = 49;

    public static final int AALOAD = 50;

    public static final int BALOAD = 51;

    public static final int CALOAD = 52;

    public static final int SALOAD = 53;

    public static final int ISTORE = 54;

    public static final int LSTORE = 55;

    public static final int FSTORE = 56;

    public static final int DSTORE = 57;

    public static final int ASTORE = 58;

    public static final int ISTORE_0 = 59;

    public static final int ISTORE_1 = 60;

    public static final int ISTORE_2 = 61;

    public static final int ISTORE_3 = 62;

    public static final int LSTORE_0 = 63;

    public static final int LSTORE_1 = 64;

    public static final int LSTORE_2 = 65;

    public static final int LSTORE_3 = 66;

    public static final int FSTORE_0 = 67;

    public static final int FSTORE_1 = 68;

    public static final int FSTORE_2 = 69;

    public static final int FSTORE_3 = 70;

    public static final int DSTORE_0 = 71;

    public static final int DSTORE_1 = 72;

    public static final int DSTORE_2 = 73;

    public static final int DSTORE_3 = 74;

    public static final int ASTORE_0 = 75;

    public static final int ASTORE_1 = 76;

    public static final int ASTORE_2 = 77;

    public static final int ASTORE_3 = 78;

    public static final int IASTORE = 79;

    public static final int LASTORE = 80;

    public static final int FASTORE = 81;

    public static final int DASTORE = 82;

    public static final int AASTORE = 83;

    public static final int BASTORE = 84;

    public static final int CASTORE = 85;

    public static final int SASTORE = 86;

    public static final int POP = 87;

    public static final int POP2 = 88;

    public static final int DUP = 89;

    public static final int DUP_X1 = 90;

    public static final int DUP_X2 = 91;

    public static final int DUP2 = 92;

    public static final int DUP2_X1 = 93;

    public static final int DUP2_X2 = 94;

    public static final int SWAP = 95;

    public static final int IADD = 96;

    public static final int LADD = 97;

    public static final int FADD = 98;

    public static final int DADD = 99;

    public static final int ISUB = 100;

    public static final int LSUB = 101;

    public static final int FSUB = 102;

    public static final int DSUB = 103;

    public static final int IMUL = 104;

    public static final int LMUL = 105;

    public static final int FMUL = 106;

    public static final int DMUL = 107;

    public static final int IDIV = 108;

    public static final int LDIV = 109;

    public static final int FDIV = 110;

    public static final int DDIV = 111;

    public static final int IREM = 112;

    public static final int LREM = 113;

    public static final int FREM = 114;

    public static final int DREM = 115;

    public static final int INEG = 116;

    public static final int LNEG = 117;

    public static final int FNEG = 118;

    public static final int DNEG = 119;

    public static final int ISHL = 120;

    public static final int LSHL = 121;

    public static final int ISHR = 122;

    public static final int LSHR = 123;

    public static final int IUSHR = 124;

    public static final int LUSHR = 125;

    public static final int IAND = 126;

    public static final int LAND = 127;

    public static final int IOR = 128;

    public static final int LOR = 129;

    public static final int IXOR = 130;

    public static final int LXOR = 131;

    public static final int IINC = 132;

    public static final int I2L = 133;

    public static final int I2F = 134;

    public static final int I2D = 135;

    public static final int L2I = 136;

    public static final int L2F = 137;

    public static final int L2D = 138;

    public static final int F2I = 139;

    public static final int F2L = 140;

    public static final int F2D = 141;

    public static final int D2I = 142;

    public static final int D2L = 143;

    public static final int D2F = 144;

    public static final int I2B = 145;

    public static final int I2C = 146;

    public static final int I2S = 147;

    public static final int LCMP = 148;

    public static final int FCMPL = 149;

    public static final int FCMPG = 150;

    public static final int DCMPL = 151;

    public static final int DCMPG = 152;

    public static final int IFEQ = 153;

    public static final int IFNE = 154;

    public static final int IFLT = 155;

    public static final int IFGE = 156;

    public static final int IFGT = 157;

    public static final int IFLE = 158;

    public static final int IF_ICMPEQ = 159;

    public static final int IF_ICMPNE = 160;

    public static final int IF_ICMPLT = 161;

    public static final int IF_ICMPGE = 162;

    public static final int IF_ICMPGT = 163;

    public static final int IF_ICMPLE = 164;

    public static final int IF_ACMPEQ = 165;

    public static final int IF_ACMPNE = 166;

    public static final int GOTO = 167;

    public static final int JSR = 168;

    public static final int RET = 169;

    public static final int TABLESWITCH = 170;

    public static final int LOOKUPSWITCH = 171;

    public static final int IRETURN = 172;

    public static final int LRETURN = 173;

    public static final int FRETURN = 174;

    public static final int DRETURN = 175;

    public static final int ARETURN = 176;

    public static final int RETURN = 177;

    public static final int GETSTATIC = 178;

    public static final int PUTSTATIC = 179;

    public static final int GETFIELD = 180;

    public static final int PUTFIELD = 181;

    public static final int INVOKEVIRTUAL = 182;

    public static final int INVOKESPECIAL = 183;

    public static final int INVOKESTATIC = 184;

    public static final int INVOKEINTERFACE = 185;

    public static final int INVOKEDYNAMIC = 186;

    public static final int NEW = 187;

    public static final int NEWARRAY = 188;

    public static final int ANEWARRAY = 189;

    public static final int ARRAYLENGTH = 190;

    public static final int ATHROW = 191;

    public static final int CHECKCAST = 192;

    public static final int INSTANCEOF = 193;

    public static final int MONITORENTER = 194;

    public static final int MONITOREXIT = 195;

    public static final int WIDE = 196;

    public static final int MULTIANEWARRAY = 197;

    public static final int IFNULL = 198;

    public static final int IFNONNULL = 199;

    public static final int GOTO_W = 200;

    public static final int JSR_W = 201;

    public static final int BREAKPOINT = 202;

    public static final int QUICK = 203;

    public static final int ILLEGAL = 255;

    public static final int END = 256;

    public static final int LAST_JVM_OPCODE = JSR_W;

    static class Flags {

        static final int STOP = 0x00000001;

        static final int FALL_THROUGH = 0x00000002;

        static final int BRANCH = 0x00000004;

        static final int FIELD_READ = 0x00000008;

        static final int FIELD_WRITE = 0x00000010;

        static final int TRAP = 0x00000080;

        static final int COMMUTATIVE = 0x00000100;

        static final int ASSOCIATIVE = 0x00000200;

        static final int LOAD = 0x00000400;

        static final int STORE = 0x00000800;

        static final int INVOKE = 0x00001000;

        static final int QUICKENED = 0x00002000;
    }

    static {
        int allFlags = 0;
        try {
            for (Field field : Flags.class.getDeclaredFields()) {
                int flagsFilter = Modifier.FINAL | Modifier.STATIC;
                if ((field.getModifiers() & flagsFilter) == flagsFilter && !field.isSynthetic()) {
                    assert field.getType() == int.class : "Field is not int : " + field;
                    final int flag = field.getInt(null);
                    assert flag != 0;
                    assert (flag & allFlags) == 0 : field.getName() + " has a value conflicting with another flag";
                    allFlags |= flag;
                }
            }
        } catch (Exception e) {
            throw new InternalError(e.toString());
        }
    }

    @CompilationFinal(dimensions = 1)
    private static final String[] nameArray = new String[256];

    @CompilationFinal(dimensions = 1)
    private static final int[] flagsArray = new int[256];

    @CompilationFinal(dimensions = 1)
    private static final int[] lengthArray = new int[256];

    @CompilationFinal(dimensions = 1)
    private static final int[] stackEffectArray = new int[256];

    static {
        def(NOP, "nop", "b", 0);
        def(ACONST_NULL, "aconst_null", "b", 1);
        def(ICONST_M1, "iconst_m1", "b", 1);
        def(ICONST_0, "iconst_0", "b", 1);
        def(ICONST_1, "iconst_1", "b", 1);
        def(ICONST_2, "iconst_2", "b", 1);
        def(ICONST_3, "iconst_3", "b", 1);
        def(ICONST_4, "iconst_4", "b", 1);
        def(ICONST_5, "iconst_5", "b", 1);
        def(LCONST_0, "lconst_0", "b", 2);
        def(LCONST_1, "lconst_1", "b", 2);
        def(FCONST_0, "fconst_0", "b", 1);
        def(FCONST_1, "fconst_1", "b", 1);
        def(FCONST_2, "fconst_2", "b", 1);
        def(DCONST_0, "dconst_0", "b", 2);
        def(DCONST_1, "dconst_1", "b", 2);
        def(BIPUSH, "bipush", "bc", 1);
        def(SIPUSH, "sipush", "bcc", 1);
        def(LDC, "ldc", "bi", 1, TRAP);
        def(LDC_W, "ldc_w", "bii", 1, TRAP);
        def(LDC2_W, "ldc2_w", "bii", 2, TRAP);
        def(ILOAD, "iload", "bi", 1, LOAD);
        def(LLOAD, "lload", "bi", 2, LOAD);
        def(FLOAD, "fload", "bi", 1, LOAD);
        def(DLOAD, "dload", "bi", 2, LOAD);
        def(ALOAD, "aload", "bi", 1, LOAD);
        def(ILOAD_0, "iload_0", "b", 1, LOAD);
        def(ILOAD_1, "iload_1", "b", 1, LOAD);
        def(ILOAD_2, "iload_2", "b", 1, LOAD);
        def(ILOAD_3, "iload_3", "b", 1, LOAD);
        def(LLOAD_0, "lload_0", "b", 2, LOAD);
        def(LLOAD_1, "lload_1", "b", 2, LOAD);
        def(LLOAD_2, "lload_2", "b", 2, LOAD);
        def(LLOAD_3, "lload_3", "b", 2, LOAD);
        def(FLOAD_0, "fload_0", "b", 1, LOAD);
        def(FLOAD_1, "fload_1", "b", 1, LOAD);
        def(FLOAD_2, "fload_2", "b", 1, LOAD);
        def(FLOAD_3, "fload_3", "b", 1, LOAD);
        def(DLOAD_0, "dload_0", "b", 2, LOAD);
        def(DLOAD_1, "dload_1", "b", 2, LOAD);
        def(DLOAD_2, "dload_2", "b", 2, LOAD);
        def(DLOAD_3, "dload_3", "b", 2, LOAD);
        def(ALOAD_0, "aload_0", "b", 1, LOAD);
        def(ALOAD_1, "aload_1", "b", 1, LOAD);
        def(ALOAD_2, "aload_2", "b", 1, LOAD);
        def(ALOAD_3, "aload_3", "b", 1, LOAD);
        def(IALOAD, "iaload", "b", -1, TRAP);
        def(LALOAD, "laload", "b", 0, TRAP);
        def(FALOAD, "faload", "b", -1, TRAP);
        def(DALOAD, "daload", "b", 0, TRAP);
        def(AALOAD, "aaload", "b", -1, TRAP);
        def(BALOAD, "baload", "b", -1, TRAP);
        def(CALOAD, "caload", "b", -1, TRAP);
        def(SALOAD, "saload", "b", -1, TRAP);
        def(ISTORE, "istore", "bi", -1, STORE);
        def(LSTORE, "lstore", "bi", -2, STORE);
        def(FSTORE, "fstore", "bi", -1, STORE);
        def(DSTORE, "dstore", "bi", -2, STORE);
        def(ASTORE, "astore", "bi", -1, STORE);
        def(ISTORE_0, "istore_0", "b", -1, STORE);
        def(ISTORE_1, "istore_1", "b", -1, STORE);
        def(ISTORE_2, "istore_2", "b", -1, STORE);
        def(ISTORE_3, "istore_3", "b", -1, STORE);
        def(LSTORE_0, "lstore_0", "b", -2, STORE);
        def(LSTORE_1, "lstore_1", "b", -2, STORE);
        def(LSTORE_2, "lstore_2", "b", -2, STORE);
        def(LSTORE_3, "lstore_3", "b", -2, STORE);
        def(FSTORE_0, "fstore_0", "b", -1, STORE);
        def(FSTORE_1, "fstore_1", "b", -1, STORE);
        def(FSTORE_2, "fstore_2", "b", -1, STORE);
        def(FSTORE_3, "fstore_3", "b", -1, STORE);
        def(DSTORE_0, "dstore_0", "b", -2, STORE);
        def(DSTORE_1, "dstore_1", "b", -2, STORE);
        def(DSTORE_2, "dstore_2", "b", -2, STORE);
        def(DSTORE_3, "dstore_3", "b", -2, STORE);
        def(ASTORE_0, "astore_0", "b", -1, STORE);
        def(ASTORE_1, "astore_1", "b", -1, STORE);
        def(ASTORE_2, "astore_2", "b", -1, STORE);
        def(ASTORE_3, "astore_3", "b", -1, STORE);
        def(IASTORE, "iastore", "b", -3, TRAP);
        def(LASTORE, "lastore", "b", -4, TRAP);
        def(FASTORE, "fastore", "b", -3, TRAP);
        def(DASTORE, "dastore", "b", -4, TRAP);
        def(AASTORE, "aastore", "b", -3, TRAP);
        def(BASTORE, "bastore", "b", -3, TRAP);
        def(CASTORE, "castore", "b", -3, TRAP);
        def(SASTORE, "sastore", "b", -3, TRAP);
        def(POP, "pop", "b", -1);
        def(POP2, "pop2", "b", -2);
        def(DUP, "dup", "b", 1);
        def(DUP_X1, "dup_x1", "b", 1);
        def(DUP_X2, "dup_x2", "b", 1);
        def(DUP2, "dup2", "b", 2);
        def(DUP2_X1, "dup2_x1", "b", 2);
        def(DUP2_X2, "dup2_x2", "b", 2);
        def(SWAP, "swap", "b", 0);
        def(IADD, "iadd", "b", -1, COMMUTATIVE | ASSOCIATIVE);
        def(LADD, "ladd", "b", -2, COMMUTATIVE | ASSOCIATIVE);
        def(FADD, "fadd", "b", -1, COMMUTATIVE | ASSOCIATIVE);
        def(DADD, "dadd", "b", -2, COMMUTATIVE | ASSOCIATIVE);
        def(ISUB, "isub", "b", -1);
        def(LSUB, "lsub", "b", -2);
        def(FSUB, "fsub", "b", -1);
        def(DSUB, "dsub", "b", -2);
        def(IMUL, "imul", "b", -1, COMMUTATIVE | ASSOCIATIVE);
        def(LMUL, "lmul", "b", -2, COMMUTATIVE | ASSOCIATIVE);
        def(FMUL, "fmul", "b", -1, COMMUTATIVE | ASSOCIATIVE);
        def(DMUL, "dmul", "b", -2, COMMUTATIVE | ASSOCIATIVE);
        def(IDIV, "idiv", "b", -1, TRAP);
        def(LDIV, "ldiv", "b", -2, TRAP);
        def(FDIV, "fdiv", "b", -1);
        def(DDIV, "ddiv", "b", -2);
        def(IREM, "irem", "b", -1, TRAP);
        def(LREM, "lrem", "b", -2, TRAP);
        def(FREM, "frem", "b", -1);
        def(DREM, "drem", "b", -2);
        def(INEG, "ineg", "b", 0);
        def(LNEG, "lneg", "b", 0);
        def(FNEG, "fneg", "b", 0);
        def(DNEG, "dneg", "b", 0);
        def(ISHL, "ishl", "b", -1);
        def(LSHL, "lshl", "b", -1);
        def(ISHR, "ishr", "b", -1);
        def(LSHR, "lshr", "b", -1);
        def(IUSHR, "iushr", "b", -1);
        def(LUSHR, "lushr", "b", -1);
        def(IAND, "iand", "b", -1, COMMUTATIVE | ASSOCIATIVE);
        def(LAND, "land", "b", -2, COMMUTATIVE | ASSOCIATIVE);
        def(IOR, "ior", "b", -1, COMMUTATIVE | ASSOCIATIVE);
        def(LOR, "lor", "b", -2, COMMUTATIVE | ASSOCIATIVE);
        def(IXOR, "ixor", "b", -1, COMMUTATIVE | ASSOCIATIVE);
        def(LXOR, "lxor", "b", -2, COMMUTATIVE | ASSOCIATIVE);
        def(IINC, "iinc", "bic", 0, LOAD | STORE);
        def(I2L, "i2l", "b", 1);
        def(I2F, "i2f", "b", 0);
        def(I2D, "i2d", "b", 1);
        def(L2I, "l2i", "b", -1);
        def(L2F, "l2f", "b", -1);
        def(L2D, "l2d", "b", 0);
        def(F2I, "f2i", "b", 0);
        def(F2L, "f2l", "b", 1);
        def(F2D, "f2d", "b", 1);
        def(D2I, "d2i", "b", -1);
        def(D2L, "d2l", "b", 0);
        def(D2F, "d2f", "b", -1);
        def(I2B, "i2b", "b", 0);
        def(I2C, "i2c", "b", 0);
        def(I2S, "i2s", "b", 0);
        def(LCMP, "lcmp", "b", -3);
        def(FCMPL, "fcmpl", "b", -1);
        def(FCMPG, "fcmpg", "b", -1);
        def(DCMPL, "dcmpl", "b", -3);
        def(DCMPG, "dcmpg", "b", -3);
        def(IFEQ, "ifeq", "boo", -1, FALL_THROUGH | BRANCH);
        def(IFNE, "ifne", "boo", -1, FALL_THROUGH | BRANCH);
        def(IFLT, "iflt", "boo", -1, FALL_THROUGH | BRANCH);
        def(IFGE, "ifge", "boo", -1, FALL_THROUGH | BRANCH);
        def(IFGT, "ifgt", "boo", -1, FALL_THROUGH | BRANCH);
        def(IFLE, "ifle", "boo", -1, FALL_THROUGH | BRANCH);
        def(IF_ICMPEQ, "if_icmpeq", "boo", -2, COMMUTATIVE | FALL_THROUGH | BRANCH);
        def(IF_ICMPNE, "if_icmpne", "boo", -2, COMMUTATIVE | FALL_THROUGH | BRANCH);
        def(IF_ICMPLT, "if_icmplt", "boo", -2, FALL_THROUGH | BRANCH);
        def(IF_ICMPGE, "if_icmpge", "boo", -2, FALL_THROUGH | BRANCH);
        def(IF_ICMPGT, "if_icmpgt", "boo", -2, FALL_THROUGH | BRANCH);
        def(IF_ICMPLE, "if_icmple", "boo", -2, FALL_THROUGH | BRANCH);
        def(IF_ACMPEQ, "if_acmpeq", "boo", -2, COMMUTATIVE | FALL_THROUGH | BRANCH);
        def(IF_ACMPNE, "if_acmpne", "boo", -2, COMMUTATIVE | FALL_THROUGH | BRANCH);
        def(GOTO, "goto", "boo", 0, STOP | BRANCH);
        def(JSR, "jsr", "boo", 1, STOP | BRANCH);
        def(RET, "ret", "bi", 0, STOP);
        def(TABLESWITCH, "tableswitch", "", -1, STOP);
        def(LOOKUPSWITCH, "lookupswitch", "", -1, STOP);
        def(IRETURN, "ireturn", "b", -1, TRAP | STOP);
        def(LRETURN, "lreturn", "b", -2, TRAP | STOP);
        def(FRETURN, "freturn", "b", -1, TRAP | STOP);
        def(DRETURN, "dreturn", "b", -2, TRAP | STOP);
        def(ARETURN, "areturn", "b", -1, TRAP | STOP);
        def(RETURN, "return", "b", 0, TRAP | STOP);
        def(GETSTATIC, "getstatic", "bjj", 0, TRAP | FIELD_READ);
        def(PUTSTATIC, "putstatic", "bjj", 0, TRAP | FIELD_WRITE);
        def(GETFIELD, "getfield", "bjj", -1, TRAP | FIELD_READ);
        def(PUTFIELD, "putfield", "bjj", -1, TRAP | FIELD_WRITE);
        def(INVOKEVIRTUAL, "invokevirtual", "bjj", -1, TRAP | INVOKE);
        def(INVOKESPECIAL, "invokespecial", "bjj", -1, TRAP | INVOKE);
        def(INVOKESTATIC, "invokestatic", "bjj", 0, TRAP | INVOKE);
        def(INVOKEINTERFACE, "invokeinterface", "bjja_", -1, TRAP | INVOKE);
        def(INVOKEDYNAMIC, "invokedynamic", "bjjjj", 0, TRAP | INVOKE);
        def(NEW, "new", "bii", 1, TRAP);
        def(NEWARRAY, "newarray", "bc", 0, TRAP);
        def(ANEWARRAY, "anewarray", "bii", 0, TRAP);
        def(ARRAYLENGTH, "arraylength", "b", 0, TRAP);
        def(ATHROW, "athrow", "b", -1, TRAP | STOP);
        def(CHECKCAST, "checkcast", "bii", 0, TRAP);
        def(INSTANCEOF, "instanceof", "bii", 0, TRAP);
        def(MONITORENTER, "monitorenter", "b", -1, TRAP);
        def(MONITOREXIT, "monitorexit", "b", -1, TRAP);
        def(WIDE, "wide", "", 0);
        def(MULTIANEWARRAY, "multianewarray", "biic", 1, TRAP);
        def(IFNULL, "ifnull", "boo", -1, FALL_THROUGH | BRANCH);
        def(IFNONNULL, "ifnonnull", "boo", -1, FALL_THROUGH | BRANCH);
        def(GOTO_W, "goto_w", "boooo", 0, STOP | BRANCH);
        def(JSR_W, "jsr_w", "boooo", 1, STOP | BRANCH);
        def(BREAKPOINT, "breakpoint", "b", 0, TRAP);
        def(QUICK, "quick", "bjj", 0, QUICKENED);
    }

    public static boolean isCommutative(int opcode) {
        return (flagsArray[opcode & 0xff] & COMMUTATIVE) != 0;
    }

    public static int lengthOf(int opcode) {
        return lengthArray[opcode & 0xff];
    }

    public static int stackEffectOf(int opcode) {
        return stackEffectArray[opcode & 0xff];
    }

    public static String nameOf(int opcode) throws IllegalArgumentException {
        String name = nameArray[opcode & 0xff];
        if (name == null) {
            return "<illegal opcode: " + opcode + ">";
        }
        return name;
    }

    public static String baseNameOf(int opcode) {
        String name = nameArray[opcode & 0xff];
        if (name == null) {
            return "<illegal opcode>";
        }
        return name;
    }

    public static int valueOf(String name) {
        for (int opcode = 0; opcode < nameArray.length; ++opcode) {
            if (name.equalsIgnoreCase(nameArray[opcode])) {
                return opcode;
            }
        }
        throw new IllegalArgumentException("No opcode for " + name);
    }

    public static boolean canTrap(int opcode) {
        return (flagsArray[opcode & 0xff] & TRAP) != 0;
    }

    public static boolean isLoad(int opcode) {
        return (flagsArray[opcode & 0xff] & LOAD) != 0;
    }

    public static boolean isStop(int opcode) {
        return (flagsArray[opcode & 0xff] & STOP) != 0;
    }

    public static boolean isInvoke(int opcode) {
        return (flagsArray[opcode & 0xff] & INVOKE) != 0;
    }

    public static boolean isQuickened(int opcode) {
        return (flagsArray[opcode & 0xff] & QUICKENED) != 0;
    }

    public static boolean isStore(int opcode) {
        return (flagsArray[opcode & 0xff] & STORE) != 0;
    }

    public static boolean isBlockEnd(int opcode) {
        return (flagsArray[opcode & 0xff] & (STOP | FALL_THROUGH)) != 0;
    }

    public static boolean isBranch(int opcode) {
        return (flagsArray[opcode & 0xff] & BRANCH) != 0;
    }

    public static boolean isConditionalBranch(int opcode) {
        return (flagsArray[opcode & 0xff] & FALL_THROUGH) != 0;
    }

    public static String operator(int op) {
        switch(op) {
            case IADD:
            case LADD:
            case FADD:
            case DADD:
                return "+";
            case ISUB:
            case LSUB:
            case FSUB:
            case DSUB:
                return "-";
            case IMUL:
            case LMUL:
            case FMUL:
            case DMUL:
                return "*";
            case IDIV:
            case LDIV:
            case FDIV:
            case DDIV:
                return "/";
            case IREM:
            case LREM:
            case FREM:
            case DREM:
                return "%";
            case ISHL:
            case LSHL:
                return "<<";
            case ISHR:
            case LSHR:
                return ">>";
            case IUSHR:
            case LUSHR:
                return ">>>";
            case IAND:
            case LAND:
                return "&";
            case IOR:
            case LOR:
                return "|";
            case IXOR:
            case LXOR:
                return "^";
        }
        return nameOf(op);
    }

    private static void def(int opcode, String name, String format, int stackEffect) {
        def(opcode, name, format, stackEffect, 0);
    }

    private static void def(int opcode, String name, String format, int stackEffect, int flags) {
        assert nameArray[opcode] == null : "opcode " + opcode + " is already bound to name " + nameArray[opcode];
        nameArray[opcode] = name;
        int instructionLength = format.length();
        lengthArray[opcode] = instructionLength;
        stackEffectArray[opcode] = stackEffect;
        Bytecodes.flagsArray[opcode] = flags;
        assert !isConditionalBranch(opcode) || isBranch(opcode) : "a conditional branch must also be a branch";
    }

    public static boolean isIfBytecode(int bytecode) {
        switch(bytecode) {
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case IFNULL:
            case IFNONNULL:
                return true;
        }
        return false;
    }
}
