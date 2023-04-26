package sun.awt.X11;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.MouseDragGestureRecognizer;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.im.InputMethodHighlight;
import java.awt.im.spi.InputMethodDescriptor;
import java.awt.image.ColorModel;
import java.awt.peer.*;
import java.beans.PropertyChangeListener;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import sun.awt.*;
import sun.awt.datatransfer.DataTransferer;
import sun.font.FontConfigManager;
import sun.java2d.SunGraphicsEnvironment;
import sun.misc.PerformanceLogger;
import sun.print.PrintJob2D;
import sun.security.action.GetPropertyAction;
import sun.security.action.GetBooleanAction;
import sun.util.logging.PlatformLogger;

public final class XToolkit extends UNIXToolkit implements Runnable {

    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XToolkit");

    private static final PlatformLogger eventLog = PlatformLogger.getLogger("sun.awt.X11.event.XToolkit");

    private static final PlatformLogger timeoutTaskLog = PlatformLogger.getLogger("sun.awt.X11.timeoutTask.XToolkit");

    private static final PlatformLogger keyEventLog = PlatformLogger.getLogger("sun.awt.X11.kye.XToolkit");

    private static final PlatformLogger backingStoreLog = PlatformLogger.getLogger("sun.awt.X11.backingStore.XToolkit");

    private final static int AWT_MULTICLICK_DEFAULT_TIME = 500;

    static final boolean PRIMARY_LOOP = false;

    static final boolean SECONDARY_LOOP = true;

    private static String awtAppClassName = null;

    XClipboard clipboard;

    XClipboard selection;

    protected static boolean dynamicLayoutSetting = false;

    private static boolean areExtraMouseButtonsEnabled = true;

    private boolean loadedXSettings;

    private XSettings xs;

    private FontConfigManager fcManager = new FontConfigManager();

    static int arrowCursor;

    static TreeMap winMap = new TreeMap();

    static HashMap specialPeerMap = new HashMap();

    static HashMap winToDispatcher = new HashMap();

    private static long _display;

    static UIDefaults uidefaults;

    static X11GraphicsEnvironment localEnv;

    static X11GraphicsDevice device;

    static final X11GraphicsConfig config;

    static int awt_multiclick_time;

    static boolean securityWarningEnabled;

    private static volatile int screenWidth = -1, screenHeight = -1;

    static long awt_defaultFg;

    private static XMouseInfoPeer xPeer;

    static {
        initSecurityWarning();
        if (GraphicsEnvironment.isHeadless()) {
            config = null;
        } else {
            localEnv = (X11GraphicsEnvironment) GraphicsEnvironment.getLocalGraphicsEnvironment();
            device = (X11GraphicsDevice) localEnv.getDefaultScreenDevice();
            config = (X11GraphicsConfig) (device.getDefaultConfiguration());
            if (device != null) {
                _display = device.getDisplay();
            }
            setupModifierMap();
            initIDs();
            setBackingStoreType();
        }
    }

    static native long getTrayIconDisplayTimeout();

    private native static void initIDs();

    native static void waitForEvents(long nextTaskTime);

    static Thread toolkitThread;

    static boolean isToolkitThread() {
        return Thread.currentThread() == toolkitThread;
    }

    static void initSecurityWarning() {
        String runtime = AccessController.doPrivileged(new GetPropertyAction("java.runtime.version"));
        securityWarningEnabled = (runtime != null && runtime.contains("internal"));
    }

    static boolean isSecurityWarningEnabled() {
        return securityWarningEnabled;
    }

    static native void awt_output_flush();

    static final void awtFUnlock() {
        awtUnlock();
        awt_output_flush();
    }

    public native void nativeLoadSystemColors(int[] systemColors);

    static UIDefaults getUIDefaults() {
        if (uidefaults == null) {
            initUIDefaults();
        }
        return uidefaults;
    }

    public void loadSystemColors(int[] systemColors) {
        nativeLoadSystemColors(systemColors);
        MotifColorUtilities.loadSystemColors(systemColors);
    }

    static void initUIDefaults() {
        try {
            Color c = SystemColor.text;
            LookAndFeel lnf = new XAWTLookAndFeel();
            uidefaults = lnf.getDefaults();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static Object displayLock = new Object();

    public static long getDisplay() {
        return _display;
    }

    public static long getDefaultRootWindow() {
        awtLock();
        try {
            long res = XlibWrapper.RootWindow(XToolkit.getDisplay(), XlibWrapper.DefaultScreen(XToolkit.getDisplay()));
            if (res == 0) {
                throw new IllegalStateException("Root window must not be null");
            }
            return res;
        } finally {
            awtUnlock();
        }
    }

    void init() {
        awtLock();
        try {
            XlibWrapper.XSupportsLocale();
            if (XlibWrapper.XSetLocaleModifiers("") == null) {
                log.finer("X locale modifiers are not supported, using default");
            }
            tryXKB();
            AwtScreenData defaultScreen = new AwtScreenData(XToolkit.getDefaultScreenData());
            awt_defaultFg = defaultScreen.get_blackpixel();
            arrowCursor = XlibWrapper.XCreateFontCursor(XToolkit.getDisplay(), XCursorFontConstants.XC_arrow);
            areExtraMouseButtonsEnabled = Boolean.parseBoolean(System.getProperty("sun.awt.enableExtraMouseButtons", "true"));
            System.setProperty("sun.awt.enableExtraMouseButtons", "" + areExtraMouseButtonsEnabled);
            XlibWrapper.XSelectInput(XToolkit.getDisplay(), XToolkit.getDefaultRootWindow(), XConstants.StructureNotifyMask);
            XToolkit.addEventDispatcher(XToolkit.getDefaultRootWindow(), new XEventDispatcher() {

                @Override
                public void dispatchEvent(XEvent ev) {
                    if (ev.get_type() == XConstants.ConfigureNotify) {
                        awtUnlock();
                        try {
                            ((X11GraphicsEnvironment) GraphicsEnvironment.getLocalGraphicsEnvironment()).displayChanged();
                        } finally {
                            awtLock();
                        }
                    }
                }
            });
        } finally {
            awtUnlock();
        }
        PrivilegedAction<Void> a = new PrivilegedAction<Void>() {

            public Void run() {
                ThreadGroup mainTG = Thread.currentThread().getThreadGroup();
                ThreadGroup parentTG = mainTG.getParent();
                while (parentTG != null) {
                    mainTG = parentTG;
                    parentTG = mainTG.getParent();
                }
                Thread shutdownThread = new Thread(mainTG, "XToolkt-Shutdown-Thread") {

                    public void run() {
                        XSystemTrayPeer peer = XSystemTrayPeer.getPeerInstance();
                        if (peer != null) {
                            peer.dispose();
                        }
                        if (xs != null) {
                            ((XAWTXSettings) xs).dispose();
                        }
                        freeXKB();
                        if (log.isLoggable(PlatformLogger.Level.FINE)) {
                            dumpPeers();
                        }
                    }
                };
                shutdownThread.setContextClassLoader(null);
                Runtime.getRuntime().addShutdownHook(shutdownThread);
                return null;
            }
        };
        AccessController.doPrivileged(a);
    }

    static String getCorrectXIDString(String val) {
        if (val != null) {
            return val.replace('.', '-');
        } else {
            return val;
        }
    }

    static native String getEnv(String key);

    static String getAWTAppClassName() {
        return awtAppClassName;
    }

    public XToolkit() {
        super();
        if (PerformanceLogger.loggingEnabled()) {
            PerformanceLogger.setTime("XToolkit construction");
        }
        if (!GraphicsEnvironment.isHeadless()) {
            String mainClassName = null;
            StackTraceElement[] trace = (new Throwable()).getStackTrace();
            int bottom = trace.length - 1;
            if (bottom >= 0) {
                mainClassName = trace[bottom].getClassName();
            }
            if (mainClassName == null || mainClassName.equals("")) {
                mainClassName = "AWT";
            }
            awtAppClassName = getCorrectXIDString(mainClassName);
            init();
            XWM.init();
            PrivilegedAction<Thread> action = new PrivilegedAction() {

                public Thread run() {
                    ThreadGroup currentTG = Thread.currentThread().getThreadGroup();
                    ThreadGroup parentTG = currentTG.getParent();
                    while (parentTG != null) {
                        currentTG = parentTG;
                        parentTG = currentTG.getParent();
                    }
                    Thread thread = new Thread(currentTG, XToolkit.this, "AWT-XAWT");
                    thread.setPriority(Thread.NORM_PRIORITY + 1);
                    thread.setDaemon(true);
                    return thread;
                }
            };
            toolkitThread = AccessController.doPrivileged(action);
            toolkitThread.start();
        }
    }

    public ButtonPeer createButton(Button target) {
        ButtonPeer peer = new XButtonPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public FramePeer createLightweightFrame(LightweightFrame target) {
        FramePeer peer = new XLightweightFramePeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public FramePeer createFrame(Frame target) {
        FramePeer peer = new XFramePeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    static void addToWinMap(long window, XBaseWindow xwin) {
        synchronized (winMap) {
            winMap.put(Long.valueOf(window), xwin);
        }
    }

    static void removeFromWinMap(long window, XBaseWindow xwin) {
        synchronized (winMap) {
            winMap.remove(Long.valueOf(window));
        }
    }

    static XBaseWindow windowToXWindow(long window) {
        synchronized (winMap) {
            return (XBaseWindow) winMap.get(Long.valueOf(window));
        }
    }

    static void addEventDispatcher(long window, XEventDispatcher dispatcher) {
        synchronized (winToDispatcher) {
            Long key = Long.valueOf(window);
            Collection dispatchers = (Collection) winToDispatcher.get(key);
            if (dispatchers == null) {
                dispatchers = new Vector();
                winToDispatcher.put(key, dispatchers);
            }
            dispatchers.add(dispatcher);
        }
    }

    static void removeEventDispatcher(long window, XEventDispatcher dispatcher) {
        synchronized (winToDispatcher) {
            Long key = Long.valueOf(window);
            Collection dispatchers = (Collection) winToDispatcher.get(key);
            if (dispatchers != null) {
                dispatchers.remove(dispatcher);
            }
        }
    }

    private Point lastCursorPos;

    boolean getLastCursorPos(Point p) {
        awtLock();
        try {
            if (lastCursorPos == null) {
                return false;
            }
            p.setLocation(lastCursorPos);
            return true;
        } finally {
            awtUnlock();
        }
    }

    private void processGlobalMotionEvent(XEvent e) {
        if (e.get_type() == XConstants.MotionNotify) {
            XMotionEvent ev = e.get_xmotion();
            awtLock();
            try {
                if (lastCursorPos == null) {
                    lastCursorPos = new Point(ev.get_x_root(), ev.get_y_root());
                } else {
                    lastCursorPos.setLocation(ev.get_x_root(), ev.get_y_root());
                }
            } finally {
                awtUnlock();
            }
        } else if (e.get_type() == XConstants.LeaveNotify) {
            awtLock();
            try {
                lastCursorPos = null;
            } finally {
                awtUnlock();
            }
        } else if (e.get_type() == XConstants.EnterNotify) {
            XCrossingEvent ev = e.get_xcrossing();
            awtLock();
            try {
                if (lastCursorPos == null) {
                    lastCursorPos = new Point(ev.get_x_root(), ev.get_y_root());
                } else {
                    lastCursorPos.setLocation(ev.get_x_root(), ev.get_y_root());
                }
            } finally {
                awtUnlock();
            }
        }
    }

    public interface XEventListener {

        public void eventProcessed(XEvent e);
    }

    private Collection<XEventListener> listeners = new LinkedList<XEventListener>();

    public void addXEventListener(XEventListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    private void notifyListeners(XEvent xev) {
        synchronized (listeners) {
            if (listeners.size() == 0)
                return;
            XEvent copy = xev.clone();
            try {
                for (XEventListener listener : listeners) {
                    listener.eventProcessed(copy);
                }
            } finally {
                copy.dispose();
            }
        }
    }

    private void dispatchEvent(XEvent ev) {
        final XAnyEvent xany = ev.get_xany();
        if (windowToXWindow(xany.get_window()) != null && (ev.get_type() == XConstants.MotionNotify || ev.get_type() == XConstants.EnterNotify || ev.get_type() == XConstants.LeaveNotify)) {
            processGlobalMotionEvent(ev);
        }
        if (ev.get_type() == XConstants.MappingNotify) {
            XlibWrapper.XRefreshKeyboardMapping(ev.pData);
            resetKeyboardSniffer();
            setupModifierMap();
        }
        XBaseWindow.dispatchToWindow(ev);
        Collection dispatchers = null;
        synchronized (winToDispatcher) {
            Long key = Long.valueOf(xany.get_window());
            dispatchers = (Collection) winToDispatcher.get(key);
            if (dispatchers != null) {
                dispatchers = new Vector(dispatchers);
            }
        }
        if (dispatchers != null) {
            Iterator iter = dispatchers.iterator();
            while (iter.hasNext()) {
                XEventDispatcher disp = (XEventDispatcher) iter.next();
                disp.dispatchEvent(ev);
            }
        }
        notifyListeners(ev);
    }

    static void processException(Throwable thr) {
        if (log.isLoggable(PlatformLogger.Level.WARNING)) {
            log.warning("Exception on Toolkit thread", thr);
        }
    }

    static native void awt_toolkit_init();

    public void run() {
        awt_toolkit_init();
        run(PRIMARY_LOOP);
    }

    public void run(boolean loop) {
        XEvent ev = new XEvent();
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                if (AppContext.getAppContext().isDisposed()) {
                    break;
                }
            }
            awtLock();
            try {
                if (loop == SECONDARY_LOOP) {
                    if (!XlibWrapper.XNextSecondaryLoopEvent(getDisplay(), ev.pData)) {
                        break;
                    }
                } else {
                    callTimeoutTasks();
                    while ((XlibWrapper.XEventsQueued(getDisplay(), XConstants.QueuedAfterReading) == 0) && (XlibWrapper.XEventsQueued(getDisplay(), XConstants.QueuedAfterFlush) == 0)) {
                        callTimeoutTasks();
                        waitForEvents(getNextTaskTime());
                    }
                    XlibWrapper.XNextEvent(getDisplay(), ev.pData);
                }
                if (ev.get_type() != XConstants.NoExpose) {
                    eventNumber++;
                }
                if (awt_UseXKB_Calls && ev.get_type() == awt_XKBBaseEventCode) {
                    processXkbChanges(ev);
                }
                if (XDropTargetEventProcessor.processEvent(ev) || XDragSourceContextPeer.processEvent(ev)) {
                    continue;
                }
                if (eventLog.isLoggable(PlatformLogger.Level.FINER)) {
                    eventLog.finer("{0}", ev);
                }
                long w = 0;
                if (windowToXWindow(ev.get_xany().get_window()) != null) {
                    Component owner = XKeyboardFocusManagerPeer.getInstance().getCurrentFocusOwner();
                    if (owner != null) {
                        XWindow ownerWindow = (XWindow) AWTAccessor.getComponentAccessor().getPeer(owner);
                        if (ownerWindow != null) {
                            w = ownerWindow.getContentWindow();
                        }
                    }
                }
                if (keyEventLog.isLoggable(PlatformLogger.Level.FINE) && (ev.get_type() == XConstants.KeyPress || ev.get_type() == XConstants.KeyRelease)) {
                    keyEventLog.fine("before XFilterEvent:" + ev);
                }
                if (XlibWrapper.XFilterEvent(ev.getPData(), w)) {
                    continue;
                }
                if (keyEventLog.isLoggable(PlatformLogger.Level.FINE) && (ev.get_type() == XConstants.KeyPress || ev.get_type() == XConstants.KeyRelease)) {
                    keyEventLog.fine("after XFilterEvent:" + ev);
                }
                dispatchEvent(ev);
            } catch (ThreadDeath td) {
                XBaseWindow.ungrabInput();
                return;
            } catch (Throwable thr) {
                XBaseWindow.ungrabInput();
                processException(thr);
            } finally {
                awtUnlock();
            }
        }
    }

    static {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge instanceof SunGraphicsEnvironment) {
            ((SunGraphicsEnvironment) ge).addDisplayChangedListener(new DisplayChangedListener() {

                @Override
                public void displayChanged() {
                    XToolkit.screenWidth = -1;
                    XToolkit.screenHeight = -1;
                }

                @Override
                public void paletteChanged() {
                }
            });
        }
    }

    private static void initScreenSize() {
        if (screenWidth == -1 || screenHeight == -1) {
            awtLock();
            try {
                XWindowAttributes pattr = new XWindowAttributes();
                try {
                    XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(), XToolkit.getDefaultRootWindow(), pattr.pData);
                    screenWidth = pattr.get_width();
                    screenHeight = pattr.get_height();
                } finally {
                    pattr.dispose();
                }
            } finally {
                awtUnlock();
            }
        }
    }

    static int getDefaultScreenWidth() {
        initScreenSize();
        return screenWidth;
    }

    static int getDefaultScreenHeight() {
        initScreenSize();
        return screenHeight;
    }

    protected int getScreenWidth() {
        return getDefaultScreenWidth();
    }

    protected int getScreenHeight() {
        return getDefaultScreenHeight();
    }

    private static Rectangle getWorkArea(long root) {
        XAtom XA_NET_WORKAREA = XAtom.get("_NET_WORKAREA");
        long native_ptr = Native.allocateLongArray(4);
        try {
            boolean workareaPresent = XA_NET_WORKAREA.getAtomData(root, XAtom.XA_CARDINAL, native_ptr, 4);
            if (workareaPresent) {
                int rootX = (int) Native.getLong(native_ptr, 0);
                int rootY = (int) Native.getLong(native_ptr, 1);
                int rootWidth = (int) Native.getLong(native_ptr, 2);
                int rootHeight = (int) Native.getLong(native_ptr, 3);
                return new Rectangle(rootX, rootY, rootWidth, rootHeight);
            }
        } finally {
            XlibWrapper.unsafe.freeMemory(native_ptr);
        }
        return null;
    }

    public Insets getScreenInsets(GraphicsConfiguration gc) {
        XNETProtocol netProto = XWM.getWM().getNETProtocol();
        if ((netProto == null) || !netProto.active()) {
            return super.getScreenInsets(gc);
        }
        XToolkit.awtLock();
        try {
            X11GraphicsConfig x11gc = (X11GraphicsConfig) gc;
            X11GraphicsDevice x11gd = (X11GraphicsDevice) x11gc.getDevice();
            long root = XlibUtil.getRootWindow(x11gd.getScreen());
            Rectangle rootBounds = XlibUtil.getWindowGeometry(root);
            X11GraphicsEnvironment x11ge = (X11GraphicsEnvironment) GraphicsEnvironment.getLocalGraphicsEnvironment();
            if (!x11ge.runningXinerama()) {
                Rectangle workArea = XToolkit.getWorkArea(root);
                if (workArea != null) {
                    return new Insets(workArea.y, workArea.x, rootBounds.height - workArea.height - workArea.y, rootBounds.width - workArea.width - workArea.x);
                }
            }
            return getScreenInsetsManually(root, rootBounds, gc.getBounds());
        } finally {
            XToolkit.awtUnlock();
        }
    }

    private Insets getScreenInsetsManually(long root, Rectangle rootBounds, Rectangle screenBounds) {
        final int MAX_NESTED_LEVEL = 3;
        XAtom XA_NET_WM_STRUT = XAtom.get("_NET_WM_STRUT");
        XAtom XA_NET_WM_STRUT_PARTIAL = XAtom.get("_NET_WM_STRUT_PARTIAL");
        Insets insets = new Insets(0, 0, 0, 0);
        java.util.List search = new LinkedList();
        search.add(root);
        search.add(0);
        while (!search.isEmpty()) {
            long window = (Long) search.remove(0);
            int windowLevel = (Integer) search.remove(0);
            if (XlibUtil.getWindowMapState(window) == XConstants.IsUnmapped) {
                continue;
            }
            long native_ptr = Native.allocateLongArray(4);
            try {
                boolean strutPresent = XA_NET_WM_STRUT_PARTIAL.getAtomData(window, XAtom.XA_CARDINAL, native_ptr, 4);
                if (!strutPresent) {
                    strutPresent = XA_NET_WM_STRUT.getAtomData(window, XAtom.XA_CARDINAL, native_ptr, 4);
                }
                if (strutPresent) {
                    Rectangle windowBounds = XlibUtil.getWindowGeometry(window);
                    if (windowLevel > 1) {
                        windowBounds = XlibUtil.translateCoordinates(window, root, windowBounds);
                    }
                    if (windowBounds != null && windowBounds.intersects(screenBounds)) {
                        insets.left = Math.max((int) Native.getLong(native_ptr, 0), insets.left);
                        insets.right = Math.max((int) Native.getLong(native_ptr, 1), insets.right);
                        insets.top = Math.max((int) Native.getLong(native_ptr, 2), insets.top);
                        insets.bottom = Math.max((int) Native.getLong(native_ptr, 3), insets.bottom);
                    }
                }
            } finally {
                XlibWrapper.unsafe.freeMemory(native_ptr);
            }
            if (windowLevel < MAX_NESTED_LEVEL) {
                Set<Long> children = XlibUtil.getChildWindows(window);
                for (long child : children) {
                    search.add(child);
                    search.add(windowLevel + 1);
                }
            }
        }
        return insets;
    }

    protected static final Object targetToPeer(Object target) {
        Object p = null;
        if (target != null && !GraphicsEnvironment.isHeadless()) {
            p = specialPeerMap.get(target);
        }
        if (p != null)
            return p;
        else
            return SunToolkit.targetToPeer(target);
    }

    protected static final void targetDisposedPeer(Object target, Object peer) {
        SunToolkit.targetDisposedPeer(target, peer);
    }

    public RobotPeer createRobot(Robot target, GraphicsDevice screen) {
        return new XRobotPeer(screen.getDefaultConfiguration());
    }

    public void setDynamicLayout(boolean b) {
        dynamicLayoutSetting = b;
    }

    protected boolean isDynamicLayoutSet() {
        return dynamicLayoutSetting;
    }

    protected boolean isDynamicLayoutSupported() {
        return XWM.getWM().supportsDynamicLayout();
    }

    public boolean isDynamicLayoutActive() {
        return isDynamicLayoutSupported();
    }

    public FontPeer getFontPeer(String name, int style) {
        return new XFontPeer(name, style);
    }

    public DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) throws InvalidDnDOperationException {
        return XDragSourceContextPeer.createDragSourceContextPeer(dge);
    }

    public <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> recognizerClass, DragSource ds, Component c, int srcActions, DragGestureListener dgl) {
        if (MouseDragGestureRecognizer.class.equals(recognizerClass))
            return (T) new XMouseDragGestureRecognizer(ds, c, srcActions, dgl);
        else
            return null;
    }

    public CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem target) {
        XCheckboxMenuItemPeer peer = new XCheckboxMenuItemPeer(target);
        return peer;
    }

    public MenuItemPeer createMenuItem(MenuItem target) {
        XMenuItemPeer peer = new XMenuItemPeer(target);
        return peer;
    }

    public TextFieldPeer createTextField(TextField target) {
        TextFieldPeer peer = new XTextFieldPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public LabelPeer createLabel(Label target) {
        LabelPeer peer = new XLabelPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public ListPeer createList(java.awt.List target) {
        ListPeer peer = new XListPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public CheckboxPeer createCheckbox(Checkbox target) {
        CheckboxPeer peer = new XCheckboxPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public ScrollbarPeer createScrollbar(Scrollbar target) {
        XScrollbarPeer peer = new XScrollbarPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public ScrollPanePeer createScrollPane(ScrollPane target) {
        XScrollPanePeer peer = new XScrollPanePeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public TextAreaPeer createTextArea(TextArea target) {
        TextAreaPeer peer = new XTextAreaPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public ChoicePeer createChoice(Choice target) {
        XChoicePeer peer = new XChoicePeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public CanvasPeer createCanvas(Canvas target) {
        XCanvasPeer peer = (isXEmbedServerRequested() ? new XEmbedCanvasPeer(target) : new XCanvasPeer(target));
        targetCreatedPeer(target, peer);
        return peer;
    }

    public PanelPeer createPanel(Panel target) {
        PanelPeer peer = new XPanelPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public WindowPeer createWindow(Window target) {
        WindowPeer peer = new XWindowPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public DialogPeer createDialog(Dialog target) {
        DialogPeer peer = new XDialogPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    private static Boolean sunAwtDisableGtkFileDialogs = null;

    public synchronized static boolean getSunAwtDisableGtkFileDialogs() {
        if (sunAwtDisableGtkFileDialogs == null) {
            sunAwtDisableGtkFileDialogs = AccessController.doPrivileged(new GetBooleanAction("sun.awt.disableGtkFileDialogs"));
        }
        return sunAwtDisableGtkFileDialogs.booleanValue();
    }

    public FileDialogPeer createFileDialog(FileDialog target) {
        FileDialogPeer peer = null;
        if (!getSunAwtDisableGtkFileDialogs() && checkGtkVersion(2, 4, 0)) {
            peer = new GtkFileDialogPeer(target);
        } else {
            peer = new XFileDialogPeer(target);
        }
        targetCreatedPeer(target, peer);
        return peer;
    }

    public MenuBarPeer createMenuBar(MenuBar target) {
        XMenuBarPeer peer = new XMenuBarPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public MenuPeer createMenu(Menu target) {
        XMenuPeer peer = new XMenuPeer(target);
        return peer;
    }

    public PopupMenuPeer createPopupMenu(PopupMenu target) {
        XPopupMenuPeer peer = new XPopupMenuPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public synchronized MouseInfoPeer getMouseInfoPeer() {
        if (xPeer == null) {
            xPeer = new XMouseInfoPeer();
        }
        return xPeer;
    }

    public XEmbeddedFramePeer createEmbeddedFrame(XEmbeddedFrame target) {
        XEmbeddedFramePeer peer = new XEmbeddedFramePeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    XEmbedChildProxyPeer createEmbedProxy(XEmbedChildProxy target) {
        XEmbedChildProxyPeer peer = new XEmbedChildProxyPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public KeyboardFocusManagerPeer getKeyboardFocusManagerPeer() throws HeadlessException {
        return XKeyboardFocusManagerPeer.getInstance();
    }

    public Cursor createCustomCursor(Image cursor, Point hotSpot, String name) throws IndexOutOfBoundsException {
        return new XCustomCursor(cursor, hotSpot, name);
    }

    public TrayIconPeer createTrayIcon(TrayIcon target) throws HeadlessException, AWTException {
        TrayIconPeer peer = new XTrayIconPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public SystemTrayPeer createSystemTray(SystemTray target) throws HeadlessException {
        SystemTrayPeer peer = new XSystemTrayPeer(target);
        return peer;
    }

    public boolean isTraySupported() {
        XSystemTrayPeer peer = XSystemTrayPeer.getPeerInstance();
        if (peer != null) {
            return peer.isAvailable();
        }
        return false;
    }

    @Override
    public DataTransferer getDataTransferer() {
        return XDataTransferer.getInstanceImpl();
    }

    public Dimension getBestCursorSize(int preferredWidth, int preferredHeight) {
        return XCustomCursor.getBestCursorSize(java.lang.Math.max(1, preferredWidth), java.lang.Math.max(1, preferredHeight));
    }

    public int getMaximumCursorColors() {
        return 2;
    }

    public Map mapInputMethodHighlight(InputMethodHighlight highlight) {
        return XInputMethod.mapInputMethodHighlight(highlight);
    }

    @Override
    public boolean getLockingKeyState(int key) {
        if (!(key == KeyEvent.VK_CAPS_LOCK || key == KeyEvent.VK_NUM_LOCK || key == KeyEvent.VK_SCROLL_LOCK || key == KeyEvent.VK_KANA_LOCK)) {
            throw new IllegalArgumentException("invalid key for Toolkit.getLockingKeyState");
        }
        awtLock();
        try {
            return getModifierState(key);
        } finally {
            awtUnlock();
        }
    }

    public Clipboard getSystemClipboard() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(AWTPermissions.ACCESS_CLIPBOARD_PERMISSION);
        }
        synchronized (this) {
            if (clipboard == null) {
                clipboard = new XClipboard("System", "CLIPBOARD");
            }
        }
        return clipboard;
    }

    public Clipboard getSystemSelection() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(AWTPermissions.ACCESS_CLIPBOARD_PERMISSION);
        }
        synchronized (this) {
            if (selection == null) {
                selection = new XClipboard("Selection", "PRIMARY");
            }
        }
        return selection;
    }

    public void beep() {
        awtLock();
        try {
            XlibWrapper.XBell(getDisplay(), 0);
            XlibWrapper.XFlush(getDisplay());
        } finally {
            awtUnlock();
        }
    }

    public PrintJob getPrintJob(final Frame frame, final String doctitle, final Properties props) {
        if (frame == null) {
            throw new NullPointerException("frame must not be null");
        }
        PrintJob2D printJob = new PrintJob2D(frame, doctitle, props);
        if (printJob.printDialog() == false) {
            printJob = null;
        }
        return printJob;
    }

    public PrintJob getPrintJob(final Frame frame, final String doctitle, final JobAttributes jobAttributes, final PageAttributes pageAttributes) {
        if (frame == null) {
            throw new NullPointerException("frame must not be null");
        }
        PrintJob2D printJob = new PrintJob2D(frame, doctitle, jobAttributes, pageAttributes);
        if (printJob.printDialog() == false) {
            printJob = null;
        }
        return printJob;
    }

    static void XSync() {
        awtLock();
        try {
            XlibWrapper.XSync(getDisplay(), 0);
        } finally {
            awtUnlock();
        }
    }

    public int getScreenResolution() {
        long display = getDisplay();
        awtLock();
        try {
            return (int) ((XlibWrapper.DisplayWidth(display, XlibWrapper.DefaultScreen(display)) * 25.4) / XlibWrapper.DisplayWidthMM(display, XlibWrapper.DefaultScreen(display)));
        } finally {
            awtUnlock();
        }
    }

    static native long getDefaultXColormap();

    static native long getDefaultScreenData();

    static ColorModel screenmodel;

    static ColorModel getStaticColorModel() {
        if (screenmodel == null) {
            screenmodel = config.getColorModel();
        }
        return screenmodel;
    }

    public ColorModel getColorModel() {
        return getStaticColorModel();
    }

    public InputMethodDescriptor getInputMethodAdapterDescriptor() throws AWTException {
        return new XInputMethodDescriptor();
    }

    @Override
    public boolean enableInputMethodsForTextComponent() {
        return true;
    }

    static int getMultiClickTime() {
        if (awt_multiclick_time == 0) {
            initializeMultiClickTime();
        }
        return awt_multiclick_time;
    }

    static void initializeMultiClickTime() {
        awtLock();
        try {
            try {
                String multiclick_time_query = XlibWrapper.XGetDefault(XToolkit.getDisplay(), "*", "multiClickTime");
                if (multiclick_time_query != null) {
                    awt_multiclick_time = (int) Long.parseLong(multiclick_time_query);
                } else {
                    multiclick_time_query = XlibWrapper.XGetDefault(XToolkit.getDisplay(), "OpenWindows", "MultiClickTimeout");
                    if (multiclick_time_query != null) {
                        awt_multiclick_time = (int) Long.parseLong(multiclick_time_query) * 100;
                    } else {
                        awt_multiclick_time = AWT_MULTICLICK_DEFAULT_TIME;
                    }
                }
            } catch (NumberFormatException nf) {
                awt_multiclick_time = AWT_MULTICLICK_DEFAULT_TIME;
            } catch (NullPointerException npe) {
                awt_multiclick_time = AWT_MULTICLICK_DEFAULT_TIME;
            }
        } finally {
            awtUnlock();
        }
        if (awt_multiclick_time == 0) {
            awt_multiclick_time = AWT_MULTICLICK_DEFAULT_TIME;
        }
    }

    public boolean isFrameStateSupported(int state) throws HeadlessException {
        if (state == Frame.NORMAL || state == Frame.ICONIFIED) {
            return true;
        } else {
            return XWM.getWM().supportsExtendedState(state);
        }
    }

    static void dumpPeers() {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("Mapped windows:");
            Iterator iter = winMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                log.fine(entry.getKey() + "->" + entry.getValue());
                if (entry.getValue() instanceof XComponentPeer) {
                    Component target = (Component) ((XComponentPeer) entry.getValue()).getTarget();
                    log.fine("\ttarget: " + target);
                }
            }
            SunToolkit.dumpPeers(log);
            log.fine("Mapped special peers:");
            iter = specialPeerMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                log.fine(entry.getKey() + "->" + entry.getValue());
            }
            log.fine("Mapped dispatchers:");
            iter = winToDispatcher.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                log.fine(entry.getKey() + "->" + entry.getValue());
            }
        }
    }

    private static boolean initialized;

    private static boolean timeStampUpdated;

    private static long timeStamp;

    private static final XEventDispatcher timeFetcher = new XEventDispatcher() {

        public void dispatchEvent(XEvent ev) {
            switch(ev.get_type()) {
                case XConstants.PropertyNotify:
                    XPropertyEvent xpe = ev.get_xproperty();
                    awtLock();
                    try {
                        timeStamp = xpe.get_time();
                        timeStampUpdated = true;
                        awtLockNotifyAll();
                    } finally {
                        awtUnlock();
                    }
                    break;
            }
        }
    };

    private static XAtom _XA_JAVA_TIME_PROPERTY_ATOM;

    static long getCurrentServerTime() {
        awtLock();
        try {
            try {
                if (!initialized) {
                    XToolkit.addEventDispatcher(XBaseWindow.getXAWTRootWindow().getWindow(), timeFetcher);
                    _XA_JAVA_TIME_PROPERTY_ATOM = XAtom.get("_SUNW_JAVA_AWT_TIME");
                    initialized = true;
                }
                timeStampUpdated = false;
                XlibWrapper.XChangeProperty(XToolkit.getDisplay(), XBaseWindow.getXAWTRootWindow().getWindow(), _XA_JAVA_TIME_PROPERTY_ATOM.getAtom(), XAtom.XA_ATOM, 32, XConstants.PropModeAppend, 0, 0);
                XlibWrapper.XFlush(XToolkit.getDisplay());
                if (isToolkitThread()) {
                    XEvent event = new XEvent();
                    try {
                        XlibWrapper.XWindowEvent(XToolkit.getDisplay(), XBaseWindow.getXAWTRootWindow().getWindow(), XConstants.PropertyChangeMask, event.pData);
                        timeFetcher.dispatchEvent(event);
                    } finally {
                        event.dispose();
                    }
                } else {
                    while (!timeStampUpdated) {
                        awtLockWait();
                    }
                }
            } catch (InterruptedException ie) {
                if (log.isLoggable(PlatformLogger.Level.FINE)) {
                    log.fine("Catched exception, timeStamp may not be correct (ie = " + ie + ")");
                }
            }
        } finally {
            awtUnlock();
        }
        return timeStamp;
    }

    protected void initializeDesktopProperties() {
        desktopProperties.put("DnD.Autoscroll.initialDelay", Integer.valueOf(50));
        desktopProperties.put("DnD.Autoscroll.interval", Integer.valueOf(50));
        desktopProperties.put("DnD.Autoscroll.cursorHysteresis", Integer.valueOf(5));
        desktopProperties.put("Shell.shellFolderManager", "sun.awt.shell.ShellFolderManager");
        if (!GraphicsEnvironment.isHeadless()) {
            desktopProperties.put("awt.multiClickInterval", Integer.valueOf(getMultiClickTime()));
            desktopProperties.put("awt.mouse.numButtons", Integer.valueOf(getNumberOfButtons()));
        }
    }

    private native int getNumberOfButtonsImpl();

    @Override
    public int getNumberOfButtons() {
        awtLock();
        try {
            if (numberOfButtons == 0) {
                numberOfButtons = getNumberOfButtonsImpl();
                numberOfButtons = (numberOfButtons > MAX_BUTTONS_SUPPORTED) ? MAX_BUTTONS_SUPPORTED : numberOfButtons;
                if (numberOfButtons >= 5) {
                    numberOfButtons -= 2;
                } else if (numberOfButtons == 4 || numberOfButtons == 5) {
                    numberOfButtons = 3;
                }
            }
            return numberOfButtons;
        } finally {
            awtUnlock();
        }
    }

    static int getNumberOfButtonsForMask() {
        return Math.min(XConstants.MAX_BUTTONS, ((SunToolkit) (Toolkit.getDefaultToolkit())).getNumberOfButtons());
    }

    private final static String prefix = "DnD.Cursor.";

    private final static String postfix = ".32x32";

    private static final String dndPrefix = "DnD.";

    protected Object lazilyLoadDesktopProperty(String name) {
        if (name.startsWith(prefix)) {
            String cursorName = name.substring(prefix.length(), name.length()) + postfix;
            try {
                return Cursor.getSystemCustomCursor(cursorName);
            } catch (AWTException awte) {
                throw new RuntimeException("cannot load system cursor: " + cursorName, awte);
            }
        }
        if (name.equals("awt.dynamicLayoutSupported")) {
            return Boolean.valueOf(isDynamicLayoutSupported());
        }
        if (initXSettingsIfNeeded(name)) {
            return desktopProperties.get(name);
        }
        return super.lazilyLoadDesktopProperty(name);
    }

    public synchronized void addPropertyChangeListener(String name, PropertyChangeListener pcl) {
        if (name == null) {
            return;
        }
        initXSettingsIfNeeded(name);
        super.addPropertyChangeListener(name, pcl);
    }

    private boolean initXSettingsIfNeeded(final String propName) {
        if (!loadedXSettings && (propName.startsWith("gnome.") || propName.equals(SunToolkit.DESKTOPFONTHINTS) || propName.startsWith(dndPrefix))) {
            loadedXSettings = true;
            if (!GraphicsEnvironment.isHeadless()) {
                loadXSettings();
                if (desktopProperties.get(SunToolkit.DESKTOPFONTHINTS) == null) {
                    if (XWM.isKDE2()) {
                        Object hint = FontConfigManager.getFontConfigAAHint();
                        if (hint != null) {
                            desktopProperties.put(UNIXToolkit.FONTCONFIGAAHINT, hint);
                        }
                    }
                    desktopProperties.put(SunToolkit.DESKTOPFONTHINTS, SunToolkit.getDesktopFontHints());
                }
                return true;
            }
        }
        return false;
    }

    private void loadXSettings() {
        xs = new XAWTXSettings();
    }

    void parseXSettings(int screen_XXX_ignored, Map updatedSettings) {
        if (updatedSettings == null || updatedSettings.isEmpty()) {
            return;
        }
        Iterator i = updatedSettings.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            String name = (String) e.getKey();
            name = "gnome." + name;
            setDesktopProperty(name, e.getValue());
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine("name = " + name + " value = " + e.getValue());
            }
        }
        setDesktopProperty(SunToolkit.DESKTOPFONTHINTS, SunToolkit.getDesktopFontHints());
        Integer dragThreshold = null;
        synchronized (this) {
            dragThreshold = (Integer) desktopProperties.get("gnome.Net/DndDragThreshold");
        }
        if (dragThreshold != null) {
            setDesktopProperty("DnD.gestureMotionThreshold", dragThreshold);
        }
    }

    static int altMask;

    static int metaMask;

    static int numLockMask;

    static int modeSwitchMask;

    static int modLockIsShiftLock;

    static int keysymToPrimaryKeycode(long sym) {
        awtLock();
        try {
            int code = XlibWrapper.XKeysymToKeycode(getDisplay(), sym);
            if (code == 0) {
                return 0;
            }
            long primary = XlibWrapper.XKeycodeToKeysym(getDisplay(), code, 0);
            if (sym != primary) {
                return 0;
            }
            return code;
        } finally {
            awtUnlock();
        }
    }

    static boolean getModifierState(int jkc) {
        int iKeyMask = 0;
        long ks = XKeysym.javaKeycode2Keysym(jkc);
        int kc = XlibWrapper.XKeysymToKeycode(getDisplay(), ks);
        if (kc == 0) {
            return false;
        }
        awtLock();
        try {
            XModifierKeymap modmap = new XModifierKeymap(XlibWrapper.XGetModifierMapping(getDisplay()));
            int nkeys = modmap.get_max_keypermod();
            long map_ptr = modmap.get_modifiermap();
            for (int k = 0; k < 8; k++) {
                for (int i = 0; i < nkeys; ++i) {
                    int keycode = Native.getUByte(map_ptr, k * nkeys + i);
                    if (keycode == 0) {
                        continue;
                    }
                    if (kc == keycode) {
                        iKeyMask = 1 << k;
                        break;
                    }
                }
                if (iKeyMask != 0) {
                    break;
                }
            }
            XlibWrapper.XFreeModifiermap(modmap.pData);
            if (iKeyMask == 0) {
                return false;
            }
            long window = 0;
            try {
                window = ((Long) (winMap.firstKey())).longValue();
            } catch (NoSuchElementException nex) {
                window = getDefaultRootWindow();
            }
            boolean res = XlibWrapper.XQueryPointer(getDisplay(), window, XlibWrapper.larg1, XlibWrapper.larg2, XlibWrapper.larg3, XlibWrapper.larg4, XlibWrapper.larg5, XlibWrapper.larg6, XlibWrapper.larg7);
            int mask = Native.getInt(XlibWrapper.larg7);
            return ((mask & iKeyMask) != 0);
        } finally {
            awtUnlock();
        }
    }

    static void setupModifierMap() {
        final int metaL = keysymToPrimaryKeycode(XKeySymConstants.XK_Meta_L);
        final int metaR = keysymToPrimaryKeycode(XKeySymConstants.XK_Meta_R);
        final int altL = keysymToPrimaryKeycode(XKeySymConstants.XK_Alt_L);
        final int altR = keysymToPrimaryKeycode(XKeySymConstants.XK_Alt_R);
        final int numLock = keysymToPrimaryKeycode(XKeySymConstants.XK_Num_Lock);
        final int modeSwitch = keysymToPrimaryKeycode(XKeySymConstants.XK_Mode_switch);
        final int shiftLock = keysymToPrimaryKeycode(XKeySymConstants.XK_Shift_Lock);
        final int capsLock = keysymToPrimaryKeycode(XKeySymConstants.XK_Caps_Lock);
        final int[] modmask = { XConstants.ShiftMask, XConstants.LockMask, XConstants.ControlMask, XConstants.Mod1Mask, XConstants.Mod2Mask, XConstants.Mod3Mask, XConstants.Mod4Mask, XConstants.Mod5Mask };
        log.fine("In setupModifierMap");
        awtLock();
        try {
            XModifierKeymap modmap = new XModifierKeymap(XlibWrapper.XGetModifierMapping(getDisplay()));
            int nkeys = modmap.get_max_keypermod();
            long map_ptr = modmap.get_modifiermap();
            for (int modn = XConstants.Mod1MapIndex; modn <= XConstants.Mod5MapIndex; ++modn) {
                for (int i = 0; i < nkeys; ++i) {
                    int keycode = Native.getUByte(map_ptr, modn * nkeys + i);
                    if (keycode == 0) {
                        break;
                    }
                    if (metaMask == 0 && (keycode == metaL || keycode == metaR)) {
                        metaMask = modmask[modn];
                        break;
                    }
                    if (altMask == 0 && (keycode == altL || keycode == altR)) {
                        altMask = modmask[modn];
                        break;
                    }
                    if (numLockMask == 0 && keycode == numLock) {
                        numLockMask = modmask[modn];
                        break;
                    }
                    if (modeSwitchMask == 0 && keycode == modeSwitch) {
                        modeSwitchMask = modmask[modn];
                        break;
                    }
                    continue;
                }
            }
            modLockIsShiftLock = 0;
            for (int j = 0; j < nkeys; ++j) {
                int keycode = Native.getUByte(map_ptr, XConstants.LockMapIndex * nkeys + j);
                if (keycode == 0) {
                    break;
                }
                if (keycode == shiftLock) {
                    modLockIsShiftLock = 1;
                    break;
                }
                if (keycode == capsLock) {
                    break;
                }
            }
            XlibWrapper.XFreeModifiermap(modmap.pData);
        } finally {
            awtUnlock();
        }
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("metaMask = " + metaMask);
            log.fine("altMask = " + altMask);
            log.fine("numLockMask = " + numLockMask);
            log.fine("modeSwitchMask = " + modeSwitchMask);
            log.fine("modLockIsShiftLock = " + modLockIsShiftLock);
        }
    }

    private static SortedMap timeoutTasks;

    static void remove(Runnable task) {
        if (task == null) {
            throw new NullPointerException("task is null");
        }
        awtLock();
        try {
            if (timeoutTaskLog.isLoggable(PlatformLogger.Level.FINER)) {
                timeoutTaskLog.finer("Removing task " + task);
            }
            if (timeoutTasks == null) {
                if (timeoutTaskLog.isLoggable(PlatformLogger.Level.FINER)) {
                    timeoutTaskLog.finer("Task is not scheduled");
                }
                return;
            }
            Collection values = timeoutTasks.values();
            Iterator iter = values.iterator();
            while (iter.hasNext()) {
                java.util.List list = (java.util.List) iter.next();
                boolean removed = false;
                if (list.contains(task)) {
                    list.remove(task);
                    if (list.isEmpty()) {
                        iter.remove();
                    }
                    break;
                }
            }
        } finally {
            awtUnlock();
        }
    }

    static native void wakeup_poll();

    static void schedule(Runnable task, long interval) {
        if (task == null) {
            throw new NullPointerException("task is null");
        }
        if (interval <= 0) {
            throw new IllegalArgumentException("interval " + interval + " is not positive");
        }
        awtLock();
        try {
            if (timeoutTaskLog.isLoggable(PlatformLogger.Level.FINER)) {
                timeoutTaskLog.finer("XToolkit.schedule(): current time={0}" + ";  interval={1}" + ";  task being added={2}" + ";  tasks before addition={3}", Long.valueOf(System.currentTimeMillis()), Long.valueOf(interval), task, timeoutTasks);
            }
            if (timeoutTasks == null) {
                timeoutTasks = new TreeMap();
            }
            Long time = Long.valueOf(System.currentTimeMillis() + interval);
            java.util.List tasks = (java.util.List) timeoutTasks.get(time);
            if (tasks == null) {
                tasks = new ArrayList(1);
                timeoutTasks.put(time, tasks);
            }
            tasks.add(task);
            if (timeoutTasks.get(timeoutTasks.firstKey()) == tasks && tasks.size() == 1) {
                wakeup_poll();
            }
        } finally {
            awtUnlock();
        }
    }

    private long getNextTaskTime() {
        awtLock();
        try {
            if (timeoutTasks == null || timeoutTasks.isEmpty()) {
                return -1L;
            }
            return (Long) timeoutTasks.firstKey();
        } finally {
            awtUnlock();
        }
    }

    private static void callTimeoutTasks() {
        if (timeoutTaskLog.isLoggable(PlatformLogger.Level.FINER)) {
            timeoutTaskLog.finer("XToolkit.callTimeoutTasks(): current time={0}" + ";  tasks={1}", Long.valueOf(System.currentTimeMillis()), timeoutTasks);
        }
        if (timeoutTasks == null || timeoutTasks.isEmpty()) {
            return;
        }
        Long currentTime = Long.valueOf(System.currentTimeMillis());
        Long time = (Long) timeoutTasks.firstKey();
        while (time.compareTo(currentTime) <= 0) {
            java.util.List tasks = (java.util.List) timeoutTasks.remove(time);
            for (Iterator iter = tasks.iterator(); iter.hasNext(); ) {
                Runnable task = (Runnable) iter.next();
                if (timeoutTaskLog.isLoggable(PlatformLogger.Level.FINER)) {
                    timeoutTaskLog.finer("XToolkit.callTimeoutTasks(): current time={0}" + ";  about to run task={1}", Long.valueOf(currentTime), task);
                }
                try {
                    task.run();
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable thr) {
                    processException(thr);
                }
            }
            if (timeoutTasks.isEmpty()) {
                break;
            }
            time = (Long) timeoutTasks.firstKey();
        }
    }

    static long getAwtDefaultFg() {
        return awt_defaultFg;
    }

    static boolean isLeftMouseButton(MouseEvent me) {
        switch(me.getID()) {
            case MouseEvent.MOUSE_PRESSED:
            case MouseEvent.MOUSE_RELEASED:
                return (me.getButton() == MouseEvent.BUTTON1);
            case MouseEvent.MOUSE_ENTERED:
            case MouseEvent.MOUSE_EXITED:
            case MouseEvent.MOUSE_CLICKED:
            case MouseEvent.MOUSE_DRAGGED:
                return ((me.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0);
        }
        return false;
    }

    static boolean isRightMouseButton(MouseEvent me) {
        int numButtons = ((Integer) getDefaultToolkit().getDesktopProperty("awt.mouse.numButtons")).intValue();
        switch(me.getID()) {
            case MouseEvent.MOUSE_PRESSED:
            case MouseEvent.MOUSE_RELEASED:
                return ((numButtons == 2 && me.getButton() == MouseEvent.BUTTON2) || (numButtons > 2 && me.getButton() == MouseEvent.BUTTON3));
            case MouseEvent.MOUSE_ENTERED:
            case MouseEvent.MOUSE_EXITED:
            case MouseEvent.MOUSE_CLICKED:
            case MouseEvent.MOUSE_DRAGGED:
                return ((numButtons == 2 && (me.getModifiersEx() & InputEvent.BUTTON2_DOWN_MASK) != 0) || (numButtons > 2 && (me.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0));
        }
        return false;
    }

    static long reset_time_utc;

    static final long WRAP_TIME_MILLIS = 0x00000000FFFFFFFFL;

    static long nowMillisUTC_offset(long server_offset) {
        long current_time_utc = System.currentTimeMillis();
        if (log.isLoggable(PlatformLogger.Level.FINER)) {
            log.finer("reset_time=" + reset_time_utc + ", current_time=" + current_time_utc + ", server_offset=" + server_offset + ", wrap_time=" + WRAP_TIME_MILLIS);
        }
        if ((current_time_utc - reset_time_utc) > WRAP_TIME_MILLIS) {
            reset_time_utc = System.currentTimeMillis() - getCurrentServerTime();
        }
        if (log.isLoggable(PlatformLogger.Level.FINER)) {
            log.finer("result = " + (reset_time_utc + server_offset));
        }
        return reset_time_utc + server_offset;
    }

    protected boolean needsXEmbedImpl() {
        return true;
    }

    public boolean isModalityTypeSupported(Dialog.ModalityType modalityType) {
        return (modalityType == null) || (modalityType == Dialog.ModalityType.MODELESS) || (modalityType == Dialog.ModalityType.DOCUMENT_MODAL) || (modalityType == Dialog.ModalityType.APPLICATION_MODAL) || (modalityType == Dialog.ModalityType.TOOLKIT_MODAL);
    }

    public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType exclusionType) {
        return (exclusionType == null) || (exclusionType == Dialog.ModalExclusionType.NO_EXCLUDE) || (exclusionType == Dialog.ModalExclusionType.APPLICATION_EXCLUDE) || (exclusionType == Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
    }

    static EventQueue getEventQueue(Object target) {
        AppContext appContext = targetToAppContext(target);
        if (appContext != null) {
            return (EventQueue) appContext.get(AppContext.EVENT_QUEUE_KEY);
        }
        return null;
    }

    static void removeSourceEvents(EventQueue queue, Object source, boolean removeAllEvents) {
        AWTAccessor.getEventQueueAccessor().removeSourceEvents(queue, source, removeAllEvents);
    }

    public boolean isAlwaysOnTopSupported() {
        for (XLayerProtocol proto : XWM.getWM().getProtocols(XLayerProtocol.class)) {
            if (proto.supportsLayer(XLayerProtocol.LAYER_ALWAYS_ON_TOP)) {
                return true;
            }
        }
        return false;
    }

    public boolean useBufferPerWindow() {
        return XToolkit.getBackingStoreType() == XConstants.NotUseful;
    }

    static int getBackingStoreType() {
        return backingStoreType;
    }

    private static void setBackingStoreType() {
        String prop = AccessController.doPrivileged(new sun.security.action.GetPropertyAction("sun.awt.backingStore"));
        if (prop == null) {
            backingStoreType = XConstants.NotUseful;
            if (backingStoreLog.isLoggable(PlatformLogger.Level.CONFIG)) {
                backingStoreLog.config("The system property sun.awt.backingStore is not set" + ", by default backingStore=NotUseful");
            }
            return;
        }
        if (backingStoreLog.isLoggable(PlatformLogger.Level.CONFIG)) {
            backingStoreLog.config("The system property sun.awt.backingStore is " + prop);
        }
        prop = prop.toLowerCase();
        if (prop.equals("always")) {
            backingStoreType = XConstants.Always;
        } else if (prop.equals("whenmapped")) {
            backingStoreType = XConstants.WhenMapped;
        } else {
            backingStoreType = XConstants.NotUseful;
        }
        if (backingStoreLog.isLoggable(PlatformLogger.Level.CONFIG)) {
            backingStoreLog.config("backingStore(as provided by the system property)=" + (backingStoreType == XConstants.NotUseful ? "NotUseful" : backingStoreType == XConstants.WhenMapped ? "WhenMapped" : "Always"));
        }
        if (sun.java2d.x11.X11SurfaceData.isDgaAvailable()) {
            backingStoreType = XConstants.NotUseful;
            if (backingStoreLog.isLoggable(PlatformLogger.Level.CONFIG)) {
                backingStoreLog.config("DGA is available, backingStore=NotUseful");
            }
            return;
        }
        awtLock();
        try {
            int screenCount = XlibWrapper.ScreenCount(getDisplay());
            for (int i = 0; i < screenCount; i++) {
                if (XlibWrapper.DoesBackingStore(XlibWrapper.ScreenOfDisplay(getDisplay(), i)) == XConstants.NotUseful) {
                    backingStoreType = XConstants.NotUseful;
                    if (backingStoreLog.isLoggable(PlatformLogger.Level.CONFIG)) {
                        backingStoreLog.config("Backing store is not available on the screen " + i + ", backingStore=NotUseful");
                    }
                    return;
                }
            }
        } finally {
            awtUnlock();
        }
    }

    private static int backingStoreType;

    static final int XSUN_KP_BEHAVIOR = 1;

    static final int XORG_KP_BEHAVIOR = 2;

    static final int IS_SUN_KEYBOARD = 1;

    static final int IS_NONSUN_KEYBOARD = 2;

    static final int IS_KANA_KEYBOARD = 1;

    static final int IS_NONKANA_KEYBOARD = 2;

    static int awt_IsXsunKPBehavior = 0;

    static boolean awt_UseXKB = false;

    static boolean awt_UseXKB_Calls = false;

    static int awt_XKBBaseEventCode = 0;

    static int awt_XKBEffectiveGroup = 0;

    static long awt_XKBDescPtr = 0;

    static boolean isXsunKPBehavior() {
        awtLock();
        try {
            if (awt_IsXsunKPBehavior == 0) {
                if (XlibWrapper.IsXsunKPBehavior(getDisplay())) {
                    awt_IsXsunKPBehavior = XSUN_KP_BEHAVIOR;
                } else {
                    awt_IsXsunKPBehavior = XORG_KP_BEHAVIOR;
                }
            }
            return awt_IsXsunKPBehavior == XSUN_KP_BEHAVIOR ? true : false;
        } finally {
            awtUnlock();
        }
    }

    static int sunOrNotKeyboard = 0;

    static int kanaOrNotKeyboard = 0;

    static void resetKeyboardSniffer() {
        sunOrNotKeyboard = 0;
        kanaOrNotKeyboard = 0;
    }

    static boolean isSunKeyboard() {
        if (sunOrNotKeyboard == 0) {
            if (XlibWrapper.IsSunKeyboard(getDisplay())) {
                sunOrNotKeyboard = IS_SUN_KEYBOARD;
            } else {
                sunOrNotKeyboard = IS_NONSUN_KEYBOARD;
            }
        }
        return (sunOrNotKeyboard == IS_SUN_KEYBOARD);
    }

    static boolean isKanaKeyboard() {
        if (kanaOrNotKeyboard == 0) {
            if (XlibWrapper.IsKanaKeyboard(getDisplay())) {
                kanaOrNotKeyboard = IS_KANA_KEYBOARD;
            } else {
                kanaOrNotKeyboard = IS_NONKANA_KEYBOARD;
            }
        }
        return (kanaOrNotKeyboard == IS_KANA_KEYBOARD);
    }

    static boolean isXKBenabled() {
        awtLock();
        try {
            return awt_UseXKB;
        } finally {
            awtUnlock();
        }
    }

    static boolean tryXKB() {
        awtLock();
        try {
            String name = "XKEYBOARD";
            awt_UseXKB = XlibWrapper.XQueryExtension(getDisplay(), name, XlibWrapper.larg1, XlibWrapper.larg2, XlibWrapper.larg3);
            if (awt_UseXKB) {
                awt_UseXKB_Calls = XlibWrapper.XkbLibraryVersion(XlibWrapper.larg1, XlibWrapper.larg2);
                if (awt_UseXKB_Calls) {
                    awt_UseXKB_Calls = XlibWrapper.XkbQueryExtension(getDisplay(), XlibWrapper.larg1, XlibWrapper.larg2, XlibWrapper.larg3, XlibWrapper.larg4, XlibWrapper.larg5);
                    if (awt_UseXKB_Calls) {
                        awt_XKBBaseEventCode = Native.getInt(XlibWrapper.larg2);
                        XlibWrapper.XkbSelectEvents(getDisplay(), XConstants.XkbUseCoreKbd, XConstants.XkbNewKeyboardNotifyMask | XConstants.XkbMapNotifyMask, XConstants.XkbNewKeyboardNotifyMask | XConstants.XkbMapNotifyMask);
                        XlibWrapper.XkbSelectEventDetails(getDisplay(), XConstants.XkbUseCoreKbd, XConstants.XkbStateNotify, XConstants.XkbGroupStateMask, XConstants.XkbGroupStateMask);
                        awt_XKBDescPtr = XlibWrapper.XkbGetMap(getDisplay(), XConstants.XkbKeyTypesMask | XConstants.XkbKeySymsMask | XConstants.XkbModifierMapMask | XConstants.XkbVirtualModsMask, XConstants.XkbUseCoreKbd);
                        XlibWrapper.XkbSetDetectableAutoRepeat(getDisplay(), true);
                    }
                }
            }
            return awt_UseXKB;
        } finally {
            awtUnlock();
        }
    }

    static boolean canUseXKBCalls() {
        awtLock();
        try {
            return awt_UseXKB_Calls;
        } finally {
            awtUnlock();
        }
    }

    static int getXKBEffectiveGroup() {
        awtLock();
        try {
            return awt_XKBEffectiveGroup;
        } finally {
            awtUnlock();
        }
    }

    static int getXKBBaseEventCode() {
        awtLock();
        try {
            return awt_XKBBaseEventCode;
        } finally {
            awtUnlock();
        }
    }

    static long getXKBKbdDesc() {
        awtLock();
        try {
            return awt_XKBDescPtr;
        } finally {
            awtUnlock();
        }
    }

    void freeXKB() {
        awtLock();
        try {
            if (awt_UseXKB_Calls && awt_XKBDescPtr != 0) {
                XlibWrapper.XkbFreeKeyboard(awt_XKBDescPtr, 0xFF, true);
                awt_XKBDescPtr = 0;
            }
        } finally {
            awtUnlock();
        }
    }

    private void processXkbChanges(XEvent ev) {
        XkbEvent xke = new XkbEvent(ev.getPData());
        int xkb_type = xke.get_any().get_xkb_type();
        switch(xkb_type) {
            case XConstants.XkbNewKeyboardNotify:
                if (awt_XKBDescPtr != 0) {
                    freeXKB();
                }
                awt_XKBDescPtr = XlibWrapper.XkbGetMap(getDisplay(), XConstants.XkbKeyTypesMask | XConstants.XkbKeySymsMask | XConstants.XkbModifierMapMask | XConstants.XkbVirtualModsMask, XConstants.XkbUseCoreKbd);
                break;
            case XConstants.XkbMapNotify:
                XlibWrapper.XkbGetUpdatedMap(getDisplay(), XConstants.XkbKeyTypesMask | XConstants.XkbKeySymsMask | XConstants.XkbModifierMapMask | XConstants.XkbVirtualModsMask, awt_XKBDescPtr);
                break;
            case XConstants.XkbStateNotify:
                break;
            default:
                break;
        }
    }

    private static long eventNumber;

    public static long getEventNumber() {
        awtLock();
        try {
            return eventNumber;
        } finally {
            awtUnlock();
        }
    }

    private static XEventDispatcher oops_waiter;

    private static boolean oops_updated;

    private static boolean oops_failed;

    private XAtom oops;

    private static final long WORKAROUND_SLEEP = 100;

    protected boolean syncNativeQueue(final long timeout) {
        XBaseWindow win = XBaseWindow.getXAWTRootWindow();
        if (oops_waiter == null) {
            oops_waiter = new XEventDispatcher() {

                public void dispatchEvent(XEvent e) {
                    if (e.get_type() == XConstants.SelectionNotify) {
                        XSelectionEvent pe = e.get_xselection();
                        if (pe.get_property() == oops.getAtom()) {
                            oops_updated = true;
                            awtLockNotifyAll();
                        } else if (pe.get_selection() == XAtom.get("WM_S0").getAtom() && pe.get_target() == XAtom.get("VERSION").getAtom() && pe.get_property() == 0 && XlibWrapper.XGetSelectionOwner(getDisplay(), XAtom.get("WM_S0").getAtom()) == 0) {
                            oops_failed = true;
                            awtLockNotifyAll();
                        }
                    }
                }
            };
        }
        if (oops == null) {
            oops = XAtom.get("OOPS");
        }
        awtLock();
        try {
            addEventDispatcher(win.getWindow(), oops_waiter);
            oops_updated = false;
            oops_failed = false;
            long event_number = getEventNumber();
            XAtom atom = XAtom.get("WM_S0");
            if (eventLog.isLoggable(PlatformLogger.Level.FINER)) {
                eventLog.finer("WM_S0 selection owner {0}", XlibWrapper.XGetSelectionOwner(getDisplay(), atom.getAtom()));
            }
            XlibWrapper.XConvertSelection(getDisplay(), atom.getAtom(), XAtom.get("VERSION").getAtom(), oops.getAtom(), win.getWindow(), XConstants.CurrentTime);
            XSync();
            eventLog.finer("Requested OOPS");
            long start = System.currentTimeMillis();
            while (!oops_updated && !oops_failed) {
                try {
                    awtLockWait(timeout);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if ((System.currentTimeMillis() - start > timeout) && timeout >= 0) {
                    throw new OperationTimedOut(Long.toString(System.currentTimeMillis() - start));
                }
            }
            if (oops_failed && getEventNumber() - event_number == 1) {
                awtUnlock();
                eventLog.finest("Emergency sleep");
                try {
                    Thread.sleep(WORKAROUND_SLEEP);
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                } finally {
                    awtLock();
                }
            }
            return getEventNumber() - event_number > 2;
        } finally {
            removeEventDispatcher(win.getWindow(), oops_waiter);
            eventLog.finer("Exiting syncNativeQueue");
            awtUnlock();
        }
    }

    public void grab(Window w) {
        if (w.getPeer() != null) {
            ((XWindowPeer) w.getPeer()).setGrab(true);
        }
    }

    public void ungrab(Window w) {
        if (w.getPeer() != null) {
            ((XWindowPeer) w.getPeer()).setGrab(false);
        }
    }

    public boolean isDesktopSupported() {
        return XDesktopPeer.isDesktopSupported();
    }

    public DesktopPeer createDesktopPeer(Desktop target) {
        return new XDesktopPeer();
    }

    public boolean areExtraMouseButtonsEnabled() throws HeadlessException {
        return areExtraMouseButtonsEnabled;
    }

    @Override
    public boolean isWindowOpacitySupported() {
        XNETProtocol net_protocol = XWM.getWM().getNETProtocol();
        if (net_protocol == null) {
            return false;
        }
        return net_protocol.doOpacityProtocol();
    }

    @Override
    public boolean isWindowShapingSupported() {
        return XlibUtil.isShapingSupported();
    }

    @Override
    public boolean isWindowTranslucencySupported() {
        return true;
    }

    @Override
    public boolean isTranslucencyCapable(GraphicsConfiguration gc) {
        if (!(gc instanceof X11GraphicsConfig)) {
            return false;
        }
        return ((X11GraphicsConfig) gc).isTranslucencyCapable();
    }

    public static boolean getSunAwtDisableGrab() {
        return AccessController.doPrivileged(new GetBooleanAction("sun.awt.disablegrab"));
    }
}
