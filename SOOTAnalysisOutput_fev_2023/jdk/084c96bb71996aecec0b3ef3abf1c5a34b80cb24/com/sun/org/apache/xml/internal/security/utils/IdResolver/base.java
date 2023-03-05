package com.sun.org.apache.xml.internal.security.utils;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.WeakHashMap;
import java.util.Map;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class IdResolver {

    private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(IdResolver.class.getName());

    private static Map<Document, Map<String, WeakReference<Element>>> docMap = new WeakHashMap<Document, Map<String, WeakReference<Element>>>();

    private IdResolver() {
    }

    public static void registerElementById(Element element, String idValue) {
        Document doc = element.getOwnerDocument();
        Map<String, WeakReference<Element>> elementMap;
        synchronized (docMap) {
            elementMap = docMap.get(doc);
            if (elementMap == null) {
                elementMap = new WeakHashMap<String, WeakReference<Element>>();
                docMap.put(doc, elementMap);
            }
        }
        elementMap.put(idValue, new WeakReference<Element>(element));
    }

    public static void registerElementById(Element element, Attr id) {
        IdResolver.registerElementById(element, id.getNodeValue());
    }

    public static Element getElementById(Document doc, String id) {
        Element result = IdResolver.getElementByIdType(doc, id);
        if (result != null) {
            log.log(java.util.logging.Level.FINE, "I could find an Element using the simple getElementByIdType method: " + result.getTagName());
            return result;
        }
        result = IdResolver.getElementByIdUsingDOM(doc, id);
        if (result != null) {
            log.log(java.util.logging.Level.FINE, "I could find an Element using the simple getElementByIdUsingDOM method: " + result.getTagName());
            return result;
        }
        result = IdResolver.getElementBySearching(doc, id);
        if (result != null) {
            IdResolver.registerElementById(result, id);
            return result;
        }
        return null;
    }

    private static Element getElementByIdUsingDOM(Document doc, String id) {
        if (log.isLoggable(java.util.logging.Level.FINE))
            log.log(java.util.logging.Level.FINE, "getElementByIdUsingDOM() Search for ID " + id);
        return doc.getElementById(id);
    }

    private static Element getElementByIdType(Document doc, String id) {
        if (log.isLoggable(java.util.logging.Level.FINE))
            log.log(java.util.logging.Level.FINE, "getElementByIdType() Search for ID " + id);
        Map<String, WeakReference<Element>> elementMap;
        synchronized (docMap) {
            elementMap = docMap.get(doc);
        }
        if (elementMap != null) {
            WeakReference<Element> weakReference = elementMap.get(id);
            if (weakReference != null) {
                return weakReference.get();
            }
        }
        return null;
    }

    private static java.util.List<String> names;

    private static int namesLength;

    static {
        String[] namespaces = { Constants.SignatureSpecNS, EncryptionConstants.EncryptionSpecNS, "http://schemas.xmlsoap.org/soap/security/2000-12", "http://www.w3.org/2002/03/xkms#", "urn:oasis:names:tc:SAML:1.0:assertion", "urn:oasis:names:tc:SAML:1.0:protocol" };
        names = Arrays.asList(namespaces);
        namesLength = names.size();
    }

    private static Element getElementBySearching(Node root, String id) {
        Element[] els = new Element[namesLength + 1];
        getEl(root, id, els);
        for (int i = 0; i < els.length; i++) {
            if (els[i] != null) {
                return els[i];
            }
        }
        return null;
    }

    private static int getEl(Node currentNode, String id, Element[] els) {
        Node sibling = null;
        Node parentNode = null;
        do {
            switch(currentNode.getNodeType()) {
                case Node.DOCUMENT_FRAGMENT_NODE:
                case Node.DOCUMENT_NODE:
                    sibling = currentNode.getFirstChild();
                    break;
                case Node.ELEMENT_NODE:
                    Element currentElement = (Element) currentNode;
                    if (isElement(currentElement, id, els) == 1)
                        return 1;
                    sibling = currentNode.getFirstChild();
                    if (sibling == null) {
                        if (parentNode != null) {
                            sibling = currentNode.getNextSibling();
                        }
                    } else {
                        parentNode = currentElement;
                    }
                    break;
            }
            while (sibling == null && parentNode != null) {
                sibling = parentNode.getNextSibling();
                parentNode = parentNode.getParentNode();
                if (parentNode != null && parentNode.getNodeType() != Node.ELEMENT_NODE) {
                    parentNode = null;
                }
            }
            if (sibling == null)
                return 1;
            currentNode = sibling;
            sibling = currentNode.getNextSibling();
        } while (true);
    }

    public static int isElement(Element el, String id, Element[] els) {
        if (!el.hasAttributes()) {
            return 0;
        }
        NamedNodeMap ns = el.getAttributes();
        int elementIndex = names.indexOf(el.getNamespaceURI());
        elementIndex = (elementIndex < 0) ? namesLength : elementIndex;
        for (int length = ns.getLength(), i = 0; i < length; i++) {
            Attr n = (Attr) ns.item(i);
            String s = n.getNamespaceURI();
            int index = s == null ? elementIndex : names.indexOf(n.getNamespaceURI());
            index = (index < 0) ? namesLength : index;
            String name = n.getLocalName();
            if (name == null)
                name = n.getName();
            if (name.length() > 2)
                continue;
            String value = n.getNodeValue();
            if (name.charAt(0) == 'I') {
                char ch = name.charAt(1);
                if (ch == 'd' && value.equals(id)) {
                    els[index] = el;
                    if (index == 0) {
                        return 1;
                    }
                } else if (ch == 'D' && value.endsWith(id)) {
                    if (index != 3) {
                        index = namesLength;
                    }
                    els[index] = el;
                }
            } else if ("id".equals(name) && value.equals(id)) {
                if (index != 2) {
                    index = namesLength;
                }
                els[index] = el;
            }
        }
        if ((elementIndex == 3) && (el.getAttribute("OriginalRequestID").equals(id) || el.getAttribute("RequestID").equals(id) || el.getAttribute("ResponseID").equals(id))) {
            els[3] = el;
        } else if ((elementIndex == 4) && (el.getAttribute("AssertionID").equals(id))) {
            els[4] = el;
        } else if ((elementIndex == 5) && (el.getAttribute("RequestID").equals(id) || el.getAttribute("ResponseID").equals(id))) {
            els[5] = el;
        }
        return 0;
    }
}
