package javax.swing;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.lang.reflect.*;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.util.*;
import java.util.logging.*;
import sun.awt.SubRegionShowable;
import sun.java2d.SunGraphics2D;
import sun.security.action.GetPropertyAction;

class BufferStrategyPaintManager extends RepaintManager.PaintManager {

    private static Method COMPONENT_CREATE_BUFFER_STRATEGY_METHOD;

    private static Method COMPONENT_GET_BUFFER_STRATEGY_METHOD;

    private static boolean TRY_FLIP;

    private static final Logger LOGGER = Logger.getLogger("javax.swing.BufferStrategyPaintManager");

    private ArrayList<BufferInfo> bufferInfos;

    private boolean painting;

    private boolean showing;

    private int accumulatedX;

    private int accumulatedY;

    private int accumulatedMaxX;

    private int accumulatedMaxY;

    private JComponent rootJ;

    private Container root;

    private int xOffset;

    private int yOffset;

    private Graphics bsg;

    private BufferStrategy bufferStrategy;

    private BufferInfo bufferInfo;

    private boolean disposeBufferOnEnd;

    static {
        TRY_FLIP = "true".equals(AccessController.doPrivileged(new GetPropertyAction("swing.useFlipBufferStrategy", "false")));
    }

    private static Method getGetBufferStrategyMethod() {
        if (COMPONENT_GET_BUFFER_STRATEGY_METHOD == null) {
            getMethods();
        }
        return COMPONENT_GET_BUFFER_STRATEGY_METHOD;
    }

    private static Method getCreateBufferStrategyMethod() {
        if (COMPONENT_CREATE_BUFFER_STRATEGY_METHOD == null) {
            getMethods();
        }
        return COMPONENT_CREATE_BUFFER_STRATEGY_METHOD;
    }

    private static void getMethods() {
        java.security.AccessController.doPrivileged(new java.security.PrivilegedAction<Object>() {

            public Object run() {
                try {
                    COMPONENT_CREATE_BUFFER_STRATEGY_METHOD = Component.class.getDeclaredMethod("createBufferStrategy", new Class[] { int.class, BufferCapabilities.class });
                    COMPONENT_CREATE_BUFFER_STRATEGY_METHOD.setAccessible(true);
                    COMPONENT_GET_BUFFER_STRATEGY_METHOD = Component.class.getDeclaredMethod("getBufferStrategy");
                    COMPONENT_GET_BUFFER_STRATEGY_METHOD.setAccessible(true);
                } catch (SecurityException e) {
                    assert false;
                } catch (NoSuchMethodException nsme) {
                    assert false;
                }
                return null;
            }
        });
    }

    BufferStrategyPaintManager() {
        bufferInfos = new ArrayList<BufferInfo>(1);
    }

    protected void dispose() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                java.util.List<BufferInfo> bufferInfos;
                synchronized (BufferStrategyPaintManager.this) {
                    while (showing) {
                        try {
                            wait();
                        } catch (InterruptedException ie) {
                        }
                    }
                    bufferInfos = BufferStrategyPaintManager.this.bufferInfos;
                    BufferStrategyPaintManager.this.bufferInfos = null;
                }
                dispose(bufferInfos);
            }
        });
    }

    private void dispose(java.util.List<BufferInfo> bufferInfos) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, "BufferStrategyPaintManager disposed", new RuntimeException());
        }
        if (bufferInfos != null) {
            for (BufferInfo bufferInfo : bufferInfos) {
                bufferInfo.dispose();
            }
        }
    }

    public boolean show(Container c, int x, int y, int w, int h) {
        synchronized (this) {
            if (painting) {
                return false;
            }
            showing = true;
        }
        try {
            BufferInfo info = getBufferInfo(c);
            BufferStrategy bufferStrategy;
            if (info != null && !info.usingFlip && info.isInSync() && (bufferStrategy = info.getBufferStrategy(false)) != null) {
                SubRegionShowable bsSubRegion = (SubRegionShowable) bufferStrategy;
                boolean paintAllOnExpose = info.getPaintAllOnExpose();
                info.setPaintAllOnExpose(false);
                if (bsSubRegion.showIfNotLost(x, y, (x + w), (y + h))) {
                    return !paintAllOnExpose;
                }
                bufferInfo.setContentsLostDuringExpose(true);
            }
        } finally {
            synchronized (this) {
                showing = false;
                notifyAll();
            }
        }
        return false;
    }

    public boolean paint(JComponent paintingComponent, JComponent bufferComponent, Graphics g, int x, int y, int w, int h) {
        if (prepare(paintingComponent, true, x, y, w, h)) {
            if ((g instanceof SunGraphics2D) && ((SunGraphics2D) g).getDestination() == root) {
                int cx = ((SunGraphics2D) bsg).constrainX;
                int cy = ((SunGraphics2D) bsg).constrainY;
                if (cx != 0 || cy != 0) {
                    bsg.translate(-cx, -cy);
                }
                ((SunGraphics2D) bsg).constrain(xOffset + cx, yOffset + cy, x + w, y + h);
                bsg.setClip(x, y, w, h);
                paintingComponent.paintToOffscreen(bsg, x, y, w, h, x + w, y + h);
                accumulate(xOffset + x, yOffset + y, w, h);
                return true;
            } else {
                bufferInfo.setInSync(false);
            }
        }
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("prepare failed");
        }
        return super.paint(paintingComponent, bufferComponent, g, x, y, w, h);
    }

    public void copyArea(JComponent c, Graphics g, int x, int y, int w, int h, int deltaX, int deltaY, boolean clip) {
        if (prepare(c, false, 0, 0, 0, 0) && bufferInfo.isInSync()) {
            if (clip) {
                Rectangle cBounds = c.getVisibleRect();
                int relX = xOffset + x;
                int relY = yOffset + y;
                bsg.clipRect(xOffset + cBounds.x, yOffset + cBounds.y, cBounds.width, cBounds.height);
                bsg.copyArea(relX, relY, w, h, deltaX, deltaY);
            } else {
                bsg.copyArea(xOffset + x, yOffset + y, w, h, deltaX, deltaY);
            }
            accumulate(x + xOffset + deltaX, y + yOffset + deltaY, w, h);
        } else {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("copyArea: prepare failed or not in sync");
            }
            if (!flushAccumulatedRegion()) {
                rootJ.repaint();
            } else {
                super.copyArea(c, g, x, y, w, h, deltaX, deltaY, clip);
            }
        }
    }

    public void beginPaint() {
        synchronized (this) {
            painting = true;
            while (showing) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                }
            }
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("beginPaint");
        }
        resetAccumulated();
    }

    public void endPaint() {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("endPaint: region " + accumulatedX + " " + accumulatedY + " " + accumulatedMaxX + " " + accumulatedMaxY);
        }
        if (painting) {
            if (!flushAccumulatedRegion()) {
                if (!isRepaintingRoot()) {
                    repaintRoot(rootJ);
                } else {
                    resetDoubleBufferPerWindow();
                    rootJ.repaint();
                }
            }
        }
        BufferInfo toDispose = null;
        synchronized (this) {
            painting = false;
            if (disposeBufferOnEnd) {
                disposeBufferOnEnd = false;
                toDispose = bufferInfo;
                bufferInfos.remove(toDispose);
            }
        }
        if (toDispose != null) {
            toDispose.dispose();
        }
    }

    private boolean flushAccumulatedRegion() {
        boolean success = true;
        if (accumulatedX != Integer.MAX_VALUE) {
            SubRegionShowable bsSubRegion = (SubRegionShowable) bufferStrategy;
            boolean contentsLost = bufferStrategy.contentsLost();
            if (!contentsLost) {
                bsSubRegion.show(accumulatedX, accumulatedY, accumulatedMaxX, accumulatedMaxY);
                contentsLost = bufferStrategy.contentsLost();
            }
            if (contentsLost) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("endPaint: contents lost");
                }
                bufferInfo.setInSync(false);
                success = false;
            }
        }
        resetAccumulated();
        return success;
    }

    private void resetAccumulated() {
        accumulatedX = Integer.MAX_VALUE;
        accumulatedY = Integer.MAX_VALUE;
        accumulatedMaxX = 0;
        accumulatedMaxY = 0;
    }

    public void doubleBufferingChanged(final JRootPane rootPane) {
        if ((!rootPane.isDoubleBuffered() || !rootPane.getUseTrueDoubleBuffering()) && rootPane.getParent() != null) {
            if (!SwingUtilities.isEventDispatchThread()) {
                Runnable updater = new Runnable() {

                    public void run() {
                        doubleBufferingChanged0(rootPane);
                    }
                };
                SwingUtilities.invokeLater(updater);
            } else {
                doubleBufferingChanged0(rootPane);
            }
        }
    }

    private void doubleBufferingChanged0(JRootPane rootPane) {
        BufferInfo info;
        synchronized (this) {
            while (showing) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                }
            }
            info = getBufferInfo(rootPane.getParent());
            if (painting && bufferInfo == info) {
                disposeBufferOnEnd = true;
                info = null;
            } else if (info != null) {
                bufferInfos.remove(info);
            }
        }
        if (info != null) {
            info.dispose();
        }
    }

    private boolean prepare(JComponent c, boolean isPaint, int x, int y, int w, int h) {
        if (bsg != null) {
            bsg.dispose();
            bsg = null;
        }
        bufferStrategy = null;
        if (fetchRoot(c)) {
            boolean contentsLost = false;
            BufferInfo bufferInfo = getBufferInfo(root);
            if (bufferInfo == null) {
                contentsLost = true;
                bufferInfo = new BufferInfo(root);
                bufferInfos.add(bufferInfo);
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("prepare: new BufferInfo: " + root);
                }
            }
            this.bufferInfo = bufferInfo;
            if (!bufferInfo.hasBufferStrategyChanged()) {
                bufferStrategy = bufferInfo.getBufferStrategy(true);
                if (bufferStrategy != null) {
                    bsg = bufferStrategy.getDrawGraphics();
                    if (bufferStrategy.contentsRestored()) {
                        contentsLost = true;
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.finer("prepare: contents restored in prepare");
                        }
                    }
                } else {
                    return false;
                }
                if (bufferInfo.getContentsLostDuringExpose()) {
                    contentsLost = true;
                    bufferInfo.setContentsLostDuringExpose(false);
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.finer("prepare: contents lost on expose");
                    }
                }
                if (isPaint && c == rootJ && x == 0 && y == 0 && c.getWidth() == w && c.getHeight() == h) {
                    bufferInfo.setInSync(true);
                } else if (contentsLost) {
                    bufferInfo.setInSync(false);
                    if (!isRepaintingRoot()) {
                        repaintRoot(rootJ);
                    } else {
                        resetDoubleBufferPerWindow();
                    }
                }
                return (bufferInfos != null);
            }
        }
        return false;
    }

    private boolean fetchRoot(JComponent c) {
        boolean encounteredHW = false;
        rootJ = c;
        root = c;
        xOffset = yOffset = 0;
        while (root != null && (!(root instanceof Window) && !(root instanceof Applet))) {
            xOffset += root.getX();
            yOffset += root.getY();
            root = root.getParent();
            if (root != null) {
                if (root instanceof JComponent) {
                    rootJ = (JComponent) root;
                } else if (!root.isLightweight()) {
                    if (!encounteredHW) {
                        encounteredHW = true;
                    } else {
                        return false;
                    }
                }
            }
        }
        if ((root instanceof RootPaneContainer) && (rootJ instanceof JRootPane)) {
            if (rootJ.isDoubleBuffered() && ((JRootPane) rootJ).getUseTrueDoubleBuffering()) {
                return true;
            }
        }
        return false;
    }

    private void resetDoubleBufferPerWindow() {
        if (bufferInfos != null) {
            dispose(bufferInfos);
            bufferInfos = null;
            repaintManager.setPaintManager(null);
        }
    }

    private BufferInfo getBufferInfo(Container root) {
        for (int counter = bufferInfos.size() - 1; counter >= 0; counter--) {
            BufferInfo bufferInfo = bufferInfos.get(counter);
            Container biRoot = bufferInfo.getRoot();
            if (biRoot == null) {
                bufferInfos.remove(counter);
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("BufferInfo pruned, root null");
                }
            } else if (biRoot == root) {
                return bufferInfo;
            }
        }
        return null;
    }

    private void accumulate(int x, int y, int w, int h) {
        accumulatedX = Math.min(x, accumulatedX);
        accumulatedY = Math.min(y, accumulatedY);
        accumulatedMaxX = Math.max(accumulatedMaxX, x + w);
        accumulatedMaxY = Math.max(accumulatedMaxY, y + h);
    }

    private class BufferInfo extends ComponentAdapter implements WindowListener {

        private WeakReference<BufferStrategy> weakBS;

        private WeakReference<Container> root;

        private boolean usingFlip;

        private boolean inSync;

        private boolean contentsLostDuringExpose;

        private boolean paintAllOnExpose;

        public BufferInfo(Container root) {
            this.root = new WeakReference<Container>(root);
            root.addComponentListener(this);
            if (root instanceof Window) {
                ((Window) root).addWindowListener(this);
            }
        }

        public void setPaintAllOnExpose(boolean paintAllOnExpose) {
            this.paintAllOnExpose = paintAllOnExpose;
        }

        public boolean getPaintAllOnExpose() {
            return paintAllOnExpose;
        }

        public void setContentsLostDuringExpose(boolean value) {
            contentsLostDuringExpose = value;
        }

        public boolean getContentsLostDuringExpose() {
            return contentsLostDuringExpose;
        }

        public void setInSync(boolean inSync) {
            this.inSync = inSync;
        }

        public boolean isInSync() {
            return inSync;
        }

        public Container getRoot() {
            return (root == null) ? null : root.get();
        }

        public BufferStrategy getBufferStrategy(boolean create) {
            BufferStrategy bs = (weakBS == null) ? null : weakBS.get();
            if (bs == null && create) {
                bs = createBufferStrategy();
                if (bs != null) {
                    weakBS = new WeakReference<BufferStrategy>(bs);
                }
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("getBufferStrategy: created bs: " + bs);
                }
            }
            return bs;
        }

        public boolean usingFlip() {
            return usingFlip;
        }

        public boolean hasBufferStrategyChanged() {
            Container root = getRoot();
            if (root != null) {
                BufferStrategy ourBS = null;
                BufferStrategy componentBS = null;
                ourBS = getBufferStrategy(false);
                if (root instanceof Window) {
                    componentBS = ((Window) root).getBufferStrategy();
                } else {
                    try {
                        componentBS = (BufferStrategy) getGetBufferStrategyMethod().invoke(root);
                    } catch (InvocationTargetException ite) {
                        assert false;
                    } catch (IllegalArgumentException iae) {
                        assert false;
                    } catch (IllegalAccessException iae2) {
                        assert false;
                    }
                }
                if (componentBS != ourBS) {
                    if (ourBS != null) {
                        ourBS.dispose();
                    }
                    weakBS = null;
                    return true;
                }
            }
            return false;
        }

        private BufferStrategy createBufferStrategy() {
            BufferCapabilities caps;
            Container root = getRoot();
            if (root == null) {
                return null;
            }
            BufferStrategy bs = null;
            if (TRY_FLIP) {
                bs = createBufferStrategy(root, BufferCapabilities.FlipContents.COPIED);
                usingFlip = true;
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("createBufferStrategy: using flip strategy");
                }
            }
            if (bs == null) {
                bs = createBufferStrategy(root, null);
                usingFlip = false;
            }
            if (!(bs instanceof SubRegionShowable)) {
                bs = null;
            }
            return bs;
        }

        private BufferStrategy createBufferStrategy(Container root, BufferCapabilities.FlipContents type) {
            BufferCapabilities caps = new BufferCapabilities(new ImageCapabilities(true), new ImageCapabilities(true), type);
            BufferStrategy bs = null;
            if (root instanceof Applet) {
                try {
                    getCreateBufferStrategyMethod().invoke(root, 2, caps);
                    bs = (BufferStrategy) getGetBufferStrategyMethod().invoke(root);
                } catch (InvocationTargetException ite) {
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.log(Level.FINER, "createBufferStratety failed", ite);
                    }
                } catch (IllegalArgumentException iae) {
                    assert false;
                } catch (IllegalAccessException iae2) {
                    assert false;
                }
            } else {
                try {
                    ((Window) root).createBufferStrategy(2, caps);
                    bs = ((Window) root).getBufferStrategy();
                } catch (AWTException e) {
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.log(Level.FINER, "createBufferStratety failed", e);
                    }
                }
            }
            return bs;
        }

        public void dispose() {
            Container root = getRoot();
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, "disposed BufferInfo for: " + root);
            }
            if (root != null) {
                root.removeComponentListener(this);
                if (root instanceof Window) {
                    ((Window) root).removeWindowListener(this);
                }
                BufferStrategy bs = getBufferStrategy(false);
                if (bs != null) {
                    bs.dispose();
                }
            }
            this.root = null;
            weakBS = null;
        }

        public void componentHidden(ComponentEvent e) {
            Container root = getRoot();
            if (root != null && root.isVisible()) {
                root.repaint();
            } else {
                setPaintAllOnExpose(true);
            }
        }

        public void windowIconified(WindowEvent e) {
            setPaintAllOnExpose(true);
        }

        public void windowClosed(WindowEvent e) {
            synchronized (BufferStrategyPaintManager.this) {
                while (showing) {
                    try {
                        BufferStrategyPaintManager.this.wait();
                    } catch (InterruptedException ie) {
                    }
                }
                bufferInfos.remove(this);
            }
            dispose();
        }

        public void windowOpened(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowActivated(WindowEvent e) {
        }

        public void windowDeactivated(WindowEvent e) {
        }
    }
}
