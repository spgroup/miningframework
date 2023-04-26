package sun.awt;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.im.InputMethodHighlight;
import java.awt.im.spi.InputMethodContext;
import sun.awt.im.InputMethodAdapter;
import java.awt.event.InputMethodEvent;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.peer.ComponentPeer;
import java.lang.Character.Subset;
import java.text.AttributedString;
import java.text.AttributedCharacterIterator;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import sun.util.logging.PlatformLogger;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public abstract class X11InputMethod extends InputMethodAdapter {

    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11InputMethod");

    private static final int XIMReverse = (1 << 0);

    private static final int XIMUnderline = (1 << 1);

    private static final int XIMHighlight = (1 << 2);

    private static final int XIMPrimary = (1 << 5);

    private static final int XIMSecondary = (1 << 6);

    private static final int XIMTertiary = (1 << 7);

    private static final int XIMVisibleToForward = (1 << 8);

    private static final int XIMVisibleToBackward = (1 << 9);

    private static final int XIMVisibleCenter = (1 << 10);

    private static final int XIMVisibleMask = (XIMVisibleToForward | XIMVisibleToBackward | XIMVisibleCenter);

    private Locale locale;

    private static boolean isXIMOpened = false;

    protected Container clientComponentWindow = null;

    private Component awtFocussedComponent = null;

    private Component lastXICFocussedComponent = null;

    private boolean isLastXICActive = false;

    private boolean isLastTemporary = false;

    private boolean isActive = false;

    private boolean isActiveClient = false;

    private static Map<TextAttribute, ?>[] highlightStyles;

    private boolean disposed = false;

    private boolean needResetXIC = false;

    private WeakReference<Component> needResetXICClient = new WeakReference<>(null);

    private boolean compositionEnableSupported = true;

    private boolean savedCompositionState = false;

    private String committedText = null;

    private StringBuffer composedText = null;

    private IntBuffer rawFeedbacks;

    private transient long pData = 0;

    static {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Map<TextAttribute, ?>[] styles = new Map[4];
        HashMap<TextAttribute, Object> map;
        map = new HashMap<>(1);
        map.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        styles[0] = Collections.unmodifiableMap(map);
        map = new HashMap<>(1);
        map.put(TextAttribute.SWAP_COLORS, TextAttribute.SWAP_COLORS_ON);
        styles[1] = Collections.unmodifiableMap(map);
        map = new HashMap<>(1);
        map.put(TextAttribute.INPUT_METHOD_UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
        styles[2] = Collections.unmodifiableMap(map);
        map = new HashMap<>(1);
        map.put(TextAttribute.SWAP_COLORS, TextAttribute.SWAP_COLORS_ON);
        styles[3] = Collections.unmodifiableMap(map);
        highlightStyles = styles;
    }

    static {
        initIDs();
    }

    private static native void initIDs();

    public X11InputMethod() throws AWTException {
        locale = X11InputMethodDescriptor.getSupportedLocale();
        if (initXIM() == false) {
            throw new AWTException("Cannot open X Input Method");
        }
    }

    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }

    private synchronized boolean initXIM() {
        if (isXIMOpened == false)
            isXIMOpened = openXIM();
        return isXIMOpened;
    }

    protected abstract boolean openXIM();

    protected boolean isDisposed() {
        return disposed;
    }

    protected abstract void setXICFocus(ComponentPeer peer, boolean value, boolean active);

    public void setInputMethodContext(InputMethodContext context) {
    }

    public boolean setLocale(Locale lang) {
        if (lang.equals(locale)) {
            return true;
        }
        if (locale.equals(Locale.JAPAN) && lang.equals(Locale.JAPANESE) || locale.equals(Locale.KOREA) && lang.equals(Locale.KOREAN)) {
            return true;
        }
        return false;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setCharacterSubsets(Subset[] subsets) {
    }

    public void dispatchEvent(AWTEvent e) {
    }

    protected final void resetXICifneeded() {
        if (needResetXIC && haveActiveClient() && getClientComponent() != needResetXICClient.get()) {
            resetXIC();
            lastXICFocussedComponent = null;
            isLastXICActive = false;
            needResetXICClient.clear();
            needResetXIC = false;
        }
    }

    private void resetCompositionState() {
        if (compositionEnableSupported) {
            try {
                setCompositionEnabled(savedCompositionState);
            } catch (UnsupportedOperationException e) {
                compositionEnableSupported = false;
            }
        }
    }

    private boolean getCompositionState() {
        boolean compositionState = false;
        if (compositionEnableSupported) {
            try {
                compositionState = isCompositionEnabled();
            } catch (UnsupportedOperationException e) {
                compositionEnableSupported = false;
            }
        }
        return compositionState;
    }

    public synchronized void activate() {
        clientComponentWindow = getClientComponentWindow();
        if (clientComponentWindow == null)
            return;
        if (lastXICFocussedComponent != null) {
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine("XICFocused {0}, AWTFocused {1}", lastXICFocussedComponent, awtFocussedComponent);
            }
        }
        if (pData == 0) {
            if (!createXIC()) {
                return;
            }
            disposed = false;
        }
        resetXICifneeded();
        ComponentPeer lastXICFocussedComponentPeer = null;
        ComponentPeer awtFocussedComponentPeer = getPeer(awtFocussedComponent);
        if (lastXICFocussedComponent != null) {
            lastXICFocussedComponentPeer = getPeer(lastXICFocussedComponent);
        }
        if (isLastTemporary || lastXICFocussedComponentPeer != awtFocussedComponentPeer || isLastXICActive != haveActiveClient()) {
            if (lastXICFocussedComponentPeer != null) {
                setXICFocus(lastXICFocussedComponentPeer, false, isLastXICActive);
            }
            if (awtFocussedComponentPeer != null) {
                setXICFocus(awtFocussedComponentPeer, true, haveActiveClient());
            }
            lastXICFocussedComponent = awtFocussedComponent;
            isLastXICActive = haveActiveClient();
        }
        resetCompositionState();
        isActive = true;
    }

    protected abstract boolean createXIC();

    public synchronized void deactivate(boolean isTemporary) {
        boolean isAc = haveActiveClient();
        savedCompositionState = getCompositionState();
        if (isTemporary) {
            turnoffStatusWindow();
        }
        lastXICFocussedComponent = awtFocussedComponent;
        isLastXICActive = isAc;
        isLastTemporary = isTemporary;
        isActive = false;
    }

    public void disableInputMethod() {
        if (lastXICFocussedComponent != null) {
            setXICFocus(getPeer(lastXICFocussedComponent), false, isLastXICActive);
            lastXICFocussedComponent = null;
            isLastXICActive = false;
            resetXIC();
            needResetXICClient.clear();
            needResetXIC = false;
        }
    }

    public void hideWindows() {
    }

    public static Map<TextAttribute, ?> mapInputMethodHighlight(InputMethodHighlight highlight) {
        int index;
        int state = highlight.getState();
        if (state == InputMethodHighlight.RAW_TEXT) {
            index = 0;
        } else if (state == InputMethodHighlight.CONVERTED_TEXT) {
            index = 2;
        } else {
            return null;
        }
        if (highlight.isSelected()) {
            index += 1;
        }
        return highlightStyles[index];
    }

    protected void setAWTFocussedComponent(Component component) {
        if (component == null) {
            return;
        }
        if (isActive) {
            boolean ac = haveActiveClient();
            setXICFocus(getPeer(awtFocussedComponent), false, ac);
            setXICFocus(getPeer(component), true, ac);
        }
        awtFocussedComponent = component;
    }

    protected void stopListening() {
        endComposition();
        disableInputMethod();
        if (needResetXIC) {
            resetXIC();
            needResetXICClient.clear();
            needResetXIC = false;
        }
    }

    private Window getClientComponentWindow() {
        Component client = getClientComponent();
        Container container;
        if (client instanceof Container) {
            container = (Container) client;
        } else {
            container = getParent(client);
        }
        while (container != null && !(container instanceof java.awt.Window)) {
            container = getParent(container);
        }
        return (Window) container;
    }

    protected abstract Container getParent(Component client);

    protected abstract ComponentPeer getPeer(Component client);

    protected abstract void awtLock();

    protected abstract void awtUnlock();

    private void postInputMethodEvent(int id, AttributedCharacterIterator text, int committedCharacterCount, TextHitInfo caret, TextHitInfo visiblePosition, long when) {
        Component source = getClientComponent();
        if (source != null) {
            InputMethodEvent event = new InputMethodEvent(source, id, when, text, committedCharacterCount, caret, visiblePosition);
            SunToolkit.postEvent(SunToolkit.targetToAppContext(source), (AWTEvent) event);
        }
    }

    private void postInputMethodEvent(int id, AttributedCharacterIterator text, int committedCharacterCount, TextHitInfo caret, TextHitInfo visiblePosition) {
        postInputMethodEvent(id, text, committedCharacterCount, caret, visiblePosition, EventQueue.getMostRecentEventTime());
    }

    void dispatchCommittedText(String str, long when) {
        if (str == null)
            return;
        if (composedText == null) {
            AttributedString attrstr = new AttributedString(str);
            postInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, attrstr.getIterator(), str.length(), null, null, when);
        } else {
            committedText = str;
        }
    }

    private void dispatchCommittedText(String str) {
        dispatchCommittedText(str, EventQueue.getMostRecentEventTime());
    }

    void dispatchComposedText(String chgText, int[] chgStyles, int chgOffset, int chgLength, int caretPosition, long when) {
        if (disposed) {
            return;
        }
        if (chgText == null && chgStyles == null && chgOffset == 0 && chgLength == 0 && caretPosition == 0 && composedText == null && committedText == null)
            return;
        if (composedText == null) {
            composedText = new StringBuffer(INITIAL_SIZE);
            rawFeedbacks = new IntBuffer(INITIAL_SIZE);
        }
        if (chgLength > 0) {
            if (chgText == null && chgStyles != null) {
                rawFeedbacks.replace(chgOffset, chgStyles);
            } else {
                if (chgLength == composedText.length()) {
                    composedText = new StringBuffer(INITIAL_SIZE);
                    rawFeedbacks = new IntBuffer(INITIAL_SIZE);
                } else {
                    if (composedText.length() > 0) {
                        if (chgOffset + chgLength < composedText.length()) {
                            String text;
                            text = composedText.toString().substring(chgOffset + chgLength, composedText.length());
                            composedText.setLength(chgOffset);
                            composedText.append(text);
                        } else {
                            composedText.setLength(chgOffset);
                        }
                        rawFeedbacks.remove(chgOffset, chgLength);
                    }
                }
            }
        }
        if (chgText != null) {
            composedText.insert(chgOffset, chgText);
            if (chgStyles != null)
                rawFeedbacks.insert(chgOffset, chgStyles);
        }
        if (composedText.length() == 0) {
            composedText = null;
            rawFeedbacks = null;
            if (committedText != null) {
                dispatchCommittedText(committedText, when);
                committedText = null;
                return;
            }
            postInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, null, 0, null, null, when);
            return;
        }
        int composedOffset;
        AttributedString inputText;
        if (committedText != null) {
            composedOffset = committedText.length();
            inputText = new AttributedString(committedText + composedText);
            committedText = null;
        } else {
            composedOffset = 0;
            inputText = new AttributedString(composedText.toString());
        }
        int currentFeedback;
        int nextFeedback;
        int startOffset = 0;
        int currentOffset;
        int visiblePosition = 0;
        TextHitInfo visiblePositionInfo = null;
        rawFeedbacks.rewind();
        currentFeedback = rawFeedbacks.getNext();
        rawFeedbacks.unget();
        while ((nextFeedback = rawFeedbacks.getNext()) != -1) {
            if (visiblePosition == 0) {
                visiblePosition = nextFeedback & XIMVisibleMask;
                if (visiblePosition != 0) {
                    int index = rawFeedbacks.getOffset() - 1;
                    if (visiblePosition == XIMVisibleToBackward)
                        visiblePositionInfo = TextHitInfo.leading(index);
                    else
                        visiblePositionInfo = TextHitInfo.trailing(index);
                }
            }
            nextFeedback &= ~XIMVisibleMask;
            if (currentFeedback != nextFeedback) {
                rawFeedbacks.unget();
                currentOffset = rawFeedbacks.getOffset();
                inputText.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT, convertVisualFeedbackToHighlight(currentFeedback), composedOffset + startOffset, composedOffset + currentOffset);
                startOffset = currentOffset;
                currentFeedback = nextFeedback;
            }
        }
        currentOffset = rawFeedbacks.getOffset();
        if (currentOffset >= 0) {
            inputText.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT, convertVisualFeedbackToHighlight(currentFeedback), composedOffset + startOffset, composedOffset + currentOffset);
        }
        postInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, inputText.getIterator(), composedOffset, TextHitInfo.leading(caretPosition), visiblePositionInfo, when);
    }

    void flushText() {
        String flush = (committedText != null ? committedText : "");
        if (composedText != null) {
            flush += composedText.toString();
        }
        if (!flush.equals("")) {
            AttributedString attrstr = new AttributedString(flush);
            postInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, attrstr.getIterator(), flush.length(), null, null, EventQueue.getMostRecentEventTime());
            composedText = null;
            committedText = null;
        }
    }

    protected synchronized void disposeImpl() {
        disposeXIC();
        awtLock();
        composedText = null;
        committedText = null;
        rawFeedbacks = null;
        awtUnlock();
        awtFocussedComponent = null;
        lastXICFocussedComponent = null;
    }

    public final void dispose() {
        boolean call_disposeImpl = false;
        if (!disposed) {
            synchronized (this) {
                if (!disposed) {
                    disposed = call_disposeImpl = true;
                }
            }
        }
        if (call_disposeImpl) {
            disposeImpl();
        }
    }

    public Object getControlObject() {
        return null;
    }

    public synchronized void removeNotify() {
        dispose();
    }

    public void setCompositionEnabled(boolean enable) {
        if (setCompositionEnabledNative(enable)) {
            savedCompositionState = enable;
        }
    }

    public boolean isCompositionEnabled() {
        return isCompositionEnabledNative();
    }

    public void endComposition() {
        if (disposed) {
            return;
        }
        savedCompositionState = getCompositionState();
        boolean active = haveActiveClient();
        if (active && composedText == null && committedText == null) {
            needResetXIC = true;
            needResetXICClient = new WeakReference<>(getClientComponent());
            return;
        }
        String text = resetXIC();
        if (active) {
            needResetXIC = false;
        }
        awtLock();
        composedText = null;
        postInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED, null, 0, null, null);
        if (text != null && text.length() > 0) {
            dispatchCommittedText(text);
        }
        awtUnlock();
        if (savedCompositionState) {
            resetCompositionState();
        }
    }

    public String getNativeInputMethodInfo() {
        String xmodifiers = System.getenv("XMODIFIERS");
        String imInfo = null;
        if (xmodifiers != null) {
            int imIndex = xmodifiers.indexOf("@im=");
            if (imIndex != -1) {
                imInfo = xmodifiers.substring(imIndex + 4);
            }
        } else if (System.getProperty("os.name").startsWith("SunOS")) {
            File dtprofile = new File(System.getProperty("user.home") + "/.dtprofile");
            String languageEngineInfo = null;
            try {
                BufferedReader br = new BufferedReader(new FileReader(dtprofile));
                String line = null;
                while (languageEngineInfo == null && (line = br.readLine()) != null) {
                    if (line.contains("atok") || line.contains("wnn")) {
                        StringTokenizer tokens = new StringTokenizer(line);
                        while (tokens.hasMoreTokens()) {
                            String token = tokens.nextToken();
                            if (Pattern.matches("atok.*setup", token) || Pattern.matches("wnn.*setup", token)) {
                                languageEngineInfo = token.substring(0, token.indexOf("setup"));
                                break;
                            }
                        }
                    }
                }
                br.close();
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
            imInfo = "htt " + languageEngineInfo;
        }
        return imInfo;
    }

    private InputMethodHighlight convertVisualFeedbackToHighlight(int feedback) {
        InputMethodHighlight highlight;
        switch(feedback) {
            case XIMUnderline:
                highlight = InputMethodHighlight.UNSELECTED_CONVERTED_TEXT_HIGHLIGHT;
                break;
            case XIMReverse:
                highlight = InputMethodHighlight.SELECTED_CONVERTED_TEXT_HIGHLIGHT;
                break;
            case XIMHighlight:
                highlight = InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT;
                break;
            case XIMPrimary:
                highlight = InputMethodHighlight.UNSELECTED_CONVERTED_TEXT_HIGHLIGHT;
                break;
            case XIMSecondary:
                highlight = InputMethodHighlight.SELECTED_CONVERTED_TEXT_HIGHLIGHT;
                break;
            case XIMTertiary:
                highlight = InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT;
                break;
            default:
                highlight = InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT;
                break;
        }
        return highlight;
    }

    private static final int INITIAL_SIZE = 64;

    private final class IntBuffer {

        private int[] intArray;

        private int size;

        private int index;

        IntBuffer(int initialCapacity) {
            intArray = new int[initialCapacity];
            size = 0;
            index = 0;
        }

        void insert(int offset, int[] values) {
            int newSize = size + values.length;
            if (intArray.length < newSize) {
                int[] newIntArray = new int[newSize * 2];
                System.arraycopy(intArray, 0, newIntArray, 0, size);
                intArray = newIntArray;
            }
            System.arraycopy(intArray, offset, intArray, offset + values.length, size - offset);
            System.arraycopy(values, 0, intArray, offset, values.length);
            size += values.length;
            if (index > offset)
                index = offset;
        }

        void remove(int offset, int length) {
            if (offset + length != size)
                System.arraycopy(intArray, offset + length, intArray, offset, size - offset - length);
            size -= length;
            if (index > offset)
                index = offset;
        }

        void replace(int offset, int[] values) {
            System.arraycopy(values, 0, intArray, offset, values.length);
        }

        void removeAll() {
            size = 0;
            index = 0;
        }

        void rewind() {
            index = 0;
        }

        int getNext() {
            if (index == size)
                return -1;
            return intArray[index++];
        }

        void unget() {
            if (index != 0)
                index--;
        }

        int getOffset() {
            return index;
        }

        public String toString() {
            StringBuffer s = new StringBuffer();
            for (int i = 0; i < size; ) {
                s.append(intArray[i++]);
                if (i < size)
                    s.append(",");
            }
            return s.toString();
        }
    }

    private native String resetXIC();

    private native void disposeXIC();

    private native boolean setCompositionEnabledNative(boolean enable);

    private native boolean isCompositionEnabledNative();

    private native void turnoffStatusWindow();
}
