package sun.awt.windows;

import java.awt.*;
import java.awt.event.FocusEvent.Cause;
import java.awt.dnd.DropTarget;
import java.awt.peer.*;
import java.io.File;
import java.io.FilenameFilter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Vector;
import sun.awt.AWTAccessor;

final class WFileDialogPeer extends WWindowPeer implements FileDialogPeer {

    static {
        initIDs();
    }

    private WComponentPeer parent;

    private FilenameFilter fileFilter;

    private Vector<WWindowPeer> blockedWindows = new Vector<>();

    private static native void setFilterString(String allFilter);

    @Override
    public void setFilenameFilter(FilenameFilter filter) {
        this.fileFilter = filter;
    }

    boolean checkFilenameFilter(String filename) {
        FileDialog fileDialog = (FileDialog) target;
        if (fileFilter == null) {
            return true;
        }
        File file = new File(filename);
        return fileFilter.accept(new File(file.getParent()), file.getName());
    }

    WFileDialogPeer(FileDialog target) {
        super(target);
    }

    @Override
    void create(WComponentPeer parent) {
        this.parent = parent;
    }

    @Override
    protected void checkCreation() {
    }

    @Override
    void initialize() {
        setFilenameFilter(((FileDialog) target).getFilenameFilter());
    }

    private native void _dispose();

    @Override
    protected void disposeImpl() {
        WToolkit.targetDisposedPeer(target, this);
        _dispose();
    }

    private native void _show();

    private native void _hide();

    @Override
    public void show() {
        new Thread(null, this::_show, "FileDialog", 0, false).start();
    }

    @Override
    void hide() {
        _hide();
    }

    void setHWnd(long hwnd) {
        if (this.hwnd == hwnd) {
            return;
        }
        this.hwnd = hwnd;
        for (WWindowPeer window : blockedWindows) {
            if (hwnd != 0) {
                window.modalDisable((Dialog) target, hwnd);
            } else {
                window.modalEnable((Dialog) target);
            }
        }
    }

    void handleSelected(final char[] buffer) {
        String[] wFiles = (new String(buffer)).split("\0");
        boolean multiple = (wFiles.length > 1);
        String jDirectory = null;
        String jFile = null;
        File[] jFiles = null;
        if (multiple) {
            jDirectory = wFiles[0];
            int filesNumber = wFiles.length - 1;
            jFiles = new File[filesNumber];
            for (int i = 0; i < filesNumber; i++) {
                jFiles[i] = new File(jDirectory, wFiles[i + 1]);
            }
            jFile = wFiles[1];
        } else {
            int index = wFiles[0].lastIndexOf(java.io.File.separatorChar);
            if (index == -1) {
                jDirectory = "." + java.io.File.separator;
                jFile = wFiles[0];
            } else {
                jDirectory = wFiles[0].substring(0, index + 1);
                jFile = wFiles[0].substring(index + 1);
            }
            jFiles = new File[] { new File(jDirectory, jFile) };
        }
        final FileDialog fileDialog = (FileDialog) target;
        AWTAccessor.FileDialogAccessor fileDialogAccessor = AWTAccessor.getFileDialogAccessor();
        fileDialogAccessor.setDirectory(fileDialog, jDirectory);
        fileDialogAccessor.setFile(fileDialog, jFile);
        fileDialogAccessor.setFiles(fileDialog, jFiles);
        WToolkit.executeOnEventHandlerThread(fileDialog, new Runnable() {

            @Override
            public void run() {
                fileDialog.setVisible(false);
            }
        });
    }

    void handleCancel() {
        final FileDialog fileDialog = (FileDialog) target;
        AWTAccessor.getFileDialogAccessor().setFile(fileDialog, null);
        AWTAccessor.getFileDialogAccessor().setFiles(fileDialog, null);
        AWTAccessor.getFileDialogAccessor().setDirectory(fileDialog, null);
        WToolkit.executeOnEventHandlerThread(fileDialog, new Runnable() {

            @Override
            public void run() {
                fileDialog.setVisible(false);
            }
        });
    }

    static {
        String filterString = AccessController.doPrivileged(new PrivilegedAction<String>() {

            @Override
            public String run() {
                try {
                    ResourceBundle rb = ResourceBundle.getBundle("sun.awt.windows.awtLocalization");
                    return rb.getString("allFiles");
                } catch (MissingResourceException e) {
                    return "All Files";
                }
            }
        });
        setFilterString(filterString);
    }

    void blockWindow(WWindowPeer window) {
        blockedWindows.add(window);
        if (hwnd != 0) {
            window.modalDisable((Dialog) target, hwnd);
        }
    }

    void unblockWindow(WWindowPeer window) {
        blockedWindows.remove(window);
        if (hwnd != 0) {
            window.modalEnable((Dialog) target);
        }
    }

    @Override
    public void blockWindows(java.util.List<Window> toBlock) {
        for (Window w : toBlock) {
            WWindowPeer wp = AWTAccessor.getComponentAccessor().getPeer(w);
            if (wp != null) {
                blockWindow(wp);
            }
        }
    }

    @Override
    public native void toFront();

    @Override
    public native void toBack();

    @Override
    public void updateAlwaysOnTopState() {
    }

    @Override
    public void setDirectory(String dir) {
    }

    @Override
    public void setFile(String file) {
    }

    @Override
    public void setTitle(String title) {
    }

    @Override
    public void setResizable(boolean resizable) {
    }

    @Override
    void enable() {
    }

    @Override
    void disable() {
    }

    @Override
    public void reshape(int x, int y, int width, int height) {
    }

    @SuppressWarnings("deprecation")
    public boolean handleEvent(Event e) {
        return false;
    }

    @Override
    public void setForeground(Color c) {
    }

    @Override
    public void setBackground(Color c) {
    }

    @Override
    public void setFont(Font f) {
    }

    @Override
    public void updateMinimumSize() {
    }

    @Override
    public void updateIconImages() {
    }

    public boolean requestFocus(boolean temporary, boolean focusedWindowChangeAllowed) {
        return false;
    }

    @Override
    public boolean requestFocus(Component lightweightChild, boolean temporary, boolean focusedWindowChangeAllowed, long time, Cause cause) {
        return false;
    }

    @Override
    void start() {
    }

    @Override
    public void beginValidate() {
    }

    @Override
    public void endValidate() {
    }

    void invalidate(int x, int y, int width, int height) {
    }

    @Override
    public void addDropTarget(DropTarget dt) {
    }

    @Override
    public void removeDropTarget(DropTarget dt) {
    }

    @Override
    public void updateFocusableWindowState() {
    }

    @Override
    public void setZOrder(ComponentPeer above) {
    }

    private static native void initIDs();

    @Override
    public void applyShape(sun.java2d.pipe.Region shape) {
    }

    @Override
    public void setOpacity(float opacity) {
    }

    @Override
    public void setOpaque(boolean isOpaque) {
    }

    public void updateWindow(java.awt.image.BufferedImage backBuffer) {
    }

    @Override
    public void createScreenSurface(boolean isResize) {
    }

    @Override
    public void replaceSurfaceData() {
    }

    public boolean isMultipleMode() {
        FileDialog fileDialog = (FileDialog) target;
        return AWTAccessor.getFileDialogAccessor().isMultipleMode(fileDialog);
    }

    @Override
    public native Point getLocationOnScreen();
}