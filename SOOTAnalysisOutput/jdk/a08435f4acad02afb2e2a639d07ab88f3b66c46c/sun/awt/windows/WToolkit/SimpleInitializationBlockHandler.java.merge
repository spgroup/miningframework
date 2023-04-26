package sun.awt.windows;

import java.awt.*;
import java.awt.im.InputMethodHighlight;
import java.awt.im.spi.InputMethodDescriptor;
import java.awt.image.*;
import java.awt.peer.*;
import java.awt.event.KeyEvent;
import java.awt.datatransfer.Clipboard;
import java.awt.TrayIcon;
import java.beans.PropertyChangeListener;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.awt.AWTAutoShutdown;
import sun.awt.LightweightFrame;
import sun.awt.SunToolkit;
import sun.awt.Win32GraphicsDevice;
import sun.awt.Win32GraphicsEnvironment;
import sun.java2d.d3d.D3DRenderQueue;
import sun.java2d.opengl.OGLRenderQueue;
import sun.print.PrintJob2D;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.MouseDragGestureRecognizer;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import sun.font.FontManager;
import sun.font.FontManagerFactory;
import sun.font.SunFontManager;
import sun.misc.PerformanceLogger;
import sun.util.logging.PlatformLogger;

public class WToolkit extends SunToolkit implements Runnable {

    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.windows.WToolkit");

    static GraphicsConfiguration config;

    WClipboard clipboard;

    private Hashtable<String, FontPeer> cacheFontPeer;

    private WDesktopProperties wprops;

    protected boolean dynamicLayoutSetting = false;

    private static boolean areExtraMouseButtonsEnabled = true;

    private static native void initIDs();

    private static boolean loaded = false;

    public static void loadLibraries() {
        if (!loaded) {
            java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Void>() {

                public Void run() {
                    System.loadLibrary("awt");
                    return null;
                }
            });
            loaded = true;
        }
    }

    private static native String getWindowsVersion();

    static {
        loadLibraries();
        initIDs();
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("Win version: " + getWindowsVersion());
        }
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            public Void run() {
                String browserProp = System.getProperty("browser");
                if (browserProp != null && browserProp.equals("sun.plugin")) {
                    disableCustomPalette();
                }
                return null;
            }
        });
    }

    private static native void disableCustomPalette();

    public static void resetGC() {
        if (GraphicsEnvironment.isHeadless()) {
            config = null;
        } else {
            config = (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
        }
    }

    public static native boolean embeddedInit();

    public static native boolean embeddedDispose();

    public native void embeddedEventLoopIdleProcessing();

    public static final String DATA_TRANSFERER_CLASS_NAME = "sun.awt.windows.WDataTransferer";

    static class ToolkitDisposer implements sun.java2d.DisposerRecord {

        public void dispose() {
            WToolkit.postDispose();
        }
    }

    private final Object anchor = new Object();

    private static native void postDispose();

    private static native boolean startToolkitThread(Runnable thread);

    public WToolkit() {
        if (PerformanceLogger.loggingEnabled()) {
            PerformanceLogger.setTime("WToolkit construction");
        }
        sun.java2d.Disposer.addRecord(anchor, new ToolkitDisposer());
        AWTAutoShutdown.notifyToolkitThreadBusy();
        if (!startToolkitThread(this)) {
            Thread toolkitThread = new Thread(this, "AWT-Windows");
            toolkitThread.setDaemon(true);
            toolkitThread.start();
        }
        try {
            synchronized (this) {
                while (!inited) {
                    wait();
                }
            }
        } catch (InterruptedException x) {
        }
        SunToolkit.setDataTransfererClassName(DATA_TRANSFERER_CLASS_NAME);
        setDynamicLayout(true);
        areExtraMouseButtonsEnabled = Boolean.parseBoolean(System.getProperty("sun.awt.enableExtraMouseButtons", "true"));
        System.setProperty("sun.awt.enableExtraMouseButtons", "" + areExtraMouseButtonsEnabled);
        setExtraMouseButtonsEnabledNative(areExtraMouseButtonsEnabled);
    }

    private final void registerShutdownHook() {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            public Void run() {
                ThreadGroup currentTG = Thread.currentThread().getThreadGroup();
                ThreadGroup parentTG = currentTG.getParent();
                while (parentTG != null) {
                    currentTG = parentTG;
                    parentTG = currentTG.getParent();
                }
                Thread shutdown = new Thread(currentTG, new Runnable() {

                    public void run() {
                        shutdown();
                    }
                });
                shutdown.setContextClassLoader(null);
                Runtime.getRuntime().addShutdownHook(shutdown);
                return null;
            }
        });
    }

    public void run() {
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 1);
        boolean startPump = init();
        if (startPump) {
            registerShutdownHook();
        }
        synchronized (this) {
            inited = true;
            notifyAll();
        }
        if (startPump) {
            eventLoop();
        }
    }

    private native boolean init();

    private boolean inited = false;

    private native void eventLoop();

    private native void shutdown();

    public static native void startSecondaryEventLoop();

    public static native void quitSecondaryEventLoop();

    public ButtonPeer createButton(Button target) {
        ButtonPeer peer = new WButtonPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public TextFieldPeer createTextField(TextField target) {
        TextFieldPeer peer = new WTextFieldPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public LabelPeer createLabel(Label target) {
        LabelPeer peer = new WLabelPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public ListPeer createList(List target) {
        ListPeer peer = new WListPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public CheckboxPeer createCheckbox(Checkbox target) {
        CheckboxPeer peer = new WCheckboxPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public ScrollbarPeer createScrollbar(Scrollbar target) {
        ScrollbarPeer peer = new WScrollbarPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public ScrollPanePeer createScrollPane(ScrollPane target) {
        ScrollPanePeer peer = new WScrollPanePeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public TextAreaPeer createTextArea(TextArea target) {
        TextAreaPeer peer = new WTextAreaPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public ChoicePeer createChoice(Choice target) {
        ChoicePeer peer = new WChoicePeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public FramePeer createFrame(Frame target) {
        FramePeer peer = new WFramePeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public FramePeer createLightweightFrame(LightweightFrame target) {
        FramePeer peer = new WLightweightFramePeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public CanvasPeer createCanvas(Canvas target) {
        CanvasPeer peer = new WCanvasPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    @SuppressWarnings("deprecation")
    public void disableBackgroundErase(Canvas canvas) {
        WCanvasPeer peer = (WCanvasPeer) canvas.getPeer();
        if (peer == null) {
            throw new IllegalStateException("Canvas must have a valid peer");
        }
        peer.disableBackgroundErase();
    }

    public PanelPeer createPanel(Panel target) {
        PanelPeer peer = new WPanelPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public WindowPeer createWindow(Window target) {
        WindowPeer peer = new WWindowPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public DialogPeer createDialog(Dialog target) {
        DialogPeer peer = new WDialogPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public FileDialogPeer createFileDialog(FileDialog target) {
        FileDialogPeer peer = new WFileDialogPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public MenuBarPeer createMenuBar(MenuBar target) {
        MenuBarPeer peer = new WMenuBarPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public MenuPeer createMenu(Menu target) {
        MenuPeer peer = new WMenuPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public PopupMenuPeer createPopupMenu(PopupMenu target) {
        PopupMenuPeer peer = new WPopupMenuPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public MenuItemPeer createMenuItem(MenuItem target) {
        MenuItemPeer peer = new WMenuItemPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem target) {
        CheckboxMenuItemPeer peer = new WCheckboxMenuItemPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public RobotPeer createRobot(Robot target, GraphicsDevice screen) {
        return new WRobotPeer(screen);
    }

    public WEmbeddedFramePeer createEmbeddedFrame(WEmbeddedFrame target) {
        WEmbeddedFramePeer peer = new WEmbeddedFramePeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    WPrintDialogPeer createWPrintDialog(WPrintDialog target) {
        WPrintDialogPeer peer = new WPrintDialogPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    WPageDialogPeer createWPageDialog(WPageDialog target) {
        WPageDialogPeer peer = new WPageDialogPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public TrayIconPeer createTrayIcon(TrayIcon target) {
        WTrayIconPeer peer = new WTrayIconPeer(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    public SystemTrayPeer createSystemTray(SystemTray target) {
        return new WSystemTrayPeer(target);
    }

    public boolean isTraySupported() {
        return true;
    }

    public KeyboardFocusManagerPeer getKeyboardFocusManagerPeer() throws HeadlessException {
        return WKeyboardFocusManagerPeer.getInstance();
    }

    protected native void setDynamicLayoutNative(boolean b);

    public void setDynamicLayout(boolean b) {
        if (b == dynamicLayoutSetting) {
            return;
        }
        dynamicLayoutSetting = b;
        setDynamicLayoutNative(b);
    }

    protected boolean isDynamicLayoutSet() {
        return dynamicLayoutSetting;
    }

    protected native boolean isDynamicLayoutSupportedNative();

    public boolean isDynamicLayoutActive() {
        return (isDynamicLayoutSet() && isDynamicLayoutSupported());
    }

    public boolean isFrameStateSupported(int state) {
        switch(state) {
            case Frame.NORMAL:
            case Frame.ICONIFIED:
            case Frame.MAXIMIZED_BOTH:
                return true;
            default:
                return false;
        }
    }

    static native ColorModel makeColorModel();

    static ColorModel screenmodel;

    static ColorModel getStaticColorModel() {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalArgumentException();
        }
        if (config == null) {
            resetGC();
        }
        return config.getColorModel();
    }

    public ColorModel getColorModel() {
        return getStaticColorModel();
    }

    public Insets getScreenInsets(GraphicsConfiguration gc) {
        return getScreenInsets(((Win32GraphicsDevice) gc.getDevice()).getScreen());
    }

    public int getScreenResolution() {
        Win32GraphicsEnvironment ge = (Win32GraphicsEnvironment) GraphicsEnvironment.getLocalGraphicsEnvironment();
        return ge.getXResolution();
    }

    protected native int getScreenWidth();

    protected native int getScreenHeight();

    protected native Insets getScreenInsets(int screen);

    public FontMetrics getFontMetrics(Font font) {
        FontManager fm = FontManagerFactory.getInstance();
        if (fm instanceof SunFontManager && ((SunFontManager) fm).usePlatformFontMetrics()) {
            return WFontMetrics.getFontMetrics(font);
        }
        return super.getFontMetrics(font);
    }

    public FontPeer getFontPeer(String name, int style) {
        FontPeer retval = null;
        String lcName = name.toLowerCase();
        if (null != cacheFontPeer) {
            retval = cacheFontPeer.get(lcName + style);
            if (null != retval) {
                return retval;
            }
        }
        retval = new WFontPeer(name, style);
        if (retval != null) {
            if (null == cacheFontPeer) {
                cacheFontPeer = new Hashtable<>(5, 0.9f);
            }
            if (null != cacheFontPeer) {
                cacheFontPeer.put(lcName + style, retval);
            }
        }
        return retval;
    }

    private native void nativeSync();

    public void sync() {
        nativeSync();
        OGLRenderQueue.sync();
        D3DRenderQueue.sync();
    }

    public PrintJob getPrintJob(Frame frame, String doctitle, Properties props) {
        return getPrintJob(frame, doctitle, null, null);
    }

    public PrintJob getPrintJob(Frame frame, String doctitle, JobAttributes jobAttributes, PageAttributes pageAttributes) {
        if (frame == null) {
            throw new NullPointerException("frame must not be null");
        }
        PrintJob2D printJob = new PrintJob2D(frame, doctitle, jobAttributes, pageAttributes);
        if (printJob.printDialog() == false) {
            printJob = null;
        }
        return printJob;
    }

    public native void beep();

    public boolean getLockingKeyState(int key) {
        if (!(key == KeyEvent.VK_CAPS_LOCK || key == KeyEvent.VK_NUM_LOCK || key == KeyEvent.VK_SCROLL_LOCK || key == KeyEvent.VK_KANA_LOCK)) {
            throw new IllegalArgumentException("invalid key for Toolkit.getLockingKeyState");
        }
        return getLockingKeyStateNative(key);
    }

    public native boolean getLockingKeyStateNative(int key);

    public void setLockingKeyState(int key, boolean on) {
        if (!(key == KeyEvent.VK_CAPS_LOCK || key == KeyEvent.VK_NUM_LOCK || key == KeyEvent.VK_SCROLL_LOCK || key == KeyEvent.VK_KANA_LOCK)) {
            throw new IllegalArgumentException("invalid key for Toolkit.setLockingKeyState");
        }
        setLockingKeyStateNative(key, on);
    }

    public native void setLockingKeyStateNative(int key, boolean on);

    public Clipboard getSystemClipboard() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSystemClipboardAccess();
        }
        synchronized (this) {
            if (clipboard == null) {
                clipboard = new WClipboard();
            }
        }
        return clipboard;
    }

    protected native void loadSystemColors(int[] systemColors);

    public static final Object targetToPeer(Object target) {
        return SunToolkit.targetToPeer(target);
    }

    public static final void targetDisposedPeer(Object target, Object peer) {
        SunToolkit.targetDisposedPeer(target, peer);
    }

    public InputMethodDescriptor getInputMethodAdapterDescriptor() {
        return new WInputMethodDescriptor();
    }

    public Map<java.awt.font.TextAttribute, ?> mapInputMethodHighlight(InputMethodHighlight highlight) {
        return WInputMethod.mapInputMethodHighlight(highlight);
    }

    public boolean enableInputMethodsForTextComponent() {
        return true;
    }

    public Locale getDefaultKeyboardLocale() {
        Locale locale = WInputMethod.getNativeLocale();
        if (locale == null) {
            return super.getDefaultKeyboardLocale();
        } else {
            return locale;
        }
    }

    public Cursor createCustomCursor(Image cursor, Point hotSpot, String name) throws IndexOutOfBoundsException {
        return new WCustomCursor(cursor, hotSpot, name);
    }

    public Dimension getBestCursorSize(int preferredWidth, int preferredHeight) {
        return new Dimension(WCustomCursor.getCursorWidth(), WCustomCursor.getCursorHeight());
    }

    public native int getMaximumCursorColors();

    static void paletteChanged() {
        ((Win32GraphicsEnvironment) GraphicsEnvironment.getLocalGraphicsEnvironment()).paletteChanged();
    }

    static public void displayChanged() {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                ((Win32GraphicsEnvironment) GraphicsEnvironment.getLocalGraphicsEnvironment()).displayChanged();
            }
        });
    }

    public DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) throws InvalidDnDOperationException {
        return WDragSourceContextPeer.createDragSourceContextPeer(dge);
    }

    public <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> abstractRecognizerClass, DragSource ds, Component c, int srcActions, DragGestureListener dgl) {
        if (MouseDragGestureRecognizer.class.equals(abstractRecognizerClass))
            return (T) new WMouseDragGestureRecognizer(ds, c, srcActions, dgl);
        else
            return null;
    }

    private static final String prefix = "DnD.Cursor.";

    private static final String postfix = ".32x32";

    private static final String awtPrefix = "awt.";

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
        if (WDesktopProperties.isWindowsProperty(name) || name.startsWith(awtPrefix) || name.startsWith(dndPrefix)) {
            synchronized (this) {
                lazilyInitWProps();
                return desktopProperties.get(name);
            }
        }
        return super.lazilyLoadDesktopProperty(name);
    }

    private synchronized void lazilyInitWProps() {
        if (wprops == null) {
            wprops = new WDesktopProperties(this);
            updateProperties();
        }
    }

    private synchronized boolean isDynamicLayoutSupported() {
        boolean nativeDynamic = isDynamicLayoutSupportedNative();
        lazilyInitWProps();
        Boolean prop = (Boolean) desktopProperties.get("awt.dynamicLayoutSupported");
        if (log.isLoggable(PlatformLogger.Level.FINER)) {
            log.finer("In WTK.isDynamicLayoutSupported()" + "   nativeDynamic == " + nativeDynamic + "   wprops.dynamic == " + prop);
        }
        if ((prop == null) || (nativeDynamic != prop.booleanValue())) {
            windowsSettingChange();
            return nativeDynamic;
        }
        return prop.booleanValue();
    }

    private void windowsSettingChange() {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                updateProperties();
            }
        });
    }

    private synchronized void updateProperties() {
        if (null == wprops) {
            return;
        }
        Map<String, Object> props = wprops.getProperties();
        for (String propName : props.keySet()) {
            Object val = props.get(propName);
            if (log.isLoggable(PlatformLogger.Level.FINER)) {
                log.finer("changed " + propName + " to " + val);
            }
            setDesktopProperty(propName, val);
        }
    }

    public synchronized void addPropertyChangeListener(String name, PropertyChangeListener pcl) {
        if (name == null) {
            return;
        }
        if (WDesktopProperties.isWindowsProperty(name) || name.startsWith(awtPrefix) || name.startsWith(dndPrefix)) {
            lazilyInitWProps();
        }
        super.addPropertyChangeListener(name, pcl);
    }

    protected synchronized void initializeDesktopProperties() {
        desktopProperties.put("DnD.Autoscroll.initialDelay", Integer.valueOf(50));
        desktopProperties.put("DnD.Autoscroll.interval", Integer.valueOf(50));
        desktopProperties.put("DnD.isDragImageSupported", Boolean.TRUE);
        desktopProperties.put("Shell.shellFolderManager", "sun.awt.shell.Win32ShellFolderManager2");
    }

    protected synchronized RenderingHints getDesktopAAHints() {
        if (wprops == null) {
            return null;
        } else {
            return wprops.getDesktopAAHints();
        }
    }

    public boolean isModalityTypeSupported(Dialog.ModalityType modalityType) {
        return (modalityType == null) || (modalityType == Dialog.ModalityType.MODELESS) || (modalityType == Dialog.ModalityType.DOCUMENT_MODAL) || (modalityType == Dialog.ModalityType.APPLICATION_MODAL) || (modalityType == Dialog.ModalityType.TOOLKIT_MODAL);
    }

    public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType exclusionType) {
        return (exclusionType == null) || (exclusionType == Dialog.ModalExclusionType.NO_EXCLUDE) || (exclusionType == Dialog.ModalExclusionType.APPLICATION_EXCLUDE) || (exclusionType == Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
    }

    public static WToolkit getWToolkit() {
        WToolkit toolkit = (WToolkit) Toolkit.getDefaultToolkit();
        return toolkit;
    }

    @Override
    public boolean useBufferPerWindow() {
        return !Win32GraphicsEnvironment.isDWMCompositionEnabled();
    }

    @SuppressWarnings("deprecation")
    public void grab(Window w) {
        if (w.getPeer() != null) {
            ((WWindowPeer) w.getPeer()).grab();
        }
    }

    @SuppressWarnings("deprecation")
    public void ungrab(Window w) {
        if (w.getPeer() != null) {
            ((WWindowPeer) w.getPeer()).ungrab();
        }
    }

    public native boolean syncNativeQueue(final long timeout);

    public boolean isDesktopSupported() {
        return true;
    }

    public DesktopPeer createDesktopPeer(Desktop target) {
        return new WDesktopPeer();
    }

    public static native void setExtraMouseButtonsEnabledNative(boolean enable);

    public boolean areExtraMouseButtonsEnabled() throws HeadlessException {
        return areExtraMouseButtonsEnabled;
    }

    private native synchronized int getNumberOfButtonsImpl();

    @Override
    public int getNumberOfButtons() {
        if (numberOfButtons == 0) {
            numberOfButtons = getNumberOfButtonsImpl();
        }
        return (numberOfButtons > MAX_BUTTONS_SUPPORTED) ? MAX_BUTTONS_SUPPORTED : numberOfButtons;
    }

    @Override
    public boolean isWindowOpacitySupported() {
        return true;
    }

    @Override
    public boolean isWindowShapingSupported() {
        return true;
    }

    @Override
    public boolean isWindowTranslucencySupported() {
        return true;
    }

    @Override
    public boolean isTranslucencyCapable(GraphicsConfiguration gc) {
        return true;
    }

    @Override
    public boolean needUpdateWindow() {
        return true;
    }
}