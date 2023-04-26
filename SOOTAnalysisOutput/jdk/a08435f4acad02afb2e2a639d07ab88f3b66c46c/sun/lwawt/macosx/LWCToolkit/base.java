package sun.lwawt.macosx;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.*;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.event.InputEvent;
import java.awt.event.InvocationEvent;
import java.awt.event.KeyEvent;
import java.awt.im.InputMethodHighlight;
import java.awt.peer.*;
import java.lang.reflect.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.Callable;
import sun.awt.*;
import sun.lwawt.*;
import sun.lwawt.LWWindowPeer.PeerType;
import sun.security.action.GetBooleanAction;

class NamedCursor extends Cursor {

    NamedCursor(String name) {
        super(name);
    }
}

public final class LWCToolkit extends LWToolkit {

    private static final int BUTTONS = 5;

    private static native void initIDs();

    private static CInputMethodDescriptor sInputMethodDescriptor;

    static {
        System.err.flush();
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Object>() {

            public Object run() {
                System.loadLibrary("awt");
                System.loadLibrary("fontmanager");
                return null;
            }
        });
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    public LWCToolkit() {
        SunToolkit.setDataTransfererClassName("sun.lwawt.macosx.CDataTransferer");
        areExtraMouseButtonsEnabled = Boolean.parseBoolean(System.getProperty("sun.awt.enableExtraMouseButtons", "true"));
        System.setProperty("sun.awt.enableExtraMouseButtons", "" + areExtraMouseButtonsEnabled);
    }

    private final static int NUM_APPLE_COLORS = 3;

    public final static int KEYBOARD_FOCUS_COLOR = 0;

    public final static int INACTIVE_SELECTION_BACKGROUND_COLOR = 1;

    public final static int INACTIVE_SELECTION_FOREGROUND_COLOR = 2;

    private static int[] appleColors = { 0xFF808080, 0xFFC0C0C0, 0xFF303030 };

    private native void loadNativeColors(final int[] systemColors, final int[] appleColors);

    protected void loadSystemColors(final int[] systemColors) {
        if (systemColors == null)
            return;
        loadNativeColors(systemColors, appleColors);
    }

    private static class AppleSpecificColor extends Color {

        int index;

        public AppleSpecificColor(int index) {
            super(appleColors[index]);
            this.index = index;
        }

        public int getRGB() {
            return appleColors[index];
        }
    }

    public static Color getAppleColor(int color) {
        return new AppleSpecificColor(color);
    }

    static void systemColorsChanged() {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                AccessController.doPrivileged(new PrivilegedAction<Object>() {

                    public Object run() {
                        try {
                            final Method updateColorsMethod = SystemColor.class.getDeclaredMethod("updateSystemColors", new Class[0]);
                            updateColorsMethod.setAccessible(true);
                            updateColorsMethod.invoke(null, new Object[0]);
                        } catch (final Throwable e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                });
            }
        });
    }

    public static LWCToolkit getLWCToolkit() {
        return (LWCToolkit) Toolkit.getDefaultToolkit();
    }

    @Override
    protected PlatformWindow createPlatformWindow(PeerType peerType) {
        if (peerType == PeerType.EMBEDDED_FRAME) {
            return new CPlatformEmbeddedFrame();
        } else if (peerType == PeerType.VIEW_EMBEDDED_FRAME) {
            return new CViewPlatformEmbeddedFrame();
        } else if (peerType == PeerType.LW_FRAME) {
            return new CPlatformLWWindow();
        } else {
            assert (peerType == PeerType.SIMPLEWINDOW || peerType == PeerType.DIALOG || peerType == PeerType.FRAME);
            return new CPlatformWindow();
        }
    }

    @Override
    protected PlatformComponent createPlatformComponent() {
        return new CPlatformComponent();
    }

    @Override
    protected PlatformComponent createLwPlatformComponent() {
        return new CPlatformLWComponent();
    }

    @Override
    protected FileDialogPeer createFileDialogPeer(FileDialog target) {
        return new CFileDialog(target);
    }

    @Override
    public MenuPeer createMenu(Menu target) {
        MenuPeer peer = new CMenu(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public MenuBarPeer createMenuBar(MenuBar target) {
        MenuBarPeer peer = new CMenuBar(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public MenuItemPeer createMenuItem(MenuItem target) {
        MenuItemPeer peer = new CMenuItem(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem target) {
        CheckboxMenuItemPeer peer = new CCheckboxMenuItem(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public PopupMenuPeer createPopupMenu(PopupMenu target) {
        PopupMenuPeer peer = new CPopupMenu(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public SystemTrayPeer createSystemTray(SystemTray target) {
        SystemTrayPeer peer = new CSystemTray();
        return peer;
    }

    @Override
    public TrayIconPeer createTrayIcon(TrayIcon target) {
        TrayIconPeer peer = new CTrayIcon(target);
        targetCreatedPeer(target, peer);
        return peer;
    }

    @Override
    public LWCursorManager getCursorManager() {
        return CCursorManager.getInstance();
    }

    @Override
    public Cursor createCustomCursor(final Image cursor, final Point hotSpot, final String name) throws IndexOutOfBoundsException, HeadlessException {
        return new CCustomCursor(cursor, hotSpot, name);
    }

    @Override
    public Dimension getBestCursorSize(final int preferredWidth, final int preferredHeight) throws HeadlessException {
        return CCustomCursor.getBestCursorSize(preferredWidth, preferredHeight);
    }

    @Override
    protected void platformCleanup() {
    }

    @Override
    protected void platformInit() {
    }

    @Override
    protected void platformRunMessage() {
    }

    @Override
    protected void platformShutdown() {
    }

    class OSXPlatformFont extends sun.awt.PlatformFont {

        public OSXPlatformFont(String name, int style) {
            super(name, style);
        }

        protected char getMissingGlyphCharacter() {
            return (char) 0xfff8;
        }
    }

    public FontPeer getFontPeer(String name, int style) {
        return new OSXPlatformFont(name, style);
    }

    @Override
    protected MouseInfoPeer createMouseInfoPeerImpl() {
        return new CMouseInfoPeer();
    }

    @Override
    protected int getScreenHeight() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds().height;
    }

    @Override
    protected int getScreenWidth() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getBounds().width;
    }

    @Override
    protected void initializeDesktopProperties() {
        super.initializeDesktopProperties();
        Map<Object, Object> fontHints = new HashMap<Object, Object>();
        fontHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        fontHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        desktopProperties.put(SunToolkit.DESKTOPFONTHINTS, fontHints);
        desktopProperties.put("awt.mouse.numButtons", BUTTONS);
        desktopProperties.put("DnD.Autoscroll.initialDelay", new Integer(50));
        desktopProperties.put("DnD.Autoscroll.interval", new Integer(50));
        desktopProperties.put("DnD.Autoscroll.cursorHysteresis", new Integer(5));
        desktopProperties.put("DnD.isDragImageSupported", new Boolean(true));
        desktopProperties.put("DnD.Cursor.CopyDrop", new NamedCursor("DnD.Cursor.CopyDrop"));
        desktopProperties.put("DnD.Cursor.MoveDrop", new NamedCursor("DnD.Cursor.MoveDrop"));
        desktopProperties.put("DnD.Cursor.LinkDrop", new NamedCursor("DnD.Cursor.LinkDrop"));
        desktopProperties.put("DnD.Cursor.CopyNoDrop", new NamedCursor("DnD.Cursor.CopyNoDrop"));
        desktopProperties.put("DnD.Cursor.MoveNoDrop", new NamedCursor("DnD.Cursor.MoveNoDrop"));
        desktopProperties.put("DnD.Cursor.LinkNoDrop", new NamedCursor("DnD.Cursor.LinkNoDrop"));
    }

    @Override
    protected boolean syncNativeQueue(long timeout) {
        return nativeSyncQueue(timeout);
    }

    @Override
    public native void beep();

    @Override
    public int getScreenResolution() throws HeadlessException {
        return (int) ((CGraphicsDevice) GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()).getXResolution();
    }

    @Override
    public Insets getScreenInsets(final GraphicsConfiguration gc) {
        return ((CGraphicsConfig) gc).getDevice().getScreenInsets();
    }

    @Override
    public void sync() {
    }

    @Override
    public RobotPeer createRobot(Robot target, GraphicsDevice screen) {
        return new CRobot(target, (CGraphicsDevice) screen);
    }

    private native boolean isCapsLockOn();

    public boolean getLockingKeyState(int keyCode) throws UnsupportedOperationException {
        switch(keyCode) {
            case KeyEvent.VK_NUM_LOCK:
            case KeyEvent.VK_SCROLL_LOCK:
            case KeyEvent.VK_KANA_LOCK:
                throw new UnsupportedOperationException("Toolkit.getLockingKeyState");
            case KeyEvent.VK_CAPS_LOCK:
                return isCapsLockOn();
            default:
                throw new IllegalArgumentException("invalid key for Toolkit.getLockingKeyState");
        }
    }

    private static boolean areExtraMouseButtonsEnabled = true;

    public boolean areExtraMouseButtonsEnabled() throws HeadlessException {
        return areExtraMouseButtonsEnabled;
    }

    public int getNumberOfButtons() {
        return BUTTONS;
    }

    @Override
    public boolean isTraySupported() {
        return true;
    }

    @Override
    public boolean isAlwaysOnTopSupported() {
        return true;
    }

    private static void installToolkitThreadNameInJava() {
        Thread.currentThread().setName(CThreading.APPKIT_THREAD_NAME);
    }

    @Override
    public boolean isWindowOpacitySupported() {
        return true;
    }

    @Override
    public boolean isFrameStateSupported(int state) throws HeadlessException {
        switch(state) {
            case Frame.NORMAL:
            case Frame.ICONIFIED:
            case Frame.MAXIMIZED_BOTH:
                return true;
            default:
                return false;
        }
    }

    public int getMenuShortcutKeyMask() {
        return Event.META_MASK;
    }

    @Override
    public Image getImage(final String filename) {
        final Image nsImage = checkForNSImage(filename);
        if (nsImage != null)
            return nsImage;
        return super.getImage(filename);
    }

    static final String nsImagePrefix = "NSImage://";

    protected Image checkForNSImage(final String imageName) {
        if (imageName == null)
            return null;
        if (!imageName.startsWith(nsImagePrefix))
            return null;
        return CImage.getCreator().createImageFromName(imageName.substring(nsImagePrefix.length()));
    }

    public static boolean doEquals(final Object a, final Object b, Component c) {
        if (a == b)
            return true;
        final boolean[] ret = new boolean[1];
        try {
            invokeAndWait(new Runnable() {

                public void run() {
                    synchronized (ret) {
                        ret[0] = a.equals(b);
                    }
                }
            }, c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        synchronized (ret) {
            return ret[0];
        }
    }

    public static <T> T invokeAndWait(final Callable<T> callable, Component component) throws Exception {
        final CallableWrapper<T> wrapper = new CallableWrapper<T>(callable);
        invokeAndWait(wrapper, component);
        return wrapper.getResult();
    }

    static final class CallableWrapper<T> implements Runnable {

        final Callable<T> callable;

        T object;

        Exception e;

        public CallableWrapper(final Callable<T> callable) {
            this.callable = callable;
        }

        public void run() {
            try {
                object = callable.call();
            } catch (final Exception e) {
                this.e = e;
            }
        }

        public T getResult() throws Exception {
            if (e != null)
                throw e;
            return object;
        }
    }

    public static void invokeAndWait(Runnable event, Component component) throws InterruptedException, InvocationTargetException {
        final long mediator = createAWTRunLoopMediator();
        InvocationEvent invocationEvent = new InvocationEvent(component != null ? component : Toolkit.getDefaultToolkit(), event) {

            @Override
            public void dispatch() {
                try {
                    super.dispatch();
                } finally {
                    if (mediator != 0) {
                        stopAWTRunLoop(mediator);
                    }
                }
            }
        };
        if (component != null) {
            AppContext appContext = SunToolkit.targetToAppContext(component);
            SunToolkit.postEvent(appContext, invocationEvent);
            SunToolkit.flushPendingEvents(appContext);
        } else {
            ((LWCToolkit) Toolkit.getDefaultToolkit()).getSystemEventQueueForInvokeAndWait().postEvent(invocationEvent);
        }
        doAWTRunLoop(mediator, false);
        Throwable eventException = invocationEvent.getException();
        if (eventException != null) {
            if (eventException instanceof UndeclaredThrowableException) {
                eventException = ((UndeclaredThrowableException) eventException).getUndeclaredThrowable();
            }
            throw new InvocationTargetException(eventException);
        }
    }

    public static void invokeLater(Runnable event, Component component) throws InvocationTargetException {
        final InvocationEvent invocationEvent = new InvocationEvent(component != null ? component : Toolkit.getDefaultToolkit(), event);
        if (component != null) {
            final AppContext appContext = SunToolkit.targetToAppContext(component);
            SunToolkit.postEvent(appContext, invocationEvent);
            SunToolkit.flushPendingEvents(appContext);
        } else {
            ((LWCToolkit) Toolkit.getDefaultToolkit()).getSystemEventQueueForInvokeAndWait().postEvent(invocationEvent);
        }
        final Throwable eventException = invocationEvent.getException();
        if (eventException == null)
            return;
        if (eventException instanceof UndeclaredThrowableException) {
            throw new InvocationTargetException(((UndeclaredThrowableException) eventException).getUndeclaredThrowable());
        }
        throw new InvocationTargetException(eventException);
    }

    EventQueue getSystemEventQueueForInvokeAndWait() {
        return getSystemEventQueueImpl();
    }

    public DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) throws InvalidDnDOperationException {
        DragSourceContextPeer dscp = CDragSourceContextPeer.createDragSourceContextPeer(dge);
        return dscp;
    }

    public <T extends DragGestureRecognizer> T createDragGestureRecognizer(Class<T> abstractRecognizerClass, DragSource ds, Component c, int srcActions, DragGestureListener dgl) {
        DragGestureRecognizer dgr = null;
        if (MouseDragGestureRecognizer.class.equals(abstractRecognizerClass))
            dgr = new CMouseDragGestureRecognizer(ds, c, srcActions, dgl);
        return (T) dgr;
    }

    public Locale getDefaultKeyboardLocale() {
        Locale locale = CInputMethod.getNativeLocale();
        if (locale == null) {
            return super.getDefaultKeyboardLocale();
        }
        return locale;
    }

    public java.awt.im.spi.InputMethodDescriptor getInputMethodAdapterDescriptor() {
        if (sInputMethodDescriptor == null)
            sInputMethodDescriptor = new CInputMethodDescriptor();
        return sInputMethodDescriptor;
    }

    public Map mapInputMethodHighlight(InputMethodHighlight highlight) {
        return CInputMethod.mapInputMethodHighlight(highlight);
    }

    @Override
    public int getFocusAcceleratorKeyMask() {
        return InputEvent.CTRL_MASK | InputEvent.ALT_MASK;
    }

    @Override
    public boolean isPrintableCharacterModifiersMask(int mods) {
        return ((mods & (InputEvent.META_MASK | InputEvent.CTRL_MASK)) == 0);
    }

    @Override
    public boolean canPopupOverlapTaskBar() {
        return false;
    }

    private static Boolean sunAwtDisableCALayers = null;

    public synchronized static boolean getSunAwtDisableCALayers() {
        if (sunAwtDisableCALayers == null) {
            sunAwtDisableCALayers = AccessController.doPrivileged(new GetBooleanAction("sun.awt.disableCALayers"));
        }
        return sunAwtDisableCALayers.booleanValue();
    }

    public native boolean isApplicationActive();

    static native long createAWTRunLoopMediator();

    static native void doAWTRunLoop(long mediator, boolean processEvents);

    static native void stopAWTRunLoop(long mediator);

    private native boolean nativeSyncQueue(long timeout);

    @Override
    public Clipboard createPlatformClipboard() {
        return new CClipboard("System");
    }

    @Override
    public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType exclusionType) {
        return (exclusionType == null) || (exclusionType == Dialog.ModalExclusionType.NO_EXCLUDE) || (exclusionType == Dialog.ModalExclusionType.APPLICATION_EXCLUDE) || (exclusionType == Dialog.ModalExclusionType.TOOLKIT_EXCLUDE);
    }

    @Override
    public boolean isModalityTypeSupported(Dialog.ModalityType modalityType) {
        return (modalityType == null) || (modalityType == Dialog.ModalityType.MODELESS) || (modalityType == Dialog.ModalityType.DOCUMENT_MODAL) || (modalityType == Dialog.ModalityType.APPLICATION_MODAL) || (modalityType == Dialog.ModalityType.TOOLKIT_MODAL);
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

    public boolean isSwingBackbufferTranslucencySupported() {
        return true;
    }

    @Override
    public boolean enableInputMethodsForTextComponent() {
        return true;
    }
}
