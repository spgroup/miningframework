package sun.jvm.hotspot.runtime.sparc;

import java.util.*;
import sun.jvm.hotspot.asm.sparc.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class SPARCRegisterMap extends RegisterMap {

    private Address window;

    private Address youngerWindow;

    private static int registerImplNumberOfRegisters;

    private static int[] R_L_nums = new int[] { 0 + 040, 2 + 040, 4 + 040, 6 + 040, 8 + 040, 10 + 040, 12 + 040, 14 + 040 };

    private static int[] R_I_nums = new int[] { 0 + 060, 2 + 060, 4 + 060, 6 + 060, 8 + 060, 10 + 060, 12 + 060, 14 + 060 };

    private static int[] R_O_nums = new int[] { 0 + 020, 2 + 020, 4 + 020, 6 + 020, 8 + 020, 10 + 020, 12 + 020, 14 + 020 };

    private static int[] R_G_nums = new int[] { 0 + 000, 2 + 000, 4 + 000, 6 + 000, 8 + 000, 10 + 000, 12 + 000, 14 + 000 };

    private static long badMask;

    private static long R_LIO_mask;

    private static int sizeofJint;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static void initialize(TypeDataBase db) {
        badMask = 0;
        R_LIO_mask = 0;
        sizeofJint = (int) db.lookupType("jint").getSize();
        registerImplNumberOfRegisters = db.lookupIntConstant("RegisterImpl::number_of_registers").intValue();
        for (int i = 0; i < 8; i++) {
            Assert.that(R_L_nums[i] < locationValidTypeSize, "in first chunk");
            Assert.that(R_I_nums[i] < locationValidTypeSize, "in first chunk");
            Assert.that(R_O_nums[i] < locationValidTypeSize, "in first chunk");
            Assert.that(R_G_nums[i] < locationValidTypeSize, "in first chunk");
        }
        badMask |= ((long) 1 << R_O_nums[6]);
        badMask |= ((long) 1 << R_O_nums[7]);
        badMask |= ((long) 1 << R_I_nums[6]);
        badMask |= ((long) 1 << R_I_nums[7]);
        badMask |= ((long) 1 << R_G_nums[2]);
        badMask |= ((long) 1 << R_G_nums[7]);
        for (int i = 0; i < 8; i++) {
            R_LIO_mask |= ((long) 1 << R_L_nums[i]);
            R_LIO_mask |= ((long) 1 << R_I_nums[i]);
            R_LIO_mask |= ((long) 1 << R_O_nums[i]);
        }
    }

    public SPARCRegisterMap(JavaThread thread, boolean updateMap) {
        super(thread, updateMap);
    }

    protected SPARCRegisterMap(RegisterMap map) {
        super(map);
    }

    public Object clone() {
        SPARCRegisterMap retval = new SPARCRegisterMap(this);
        return retval;
    }

    protected void clearPD() {
        if (thread.hasLastJavaFrame()) {
            Frame fr = thread.getLastFrame();
            window = fr.getSP();
        } else {
            window = null;
            if (VM.getVM().isDebugging()) {
                Frame fr = thread.getCurrentFrameGuess();
                if (fr != null) {
                    window = fr.getSP();
                }
            }
        }
        youngerWindow = null;
    }

    protected Address getLocationPD(VMReg vmreg) {
        VM vm = VM.getVM();
        int regname = vmreg.getValue();
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(0 <= regname && regname < regCount, "sanity check");
        }
        if (regname >= (registerImplNumberOfRegisters << 1)) {
            return null;
        }
        if ((badMask & ((long) 1 << regname)) != 0) {
            return null;
        }
        int secondWord = 0;
        if (!isEven(regname)) {
            if (vm.isLP64()) {
                secondWord = sizeofJint;
            } else {
                return null;
            }
        }
        SPARCRegister reg = new SPARCRegister(regname >> 1);
        if (reg.isOut()) {
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(youngerWindow != null, "Younger window should be available");
            }
            return youngerWindow.addOffsetTo(reg.afterSave().spOffsetInSavedWindow() + secondWord);
        }
        if (reg.isLocal() || reg.isIn()) {
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(window != null, "Window should be available");
            }
            return window.addOffsetTo(reg.spOffsetInSavedWindow() + secondWord);
        }
        return null;
    }

    protected void initializePD() {
        window = null;
        youngerWindow = null;
        makeIntegerRegsUnsaved();
    }

    protected void initializeFromPD(RegisterMap map) {
        SPARCRegisterMap srm = (SPARCRegisterMap) map;
        window = srm.window;
        youngerWindow = srm.youngerWindow;
        makeIntegerRegsUnsaved();
    }

    public void shiftWindow(Address sp, Address youngerSP) {
        window = sp;
        youngerWindow = youngerSP;
        if (locationValid[0] != 0) {
            shiftIndividualRegisters();
        }
    }

    public void makeIntegerRegsUnsaved() {
        locationValid[0] = 0;
    }

    private void shiftIndividualRegisters() {
        if (!getUpdateMap()) {
            return;
        }
        checkLocationValid();
        long lv = locationValid[0];
        long lv0 = lv;
        lv &= ~R_LIO_mask;
        if (lv != lv0) {
            for (int i = 0; i < 8; i++) {
                if ((lv0 & ((long) 1 << R_I_nums[i])) != 0) {
                    location[R_O_nums[i]] = location[R_I_nums[i]];
                    lv |= ((long) 1 << R_O_nums[i]);
                }
            }
        }
        locationValid[0] = lv;
        checkLocationValid();
    }

    private boolean isEven(int i) {
        return (i & 1) == 0;
    }

    private void checkLocationValid() {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that((locationValid[0] & badMask) == 0, "cannot have special locations for SP,FP,TLS,etc.");
        }
    }
}
