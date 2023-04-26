package sun.awt.windows;

import java.awt.Color;
import java.awt.Font;
import static java.awt.RenderingHints.*;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import sun.util.logging.PlatformLogger;
import sun.awt.SunToolkit;

class WDesktopProperties {

    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.windows.WDesktopProperties");

    private static final String PREFIX = "win.";

    private static final String FILE_PREFIX = "awt.file.";

    private static final String PROP_NAMES = "win.propNames";

    private long pData;

    static {
        initIDs();
    }

    private WToolkit wToolkit;

    private HashMap<String, Object> map = new HashMap<String, Object>();

    private static native void initIDs();

    static boolean isWindowsProperty(String name) {
        return name.startsWith(PREFIX) || name.startsWith(FILE_PREFIX) || name.equals(SunToolkit.DESKTOPFONTHINTS);
    }

    WDesktopProperties(WToolkit wToolkit) {
        this.wToolkit = wToolkit;
        init();
    }

    private native void init();

    private String[] getKeyNames() {
        Object[] keys = map.keySet().toArray();
        String[] sortedKeys = new String[keys.length];
        for (int nkey = 0; nkey < keys.length; nkey++) {
            sortedKeys[nkey] = keys[nkey].toString();
        }
        Arrays.sort(sortedKeys);
        return sortedKeys;
    }

    private native void getWindowsParameters();

    private synchronized void setBooleanProperty(String key, boolean value) {
        assert (key != null);
        if (log.isLoggable(PlatformLogger.FINE)) {
            log.fine(key + "=" + String.valueOf(value));
        }
        map.put(key, Boolean.valueOf(value));
    }

    private synchronized void setIntegerProperty(String key, int value) {
        assert (key != null);
        if (log.isLoggable(PlatformLogger.FINE)) {
            log.fine(key + "=" + String.valueOf(value));
        }
        map.put(key, Integer.valueOf(value));
    }

    private synchronized void setStringProperty(String key, String value) {
        assert (key != null);
        if (log.isLoggable(PlatformLogger.FINE)) {
            log.fine(key + "=" + value);
        }
        map.put(key, value);
    }

    private synchronized void setColorProperty(String key, int r, int g, int b) {
        assert (key != null && r <= 255 && g <= 255 && b <= 255);
        Color color = new Color(r, g, b);
        if (log.isLoggable(PlatformLogger.FINE)) {
            log.fine(key + "=" + color);
        }
        map.put(key, color);
    }

    static HashMap<String, String> fontNameMap;

    static {
        fontNameMap = new HashMap<String, String>();
        fontNameMap.put("Courier", Font.MONOSPACED);
        fontNameMap.put("MS Serif", "Microsoft Serif");
        fontNameMap.put("MS Sans Serif", "Microsoft Sans Serif");
        fontNameMap.put("Terminal", Font.DIALOG);
        fontNameMap.put("FixedSys", Font.MONOSPACED);
        fontNameMap.put("System", Font.DIALOG);
    }

    private synchronized void setFontProperty(String key, String name, int style, int size) {
        assert (key != null && style <= (Font.BOLD | Font.ITALIC) && size >= 0);
        String mappedName = fontNameMap.get(name);
        if (mappedName != null) {
            name = mappedName;
        }
        Font font = new Font(name, style, size);
        if (log.isLoggable(PlatformLogger.FINE)) {
            log.fine(key + "=" + font);
        }
        map.put(key, font);
        String sizeKey = key + ".height";
        Integer iSize = Integer.valueOf(size);
        if (log.isLoggable(PlatformLogger.FINE)) {
            log.fine(sizeKey + "=" + iSize);
        }
        map.put(sizeKey, iSize);
    }

    private synchronized void setSoundProperty(String key, String winEventName) {
        assert (key != null && winEventName != null);
        Runnable soundRunnable = new WinPlaySound(winEventName);
        if (log.isLoggable(PlatformLogger.FINE)) {
            log.fine(key + "=" + soundRunnable);
        }
        map.put(key, soundRunnable);
    }

    private native void playWindowsSound(String winEventName);

    class WinPlaySound implements Runnable {

        String winEventName;

        WinPlaySound(String winEventName) {
            this.winEventName = winEventName;
        }

        public void run() {
            WDesktopProperties.this.playWindowsSound(winEventName);
        }

        public String toString() {
            return "WinPlaySound(" + winEventName + ")";
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            try {
                return winEventName.equals(((WinPlaySound) o).winEventName);
            } catch (Exception e) {
                return false;
            }
        }

        public int hashCode() {
            return winEventName.hashCode();
        }
    }

    @SuppressWarnings("unchecked")
    synchronized Map<String, Object> getProperties() {
        ThemeReader.flush();
        map = new HashMap<String, Object>();
        getWindowsParameters();
        map.put(SunToolkit.DESKTOPFONTHINTS, SunToolkit.getDesktopFontHints());
        map.put(PROP_NAMES, getKeyNames());
        map.put("DnD.Autoscroll.cursorHysteresis", map.get("win.drag.x"));
        return (Map<String, Object>) map.clone();
    }

    synchronized RenderingHints getDesktopAAHints() {
        Object fontSmoothingHint = VALUE_TEXT_ANTIALIAS_DEFAULT;
        Integer fontSmoothingContrast = null;
        Boolean smoothingOn = (Boolean) map.get("win.text.fontSmoothingOn");
        if (smoothingOn != null && smoothingOn.equals(Boolean.TRUE)) {
            Integer typeID = (Integer) map.get("win.text.fontSmoothingType");
            if (typeID == null || typeID.intValue() <= 1 || typeID.intValue() > 2) {
                fontSmoothingHint = VALUE_TEXT_ANTIALIAS_GASP;
            } else {
                Integer orientID = (Integer) map.get("win.text.fontSmoothingOrientation");
                if (orientID == null || orientID.intValue() != 0) {
                    fontSmoothingHint = VALUE_TEXT_ANTIALIAS_LCD_HRGB;
                } else {
                    fontSmoothingHint = VALUE_TEXT_ANTIALIAS_LCD_HBGR;
                }
                fontSmoothingContrast = (Integer) map.get("win.text.fontSmoothingContrast");
                if (fontSmoothingContrast == null) {
                    fontSmoothingContrast = Integer.valueOf(140);
                } else {
                    fontSmoothingContrast = Integer.valueOf(fontSmoothingContrast.intValue() / 10);
                }
            }
        }
        RenderingHints hints = new RenderingHints(null);
        hints.put(KEY_TEXT_ANTIALIASING, fontSmoothingHint);
        if (fontSmoothingContrast != null) {
            hints.put(KEY_TEXT_LCD_CONTRAST, fontSmoothingContrast);
        }
        return hints;
    }
}
