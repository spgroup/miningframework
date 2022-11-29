package com.sun.org.apache.xml.internal.security.utils;

public abstract class XPathFactory {

    private static boolean xalanInstalled;

    static {
        try {
            Class<?> funcTableClass = ClassLoaderUtils.loadClass("com.sun.org.apache.xpath.internal.compiler.FunctionTable", XPathFactory.class);
            if (funcTableClass != null) {
                xalanInstalled = true;
            }
        } catch (Exception e) {
        }
    }

    protected synchronized static boolean isXalanInstalled() {
        return xalanInstalled;
    }

    public static XPathFactory newInstance() {
        if (!isXalanInstalled()) {
            return new JDKXPathFactory();
        }
        if (XalanXPathAPI.isInstalled()) {
            return new XalanXPathFactory();
        }
        return new JDKXPathFactory();
    }

    public abstract XPathAPI newXPathAPI();
}