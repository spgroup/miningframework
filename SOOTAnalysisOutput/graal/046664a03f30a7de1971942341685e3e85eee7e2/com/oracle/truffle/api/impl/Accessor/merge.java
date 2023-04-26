package com.oracle.truffle.api.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;

public abstract class Accessor {

    public abstract static class Nodes {

        @SuppressWarnings("rawtypes")
        public abstract Class<? extends TruffleLanguage> findLanguage(RootNode n);

        public abstract boolean isInstrumentable(RootNode rootNode);

        public abstract boolean isTaggedWith(Node node, Class<?> tag);
    }

    public abstract static class DebugSupport {

        public abstract void executionStarted(Object vm);
    }

    public abstract static class EngineSupport {

        public static final int EXECUTION_EVENT = 1;

        public static final int SUSPENDED_EVENT = 2;

        public abstract <C> FindContextNode<C> createFindContextNode(TruffleLanguage<C> lang);

        @SuppressWarnings("rawtypes")
        public abstract Env findEnv(Object vm, Class<? extends TruffleLanguage> languageClass);

        @SuppressWarnings("rawtypes")
        public abstract TruffleLanguage<?> findLanguageImpl(Object known, Class<? extends TruffleLanguage> languageClass, String mimeType);

        public abstract Object getInstrumentationHandler(Object vm);

        public abstract Object importSymbol(Object vm, TruffleLanguage<?> queryingLang, String globalName);

        public abstract void dispatchEvent(Object vm, Object event, int type);

        public abstract boolean isMimeTypeSupported(Object vm, String mimeType);

        public abstract void registerDebugger(Object vm, Object debugger);

        public abstract boolean isEvalRoot(RootNode target);

        @SuppressWarnings("rawtypes")
        public abstract Object findLanguage(Class<? extends TruffleLanguage> language);
    }

    public abstract static class LanguageSupport {

        public abstract Env attachEnv(Object vm, TruffleLanguage<?> language, OutputStream stdOut, OutputStream stdErr, InputStream stdIn, Map<String, Object> config);

        public abstract Object evalInContext(Object sourceVM, String code, Node node, MaterializedFrame frame);

        public abstract Object findExportedSymbol(TruffleLanguage.Env env, String globalName, boolean onlyExplicit);

        public abstract Object languageGlobal(TruffleLanguage.Env env);

        public abstract void dispose(TruffleLanguage<?> impl, Env env);

        public abstract TruffleLanguage<?> findLanguage(Env env);

        public abstract CallTarget parse(TruffleLanguage<?> truffleLanguage, Source code, Node context, String... argumentNames);

        public abstract String toString(TruffleLanguage<?> language, Env env, Object obj);

        public abstract Object findContext(Env env);

        public abstract Object getVM(Env env);
    }

    public abstract static class InstrumentSupport {

        public abstract void addInstrument(Object instrumentationHandler, Object key, Class<?> instrumentClass);

        public abstract void disposeInstrument(Object instrumentationHandler, Object key, boolean cleanupRequired);

        public abstract <T> T getInstrumentationHandlerService(Object handler, Object key, Class<T> type);

        public abstract Object createInstrumentationHandler(Object vm, OutputStream out, OutputStream err, InputStream in);

        public abstract void collectEnvServices(Set<Object> collectTo, Object vm, TruffleLanguage<?> impl, Env context);

        public abstract void detachLanguageFromInstrumentation(Object vm, Env context);

        public abstract void onFirstExecution(RootNode rootNode);

        public abstract void onLoad(RootNode rootNode);
    }

    protected abstract static class Frames {

        protected abstract void markMaterializeCalled(FrameDescriptor descriptor);

        protected abstract boolean getMaterializeCalled(FrameDescriptor descriptor);
    }

    private static Accessor.LanguageSupport API;

    private static Accessor.EngineSupport SPI;

    private static Accessor.Nodes NODES;

    private static Accessor.InstrumentSupport INSTRUMENTHANDLER;

    private static Accessor.DebugSupport DEBUG;

    private static Accessor.Frames FRAMES;

    static {
        TruffleLanguage<?> lng = new TruffleLanguage<Object>() {

            @Override
            protected Object findExportedSymbol(Object context, String globalName, boolean onlyExplicit) {
                return null;
            }

            @Override
            protected Object getLanguageGlobal(Object context) {
                return null;
            }

            @Override
            protected boolean isObjectOfLanguage(Object object) {
                return false;
            }

            @Override
            protected CallTarget parse(Source code, Node context, String... argumentNames) {
                throw new IllegalStateException();
            }

            @Override
            protected Object createContext(TruffleLanguage.Env env) {
                return null;
            }

            @Override
            protected Object evalInContext(Source source, Node node, MaterializedFrame mFrame) throws IOException {
                return null;
            }
        };
        lng.hashCode();
        new Node() {
        }.getRootNode();
        conditionallyInitDebugger();
    }

    @SuppressWarnings("all")
    private static void conditionallyInitDebugger() throws IllegalStateException {
        try {
            Class.forName("com.oracle.truffle.api.debug.Debugger", true, Accessor.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            boolean assertOn = false;
            assert assertOn = true;
            if (!assertOn) {
                throw new IllegalStateException(ex);
            }
        }
    }

    protected Accessor() {
        if (!this.getClass().getName().startsWith("com.oracle.truffle.api")) {
            throw new IllegalStateException();
        }
        if (this.getClass().getSimpleName().endsWith("API")) {
            if (API != null) {
                throw new IllegalStateException();
            }
            API = this.languageSupport();
        } else if (this.getClass().getSimpleName().endsWith("Nodes")) {
            if (NODES != null) {
                throw new IllegalStateException();
            }
            NODES = this.nodes();
        } else if (this.getClass().getSimpleName().endsWith("InstrumentHandler")) {
            if (INSTRUMENTHANDLER != null) {
                throw new IllegalStateException();
            }
            INSTRUMENTHANDLER = this.instrumentSupport();
        } else if (this.getClass().getSimpleName().endsWith("Debug")) {
            if (DEBUG != null) {
                throw new IllegalStateException();
            }
            DEBUG = this.debugSupport();
        } else if (this.getClass().getSimpleName().endsWith("Frames")) {
            if (FRAMES != null) {
                throw new IllegalStateException();
            }
            FRAMES = this.framesSupport();
        } else {
            if (SPI != null) {
                throw new IllegalStateException();
            }
            SPI = this.engineSupport();
        }
    }

    protected Accessor.Nodes nodes() {
        return NODES;
    }

    protected LanguageSupport languageSupport() {
        return API;
    }

    protected DebugSupport debugSupport() {
        return DEBUG;
    }

    protected EngineSupport engineSupport() {
        return SPI;
    }

    protected InstrumentSupport instrumentSupport() {
        return INSTRUMENTHANDLER;
    }

    static InstrumentSupport instrumentAccess() {
        return INSTRUMENTHANDLER;
    }

    static LanguageSupport languageAccess() {
        return API;
    }

    static EngineSupport engineAccess() {
        return SPI;
    }

    static DebugSupport debugAccess() {
        return DEBUG;
    }

    static Accessor.Nodes nodesAccess() {
        return NODES;
    }

    protected Accessor.Frames framesSupport() {
        return FRAMES;
    }

    static Accessor.Frames framesAccess() {
        return FRAMES;
    }

    public static void main(String... args) {
        throw new IllegalStateException();
    }

    private static final TVMCI SUPPORT = Truffle.getRuntime().getCapability(TVMCI.class);

    @SuppressWarnings("deprecation")
    protected void onLoopCount(Node source, int iterations) {
        if (SUPPORT != null) {
            SUPPORT.onLoopCount(source, iterations);
        } else {
            RootNode root = source.getRootNode();
            if (root != null) {
                RootCallTarget target = root.getCallTarget();
                if (target instanceof com.oracle.truffle.api.LoopCountReceiver) {
                    ((com.oracle.truffle.api.LoopCountReceiver) target).reportLoopCount(iterations);
                }
            }
        }
    }

    static <T extends TruffleLanguage<?>> T findLanguageByClass(Object vm, Class<T> languageClass) {
        Env env = SPI.findEnv(vm, languageClass);
        TruffleLanguage<?> language = API.findLanguage(env);
        return languageClass.cast(language);
    }
}
