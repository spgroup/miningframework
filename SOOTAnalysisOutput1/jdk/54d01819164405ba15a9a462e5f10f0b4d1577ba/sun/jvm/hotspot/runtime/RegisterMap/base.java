package sun.jvm.hotspot.runtime;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.interpreter.*;
import sun.jvm.hotspot.code.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public abstract class RegisterMap implements Cloneable {

    protected Address[] location;

    protected long[] locationValid;

    protected boolean includeArgumentOops;

    protected JavaThread thread;

    protected boolean updateMap;

    protected static int regCount;

    protected static int locationValidTypeSize;

    protected static int locationValidSize;

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static void initialize(TypeDataBase db) {
        regCount = db.lookupIntConstant("ConcreteRegisterImpl::number_of_registers").intValue();
        locationValidTypeSize = (int) db.lookupType("julong").getSize() * 8;
        locationValidSize = (regCount + locationValidTypeSize - 1) / locationValidTypeSize;
    }

    protected RegisterMap(JavaThread thread, boolean updateMap) {
        this.thread = thread;
        this.updateMap = updateMap;
        location = new Address[regCount];
        locationValid = new long[locationValidSize];
        clear();
    }

    protected RegisterMap(RegisterMap map) {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(map != null, "RegisterMap must be present");
        }
        this.thread = map.getThread();
        this.updateMap = map.getUpdateMap();
        this.includeArgumentOops = map.getIncludeArgumentOops();
        location = new Address[map.location.length];
        locationValid = new long[map.locationValid.length];
        initializeFromPD(map);
        if (updateMap) {
            for (int i = 0; i < locationValidSize; i++) {
                long bits = (!getUpdateMap()) ? 0 : map.locationValid[i];
                locationValid[i] = bits;
                int j = i * locationValidTypeSize;
                while (bits != 0) {
                    if ((bits & 1) != 0) {
                        if (Assert.ASSERTS_ENABLED) {
                            Assert.that(0 <= j && j < regCount, "range check");
                        }
                        location[j] = map.location[j];
                    }
                    bits >>>= 1;
                    j += 1;
                }
            }
        }
    }

    public abstract Object clone();

    public RegisterMap copy() {
        return (RegisterMap) clone();
    }

    public void clear() {
        setIncludeArgumentOops(true);
        if (!VM.getVM().isCore()) {
            if (updateMap) {
                for (int i = 0; i < locationValid.length; i++) {
                    locationValid[i] = 0;
                }
                clearPD();
            } else {
                initializePD();
            }
        }
    }

    public Address getLocation(VMReg reg) {
        int i = reg.getValue();
        int index = i / locationValidTypeSize;
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(0 <= i && i < regCount, "sanity check");
            Assert.that(0 <= index && index < locationValidSize, "sanity check");
        }
        if ((locationValid[index] & (1 << i % locationValidTypeSize)) != 0) {
            return location[i];
        } else {
            return getLocationPD(reg);
        }
    }

    public void setLocation(VMReg reg, Address loc) {
        int i = reg.getValue();
        int index = i / locationValidTypeSize;
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(0 <= i && i < regCount, "sanity check");
            Assert.that(0 <= index && index < locationValidSize, "sanity check");
            Assert.that(updateMap, "updating map that does not need updating");
        }
        location[i] = loc;
        locationValid[index] |= (1 << (i % locationValidTypeSize));
    }

    public boolean getIncludeArgumentOops() {
        return includeArgumentOops;
    }

    public void setIncludeArgumentOops(boolean f) {
        includeArgumentOops = f;
    }

    public JavaThread getThread() {
        return thread;
    }

    public boolean getUpdateMap() {
        return updateMap;
    }

    public void print() {
        printOn(System.out);
    }

    public void printOn(PrintStream tty) {
        tty.println("Register map");
        for (int i = 0; i < location.length; i++) {
            Address src = getLocation(new VMReg(i));
            if (src != null) {
                tty.print("  " + VMRegImpl.getRegisterName(i) + " [" + src + "] = ");
                if (src.andWithMask(VM.getVM().getAddressSize() - 1) != null) {
                    tty.print("<misaligned>");
                } else {
                    tty.print(src.getAddressAt(0));
                }
            }
        }
    }

    protected abstract void clearPD();

    protected abstract void initializePD();

    protected abstract void initializeFromPD(RegisterMap map);

    protected abstract Address getLocationPD(VMReg reg);
}
