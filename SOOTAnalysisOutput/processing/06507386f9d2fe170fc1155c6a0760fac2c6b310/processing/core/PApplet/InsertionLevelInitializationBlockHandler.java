package processing.core;

import processing.data.*;
import processing.event.*;
import processing.event.Event;
import processing.opengl.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

public class PApplet extends Applet implements PConstants, Runnable, MouseListener, MouseWheelListener, MouseMotionListener, KeyListener, FocusListener {

    public static final String javaVersionName = System.getProperty("java.version");

    public static final float javaVersion = new Float(javaVersionName.substring(0, 3)).floatValue();

    static public int platform;

    static {
        String osname = System.getProperty("os.name");
        if (osname.indexOf("Mac") != -1) {
            platform = MACOSX;
        } else if (osname.indexOf("Windows") != -1) {
            platform = WINDOWS;
        } else if (osname.equals("Linux")) {
            platform = LINUX;
        } else {
            platform = OTHER;
        }
    }

    static public boolean useQuartz = false;

    static public boolean useNativeSelect = (platform != LINUX);

    public PGraphics g;

    public Frame frame;

    boolean useActive = true;

    boolean useStrategy = false;

    Canvas canvas;

    public int displayWidth;

    public int displayHeight;

    public PGraphics recorder;

    public String[] args;

    public String sketchPath;

    static final boolean DEBUG = false;

    static public final int DEFAULT_WIDTH = 100;

    static public final int DEFAULT_HEIGHT = 100;

    static public final int MIN_WINDOW_WIDTH = 128;

    static public final int MIN_WINDOW_HEIGHT = 128;

    static public class RendererChangeException extends RuntimeException {
    }

    public boolean defaultSize;

    Dimension currentSize = new Dimension();

    public int[] pixels;

    public int width;

    public int height;

    public int mouseX;

    public int mouseY;

    public int pmouseX;

    public int pmouseY;

    protected int dmouseX, dmouseY;

    protected int emouseX, emouseY;

    @Deprecated
    public boolean firstMouse;

    public int mouseButton;

    public boolean mousePressed;

    @Deprecated
    public MouseEvent mouseEvent;

    public char key;

    public int keyCode;

    public boolean keyPressed;

    @Deprecated
    public KeyEvent keyEvent;

    public boolean focused = false;

    @Deprecated
    public boolean online = false;

    long millisOffset = System.currentTimeMillis();

    public float frameRate = 10;

    protected long frameRateLastNanos = 0;

    protected float frameRateTarget = 60;

    protected long frameRatePeriod = 1000000000L / 60L;

    protected boolean looping;

    protected boolean redraw;

    public int frameCount;

    public volatile boolean finished;

    public volatile boolean paused;

    protected boolean exitCalled;

    Object pauseObject = new Object();

    Thread thread;

    static public final String ARGS_EDITOR_LOCATION = "--editor-location";

    static public final String ARGS_EXTERNAL = "--external";

    static public final String ARGS_LOCATION = "--location";

    static public final String ARGS_DISPLAY = "--display";

    static public final String ARGS_BGCOLOR = "--bgcolor";

    static public final String ARGS_PRESENT = "--present";

    static public final String ARGS_FULL_SCREEN = "--full-screen";

    static public final String ARGS_STOP_COLOR = "--stop-color";

    static public final String ARGS_HIDE_STOP = "--hide-stop";

    static public final String ARGS_SKETCH_FOLDER = "--sketch-path";

    static public final String EXTERNAL_STOP = "__STOP__";

    static public final String EXTERNAL_MOVE = "__MOVE__";

    boolean external = false;

    boolean retina;

    static final String ERROR_MIN_MAX = "Cannot use min() or max() on an empty array.";

    @Override
    public void init() {
        if (platform == MACOSX) {
            Float prop = (Float) getToolkit().getDesktopProperty("apple.awt.contentScaleFactor");
            if (prop != null) {
                retina = prop == 2;
                if (retina) {
                    useActive = false;
                }
            }
        }
        setFocusTraversalKeysEnabled(false);
        finished = false;
        looping = true;
        redraw = true;
        firstMouse = true;
        try {
            getAppletContext();
            online = true;
        } catch (NullPointerException e) {
            online = false;
        }
        try {
            if (sketchPath == null) {
                sketchPath = System.getProperty("user.dir");
            }
        } catch (Exception e) {
        }
        Dimension size = getSize();
        if ((size.width != 0) && (size.height != 0)) {
            g = makeGraphics(size.width, size.height, sketchRenderer(), null, true);
        } else {
            this.defaultSize = true;
            int w = sketchWidth();
            int h = sketchHeight();
            g = makeGraphics(w, h, sketchRenderer(), null, true);
            setSize(w, h);
            setPreferredSize(new Dimension(w, h));
        }
        width = g.width;
        height = g.height;
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                if (!looping) {
                    redraw();
                }
            }
        });
        thread = new Thread(this, "Animation Thread");
        thread.start();
    }

    public int sketchQuality() {
        return 2;
    }

    public int sketchWidth() {
        return DEFAULT_WIDTH;
    }

    public int sketchHeight() {
        return DEFAULT_HEIGHT;
    }

    public String sketchRenderer() {
        return JAVA2D;
    }

    public boolean sketchFullScreen() {
        return false;
    }

    public void orientation(int which) {
    }

    @Override
    public void start() {
        debug("start() called");
        paused = false;
        resume();
        handleMethods("resume");
        debug("un-pausing thread");
        synchronized (pauseObject) {
            debug("start() calling pauseObject.notifyAll()");
            pauseObject.notifyAll();
            debug("un-pausing thread 3");
        }
    }

    @Override
    public void stop() {
        paused = true;
        pause();
        handleMethods("pause");
    }

    public void pause() {
    }

    public void resume() {
    }

    @Override
    public void destroy() {
        this.dispose();
    }

    HashMap<String, RegisteredMethods> registerMap = new HashMap<String, PApplet.RegisteredMethods>();

    class RegisteredMethods {

        int count;

        Object[] objects;

        Method[] methods;

        Object[] emptyArgs = new Object[] {};

        void handle() {
            handle(emptyArgs);
        }

        void handle(Object[] args) {
            for (int i = 0; i < count; i++) {
                try {
                    methods[i].invoke(objects[i], args);
                } catch (Exception e) {
                    Throwable t;
                    if (e instanceof InvocationTargetException) {
                        InvocationTargetException ite = (InvocationTargetException) e;
                        t = ite.getCause();
                    } else {
                        t = e;
                    }
                    if (t instanceof RuntimeException) {
                        throw (RuntimeException) t;
                    } else {
                        t.printStackTrace();
                    }
                }
            }
        }

        void add(Object object, Method method) {
            if (findIndex(object) == -1) {
                if (objects == null) {
                    objects = new Object[5];
                    methods = new Method[5];
                } else if (count == objects.length) {
                    objects = (Object[]) PApplet.expand(objects);
                    methods = (Method[]) PApplet.expand(methods);
                }
                objects[count] = object;
                methods[count] = method;
                count++;
            } else {
                die(method.getName() + "() already added for this instance of " + object.getClass().getName());
            }
        }

        public void remove(Object object) {
            int index = findIndex(object);
            if (index != -1) {
                count--;
                for (int i = index; i < count; i++) {
                    objects[i] = objects[i + 1];
                    methods[i] = methods[i + 1];
                }
                objects[count] = null;
                methods[count] = null;
            }
        }

        protected int findIndex(Object object) {
            for (int i = 0; i < count; i++) {
                if (objects[i] == object) {
                    return i;
                }
            }
            return -1;
        }
    }

    public void registerMethod(String methodName, Object target) {
        if (methodName.equals("mouseEvent")) {
            registerWithArgs("mouseEvent", target, new Class[] { processing.event.MouseEvent.class });
        } else if (methodName.equals("keyEvent")) {
            registerWithArgs("keyEvent", target, new Class[] { processing.event.KeyEvent.class });
        } else if (methodName.equals("touchEvent")) {
            registerWithArgs("touchEvent", target, new Class[] { processing.event.TouchEvent.class });
        } else {
            registerNoArgs(methodName, target);
        }
    }

    private void registerNoArgs(String name, Object o) {
        RegisteredMethods meth = registerMap.get(name);
        if (meth == null) {
            meth = new RegisteredMethods();
            registerMap.put(name, meth);
        }
        Class<?> c = o.getClass();
        try {
            Method method = c.getMethod(name, new Class[] {});
            meth.add(o, method);
        } catch (NoSuchMethodException nsme) {
            die("There is no public " + name + "() method in the class " + o.getClass().getName());
        } catch (Exception e) {
            die("Could not register " + name + " + () for " + o, e);
        }
    }

    private void registerWithArgs(String name, Object o, Class<?>[] cargs) {
        RegisteredMethods meth = registerMap.get(name);
        if (meth == null) {
            meth = new RegisteredMethods();
            registerMap.put(name, meth);
        }
        Class<?> c = o.getClass();
        try {
            Method method = c.getMethod(name, cargs);
            meth.add(o, method);
        } catch (NoSuchMethodException nsme) {
            die("There is no public " + name + "() method in the class " + o.getClass().getName());
        } catch (Exception e) {
            die("Could not register " + name + " + () for " + o, e);
        }
    }

    public void unregisterMethod(String name, Object target) {
        RegisteredMethods meth = registerMap.get(name);
        if (meth == null) {
            die("No registered methods with the name " + name + "() were found.");
        }
        try {
            meth.remove(target);
        } catch (Exception e) {
            die("Could not unregister " + name + "() for " + target, e);
        }
    }

    protected void handleMethods(String methodName) {
        RegisteredMethods meth = registerMap.get(methodName);
        if (meth != null) {
            meth.handle();
        }
    }

    protected void handleMethods(String methodName, Object[] args) {
        RegisteredMethods meth = registerMap.get(methodName);
        if (meth != null) {
            meth.handle(args);
        }
    }

    @Deprecated
    public void registerSize(Object o) {
        System.err.println("The registerSize() command is no longer supported.");
    }

    @Deprecated
    public void registerPre(Object o) {
        registerNoArgs("pre", o);
    }

    @Deprecated
    public void registerDraw(Object o) {
        registerNoArgs("draw", o);
    }

    @Deprecated
    public void registerPost(Object o) {
        registerNoArgs("post", o);
    }

    @Deprecated
    public void registerDispose(Object o) {
        registerNoArgs("dispose", o);
    }

    @Deprecated
    public void unregisterSize(Object o) {
        System.err.println("The unregisterSize() command is no longer supported.");
    }

    @Deprecated
    public void unregisterPre(Object o) {
        unregisterMethod("pre", o);
    }

    @Deprecated
    public void unregisterDraw(Object o) {
        unregisterMethod("draw", o);
    }

    @Deprecated
    public void unregisterPost(Object o) {
        unregisterMethod("post", o);
    }

    @Deprecated
    public void unregisterDispose(Object o) {
        unregisterMethod("dispose", o);
    }

    RegisteredMethods mouseEventMethods, keyEventMethods;

    protected void reportDeprecation(Class<?> c, boolean mouse) {
        if (g != null) {
            PGraphics.showWarning("The class " + c.getName() + " is incompatible with Processing 2.0.");
            PGraphics.showWarning("A library (or other code) is using register" + (mouse ? "Mouse" : "Key") + "Event() " + "which is no longer available.");
            if (g instanceof PGraphicsOpenGL) {
                PGraphics.showWarning("Stopping the sketch because this code will " + "not work correctly with OpenGL.");
                throw new RuntimeException("This sketch uses a library that " + "needs to be updated for Processing 2.0.");
            }
        }
    }

    @Deprecated
    public void registerMouseEvent(Object o) {
        Class<?> c = o.getClass();
        reportDeprecation(c, true);
        try {
            Method method = c.getMethod("mouseEvent", new Class[] { java.awt.event.MouseEvent.class });
            if (mouseEventMethods == null) {
                mouseEventMethods = new RegisteredMethods();
            }
            mouseEventMethods.add(o, method);
        } catch (Exception e) {
            die("Could not register mouseEvent() for " + o, e);
        }
    }

    @Deprecated
    public void unregisterMouseEvent(Object o) {
        try {
            mouseEventMethods.remove(o);
        } catch (Exception e) {
            die("Could not unregister mouseEvent() for " + o, e);
        }
    }

    @Deprecated
    public void registerKeyEvent(Object o) {
        Class<?> c = o.getClass();
        reportDeprecation(c, false);
        try {
            Method method = c.getMethod("keyEvent", new Class[] { java.awt.event.KeyEvent.class });
            if (keyEventMethods == null) {
                keyEventMethods = new RegisteredMethods();
            }
            keyEventMethods.add(o, method);
        } catch (Exception e) {
            die("Could not register keyEvent() for " + o, e);
        }
    }

    @Deprecated
    public void unregisterKeyEvent(Object o) {
        try {
            keyEventMethods.remove(o);
        } catch (Exception e) {
            die("Could not unregister keyEvent() for " + o, e);
        }
    }

    public void setup() {
    }

    public void draw() {
        finished = true;
    }

    protected void resizeRenderer(int newWidth, int newHeight) {
        debug("resizeRenderer request for " + newWidth + " " + newHeight);
        if (width != newWidth || height != newHeight) {
            debug("  former size was " + width + " " + height);
            g.setSize(newWidth, newHeight);
            width = newWidth;
            height = newHeight;
        }
    }

    public void size(int w, int h) {
        size(w, h, JAVA2D, null);
    }

    public void size(int w, int h, String renderer) {
        size(w, h, renderer, null);
    }

    public void size(final int w, final int h, String renderer, String path) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                setPreferredSize(new Dimension(w, h));
                setSize(w, h);
            }
        });
        if (path != null)
            path = savePath(path);
        String currentRenderer = g.getClass().getName();
        if (currentRenderer.equals(renderer)) {
            resizeRenderer(w, h);
        } else {
            g = makeGraphics(w, h, renderer, path, true);
            this.width = w;
            this.height = h;
            defaultSize = false;
            throw new RendererChangeException();
        }
    }

    public PGraphics createGraphics(int w, int h) {
        return createGraphics(w, h, JAVA2D);
    }

    public PGraphics createGraphics(int w, int h, String renderer) {
        PGraphics pg = makeGraphics(w, h, renderer, null, false);
        return pg;
    }

    public PGraphics createGraphics(int w, int h, String renderer, String path) {
        if (path != null) {
            path = savePath(path);
        }
        PGraphics pg = makeGraphics(w, h, renderer, path, false);
        pg.parent = this;
        return pg;
    }

    protected PGraphics makeGraphics(int w, int h, String renderer, String path, boolean primary) {
        String openglError = external ? "Before using OpenGL, first select " + "Import Library > OpenGL from the Sketch menu." : "The Java classpath and native library path is not " + "properly set for using the OpenGL library.";
        if (!primary && !g.isGL()) {
            if (renderer.equals(P2D)) {
                throw new RuntimeException("createGraphics() with P2D requires size() to use P2D or P3D");
            } else if (renderer.equals(P3D)) {
                throw new RuntimeException("createGraphics() with P3D or OPENGL requires size() to use P2D or P3D");
            }
        }
        try {
            Class<?> rendererClass = Thread.currentThread().getContextClassLoader().loadClass(renderer);
            Constructor<?> constructor = rendererClass.getConstructor(new Class[] {});
            PGraphics pg = (PGraphics) constructor.newInstance();
            pg.setParent(this);
            pg.setPrimary(primary);
            if (path != null)
                pg.setPath(path);
            pg.setSize(w, h);
            return pg;
        } catch (InvocationTargetException ite) {
            String msg = ite.getTargetException().getMessage();
            if ((msg != null) && (msg.indexOf("no jogl in java.library.path") != -1)) {
                throw new RuntimeException(openglError + " (The native library is missing.)");
            } else {
                ite.getTargetException().printStackTrace();
                Throwable target = ite.getTargetException();
                if (platform == MACOSX)
                    target.printStackTrace(System.out);
                throw new RuntimeException(target.getMessage());
            }
        } catch (ClassNotFoundException cnfe) {
            if (cnfe.getMessage().indexOf("processing.opengl.PGraphicsOpenGL") != -1) {
                throw new RuntimeException(openglError + " (The library .jar file is missing.)");
            } else {
                throw new RuntimeException("You need to use \"Import Library\" " + "to add " + renderer + " to your sketch.");
            }
        } catch (Exception e) {
            if ((e instanceof IllegalArgumentException) || (e instanceof NoSuchMethodException) || (e instanceof IllegalAccessException)) {
                if (e.getMessage().contains("cannot be <= 0")) {
                    throw new RuntimeException(e);
                } else {
                    e.printStackTrace();
                    String msg = renderer + " needs to be updated " + "for the current release of Processing.";
                    throw new RuntimeException(msg);
                }
            } else {
                if (platform == MACOSX)
                    e.printStackTrace(System.out);
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public PImage createImage(int w, int h, int format) {
        PImage image = new PImage(w, h, format);
        image.parent = this;
        return image;
    }

    @Override
    public void update(Graphics screen) {
        paint(screen);
    }

    @Override
    public void paint(Graphics screen) {
        if (frameCount == 0) {
            return;
        }
        if (!insideDraw && (g != null) && (g.image != null)) {
            if (useStrategy) {
                render();
            } else {
                screen.drawImage(g.image, 0, 0, width, height, null);
            }
        } else {
            debug(insideDraw + " " + g + " " + ((g != null) ? g.image : "-"));
        }
    }

    protected synchronized void render() {
        if (canvas == null) {
            removeListeners(this);
            canvas = new Canvas();
            add(canvas);
            setIgnoreRepaint(true);
            canvas.setIgnoreRepaint(true);
            addListeners(canvas);
        }
        canvas.setBounds(0, 0, width, height);
        if (canvas.getBufferStrategy() == null) {
            canvas.createBufferStrategy(2);
        }
        BufferStrategy strategy = canvas.getBufferStrategy();
        if (strategy == null) {
            return;
        }
        do {
            do {
                Graphics draw = strategy.getDrawGraphics();
                draw.drawImage(g.image, 0, 0, width, height, null);
                draw.dispose();
            } while (strategy.contentsRestored());
            strategy.show();
        } while (strategy.contentsLost());
    }

    public void run() {
        long beforeTime = System.nanoTime();
        long overSleepTime = 0L;
        int noDelays = 0;
        final int NO_DELAYS_PER_YIELD = 15;
        if (!online) {
            start();
        }
        while ((Thread.currentThread() == thread) && !finished) {
            if (paused) {
                debug("PApplet.run() paused, calling object wait...");
                synchronized (pauseObject) {
                    try {
                        pauseObject.wait();
                        debug("out of wait");
                    } catch (InterruptedException e) {
                    }
                }
            }
            debug("done with pause");
            if (g != null) {
                getSize(currentSize);
                if (currentSize.width != g.width || currentSize.height != g.height) {
                    resizeRenderer(currentSize.width, currentSize.height);
                }
            }
            if (g != null)
                g.requestDraw();
            if (frameCount == 1) {
                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        requestFocusInWindow();
                    }
                });
            }
            long afterTime = System.nanoTime();
            long timeDiff = afterTime - beforeTime;
            long sleepTime = (frameRatePeriod - timeDiff) - overSleepTime;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1000000L, (int) (sleepTime % 1000000L));
                    noDelays = 0;
                } catch (InterruptedException ex) {
                }
                overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
            } else {
                overSleepTime = 0L;
                noDelays++;
                if (noDelays > NO_DELAYS_PER_YIELD) {
                    Thread.yield();
                    noDelays = 0;
                }
            }
            beforeTime = System.nanoTime();
        }
        dispose();
        if (exitCalled) {
            exitActual();
        }
    }

    protected boolean insideDraw;

    public void handleDraw() {
        debug("handleDraw() " + g + " " + looping + " " + redraw + " valid:" + this.isValid() + " visible:" + this.isVisible());
        if (canDraw()) {
            if (!g.canDraw()) {
                debug("g.canDraw() is false");
                return;
            }
            insideDraw = true;
            g.beginDraw();
            if (recorder != null) {
                recorder.beginDraw();
            }
            long now = System.nanoTime();
            if (frameCount == 0) {
                GraphicsConfiguration gc = getGraphicsConfiguration();
                if (gc == null)
                    return;
                GraphicsDevice displayDevice = getGraphicsConfiguration().getDevice();
                if (displayDevice == null)
                    return;
                Rectangle screenRect = displayDevice.getDefaultConfiguration().getBounds();
                displayWidth = screenRect.width;
                displayHeight = screenRect.height;
                try {
                    setup();
                } catch (RendererChangeException e) {
                    return;
                }
                this.defaultSize = false;
            } else {
                double rate = 1000000.0 / ((now - frameRateLastNanos) / 1000000.0);
                float instantaneousRate = (float) rate / 1000.0f;
                frameRate = (frameRate * 0.9f) + (instantaneousRate * 0.1f);
                if (frameCount != 0) {
                    handleMethods("pre");
                }
                pmouseX = dmouseX;
                pmouseY = dmouseY;
                draw();
                dmouseX = mouseX;
                dmouseY = mouseY;
                dequeueEvents();
                handleMethods("draw");
                redraw = false;
            }
            g.endDraw();
            if (recorder != null) {
                recorder.endDraw();
            }
            insideDraw = false;
            if (useActive) {
                if (useStrategy) {
                    render();
                } else {
                    Graphics screen = getGraphics();
                    screen.drawImage(g.image, 0, 0, width, height, null);
                }
            } else {
                repaint();
            }
            if (frameCount != 0) {
                handleMethods("post");
            }
            frameRateLastNanos = now;
            frameCount++;
        }
    }

    public boolean canDraw() {
        return g != null && (looping || redraw);
    }

    synchronized public void redraw() {
        if (!looping) {
            redraw = true;
        }
    }

    synchronized public void loop() {
        if (!looping) {
            looping = true;
        }
    }

    synchronized public void noLoop() {
        if (looping) {
            looping = false;
        }
    }

    public void addListeners(Component comp) {
        comp.addMouseListener(this);
        comp.addMouseWheelListener(this);
        comp.addMouseMotionListener(this);
        comp.addKeyListener(this);
        comp.addFocusListener(this);
    }

    public void removeListeners(Component comp) {
        comp.removeMouseListener(this);
        comp.removeMouseWheelListener(this);
        comp.removeMouseMotionListener(this);
        comp.removeKeyListener(this);
        comp.removeFocusListener(this);
    }

    public void updateListeners(Component comp) {
        removeListeners(comp);
        addListeners(comp);
    }

    class InternalEventQueue {

        protected Event[] queue = new Event[10];

        protected int offset;

        protected int count;

        synchronized void add(Event e) {
            if (count == queue.length) {
                queue = (Event[]) expand(queue);
            }
            queue[count++] = e;
        }

        synchronized Event remove() {
            if (offset == count) {
                throw new RuntimeException("Nothing left on the event queue.");
            }
            Event outgoing = queue[offset++];
            if (offset == count) {
                offset = 0;
                count = 0;
            }
            return outgoing;
        }

        synchronized boolean available() {
            return count != 0;
        }
    }

    InternalEventQueue eventQueue = new InternalEventQueue();

    public void postEvent(processing.event.Event pe) {
        eventQueue.add(pe);
        if (!looping) {
            dequeueEvents();
        }
    }

    protected void dequeueEvents() {
        while (eventQueue.available()) {
            Event e = eventQueue.remove();
            switch(e.getFlavor()) {
                case Event.MOUSE:
                    handleMouseEvent((MouseEvent) e);
                    break;
                case Event.KEY:
                    handleKeyEvent((KeyEvent) e);
                    break;
            }
        }
    }

    protected void handleMouseEvent(MouseEvent event) {
        if (event.getAction() == MouseEvent.DRAG || event.getAction() == MouseEvent.MOVE) {
            pmouseX = emouseX;
            pmouseY = emouseY;
            mouseX = event.getX();
            mouseY = event.getY();
        }
        mouseButton = event.getButton();
        if (mouseEventMethods != null) {
            if (event.getNative() != null) {
                mouseEventMethods.handle(new Object[] { event.getNative() });
            }
        }
        if (firstMouse) {
            pmouseX = mouseX;
            pmouseY = mouseY;
            dmouseX = mouseX;
            dmouseY = mouseY;
            firstMouse = false;
        }
        mouseEvent = event;
        switch(event.getAction()) {
            case MouseEvent.PRESS:
                mousePressed = true;
                break;
            case MouseEvent.RELEASE:
                mousePressed = false;
                break;
        }
        handleMethods("mouseEvent", new Object[] { event });
        switch(event.getAction()) {
            case MouseEvent.PRESS:
                mousePressed(event);
                break;
            case MouseEvent.RELEASE:
                mouseReleased(event);
                break;
            case MouseEvent.CLICK:
                mouseClicked(event);
                break;
            case MouseEvent.DRAG:
                mouseDragged(event);
                break;
            case MouseEvent.MOVE:
                mouseMoved(event);
                break;
            case MouseEvent.ENTER:
                mouseEntered(event);
                break;
            case MouseEvent.EXIT:
                mouseExited(event);
                break;
            case MouseEvent.WHEEL:
                mouseWheel(event);
                break;
        }
        if ((event.getAction() == MouseEvent.DRAG) || (event.getAction() == MouseEvent.MOVE)) {
            emouseX = mouseX;
            emouseY = mouseY;
        }
    }

    static protected Method preciseWheelMethod;

    static {
        try {
            preciseWheelMethod = MouseWheelEvent.class.getMethod("getPreciseWheelRotation", new Class[] {});
        } catch (Exception e) {
        }
    }

    protected void nativeMouseEvent(java.awt.event.MouseEvent nativeEvent) {
        float peAmount = nativeEvent.getClickCount();
        int peAction = 0;
        switch(nativeEvent.getID()) {
            case java.awt.event.MouseEvent.MOUSE_PRESSED:
                peAction = MouseEvent.PRESS;
                break;
            case java.awt.event.MouseEvent.MOUSE_RELEASED:
                peAction = MouseEvent.RELEASE;
                break;
            case java.awt.event.MouseEvent.MOUSE_CLICKED:
                peAction = MouseEvent.CLICK;
                break;
            case java.awt.event.MouseEvent.MOUSE_DRAGGED:
                peAction = MouseEvent.DRAG;
                break;
            case java.awt.event.MouseEvent.MOUSE_MOVED:
                peAction = MouseEvent.MOVE;
                break;
            case java.awt.event.MouseEvent.MOUSE_ENTERED:
                peAction = MouseEvent.ENTER;
                break;
            case java.awt.event.MouseEvent.MOUSE_EXITED:
                peAction = MouseEvent.EXIT;
                break;
            case java.awt.event.MouseWheelEvent.WHEEL_UNIT_SCROLL:
                peAction = MouseEvent.WHEEL;
                if (preciseWheelMethod != null) {
                    try {
                        peAmount = ((Double) preciseWheelMethod.invoke(nativeEvent, (Object[]) null)).floatValue();
                    } catch (Exception e) {
                        preciseWheelMethod = null;
                    }
                }
                if (preciseWheelMethod == null) {
                    peAmount = ((MouseWheelEvent) nativeEvent).getWheelRotation();
                }
                break;
        }
        int modifiers = nativeEvent.getModifiers();
        int peModifiers = modifiers & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK | InputEvent.META_MASK | InputEvent.ALT_MASK);
        int peButton = 0;
        if ((modifiers & InputEvent.BUTTON1_MASK) != 0) {
            peButton = LEFT;
        } else if ((modifiers & InputEvent.BUTTON2_MASK) != 0) {
            peButton = CENTER;
        } else if ((modifiers & InputEvent.BUTTON3_MASK) != 0) {
            peButton = RIGHT;
        }
        if (platform == MACOSX) {
            if ((modifiers & InputEvent.CTRL_MASK) != 0) {
                peButton = RIGHT;
            }
        }
        postEvent(new MouseEvent(nativeEvent, nativeEvent.getWhen(), peAction, peModifiers, nativeEvent.getX(), nativeEvent.getY(), peButton, peAmount));
    }

    public void mousePressed(java.awt.event.MouseEvent e) {
        nativeMouseEvent(e);
    }

    public void mouseReleased(java.awt.event.MouseEvent e) {
        nativeMouseEvent(e);
    }

    public void mouseClicked(java.awt.event.MouseEvent e) {
        nativeMouseEvent(e);
    }

    public void mouseEntered(java.awt.event.MouseEvent e) {
        nativeMouseEvent(e);
    }

    public void mouseExited(java.awt.event.MouseEvent e) {
        nativeMouseEvent(e);
    }

    public void mouseDragged(java.awt.event.MouseEvent e) {
        nativeMouseEvent(e);
    }

    public void mouseMoved(java.awt.event.MouseEvent e) {
        nativeMouseEvent(e);
    }

    public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
        nativeMouseEvent(e);
    }

    public void mousePressed() {
    }

    public void mousePressed(MouseEvent event) {
        mousePressed();
    }

    public void mouseReleased() {
    }

    public void mouseReleased(MouseEvent event) {
        mouseReleased();
    }

    public void mouseClicked() {
    }

    public void mouseClicked(MouseEvent event) {
        mouseClicked();
    }

    public void mouseDragged() {
    }

    public void mouseDragged(MouseEvent event) {
        mouseDragged();
    }

    public void mouseMoved() {
    }

    public void mouseMoved(MouseEvent event) {
        mouseMoved();
    }

    public void mouseEntered() {
    }

    public void mouseEntered(MouseEvent event) {
        mouseEntered();
    }

    public void mouseExited() {
    }

    public void mouseExited(MouseEvent event) {
        mouseExited();
    }

    public void mouseWheel() {
    }

    public void mouseWheel(MouseEvent event) {
        mouseWheel();
    }

    protected void handleKeyEvent(KeyEvent event) {
        keyEvent = event;
        key = event.getKey();
        keyCode = event.getKeyCode();
        switch(event.getAction()) {
            case KeyEvent.PRESS:
                keyPressed = true;
                keyPressed(keyEvent);
                break;
            case KeyEvent.RELEASE:
                keyPressed = false;
                keyReleased(keyEvent);
                break;
            case KeyEvent.TYPE:
                keyTyped(keyEvent);
                break;
        }
        if (keyEventMethods != null) {
            keyEventMethods.handle(new Object[] { event.getNative() });
        }
        handleMethods("keyEvent", new Object[] { event });
        if (event.getAction() == KeyEvent.PRESS) {
            if (key == ESC) {
                exit();
            }
            if (external && event.getKeyCode() == 'W' && ((event.isMetaDown() && platform == MACOSX) || (event.isControlDown() && platform != MACOSX))) {
                exit();
            }
        }
    }

    protected void nativeKeyEvent(java.awt.event.KeyEvent event) {
        int peAction = 0;
        switch(event.getID()) {
            case java.awt.event.KeyEvent.KEY_PRESSED:
                peAction = KeyEvent.PRESS;
                break;
            case java.awt.event.KeyEvent.KEY_RELEASED:
                peAction = KeyEvent.RELEASE;
                break;
            case java.awt.event.KeyEvent.KEY_TYPED:
                peAction = KeyEvent.TYPE;
                break;
        }
        int peModifiers = event.getModifiers() & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK | InputEvent.META_MASK | InputEvent.ALT_MASK);
        postEvent(new KeyEvent(event, event.getWhen(), peAction, peModifiers, event.getKeyChar(), event.getKeyCode()));
    }

    public void keyPressed(java.awt.event.KeyEvent e) {
        nativeKeyEvent(e);
    }

    public void keyReleased(java.awt.event.KeyEvent e) {
        nativeKeyEvent(e);
    }

    public void keyTyped(java.awt.event.KeyEvent e) {
        nativeKeyEvent(e);
    }

    public void keyPressed() {
    }

    public void keyPressed(KeyEvent event) {
        keyPressed();
    }

    public void keyReleased() {
    }

    public void keyReleased(KeyEvent event) {
        keyReleased();
    }

    public void keyTyped() {
    }

    public void keyTyped(KeyEvent event) {
        keyTyped();
    }

    public void focusGained() {
    }

    public void focusGained(FocusEvent e) {
        focused = true;
        focusGained();
    }

    public void focusLost() {
    }

    public void focusLost(FocusEvent e) {
        focused = false;
        focusLost();
    }

    public int millis() {
        return (int) (System.currentTimeMillis() - millisOffset);
    }

    static public int second() {
        return Calendar.getInstance().get(Calendar.SECOND);
    }

    static public int minute() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

    static public int hour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    static public int day() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    static public int month() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    static public int year() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public void delay(int napTime) {
        try {
            Thread.sleep(napTime);
        } catch (InterruptedException e) {
        }
    }

    public void frameRate(float fps) {
        frameRateTarget = fps;
        frameRatePeriod = (long) (1000000000.0 / frameRateTarget);
        g.setFrameRate(fps);
    }

    public String param(String name) {
        if (online) {
            return getParameter(name);
        } else {
            System.err.println("param() only works inside a web browser");
        }
        return null;
    }

    public void status(String value) {
        if (online) {
            showStatus(value);
        } else {
            System.out.println(value);
        }
    }

    public void link(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                open(url);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void link(String url, String target) {
        link(url);
    }

    static public void open(String filename) {
        open(new String[] { filename });
    }

    static String openLauncher;

    static public Process open(String[] argv) {
        String[] params = null;
        if (platform == WINDOWS) {
            params = new String[] { "cmd", "/c" };
        } else if (platform == MACOSX) {
            params = new String[] { "open" };
        } else if (platform == LINUX) {
            if (openLauncher == null) {
                try {
                    Process p = Runtime.getRuntime().exec(new String[] { "gnome-open" });
                    p.waitFor();
                    openLauncher = "gnome-open";
                } catch (Exception e) {
                }
            }
            if (openLauncher == null) {
                try {
                    Process p = Runtime.getRuntime().exec(new String[] { "kde-open" });
                    p.waitFor();
                    openLauncher = "kde-open";
                } catch (Exception e) {
                }
            }
            if (openLauncher == null) {
                System.err.println("Could not find gnome-open or kde-open, " + "the open() command may not work.");
            }
            if (openLauncher != null) {
                params = new String[] { openLauncher };
            }
        }
        if (params != null) {
            if (params[0].equals(argv[0])) {
                return exec(argv);
            } else {
                params = concat(params, argv);
                return exec(params);
            }
        } else {
            return exec(argv);
        }
    }

    static public Process exec(String[] argv) {
        try {
            return Runtime.getRuntime().exec(argv);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not open " + join(argv, ' '));
        }
    }

    public void die(String what) {
        dispose();
        throw new RuntimeException(what);
    }

    public void die(String what, Exception e) {
        if (e != null)
            e.printStackTrace();
        die(what);
    }

    public void exit() {
        if (thread == null) {
            exitActual();
        } else if (looping) {
            finished = true;
            exitCalled = true;
        } else if (!looping) {
            dispose();
            exitActual();
        }
    }

    void exitActual() {
        try {
            System.exit(0);
        } catch (SecurityException e) {
        }
    }

    public void dispose() {
        finished = true;
        if (thread != null) {
            thread = null;
            if (g != null) {
                g.dispose();
            }
            handleMethods("dispose");
        }
    }

    public void method(String name) {
        try {
            Method method = getClass().getMethod(name, new Class[] {});
            method.invoke(this, new Object[] {});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.getTargetException().printStackTrace();
        } catch (NoSuchMethodException nsme) {
            System.err.println("There is no public " + name + "() method " + "in the class " + getClass().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void thread(final String name) {
        Thread later = new Thread() {

            @Override
            public void run() {
                method(name);
            }
        };
        later.start();
    }

    public void save(String filename) {
        g.save(savePath(filename));
    }

    public void saveFrame() {
        try {
            g.save(savePath("screen-" + nf(frameCount, 4) + ".tif"));
        } catch (SecurityException se) {
            System.err.println("Can't use saveFrame() when running in a browser, " + "unless using a signed applet.");
        }
    }

    public void saveFrame(String filename) {
        try {
            g.save(savePath(insertFrame(filename)));
        } catch (SecurityException se) {
            System.err.println("Can't use saveFrame() when running in a browser, " + "unless using a signed applet.");
        }
    }

    public String insertFrame(String what) {
        int first = what.indexOf('#');
        int last = what.lastIndexOf('#');
        if ((first != -1) && (last - first > 0)) {
            String prefix = what.substring(0, first);
            int count = last - first + 1;
            String suffix = what.substring(last + 1);
            return prefix + nf(frameCount, count) + suffix;
        }
        return what;
    }

    int cursorType = ARROW;

    boolean cursorVisible = true;

    Cursor invisibleCursor;

    public void cursor(int kind) {
        setCursor(Cursor.getPredefinedCursor(kind));
        cursorVisible = true;
        this.cursorType = kind;
    }

    public void cursor(PImage img) {
        cursor(img, img.width / 2, img.height / 2);
    }

    public void cursor(PImage img, int x, int y) {
        Image jimage = createImage(new MemoryImageSource(img.width, img.height, img.pixels, 0, img.width));
        Point hotspot = new Point(x, y);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Cursor cursor = tk.createCustomCursor(jimage, hotspot, "Custom Cursor");
        setCursor(cursor);
        cursorVisible = true;
    }

    public void cursor() {
        if (!cursorVisible) {
            cursorVisible = true;
            setCursor(Cursor.getPredefinedCursor(cursorType));
        }
    }

    public void noCursor() {
        if (invisibleCursor == null) {
            BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            invisibleCursor = getToolkit().createCustomCursor(cursorImg, new Point(8, 8), "blank");
        }
        setCursor(invisibleCursor);
        cursorVisible = false;
    }

    static public void print(byte what) {
        System.out.print(what);
        System.out.flush();
    }

    static public void print(boolean what) {
        System.out.print(what);
        System.out.flush();
    }

    static public void print(char what) {
        System.out.print(what);
        System.out.flush();
    }

    static public void print(int what) {
        System.out.print(what);
        System.out.flush();
    }

    static public void print(long what) {
        System.out.print(what);
        System.out.flush();
    }

    static public void print(float what) {
        System.out.print(what);
        System.out.flush();
    }

    static public void print(double what) {
        System.out.print(what);
        System.out.flush();
    }

    static public void print(String what) {
        System.out.print(what);
        System.out.flush();
    }

    static public void print(Object what) {
        if (what == null) {
            System.out.print("null");
        } else {
            System.out.println(what.toString());
        }
    }

    static public void println() {
        System.out.println();
    }

    static public void println(byte what) {
        System.out.println(what);
        System.out.flush();
    }

    static public void println(boolean what) {
        System.out.println(what);
        System.out.flush();
    }

    static public void println(char what) {
        System.out.println(what);
        System.out.flush();
    }

    static public void println(int what) {
        System.out.println(what);
        System.out.flush();
    }

    static public void println(long what) {
        System.out.println(what);
        System.out.flush();
    }

    static public void println(float what) {
        System.out.println(what);
        System.out.flush();
    }

    static public void println(double what) {
        System.out.println(what);
        System.out.flush();
    }

    static public void println(String what) {
        System.out.println(what);
        System.out.flush();
    }

    static public void println(Object what) {
        if (what == null) {
            System.out.println("null");
        } else {
            String name = what.getClass().getName();
            if (name.charAt(0) == '[') {
                switch(name.charAt(1)) {
                    case '[':
                        System.out.println(what);
                        break;
                    case 'L':
                        Object[] poo = (Object[]) what;
                        for (int i = 0; i < poo.length; i++) {
                            if (poo[i] instanceof String) {
                                System.out.println("[" + i + "] \"" + poo[i] + "\"");
                            } else {
                                System.out.println("[" + i + "] " + poo[i]);
                            }
                        }
                        break;
                    case 'Z':
                        boolean[] zz = (boolean[]) what;
                        for (int i = 0; i < zz.length; i++) {
                            System.out.println("[" + i + "] " + zz[i]);
                        }
                        break;
                    case 'B':
                        byte[] bb = (byte[]) what;
                        for (int i = 0; i < bb.length; i++) {
                            System.out.println("[" + i + "] " + bb[i]);
                        }
                        break;
                    case 'C':
                        char[] cc = (char[]) what;
                        for (int i = 0; i < cc.length; i++) {
                            System.out.println("[" + i + "] '" + cc[i] + "'");
                        }
                        break;
                    case 'I':
                        int[] ii = (int[]) what;
                        for (int i = 0; i < ii.length; i++) {
                            System.out.println("[" + i + "] " + ii[i]);
                        }
                        break;
                    case 'J':
                        long[] jj = (long[]) what;
                        for (int i = 0; i < jj.length; i++) {
                            System.out.println("[" + i + "] " + jj[i]);
                        }
                        break;
                    case 'F':
                        float[] ff = (float[]) what;
                        for (int i = 0; i < ff.length; i++) {
                            System.out.println("[" + i + "] " + ff[i]);
                        }
                        break;
                    case 'D':
                        double[] dd = (double[]) what;
                        for (int i = 0; i < dd.length; i++) {
                            System.out.println("[" + i + "] " + dd[i]);
                        }
                        break;
                    default:
                        System.out.println(what);
                }
            } else {
                System.out.println(what);
            }
        }
    }

    static public void debug(String msg) {
        if (DEBUG)
            println(msg);
    }

    static public final float abs(float n) {
        return (n < 0) ? -n : n;
    }

    static public final int abs(int n) {
        return (n < 0) ? -n : n;
    }

    static public final float sq(float n) {
        return n * n;
    }

    static public final float sqrt(float n) {
        return (float) Math.sqrt(n);
    }

    static public final float log(float n) {
        return (float) Math.log(n);
    }

    static public final float exp(float n) {
        return (float) Math.exp(n);
    }

    static public final float pow(float n, float e) {
        return (float) Math.pow(n, e);
    }

    static public final int max(int a, int b) {
        return (a > b) ? a : b;
    }

    static public final float max(float a, float b) {
        return (a > b) ? a : b;
    }

    static public final int max(int a, int b, int c) {
        return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
    }

    static public final float max(float a, float b, float c) {
        return (a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c);
    }

    static public final int max(int[] list) {
        if (list.length == 0) {
            throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX);
        }
        int max = list[0];
        for (int i = 1; i < list.length; i++) {
            if (list[i] > max)
                max = list[i];
        }
        return max;
    }

    static public final float max(float[] list) {
        if (list.length == 0) {
            throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX);
        }
        float max = list[0];
        for (int i = 1; i < list.length; i++) {
            if (list[i] > max)
                max = list[i];
        }
        return max;
    }

    static public final int min(int a, int b) {
        return (a < b) ? a : b;
    }

    static public final float min(float a, float b) {
        return (a < b) ? a : b;
    }

    static public final int min(int a, int b, int c) {
        return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
    }

    static public final float min(float a, float b, float c) {
        return (a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c);
    }

    static public final int min(int[] list) {
        if (list.length == 0) {
            throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX);
        }
        int min = list[0];
        for (int i = 1; i < list.length; i++) {
            if (list[i] < min)
                min = list[i];
        }
        return min;
    }

    static public final float min(float[] list) {
        if (list.length == 0) {
            throw new ArrayIndexOutOfBoundsException(ERROR_MIN_MAX);
        }
        float min = list[0];
        for (int i = 1; i < list.length; i++) {
            if (list[i] < min)
                min = list[i];
        }
        return min;
    }

    static public final int constrain(int amt, int low, int high) {
        return (amt < low) ? low : ((amt > high) ? high : amt);
    }

    static public final float constrain(float amt, float low, float high) {
        return (amt < low) ? low : ((amt > high) ? high : amt);
    }

    static public final float sin(float angle) {
        return (float) Math.sin(angle);
    }

    static public final float cos(float angle) {
        return (float) Math.cos(angle);
    }

    static public final float tan(float angle) {
        return (float) Math.tan(angle);
    }

    static public final float asin(float value) {
        return (float) Math.asin(value);
    }

    static public final float acos(float value) {
        return (float) Math.acos(value);
    }

    static public final float atan(float value) {
        return (float) Math.atan(value);
    }

    static public final float atan2(float y, float x) {
        return (float) Math.atan2(y, x);
    }

    static public final float degrees(float radians) {
        return radians * RAD_TO_DEG;
    }

    static public final float radians(float degrees) {
        return degrees * DEG_TO_RAD;
    }

    static public final int ceil(float n) {
        return (int) Math.ceil(n);
    }

    static public final int floor(float n) {
        return (int) Math.floor(n);
    }

    static public final int round(float n) {
        return Math.round(n);
    }

    static public final float mag(float a, float b) {
        return (float) Math.sqrt(a * a + b * b);
    }

    static public final float mag(float a, float b, float c) {
        return (float) Math.sqrt(a * a + b * b + c * c);
    }

    static public final float dist(float x1, float y1, float x2, float y2) {
        return sqrt(sq(x2 - x1) + sq(y2 - y1));
    }

    static public final float dist(float x1, float y1, float z1, float x2, float y2, float z2) {
        return sqrt(sq(x2 - x1) + sq(y2 - y1) + sq(z2 - z1));
    }

    static public final float lerp(float start, float stop, float amt) {
        return start + (stop - start) * amt;
    }

    static public final float norm(float value, float start, float stop) {
        return (value - start) / (stop - start);
    }

    static public final float map(float value, float start1, float stop1, float start2, float stop2) {
        return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    }

    Random internalRandom;

    public final float random(float high) {
        if (high == 0 || high != high) {
            return 0;
        }
        if (internalRandom == null) {
            internalRandom = new Random();
        }
        float value = 0;
        do {
            value = internalRandom.nextFloat() * high;
        } while (value == high);
        return value;
    }

    public final float randomGaussian() {
        if (internalRandom == null) {
            internalRandom = new Random();
        }
        return (float) internalRandom.nextGaussian();
    }

    public final float random(float low, float high) {
        if (low >= high)
            return low;
        float diff = high - low;
        return random(diff) + low;
    }

    public final void randomSeed(long seed) {
        if (internalRandom == null) {
            internalRandom = new Random();
        }
        internalRandom.setSeed(seed);
    }

    static final int PERLIN_YWRAPB = 4;

    static final int PERLIN_YWRAP = 1 << PERLIN_YWRAPB;

    static final int PERLIN_ZWRAPB = 8;

    static final int PERLIN_ZWRAP = 1 << PERLIN_ZWRAPB;

    static final int PERLIN_SIZE = 4095;

    int perlin_octaves = 4;

    float perlin_amp_falloff = 0.5f;

    int perlin_TWOPI, perlin_PI;

    float[] perlin_cosTable;

    float[] perlin;

    Random perlinRandom;

    public float noise(float x) {
        return noise(x, 0f, 0f);
    }

    public float noise(float x, float y) {
        return noise(x, y, 0f);
    }

    public float noise(float x, float y, float z) {
        if (perlin == null) {
            if (perlinRandom == null) {
                perlinRandom = new Random();
            }
            perlin = new float[PERLIN_SIZE + 1];
            for (int i = 0; i < PERLIN_SIZE + 1; i++) {
                perlin[i] = perlinRandom.nextFloat();
            }
            perlin_cosTable = PGraphics.cosLUT;
            perlin_TWOPI = perlin_PI = PGraphics.SINCOS_LENGTH;
            perlin_PI >>= 1;
        }
        if (x < 0)
            x = -x;
        if (y < 0)
            y = -y;
        if (z < 0)
            z = -z;
        int xi = (int) x, yi = (int) y, zi = (int) z;
        float xf = x - xi;
        float yf = y - yi;
        float zf = z - zi;
        float rxf, ryf;
        float r = 0;
        float ampl = 0.5f;
        float n1, n2, n3;
        for (int i = 0; i < perlin_octaves; i++) {
            int of = xi + (yi << PERLIN_YWRAPB) + (zi << PERLIN_ZWRAPB);
            rxf = noise_fsc(xf);
            ryf = noise_fsc(yf);
            n1 = perlin[of & PERLIN_SIZE];
            n1 += rxf * (perlin[(of + 1) & PERLIN_SIZE] - n1);
            n2 = perlin[(of + PERLIN_YWRAP) & PERLIN_SIZE];
            n2 += rxf * (perlin[(of + PERLIN_YWRAP + 1) & PERLIN_SIZE] - n2);
            n1 += ryf * (n2 - n1);
            of += PERLIN_ZWRAP;
            n2 = perlin[of & PERLIN_SIZE];
            n2 += rxf * (perlin[(of + 1) & PERLIN_SIZE] - n2);
            n3 = perlin[(of + PERLIN_YWRAP) & PERLIN_SIZE];
            n3 += rxf * (perlin[(of + PERLIN_YWRAP + 1) & PERLIN_SIZE] - n3);
            n2 += ryf * (n3 - n2);
            n1 += noise_fsc(zf) * (n2 - n1);
            r += n1 * ampl;
            ampl *= perlin_amp_falloff;
            xi <<= 1;
            xf *= 2;
            yi <<= 1;
            yf *= 2;
            zi <<= 1;
            zf *= 2;
            if (xf >= 1.0f) {
                xi++;
                xf--;
            }
            if (yf >= 1.0f) {
                yi++;
                yf--;
            }
            if (zf >= 1.0f) {
                zi++;
                zf--;
            }
        }
        return r;
    }

    private float noise_fsc(float i) {
        return 0.5f * (1.0f - perlin_cosTable[(int) (i * perlin_PI) % perlin_TWOPI]);
    }

    public void noiseDetail(int lod) {
        if (lod > 0)
            perlin_octaves = lod;
    }

    public void noiseDetail(int lod, float falloff) {
        if (lod > 0)
            perlin_octaves = lod;
        if (falloff > 0)
            perlin_amp_falloff = falloff;
    }

    public void noiseSeed(long seed) {
        if (perlinRandom == null)
            perlinRandom = new Random();
        perlinRandom.setSeed(seed);
        perlin = null;
    }

    protected String[] loadImageFormats;

    public PImage loadImage(String filename) {
        return loadImage(filename, null);
    }

    public PImage loadImage(String filename, String extension) {
        if (extension == null) {
            String lower = filename.toLowerCase();
            int dot = filename.lastIndexOf('.');
            if (dot == -1) {
                extension = "unknown";
            }
            extension = lower.substring(dot + 1);
            int question = extension.indexOf('?');
            if (question != -1) {
                extension = extension.substring(0, question);
            }
        }
        extension = extension.toLowerCase();
        if (extension.equals("tga")) {
            try {
                PImage image = loadImageTGA(filename);
                return image;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        if (extension.equals("tif") || extension.equals("tiff")) {
            byte[] bytes = loadBytes(filename);
            PImage image = (bytes == null) ? null : PImage.loadTIFF(bytes);
            return image;
        }
        try {
            if (extension.equals("jpg") || extension.equals("jpeg") || extension.equals("gif") || extension.equals("png") || extension.equals("unknown")) {
                byte[] bytes = loadBytes(filename);
                if (bytes == null) {
                    return null;
                } else {
                    Image awtImage = Toolkit.getDefaultToolkit().createImage(bytes);
                    PImage image = loadImageMT(awtImage);
                    if (image.width == -1) {
                        System.err.println("The file " + filename + " contains bad image data, or may not be an image.");
                    }
                    if (extension.equals("gif") || extension.equals("png")) {
                        image.checkAlpha();
                    }
                    return image;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (loadImageFormats == null) {
            loadImageFormats = ImageIO.getReaderFormatNames();
        }
        if (loadImageFormats != null) {
            for (int i = 0; i < loadImageFormats.length; i++) {
                if (extension.equals(loadImageFormats[i])) {
                    return loadImageIO(filename);
                }
            }
        }
        System.err.println("Could not find a method to load " + filename);
        return null;
    }

    public PImage requestImage(String filename) {
        return requestImage(filename, null);
    }

    public PImage requestImage(String filename, String extension) {
        PImage vessel = createImage(0, 0, ARGB);
        AsyncImageLoader ail = new AsyncImageLoader(filename, extension, vessel);
        ail.start();
        return vessel;
    }

    public int requestImageMax = 4;

    volatile int requestImageCount;

    class AsyncImageLoader extends Thread {

        String filename;

        String extension;

        PImage vessel;

        public AsyncImageLoader(String filename, String extension, PImage vessel) {
            this.filename = filename;
            this.extension = extension;
            this.vessel = vessel;
        }

        @Override
        public void run() {
            while (requestImageCount == requestImageMax) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
            requestImageCount++;
            PImage actual = loadImage(filename, extension);
            if (actual == null) {
                vessel.width = -1;
                vessel.height = -1;
            } else {
                vessel.width = actual.width;
                vessel.height = actual.height;
                vessel.format = actual.format;
                vessel.pixels = actual.pixels;
            }
            requestImageCount--;
        }
    }

    protected PImage loadImageMT(Image awtImage) {
        MediaTracker tracker = new MediaTracker(this);
        tracker.addImage(awtImage, 0);
        try {
            tracker.waitForAll();
        } catch (InterruptedException e) {
        }
        PImage image = new PImage(awtImage);
        image.parent = this;
        return image;
    }

    protected PImage loadImageIO(String filename) {
        InputStream stream = createInput(filename);
        if (stream == null) {
            System.err.println("The image " + filename + " could not be found.");
            return null;
        }
        try {
            BufferedImage bi = ImageIO.read(stream);
            PImage outgoing = new PImage(bi.getWidth(), bi.getHeight());
            outgoing.parent = this;
            bi.getRGB(0, 0, outgoing.width, outgoing.height, outgoing.pixels, 0, outgoing.width);
            outgoing.checkAlpha();
            return outgoing;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected PImage loadImageTGA(String filename) throws IOException {
        InputStream is = createInput(filename);
        if (is == null)
            return null;
        byte[] header = new byte[18];
        int offset = 0;
        do {
            int count = is.read(header, offset, header.length - offset);
            if (count == -1)
                return null;
            offset += count;
        } while (offset < 18);
        int format = 0;
        if (((header[2] == 3) || (header[2] == 11)) && (header[16] == 8) && ((header[17] == 0x8) || (header[17] == 0x28))) {
            format = ALPHA;
        } else if (((header[2] == 2) || (header[2] == 10)) && (header[16] == 24) && ((header[17] == 0x20) || (header[17] == 0))) {
            format = RGB;
        } else if (((header[2] == 2) || (header[2] == 10)) && (header[16] == 32) && ((header[17] == 0x8) || (header[17] == 0x28))) {
            format = ARGB;
        }
        if (format == 0) {
            System.err.println("Unknown .tga file format for " + filename);
            return null;
        }
        int w = ((header[13] & 0xff) << 8) + (header[12] & 0xff);
        int h = ((header[15] & 0xff) << 8) + (header[14] & 0xff);
        PImage outgoing = createImage(w, h, format);
        boolean reversed = (header[17] & 0x20) != 0;
        if ((header[2] == 2) || (header[2] == 3)) {
            if (reversed) {
                int index = (h - 1) * w;
                switch(format) {
                    case ALPHA:
                        for (int y = h - 1; y >= 0; y--) {
                            for (int x = 0; x < w; x++) {
                                outgoing.pixels[index + x] = is.read();
                            }
                            index -= w;
                        }
                        break;
                    case RGB:
                        for (int y = h - 1; y >= 0; y--) {
                            for (int x = 0; x < w; x++) {
                                outgoing.pixels[index + x] = is.read() | (is.read() << 8) | (is.read() << 16) | 0xff000000;
                            }
                            index -= w;
                        }
                        break;
                    case ARGB:
                        for (int y = h - 1; y >= 0; y--) {
                            for (int x = 0; x < w; x++) {
                                outgoing.pixels[index + x] = is.read() | (is.read() << 8) | (is.read() << 16) | (is.read() << 24);
                            }
                            index -= w;
                        }
                }
            } else {
                int count = w * h;
                switch(format) {
                    case ALPHA:
                        for (int i = 0; i < count; i++) {
                            outgoing.pixels[i] = is.read();
                        }
                        break;
                    case RGB:
                        for (int i = 0; i < count; i++) {
                            outgoing.pixels[i] = is.read() | (is.read() << 8) | (is.read() << 16) | 0xff000000;
                        }
                        break;
                    case ARGB:
                        for (int i = 0; i < count; i++) {
                            outgoing.pixels[i] = is.read() | (is.read() << 8) | (is.read() << 16) | (is.read() << 24);
                        }
                        break;
                }
            }
        } else {
            int index = 0;
            int[] px = outgoing.pixels;
            while (index < px.length) {
                int num = is.read();
                boolean isRLE = (num & 0x80) != 0;
                if (isRLE) {
                    num -= 127;
                    int pixel = 0;
                    switch(format) {
                        case ALPHA:
                            pixel = is.read();
                            break;
                        case RGB:
                            pixel = 0xFF000000 | is.read() | (is.read() << 8) | (is.read() << 16);
                            break;
                        case ARGB:
                            pixel = is.read() | (is.read() << 8) | (is.read() << 16) | (is.read() << 24);
                            break;
                    }
                    for (int i = 0; i < num; i++) {
                        px[index++] = pixel;
                        if (index == px.length)
                            break;
                    }
                } else {
                    num += 1;
                    switch(format) {
                        case ALPHA:
                            for (int i = 0; i < num; i++) {
                                px[index++] = is.read();
                            }
                            break;
                        case RGB:
                            for (int i = 0; i < num; i++) {
                                px[index++] = 0xFF000000 | is.read() | (is.read() << 8) | (is.read() << 16);
                            }
                            break;
                        case ARGB:
                            for (int i = 0; i < num; i++) {
                                px[index++] = is.read() | (is.read() << 8) | (is.read() << 16) | (is.read() << 24);
                            }
                            break;
                    }
                }
            }
            if (!reversed) {
                int[] temp = new int[w];
                for (int y = 0; y < h / 2; y++) {
                    int z = (h - 1) - y;
                    System.arraycopy(px, y * w, temp, 0, w);
                    System.arraycopy(px, z * w, px, y * w, w);
                    System.arraycopy(temp, 0, px, z * w, w);
                }
            }
        }
        return outgoing;
    }

    public XML createXML(String name) {
        try {
            return new XML(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public XML loadXML(String filename) {
        return loadXML(filename, null);
    }

    public XML loadXML(String filename, String options) {
        try {
            return new XML(createInput(filename), options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public XML parseXML(String xmlString) {
        return parseXML(xmlString, null);
    }

    public XML parseXML(String xmlString, String options) {
        try {
            return XML.parse(xmlString, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean saveXML(XML xml, String filename) {
        return saveXML(xml, filename, null);
    }

    public boolean saveXML(XML xml, String filename, String options) {
        return xml.save(saveFile(filename), options);
    }

    public JSONObject loadJSONObject(String filename) {
        JSONTokener tokener = new JSONTokener(createReader(filename));
        return new JSONObject(tokener);
    }

    public Table createTable() {
        return new Table();
    }

    public Table loadTable(String filename) {
        return loadTable(filename, null);
    }

    public Table loadTable(String filename, String options) {
        try {
            String ext = checkExtension(filename);
            if (ext != null) {
                if (ext.equals("csv") || ext.equals("tsv") || ext.equals("bin")) {
                    if (options == null) {
                        options = ext;
                    } else {
                        options = ext + "," + options;
                    }
                }
            }
            return new Table(createInput(filename), options);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean saveTable(Table table, String filename) {
        return saveTable(table, filename, null);
    }

    public boolean saveTable(Table table, String filename, String options) {
        String ext = checkExtension(filename);
        if (ext != null) {
            if (ext.equals("csv") || ext.equals("tsv") || ext.equals("bin") || ext.equals("html")) {
                if (options == null) {
                    options = ext;
                } else {
                    options = ext + "," + options;
                }
            }
        }
        File outputFile = saveFile(filename);
        return table.save(createOutput(outputFile), options);
    }

    protected String checkExtension(String filename) {
        if (filename.toLowerCase().endsWith(".gz")) {
            filename = filename.substring(0, filename.length() - 3);
        }
        int index = filename.lastIndexOf('.');
        if (index == -1) {
            return null;
        }
        return filename.substring(index + 1).toLowerCase();
    }

    public PFont loadFont(String filename) {
        try {
            InputStream input = createInput(filename);
            return new PFont(input);
        } catch (Exception e) {
            die("Could not load font " + filename + ". " + "Make sure that the font has been copied " + "to the data folder of your sketch.", e);
        }
        return null;
    }

    protected PFont createDefaultFont(float size) {
        return createFont("Lucida Sans", size, true, null);
    }

    public PFont createFont(String name, float size) {
        return createFont(name, size, true, null);
    }

    public PFont createFont(String name, float size, boolean smooth) {
        return createFont(name, size, smooth, null);
    }

    public PFont createFont(String name, float size, boolean smooth, char[] charset) {
        String lowerName = name.toLowerCase();
        Font baseFont = null;
        try {
            InputStream stream = null;
            if (lowerName.endsWith(".otf") || lowerName.endsWith(".ttf")) {
                stream = createInput(name);
                if (stream == null) {
                    System.err.println("The font \"" + name + "\" " + "is missing or inaccessible, make sure " + "the URL is valid or that the file has been " + "added to your sketch and is readable.");
                    return null;
                }
                baseFont = Font.createFont(Font.TRUETYPE_FONT, createInput(name));
            } else {
                baseFont = PFont.findFont(name);
            }
            return new PFont(baseFont.deriveFont(size), smooth, charset, stream != null);
        } catch (Exception e) {
            System.err.println("Problem createFont(" + name + ")");
            e.printStackTrace();
            return null;
        }
    }

    private Frame selectFrame;

    private Frame selectFrame() {
        if (frame != null) {
            selectFrame = frame;
        } else if (selectFrame == null) {
            Component comp = getParent();
            while (comp != null) {
                if (comp instanceof Frame) {
                    selectFrame = (Frame) comp;
                    break;
                }
                comp = comp.getParent();
            }
            if (selectFrame == null) {
                selectFrame = new Frame();
            }
        }
        return selectFrame;
    }

    public void selectInput(String prompt, String callback) {
        selectInput(prompt, callback, null);
    }

    public void selectInput(String prompt, String callback, File file) {
        selectInput(prompt, callback, file, this);
    }

    public void selectInput(String prompt, String callback, File file, Object callbackObject) {
        selectInput(prompt, callback, file, callbackObject, selectFrame());
    }

    static public void selectInput(String prompt, String callbackMethod, File file, Object callbackObject, Frame parent) {
        selectImpl(prompt, callbackMethod, file, callbackObject, parent, FileDialog.LOAD);
    }

    public void selectOutput(String prompt, String callback) {
        selectOutput(prompt, callback, null);
    }

    public void selectOutput(String prompt, String callback, File file) {
        selectOutput(prompt, callback, file, this);
    }

    public void selectOutput(String prompt, String callback, File file, Object callbackObject) {
        selectOutput(prompt, callback, file, callbackObject, selectFrame());
    }

    static public void selectOutput(String prompt, String callbackMethod, File file, Object callbackObject, Frame parent) {
        selectImpl(prompt, callbackMethod, file, callbackObject, parent, FileDialog.SAVE);
    }

    static protected void selectImpl(final String prompt, final String callbackMethod, final File defaultSelection, final Object callbackObject, final Frame parentFrame, final int mode) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                File selectedFile = null;
                if (useNativeSelect) {
                    FileDialog dialog = new FileDialog(parentFrame, prompt, mode);
                    if (defaultSelection != null) {
                        dialog.setDirectory(defaultSelection.getParent());
                        dialog.setFile(defaultSelection.getName());
                    }
                    dialog.setVisible(true);
                    String directory = dialog.getDirectory();
                    String filename = dialog.getFile();
                    if (filename != null) {
                        selectedFile = new File(directory, filename);
                    }
                } else {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle(prompt);
                    if (defaultSelection != null) {
                        chooser.setSelectedFile(defaultSelection);
                    }
                    int result = -1;
                    if (mode == FileDialog.SAVE) {
                        result = chooser.showSaveDialog(parentFrame);
                    } else if (mode == FileDialog.LOAD) {
                        result = chooser.showOpenDialog(parentFrame);
                    }
                    if (result == JFileChooser.APPROVE_OPTION) {
                        selectedFile = chooser.getSelectedFile();
                    }
                }
                selectCallback(selectedFile, callbackMethod, callbackObject);
            }
        });
    }

    public void selectFolder(String prompt, String callback) {
        selectFolder(prompt, callback, null);
    }

    public void selectFolder(String prompt, String callback, File file) {
        selectFolder(prompt, callback, file, this);
    }

    public void selectFolder(String prompt, String callback, File file, Object callbackObject) {
        selectFolder(prompt, callback, file, callbackObject, selectFrame());
    }

    static public void selectFolder(final String prompt, final String callbackMethod, final File defaultSelection, final Object callbackObject, final Frame parentFrame) {
        EventQueue.invokeLater(new Runnable() {

            public void run() {
                File selectedFile = null;
                if (platform == MACOSX && useNativeSelect != false) {
                    FileDialog fileDialog = new FileDialog(parentFrame, prompt, FileDialog.LOAD);
                    System.setProperty("apple.awt.fileDialogForDirectories", "true");
                    fileDialog.setVisible(true);
                    System.setProperty("apple.awt.fileDialogForDirectories", "false");
                    String filename = fileDialog.getFile();
                    if (filename != null) {
                        selectedFile = new File(fileDialog.getDirectory(), fileDialog.getFile());
                    }
                } else {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle(prompt);
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (defaultSelection != null) {
                        fileChooser.setSelectedFile(defaultSelection);
                    }
                    int result = fileChooser.showOpenDialog(parentFrame);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        selectedFile = fileChooser.getSelectedFile();
                    }
                }
                selectCallback(selectedFile, callbackMethod, callbackObject);
            }
        });
    }

    static private void selectCallback(File selectedFile, String callbackMethod, Object callbackObject) {
        try {
            Class<?> callbackClass = callbackObject.getClass();
            Method selectMethod = callbackClass.getMethod(callbackMethod, new Class[] { File.class });
            selectMethod.invoke(callbackObject, new Object[] { selectedFile });
        } catch (IllegalAccessException iae) {
            System.err.println(callbackMethod + "() must be public");
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
        } catch (NoSuchMethodException nsme) {
            System.err.println(callbackMethod + "() could not be found");
        }
    }

    public BufferedReader createReader(String filename) {
        try {
            InputStream is = createInput(filename);
            if (is == null) {
                System.err.println(filename + " does not exist or could not be read");
                return null;
            }
            return createReader(is);
        } catch (Exception e) {
            if (filename == null) {
                System.err.println("Filename passed to reader() was null");
            } else {
                System.err.println("Couldn't create a reader for " + filename);
            }
        }
        return null;
    }

    static public BufferedReader createReader(File file) {
        try {
            InputStream is = new FileInputStream(file);
            if (file.getName().toLowerCase().endsWith(".gz")) {
                is = new GZIPInputStream(is);
            }
            return createReader(is);
        } catch (Exception e) {
            if (file == null) {
                throw new RuntimeException("File passed to createReader() was null");
            } else {
                e.printStackTrace();
                throw new RuntimeException("Couldn't create a reader for " + file.getAbsolutePath());
            }
        }
    }

    static public BufferedReader createReader(InputStream input) {
        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return new BufferedReader(isr);
    }

    public PrintWriter createWriter(String filename) {
        return createWriter(saveFile(filename));
    }

    static public PrintWriter createWriter(File file) {
        try {
            createPath(file);
            OutputStream output = new FileOutputStream(file);
            if (file.getName().toLowerCase().endsWith(".gz")) {
                output = new GZIPOutputStream(output);
            }
            return createWriter(output);
        } catch (Exception e) {
            if (file == null) {
                throw new RuntimeException("File passed to createWriter() was null");
            } else {
                e.printStackTrace();
                throw new RuntimeException("Couldn't create a writer for " + file.getAbsolutePath());
            }
        }
    }

    static public PrintWriter createWriter(OutputStream output) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(output, 8192);
            OutputStreamWriter osw = new OutputStreamWriter(bos, "UTF-8");
            return new PrintWriter(osw);
        } catch (UnsupportedEncodingException e) {
        }
        return null;
    }

    public InputStream openStream(String filename) {
        return createInput(filename);
    }

    public InputStream createInput(String filename) {
        InputStream input = createInputRaw(filename);
        if ((input != null) && filename.toLowerCase().endsWith(".gz")) {
            try {
                return new GZIPInputStream(input);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return input;
    }

    public InputStream createInputRaw(String filename) {
        InputStream stream = null;
        if (filename == null)
            return null;
        if (filename.length() == 0) {
            return null;
        }
        if (filename.contains(":")) {
            try {
                URL url = new URL(filename);
                stream = url.openStream();
                return stream;
            } catch (MalformedURLException mfue) {
            } catch (FileNotFoundException fnfe) {
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        try {
            File file = new File(dataPath(filename));
            if (!file.exists()) {
                file = new File(sketchPath, filename);
            }
            if (file.isDirectory()) {
                return null;
            }
            if (file.exists()) {
                try {
                    String filePath = file.getCanonicalPath();
                    String filenameActual = new File(filePath).getName();
                    String filenameShort = new File(filename).getName();
                    if (!filenameActual.equals(filenameShort)) {
                        throw new RuntimeException("This file is named " + filenameActual + " not " + filename + ". Rename the file " + "or change your code.");
                    }
                } catch (IOException e) {
                }
            }
            stream = new FileInputStream(file);
            if (stream != null)
                return stream;
        } catch (IOException ioe) {
        } catch (SecurityException se) {
        }
        ClassLoader cl = getClass().getClassLoader();
        stream = cl.getResourceAsStream("data/" + filename);
        if (stream != null) {
            String cn = stream.getClass().getName();
            if (!cn.equals("sun.plugin.cache.EmptyInputStream")) {
                return stream;
            }
        }
        stream = cl.getResourceAsStream(filename);
        if (stream != null) {
            String cn = stream.getClass().getName();
            if (!cn.equals("sun.plugin.cache.EmptyInputStream")) {
                return stream;
            }
        }
        try {
            URL base = getDocumentBase();
            if (base != null) {
                URL url = new URL(base, filename);
                URLConnection conn = url.openConnection();
                return conn.getInputStream();
            }
        } catch (Exception e) {
        }
        try {
            URL base = getDocumentBase();
            if (base != null) {
                URL url = new URL(base, "data/" + filename);
                URLConnection conn = url.openConnection();
                return conn.getInputStream();
            }
        } catch (Exception e) {
        }
        try {
            try {
                try {
                    stream = new FileInputStream(dataPath(filename));
                    if (stream != null)
                        return stream;
                } catch (IOException e2) {
                }
                try {
                    stream = new FileInputStream(sketchPath(filename));
                    if (stream != null)
                        return stream;
                } catch (Exception e) {
                }
                try {
                    stream = new FileInputStream(filename);
                    if (stream != null)
                        return stream;
                } catch (IOException e1) {
                }
            } catch (SecurityException se) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static public InputStream createInput(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File passed to createInput() was null");
        }
        try {
            InputStream input = new FileInputStream(file);
            if (file.getName().toLowerCase().endsWith(".gz")) {
                return new GZIPInputStream(input);
            }
            return input;
        } catch (IOException e) {
            System.err.println("Could not createInput() for " + file);
            e.printStackTrace();
            return null;
        }
    }

    public byte[] loadBytes(String filename) {
        InputStream is = createInput(filename);
        if (is != null) {
            byte[] outgoing = loadBytes(is);
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return outgoing;
        }
        System.err.println("The file \"" + filename + "\" " + "is missing or inaccessible, make sure " + "the URL is valid or that the file has been " + "added to your sketch and is readable.");
        return null;
    }

    static public byte[] loadBytes(InputStream input) {
        try {
            BufferedInputStream bis = new BufferedInputStream(input);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int c = bis.read();
            while (c != -1) {
                out.write(c);
                c = bis.read();
            }
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static public byte[] loadBytes(File file) {
        InputStream is = createInput(file);
        return loadBytes(is);
    }

    static public String[] loadStrings(File file) {
        InputStream is = createInput(file);
        if (is != null) {
            String[] outgoing = loadStrings(is);
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return outgoing;
        }
        return null;
    }

    public String[] loadStrings(String filename) {
        InputStream is = createInput(filename);
        if (is != null)
            return loadStrings(is);
        System.err.println("The file \"" + filename + "\" " + "is missing or inaccessible, make sure " + "the URL is valid or that the file has been " + "added to your sketch and is readable.");
        return null;
    }

    static public String[] loadStrings(InputStream input) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
            return loadStrings(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static public String[] loadStrings(BufferedReader reader) {
        try {
            String[] lines = new String[100];
            int lineCount = 0;
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (lineCount == lines.length) {
                    String[] temp = new String[lineCount << 1];
                    System.arraycopy(lines, 0, temp, 0, lineCount);
                    lines = temp;
                }
                lines[lineCount++] = line;
            }
            reader.close();
            if (lineCount == lines.length) {
                return lines;
            }
            String[] output = new String[lineCount];
            System.arraycopy(lines, 0, output, 0, lineCount);
            return output;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public OutputStream createOutput(String filename) {
        return createOutput(saveFile(filename));
    }

    static public OutputStream createOutput(File file) {
        try {
            createPath(file);
            FileOutputStream fos = new FileOutputStream(file);
            if (file.getName().toLowerCase().endsWith(".gz")) {
                return new GZIPOutputStream(fos);
            }
            return fos;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean saveStream(String target, String source) {
        return saveStream(saveFile(target), source);
    }

    public boolean saveStream(File target, String source) {
        return saveStream(target, createInputRaw(source));
    }

    public boolean saveStream(String target, InputStream source) {
        return saveStream(saveFile(target), source);
    }

    static public boolean saveStream(File target, InputStream source) {
        File tempFile = null;
        try {
            File parentDir = target.getParentFile();
            createPath(target);
            tempFile = File.createTempFile(target.getName(), null, parentDir);
            FileOutputStream targetStream = new FileOutputStream(tempFile);
            saveStream(targetStream, source);
            targetStream.close();
            targetStream = null;
            if (target.exists()) {
                if (!target.delete()) {
                    System.err.println("Could not replace " + target.getAbsolutePath() + ".");
                }
            }
            if (!tempFile.renameTo(target)) {
                System.err.println("Could not rename temporary file " + tempFile.getAbsolutePath());
                return false;
            }
            return true;
        } catch (IOException e) {
            if (tempFile != null) {
                tempFile.delete();
            }
            e.printStackTrace();
            return false;
        }
    }

    static public void saveStream(OutputStream target, InputStream source) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(source, 16384);
        BufferedOutputStream bos = new BufferedOutputStream(target);
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = bis.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        bos.flush();
    }

    public void saveBytes(String filename, byte[] data) {
        saveBytes(saveFile(filename), data);
    }

    static public void saveBytes(File file, byte[] data) {
        File tempFile = null;
        try {
            File parentDir = file.getParentFile();
            tempFile = File.createTempFile(file.getName(), null, parentDir);
            OutputStream output = createOutput(tempFile);
            saveBytes(output, data);
            output.close();
            output = null;
            if (file.exists()) {
                if (!file.delete()) {
                    System.err.println("Could not replace " + file.getAbsolutePath());
                }
            }
            if (!tempFile.renameTo(file)) {
                System.err.println("Could not rename temporary file " + tempFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("error saving bytes to " + file);
            if (tempFile != null) {
                tempFile.delete();
            }
            e.printStackTrace();
        }
    }

    static public void saveBytes(OutputStream output, byte[] data) {
        try {
            output.write(data);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveStrings(String filename, String[] data) {
        saveStrings(saveFile(filename), data);
    }

    static public void saveStrings(File file, String[] data) {
        saveStrings(createOutput(file), data);
    }

    static public void saveStrings(OutputStream output, String[] data) {
        PrintWriter writer = createWriter(output);
        for (int i = 0; i < data.length; i++) {
            writer.println(data[i]);
        }
        writer.flush();
        writer.close();
    }

    public String sketchPath(String where) {
        if (sketchPath == null) {
            return where;
        }
        try {
            if (new File(where).isAbsolute())
                return where;
        } catch (Exception e) {
        }
        return sketchPath + File.separator + where;
    }

    public File sketchFile(String where) {
        return new File(sketchPath(where));
    }

    public String savePath(String where) {
        if (where == null)
            return null;
        String filename = sketchPath(where);
        createPath(filename);
        return filename;
    }

    public File saveFile(String where) {
        return new File(savePath(where));
    }

    public String dataPath(String where) {
        return dataFile(where).getAbsolutePath();
    }

    public File dataFile(String where) {
        File why = new File(where);
        if (why.isAbsolute())
            return why;
        String jarPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        if (jarPath.contains("Contents/Resources/Java/")) {
            File containingFolder = new File(urlDecode(jarPath)).getParentFile();
            File dataFolder = new File(containingFolder, "data");
            return new File(dataFolder, where);
        }
        return new File(sketchPath + File.separator + "data" + File.separator + where);
    }

    static public void createPath(String path) {
        createPath(new File(path));
    }

    static public void createPath(File file) {
        try {
            String parent = file.getParent();
            if (parent != null) {
                File unit = new File(parent);
                if (!unit.exists())
                    unit.mkdirs();
            }
        } catch (SecurityException se) {
            System.err.println("You don't have permissions to create " + file.getAbsolutePath());
        }
    }

    static public String getExtension(String filename) {
        String extension;
        String lower = filename.toLowerCase();
        int dot = filename.lastIndexOf('.');
        if (dot == -1) {
            extension = "unknown";
        }
        extension = lower.substring(dot + 1);
        int question = extension.indexOf('?');
        if (question != -1) {
            extension = extension.substring(0, question);
        }
        return extension;
    }

    static public String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    static public String urlDecode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    static public byte[] sort(byte[] list) {
        return sort(list, list.length);
    }

    static public byte[] sort(byte[] list, int count) {
        byte[] outgoing = new byte[list.length];
        System.arraycopy(list, 0, outgoing, 0, list.length);
        Arrays.sort(outgoing, 0, count);
        return outgoing;
    }

    static public char[] sort(char[] list) {
        return sort(list, list.length);
    }

    static public char[] sort(char[] list, int count) {
        char[] outgoing = new char[list.length];
        System.arraycopy(list, 0, outgoing, 0, list.length);
        Arrays.sort(outgoing, 0, count);
        return outgoing;
    }

    static public int[] sort(int[] list) {
        return sort(list, list.length);
    }

    static public int[] sort(int[] list, int count) {
        int[] outgoing = new int[list.length];
        System.arraycopy(list, 0, outgoing, 0, list.length);
        Arrays.sort(outgoing, 0, count);
        return outgoing;
    }

    static public float[] sort(float[] list) {
        return sort(list, list.length);
    }

    static public float[] sort(float[] list, int count) {
        float[] outgoing = new float[list.length];
        System.arraycopy(list, 0, outgoing, 0, list.length);
        Arrays.sort(outgoing, 0, count);
        return outgoing;
    }

    static public String[] sort(String[] list) {
        return sort(list, list.length);
    }

    static public String[] sort(String[] list, int count) {
        String[] outgoing = new String[list.length];
        System.arraycopy(list, 0, outgoing, 0, list.length);
        Arrays.sort(outgoing, 0, count);
        return outgoing;
    }

    static public void arrayCopy(Object src, int srcPosition, Object dst, int dstPosition, int length) {
        System.arraycopy(src, srcPosition, dst, dstPosition, length);
    }

    static public void arrayCopy(Object src, Object dst, int length) {
        System.arraycopy(src, 0, dst, 0, length);
    }

    static public void arrayCopy(Object src, Object dst) {
        System.arraycopy(src, 0, dst, 0, Array.getLength(src));
    }

    static public void arraycopy(Object src, int srcPosition, Object dst, int dstPosition, int length) {
        System.arraycopy(src, srcPosition, dst, dstPosition, length);
    }

    static public void arraycopy(Object src, Object dst, int length) {
        System.arraycopy(src, 0, dst, 0, length);
    }

    static public void arraycopy(Object src, Object dst) {
        System.arraycopy(src, 0, dst, 0, Array.getLength(src));
    }

    static public boolean[] expand(boolean[] list) {
        return expand(list, list.length << 1);
    }

    static public boolean[] expand(boolean[] list, int newSize) {
        boolean[] temp = new boolean[newSize];
        System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
        return temp;
    }

    static public byte[] expand(byte[] list) {
        return expand(list, list.length << 1);
    }

    static public byte[] expand(byte[] list, int newSize) {
        byte[] temp = new byte[newSize];
        System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
        return temp;
    }

    static public char[] expand(char[] list) {
        return expand(list, list.length << 1);
    }

    static public char[] expand(char[] list, int newSize) {
        char[] temp = new char[newSize];
        System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
        return temp;
    }

    static public int[] expand(int[] list) {
        return expand(list, list.length << 1);
    }

    static public int[] expand(int[] list, int newSize) {
        int[] temp = new int[newSize];
        System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
        return temp;
    }

    static public long[] expand(long[] list) {
        return expand(list, list.length << 1);
    }

    static public long[] expand(long[] list, int newSize) {
        long[] temp = new long[newSize];
        System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
        return temp;
    }

    static public float[] expand(float[] list) {
        return expand(list, list.length << 1);
    }

    static public float[] expand(float[] list, int newSize) {
        float[] temp = new float[newSize];
        System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
        return temp;
    }

    static public double[] expand(double[] list) {
        return expand(list, list.length << 1);
    }

    static public double[] expand(double[] list, int newSize) {
        double[] temp = new double[newSize];
        System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
        return temp;
    }

    static public String[] expand(String[] list) {
        return expand(list, list.length << 1);
    }

    static public String[] expand(String[] list, int newSize) {
        String[] temp = new String[newSize];
        System.arraycopy(list, 0, temp, 0, Math.min(newSize, list.length));
        return temp;
    }

    static public Object expand(Object array) {
        return expand(array, Array.getLength(array) << 1);
    }

    static public Object expand(Object list, int newSize) {
        Class<?> type = list.getClass().getComponentType();
        Object temp = Array.newInstance(type, newSize);
        System.arraycopy(list, 0, temp, 0, Math.min(Array.getLength(list), newSize));
        return temp;
    }

    static public byte[] append(byte[] array, byte value) {
        array = expand(array, array.length + 1);
        array[array.length - 1] = value;
        return array;
    }

    static public char[] append(char[] array, char value) {
        array = expand(array, array.length + 1);
        array[array.length - 1] = value;
        return array;
    }

    static public int[] append(int[] array, int value) {
        array = expand(array, array.length + 1);
        array[array.length - 1] = value;
        return array;
    }

    static public float[] append(float[] array, float value) {
        array = expand(array, array.length + 1);
        array[array.length - 1] = value;
        return array;
    }

    static public String[] append(String[] array, String value) {
        array = expand(array, array.length + 1);
        array[array.length - 1] = value;
        return array;
    }

    static public Object append(Object array, Object value) {
        int length = Array.getLength(array);
        array = expand(array, length + 1);
        Array.set(array, length, value);
        return array;
    }

    static public boolean[] shorten(boolean[] list) {
        return subset(list, 0, list.length - 1);
    }

    static public byte[] shorten(byte[] list) {
        return subset(list, 0, list.length - 1);
    }

    static public char[] shorten(char[] list) {
        return subset(list, 0, list.length - 1);
    }

    static public int[] shorten(int[] list) {
        return subset(list, 0, list.length - 1);
    }

    static public float[] shorten(float[] list) {
        return subset(list, 0, list.length - 1);
    }

    static public String[] shorten(String[] list) {
        return subset(list, 0, list.length - 1);
    }

    static public Object shorten(Object list) {
        int length = Array.getLength(list);
        return subset(list, 0, length - 1);
    }

    static final public boolean[] splice(boolean[] list, boolean value, int index) {
        boolean[] outgoing = new boolean[list.length + 1];
        System.arraycopy(list, 0, outgoing, 0, index);
        outgoing[index] = value;
        System.arraycopy(list, index, outgoing, index + 1, list.length - index);
        return outgoing;
    }

    static final public boolean[] splice(boolean[] list, boolean[] value, int index) {
        boolean[] outgoing = new boolean[list.length + value.length];
        System.arraycopy(list, 0, outgoing, 0, index);
        System.arraycopy(value, 0, outgoing, index, value.length);
        System.arraycopy(list, index, outgoing, index + value.length, list.length - index);
        return outgoing;
    }

    static final public byte[] splice(byte[] list, byte value, int index) {
        byte[] outgoing = new byte[list.length + 1];
        System.arraycopy(list, 0, outgoing, 0, index);
        outgoing[index] = value;
        System.arraycopy(list, index, outgoing, index + 1, list.length - index);
        return outgoing;
    }

    static final public byte[] splice(byte[] list, byte[] value, int index) {
        byte[] outgoing = new byte[list.length + value.length];
        System.arraycopy(list, 0, outgoing, 0, index);
        System.arraycopy(value, 0, outgoing, index, value.length);
        System.arraycopy(list, index, outgoing, index + value.length, list.length - index);
        return outgoing;
    }

    static final public char[] splice(char[] list, char value, int index) {
        char[] outgoing = new char[list.length + 1];
        System.arraycopy(list, 0, outgoing, 0, index);
        outgoing[index] = value;
        System.arraycopy(list, index, outgoing, index + 1, list.length - index);
        return outgoing;
    }

    static final public char[] splice(char[] list, char[] value, int index) {
        char[] outgoing = new char[list.length + value.length];
        System.arraycopy(list, 0, outgoing, 0, index);
        System.arraycopy(value, 0, outgoing, index, value.length);
        System.arraycopy(list, index, outgoing, index + value.length, list.length - index);
        return outgoing;
    }

    static final public int[] splice(int[] list, int value, int index) {
        int[] outgoing = new int[list.length + 1];
        System.arraycopy(list, 0, outgoing, 0, index);
        outgoing[index] = value;
        System.arraycopy(list, index, outgoing, index + 1, list.length - index);
        return outgoing;
    }

    static final public int[] splice(int[] list, int[] value, int index) {
        int[] outgoing = new int[list.length + value.length];
        System.arraycopy(list, 0, outgoing, 0, index);
        System.arraycopy(value, 0, outgoing, index, value.length);
        System.arraycopy(list, index, outgoing, index + value.length, list.length - index);
        return outgoing;
    }

    static final public float[] splice(float[] list, float value, int index) {
        float[] outgoing = new float[list.length + 1];
        System.arraycopy(list, 0, outgoing, 0, index);
        outgoing[index] = value;
        System.arraycopy(list, index, outgoing, index + 1, list.length - index);
        return outgoing;
    }

    static final public float[] splice(float[] list, float[] value, int index) {
        float[] outgoing = new float[list.length + value.length];
        System.arraycopy(list, 0, outgoing, 0, index);
        System.arraycopy(value, 0, outgoing, index, value.length);
        System.arraycopy(list, index, outgoing, index + value.length, list.length - index);
        return outgoing;
    }

    static final public String[] splice(String[] list, String value, int index) {
        String[] outgoing = new String[list.length + 1];
        System.arraycopy(list, 0, outgoing, 0, index);
        outgoing[index] = value;
        System.arraycopy(list, index, outgoing, index + 1, list.length - index);
        return outgoing;
    }

    static final public String[] splice(String[] list, String[] value, int index) {
        String[] outgoing = new String[list.length + value.length];
        System.arraycopy(list, 0, outgoing, 0, index);
        System.arraycopy(value, 0, outgoing, index, value.length);
        System.arraycopy(list, index, outgoing, index + value.length, list.length - index);
        return outgoing;
    }

    static final public Object splice(Object list, Object value, int index) {
        Object[] outgoing = null;
        int length = Array.getLength(list);
        if (value.getClass().getName().charAt(0) == '[') {
            int vlength = Array.getLength(value);
            outgoing = new Object[length + vlength];
            System.arraycopy(list, 0, outgoing, 0, index);
            System.arraycopy(value, 0, outgoing, index, vlength);
            System.arraycopy(list, index, outgoing, index + vlength, length - index);
        } else {
            outgoing = new Object[length + 1];
            System.arraycopy(list, 0, outgoing, 0, index);
            Array.set(outgoing, index, value);
            System.arraycopy(list, index, outgoing, index + 1, length - index);
        }
        return outgoing;
    }

    static public boolean[] subset(boolean[] list, int start) {
        return subset(list, start, list.length - start);
    }

    static public boolean[] subset(boolean[] list, int start, int count) {
        boolean[] output = new boolean[count];
        System.arraycopy(list, start, output, 0, count);
        return output;
    }

    static public byte[] subset(byte[] list, int start) {
        return subset(list, start, list.length - start);
    }

    static public byte[] subset(byte[] list, int start, int count) {
        byte[] output = new byte[count];
        System.arraycopy(list, start, output, 0, count);
        return output;
    }

    static public char[] subset(char[] list, int start) {
        return subset(list, start, list.length - start);
    }

    static public char[] subset(char[] list, int start, int count) {
        char[] output = new char[count];
        System.arraycopy(list, start, output, 0, count);
        return output;
    }

    static public int[] subset(int[] list, int start) {
        return subset(list, start, list.length - start);
    }

    static public int[] subset(int[] list, int start, int count) {
        int[] output = new int[count];
        System.arraycopy(list, start, output, 0, count);
        return output;
    }

    static public float[] subset(float[] list, int start) {
        return subset(list, start, list.length - start);
    }

    static public float[] subset(float[] list, int start, int count) {
        float[] output = new float[count];
        System.arraycopy(list, start, output, 0, count);
        return output;
    }

    static public String[] subset(String[] list, int start) {
        return subset(list, start, list.length - start);
    }

    static public String[] subset(String[] list, int start, int count) {
        String[] output = new String[count];
        System.arraycopy(list, start, output, 0, count);
        return output;
    }

    static public Object subset(Object list, int start) {
        int length = Array.getLength(list);
        return subset(list, start, length - start);
    }

    static public Object subset(Object list, int start, int count) {
        Class<?> type = list.getClass().getComponentType();
        Object outgoing = Array.newInstance(type, count);
        System.arraycopy(list, start, outgoing, 0, count);
        return outgoing;
    }

    static public boolean[] concat(boolean[] a, boolean[] b) {
        boolean[] c = new boolean[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    static public byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    static public char[] concat(char[] a, char[] b) {
        char[] c = new char[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    static public int[] concat(int[] a, int[] b) {
        int[] c = new int[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    static public float[] concat(float[] a, float[] b) {
        float[] c = new float[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    static public String[] concat(String[] a, String[] b) {
        String[] c = new String[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    static public Object concat(Object a, Object b) {
        Class<?> type = a.getClass().getComponentType();
        int alength = Array.getLength(a);
        int blength = Array.getLength(b);
        Object outgoing = Array.newInstance(type, alength + blength);
        System.arraycopy(a, 0, outgoing, 0, alength);
        System.arraycopy(b, 0, outgoing, alength, blength);
        return outgoing;
    }

    static public boolean[] reverse(boolean[] list) {
        boolean[] outgoing = new boolean[list.length];
        int length1 = list.length - 1;
        for (int i = 0; i < list.length; i++) {
            outgoing[i] = list[length1 - i];
        }
        return outgoing;
    }

    static public byte[] reverse(byte[] list) {
        byte[] outgoing = new byte[list.length];
        int length1 = list.length - 1;
        for (int i = 0; i < list.length; i++) {
            outgoing[i] = list[length1 - i];
        }
        return outgoing;
    }

    static public char[] reverse(char[] list) {
        char[] outgoing = new char[list.length];
        int length1 = list.length - 1;
        for (int i = 0; i < list.length; i++) {
            outgoing[i] = list[length1 - i];
        }
        return outgoing;
    }

    static public int[] reverse(int[] list) {
        int[] outgoing = new int[list.length];
        int length1 = list.length - 1;
        for (int i = 0; i < list.length; i++) {
            outgoing[i] = list[length1 - i];
        }
        return outgoing;
    }

    static public float[] reverse(float[] list) {
        float[] outgoing = new float[list.length];
        int length1 = list.length - 1;
        for (int i = 0; i < list.length; i++) {
            outgoing[i] = list[length1 - i];
        }
        return outgoing;
    }

    static public String[] reverse(String[] list) {
        String[] outgoing = new String[list.length];
        int length1 = list.length - 1;
        for (int i = 0; i < list.length; i++) {
            outgoing[i] = list[length1 - i];
        }
        return outgoing;
    }

    static public Object reverse(Object list) {
        Class<?> type = list.getClass().getComponentType();
        int length = Array.getLength(list);
        Object outgoing = Array.newInstance(type, length);
        for (int i = 0; i < length; i++) {
            Array.set(outgoing, i, Array.get(list, (length - 1) - i));
        }
        return outgoing;
    }

    static public String trim(String str) {
        return str.replace('\u00A0', ' ').trim();
    }

    static public String[] trim(String[] array) {
        String[] outgoing = new String[array.length];
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                outgoing[i] = array[i].replace('\u00A0', ' ').trim();
            }
        }
        return outgoing;
    }

    static public String join(String[] list, char separator) {
        return join(list, String.valueOf(separator));
    }

    static public String join(String[] list, String separator) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < list.length; i++) {
            if (i != 0)
                buffer.append(separator);
            buffer.append(list[i]);
        }
        return buffer.toString();
    }

    static public String[] splitTokens(String value) {
        return splitTokens(value, WHITESPACE);
    }

    static public String[] splitTokens(String value, String delim) {
        StringTokenizer toker = new StringTokenizer(value, delim);
        String[] pieces = new String[toker.countTokens()];
        int index = 0;
        while (toker.hasMoreTokens()) {
            pieces[index++] = toker.nextToken();
        }
        return pieces;
    }

    static public String[] split(String value, char delim) {
        if (value == null)
            return null;
        char[] chars = value.toCharArray();
        int splitCount = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == delim)
                splitCount++;
        }
        if (splitCount == 0) {
            String[] splits = new String[1];
            splits[0] = new String(value);
            return splits;
        }
        String[] splits = new String[splitCount + 1];
        int splitIndex = 0;
        int startIndex = 0;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == delim) {
                splits[splitIndex++] = new String(chars, startIndex, i - startIndex);
                startIndex = i + 1;
            }
        }
        splits[splitIndex] = new String(chars, startIndex, chars.length - startIndex);
        return splits;
    }

    static public String[] split(String value, String delim) {
        ArrayList<String> items = new ArrayList<String>();
        int index;
        int offset = 0;
        while ((index = value.indexOf(delim, offset)) != -1) {
            items.add(value.substring(offset, index));
            offset = index + delim.length();
        }
        items.add(value.substring(offset));
        String[] outgoing = new String[items.size()];
        items.toArray(outgoing);
        return outgoing;
    }

    static protected HashMap<String, Pattern> matchPatterns;

    static Pattern matchPattern(String regexp) {
        Pattern p = null;
        if (matchPatterns == null) {
            matchPatterns = new HashMap<String, Pattern>();
        } else {
            p = matchPatterns.get(regexp);
        }
        if (p == null) {
            if (matchPatterns.size() == 10) {
                matchPatterns.clear();
            }
            p = Pattern.compile(regexp, Pattern.MULTILINE | Pattern.DOTALL);
            matchPatterns.put(regexp, p);
        }
        return p;
    }

    static public String[] match(String str, String regexp) {
        Pattern p = matchPattern(regexp);
        Matcher m = p.matcher(str);
        if (m.find()) {
            int count = m.groupCount() + 1;
            String[] groups = new String[count];
            for (int i = 0; i < count; i++) {
                groups[i] = m.group(i);
            }
            return groups;
        }
        return null;
    }

    static public String[][] matchAll(String str, String regexp) {
        Pattern p = matchPattern(regexp);
        Matcher m = p.matcher(str);
        ArrayList<String[]> results = new ArrayList<String[]>();
        int count = m.groupCount() + 1;
        while (m.find()) {
            String[] groups = new String[count];
            for (int i = 0; i < count; i++) {
                groups[i] = m.group(i);
            }
            results.add(groups);
        }
        if (results.isEmpty()) {
            return null;
        }
        String[][] matches = new String[results.size()][count];
        for (int i = 0; i < matches.length; i++) {
            matches[i] = results.get(i);
        }
        return matches;
    }

    static final public boolean parseBoolean(int what) {
        return (what != 0);
    }

    static final public boolean parseBoolean(String what) {
        return new Boolean(what).booleanValue();
    }

    static final public boolean[] parseBoolean(int[] what) {
        boolean[] outgoing = new boolean[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = (what[i] != 0);
        }
        return outgoing;
    }

    static final public boolean[] parseBoolean(String[] what) {
        boolean[] outgoing = new boolean[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = new Boolean(what[i]).booleanValue();
        }
        return outgoing;
    }

    static final public byte parseByte(boolean what) {
        return what ? (byte) 1 : 0;
    }

    static final public byte parseByte(char what) {
        return (byte) what;
    }

    static final public byte parseByte(int what) {
        return (byte) what;
    }

    static final public byte parseByte(float what) {
        return (byte) what;
    }

    static final public byte[] parseByte(boolean[] what) {
        byte[] outgoing = new byte[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = what[i] ? (byte) 1 : 0;
        }
        return outgoing;
    }

    static final public byte[] parseByte(char[] what) {
        byte[] outgoing = new byte[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = (byte) what[i];
        }
        return outgoing;
    }

    static final public byte[] parseByte(int[] what) {
        byte[] outgoing = new byte[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = (byte) what[i];
        }
        return outgoing;
    }

    static final public byte[] parseByte(float[] what) {
        byte[] outgoing = new byte[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = (byte) what[i];
        }
        return outgoing;
    }

    static final public char parseChar(byte what) {
        return (char) (what & 0xff);
    }

    static final public char parseChar(int what) {
        return (char) what;
    }

    static final public char[] parseChar(byte[] what) {
        char[] outgoing = new char[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = (char) (what[i] & 0xff);
        }
        return outgoing;
    }

    static final public char[] parseChar(int[] what) {
        char[] outgoing = new char[what.length];
        for (int i = 0; i < what.length; i++) {
            outgoing[i] = (char) what[i];
        }
        return outgoing;
    }

    static final public int parseInt(boolean what) {
        return what ? 1 : 0;
    }

    static final public int parseInt(byte what) {
        return what & 0xff;
    }

    static final public int parseInt(char what) {
        return what;
    }

    static final public int parseInt(float what) {
        return (int) what;
    }

    static final public int parseInt(String what) {
        return parseInt(what, 0);
    }

    static final public int parseInt(String what, int otherwise) {
        try {
            int offset = what.indexOf('.');
            if (offset == -1) {
                return Integer.parseInt(what);
            } else {
                return Integer.parseInt(what.substring(0, offset));
            }
        } catch (NumberFormatException e) {
        }
        return otherwise;
    }

    static final public int[] parseInt(boolean[] what) {
        int[] list = new int[what.length];
        for (int i = 0; i < what.length; i++) {
            list[i] = what[i] ? 1 : 0;
        }
        return list;
    }

    static final public int[] parseInt(byte[] what) {
        int[] list = new int[what.length];
        for (int i = 0; i < what.length; i++) {
            list[i] = (what[i] & 0xff);
        }
        return list;
    }

    static final public int[] parseInt(char[] what) {
        int[] list = new int[what.length];
        for (int i = 0; i < what.length; i++) {
            list[i] = what[i];
        }
        return list;
    }

    static public int[] parseInt(float[] what) {
        int[] inties = new int[what.length];
        for (int i = 0; i < what.length; i++) {
            inties[i] = (int) what[i];
        }
        return inties;
    }

    static public int[] parseInt(String[] what) {
        return parseInt(what, 0);
    }

    static public int[] parseInt(String[] what, int missing) {
        int[] output = new int[what.length];
        for (int i = 0; i < what.length; i++) {
            try {
                output[i] = Integer.parseInt(what[i]);
            } catch (NumberFormatException e) {
                output[i] = missing;
            }
        }
        return output;
    }

    static final public float parseFloat(int what) {
        return what;
    }

    static final public float parseFloat(String what) {
        return parseFloat(what, Float.NaN);
    }

    static final public float parseFloat(String what, float otherwise) {
        try {
            return new Float(what).floatValue();
        } catch (NumberFormatException e) {
        }
        return otherwise;
    }

    static final public float[] parseByte(byte[] what) {
        float[] floaties = new float[what.length];
        for (int i = 0; i < what.length; i++) {
            floaties[i] = what[i];
        }
        return floaties;
    }

    static final public float[] parseFloat(int[] what) {
        float[] floaties = new float[what.length];
        for (int i = 0; i < what.length; i++) {
            floaties[i] = what[i];
        }
        return floaties;
    }

    static final public float[] parseFloat(String[] what) {
        return parseFloat(what, Float.NaN);
    }

    static final public float[] parseFloat(String[] what, float missing) {
        float[] output = new float[what.length];
        for (int i = 0; i < what.length; i++) {
            try {
                output[i] = new Float(what[i]).floatValue();
            } catch (NumberFormatException e) {
                output[i] = missing;
            }
        }
        return output;
    }

    static final public String str(boolean x) {
        return String.valueOf(x);
    }

    static final public String str(byte x) {
        return String.valueOf(x);
    }

    static final public String str(char x) {
        return String.valueOf(x);
    }

    static final public String str(int x) {
        return String.valueOf(x);
    }

    static final public String str(float x) {
        return String.valueOf(x);
    }

    static final public String[] str(boolean[] x) {
        String[] s = new String[x.length];
        for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
        return s;
    }

    static final public String[] str(byte[] x) {
        String[] s = new String[x.length];
        for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
        return s;
    }

    static final public String[] str(char[] x) {
        String[] s = new String[x.length];
        for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
        return s;
    }

    static final public String[] str(int[] x) {
        String[] s = new String[x.length];
        for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
        return s;
    }

    static final public String[] str(float[] x) {
        String[] s = new String[x.length];
        for (int i = 0; i < x.length; i++) s[i] = String.valueOf(x[i]);
        return s;
    }

    static private NumberFormat int_nf;

    static private int int_nf_digits;

    static private boolean int_nf_commas;

    static public String[] nf(int[] num, int digits) {
        String[] formatted = new String[num.length];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = nf(num[i], digits);
        }
        return formatted;
    }

    static public String nf(int num, int digits) {
        if ((int_nf != null) && (int_nf_digits == digits) && !int_nf_commas) {
            return int_nf.format(num);
        }
        int_nf = NumberFormat.getInstance();
        int_nf.setGroupingUsed(false);
        int_nf_commas = false;
        int_nf.setMinimumIntegerDigits(digits);
        int_nf_digits = digits;
        return int_nf.format(num);
    }

    static public String[] nfc(int[] num) {
        String[] formatted = new String[num.length];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = nfc(num[i]);
        }
        return formatted;
    }

    static public String nfc(int num) {
        if ((int_nf != null) && (int_nf_digits == 0) && int_nf_commas) {
            return int_nf.format(num);
        }
        int_nf = NumberFormat.getInstance();
        int_nf.setGroupingUsed(true);
        int_nf_commas = true;
        int_nf.setMinimumIntegerDigits(0);
        int_nf_digits = 0;
        return int_nf.format(num);
    }

    static public String nfs(int num, int digits) {
        return (num < 0) ? nf(num, digits) : (' ' + nf(num, digits));
    }

    static public String[] nfs(int[] num, int digits) {
        String[] formatted = new String[num.length];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = nfs(num[i], digits);
        }
        return formatted;
    }

    static public String nfp(int num, int digits) {
        return (num < 0) ? nf(num, digits) : ('+' + nf(num, digits));
    }

    static public String[] nfp(int[] num, int digits) {
        String[] formatted = new String[num.length];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = nfp(num[i], digits);
        }
        return formatted;
    }

    static private NumberFormat float_nf;

    static private int float_nf_left, float_nf_right;

    static private boolean float_nf_commas;

    static public String[] nf(float[] num, int left, int right) {
        String[] formatted = new String[num.length];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = nf(num[i], left, right);
        }
        return formatted;
    }

    static public String nf(float num, int left, int right) {
        if ((float_nf != null) && (float_nf_left == left) && (float_nf_right == right) && !float_nf_commas) {
            return float_nf.format(num);
        }
        float_nf = NumberFormat.getInstance();
        float_nf.setGroupingUsed(false);
        float_nf_commas = false;
        if (left != 0)
            float_nf.setMinimumIntegerDigits(left);
        if (right != 0) {
            float_nf.setMinimumFractionDigits(right);
            float_nf.setMaximumFractionDigits(right);
        }
        float_nf_left = left;
        float_nf_right = right;
        return float_nf.format(num);
    }

    static public String[] nfc(float[] num, int right) {
        String[] formatted = new String[num.length];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = nfc(num[i], right);
        }
        return formatted;
    }

    static public String nfc(float num, int right) {
        if ((float_nf != null) && (float_nf_left == 0) && (float_nf_right == right) && float_nf_commas) {
            return float_nf.format(num);
        }
        float_nf = NumberFormat.getInstance();
        float_nf.setGroupingUsed(true);
        float_nf_commas = true;
        if (right != 0) {
            float_nf.setMinimumFractionDigits(right);
            float_nf.setMaximumFractionDigits(right);
        }
        float_nf_left = 0;
        float_nf_right = right;
        return float_nf.format(num);
    }

    static public String[] nfs(float[] num, int left, int right) {
        String[] formatted = new String[num.length];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = nfs(num[i], left, right);
        }
        return formatted;
    }

    static public String nfs(float num, int left, int right) {
        return (num < 0) ? nf(num, left, right) : (' ' + nf(num, left, right));
    }

    static public String[] nfp(float[] num, int left, int right) {
        String[] formatted = new String[num.length];
        for (int i = 0; i < formatted.length; i++) {
            formatted[i] = nfp(num[i], left, right);
        }
        return formatted;
    }

    static public String nfp(float num, int left, int right) {
        return (num < 0) ? nf(num, left, right) : ('+' + nf(num, left, right));
    }

    static final public String hex(byte value) {
        return hex(value, 2);
    }

    static final public String hex(char value) {
        return hex(value, 4);
    }

    static final public String hex(int value) {
        return hex(value, 8);
    }

    static final public String hex(int value, int digits) {
        String stuff = Integer.toHexString(value).toUpperCase();
        if (digits > 8) {
            digits = 8;
        }
        int length = stuff.length();
        if (length > digits) {
            return stuff.substring(length - digits);
        } else if (length < digits) {
            return "00000000".substring(8 - (digits - length)) + stuff;
        }
        return stuff;
    }

    static final public int unhex(String value) {
        return (int) (Long.parseLong(value, 16));
    }

    static final public String binary(byte value) {
        return binary(value, 8);
    }

    static final public String binary(char value) {
        return binary(value, 16);
    }

    static final public String binary(int value) {
        return binary(value, 32);
    }

    static final public String binary(int value, int digits) {
        String stuff = Integer.toBinaryString(value);
        if (digits > 32) {
            digits = 32;
        }
        int length = stuff.length();
        if (length > digits) {
            return stuff.substring(length - digits);
        } else if (length < digits) {
            int offset = 32 - (digits - length);
            return "00000000000000000000000000000000".substring(offset) + stuff;
        }
        return stuff;
    }

    static final public int unbinary(String value) {
        return Integer.parseInt(value, 2);
    }

    public final int color(int gray) {
        if (g == null) {
            if (gray > 255)
                gray = 255;
            else if (gray < 0)
                gray = 0;
            return 0xff000000 | (gray << 16) | (gray << 8) | gray;
        }
        return g.color(gray);
    }

    public final int color(float fgray) {
        if (g == null) {
            int gray = (int) fgray;
            if (gray > 255)
                gray = 255;
            else if (gray < 0)
                gray = 0;
            return 0xff000000 | (gray << 16) | (gray << 8) | gray;
        }
        return g.color(fgray);
    }

    public final int color(int gray, int alpha) {
        if (g == null) {
            if (alpha > 255)
                alpha = 255;
            else if (alpha < 0)
                alpha = 0;
            if (gray > 255) {
                return (alpha << 24) | (gray & 0xFFFFFF);
            } else {
                return (alpha << 24) | (gray << 16) | (gray << 8) | gray;
            }
        }
        return g.color(gray, alpha);
    }

    public final int color(float fgray, float falpha) {
        if (g == null) {
            int gray = (int) fgray;
            int alpha = (int) falpha;
            if (gray > 255)
                gray = 255;
            else if (gray < 0)
                gray = 0;
            if (alpha > 255)
                alpha = 255;
            else if (alpha < 0)
                alpha = 0;
            return 0xff000000 | (gray << 16) | (gray << 8) | gray;
        }
        return g.color(fgray, falpha);
    }

    public final int color(int v1, int v2, int v3) {
        if (g == null) {
            if (v1 > 255)
                v1 = 255;
            else if (v1 < 0)
                v1 = 0;
            if (v2 > 255)
                v2 = 255;
            else if (v2 < 0)
                v2 = 0;
            if (v3 > 255)
                v3 = 255;
            else if (v3 < 0)
                v3 = 0;
            return 0xff000000 | (v1 << 16) | (v2 << 8) | v3;
        }
        return g.color(v1, v2, v3);
    }

    public final int color(int v1, int v2, int v3, int alpha) {
        if (g == null) {
            if (alpha > 255)
                alpha = 255;
            else if (alpha < 0)
                alpha = 0;
            if (v1 > 255)
                v1 = 255;
            else if (v1 < 0)
                v1 = 0;
            if (v2 > 255)
                v2 = 255;
            else if (v2 < 0)
                v2 = 0;
            if (v3 > 255)
                v3 = 255;
            else if (v3 < 0)
                v3 = 0;
            return (alpha << 24) | (v1 << 16) | (v2 << 8) | v3;
        }
        return g.color(v1, v2, v3, alpha);
    }

    public final int color(float v1, float v2, float v3) {
        if (g == null) {
            if (v1 > 255)
                v1 = 255;
            else if (v1 < 0)
                v1 = 0;
            if (v2 > 255)
                v2 = 255;
            else if (v2 < 0)
                v2 = 0;
            if (v3 > 255)
                v3 = 255;
            else if (v3 < 0)
                v3 = 0;
            return 0xff000000 | ((int) v1 << 16) | ((int) v2 << 8) | (int) v3;
        }
        return g.color(v1, v2, v3);
    }

    public final int color(float v1, float v2, float v3, float alpha) {
        if (g == null) {
            if (alpha > 255)
                alpha = 255;
            else if (alpha < 0)
                alpha = 0;
            if (v1 > 255)
                v1 = 255;
            else if (v1 < 0)
                v1 = 0;
            if (v2 > 255)
                v2 = 255;
            else if (v2 < 0)
                v2 = 0;
            if (v3 > 255)
                v3 = 255;
            else if (v3 < 0)
                v3 = 0;
            return ((int) alpha << 24) | ((int) v1 << 16) | ((int) v2 << 8) | (int) v3;
        }
        return g.color(v1, v2, v3, alpha);
    }

    static public int blendColor(int c1, int c2, int mode) {
        return PImage.blendColor(c1, c2, mode);
    }

    public void setupExternalMessages() {
        frame.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentMoved(ComponentEvent e) {
                Point where = ((Frame) e.getSource()).getLocation();
                System.err.println(PApplet.EXTERNAL_MOVE + " " + where.x + " " + where.y);
                System.err.flush();
            }
        });
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
    }

    public void setupFrameResizeListener() {
        frame.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                if (frame.isResizable()) {
                    Frame farm = (Frame) e.getComponent();
                    if (farm.isVisible()) {
                        Insets insets = farm.getInsets();
                        Dimension windowSize = farm.getSize();
                        Rectangle newBounds = new Rectangle(insets.left, insets.top, windowSize.width - insets.left - insets.right, windowSize.height - insets.top - insets.bottom);
                        Rectangle oldBounds = getBounds();
                        if (!newBounds.equals(oldBounds)) {
                            setBounds(newBounds);
                        }
                    }
                }
            }
        });
    }

    static ArrayList<Image> iconImages;

    protected void setIconImage(Frame frame) {
        if (platform != MACOSX) {
            try {
                if (iconImages == null) {
                    iconImages = new ArrayList<Image>();
                    final int[] sizes = { 16, 24, 32, 48, 64 };
                    for (int sz : sizes) {
                        URL url = getClass().getResource("/icon/icon-" + sz + ".png");
                        Image image = Toolkit.getDefaultToolkit().getImage(url);
                        iconImages.add(image);
                    }
                }
                frame.setIconImages(iconImages);
            } catch (Exception e) {
            }
        }
    }

    static public void main(final String[] args) {
        runSketch(args, null);
    }

    static public void main(final String mainClass) {
        main(mainClass, null);
    }

    static public void main(final String mainClass, final String[] passedArgs) {
        String[] args = new String[] { mainClass };
        if (passedArgs != null) {
            args = concat(args, passedArgs);
        }
        runSketch(args, null);
    }

    static public void runSketch(final String[] args, final PApplet constructedApplet) {
        if (platform == MACOSX) {
            System.setProperty("apple.awt.graphics.UseQuartz", String.valueOf(useQuartz));
        }
        System.setProperty("sun.awt.noerasebackground", "true");
        if (args.length < 1) {
            System.err.println("Usage: PApplet <appletname>");
            System.err.println("For additional options, " + "see the Javadoc for PApplet");
            System.exit(1);
        }
        boolean external = false;
        int[] location = null;
        int[] editorLocation = null;
        String name = null;
        boolean present = false;
        Color backgroundColor = null;
        Color stopColor = Color.GRAY;
        GraphicsDevice displayDevice = null;
        boolean hideStop = false;
        String param = null, value = null;
        String folder = null;
        try {
            folder = System.getProperty("user.dir");
        } catch (Exception e) {
        }
        int argIndex = 0;
        while (argIndex < args.length) {
            int equals = args[argIndex].indexOf('=');
            if (equals != -1) {
                param = args[argIndex].substring(0, equals);
                value = args[argIndex].substring(equals + 1);
                if (param.equals(ARGS_EDITOR_LOCATION)) {
                    external = true;
                    editorLocation = parseInt(split(value, ','));
                } else if (param.equals(ARGS_DISPLAY)) {
                    int deviceIndex = Integer.parseInt(value);
                    GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    GraphicsDevice[] devices = environment.getScreenDevices();
                    if ((deviceIndex >= 0) && (deviceIndex < devices.length)) {
                        displayDevice = devices[deviceIndex];
                    } else {
                        System.err.println("Display " + value + " does not exist, " + "using the default display instead.");
                        for (int i = 0; i < devices.length; i++) {
                            System.err.println(i + " is " + devices[i]);
                        }
                    }
                } else if (param.equals(ARGS_BGCOLOR)) {
                    if (value.charAt(0) == '#')
                        value = value.substring(1);
                    backgroundColor = new Color(Integer.parseInt(value, 16));
                } else if (param.equals(ARGS_STOP_COLOR)) {
                    if (value.charAt(0) == '#')
                        value = value.substring(1);
                    stopColor = new Color(Integer.parseInt(value, 16));
                } else if (param.equals(ARGS_SKETCH_FOLDER)) {
                    folder = value;
                } else if (param.equals(ARGS_LOCATION)) {
                    location = parseInt(split(value, ','));
                }
            } else {
                if (args[argIndex].equals(ARGS_PRESENT)) {
                    present = true;
                } else if (args[argIndex].equals(ARGS_FULL_SCREEN)) {
                    present = true;
                } else if (args[argIndex].equals(ARGS_HIDE_STOP)) {
                    hideStop = true;
                } else if (args[argIndex].equals(ARGS_EXTERNAL)) {
                    external = true;
                } else {
                    name = args[argIndex];
                    break;
                }
            }
            argIndex++;
        }
        if (displayDevice == null) {
            GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            displayDevice = environment.getDefaultScreenDevice();
        }
        Frame frame = new Frame(displayDevice.getDefaultConfiguration());
        frame.setBackground(new Color(0xCC, 0xCC, 0xCC));
        final PApplet applet;
        if (constructedApplet != null) {
            applet = constructedApplet;
        } else {
            try {
                Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(name);
                applet = (PApplet) c.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        applet.setIconImage(frame);
        frame.setTitle(name);
        applet.frame = frame;
        applet.sketchPath = folder;
        present |= applet.sketchFullScreen();
        applet.args = PApplet.subset(args, argIndex + 1);
        applet.external = external;
        Rectangle screenRect = displayDevice.getDefaultConfiguration().getBounds();
        if (screenRect.width == applet.sketchWidth() && screenRect.height == applet.sketchHeight()) {
            present = true;
        }
        if (present) {
            frame.setUndecorated(true);
            if (backgroundColor != null) {
                frame.setBackground(backgroundColor);
            }
            frame.setBounds(screenRect);
            frame.setVisible(true);
        }
        frame.setLayout(null);
        frame.add(applet);
        if (present) {
            frame.invalidate();
        } else {
            frame.pack();
        }
        frame.setResizable(false);
        applet.init();
        while (applet.defaultSize && !applet.finished) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
            }
        }
        if (present) {
            if (platform == MACOSX) {
                japplemenubar.JAppleMenuBar.hide();
            }
            frame.setBounds(screenRect);
            applet.setBounds((screenRect.width - applet.width) / 2, (screenRect.height - applet.height) / 2, applet.width, applet.height);
            if (!hideStop) {
                Label label = new Label("stop");
                label.setForeground(stopColor);
                label.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        System.exit(0);
                    }
                });
                frame.add(label);
                Dimension labelSize = label.getPreferredSize();
                labelSize = new Dimension(100, labelSize.height);
                label.setSize(labelSize);
                label.setLocation(20, screenRect.height - labelSize.height - 20);
            }
            if (external) {
                applet.setupExternalMessages();
            }
        } else {
            Insets insets = frame.getInsets();
            int windowW = Math.max(applet.width, MIN_WINDOW_WIDTH) + insets.left + insets.right;
            int windowH = Math.max(applet.height, MIN_WINDOW_HEIGHT) + insets.top + insets.bottom;
            frame.setSize(windowW, windowH);
            if (location != null) {
                frame.setLocation(location[0], location[1]);
            } else if (external && editorLocation != null) {
                int locationX = editorLocation[0] - 20;
                int locationY = editorLocation[1];
                if (locationX - windowW > 10) {
                    frame.setLocation(locationX - windowW, locationY);
                } else {
                    locationX = editorLocation[0] + 66;
                    locationY = editorLocation[1] + 66;
                    if ((locationX + windowW > applet.displayWidth - 33) || (locationY + windowH > applet.displayHeight - 33)) {
                        locationX = (applet.displayWidth - windowW) / 2;
                        locationY = (applet.displayHeight - windowH) / 2;
                    }
                    frame.setLocation(locationX, locationY);
                }
            } else {
                frame.setLocation(screenRect.x + (screenRect.width - applet.width) / 2, screenRect.y + (screenRect.height - applet.height) / 2);
            }
            Point frameLoc = frame.getLocation();
            if (frameLoc.y < 0) {
                frame.setLocation(frameLoc.x, 30);
            }
            if (backgroundColor != null) {
                frame.setBackground(backgroundColor);
            }
            int usableWindowH = windowH - insets.top - insets.bottom;
            applet.setBounds((windowW - applet.width) / 2, insets.top + (usableWindowH - applet.height) / 2, applet.width, applet.height);
            if (external) {
                applet.setupExternalMessages();
            } else {
                frame.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
            }
            applet.setupFrameResizeListener();
            if (applet.displayable()) {
                frame.setVisible(true);
            }
        }
    }

    protected void runSketch(final String[] args) {
        final String[] argsWithSketchName = new String[args.length + 1];
        System.arraycopy(args, 0, argsWithSketchName, 0, args.length);
        final String className = this.getClass().getSimpleName();
        final String cleanedClass = className.replaceAll("__[^_]+__\\$", "").replaceAll("\\$\\d+", "");
        argsWithSketchName[args.length] = cleanedClass;
        runSketch(argsWithSketchName, this);
    }

    protected void runSketch() {
        runSketch(new String[0]);
    }

    public PGraphics beginRecord(String renderer, String filename) {
        filename = insertFrame(filename);
        PGraphics rec = createGraphics(width, height, renderer, filename);
        beginRecord(rec);
        return rec;
    }

    public void beginRecord(PGraphics recorder) {
        this.recorder = recorder;
        recorder.beginDraw();
    }

    public void endRecord() {
        if (recorder != null) {
            recorder.endDraw();
            recorder.dispose();
            recorder = null;
        }
    }

    public PGraphics beginRaw(String renderer, String filename) {
        filename = insertFrame(filename);
        PGraphics rec = createGraphics(width, height, renderer, filename);
        g.beginRaw(rec);
        return rec;
    }

    public void beginRaw(PGraphics rawGraphics) {
        g.beginRaw(rawGraphics);
    }

    public void endRaw() {
        g.endRaw();
    }

    public void loadPixels() {
        g.loadPixels();
        pixels = g.pixels;
    }

    public void updatePixels() {
        g.updatePixels();
    }

    public void updatePixels(int x1, int y1, int x2, int y2) {
        g.updatePixels(x1, y1, x2, y2);
    }

    public void setCache(PImage image, Object storage) {
        if (recorder != null)
            recorder.setCache(image, storage);
        g.setCache(image, storage);
    }

    public Object getCache(PImage image) {
        return g.getCache(image);
    }

    public void removeCache(PImage image) {
        if (recorder != null)
            recorder.removeCache(image);
        g.removeCache(image);
    }

    public PGL beginPGL() {
        return g.beginPGL();
    }

    public void endPGL() {
        if (recorder != null)
            recorder.endPGL();
        g.endPGL();
    }

    public void flush() {
        if (recorder != null)
            recorder.flush();
        g.flush();
    }

    public void hint(int which) {
        if (recorder != null)
            recorder.hint(which);
        g.hint(which);
    }

    public void beginShape() {
        if (recorder != null)
            recorder.beginShape();
        g.beginShape();
    }

    public void beginShape(int kind) {
        if (recorder != null)
            recorder.beginShape(kind);
        g.beginShape(kind);
    }

    public void edge(boolean edge) {
        if (recorder != null)
            recorder.edge(edge);
        g.edge(edge);
    }

    public void normal(float nx, float ny, float nz) {
        if (recorder != null)
            recorder.normal(nx, ny, nz);
        g.normal(nx, ny, nz);
    }

    public void textureMode(int mode) {
        if (recorder != null)
            recorder.textureMode(mode);
        g.textureMode(mode);
    }

    public void textureWrap(int wrap) {
        if (recorder != null)
            recorder.textureWrap(wrap);
        g.textureWrap(wrap);
    }

    public void texture(PImage image) {
        if (recorder != null)
            recorder.texture(image);
        g.texture(image);
    }

    public void noTexture() {
        if (recorder != null)
            recorder.noTexture();
        g.noTexture();
    }

    public void vertex(float x, float y) {
        if (recorder != null)
            recorder.vertex(x, y);
        g.vertex(x, y);
    }

    public void vertex(float x, float y, float z) {
        if (recorder != null)
            recorder.vertex(x, y, z);
        g.vertex(x, y, z);
    }

    public void vertex(float[] v) {
        if (recorder != null)
            recorder.vertex(v);
        g.vertex(v);
    }

    public void vertex(float x, float y, float u, float v) {
        if (recorder != null)
            recorder.vertex(x, y, u, v);
        g.vertex(x, y, u, v);
    }

    public void vertex(float x, float y, float z, float u, float v) {
        if (recorder != null)
            recorder.vertex(x, y, z, u, v);
        g.vertex(x, y, z, u, v);
    }

    public void beginContour() {
        if (recorder != null)
            recorder.beginContour();
        g.beginContour();
    }

    public void endContour() {
        if (recorder != null)
            recorder.endContour();
        g.endContour();
    }

    public void endShape() {
        if (recorder != null)
            recorder.endShape();
        g.endShape();
    }

    public void endShape(int mode) {
        if (recorder != null)
            recorder.endShape(mode);
        g.endShape(mode);
    }

    public PShape loadShape(String filename) {
        return g.loadShape(filename);
    }

    public PShape loadShape(String filename, String options) {
        return g.loadShape(filename, options);
    }

    public PShape createShape() {
        return g.createShape();
    }

    public PShape createShape(PShape source) {
        return g.createShape(source);
    }

    public PShape createShape(int type) {
        return g.createShape(type);
    }

    public PShape createShape(int kind, float... p) {
        return g.createShape(kind, p);
    }

    public PShader loadShader(String fragFilename) {
        return g.loadShader(fragFilename);
    }

    public PShader loadShader(String fragFilename, String vertFilename) {
        return g.loadShader(fragFilename, vertFilename);
    }

    public void shader(PShader shader) {
        if (recorder != null)
            recorder.shader(shader);
        g.shader(shader);
    }

    public void shader(PShader shader, int kind) {
        if (recorder != null)
            recorder.shader(shader, kind);
        g.shader(shader, kind);
    }

    public void resetShader() {
        if (recorder != null)
            recorder.resetShader();
        g.resetShader();
    }

    public void resetShader(int kind) {
        if (recorder != null)
            recorder.resetShader(kind);
        g.resetShader(kind);
    }

    public void filter(PShader shader) {
        if (recorder != null)
            recorder.filter(shader);
        g.filter(shader);
    }

    public void clip(float a, float b, float c, float d) {
        if (recorder != null)
            recorder.clip(a, b, c, d);
        g.clip(a, b, c, d);
    }

    public void noClip() {
        if (recorder != null)
            recorder.noClip();
        g.noClip();
    }

    public void blendMode(int mode) {
        if (recorder != null)
            recorder.blendMode(mode);
        g.blendMode(mode);
    }

    public void bezierVertex(float x2, float y2, float x3, float y3, float x4, float y4) {
        if (recorder != null)
            recorder.bezierVertex(x2, y2, x3, y3, x4, y4);
        g.bezierVertex(x2, y2, x3, y3, x4, y4);
    }

    public void bezierVertex(float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        if (recorder != null)
            recorder.bezierVertex(x2, y2, z2, x3, y3, z3, x4, y4, z4);
        g.bezierVertex(x2, y2, z2, x3, y3, z3, x4, y4, z4);
    }

    public void quadraticVertex(float cx, float cy, float x3, float y3) {
        if (recorder != null)
            recorder.quadraticVertex(cx, cy, x3, y3);
        g.quadraticVertex(cx, cy, x3, y3);
    }

    public void quadraticVertex(float cx, float cy, float cz, float x3, float y3, float z3) {
        if (recorder != null)
            recorder.quadraticVertex(cx, cy, cz, x3, y3, z3);
        g.quadraticVertex(cx, cy, cz, x3, y3, z3);
    }

    public void curveVertex(float x, float y) {
        if (recorder != null)
            recorder.curveVertex(x, y);
        g.curveVertex(x, y);
    }

    public void curveVertex(float x, float y, float z) {
        if (recorder != null)
            recorder.curveVertex(x, y, z);
        g.curveVertex(x, y, z);
    }

    public void point(float x, float y) {
        if (recorder != null)
            recorder.point(x, y);
        g.point(x, y);
    }

    public void point(float x, float y, float z) {
        if (recorder != null)
            recorder.point(x, y, z);
        g.point(x, y, z);
    }

    public void line(float x1, float y1, float x2, float y2) {
        if (recorder != null)
            recorder.line(x1, y1, x2, y2);
        g.line(x1, y1, x2, y2);
    }

    public void line(float x1, float y1, float z1, float x2, float y2, float z2) {
        if (recorder != null)
            recorder.line(x1, y1, z1, x2, y2, z2);
        g.line(x1, y1, z1, x2, y2, z2);
    }

    public void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
        if (recorder != null)
            recorder.triangle(x1, y1, x2, y2, x3, y3);
        g.triangle(x1, y1, x2, y2, x3, y3);
    }

    public void quad(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        if (recorder != null)
            recorder.quad(x1, y1, x2, y2, x3, y3, x4, y4);
        g.quad(x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public void rectMode(int mode) {
        if (recorder != null)
            recorder.rectMode(mode);
        g.rectMode(mode);
    }

    public void rect(float a, float b, float c, float d) {
        if (recorder != null)
            recorder.rect(a, b, c, d);
        g.rect(a, b, c, d);
    }

    public void rect(float a, float b, float c, float d, float r) {
        if (recorder != null)
            recorder.rect(a, b, c, d, r);
        g.rect(a, b, c, d, r);
    }

    public void rect(float a, float b, float c, float d, float tl, float tr, float br, float bl) {
        if (recorder != null)
            recorder.rect(a, b, c, d, tl, tr, br, bl);
        g.rect(a, b, c, d, tl, tr, br, bl);
    }

    public void ellipseMode(int mode) {
        if (recorder != null)
            recorder.ellipseMode(mode);
        g.ellipseMode(mode);
    }

    public void ellipse(float a, float b, float c, float d) {
        if (recorder != null)
            recorder.ellipse(a, b, c, d);
        g.ellipse(a, b, c, d);
    }

    public void arc(float a, float b, float c, float d, float start, float stop) {
        if (recorder != null)
            recorder.arc(a, b, c, d, start, stop);
        g.arc(a, b, c, d, start, stop);
    }

    public void arc(float a, float b, float c, float d, float start, float stop, int mode) {
        if (recorder != null)
            recorder.arc(a, b, c, d, start, stop, mode);
        g.arc(a, b, c, d, start, stop, mode);
    }

    public void box(float size) {
        if (recorder != null)
            recorder.box(size);
        g.box(size);
    }

    public void box(float w, float h, float d) {
        if (recorder != null)
            recorder.box(w, h, d);
        g.box(w, h, d);
    }

    public void sphereDetail(int res) {
        if (recorder != null)
            recorder.sphereDetail(res);
        g.sphereDetail(res);
    }

    public void sphereDetail(int ures, int vres) {
        if (recorder != null)
            recorder.sphereDetail(ures, vres);
        g.sphereDetail(ures, vres);
    }

    public void sphere(float r) {
        if (recorder != null)
            recorder.sphere(r);
        g.sphere(r);
    }

    public float bezierPoint(float a, float b, float c, float d, float t) {
        return g.bezierPoint(a, b, c, d, t);
    }

    public float bezierTangent(float a, float b, float c, float d, float t) {
        return g.bezierTangent(a, b, c, d, t);
    }

    public void bezierDetail(int detail) {
        if (recorder != null)
            recorder.bezierDetail(detail);
        g.bezierDetail(detail);
    }

    public void bezier(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        if (recorder != null)
            recorder.bezier(x1, y1, x2, y2, x3, y3, x4, y4);
        g.bezier(x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public void bezier(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        if (recorder != null)
            recorder.bezier(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4);
        g.bezier(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4);
    }

    public float curvePoint(float a, float b, float c, float d, float t) {
        return g.curvePoint(a, b, c, d, t);
    }

    public float curveTangent(float a, float b, float c, float d, float t) {
        return g.curveTangent(a, b, c, d, t);
    }

    public void curveDetail(int detail) {
        if (recorder != null)
            recorder.curveDetail(detail);
        g.curveDetail(detail);
    }

    public void curveTightness(float tightness) {
        if (recorder != null)
            recorder.curveTightness(tightness);
        g.curveTightness(tightness);
    }

    public void curve(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        if (recorder != null)
            recorder.curve(x1, y1, x2, y2, x3, y3, x4, y4);
        g.curve(x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public void curve(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4) {
        if (recorder != null)
            recorder.curve(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4);
        g.curve(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4);
    }

    public void smooth() {
        if (recorder != null)
            recorder.smooth();
        g.smooth();
    }

    public void smooth(int level) {
        if (recorder != null)
            recorder.smooth(level);
        g.smooth(level);
    }

    public void noSmooth() {
        if (recorder != null)
            recorder.noSmooth();
        g.noSmooth();
    }

    public void imageMode(int mode) {
        if (recorder != null)
            recorder.imageMode(mode);
        g.imageMode(mode);
    }

    public void image(PImage img, float a, float b) {
        if (recorder != null)
            recorder.image(img, a, b);
        g.image(img, a, b);
    }

    public void image(PImage img, float a, float b, float c, float d) {
        if (recorder != null)
            recorder.image(img, a, b, c, d);
        g.image(img, a, b, c, d);
    }

    public void image(PImage img, float a, float b, float c, float d, int u1, int v1, int u2, int v2) {
        if (recorder != null)
            recorder.image(img, a, b, c, d, u1, v1, u2, v2);
        g.image(img, a, b, c, d, u1, v1, u2, v2);
    }

    public void shapeMode(int mode) {
        if (recorder != null)
            recorder.shapeMode(mode);
        g.shapeMode(mode);
    }

    public void shape(PShape shape) {
        if (recorder != null)
            recorder.shape(shape);
        g.shape(shape);
    }

    public void shape(PShape shape, float x, float y) {
        if (recorder != null)
            recorder.shape(shape, x, y);
        g.shape(shape, x, y);
    }

    public void shape(PShape shape, float a, float b, float c, float d) {
        if (recorder != null)
            recorder.shape(shape, a, b, c, d);
        g.shape(shape, a, b, c, d);
    }

    public void textAlign(int alignX) {
        if (recorder != null)
            recorder.textAlign(alignX);
        g.textAlign(alignX);
    }

    public void textAlign(int alignX, int alignY) {
        if (recorder != null)
            recorder.textAlign(alignX, alignY);
        g.textAlign(alignX, alignY);
    }

    public float textAscent() {
        return g.textAscent();
    }

    public float textDescent() {
        return g.textDescent();
    }

    public void textFont(PFont which) {
        if (recorder != null)
            recorder.textFont(which);
        g.textFont(which);
    }

    public void textFont(PFont which, float size) {
        if (recorder != null)
            recorder.textFont(which, size);
        g.textFont(which, size);
    }

    public void textLeading(float leading) {
        if (recorder != null)
            recorder.textLeading(leading);
        g.textLeading(leading);
    }

    public void textMode(int mode) {
        if (recorder != null)
            recorder.textMode(mode);
        g.textMode(mode);
    }

    public void textSize(float size) {
        if (recorder != null)
            recorder.textSize(size);
        g.textSize(size);
    }

    public float textWidth(char c) {
        return g.textWidth(c);
    }

    public float textWidth(String str) {
        return g.textWidth(str);
    }

    public float textWidth(char[] chars, int start, int length) {
        return g.textWidth(chars, start, length);
    }

    public void text(char c, float x, float y) {
        if (recorder != null)
            recorder.text(c, x, y);
        g.text(c, x, y);
    }

    public void text(char c, float x, float y, float z) {
        if (recorder != null)
            recorder.text(c, x, y, z);
        g.text(c, x, y, z);
    }

    public void text(String str, float x, float y) {
        if (recorder != null)
            recorder.text(str, x, y);
        g.text(str, x, y);
    }

    public void text(char[] chars, int start, int stop, float x, float y) {
        if (recorder != null)
            recorder.text(chars, start, stop, x, y);
        g.text(chars, start, stop, x, y);
    }

    public void text(String str, float x, float y, float z) {
        if (recorder != null)
            recorder.text(str, x, y, z);
        g.text(str, x, y, z);
    }

    public void text(char[] chars, int start, int stop, float x, float y, float z) {
        if (recorder != null)
            recorder.text(chars, start, stop, x, y, z);
        g.text(chars, start, stop, x, y, z);
    }

    public void text(String str, float x1, float y1, float x2, float y2) {
        if (recorder != null)
            recorder.text(str, x1, y1, x2, y2);
        g.text(str, x1, y1, x2, y2);
    }

    public void text(int num, float x, float y) {
        if (recorder != null)
            recorder.text(num, x, y);
        g.text(num, x, y);
    }

    public void text(int num, float x, float y, float z) {
        if (recorder != null)
            recorder.text(num, x, y, z);
        g.text(num, x, y, z);
    }

    public void text(float num, float x, float y) {
        if (recorder != null)
            recorder.text(num, x, y);
        g.text(num, x, y);
    }

    public void text(float num, float x, float y, float z) {
        if (recorder != null)
            recorder.text(num, x, y, z);
        g.text(num, x, y, z);
    }

    public void pushMatrix() {
        if (recorder != null)
            recorder.pushMatrix();
        g.pushMatrix();
    }

    public void popMatrix() {
        if (recorder != null)
            recorder.popMatrix();
        g.popMatrix();
    }

    public void translate(float x, float y) {
        if (recorder != null)
            recorder.translate(x, y);
        g.translate(x, y);
    }

    public void translate(float x, float y, float z) {
        if (recorder != null)
            recorder.translate(x, y, z);
        g.translate(x, y, z);
    }

    public void rotate(float angle) {
        if (recorder != null)
            recorder.rotate(angle);
        g.rotate(angle);
    }

    public void rotateX(float angle) {
        if (recorder != null)
            recorder.rotateX(angle);
        g.rotateX(angle);
    }

    public void rotateY(float angle) {
        if (recorder != null)
            recorder.rotateY(angle);
        g.rotateY(angle);
    }

    public void rotateZ(float angle) {
        if (recorder != null)
            recorder.rotateZ(angle);
        g.rotateZ(angle);
    }

    public void rotate(float angle, float x, float y, float z) {
        if (recorder != null)
            recorder.rotate(angle, x, y, z);
        g.rotate(angle, x, y, z);
    }

    public void scale(float s) {
        if (recorder != null)
            recorder.scale(s);
        g.scale(s);
    }

    public void scale(float x, float y) {
        if (recorder != null)
            recorder.scale(x, y);
        g.scale(x, y);
    }

    public void scale(float x, float y, float z) {
        if (recorder != null)
            recorder.scale(x, y, z);
        g.scale(x, y, z);
    }

    public void shearX(float angle) {
        if (recorder != null)
            recorder.shearX(angle);
        g.shearX(angle);
    }

    public void shearY(float angle) {
        if (recorder != null)
            recorder.shearY(angle);
        g.shearY(angle);
    }

    public void resetMatrix() {
        if (recorder != null)
            recorder.resetMatrix();
        g.resetMatrix();
    }

    public void applyMatrix(PMatrix source) {
        if (recorder != null)
            recorder.applyMatrix(source);
        g.applyMatrix(source);
    }

    public void applyMatrix(PMatrix2D source) {
        if (recorder != null)
            recorder.applyMatrix(source);
        g.applyMatrix(source);
    }

    public void applyMatrix(float n00, float n01, float n02, float n10, float n11, float n12) {
        if (recorder != null)
            recorder.applyMatrix(n00, n01, n02, n10, n11, n12);
        g.applyMatrix(n00, n01, n02, n10, n11, n12);
    }

    public void applyMatrix(PMatrix3D source) {
        if (recorder != null)
            recorder.applyMatrix(source);
        g.applyMatrix(source);
    }

    public void applyMatrix(float n00, float n01, float n02, float n03, float n10, float n11, float n12, float n13, float n20, float n21, float n22, float n23, float n30, float n31, float n32, float n33) {
        if (recorder != null)
            recorder.applyMatrix(n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22, n23, n30, n31, n32, n33);
        g.applyMatrix(n00, n01, n02, n03, n10, n11, n12, n13, n20, n21, n22, n23, n30, n31, n32, n33);
    }

    public PMatrix getMatrix() {
        return g.getMatrix();
    }

    public PMatrix2D getMatrix(PMatrix2D target) {
        return g.getMatrix(target);
    }

    public PMatrix3D getMatrix(PMatrix3D target) {
        return g.getMatrix(target);
    }

    public void setMatrix(PMatrix source) {
        if (recorder != null)
            recorder.setMatrix(source);
        g.setMatrix(source);
    }

    public void setMatrix(PMatrix2D source) {
        if (recorder != null)
            recorder.setMatrix(source);
        g.setMatrix(source);
    }

    public void setMatrix(PMatrix3D source) {
        if (recorder != null)
            recorder.setMatrix(source);
        g.setMatrix(source);
    }

    public void printMatrix() {
        if (recorder != null)
            recorder.printMatrix();
        g.printMatrix();
    }

    public void beginCamera() {
        if (recorder != null)
            recorder.beginCamera();
        g.beginCamera();
    }

    public void endCamera() {
        if (recorder != null)
            recorder.endCamera();
        g.endCamera();
    }

    public void camera() {
        if (recorder != null)
            recorder.camera();
        g.camera();
    }

    public void camera(float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {
        if (recorder != null)
            recorder.camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
        g.camera(eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);
    }

    public void printCamera() {
        if (recorder != null)
            recorder.printCamera();
        g.printCamera();
    }

    public void ortho() {
        if (recorder != null)
            recorder.ortho();
        g.ortho();
    }

    public void ortho(float left, float right, float bottom, float top) {
        if (recorder != null)
            recorder.ortho(left, right, bottom, top);
        g.ortho(left, right, bottom, top);
    }

    public void ortho(float left, float right, float bottom, float top, float near, float far) {
        if (recorder != null)
            recorder.ortho(left, right, bottom, top, near, far);
        g.ortho(left, right, bottom, top, near, far);
    }

    public void perspective() {
        if (recorder != null)
            recorder.perspective();
        g.perspective();
    }

    public void perspective(float fovy, float aspect, float zNear, float zFar) {
        if (recorder != null)
            recorder.perspective(fovy, aspect, zNear, zFar);
        g.perspective(fovy, aspect, zNear, zFar);
    }

    public void frustum(float left, float right, float bottom, float top, float near, float far) {
        if (recorder != null)
            recorder.frustum(left, right, bottom, top, near, far);
        g.frustum(left, right, bottom, top, near, far);
    }

    public void printProjection() {
        if (recorder != null)
            recorder.printProjection();
        g.printProjection();
    }

    public float screenX(float x, float y) {
        return g.screenX(x, y);
    }

    public float screenY(float x, float y) {
        return g.screenY(x, y);
    }

    public float screenX(float x, float y, float z) {
        return g.screenX(x, y, z);
    }

    public float screenY(float x, float y, float z) {
        return g.screenY(x, y, z);
    }

    public float screenZ(float x, float y, float z) {
        return g.screenZ(x, y, z);
    }

    public float modelX(float x, float y, float z) {
        return g.modelX(x, y, z);
    }

    public float modelY(float x, float y, float z) {
        return g.modelY(x, y, z);
    }

    public float modelZ(float x, float y, float z) {
        return g.modelZ(x, y, z);
    }

    public void pushStyle() {
        if (recorder != null)
            recorder.pushStyle();
        g.pushStyle();
    }

    public void popStyle() {
        if (recorder != null)
            recorder.popStyle();
        g.popStyle();
    }

    public void style(PStyle s) {
        if (recorder != null)
            recorder.style(s);
        g.style(s);
    }

    public void strokeWeight(float weight) {
        if (recorder != null)
            recorder.strokeWeight(weight);
        g.strokeWeight(weight);
    }

    public void strokeJoin(int join) {
        if (recorder != null)
            recorder.strokeJoin(join);
        g.strokeJoin(join);
    }

    public void strokeCap(int cap) {
        if (recorder != null)
            recorder.strokeCap(cap);
        g.strokeCap(cap);
    }

    public void noStroke() {
        if (recorder != null)
            recorder.noStroke();
        g.noStroke();
    }

    public void stroke(int rgb) {
        if (recorder != null)
            recorder.stroke(rgb);
        g.stroke(rgb);
    }

    public void stroke(int rgb, float alpha) {
        if (recorder != null)
            recorder.stroke(rgb, alpha);
        g.stroke(rgb, alpha);
    }

    public void stroke(float gray) {
        if (recorder != null)
            recorder.stroke(gray);
        g.stroke(gray);
    }

    public void stroke(float gray, float alpha) {
        if (recorder != null)
            recorder.stroke(gray, alpha);
        g.stroke(gray, alpha);
    }

    public void stroke(float v1, float v2, float v3) {
        if (recorder != null)
            recorder.stroke(v1, v2, v3);
        g.stroke(v1, v2, v3);
    }

    public void stroke(float v1, float v2, float v3, float alpha) {
        if (recorder != null)
            recorder.stroke(v1, v2, v3, alpha);
        g.stroke(v1, v2, v3, alpha);
    }

    public void noTint() {
        if (recorder != null)
            recorder.noTint();
        g.noTint();
    }

    public void tint(int rgb) {
        if (recorder != null)
            recorder.tint(rgb);
        g.tint(rgb);
    }

    public void tint(int rgb, float alpha) {
        if (recorder != null)
            recorder.tint(rgb, alpha);
        g.tint(rgb, alpha);
    }

    public void tint(float gray) {
        if (recorder != null)
            recorder.tint(gray);
        g.tint(gray);
    }

    public void tint(float gray, float alpha) {
        if (recorder != null)
            recorder.tint(gray, alpha);
        g.tint(gray, alpha);
    }

    public void tint(float v1, float v2, float v3) {
        if (recorder != null)
            recorder.tint(v1, v2, v3);
        g.tint(v1, v2, v3);
    }

    public void tint(float v1, float v2, float v3, float alpha) {
        if (recorder != null)
            recorder.tint(v1, v2, v3, alpha);
        g.tint(v1, v2, v3, alpha);
    }

    public void noFill() {
        if (recorder != null)
            recorder.noFill();
        g.noFill();
    }

    public void fill(int rgb) {
        if (recorder != null)
            recorder.fill(rgb);
        g.fill(rgb);
    }

    public void fill(int rgb, float alpha) {
        if (recorder != null)
            recorder.fill(rgb, alpha);
        g.fill(rgb, alpha);
    }

    public void fill(float gray) {
        if (recorder != null)
            recorder.fill(gray);
        g.fill(gray);
    }

    public void fill(float gray, float alpha) {
        if (recorder != null)
            recorder.fill(gray, alpha);
        g.fill(gray, alpha);
    }

    public void fill(float v1, float v2, float v3) {
        if (recorder != null)
            recorder.fill(v1, v2, v3);
        g.fill(v1, v2, v3);
    }

    public void fill(float v1, float v2, float v3, float alpha) {
        if (recorder != null)
            recorder.fill(v1, v2, v3, alpha);
        g.fill(v1, v2, v3, alpha);
    }

    public void ambient(int rgb) {
        if (recorder != null)
            recorder.ambient(rgb);
        g.ambient(rgb);
    }

    public void ambient(float gray) {
        if (recorder != null)
            recorder.ambient(gray);
        g.ambient(gray);
    }

    public void ambient(float v1, float v2, float v3) {
        if (recorder != null)
            recorder.ambient(v1, v2, v3);
        g.ambient(v1, v2, v3);
    }

    public void specular(int rgb) {
        if (recorder != null)
            recorder.specular(rgb);
        g.specular(rgb);
    }

    public void specular(float gray) {
        if (recorder != null)
            recorder.specular(gray);
        g.specular(gray);
    }

    public void specular(float v1, float v2, float v3) {
        if (recorder != null)
            recorder.specular(v1, v2, v3);
        g.specular(v1, v2, v3);
    }

    public void shininess(float shine) {
        if (recorder != null)
            recorder.shininess(shine);
        g.shininess(shine);
    }

    public void emissive(int rgb) {
        if (recorder != null)
            recorder.emissive(rgb);
        g.emissive(rgb);
    }

    public void emissive(float gray) {
        if (recorder != null)
            recorder.emissive(gray);
        g.emissive(gray);
    }

    public void emissive(float v1, float v2, float v3) {
        if (recorder != null)
            recorder.emissive(v1, v2, v3);
        g.emissive(v1, v2, v3);
    }

    public void lights() {
        if (recorder != null)
            recorder.lights();
        g.lights();
    }

    public void noLights() {
        if (recorder != null)
            recorder.noLights();
        g.noLights();
    }

    public void ambientLight(float v1, float v2, float v3) {
        if (recorder != null)
            recorder.ambientLight(v1, v2, v3);
        g.ambientLight(v1, v2, v3);
    }

    public void ambientLight(float v1, float v2, float v3, float x, float y, float z) {
        if (recorder != null)
            recorder.ambientLight(v1, v2, v3, x, y, z);
        g.ambientLight(v1, v2, v3, x, y, z);
    }

    public void directionalLight(float v1, float v2, float v3, float nx, float ny, float nz) {
        if (recorder != null)
            recorder.directionalLight(v1, v2, v3, nx, ny, nz);
        g.directionalLight(v1, v2, v3, nx, ny, nz);
    }

    public void pointLight(float v1, float v2, float v3, float x, float y, float z) {
        if (recorder != null)
            recorder.pointLight(v1, v2, v3, x, y, z);
        g.pointLight(v1, v2, v3, x, y, z);
    }

    public void spotLight(float v1, float v2, float v3, float x, float y, float z, float nx, float ny, float nz, float angle, float concentration) {
        if (recorder != null)
            recorder.spotLight(v1, v2, v3, x, y, z, nx, ny, nz, angle, concentration);
        g.spotLight(v1, v2, v3, x, y, z, nx, ny, nz, angle, concentration);
    }

    public void lightFalloff(float constant, float linear, float quadratic) {
        if (recorder != null)
            recorder.lightFalloff(constant, linear, quadratic);
        g.lightFalloff(constant, linear, quadratic);
    }

    public void lightSpecular(float v1, float v2, float v3) {
        if (recorder != null)
            recorder.lightSpecular(v1, v2, v3);
        g.lightSpecular(v1, v2, v3);
    }

    public void background(int rgb) {
        if (recorder != null)
            recorder.background(rgb);
        g.background(rgb);
    }

    public void background(int rgb, float alpha) {
        if (recorder != null)
            recorder.background(rgb, alpha);
        g.background(rgb, alpha);
    }

    public void background(float gray) {
        if (recorder != null)
            recorder.background(gray);
        g.background(gray);
    }

    public void background(float gray, float alpha) {
        if (recorder != null)
            recorder.background(gray, alpha);
        g.background(gray, alpha);
    }

    public void background(float v1, float v2, float v3) {
        if (recorder != null)
            recorder.background(v1, v2, v3);
        g.background(v1, v2, v3);
    }

    public void background(float v1, float v2, float v3, float alpha) {
        if (recorder != null)
            recorder.background(v1, v2, v3, alpha);
        g.background(v1, v2, v3, alpha);
    }

    public void clear() {
        if (recorder != null)
            recorder.clear();
        g.clear();
    }

    public void background(PImage image) {
        if (recorder != null)
            recorder.background(image);
        g.background(image);
    }

    public void colorMode(int mode) {
        if (recorder != null)
            recorder.colorMode(mode);
        g.colorMode(mode);
    }

    public void colorMode(int mode, float max) {
        if (recorder != null)
            recorder.colorMode(mode, max);
        g.colorMode(mode, max);
    }

    public void colorMode(int mode, float max1, float max2, float max3) {
        if (recorder != null)
            recorder.colorMode(mode, max1, max2, max3);
        g.colorMode(mode, max1, max2, max3);
    }

    public void colorMode(int mode, float max1, float max2, float max3, float maxA) {
        if (recorder != null)
            recorder.colorMode(mode, max1, max2, max3, maxA);
        g.colorMode(mode, max1, max2, max3, maxA);
    }

    public final float alpha(int rgb) {
        return g.alpha(rgb);
    }

    public final float red(int rgb) {
        return g.red(rgb);
    }

    public final float green(int rgb) {
        return g.green(rgb);
    }

    public final float blue(int rgb) {
        return g.blue(rgb);
    }

    public final float hue(int rgb) {
        return g.hue(rgb);
    }

    public final float saturation(int rgb) {
        return g.saturation(rgb);
    }

    public final float brightness(int rgb) {
        return g.brightness(rgb);
    }

    public int lerpColor(int c1, int c2, float amt) {
        return g.lerpColor(c1, c2, amt);
    }

    static public int lerpColor(int c1, int c2, float amt, int mode) {
        return PGraphics.lerpColor(c1, c2, amt, mode);
    }

    static public void showDepthWarning(String method) {
        PGraphics.showDepthWarning(method);
    }

    static public void showDepthWarningXYZ(String method) {
        PGraphics.showDepthWarningXYZ(method);
    }

    static public void showMethodWarning(String method) {
        PGraphics.showMethodWarning(method);
    }

    static public void showVariationWarning(String str) {
        PGraphics.showVariationWarning(str);
    }

    static public void showMissingWarning(String method) {
        PGraphics.showMissingWarning(method);
    }

    public boolean displayable() {
        return g.displayable();
    }

    public boolean isGL() {
        return g.isGL();
    }

    public int get(int x, int y) {
        return g.get(x, y);
    }

    public PImage get(int x, int y, int w, int h) {
        return g.get(x, y, w, h);
    }

    public PImage get() {
        return g.get();
    }

    public void set(int x, int y, int c) {
        if (recorder != null)
            recorder.set(x, y, c);
        g.set(x, y, c);
    }

    public void set(int x, int y, PImage img) {
        if (recorder != null)
            recorder.set(x, y, img);
        g.set(x, y, img);
    }

    public void mask(PImage img) {
        if (recorder != null)
            recorder.mask(img);
        g.mask(img);
    }

    public void filter(int kind) {
        if (recorder != null)
            recorder.filter(kind);
        g.filter(kind);
    }

    public void filter(int kind, float param) {
        if (recorder != null)
            recorder.filter(kind, param);
        g.filter(kind, param);
    }

    public void copy(int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
        if (recorder != null)
            recorder.copy(sx, sy, sw, sh, dx, dy, dw, dh);
        g.copy(sx, sy, sw, sh, dx, dy, dw, dh);
    }

    public void copy(PImage src, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
        if (recorder != null)
            recorder.copy(src, sx, sy, sw, sh, dx, dy, dw, dh);
        g.copy(src, sx, sy, sw, sh, dx, dy, dw, dh);
    }

    public void blend(int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh, int mode) {
        if (recorder != null)
            recorder.blend(sx, sy, sw, sh, dx, dy, dw, dh, mode);
        g.blend(sx, sy, sw, sh, dx, dy, dw, dh, mode);
    }

    public void blend(PImage src, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh, int mode) {
        if (recorder != null)
            recorder.blend(src, sx, sy, sw, sh, dx, dy, dw, dh, mode);
        g.blend(src, sx, sy, sw, sh, dx, dy, dw, dh, mode);
    }
}