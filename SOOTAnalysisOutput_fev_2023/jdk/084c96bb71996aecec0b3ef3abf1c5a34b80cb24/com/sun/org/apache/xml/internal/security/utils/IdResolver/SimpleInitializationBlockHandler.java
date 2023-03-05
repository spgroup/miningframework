package com.sun.org.apache.xml.internal.security.utils;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Deprecated
public class IdResolver {

    private IdResolver() {
    }

    public static void registerElementById(Element element, Attr id) {
        element.setIdAttributeNode(id, true);
    }

    public static Element getElementById(Document doc, String id) {
        return doc.getElementById(id);
    }
}