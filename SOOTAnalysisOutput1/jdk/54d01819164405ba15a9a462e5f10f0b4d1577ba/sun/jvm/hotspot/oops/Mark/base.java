package sun.jvm.hotspot.oops;

import java.io.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.types.*;
import sun.jvm.hotspot.utilities.*;

public class Mark extends VMObject {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("oopDesc");
        markField = type.getCIntegerField("_mark");
        ageBits = db.lookupLongConstant("markOopDesc::age_bits").longValue();
        lockBits = db.lookupLongConstant("markOopDesc::lock_bits").longValue();
        biasedLockBits = db.lookupLongConstant("markOopDesc::biased_lock_bits").longValue();
        maxHashBits = db.lookupLongConstant("markOopDesc::max_hash_bits").longValue();
        hashBits = db.lookupLongConstant("markOopDesc::hash_bits").longValue();
        lockShift = db.lookupLongConstant("markOopDesc::lock_shift").longValue();
        biasedLockShift = db.lookupLongConstant("markOopDesc::biased_lock_shift").longValue();
        ageShift = db.lookupLongConstant("markOopDesc::age_shift").longValue();
        hashShift = db.lookupLongConstant("markOopDesc::hash_shift").longValue();
        lockMask = db.lookupLongConstant("markOopDesc::lock_mask").longValue();
        lockMaskInPlace = db.lookupLongConstant("markOopDesc::lock_mask_in_place").longValue();
        biasedLockMask = db.lookupLongConstant("markOopDesc::biased_lock_mask").longValue();
        biasedLockMaskInPlace = db.lookupLongConstant("markOopDesc::biased_lock_mask_in_place").longValue();
        biasedLockBitInPlace = db.lookupLongConstant("markOopDesc::biased_lock_bit_in_place").longValue();
        ageMask = db.lookupLongConstant("markOopDesc::age_mask").longValue();
        ageMaskInPlace = db.lookupLongConstant("markOopDesc::age_mask_in_place").longValue();
        hashMask = db.lookupLongConstant("markOopDesc::hash_mask").longValue();
        hashMaskInPlace = db.lookupLongConstant("markOopDesc::hash_mask_in_place").longValue();
        biasedLockAlignment = db.lookupLongConstant("markOopDesc::biased_lock_alignment").longValue();
        lockedValue = db.lookupLongConstant("markOopDesc::locked_value").longValue();
        unlockedValue = db.lookupLongConstant("markOopDesc::unlocked_value").longValue();
        monitorValue = db.lookupLongConstant("markOopDesc::monitor_value").longValue();
        markedValue = db.lookupLongConstant("markOopDesc::marked_value").longValue();
        biasedLockPattern = db.lookupLongConstant("markOopDesc::biased_lock_pattern").longValue();
        noHash = db.lookupLongConstant("markOopDesc::no_hash").longValue();
        noHashInPlace = db.lookupLongConstant("markOopDesc::no_hash_in_place").longValue();
        noLockInPlace = db.lookupLongConstant("markOopDesc::no_lock_in_place").longValue();
        maxAge = db.lookupLongConstant("markOopDesc::max_age").longValue();
        cmsShift = db.lookupLongConstant("markOopDesc::cms_shift").longValue();
        cmsMask = db.lookupLongConstant("markOopDesc::cms_mask").longValue();
        sizeShift = db.lookupLongConstant("markOopDesc::size_shift").longValue();
    }

    private static CIntegerField markField;

    private static long ageBits;

    private static long lockBits;

    private static long biasedLockBits;

    private static long maxHashBits;

    private static long hashBits;

    private static long lockShift;

    private static long biasedLockShift;

    private static long ageShift;

    private static long hashShift;

    private static long lockMask;

    private static long lockMaskInPlace;

    private static long biasedLockMask;

    private static long biasedLockMaskInPlace;

    private static long biasedLockBitInPlace;

    private static long ageMask;

    private static long ageMaskInPlace;

    private static long hashMask;

    private static long hashMaskInPlace;

    private static long biasedLockAlignment;

    private static long lockedValue;

    private static long unlockedValue;

    private static long monitorValue;

    private static long markedValue;

    private static long biasedLockPattern;

    private static long noHash;

    private static long noHashInPlace;

    private static long noLockInPlace;

    private static long maxAge;

    private static long cmsShift;

    private static long cmsMask;

    private static long sizeShift;

    public Mark(Address addr) {
        super(addr);
    }

    public long value() {
        return markField.getValue(addr);
    }

    public Address valueAsAddress() {
        return addr.getAddressAt(markField.getOffset());
    }

    public boolean hasBiasPattern() {
        return (Bits.maskBitsLong(value(), biasedLockMaskInPlace) == biasedLockPattern);
    }

    public JavaThread biasedLocker() {
        Threads threads = VM.getVM().getThreads();
        Address addr = valueAsAddress().andWithMask(~(biasedLockMaskInPlace & ageMaskInPlace));
        return threads.createJavaThreadWrapper(addr);
    }

    public boolean isBiasedAnonymously() {
        return hasBiasPattern() && (biasedLocker() == null);
    }

    public boolean isLocked() {
        return (Bits.maskBitsLong(value(), lockMaskInPlace) != unlockedValue);
    }

    public boolean isUnlocked() {
        return (Bits.maskBitsLong(value(), biasedLockMaskInPlace) == unlockedValue);
    }

    public boolean isMarked() {
        return (Bits.maskBitsLong(value(), lockMaskInPlace) == markedValue);
    }

    public boolean isBeingInflated() {
        return (value() == 0);
    }

    public boolean mustBePreserved() {
        return (!isUnlocked() || !hasNoHash());
    }

    public boolean hasLocker() {
        return ((value() & lockMaskInPlace) == lockedValue);
    }

    public BasicLock locker() {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(hasLocker(), "check");
        }
        return new BasicLock(valueAsAddress());
    }

    public boolean hasMonitor() {
        return ((value() & monitorValue) != 0);
    }

    public ObjectMonitor monitor() {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(hasMonitor(), "check");
        }
        Address monAddr = valueAsAddress().xorWithMask(monitorValue);
        return new ObjectMonitor(monAddr);
    }

    public boolean hasDisplacedMarkHelper() {
        return ((value() & unlockedValue) == 0);
    }

    public Mark displacedMarkHelper() {
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(hasDisplacedMarkHelper(), "check");
        }
        Address addr = valueAsAddress().andWithMask(~monitorValue);
        return new Mark(addr.getAddressAt(0));
    }

    public int age() {
        return (int) Bits.maskBitsLong(value() >> ageShift, ageMask);
    }

    public long hash() {
        return Bits.maskBitsLong(value() >> hashShift, hashMask);
    }

    public boolean hasNoHash() {
        return hash() == noHash;
    }

    public void printOn(PrintStream tty) {
        if (isLocked()) {
            tty.print("locked(0x" + Long.toHexString(value()) + ")->");
            displacedMarkHelper().printOn(tty);
        } else {
            if (Assert.ASSERTS_ENABLED) {
                Assert.that(isUnlocked(), "just checking");
            }
            tty.print("mark(");
            tty.print("hash " + Long.toHexString(hash()) + ",");
            tty.print("age " + age() + ")");
        }
    }

    public boolean isCmsFreeChunk() {
        return isUnlocked() && (Bits.maskBitsLong(value() >> cmsShift, cmsMask) & 0x1L) == 0x1L;
    }

    public long getSize() {
        return (long) (value() >> sizeShift);
    }
}
