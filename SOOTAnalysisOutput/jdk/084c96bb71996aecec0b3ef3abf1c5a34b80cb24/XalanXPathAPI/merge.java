package com.sun.org.apache.xml.internal.security.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import com.sun.org.apache.xml.internal.security.transforms.implementations.FuncHere;
import com.sun.org.apache.xml.internal.utils.PrefixResolver;
import com.sun.org.apache.xml.internal.utils.PrefixResolverDefault;
import com.sun.org.apache.xpath.internal.Expression;
import com.sun.org.apache.xpath.internal.XPath;
import com.sun.org.apache.xpath.internal.XPathContext;
import com.sun.org.apache.xpath.internal.compiler.FunctionTable;
import com.sun.org.apache.xpath.internal.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XalanXPathAPI implements XPathAPI {

    private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(XalanXPathAPI.class.getName());

    private String xpathStr = null;

    private XPath xpath = null;

    private static FunctionTable funcTable = null;

    private static boolean installed;

    private XPathContext context;

    static {
        fixupFunctionTable();
    }

    public NodeList selectNodeList(Node contextNode, Node xpathnode, String str, Node namespaceNode) throws TransformerException {
        XObject list = eval(contextNode, xpathnode, str, namespaceNode);
        return list.nodelist();
    }

    public boolean evaluate(Node contextNode, Node xpathnode, String str, Node namespaceNode) throws TransformerException {
        XObject object = eval(contextNode, xpathnode, str, namespaceNode);
        return object.bool();
    }

    public void clear() {
        xpathStr = null;
        xpath = null;
        context = null;
    }

    public synchronized static boolean isInstalled() {
        return installed;
    }

    private XObject eval(Node contextNode, Node xpathnode, String str, Node namespaceNode) throws TransformerException {
        if (context == null) {
            context = new XPathContext(xpathnode);
            context.setSecureProcessing(true);
        }
        Node resolverNode = (namespaceNode.getNodeType() == Node.DOCUMENT_NODE) ? ((Document) namespaceNode).getDocumentElement() : namespaceNode;
        PrefixResolverDefault prefixResolver = new PrefixResolverDefault(resolverNode);
        if (!str.equals(xpathStr)) {
            if (str.indexOf("here()") > 0) {
                context.reset();
            }
            xpath = createXPath(str, prefixResolver);
            xpathStr = str;
        }
        int ctxtNode = context.getDTMHandleFromNode(contextNode);
        return xpath.execute(context, ctxtNode, prefixResolver);
    }

    private XPath createXPath(String str, PrefixResolver prefixResolver) throws TransformerException {
        XPath xpath = null;
        Class<?>[] classes = new Class<?>[] { String.class, SourceLocator.class, PrefixResolver.class, int.class, ErrorListener.class, FunctionTable.class };
        Object[] objects = new Object[] { str, null, prefixResolver, Integer.valueOf(XPath.SELECT), null, funcTable };
        try {
            Constructor<?> constructor = XPath.class.getConstructor(classes);
            xpath = (XPath) constructor.newInstance(objects);
        } catch (Exception ex) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, ex.getMessage(), ex);
            }
        }
        if (xpath == null) {
            xpath = new XPath(str, null, prefixResolver, XPath.SELECT, null);
        }
        return xpath;
    }

    private synchronized static void fixupFunctionTable() {
        installed = false;
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, "Registering Here function");
        }
        try {
            Class<?>[] args = { String.class, Expression.class };
            Method installFunction = FunctionTable.class.getMethod("installFunction", args);
            if ((installFunction.getModifiers() & Modifier.STATIC) != 0) {
                Object[] params = { "here", new FuncHere() };
                installFunction.invoke(null, params);
                installed = true;
            }
        } catch (Exception ex) {
            log.log(java.util.logging.Level.FINE, "Error installing function using the static installFunction method", ex);
        }
        if (!installed) {
            try {
                funcTable = new FunctionTable();
                Class<?>[] args = { String.class, Class.class };
                Method installFunction = FunctionTable.class.getMethod("installFunction", args);
                Object[] params = { "here", FuncHere.class };
                installFunction.invoke(funcTable, params);
                installed = true;
            } catch (Exception ex) {
                log.log(java.util.logging.Level.FINE, "Error installing function using the static installFunction method", ex);
            }
        }
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            if (installed) {
                log.log(java.util.logging.Level.FINE, "Registered class " + FuncHere.class.getName() + " for XPath function 'here()' function in internal table");
            } else {
                log.log(java.util.logging.Level.FINE, "Unable to register class " + FuncHere.class.getName() + " for XPath function 'here()' function in internal table");
            }
        }
    }
}
