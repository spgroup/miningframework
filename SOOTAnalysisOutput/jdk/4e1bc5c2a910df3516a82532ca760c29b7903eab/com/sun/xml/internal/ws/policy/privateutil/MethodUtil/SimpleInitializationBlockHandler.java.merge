package com.sun.xml.internal.ws.policy.privateutil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

class MethodUtil {

    private static final Logger LOGGER = Logger.getLogger(MethodUtil.class.getName());

    static Object invoke(Object target, Method method, Object[] args) throws IllegalAccessException, InvocationTargetException {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Invoking method using com.sun.xml.internal.ws.policy.util.MethodUtil");
        }
        try {
            return com.sun.xml.internal.ws.policy.util.MethodUtil.invoke(method, target, args);
        } catch (InvocationTargetException ite) {
            throw unwrapException(ite);
        }
    }

    private static InvocationTargetException unwrapException(InvocationTargetException ite) {
        Throwable targetException = ite.getTargetException();
        if (targetException != null && targetException instanceof InvocationTargetException) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Unwrapping invocation target exception");
            }
            return (InvocationTargetException) targetException;
        } else {
            return ite;
        }
    }
}