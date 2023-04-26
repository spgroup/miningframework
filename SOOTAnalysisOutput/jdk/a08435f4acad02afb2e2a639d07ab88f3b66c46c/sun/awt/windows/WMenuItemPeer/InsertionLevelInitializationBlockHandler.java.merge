package sun.awt.windows;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.awt.*;
import java.awt.peer.*;
import java.awt.event.ActionEvent;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.util.logging.PlatformLogger;

class WMenuItemPeer extends WObjectPeer implements MenuItemPeer {

    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.WMenuItemPeer");

    static {
        initIDs();
    }

    String shortcutLabel;

    protected WMenuPeer parent;

    private synchronized native void _dispose();

    protected void disposeImpl() {
        WToolkit.targetDisposedPeer(target, this);
        _dispose();
    }

    public void setEnabled(boolean b) {
        enable(b);
    }

    public void enable() {
        enable(true);
    }

    public void disable() {
        enable(false);
    }

    public void readShortcutLabel() {
        WMenuPeer ancestor = parent;
        while (ancestor != null && !(ancestor instanceof WMenuBarPeer)) {
            ancestor = ancestor.parent;
        }
        if (ancestor instanceof WMenuBarPeer) {
            MenuShortcut sc = ((MenuItem) target).getShortcut();
            shortcutLabel = (sc != null) ? sc.toString() : null;
        } else {
            shortcutLabel = null;
        }
    }

    public void setLabel(String label) {
        readShortcutLabel();
        _setLabel(label);
    }

    public native void _setLabel(String label);

    private final boolean isCheckbox;

    protected WMenuItemPeer() {
        isCheckbox = false;
    }

    WMenuItemPeer(MenuItem target) {
        this(target, false);
    }

    WMenuItemPeer(MenuItem target, boolean isCheckbox) {
        this.target = target;
        this.parent = (WMenuPeer) WToolkit.targetToPeer(target.getParent());
        this.isCheckbox = isCheckbox;
        create(parent);
        checkMenuCreation();
        readShortcutLabel();
    }

    protected void checkMenuCreation() {
        if (pData == 0) {
            if (createError != null) {
                throw createError;
            } else {
                throw new InternalError("couldn't create menu peer");
            }
        }
    }

    void postEvent(AWTEvent event) {
        WToolkit.postEvent(WToolkit.targetToAppContext(target), event);
    }

    native void create(WMenuPeer parent);

    native void enable(boolean e);

    void handleAction(final long when, final int modifiers) {
        WToolkit.executeOnEventHandlerThread(target, new Runnable() {

            public void run() {
                postEvent(new ActionEvent(target, ActionEvent.ACTION_PERFORMED, ((MenuItem) target).getActionCommand(), when, modifiers));
            }
        });
    }

    private static Font defaultMenuFont;

    static {
        defaultMenuFont = AccessController.doPrivileged(new PrivilegedAction<Font>() {

            public Font run() {
                try {
                    ResourceBundle rb = ResourceBundle.getBundle("sun.awt.windows.awtLocalization");
                    return Font.decode(rb.getString("menuFont"));
                } catch (MissingResourceException e) {
                    if (log.isLoggable(PlatformLogger.Level.FINE)) {
                        log.fine("WMenuItemPeer: " + e.getMessage() + ". Using default MenuItem font.", e);
                    }
                    return new Font("SanSerif", Font.PLAIN, 11);
                }
            }
        });
    }

    static Font getDefaultFont() {
        return defaultMenuFont;
    }

    private static native void initIDs();

    private native void _setFont(Font f);

    public void setFont(final Font f) {
        _setFont(f);
    }
}