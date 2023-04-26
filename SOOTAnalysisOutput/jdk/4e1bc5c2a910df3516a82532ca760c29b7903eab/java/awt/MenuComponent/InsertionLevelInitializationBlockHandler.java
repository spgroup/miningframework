package java.awt;

import java.awt.event.ActionEvent;
import java.awt.peer.MenuComponentPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.AccessControlContext;
import java.security.AccessController;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleComponent;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleSelection;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import sun.awt.AWTAccessor;
import sun.awt.AppContext;
import sun.awt.ComponentFactory;

public abstract class MenuComponent implements java.io.Serializable {

    static {
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    transient volatile MenuComponentPeer peer;

    transient volatile MenuContainer parent;

    private transient volatile AppContext appContext;

    private volatile Font font;

    private volatile String name;

    private volatile boolean nameExplicitlySet;

    volatile boolean newEventsOnly;

    private transient volatile AccessControlContext acc = AccessController.getContext();

    final AccessControlContext getAccessControlContext() {
        if (acc == null) {
            throw new SecurityException("MenuComponent is missing AccessControlContext");
        }
        return acc;
    }

    static final String actionListenerK = Component.actionListenerK;

    static final String itemListenerK = Component.itemListenerK;

    private static final long serialVersionUID = -4536902356223894379L;

    static {
        AWTAccessor.setMenuComponentAccessor(new AWTAccessor.MenuComponentAccessor() {

            @Override
            public AppContext getAppContext(MenuComponent menuComp) {
                return menuComp.appContext;
            }

            @Override
            public void setAppContext(MenuComponent menuComp, AppContext appContext) {
                menuComp.appContext = appContext;
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T extends MenuComponentPeer> T getPeer(MenuComponent menuComp) {
                return (T) menuComp.peer;
            }

            @Override
            public MenuContainer getParent(MenuComponent menuComp) {
                return menuComp.parent;
            }

            @Override
            public void setParent(MenuComponent menuComp, MenuContainer menuContainer) {
                menuComp.parent = menuContainer;
            }

            @Override
            public Font getFont_NoClientCode(MenuComponent menuComp) {
                return menuComp.getFont_NoClientCode();
            }
        });
    }

    public MenuComponent() throws HeadlessException {
        GraphicsEnvironment.checkHeadless();
        appContext = AppContext.getAppContext();
    }

    String constructComponentName() {
        return null;
    }

    final ComponentFactory getComponentFactory() {
        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        if (toolkit instanceof ComponentFactory) {
            return (ComponentFactory) toolkit;
        }
        throw new AWTError("UI components are unsupported by: " + toolkit);
    }

    public String getName() {
        if (name == null && !nameExplicitlySet) {
            synchronized (this) {
                if (name == null && !nameExplicitlySet)
                    name = constructComponentName();
            }
        }
        return name;
    }

    public void setName(String name) {
        synchronized (this) {
            this.name = name;
            nameExplicitlySet = true;
        }
    }

    public MenuContainer getParent() {
        return getParent_NoClientCode();
    }

    final MenuContainer getParent_NoClientCode() {
        return parent;
    }

    public Font getFont() {
        Font font = this.font;
        if (font != null) {
            return font;
        }
        MenuContainer parent = this.parent;
        if (parent != null) {
            return parent.getFont();
        }
        return null;
    }

    final Font getFont_NoClientCode() {
        Font font = this.font;
        if (font != null) {
            return font;
        }
        Object parent = this.parent;
        if (parent != null) {
            if (parent instanceof Component) {
                font = ((Component) parent).getFont_NoClientCode();
            } else if (parent instanceof MenuComponent) {
                font = ((MenuComponent) parent).getFont_NoClientCode();
            }
        }
        return font;
    }

    public void setFont(Font f) {
        synchronized (getTreeLock()) {
            font = f;
            MenuComponentPeer peer = this.peer;
            if (peer != null) {
                peer.setFont(f);
            }
        }
    }

    public void removeNotify() {
        synchronized (getTreeLock()) {
            MenuComponentPeer p = this.peer;
            if (p != null) {
                Toolkit.getEventQueue().removeSourceEvents(this, true);
                this.peer = null;
                p.dispose();
            }
        }
    }

    @Deprecated
    public boolean postEvent(Event evt) {
        MenuContainer parent = this.parent;
        if (parent != null) {
            parent.postEvent(evt);
        }
        return false;
    }

    public final void dispatchEvent(AWTEvent e) {
        dispatchEventImpl(e);
    }

    void dispatchEventImpl(AWTEvent e) {
        EventQueue.setCurrentEventAndMostRecentTime(e);
        Toolkit.getDefaultToolkit().notifyAWTEventListeners(e);
        if (newEventsOnly || (parent != null && parent instanceof MenuComponent && ((MenuComponent) parent).newEventsOnly)) {
            if (eventEnabled(e)) {
                processEvent(e);
            } else if (e instanceof ActionEvent && parent != null) {
                e.setSource(parent);
                ((MenuComponent) parent).dispatchEvent(e);
            }
        } else {
            @SuppressWarnings("deprecation")
            Event olde = e.convertToOld();
            if (olde != null) {
                postEvent(olde);
            }
        }
    }

    boolean eventEnabled(AWTEvent e) {
        return false;
    }

    protected void processEvent(AWTEvent e) {
    }

    protected String paramString() {
        String thisName = getName();
        return (thisName != null ? thisName : "");
    }

    public String toString() {
        return getClass().getName() + "[" + paramString() + "]";
    }

    protected final Object getTreeLock() {
        return Component.LOCK;
    }

    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException, HeadlessException {
        GraphicsEnvironment.checkHeadless();
        acc = AccessController.getContext();
        s.defaultReadObject();
        appContext = AppContext.getAppContext();
    }

    private static native void initIDs();

    AccessibleContext accessibleContext = null;

    public AccessibleContext getAccessibleContext() {
        return accessibleContext;
    }

    protected abstract class AccessibleAWTMenuComponent extends AccessibleContext implements java.io.Serializable, AccessibleComponent, AccessibleSelection {

        private static final long serialVersionUID = -4269533416223798698L;

        protected AccessibleAWTMenuComponent() {
        }

        public AccessibleSelection getAccessibleSelection() {
            return this;
        }

        public String getAccessibleName() {
            return accessibleName;
        }

        public String getAccessibleDescription() {
            return accessibleDescription;
        }

        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.AWT_COMPONENT;
        }

        public AccessibleStateSet getAccessibleStateSet() {
            return MenuComponent.this.getAccessibleStateSet();
        }

        public Accessible getAccessibleParent() {
            if (accessibleParent != null) {
                return accessibleParent;
            } else {
                MenuContainer parent = MenuComponent.this.getParent();
                if (parent instanceof Accessible) {
                    return (Accessible) parent;
                }
            }
            return null;
        }

        public int getAccessibleIndexInParent() {
            return MenuComponent.this.getAccessibleIndexInParent();
        }

        public int getAccessibleChildrenCount() {
            return 0;
        }

        public Accessible getAccessibleChild(int i) {
            return null;
        }

        public java.util.Locale getLocale() {
            MenuContainer parent = MenuComponent.this.getParent();
            if (parent instanceof Component)
                return ((Component) parent).getLocale();
            else
                return java.util.Locale.getDefault();
        }

        public AccessibleComponent getAccessibleComponent() {
            return this;
        }

        public Color getBackground() {
            return null;
        }

        public void setBackground(Color c) {
        }

        public Color getForeground() {
            return null;
        }

        public void setForeground(Color c) {
        }

        public Cursor getCursor() {
            return null;
        }

        public void setCursor(Cursor cursor) {
        }

        public Font getFont() {
            return MenuComponent.this.getFont();
        }

        public void setFont(Font f) {
            MenuComponent.this.setFont(f);
        }

        public FontMetrics getFontMetrics(Font f) {
            return null;
        }

        public boolean isEnabled() {
            return true;
        }

        public void setEnabled(boolean b) {
        }

        public boolean isVisible() {
            return true;
        }

        public void setVisible(boolean b) {
        }

        public boolean isShowing() {
            return true;
        }

        public boolean contains(Point p) {
            return false;
        }

        public Point getLocationOnScreen() {
            return null;
        }

        public Point getLocation() {
            return null;
        }

        public void setLocation(Point p) {
        }

        public Rectangle getBounds() {
            return null;
        }

        public void setBounds(Rectangle r) {
        }

        public Dimension getSize() {
            return null;
        }

        public void setSize(Dimension d) {
        }

        public Accessible getAccessibleAt(Point p) {
            return null;
        }

        public boolean isFocusTraversable() {
            return true;
        }

        public void requestFocus() {
        }

        public void addFocusListener(java.awt.event.FocusListener l) {
        }

        public void removeFocusListener(java.awt.event.FocusListener l) {
        }

        public int getAccessibleSelectionCount() {
            return 0;
        }

        public Accessible getAccessibleSelection(int i) {
            return null;
        }

        public boolean isAccessibleChildSelected(int i) {
            return false;
        }

        public void addAccessibleSelection(int i) {
        }

        public void removeAccessibleSelection(int i) {
        }

        public void clearAccessibleSelection() {
        }

        public void selectAllAccessibleSelection() {
        }
    }

    int getAccessibleIndexInParent() {
        MenuContainer localParent = parent;
        if (!(localParent instanceof MenuComponent)) {
            return -1;
        }
        MenuComponent localParentMenu = (MenuComponent) localParent;
        return localParentMenu.getAccessibleChildIndex(this);
    }

    int getAccessibleChildIndex(MenuComponent child) {
        return -1;
    }

    AccessibleStateSet getAccessibleStateSet() {
        AccessibleStateSet states = new AccessibleStateSet();
        return states;
    }
}