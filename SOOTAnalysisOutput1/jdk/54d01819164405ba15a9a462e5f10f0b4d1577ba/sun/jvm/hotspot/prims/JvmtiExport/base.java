package sun.jvm.hotspot.prims;

import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.runtime.*;
import sun.jvm.hotspot.oops.*;
import sun.jvm.hotspot.types.*;

public class JvmtiExport {

    static {
        VM.registerVMInitializedObserver(new Observer() {

            public void update(Observable o, Object data) {
                initialize(VM.getVM().getTypeDataBase());
            }
        });
    }

    private static synchronized void initialize(TypeDataBase db) throws WrongTypeException {
        Type type = db.lookupType("JvmtiExport");
    }

    private static CIntegerField canAccessLocalVariables;

    private static CIntegerField canHotswapOrPostBreakpoint;

    private static CIntegerField canPostOnExceptions;

    public static boolean canAccessLocalVariables() {
        return false;
    }

    public static boolean canHotswapOrPostBreakpoint() {
        return false;
    }

    public static boolean canPostOnExceptions() {
        return false;
    }
}
