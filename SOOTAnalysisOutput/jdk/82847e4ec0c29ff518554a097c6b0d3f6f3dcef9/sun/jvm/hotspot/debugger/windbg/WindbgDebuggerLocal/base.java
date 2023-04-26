package sun.jvm.hotspot.debugger.windbg;

import java.io.*;
import java.net.*;
import java.util.*;
import sun.jvm.hotspot.debugger.*;
import sun.jvm.hotspot.debugger.amd64.*;
import sun.jvm.hotspot.debugger.x86.*;
import sun.jvm.hotspot.debugger.windbg.amd64.*;
import sun.jvm.hotspot.debugger.windbg.x86.*;
import sun.jvm.hotspot.debugger.win32.coff.*;
import sun.jvm.hotspot.debugger.cdbg.*;
import sun.jvm.hotspot.debugger.cdbg.basic.BasicDebugEvent;
import sun.jvm.hotspot.utilities.*;
import sun.jvm.hotspot.utilities.memo.*;
import sun.jvm.hotspot.runtime.*;

public class WindbgDebuggerLocal extends DebuggerBase implements WindbgDebugger {

    private PageCache cache;

    private boolean attached;

    private boolean isCore;

    private Map nameToDllMap;

    private List loadObjects;

    private CDebugger cdbg;

    private Map threadIntegerRegisterSet;

    private List threadList;

    private long ptrIDebugClient;

    private long ptrIDebugControl;

    private long ptrIDebugDataSpaces;

    private long ptrIDebugOutputCallbacks;

    private long ptrIDebugAdvanced;

    private long ptrIDebugSymbols;

    private long ptrIDebugSystemObjects;

    private WindbgThreadFactory threadFactory;

    public WindbgDebuggerLocal(MachineDescription machDesc, boolean useCache) throws DebuggerException {
        this.machDesc = machDesc;
        utils = new DebuggerUtilities(machDesc.getAddressSize(), machDesc.isBigEndian()) {

            public void checkAlignment(long address, long alignment) {
                if ((address % alignment != 0) && (alignment != 8 || address % 4 != 0)) {
                    throw new UnalignedAddressException("Trying to read at address: " + addressValueToString(address) + " with alignment: " + alignment, address);
                }
            }
        };
        String cpu = PlatformInfo.getCPU();
        if (cpu.equals("x86")) {
            threadFactory = new WindbgX86ThreadFactory(this);
        } else if (cpu.equals("amd64")) {
            threadFactory = new WindbgAMD64ThreadFactory(this);
        }
        if (useCache) {
            initCache(4096, 4096);
        }
    }

    public boolean hasProcessList() throws DebuggerException {
        return false;
    }

    public List getProcessList() throws DebuggerException {
        return null;
    }

    public synchronized void attach(int processID) throws DebuggerException {
        attachInit();
        attach0(processID);
        attached = true;
        isCore = false;
    }

    public synchronized void attach(String executableName, String coreFileName) throws DebuggerException {
        attachInit();
        attach0(executableName, coreFileName);
        attached = true;
        isCore = true;
    }

    public List getLoadObjectList() {
        requireAttach();
        return loadObjects;
    }

    public synchronized boolean detach() {
        if (!attached)
            return false;
        if (nameToDllMap != null) {
            for (Iterator iter = nameToDllMap.values().iterator(); iter.hasNext(); ) {
                DLL dll = (DLL) iter.next();
                dll.close();
            }
            nameToDllMap = null;
            loadObjects = null;
        }
        cdbg = null;
        clearCache();
        threadIntegerRegisterSet = null;
        threadList = null;
        try {
            detach0();
        } finally {
            attached = false;
            resetNativePointers();
        }
        return true;
    }

    public Address parseAddress(String addressString) throws NumberFormatException {
        return newAddress(utils.scanAddress(addressString));
    }

    public String getOS() {
        return PlatformInfo.getOS();
    }

    public String getCPU() {
        return PlatformInfo.getCPU();
    }

    public boolean hasConsole() throws DebuggerException {
        return true;
    }

    public synchronized String consoleExecuteCommand(String cmd) throws DebuggerException {
        requireAttach();
        if (!attached) {
            throw new DebuggerException("debugger not yet attached to a Dr. Watson dump!");
        }
        return consoleExecuteCommand0(cmd);
    }

    public String getConsolePrompt() throws DebuggerException {
        return "(windbg)";
    }

    public CDebugger getCDebugger() throws DebuggerException {
        if (cdbg == null) {
            cdbg = new WindbgCDebugger(this);
        }
        return cdbg;
    }

    public synchronized Address lookup(String objectName, String symbol) {
        requireAttach();
        return newAddress(lookupByName(objectName, symbol));
    }

    public synchronized OopHandle lookupOop(String objectName, String symbol) {
        Address addr = lookup(objectName, symbol);
        if (addr == null) {
            return null;
        }
        return addr.addOffsetToAsOopHandle(0);
    }

    public synchronized ClosestSymbol lookup(long address) {
        return lookupByAddress0(address);
    }

    public MachineDescription getMachineDescription() {
        return machDesc;
    }

    public ThreadProxy getThreadForIdentifierAddress(Address addr) {
        return threadFactory.createThreadWrapper(addr);
    }

    public ThreadProxy getThreadForThreadId(long handle) {
        throw new DebuggerException("Unimplemented!");
    }

    public long getThreadIdFromSysId(long sysId) throws DebuggerException {
        requireAttach();
        return getThreadIdFromSysId0(sysId);
    }

    public long readJLong(long address) throws UnmappedAddressException, UnalignedAddressException {
        checkJavaConfigured();
        utils.checkAlignment(address, jintSize);
        byte[] data = readBytes(address, jlongSize);
        return utils.dataToJLong(data, jlongSize);
    }

    public String addressValueToString(long address) {
        return utils.addressValueToString(address);
    }

    public WindbgAddress readAddress(long address) throws UnmappedAddressException, UnalignedAddressException {
        return (WindbgAddress) newAddress(readAddressValue(address));
    }

    public WindbgAddress readCompOopAddress(long address) throws UnmappedAddressException, UnalignedAddressException {
        return (WindbgAddress) newAddress(readCompOopAddressValue(address));
    }

    public WindbgAddress readCompKlassAddress(long address) throws UnmappedAddressException, UnalignedAddressException {
        return (WindbgAddress) newAddress(readCompKlassAddressValue(address));
    }

    public WindbgOopHandle readOopHandle(long address) throws UnmappedAddressException, UnalignedAddressException, NotInHeapException {
        long value = readAddressValue(address);
        return (value == 0 ? null : new WindbgOopHandle(this, value));
    }

    public WindbgOopHandle readCompOopHandle(long address) throws UnmappedAddressException, UnalignedAddressException, NotInHeapException {
        long value = readCompOopAddressValue(address);
        return (value == 0 ? null : new WindbgOopHandle(this, value));
    }

    public int getAddressSize() {
        return (int) machDesc.getAddressSize();
    }

    private synchronized void setThreadIntegerRegisterSet(long threadId, long[] regs) {
        threadIntegerRegisterSet.put(new Long(threadId), regs);
    }

    private synchronized void addThread(long sysId) {
        threadList.add(threadFactory.createThreadWrapper(sysId));
    }

    public synchronized long[] getThreadIntegerRegisterSet(long threadId) throws DebuggerException {
        requireAttach();
        return (long[]) threadIntegerRegisterSet.get(new Long(threadId));
    }

    public synchronized List getThreadList() throws DebuggerException {
        requireAttach();
        return threadList;
    }

    private String findFullPath(String file) {
        File f = new File(file);
        if (f.exists()) {
            return file;
        } else {
            file = f.getName();
            StringTokenizer st = new StringTokenizer(imagePath, File.pathSeparator);
            while (st.hasMoreTokens()) {
                f = new File(st.nextToken(), file);
                if (f.exists()) {
                    return f.getPath();
                }
            }
        }
        return null;
    }

    private synchronized void addLoadObject(String file, long size, long base) {
        String path = findFullPath(file);
        if (path != null) {
            DLL dll = null;
            if (useNativeLookup) {
                dll = new DLL(this, path, size, newAddress(base)) {

                    public ClosestSymbol closestSymbolToPC(Address pcAsAddr) {
                        long pc = getAddressValue(pcAsAddr);
                        ClosestSymbol sym = lookupByAddress0(pc);
                        if (sym == null) {
                            return super.closestSymbolToPC(pcAsAddr);
                        } else {
                            return sym;
                        }
                    }
                };
            } else {
                dll = new DLL(this, path, size, newAddress(base));
            }
            loadObjects.add(dll);
            nameToDllMap.put(new File(file).getName(), dll);
        }
    }

    public long getAddressValue(Address addr) {
        if (addr == null)
            return 0;
        return ((WindbgAddress) addr).getValue();
    }

    public Address newAddress(long value) {
        if (value == 0)
            return null;
        return new WindbgAddress(this, value);
    }

    private void checkAttached() {
        if (attached) {
            String msg = (isCore) ? "already attached to a Dr. Watson dump!" : "already attached to a process!";
            throw new DebuggerException(msg);
        }
    }

    private void requireAttach() {
        if (!attached) {
            throw new RuntimeException("not attached to a process or Dr Watson dump");
        }
    }

    private void attachInit() {
        checkAttached();
        loadObjects = new ArrayList();
        nameToDllMap = new HashMap();
        threadIntegerRegisterSet = new HashMap();
        threadList = new ArrayList();
    }

    private void resetNativePointers() {
        ptrIDebugClient = 0L;
        ptrIDebugControl = 0L;
        ptrIDebugDataSpaces = 0L;
        ptrIDebugOutputCallbacks = 0L;
        ptrIDebugAdvanced = 0L;
        ptrIDebugSymbols = 0L;
        ptrIDebugSystemObjects = 0L;
    }

    synchronized long lookupByName(String objectName, String symbol) {
        long res = 0L;
        if (useNativeLookup) {
            res = lookupByName0(objectName, symbol);
            if (res != 0L) {
                return res;
            }
        }
        DLL dll = (DLL) nameToDllMap.get(objectName);
        if (dll != null) {
            WindbgAddress addr = (WindbgAddress) dll.lookupSymbol(symbol);
            if (addr != null) {
                return addr.getValue();
            }
        }
        return 0L;
    }

    public synchronized ReadResult readBytesFromProcess(long address, long numBytes) throws UnmappedAddressException, DebuggerException {
        requireAttach();
        byte[] res = readBytesFromProcess0(address, numBytes);
        if (res != null)
            return new ReadResult(res);
        else
            return new ReadResult(address);
    }

    private DLL findDLLByName(String fullPathName) {
        for (Iterator iter = loadObjects.iterator(); iter.hasNext(); ) {
            DLL dll = (DLL) iter.next();
            if (dll.getName().equals(fullPathName)) {
                return dll;
            }
        }
        return null;
    }

    public void writeBytesToProcess(long address, long numBytes, byte[] data) throws UnmappedAddressException, DebuggerException {
        throw new DebuggerException("Unimplemented");
    }

    private static String imagePath;

    private static String symbolPath;

    private static boolean useNativeLookup;

    static {
        String dbgengPath = null;
        String dbghelpPath = null;
        String sawindbgPath = null;
        List searchList = new ArrayList();
        boolean loadLibraryDEBUG = System.getProperty("sun.jvm.hotspot.loadLibrary.DEBUG") != null;
        {
            searchList.add(System.getProperty("java.home") + File.separator + "bin");
            sawindbgPath = (String) searchList.get(0) + File.separator + "sawindbg.dll";
            String DTFWHome = System.getenv("DEBUGGINGTOOLSFORWINDOWS");
            if (DTFWHome != null) {
                searchList.add(DTFWHome);
            }
            String sysRoot = System.getenv("SYSTEMROOT");
            DTFWHome = sysRoot + File.separator + ".." + File.separator + "Program Files" + File.separator + "Debugging Tools For Windows";
            searchList.add(DTFWHome);
            String cpu = PlatformInfo.getCPU();
            if (cpu.equals("x86")) {
                searchList.add(DTFWHome + " (x86)");
            } else if (cpu.equals("amd64")) {
                searchList.add(DTFWHome + " (x64)");
            }
            searchList.add(sysRoot + File.separator + "system32");
        }
        for (int i = 0; i < searchList.size(); i++) {
            File dir = new File((String) searchList.get(i));
            if (!dir.exists()) {
                if (loadLibraryDEBUG) {
                    System.err.println("DEBUG: '" + searchList.get(i) + "': directory does not exist.");
                }
                continue;
            }
            dbgengPath = (String) searchList.get(i) + File.separator + "dbgeng.dll";
            dbghelpPath = (String) searchList.get(i) + File.separator + "dbghelp.dll";
            File feng = new File(dbgengPath);
            File fhelp = new File(dbghelpPath);
            if (feng.exists() && fhelp.exists()) {
                break;
            }
            if (feng.exists()) {
                System.err.println("WARNING: found '" + dbgengPath + "' but did not find '" + dbghelpPath + "'; ignoring '" + dbgengPath + "'.");
            } else if (fhelp.exists()) {
                System.err.println("WARNING: found '" + dbghelpPath + "' but did not find '" + dbgengPath + "'; ignoring '" + dbghelpPath + "'.");
            } else if (loadLibraryDEBUG) {
                System.err.println("DEBUG: searched '" + searchList.get(i) + "': dbgeng.dll and dbghelp.dll were not found.");
            }
            dbgengPath = null;
            dbghelpPath = null;
        }
        if (dbgengPath == null || dbghelpPath == null) {
            String mesg = null;
            if (dbgengPath == null && dbghelpPath == null) {
                mesg = "dbgeng.dll and dbghelp.dll cannot be found. ";
            } else if (dbgengPath == null) {
                mesg = "dbgeng.dll cannot be found (dbghelp.dll was found). ";
            } else {
                mesg = "dbghelp.dll cannot be found (dbgeng.dll was found). ";
            }
            throw new UnsatisfiedLinkError(mesg + "Please search microsoft.com for 'Debugging Tools For Windows', " + "and either download it to the default location, or download it " + "to a custom location and set environment variable " + "'DEBUGGINGTOOLSFORWINDOWS' to the pathname of that location.");
        }
        if (loadLibraryDEBUG) {
            System.err.println("DEBUG: loading '" + dbghelpPath + "'.");
        }
        System.load(dbghelpPath);
        if (loadLibraryDEBUG) {
            System.err.println("DEBUG: loading '" + dbgengPath + "'.");
        }
        System.load(dbgengPath);
        if (loadLibraryDEBUG) {
            System.err.println("DEBUG: loading '" + sawindbgPath + "'.");
        }
        System.load(sawindbgPath);
        imagePath = System.getProperty("sun.jvm.hotspot.debugger.windbg.imagePath");
        if (imagePath == null) {
            imagePath = System.getenv("PATH");
        }
        symbolPath = System.getProperty("sun.jvm.hotspot.debugger.windbg.symbolPath");
        if (symbolPath == null) {
            symbolPath = imagePath;
        }
        useNativeLookup = true;
        String str = System.getProperty("sun.jvm.hotspot.debugger.windbg.disableNativeLookup");
        if (str != null) {
            useNativeLookup = false;
        }
        initIDs();
    }

    private static native void initIDs();

    private native void attach0(String executableName, String coreFileName);

    private native void attach0(int processID);

    private native void detach0();

    private native byte[] readBytesFromProcess0(long address, long numBytes) throws UnmappedAddressException, DebuggerException;

    private native long getThreadIdFromSysId0(long sysId);

    private native String consoleExecuteCommand0(String cmd);

    private native long lookupByName0(String objName, String symName);

    private native ClosestSymbol lookupByAddress0(long address);

    private ClosestSymbol createClosestSymbol(String symbol, long diff) {
        return new ClosestSymbol(symbol, diff);
    }
}
