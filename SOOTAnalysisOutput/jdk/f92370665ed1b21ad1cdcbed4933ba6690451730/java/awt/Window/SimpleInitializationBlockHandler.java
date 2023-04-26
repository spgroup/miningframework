package java.awt;

import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.im.InputContext;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.peer.ComponentPeer;
import java.awt.peer.WindowPeer;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.accessibility.*;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.CausedFocusEvent;
import sun.awt.SunToolkit;
import sun.awt.util.IdentityArrayList;
import sun.java2d.Disposer;
import sun.java2d.pipe.Region;
import sun.security.action.GetPropertyAction;
import sun.security.util.SecurityConstants;
import sun.util.logging.PlatformLogger;

public class Window extends Container implements Accessible {

    public static enum Type {

        NORMAL, UTILITY, POPUP
    }

    String warningString;

    transient java.util.List<Image> icons;

    private transient Component temporaryLostComponent;

    static boolean systemSyncLWRequests = false;

    boolean syncLWRequests = false;

    transient boolean beforeFirstShow = true;

    private transient boolean disposing = false;

    static final int OPENED = 0x01;

    int state;

    private boolean alwaysOnTop;

    private static final IdentityArrayList<Window> allWindows = new IdentityArrayList<Window>();

    transient Vector<WeakReference<Window>> ownedWindowList = new Vector<WeakReference<Window>>();

    private transient WeakReference<Window> weakThis;

    transient boolean showWithParent;

    transient Dialog modalBlocker;

    Dialog.ModalExclusionType modalExclusionType;

    transient WindowListener windowListener;

    transient WindowStateListener windowStateListener;

    transient WindowFocusListener windowFocusListener;

    transient InputContext inputContext;

    private transient Object inputContextLock = new Object();

    private FocusManager focusMgr;

    private boolean focusableWindowState = true;

    private volatile boolean autoRequestFocus = true;

    transient boolean isInShow = false;

    private float opacity = 1.0f;

    private Shape shape = null;

    private static final String base = "win";

    private static int nameCounter = 0;

    private static final long serialVersionUID = 4497834738069338734L;

    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.Window");

    private static final boolean locationByPlatformProp;

    transient boolean isTrayIconWindow = false;

    private transient volatile int securityWarningWidth = 0;

    private transient volatile int securityWarningHeight = 0;

    private transient double securityWarningPointX = 2.0;

    private transient double securityWarningPointY = 0.0;

    private transient float securityWarningAlignmentX = RIGHT_ALIGNMENT;

    private transient float securityWarningAlignmentY = TOP_ALIGNMENT;

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
        String s = java.security.AccessController.doPrivileged(new GetPropertyAction("java.awt.syncLWRequests"));
        systemSyncLWRequests = (s != null && s.equals("true"));
        s = java.security.AccessController.doPrivileged(new GetPropertyAction("java.awt.Window.locationByPlatform"));
        locationByPlatformProp = (s != null && s.equals("true"));
    }

    private static native void initIDs();

    Window(GraphicsConfiguration gc) {
        init(gc);
    }

    transient Object anchor = new Object();

    static class WindowDisposerRecord implements sun.java2d.DisposerRecord {

        final WeakReference<Window> owner;

        final WeakReference<Window> weakThis;

        final WeakReference<AppContext> context;

        WindowDisposerRecord(AppContext context, Window victim) {
            owner = new WeakReference<Window>(victim.getOwner());
            weakThis = victim.weakThis;
            this.context = new WeakReference<AppContext>(context);
        }

        public void dispose() {
            Window parent = owner.get();
            if (parent != null) {
                parent.removeOwnedWindow(weakThis);
            }
            AppContext ac = context.get();
            if (null != ac) {
                Window.removeFromWindowList(ac, weakThis);
            }
        }
    }

    private GraphicsConfiguration initGC(GraphicsConfiguration gc) {
        GraphicsEnvironment.checkHeadless();
        if (gc == null) {
            gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        }
        setGraphicsConfiguration(gc);
        return gc;
    }

    private void init(GraphicsConfiguration gc) {
        GraphicsEnvironment.checkHeadless();
        syncLWRequests = systemSyncLWRequests;
        weakThis = new WeakReference<Window>(this);
        addToWindowList();
        setWarningString();
        this.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        this.visible = false;
        gc = initGC(gc);
        if (gc.getDevice().getType() != GraphicsDevice.TYPE_RASTER_SCREEN) {
            throw new IllegalArgumentException("not a screen device");
        }
        setLayout(new BorderLayout());
        Rectangle screenBounds = gc.getBounds();
        Insets screenInsets = getToolkit().getScreenInsets(gc);
        int x = getX() + screenBounds.x + screenInsets.left;
        int y = getY() + screenBounds.y + screenInsets.top;
        if (x != this.x || y != this.y) {
            setLocation(x, y);
            setLocationByPlatform(locationByPlatformProp);
        }
        modalExclusionType = Dialog.ModalExclusionType.NO_EXCLUDE;
        SunToolkit.checkAndSetPolicy(this);
    }

    Window() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        init((GraphicsConfiguration) null);
    }

    public Window(Frame owner) {
        this(owner == null ? (GraphicsConfiguration) null : owner.getGraphicsConfiguration());
        ownedInit(owner);
    }

    public Window(Window owner) {
        this(owner == null ? (GraphicsConfiguration) null : owner.getGraphicsConfiguration());
        ownedInit(owner);
    }

    public Window(Window owner, GraphicsConfiguration gc) {
        this(gc);
        ownedInit(owner);
    }

    private void ownedInit(Window owner) {
        this.parent = owner;
        if (owner != null) {
            owner.addOwnedWindow(weakThis);
        }
        Disposer.addRecord(anchor, new WindowDisposerRecord(appContext, this));
    }

    String constructComponentName() {
        synchronized (Window.class) {
            return base + nameCounter++;
        }
    }

    public java.util.List<Image> getIconImages() {
        java.util.List<Image> icons = this.icons;
        if (icons == null || icons.size() == 0) {
            return new ArrayList<Image>();
        }
        return new ArrayList<Image>(icons);
    }

    public synchronized void setIconImages(java.util.List<? extends Image> icons) {
        this.icons = (icons == null) ? new ArrayList<Image>() : new ArrayList<Image>(icons);
        WindowPeer peer = (WindowPeer) this.peer;
        if (peer != null) {
            peer.updateIconImages();
        }
        firePropertyChange("iconImage", null, null);
    }

    public void setIconImage(Image image) {
        ArrayList<Image> imageList = new ArrayList<Image>();
        if (image != null) {
            imageList.add(image);
        }
        setIconImages(imageList);
    }

    public void addNotify() {
        synchronized (getTreeLock()) {
            Container parent = this.parent;
            if (parent != null && parent.getPeer() == null) {
                parent.addNotify();
            }
            if (peer == null) {
                peer = getToolkit().createWindow(this);
            }
            synchronized (allWindows) {
                allWindows.add(this);
            }
            super.addNotify();
        }
    }

    public void removeNotify() {
        synchronized (getTreeLock()) {
            synchronized (allWindows) {
                allWindows.remove(this);
            }
            super.removeNotify();
        }
    }

    public void pack() {
        Container parent = this.parent;
        if (parent != null && parent.getPeer() == null) {
            parent.addNotify();
        }
        if (peer == null) {
            addNotify();
        }
        Dimension newSize = getPreferredSize();
        if (peer != null) {
            setClientSize(newSize.width, newSize.height);
        }
        if (beforeFirstShow) {
            isPacked = true;
        }
        validateUnconditionally();
    }

    public void setMinimumSize(Dimension minimumSize) {
        synchronized (getTreeLock()) {
            super.setMinimumSize(minimumSize);
            Dimension size = getSize();
            if (isMinimumSizeSet()) {
                if (size.width < minimumSize.width || size.height < minimumSize.height) {
                    int nw = Math.max(width, minimumSize.width);
                    int nh = Math.max(height, minimumSize.height);
                    setSize(nw, nh);
                }
            }
            if (peer != null) {
                ((WindowPeer) peer).updateMinimumSize();
            }
        }
    }

    public void setSize(Dimension d) {
        super.setSize(d);
    }

    public void setSize(int width, int height) {
        super.setSize(width, height);
    }

    @Override
    public void setLocation(int x, int y) {
        super.setLocation(x, y);
    }

    @Override
    public void setLocation(Point p) {
        super.setLocation(p);
    }

    @Deprecated
    public void reshape(int x, int y, int width, int height) {
        if (isMinimumSizeSet()) {
            Dimension minSize = getMinimumSize();
            if (width < minSize.width) {
                width = minSize.width;
            }
            if (height < minSize.height) {
                height = minSize.height;
            }
        }
        super.reshape(x, y, width, height);
    }

    void setClientSize(int w, int h) {
        synchronized (getTreeLock()) {
            setBoundsOp(ComponentPeer.SET_CLIENT_SIZE);
            setBounds(x, y, w, h);
        }
    }

    static private final AtomicBoolean beforeFirstWindowShown = new AtomicBoolean(true);

    final void closeSplashScreen() {
        if (isTrayIconWindow) {
            return;
        }
        if (beforeFirstWindowShown.getAndSet(false)) {
            SunToolkit.closeSplashScreen();
            SplashScreen.markClosed();
        }
    }

    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    @Deprecated
    public void show() {
        if (peer == null) {
            addNotify();
        }
        validateUnconditionally();
        isInShow = true;
        if (visible) {
            toFront();
        } else {
            beforeFirstShow = false;
            closeSplashScreen();
            Dialog.checkShouldBeBlocked(this);
            super.show();
            locationByPlatform = false;
            for (int i = 0; i < ownedWindowList.size(); i++) {
                Window child = ownedWindowList.elementAt(i).get();
                if ((child != null) && child.showWithParent) {
                    child.show();
                    child.showWithParent = false;
                }
            }
            if (!isModalBlocked()) {
                updateChildrenBlocking();
            } else {
                modalBlocker.toFront_NoClientCode();
            }
            if (this instanceof Frame || this instanceof Dialog) {
                updateChildFocusableWindowState(this);
            }
        }
        isInShow = false;
        if ((state & OPENED) == 0) {
            postWindowEvent(WindowEvent.WINDOW_OPENED);
            state |= OPENED;
        }
    }

    static void updateChildFocusableWindowState(Window w) {
        if (w.getPeer() != null && w.isShowing()) {
            ((WindowPeer) w.getPeer()).updateFocusableWindowState();
        }
        for (int i = 0; i < w.ownedWindowList.size(); i++) {
            Window child = w.ownedWindowList.elementAt(i).get();
            if (child != null) {
                updateChildFocusableWindowState(child);
            }
        }
    }

    synchronized void postWindowEvent(int id) {
        if (windowListener != null || (eventMask & AWTEvent.WINDOW_EVENT_MASK) != 0 || Toolkit.enabledOnToolkit(AWTEvent.WINDOW_EVENT_MASK)) {
            WindowEvent e = new WindowEvent(this, id);
            Toolkit.getEventQueue().postEvent(e);
        }
    }

    @Deprecated
    public void hide() {
        synchronized (ownedWindowList) {
            for (int i = 0; i < ownedWindowList.size(); i++) {
                Window child = ownedWindowList.elementAt(i).get();
                if ((child != null) && child.visible) {
                    child.hide();
                    child.showWithParent = true;
                }
            }
        }
        if (isModalBlocked()) {
            modalBlocker.unblockWindow(this);
        }
        super.hide();
    }

    final void clearMostRecentFocusOwnerOnHide() {
    }

    public void dispose() {
        doDispose();
    }

    void disposeImpl() {
        dispose();
        if (getPeer() != null) {
            doDispose();
        }
    }

    void doDispose() {
        class DisposeAction implements Runnable {

            public void run() {
                disposing = true;
                try {
                    GraphicsDevice gd = getGraphicsConfiguration().getDevice();
                    if (gd.getFullScreenWindow() == Window.this) {
                        gd.setFullScreenWindow(null);
                    }
                    Object[] ownedWindowArray;
                    synchronized (ownedWindowList) {
                        ownedWindowArray = new Object[ownedWindowList.size()];
                        ownedWindowList.copyInto(ownedWindowArray);
                    }
                    for (int i = 0; i < ownedWindowArray.length; i++) {
                        Window child = (Window) (((WeakReference) (ownedWindowArray[i])).get());
                        if (child != null) {
                            child.disposeImpl();
                        }
                    }
                    hide();
                    beforeFirstShow = true;
                    removeNotify();
                    synchronized (inputContextLock) {
                        if (inputContext != null) {
                            inputContext.dispose();
                            inputContext = null;
                        }
                    }
                    clearCurrentFocusCycleRootOnHide();
                } finally {
                    disposing = false;
                }
            }
        }
        DisposeAction action = new DisposeAction();
        if (EventQueue.isDispatchThread()) {
            action.run();
        } else {
            try {
                EventQueue.invokeAndWait(this, action);
            } catch (InterruptedException e) {
                System.err.println("Disposal was interrupted:");
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                System.err.println("Exception during disposal:");
                e.printStackTrace();
            }
        }
        postWindowEvent(WindowEvent.WINDOW_CLOSED);
    }

    void adjustListeningChildrenOnParent(long mask, int num) {
    }

    void adjustDecendantsOnParent(int num) {
    }

    public void toFront() {
        toFront_NoClientCode();
    }

    final void toFront_NoClientCode() {
        if (visible) {
            WindowPeer peer = (WindowPeer) this.peer;
            if (peer != null) {
                peer.toFront();
            }
            if (isModalBlocked()) {
                modalBlocker.toFront_NoClientCode();
            }
        }
    }

    public void toBack() {
        toBack_NoClientCode();
    }

    final void toBack_NoClientCode() {
        if (isAlwaysOnTop()) {
            try {
                setAlwaysOnTop(false);
            } catch (SecurityException e) {
            }
        }
        if (visible) {
            WindowPeer peer = (WindowPeer) this.peer;
            if (peer != null) {
                peer.toBack();
            }
        }
    }

    public Toolkit getToolkit() {
        return Toolkit.getDefaultToolkit();
    }

    public final String getWarningString() {
        return warningString;
    }

    private void setWarningString() {
        warningString = null;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (!sm.checkTopLevelWindow(this)) {
                warningString = AccessController.doPrivileged(new GetPropertyAction("awt.appletWarning", "Java Applet Window"));
            }
        }
    }

    public Locale getLocale() {
        if (this.locale == null) {
            return Locale.getDefault();
        }
        return this.locale;
    }

    public InputContext getInputContext() {
        synchronized (inputContextLock) {
            if (inputContext == null) {
                inputContext = InputContext.getInstance();
            }
        }
        return inputContext;
    }

    public void setCursor(Cursor cursor) {
        if (cursor == null) {
            cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        }
        super.setCursor(cursor);
    }

    public Window getOwner() {
        return getOwner_NoClientCode();
    }

    final Window getOwner_NoClientCode() {
        return (Window) parent;
    }

    public Window[] getOwnedWindows() {
        return getOwnedWindows_NoClientCode();
    }

    final Window[] getOwnedWindows_NoClientCode() {
        Window[] realCopy;
        synchronized (ownedWindowList) {
            int fullSize = ownedWindowList.size();
            int realSize = 0;
            Window[] fullCopy = new Window[fullSize];
            for (int i = 0; i < fullSize; i++) {
                fullCopy[realSize] = ownedWindowList.elementAt(i).get();
                if (fullCopy[realSize] != null) {
                    realSize++;
                }
            }
            if (fullSize != realSize) {
                realCopy = Arrays.copyOf(fullCopy, realSize);
            } else {
                realCopy = fullCopy;
            }
        }
        return realCopy;
    }

    boolean isModalBlocked() {
        return modalBlocker != null;
    }

    void setModalBlocked(Dialog blocker, boolean blocked, boolean peerCall) {
        this.modalBlocker = blocked ? blocker : null;
        if (peerCall) {
            WindowPeer peer = (WindowPeer) this.peer;
            if (peer != null) {
                peer.setModalBlocked(blocker, blocked);
            }
        }
    }

    Dialog getModalBlocker() {
        return modalBlocker;
    }

    static IdentityArrayList<Window> getAllWindows() {
        synchronized (allWindows) {
            IdentityArrayList<Window> v = new IdentityArrayList<Window>();
            v.addAll(allWindows);
            return v;
        }
    }

    static IdentityArrayList<Window> getAllUnblockedWindows() {
        synchronized (allWindows) {
            IdentityArrayList<Window> unblocked = new IdentityArrayList<Window>();
            for (int i = 0; i < allWindows.size(); i++) {
                Window w = allWindows.get(i);
                if (!w.isModalBlocked()) {
                    unblocked.add(w);
                }
            }
            return unblocked;
        }
    }

    private static Window[] getWindows(AppContext appContext) {
        synchronized (Window.class) {
            Window[] realCopy;
            @SuppressWarnings("unchecked")
            Vector<WeakReference<Window>> windowList = (Vector<WeakReference<Window>>) appContext.get(Window.class);
            if (windowList != null) {
                int fullSize = windowList.size();
                int realSize = 0;
                Window[] fullCopy = new Window[fullSize];
                for (int i = 0; i < fullSize; i++) {
                    Window w = windowList.get(i).get();
                    if (w != null) {
                        fullCopy[realSize++] = w;
                    }
                }
                if (fullSize != realSize) {
                    realCopy = Arrays.copyOf(fullCopy, realSize);
                } else {
                    realCopy = fullCopy;
                }
            } else {
                realCopy = new Window[0];
            }
            return realCopy;
        }
    }

    public static Window[] getWindows() {
        return getWindows(AppContext.getAppContext());
    }

    public static Window[] getOwnerlessWindows() {
        Window[] allWindows = Window.getWindows();
        int ownerlessCount = 0;
        for (Window w : allWindows) {
            if (w.getOwner() == null) {
                ownerlessCount++;
            }
        }
        Window[] ownerless = new Window[ownerlessCount];
        int c = 0;
        for (Window w : allWindows) {
            if (w.getOwner() == null) {
                ownerless[c++] = w;
            }
        }
        return ownerless;
    }

    Window getDocumentRoot() {
        synchronized (getTreeLock()) {
            Window w = this;
            while (w.getOwner() != null) {
                w = w.getOwner();
            }
            return w;
        }
    }

    public void setModalExclusionType(Dialog.ModalExclusionType exclusionType) {
        if (exclusionType == null) {
            exclusionType = Dialog.ModalExclusionType.NO_EXCLUDE;
        }
        if (!Toolkit.getDefaultToolkit().isModalExclusionTypeSupported(exclusionType)) {
            exclusionType = Dialog.ModalExclusionType.NO_EXCLUDE;
        }
        if (modalExclusionType == exclusionType) {
            return;
        }
        if (exclusionType == Dialog.ModalExclusionType.TOOLKIT_EXCLUDE) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(SecurityConstants.AWT.TOOLKIT_MODALITY_PERMISSION);
            }
        }
        modalExclusionType = exclusionType;
    }

    public Dialog.ModalExclusionType getModalExclusionType() {
        return modalExclusionType;
    }

    boolean isModalExcluded(Dialog.ModalExclusionType exclusionType) {
        if ((modalExclusionType != null) && modalExclusionType.compareTo(exclusionType) >= 0) {
            return true;
        }
        Window owner = getOwner_NoClientCode();
        return (owner != null) && owner.isModalExcluded(exclusionType);
    }

    void updateChildrenBlocking() {
        Vector<Window> childHierarchy = new Vector<Window>();
        Window[] ownedWindows = getOwnedWindows();
        for (int i = 0; i < ownedWindows.length; i++) {
            childHierarchy.add(ownedWindows[i]);
        }
        int k = 0;
        while (k < childHierarchy.size()) {
            Window w = childHierarchy.get(k);
            if (w.isVisible()) {
                if (w.isModalBlocked()) {
                    Dialog blocker = w.getModalBlocker();
                    blocker.unblockWindow(w);
                }
                Dialog.checkShouldBeBlocked(w);
                Window[] wOwned = w.getOwnedWindows();
                for (int j = 0; j < wOwned.length; j++) {
                    childHierarchy.add(wOwned[j]);
                }
            }
            k++;
        }
    }

    public synchronized void addWindowListener(WindowListener l) {
        if (l == null) {
            return;
        }
        newEventsOnly = true;
        windowListener = AWTEventMulticaster.add(windowListener, l);
    }

    public synchronized void addWindowStateListener(WindowStateListener l) {
        if (l == null) {
            return;
        }
        windowStateListener = AWTEventMulticaster.add(windowStateListener, l);
        newEventsOnly = true;
    }

    public synchronized void addWindowFocusListener(WindowFocusListener l) {
        if (l == null) {
            return;
        }
        windowFocusListener = AWTEventMulticaster.add(windowFocusListener, l);
        newEventsOnly = true;
    }

    public synchronized void removeWindowListener(WindowListener l) {
        if (l == null) {
            return;
        }
        windowListener = AWTEventMulticaster.remove(windowListener, l);
    }

    public synchronized void removeWindowStateListener(WindowStateListener l) {
        if (l == null) {
            return;
        }
        windowStateListener = AWTEventMulticaster.remove(windowStateListener, l);
    }

    public synchronized void removeWindowFocusListener(WindowFocusListener l) {
        if (l == null) {
            return;
        }
        windowFocusListener = AWTEventMulticaster.remove(windowFocusListener, l);
    }

    public synchronized WindowListener[] getWindowListeners() {
        return getListeners(WindowListener.class);
    }

    public synchronized WindowFocusListener[] getWindowFocusListeners() {
        return getListeners(WindowFocusListener.class);
    }

    public synchronized WindowStateListener[] getWindowStateListeners() {
        return getListeners(WindowStateListener.class);
    }

    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if (listenerType == WindowFocusListener.class) {
            l = windowFocusListener;
        } else if (listenerType == WindowStateListener.class) {
            l = windowStateListener;
        } else if (listenerType == WindowListener.class) {
            l = windowListener;
        } else {
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners(l, listenerType);
    }

    boolean eventEnabled(AWTEvent e) {
        switch(e.id) {
            case WindowEvent.WINDOW_OPENED:
            case WindowEvent.WINDOW_CLOSING:
            case WindowEvent.WINDOW_CLOSED:
            case WindowEvent.WINDOW_ICONIFIED:
            case WindowEvent.WINDOW_DEICONIFIED:
            case WindowEvent.WINDOW_ACTIVATED:
            case WindowEvent.WINDOW_DEACTIVATED:
                if ((eventMask & AWTEvent.WINDOW_EVENT_MASK) != 0 || windowListener != null) {
                    return true;
                }
                return false;
            case WindowEvent.WINDOW_GAINED_FOCUS:
            case WindowEvent.WINDOW_LOST_FOCUS:
                if ((eventMask & AWTEvent.WINDOW_FOCUS_EVENT_MASK) != 0 || windowFocusListener != null) {
                    return true;
                }
                return false;
            case WindowEvent.WINDOW_STATE_CHANGED:
                if ((eventMask & AWTEvent.WINDOW_STATE_EVENT_MASK) != 0 || windowStateListener != null) {
                    return true;
                }
                return false;
            default:
                break;
        }
        return super.eventEnabled(e);
    }

    protected void processEvent(AWTEvent e) {
        if (e instanceof WindowEvent) {
            switch(e.getID()) {
                case WindowEvent.WINDOW_OPENED:
                case WindowEvent.WINDOW_CLOSING:
                case WindowEvent.WINDOW_CLOSED:
                case WindowEvent.WINDOW_ICONIFIED:
                case WindowEvent.WINDOW_DEICONIFIED:
                case WindowEvent.WINDOW_ACTIVATED:
                case WindowEvent.WINDOW_DEACTIVATED:
                    processWindowEvent((WindowEvent) e);
                    break;
                case WindowEvent.WINDOW_GAINED_FOCUS:
                case WindowEvent.WINDOW_LOST_FOCUS:
                    processWindowFocusEvent((WindowEvent) e);
                    break;
                case WindowEvent.WINDOW_STATE_CHANGED:
                    processWindowStateEvent((WindowEvent) e);
                    break;
            }
            return;
        }
        super.processEvent(e);
    }

    protected void processWindowEvent(WindowEvent e) {
        WindowListener listener = windowListener;
        if (listener != null) {
            switch(e.getID()) {
                case WindowEvent.WINDOW_OPENED:
                    listener.windowOpened(e);
                    break;
                case WindowEvent.WINDOW_CLOSING:
                    listener.windowClosing(e);
                    break;
                case WindowEvent.WINDOW_CLOSED:
                    listener.windowClosed(e);
                    break;
                case WindowEvent.WINDOW_ICONIFIED:
                    listener.windowIconified(e);
                    break;
                case WindowEvent.WINDOW_DEICONIFIED:
                    listener.windowDeiconified(e);
                    break;
                case WindowEvent.WINDOW_ACTIVATED:
                    listener.windowActivated(e);
                    break;
                case WindowEvent.WINDOW_DEACTIVATED:
                    listener.windowDeactivated(e);
                    break;
                default:
                    break;
            }
        }
    }

    protected void processWindowFocusEvent(WindowEvent e) {
        WindowFocusListener listener = windowFocusListener;
        if (listener != null) {
            switch(e.getID()) {
                case WindowEvent.WINDOW_GAINED_FOCUS:
                    listener.windowGainedFocus(e);
                    break;
                case WindowEvent.WINDOW_LOST_FOCUS:
                    listener.windowLostFocus(e);
                    break;
                default:
                    break;
            }
        }
    }

    protected void processWindowStateEvent(WindowEvent e) {
        WindowStateListener listener = windowStateListener;
        if (listener != null) {
            switch(e.getID()) {
                case WindowEvent.WINDOW_STATE_CHANGED:
                    listener.windowStateChanged(e);
                    break;
                default:
                    break;
            }
        }
    }

    void preProcessKeyEvent(KeyEvent e) {
        if (e.isActionKey() && e.getKeyCode() == KeyEvent.VK_F1 && e.isControlDown() && e.isShiftDown() && e.getID() == KeyEvent.KEY_PRESSED) {
            list(System.out, 0);
        }
    }

    void postProcessKeyEvent(KeyEvent e) {
    }

    public final void setAlwaysOnTop(boolean alwaysOnTop) throws SecurityException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(SecurityConstants.AWT.SET_WINDOW_ALWAYS_ON_TOP_PERMISSION);
        }
        boolean oldAlwaysOnTop;
        synchronized (this) {
            oldAlwaysOnTop = this.alwaysOnTop;
            this.alwaysOnTop = alwaysOnTop;
        }
        if (oldAlwaysOnTop != alwaysOnTop) {
            if (isAlwaysOnTopSupported()) {
                WindowPeer peer = (WindowPeer) this.peer;
                synchronized (getTreeLock()) {
                    if (peer != null) {
                        peer.setAlwaysOnTop(alwaysOnTop);
                    }
                }
            }
            firePropertyChange("alwaysOnTop", oldAlwaysOnTop, alwaysOnTop);
        }
    }

    public boolean isAlwaysOnTopSupported() {
        return Toolkit.getDefaultToolkit().isAlwaysOnTopSupported();
    }

    public final boolean isAlwaysOnTop() {
        return alwaysOnTop;
    }

    public Component getFocusOwner() {
        return (isFocused()) ? KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() : null;
    }

    public Component getMostRecentFocusOwner() {
        if (isFocused()) {
            return getFocusOwner();
        } else {
            Component mostRecent = KeyboardFocusManager.getMostRecentFocusOwner(this);
            if (mostRecent != null) {
                return mostRecent;
            } else {
                return (isFocusableWindow()) ? getFocusTraversalPolicy().getInitialComponent(this) : null;
            }
        }
    }

    public boolean isActive() {
        return (KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() == this);
    }

    public boolean isFocused() {
        return (KeyboardFocusManager.getCurrentKeyboardFocusManager().getGlobalFocusedWindow() == this);
    }

    @SuppressWarnings("unchecked")
public Set<AWTKeyStroke> getFocusTraversalKeys(int id) {
        if (id < 0 || id >= KeyboardFocusManager.TRAVERSAL_KEY_LENGTH) {
            throw new IllegalArgumentException("invalid focus traversal key identifier");
        }
        @SuppressWarnings("rawtypes")
        Set keystrokes = (focusTraversalKeys != null) ? focusTraversalKeys[id] : null;
        if (keystrokes != null) {
            return keystrokes;
        } else {
            return KeyboardFocusManager.getCurrentKeyboardFocusManager().getDefaultFocusTraversalKeys(id);
        }
    }

    public final void setFocusCycleRoot(boolean focusCycleRoot) {
    }

    public final boolean isFocusCycleRoot() {
        return true;
    }

    public final Container getFocusCycleRootAncestor() {
        return null;
    }

    public final boolean isFocusableWindow() {
        if (!getFocusableWindowState()) {
            return false;
        }
        if (this instanceof Frame || this instanceof Dialog) {
            return true;
        }
        if (getFocusTraversalPolicy().getDefaultComponent(this) == null) {
            return false;
        }
        for (Window owner = getOwner(); owner != null; owner = owner.getOwner()) {
            if (owner instanceof Frame || owner instanceof Dialog) {
                return owner.isShowing();
            }
        }
        return false;
    }

    public boolean getFocusableWindowState() {
        return focusableWindowState;
    }

    public void setFocusableWindowState(boolean focusableWindowState) {
        boolean oldFocusableWindowState;
        synchronized (this) {
            oldFocusableWindowState = this.focusableWindowState;
            this.focusableWindowState = focusableWindowState;
        }
        WindowPeer peer = (WindowPeer) this.peer;
        if (peer != null) {
            peer.updateFocusableWindowState();
        }
        firePropertyChange("focusableWindowState", oldFocusableWindowState, focusableWindowState);
        if (oldFocusableWindowState && !focusableWindowState && isFocused()) {
            for (Window owner = getOwner(); owner != null; owner = owner.getOwner()) {
                Component toFocus = KeyboardFocusManager.getMostRecentFocusOwner(owner);
                if (toFocus != null && toFocus.requestFocus(false, CausedFocusEvent.Cause.ACTIVATION)) {
                    return;
                }
            }
            KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwnerPriv();
        }
    }

    public void setAutoRequestFocus(boolean autoRequestFocus) {
        this.autoRequestFocus = autoRequestFocus;
    }

    public boolean isAutoRequestFocus() {
        return autoRequestFocus;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        super.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public boolean isValidateRoot() {
        return true;
    }

    void dispatchEventImpl(AWTEvent e) {
        if (e.getID() == ComponentEvent.COMPONENT_RESIZED) {
            invalidate();
            validate();
        }
        super.dispatchEventImpl(e);
    }

    @Deprecated
    public boolean postEvent(Event e) {
        if (handleEvent(e)) {
            e.consume();
            return true;
        }
        return false;
    }

    public boolean isShowing() {
        return visible;
    }

    boolean isDisposing() {
        return disposing;
    }

    @Deprecated
    public void applyResourceBundle(ResourceBundle rb) {
        applyComponentOrientation(ComponentOrientation.getOrientation(rb));
    }

    @Deprecated
    public void applyResourceBundle(String rbName) {
        applyResourceBundle(ResourceBundle.getBundle(rbName));
    }

    void addOwnedWindow(WeakReference<Window> weakWindow) {
        if (weakWindow != null) {
            synchronized (ownedWindowList) {
                if (!ownedWindowList.contains(weakWindow)) {
                    ownedWindowList.addElement(weakWindow);
                }
            }
        }
    }

    void removeOwnedWindow(WeakReference<Window> weakWindow) {
        if (weakWindow != null) {
            ownedWindowList.removeElement(weakWindow);
        }
    }

    void connectOwnedWindow(Window child) {
        child.parent = this;
        addOwnedWindow(child.weakThis);
    }

    private void addToWindowList() {
        synchronized (Window.class) {
            @SuppressWarnings("unchecked")
            Vector<WeakReference<Window>> windowList = (Vector<WeakReference<Window>>) appContext.get(Window.class);
            if (windowList == null) {
                windowList = new Vector<WeakReference<Window>>();
                appContext.put(Window.class, windowList);
            }
            windowList.add(weakThis);
        }
    }

    private static void removeFromWindowList(AppContext context, WeakReference<Window> weakThis) {
        synchronized (Window.class) {
            @SuppressWarnings("unchecked")
            Vector<WeakReference<Window>> windowList = (Vector<WeakReference<Window>>) context.get(Window.class);
            if (windowList != null) {
                windowList.remove(weakThis);
            }
        }
    }

    private void removeFromWindowList() {
        removeFromWindowList(appContext, weakThis);
    }

    private Type type = Type.NORMAL;

    public void setType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type should not be null.");
        }
        synchronized (getTreeLock()) {
            if (isDisplayable()) {
                throw new IllegalComponentStateException("The window is displayable.");
            }
            synchronized (getObjectLock()) {
                this.type = type;
            }
        }
    }

    public Type getType() {
        synchronized (getObjectLock()) {
            return type;
        }
    }

    private int windowSerializedDataVersion = 2;

    private void writeObject(ObjectOutputStream s) throws IOException {
        synchronized (this) {
            focusMgr = new FocusManager();
            focusMgr.focusRoot = this;
            focusMgr.focusOwner = getMostRecentFocusOwner();
            s.defaultWriteObject();
            focusMgr = null;
            AWTEventMulticaster.save(s, windowListenerK, windowListener);
            AWTEventMulticaster.save(s, windowFocusListenerK, windowFocusListener);
            AWTEventMulticaster.save(s, windowStateListenerK, windowStateListener);
        }
        s.writeObject(null);
        synchronized (ownedWindowList) {
            for (int i = 0; i < ownedWindowList.size(); i++) {
                Window child = ownedWindowList.elementAt(i).get();
                if (child != null) {
                    s.writeObject(ownedWindowK);
                    s.writeObject(child);
                }
            }
        }
        s.writeObject(null);
        if (icons != null) {
            for (Image i : icons) {
                if (i instanceof Serializable) {
                    s.writeObject(i);
                }
            }
        }
        s.writeObject(null);
    }

    private void initDeserializedWindow() {
        setWarningString();
        inputContextLock = new Object();
        visible = false;
        weakThis = new WeakReference<>(this);
        anchor = new Object();
        sun.java2d.Disposer.addRecord(anchor, new WindowDisposerRecord(appContext, this));
        addToWindowList();
        initGC(null);
    }

    private void deserializeResources(ObjectInputStream s) throws ClassNotFoundException, IOException, HeadlessException {
        ownedWindowList = new Vector<>();
        if (windowSerializedDataVersion < 2) {
            if (focusMgr != null) {
                if (focusMgr.focusOwner != null) {
                    KeyboardFocusManager.setMostRecentFocusOwner(this, focusMgr.focusOwner);
                }
            }
            focusableWindowState = true;
        }
        Object keyOrNull;
        while (null != (keyOrNull = s.readObject())) {
            String key = ((String) keyOrNull).intern();
            if (windowListenerK == key) {
                addWindowListener((WindowListener) (s.readObject()));
            } else if (windowFocusListenerK == key) {
                addWindowFocusListener((WindowFocusListener) (s.readObject()));
            } else if (windowStateListenerK == key) {
                addWindowStateListener((WindowStateListener) (s.readObject()));
            } else
                s.readObject();
        }
        try {
            while (null != (keyOrNull = s.readObject())) {
                String key = ((String) keyOrNull).intern();
                if (ownedWindowK == key)
                    connectOwnedWindow((Window) s.readObject());
                else
                    s.readObject();
            }
            Object obj = s.readObject();
            icons = new ArrayList<Image>();
            while (obj != null) {
                if (obj instanceof Image) {
                    icons.add((Image) obj);
                }
                obj = s.readObject();
            }
        } catch (OptionalDataException e) {
        }
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException, HeadlessException {
        GraphicsEnvironment.checkHeadless();
        initDeserializedWindow();
        ObjectInputStream.GetField f = s.readFields();
        syncLWRequests = f.get("syncLWRequests", systemSyncLWRequests);
        state = f.get("state", 0);
        focusableWindowState = f.get("focusableWindowState", true);
        windowSerializedDataVersion = f.get("windowSerializedDataVersion", 1);
        locationByPlatform = f.get("locationByPlatform", locationByPlatformProp);
        focusMgr = (FocusManager) f.get("focusMgr", null);
        Dialog.ModalExclusionType et = (Dialog.ModalExclusionType) f.get("modalExclusionType", Dialog.ModalExclusionType.NO_EXCLUDE);
        setModalExclusionType(et);
        boolean aot = f.get("alwaysOnTop", false);
        if (aot) {
            setAlwaysOnTop(aot);
        }
        shape = (Shape) f.get("shape", null);
        opacity = (Float) f.get("opacity", 1.0f);
        this.securityWarningWidth = 0;
        this.securityWarningHeight = 0;
        this.securityWarningPointX = 2.0;
        this.securityWarningPointY = 0.0;
        this.securityWarningAlignmentX = RIGHT_ALIGNMENT;
        this.securityWarningAlignmentY = TOP_ALIGNMENT;
        deserializeResources(s);
    }

    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTWindow();
        }
        return accessibleContext;
    }

    protected class AccessibleAWTWindow extends AccessibleAWTContainer {

        private static final long serialVersionUID = 4215068635060671780L;

        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.WINDOW;
        }

        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if (getFocusOwner() != null) {
                states.add(AccessibleState.ACTIVE);
            }
            return states;
        }
    }

    @Override
    void setGraphicsConfiguration(GraphicsConfiguration gc) {
        if (gc == null) {
            gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        }
        synchronized (getTreeLock()) {
            super.setGraphicsConfiguration(gc);
            if (log.isLoggable(PlatformLogger.FINER)) {
                log.finer("+ Window.setGraphicsConfiguration(): new GC is \n+ " + getGraphicsConfiguration_NoClientCode() + "\n+ this is " + this);
            }
        }
    }

    public void setLocationRelativeTo(Component c) {
        int dx = 0, dy = 0;
        GraphicsConfiguration gc = getGraphicsConfiguration_NoClientCode();
        Rectangle gcBounds = gc.getBounds();
        Dimension windowSize = getSize();
        Window componentWindow = SunToolkit.getContainingWindow(c);
        if ((c == null) || (componentWindow == null)) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
            gcBounds = gc.getBounds();
            Point centerPoint = ge.getCenterPoint();
            dx = centerPoint.x - windowSize.width / 2;
            dy = centerPoint.y - windowSize.height / 2;
        } else if (!c.isShowing()) {
            gc = componentWindow.getGraphicsConfiguration();
            gcBounds = gc.getBounds();
            dx = gcBounds.x + (gcBounds.width - windowSize.width) / 2;
            dy = gcBounds.y + (gcBounds.height - windowSize.height) / 2;
        } else {
            gc = componentWindow.getGraphicsConfiguration();
            gcBounds = gc.getBounds();
            Dimension compSize = c.getSize();
            Point compLocation = c.getLocationOnScreen();
            dx = compLocation.x + ((compSize.width - windowSize.width) / 2);
            dy = compLocation.y + ((compSize.height - windowSize.height) / 2);
            if (dy + windowSize.height > gcBounds.y + gcBounds.height) {
                dy = gcBounds.y + gcBounds.height - windowSize.height;
                if (compLocation.x - gcBounds.x + compSize.width / 2 < gcBounds.width / 2) {
                    dx = compLocation.x + compSize.width;
                } else {
                    dx = compLocation.x - windowSize.width;
                }
            }
        }
        if (dy + windowSize.height > gcBounds.y + gcBounds.height) {
            dy = gcBounds.y + gcBounds.height - windowSize.height;
        }
        if (dy < gcBounds.y) {
            dy = gcBounds.y;
        }
        if (dx + windowSize.width > gcBounds.x + gcBounds.width) {
            dx = gcBounds.x + gcBounds.width - windowSize.width;
        }
        if (dx < gcBounds.x) {
            dx = gcBounds.x;
        }
        setLocation(dx, dy);
    }

    void deliverMouseWheelToAncestor(MouseWheelEvent e) {
    }

    boolean dispatchMouseWheelToAncestor(MouseWheelEvent e) {
        return false;
    }

    public void createBufferStrategy(int numBuffers) {
        super.createBufferStrategy(numBuffers);
    }

    public void createBufferStrategy(int numBuffers, BufferCapabilities caps) throws AWTException {
        super.createBufferStrategy(numBuffers, caps);
    }

    public BufferStrategy getBufferStrategy() {
        return super.getBufferStrategy();
    }

    Component getTemporaryLostComponent() {
        return temporaryLostComponent;
    }

    Component setTemporaryLostComponent(Component component) {
        Component previousComp = temporaryLostComponent;
        if (component == null || component.canBeFocusOwner()) {
            temporaryLostComponent = component;
        } else {
            temporaryLostComponent = null;
        }
        return previousComp;
    }

    boolean canContainFocusOwner(Component focusOwnerCandidate) {
        return super.canContainFocusOwner(focusOwnerCandidate) && isFocusableWindow();
    }

    private boolean locationByPlatform = locationByPlatformProp;

    public void setLocationByPlatform(boolean locationByPlatform) {
        synchronized (getTreeLock()) {
            if (locationByPlatform && isShowing()) {
                throw new IllegalComponentStateException("The window is showing on screen.");
            }
            this.locationByPlatform = locationByPlatform;
        }
    }

    public boolean isLocationByPlatform() {
        synchronized (getTreeLock()) {
            return locationByPlatform;
        }
    }

    public void setBounds(int x, int y, int width, int height) {
        synchronized (getTreeLock()) {
            if (getBoundsOp() == ComponentPeer.SET_LOCATION || getBoundsOp() == ComponentPeer.SET_BOUNDS) {
                locationByPlatform = false;
            }
            super.setBounds(x, y, width, height);
        }
    }

    public void setBounds(Rectangle r) {
        setBounds(r.x, r.y, r.width, r.height);
    }

    boolean isRecursivelyVisible() {
        return visible;
    }

    public float getOpacity() {
        synchronized (getTreeLock()) {
            return opacity;
        }
    }

    public void setOpacity(float opacity) {
        synchronized (getTreeLock()) {
            if (opacity < 0.0f || opacity > 1.0f) {
                throw new IllegalArgumentException("The value of opacity should be in the range [0.0f .. 1.0f].");
            }
            if (opacity < 1.0f) {
                GraphicsConfiguration gc = getGraphicsConfiguration();
                GraphicsDevice gd = gc.getDevice();
                if (gc.getDevice().getFullScreenWindow() == this) {
                    throw new IllegalComponentStateException("Setting opacity for full-screen window is not supported.");
                }
                if (!gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT)) {
                    throw new UnsupportedOperationException("TRANSLUCENT translucency is not supported.");
                }
            }
            this.opacity = opacity;
            WindowPeer peer = (WindowPeer) getPeer();
            if (peer != null) {
                peer.setOpacity(opacity);
            }
        }
    }

    public Shape getShape() {
        synchronized (getTreeLock()) {
            return shape == null ? null : new Path2D.Float(shape);
        }
    }

    public void setShape(Shape shape) {
        synchronized (getTreeLock()) {
            if (shape != null) {
                GraphicsConfiguration gc = getGraphicsConfiguration();
                GraphicsDevice gd = gc.getDevice();
                if (gc.getDevice().getFullScreenWindow() == this) {
                    throw new IllegalComponentStateException("Setting shape for full-screen window is not supported.");
                }
                if (!gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT)) {
                    throw new UnsupportedOperationException("PERPIXEL_TRANSPARENT translucency is not supported.");
                }
            }
            this.shape = (shape == null) ? null : new Path2D.Float(shape);
            WindowPeer peer = (WindowPeer) getPeer();
            if (peer != null) {
                peer.applyShape(shape == null ? null : Region.getInstance(shape, null));
            }
        }
    }

    @Override
    public Color getBackground() {
        return super.getBackground();
    }

    @Override
    public void setBackground(Color bgColor) {
        Color oldBg = getBackground();
        super.setBackground(bgColor);
        if (oldBg != null && oldBg.equals(bgColor)) {
            return;
        }
        int oldAlpha = oldBg != null ? oldBg.getAlpha() : 255;
        int alpha = bgColor != null ? bgColor.getAlpha() : 255;
        if ((oldAlpha == 255) && (alpha < 255)) {
            GraphicsConfiguration gc = getGraphicsConfiguration();
            GraphicsDevice gd = gc.getDevice();
            if (gc.getDevice().getFullScreenWindow() == this) {
                throw new IllegalComponentStateException("Making full-screen window non opaque is not supported.");
            }
            if (!gc.isTranslucencyCapable()) {
                GraphicsConfiguration capableGC = gd.getTranslucencyCapableGC();
                if (capableGC == null) {
                    throw new UnsupportedOperationException("PERPIXEL_TRANSLUCENT translucency is not supported");
                }
                setGraphicsConfiguration(capableGC);
            }
            setLayersOpaque(this, false);
        } else if ((oldAlpha < 255) && (alpha == 255)) {
            setLayersOpaque(this, true);
        }
        WindowPeer peer = (WindowPeer) getPeer();
        if (peer != null) {
            peer.setOpaque(alpha == 255);
        }
    }

    @Override
    public boolean isOpaque() {
        Color bg = getBackground();
        return bg != null ? bg.getAlpha() == 255 : true;
    }

    private void updateWindow() {
        synchronized (getTreeLock()) {
            WindowPeer peer = (WindowPeer) getPeer();
            if (peer != null) {
                peer.updateWindow();
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        if (!isOpaque()) {
            Graphics gg = g.create();
            try {
                if (gg instanceof Graphics2D) {
                    gg.setColor(getBackground());
                    ((Graphics2D) gg).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
                    gg.fillRect(0, 0, getWidth(), getHeight());
                }
            } finally {
                gg.dispose();
            }
        }
        super.paint(g);
    }

    private static void setLayersOpaque(Component component, boolean isOpaque) {
        if (SunToolkit.isInstanceOf(component, "javax.swing.RootPaneContainer")) {
            javax.swing.RootPaneContainer rpc = (javax.swing.RootPaneContainer) component;
            javax.swing.JRootPane root = rpc.getRootPane();
            javax.swing.JLayeredPane lp = root.getLayeredPane();
            Container c = root.getContentPane();
            javax.swing.JComponent content = (c instanceof javax.swing.JComponent) ? (javax.swing.JComponent) c : null;
            lp.setOpaque(isOpaque);
            root.setOpaque(isOpaque);
            if (content != null) {
                content.setOpaque(isOpaque);
                int numChildren = content.getComponentCount();
                if (numChildren > 0) {
                    Component child = content.getComponent(0);
                    if (child instanceof javax.swing.RootPaneContainer) {
                        setLayersOpaque(child, isOpaque);
                    }
                }
            }
        }
    }

    @Override
    final Container getContainer() {
        return null;
    }

    @Override
    final void applyCompoundShape(Region shape) {
    }

    @Override
    final void applyCurrentShape() {
    }

    @Override
    final void mixOnReshaping() {
    }

    @Override
    final Point getLocationOnWindow() {
        return new Point(0, 0);
    }

    private static double limit(double value, double min, double max) {
        value = Math.max(value, min);
        value = Math.min(value, max);
        return value;
    }

    private Point2D calculateSecurityWarningPosition(double x, double y, double w, double h) {
        double wx = x + w * securityWarningAlignmentX + securityWarningPointX;
        double wy = y + h * securityWarningAlignmentY + securityWarningPointY;
        wx = Window.limit(wx, x - securityWarningWidth - 2, x + w + 2);
        wy = Window.limit(wy, y - securityWarningHeight - 2, y + h + 2);
        GraphicsConfiguration graphicsConfig = getGraphicsConfiguration_NoClientCode();
        Rectangle screenBounds = graphicsConfig.getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfig);
        wx = Window.limit(wx, screenBounds.x + screenInsets.left, screenBounds.x + screenBounds.width - screenInsets.right - securityWarningWidth);
        wy = Window.limit(wy, screenBounds.y + screenInsets.top, screenBounds.y + screenBounds.height - screenInsets.bottom - securityWarningHeight);
        return new Point2D.Double(wx, wy);
    }

    static {
        AWTAccessor.setWindowAccessor(new AWTAccessor.WindowAccessor() {

            public float getOpacity(Window window) {
                return window.opacity;
            }

            public void setOpacity(Window window, float opacity) {
                window.setOpacity(opacity);
            }

            public Shape getShape(Window window) {
                return window.getShape();
            }

            public void setShape(Window window, Shape shape) {
                window.setShape(shape);
            }

            public void setOpaque(Window window, boolean opaque) {
                Color bg = window.getBackground();
                if (bg == null) {
                    bg = new Color(0, 0, 0, 0);
                }
                window.setBackground(new Color(bg.getRed(), bg.getGreen(), bg.getBlue(), opaque ? 255 : 0));
            }

            public void updateWindow(Window window) {
                window.updateWindow();
            }

            public Dimension getSecurityWarningSize(Window window) {
                return new Dimension(window.securityWarningWidth, window.securityWarningHeight);
            }

            public void setSecurityWarningSize(Window window, int width, int height) {
                window.securityWarningWidth = width;
                window.securityWarningHeight = height;
            }

            public void setSecurityWarningPosition(Window window, Point2D point, float alignmentX, float alignmentY) {
                window.securityWarningPointX = point.getX();
                window.securityWarningPointY = point.getY();
                window.securityWarningAlignmentX = alignmentX;
                window.securityWarningAlignmentY = alignmentY;
                synchronized (window.getTreeLock()) {
                    WindowPeer peer = (WindowPeer) window.getPeer();
                    if (peer != null) {
                        peer.repositionSecurityWarning();
                    }
                }
            }

            public Point2D calculateSecurityWarningPosition(Window window, double x, double y, double w, double h) {
                return window.calculateSecurityWarningPosition(x, y, w, h);
            }

            public void setLWRequestStatus(Window changed, boolean status) {
                changed.syncLWRequests = status;
            }

            public boolean isAutoRequestFocus(Window w) {
                return w.autoRequestFocus;
            }

            public boolean isTrayIconWindow(Window w) {
                return w.isTrayIconWindow;
            }

            public void setTrayIconWindow(Window w, boolean isTrayIconWindow) {
                w.isTrayIconWindow = isTrayIconWindow;
            }
        });
    }

    @Override
    void updateZOrder() {
    }
}

class FocusManager implements java.io.Serializable {

    Container focusRoot;

    Component focusOwner;

    static final long serialVersionUID = 2491878825643557906L;
}