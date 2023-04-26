package com.sun.xml.internal.ws.api.server;

import com.sun.istack.internal.NotNull;
import com.sun.xml.internal.ws.api.message.Packet;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Invoker extends com.sun.xml.internal.ws.server.sei.Invoker {

    public void start(@NotNull WSWebServiceContext wsc, @NotNull WSEndpoint endpoint) {
        start(wsc);
    }

    public void start(@NotNull WebServiceContext wsc) {
        throw new IllegalStateException("deprecated version called");
    }

    public void dispose() {
    }

    public <T> T invokeProvider(@NotNull Packet p, T arg) throws IllegalAccessException, InvocationTargetException {
        return (T) invoke(p, invokeMethod, arg);
    }

    public <T> void invokeAsyncProvider(@NotNull Packet p, T arg, AsyncProviderCallback cbak, WebServiceContext ctxt) throws IllegalAccessException, InvocationTargetException {
        invoke(p, asyncInvokeMethod, arg, cbak, ctxt);
    }

    private static final Method invokeMethod;

    static {
        try {
            invokeMethod = Provider.class.getMethod("invoke", Object.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    private static final Method asyncInvokeMethod;

    static {
        try {
            asyncInvokeMethod = AsyncProvider.class.getMethod("invoke", Object.class, AsyncProviderCallback.class, WebServiceContext.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }
}