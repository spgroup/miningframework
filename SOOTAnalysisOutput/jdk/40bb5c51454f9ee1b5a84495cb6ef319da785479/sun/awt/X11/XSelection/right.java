package sun.awt.X11;

import java.awt.datatransfer.Transferable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import sun.awt.AppContext;
import sun.awt.SunToolkit;
import sun.awt.UNIXToolkit;
import sun.awt.datatransfer.DataTransferer;

public final class XSelection {

    private static final Hashtable<XAtom, XSelection> table = new Hashtable<XAtom, XSelection>();

    private static final Object lock = new Object();

    private static final XAtom selectionPropertyAtom = XAtom.get("XAWT_SELECTION");

    public static final long MAX_LENGTH = 1000000;

    public static final int MAX_PROPERTY_SIZE;

    static {
        XToolkit.awtLock();
        try {
            MAX_PROPERTY_SIZE = (int) (XlibWrapper.XMaxRequestSize(XToolkit.getDisplay()) * 4 - 100);
        } finally {
            XToolkit.awtUnlock();
        }
    }

    private static final XEventDispatcher incrementalTransferHandler = new IncrementalTransferHandler();

    private static WindowPropertyGetter propertyGetter = null;

    private final XAtom selectionAtom;

    private Transferable contents = null;

    private Map formatMap = null;

    private long[] formats = null;

    private AppContext appContext = null;

    private static long lastRequestServerTime;

    private long ownershipTime = 0;

    private boolean isOwner;

    private OwnershipListener ownershipListener = null;

    private final Object stateLock = new Object();

    static {
        XToolkit.addEventDispatcher(XWindow.getXAWTRootWindow().getWindow(), new SelectionEventHandler());
    }

    static XSelection getSelection(XAtom atom) {
        return table.get(atom);
    }

    public XSelection(XAtom atom) {
        if (atom == null) {
            throw new NullPointerException("Null atom");
        }
        selectionAtom = atom;
        table.put(selectionAtom, this);
    }

    public XAtom getSelectionAtom() {
        return selectionAtom;
    }

    public synchronized boolean setOwner(Transferable contents, Map formatMap, long[] formats, long time) {
        long owner = XWindow.getXAWTRootWindow().getWindow();
        long selection = selectionAtom.getAtom();
        if (time == XlibWrapper.CurrentTime) {
            time = XToolkit.getCurrentServerTime();
        }
        this.contents = contents;
        this.formatMap = formatMap;
        this.formats = formats;
        this.appContext = AppContext.getAppContext();
        this.ownershipTime = time;
        XToolkit.awtLock();
        try {
            XlibWrapper.XSetSelectionOwner(XToolkit.getDisplay(), selection, owner, time);
            if (XlibWrapper.XGetSelectionOwner(XToolkit.getDisplay(), selection) != owner) {
                reset();
                return false;
            }
            setOwnerProp(true);
            return true;
        } finally {
            XToolkit.awtUnlock();
        }
    }

    private static void waitForSelectionNotify(WindowPropertyGetter dataGetter) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        XToolkit.awtLock();
        try {
            do {
                DataTransferer.getInstance().processDataConversionRequests();
                XToolkit.awtLockWait(250);
            } while (propertyGetter == dataGetter && System.currentTimeMillis() < startTime + UNIXToolkit.getDatatransferTimeout());
        } finally {
            XToolkit.awtUnlock();
        }
    }

    public long[] getTargets(long time) {
        if (XToolkit.isToolkitThread()) {
            throw new Error("UNIMPLEMENTED");
        }
        long[] targets = null;
        synchronized (lock) {
            WindowPropertyGetter targetsGetter = new WindowPropertyGetter(XWindow.getXAWTRootWindow().getWindow(), selectionPropertyAtom, 0, MAX_LENGTH, true, XlibWrapper.AnyPropertyType);
            try {
                XToolkit.awtLock();
                try {
                    propertyGetter = targetsGetter;
                    lastRequestServerTime = time;
                    XlibWrapper.XConvertSelection(XToolkit.getDisplay(), getSelectionAtom().getAtom(), XDataTransferer.TARGETS_ATOM.getAtom(), selectionPropertyAtom.getAtom(), XWindow.getXAWTRootWindow().getWindow(), time);
                    try {
                        waitForSelectionNotify(targetsGetter);
                    } catch (InterruptedException ie) {
                        return new long[0];
                    } finally {
                        propertyGetter = null;
                    }
                } finally {
                    XToolkit.awtUnlock();
                }
                targets = getFormats(targetsGetter);
            } finally {
                targetsGetter.dispose();
            }
        }
        return targets;
    }

    static long[] getFormats(WindowPropertyGetter targetsGetter) {
        long[] formats = null;
        if (targetsGetter.isExecuted() && !targetsGetter.isDisposed() && (targetsGetter.getActualType() == XAtom.XA_ATOM || targetsGetter.getActualType() == XDataTransferer.TARGETS_ATOM.getAtom()) && targetsGetter.getActualFormat() == 32) {
            int count = targetsGetter.getNumberOfItems();
            if (count > 0) {
                long atoms = targetsGetter.getData();
                formats = new long[count];
                for (int index = 0; index < count; index++) {
                    formats[index] = Native.getLong(atoms + index * XAtom.getAtomSize());
                }
            }
        }
        return formats != null ? formats : new long[0];
    }

    public byte[] getData(long format, long time) throws IOException {
        if (XToolkit.isToolkitThread()) {
            throw new Error("UNIMPLEMENTED");
        }
        byte[] data = null;
        synchronized (lock) {
            WindowPropertyGetter dataGetter = new WindowPropertyGetter(XWindow.getXAWTRootWindow().getWindow(), selectionPropertyAtom, 0, MAX_LENGTH, false, XlibWrapper.AnyPropertyType);
            try {
                XToolkit.awtLock();
                try {
                    propertyGetter = dataGetter;
                    lastRequestServerTime = time;
                    XlibWrapper.XConvertSelection(XToolkit.getDisplay(), getSelectionAtom().getAtom(), format, selectionPropertyAtom.getAtom(), XWindow.getXAWTRootWindow().getWindow(), time);
                    try {
                        waitForSelectionNotify(dataGetter);
                    } catch (InterruptedException ie) {
                        return new byte[0];
                    } finally {
                        propertyGetter = null;
                    }
                } finally {
                    XToolkit.awtUnlock();
                }
                if (!dataGetter.isExecuted()) {
                    throw new IOException("Owner timed out");
                }
                if (dataGetter.isDisposed()) {
                    throw new IOException("Owner failed to convert data");
                }
                if (dataGetter.getActualType() == XDataTransferer.INCR_ATOM.getAtom()) {
                    if (dataGetter.getActualFormat() != 32) {
                        throw new IOException("Unsupported INCR format: " + dataGetter.getActualFormat());
                    }
                    int count = dataGetter.getNumberOfItems();
                    if (count <= 0) {
                        throw new IOException("INCR data is missed.");
                    }
                    long ptr = dataGetter.getData();
                    int len = 0;
                    {
                        long longLength = Native.getLong(ptr, count - 1);
                        if (longLength <= 0) {
                            return new byte[0];
                        }
                        if (longLength > Integer.MAX_VALUE) {
                            throw new IOException("Can't handle large data block: " + longLength + " bytes");
                        }
                        len = (int) longLength;
                    }
                    dataGetter.dispose();
                    ByteArrayOutputStream dataStream = new ByteArrayOutputStream(len);
                    while (true) {
                        WindowPropertyGetter incrDataGetter = new WindowPropertyGetter(XWindow.getXAWTRootWindow().getWindow(), selectionPropertyAtom, 0, MAX_LENGTH, false, XlibWrapper.AnyPropertyType);
                        try {
                            XToolkit.awtLock();
                            XToolkit.addEventDispatcher(XWindow.getXAWTRootWindow().getWindow(), incrementalTransferHandler);
                            propertyGetter = incrDataGetter;
                            try {
                                XlibWrapper.XDeleteProperty(XToolkit.getDisplay(), XWindow.getXAWTRootWindow().getWindow(), selectionPropertyAtom.getAtom());
                                waitForSelectionNotify(incrDataGetter);
                            } catch (InterruptedException ie) {
                                break;
                            } finally {
                                propertyGetter = null;
                                XToolkit.removeEventDispatcher(XWindow.getXAWTRootWindow().getWindow(), incrementalTransferHandler);
                                XToolkit.awtUnlock();
                            }
                            if (!incrDataGetter.isExecuted()) {
                                throw new IOException("Owner timed out");
                            }
                            if (incrDataGetter.isDisposed()) {
                                throw new IOException("Owner failed to convert data");
                            }
                            if (incrDataGetter.getActualFormat() != 8) {
                                throw new IOException("Unsupported data format: " + incrDataGetter.getActualFormat());
                            }
                            count = incrDataGetter.getNumberOfItems();
                            if (count == 0) {
                                break;
                            }
                            if (count > 0) {
                                ptr = incrDataGetter.getData();
                                for (int index = 0; index < count; index++) {
                                    dataStream.write(Native.getByte(ptr + index));
                                }
                            }
                            data = dataStream.toByteArray();
                        } finally {
                            incrDataGetter.dispose();
                        }
                    }
                } else {
                    XToolkit.awtLock();
                    try {
                        XlibWrapper.XDeleteProperty(XToolkit.getDisplay(), XWindow.getXAWTRootWindow().getWindow(), selectionPropertyAtom.getAtom());
                    } finally {
                        XToolkit.awtUnlock();
                    }
                    if (dataGetter.getActualFormat() != 8) {
                        throw new IOException("Unsupported data format: " + dataGetter.getActualFormat());
                    }
                    int count = dataGetter.getNumberOfItems();
                    if (count > 0) {
                        data = new byte[count];
                        long ptr = dataGetter.getData();
                        for (int index = 0; index < count; index++) {
                            data[index] = Native.getByte(ptr + index);
                        }
                    }
                }
            } finally {
                dataGetter.dispose();
            }
        }
        return data != null ? data : new byte[0];
    }

    boolean isOwner() {
        return isOwner;
    }

    private void setOwnerProp(boolean f) {
        isOwner = f;
        fireOwnershipChanges(isOwner);
    }

    private void lostOwnership() {
        setOwnerProp(false);
    }

    public synchronized void reset() {
        contents = null;
        formatMap = null;
        formats = null;
        appContext = null;
        ownershipTime = 0;
    }

    private boolean convertAndStore(long requestor, long format, long property) {
        int dataFormat = 8;
        byte[] byteData = null;
        long nativeDataPtr = 0;
        int count = 0;
        try {
            SunToolkit.insertTargetMapping(this, appContext);
            byteData = DataTransferer.getInstance().convertData(this, contents, format, formatMap, XToolkit.isToolkitThread());
        } catch (IOException ioe) {
            return false;
        }
        if (byteData == null) {
            return false;
        }
        count = byteData.length;
        try {
            if (count > 0) {
                if (count <= MAX_PROPERTY_SIZE) {
                    nativeDataPtr = Native.toData(byteData);
                } else {
                    new IncrementalDataProvider(requestor, property, format, 8, byteData);
                    nativeDataPtr = XlibWrapper.unsafe.allocateMemory(XAtom.getAtomSize());
                    Native.putLong(nativeDataPtr, (long) count);
                    format = XDataTransferer.INCR_ATOM.getAtom();
                    dataFormat = 32;
                    count = 1;
                }
            }
            XToolkit.awtLock();
            try {
                XlibWrapper.XChangeProperty(XToolkit.getDisplay(), requestor, property, format, dataFormat, XlibWrapper.PropModeReplace, nativeDataPtr, count);
            } finally {
                XToolkit.awtUnlock();
            }
        } finally {
            if (nativeDataPtr != 0) {
                XlibWrapper.unsafe.freeMemory(nativeDataPtr);
                nativeDataPtr = 0;
            }
        }
        return true;
    }

    private void handleSelectionRequest(XSelectionRequestEvent xsre) {
        long property = xsre.get_property();
        final long requestor = xsre.get_requestor();
        final long requestTime = xsre.get_time();
        final long format = xsre.get_target();
        boolean conversionSucceeded = false;
        if (ownershipTime != 0 && (requestTime == XlibWrapper.CurrentTime || requestTime >= ownershipTime)) {
            if (format == XDataTransferer.MULTIPLE_ATOM.getAtom()) {
                conversionSucceeded = handleMultipleRequest(requestor, property);
            } else {
                if (property == XlibWrapper.None) {
                    property = format;
                }
                if (format == XDataTransferer.TARGETS_ATOM.getAtom()) {
                    conversionSucceeded = handleTargetsRequest(property, requestor);
                } else {
                    conversionSucceeded = convertAndStore(requestor, format, property);
                }
            }
        }
        if (!conversionSucceeded) {
            property = XlibWrapper.None;
        }
        XSelectionEvent xse = new XSelectionEvent();
        try {
            xse.set_type(XlibWrapper.SelectionNotify);
            xse.set_send_event(true);
            xse.set_requestor(requestor);
            xse.set_selection(selectionAtom.getAtom());
            xse.set_target(format);
            xse.set_property(property);
            xse.set_time(requestTime);
            XToolkit.awtLock();
            try {
                XlibWrapper.XSendEvent(XToolkit.getDisplay(), requestor, false, XlibWrapper.NoEventMask, xse.pData);
            } finally {
                XToolkit.awtUnlock();
            }
        } finally {
            xse.dispose();
        }
    }

    private boolean handleMultipleRequest(final long requestor, long property) {
        if (XlibWrapper.None == property) {
            return false;
        }
        boolean conversionSucceeded = false;
        WindowPropertyGetter wpg = new WindowPropertyGetter(requestor, XAtom.get(property), 0, MAX_LENGTH, false, XlibWrapper.AnyPropertyType);
        try {
            wpg.execute();
            if (wpg.getActualFormat() == 32 && (wpg.getNumberOfItems() % 2) == 0) {
                final long count = wpg.getNumberOfItems() / 2;
                final long pairsPtr = wpg.getData();
                boolean writeBack = false;
                for (int i = 0; i < count; i++) {
                    long target = Native.getLong(pairsPtr, 2 * i);
                    long prop = Native.getLong(pairsPtr, 2 * i + 1);
                    if (!convertAndStore(requestor, target, prop)) {
                        Native.putLong(pairsPtr, 2 * i, 0);
                        writeBack = true;
                    }
                }
                if (writeBack) {
                    XToolkit.awtLock();
                    try {
                        XlibWrapper.XChangeProperty(XToolkit.getDisplay(), requestor, property, wpg.getActualType(), wpg.getActualFormat(), XlibWrapper.PropModeReplace, wpg.getData(), wpg.getNumberOfItems());
                    } finally {
                        XToolkit.awtUnlock();
                    }
                }
                conversionSucceeded = true;
            }
        } finally {
            wpg.dispose();
        }
        return conversionSucceeded;
    }

    private boolean handleTargetsRequest(long property, long requestor) throws IllegalStateException {
        boolean conversionSucceeded = false;
        long[] formatsLocal = formats;
        if (formatsLocal == null) {
            throw new IllegalStateException("Not an owner.");
        }
        long nativeDataPtr = 0;
        try {
            final int count = formatsLocal.length;
            final int dataFormat = 32;
            if (count > 0) {
                nativeDataPtr = Native.allocateLongArray(count);
                Native.put(nativeDataPtr, formatsLocal);
            }
            conversionSucceeded = true;
            XToolkit.awtLock();
            try {
                XlibWrapper.XChangeProperty(XToolkit.getDisplay(), requestor, property, XAtom.XA_ATOM, dataFormat, XlibWrapper.PropModeReplace, nativeDataPtr, count);
            } finally {
                XToolkit.awtUnlock();
            }
        } finally {
            if (nativeDataPtr != 0) {
                XlibWrapper.unsafe.freeMemory(nativeDataPtr);
                nativeDataPtr = 0;
            }
        }
        return conversionSucceeded;
    }

    private void fireOwnershipChanges(final boolean isOwner) {
        OwnershipListener l = null;
        synchronized (stateLock) {
            l = ownershipListener;
        }
        if (null != l) {
            l.ownershipChanged(isOwner);
        }
    }

    void registerOwershipListener(OwnershipListener l) {
        synchronized (stateLock) {
            ownershipListener = l;
        }
    }

    void unregisterOwnershipListener() {
        synchronized (stateLock) {
            ownershipListener = null;
        }
    }

    private static class SelectionEventHandler implements XEventDispatcher {

        public void dispatchEvent(XEvent ev) {
            switch(ev.get_type()) {
                case XlibWrapper.SelectionNotify:
                    {
                        XToolkit.awtLock();
                        try {
                            XSelectionEvent xse = ev.get_xselection();
                            if (propertyGetter != null && xse.get_time() == lastRequestServerTime) {
                                if (xse.get_property() == selectionPropertyAtom.getAtom()) {
                                    propertyGetter.execute();
                                    propertyGetter = null;
                                } else if (xse.get_property() == 0) {
                                    propertyGetter.dispose();
                                    propertyGetter = null;
                                }
                            }
                            XToolkit.awtLockNotifyAll();
                        } finally {
                            XToolkit.awtUnlock();
                        }
                        break;
                    }
                case XlibWrapper.SelectionRequest:
                    {
                        XSelectionRequestEvent xsre = ev.get_xselectionrequest();
                        long atom = xsre.get_selection();
                        XSelection selection = XSelection.getSelection(XAtom.get(atom));
                        if (selection != null) {
                            selection.handleSelectionRequest(xsre);
                        }
                        break;
                    }
                case XlibWrapper.SelectionClear:
                    {
                        XSelectionClearEvent xsce = ev.get_xselectionclear();
                        long atom = xsce.get_selection();
                        XSelection selection = XSelection.getSelection(XAtom.get(atom));
                        if (selection != null) {
                            selection.lostOwnership();
                        }
                        XToolkit.awtLock();
                        try {
                            XToolkit.awtLockNotifyAll();
                        } finally {
                            XToolkit.awtUnlock();
                        }
                        break;
                    }
            }
        }
    }

    private static class IncrementalDataProvider implements XEventDispatcher {

        private final long requestor;

        private final long property;

        private final long target;

        private final int format;

        private final byte[] data;

        private int offset = 0;

        public IncrementalDataProvider(long requestor, long property, long target, int format, byte[] data) {
            if (format != 8) {
                throw new IllegalArgumentException("Unsupported format: " + format);
            }
            this.requestor = requestor;
            this.property = property;
            this.target = target;
            this.format = format;
            this.data = data;
            XWindowAttributes wattr = new XWindowAttributes();
            try {
                XToolkit.awtLock();
                try {
                    XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(), requestor, wattr.pData);
                    XlibWrapper.XSelectInput(XToolkit.getDisplay(), requestor, wattr.get_your_event_mask() | XlibWrapper.PropertyChangeMask);
                } finally {
                    XToolkit.awtUnlock();
                }
            } finally {
                wattr.dispose();
            }
            XToolkit.addEventDispatcher(requestor, this);
        }

        public void dispatchEvent(XEvent ev) {
            switch(ev.get_type()) {
                case XlibWrapper.PropertyNotify:
                    XPropertyEvent xpe = ev.get_xproperty();
                    if (xpe.get_window() == requestor && xpe.get_state() == XlibWrapper.PropertyDelete && xpe.get_atom() == property) {
                        int count = data.length - offset;
                        long nativeDataPtr = 0;
                        if (count > MAX_PROPERTY_SIZE) {
                            count = MAX_PROPERTY_SIZE;
                        }
                        if (count > 0) {
                            nativeDataPtr = XlibWrapper.unsafe.allocateMemory(count);
                            for (int i = 0; i < count; i++) {
                                Native.putByte(nativeDataPtr + i, data[offset + i]);
                            }
                        } else {
                            assert (count == 0);
                            XToolkit.removeEventDispatcher(requestor, this);
                        }
                        XToolkit.awtLock();
                        try {
                            XlibWrapper.XChangeProperty(XToolkit.getDisplay(), requestor, property, target, format, XlibWrapper.PropModeReplace, nativeDataPtr, count);
                        } finally {
                            XToolkit.awtUnlock();
                        }
                        if (nativeDataPtr != 0) {
                            XlibWrapper.unsafe.freeMemory(nativeDataPtr);
                            nativeDataPtr = 0;
                        }
                        offset += count;
                    }
            }
        }
    }

    private static class IncrementalTransferHandler implements XEventDispatcher {

        public void dispatchEvent(XEvent ev) {
            switch(ev.get_type()) {
                case XlibWrapper.PropertyNotify:
                    XPropertyEvent xpe = ev.get_xproperty();
                    if (xpe.get_state() == XlibWrapper.PropertyNewValue && xpe.get_atom() == selectionPropertyAtom.getAtom()) {
                        XToolkit.awtLock();
                        try {
                            if (propertyGetter != null) {
                                propertyGetter.execute();
                                propertyGetter = null;
                            }
                            XToolkit.awtLockNotifyAll();
                        } finally {
                            XToolkit.awtUnlock();
                        }
                    }
                    break;
            }
        }
    }
}
