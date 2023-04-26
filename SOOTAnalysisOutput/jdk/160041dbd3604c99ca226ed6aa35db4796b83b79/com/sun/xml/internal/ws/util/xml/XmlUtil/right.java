package com.sun.xml.internal.ws.util.xml;

import com.sun.istack.internal.Nullable;
import com.sun.org.apache.xml.internal.resolver.Catalog;
import com.sun.org.apache.xml.internal.resolver.CatalogManager;
import com.sun.org.apache.xml.internal.resolver.tools.CatalogResolver;
import com.sun.xml.internal.ws.server.ServerRtException;
import com.sun.xml.internal.ws.util.ByteArrayBuffer;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.*;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.ws.WebServiceException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XmlUtil {

    private static final String ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema";

    private final static String LEXICAL_HANDLER_PROPERTY = "http://xml.org/sax/properties/lexical-handler";

    private static final Logger LOGGER = Logger.getLogger(XmlUtil.class.getName());

    private static boolean XML_SECURITY_DISABLED;

    static {
        String disableXmlSecurity = System.getProperty("com.sun.xml.internal.ws.disableXmlSecurity");
        XML_SECURITY_DISABLED = disableXmlSecurity == null || !Boolean.valueOf(disableXmlSecurity);
    }

    public static String getPrefix(String s) {
        int i = s.indexOf(':');
        if (i == -1)
            return null;
        return s.substring(0, i);
    }

    public static String getLocalPart(String s) {
        int i = s.indexOf(':');
        if (i == -1)
            return s;
        return s.substring(i + 1);
    }

    public static String getAttributeOrNull(Element e, String name) {
        Attr a = e.getAttributeNode(name);
        if (a == null)
            return null;
        return a.getValue();
    }

    public static String getAttributeNSOrNull(Element e, String name, String nsURI) {
        Attr a = e.getAttributeNodeNS(nsURI, name);
        if (a == null)
            return null;
        return a.getValue();
    }

    public static String getAttributeNSOrNull(Element e, QName name) {
        Attr a = e.getAttributeNodeNS(name.getNamespaceURI(), name.getLocalPart());
        if (a == null)
            return null;
        return a.getValue();
    }

    public static Iterator getAllChildren(Element element) {
        return new NodeListIterator(element.getChildNodes());
    }

    public static Iterator getAllAttributes(Element element) {
        return new NamedNodeMapIterator(element.getAttributes());
    }

    public static List<String> parseTokenList(String tokenList) {
        List<String> result = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(tokenList, " ");
        while (tokenizer.hasMoreTokens()) {
            result.add(tokenizer.nextToken());
        }
        return result;
    }

    public static String getTextForNode(Node node) {
        StringBuilder sb = new StringBuilder();
        NodeList children = node.getChildNodes();
        if (children.getLength() == 0)
            return null;
        for (int i = 0; i < children.getLength(); ++i) {
            Node n = children.item(i);
            if (n instanceof Text)
                sb.append(n.getNodeValue());
            else if (n instanceof EntityReference) {
                String s = getTextForNode(n);
                if (s == null)
                    return null;
                else
                    sb.append(s);
            } else
                return null;
        }
        return sb.toString();
    }

    public static InputStream getUTF8Stream(String s) {
        try {
            ByteArrayBuffer bab = new ByteArrayBuffer();
            Writer w = new OutputStreamWriter(bab, "utf-8");
            w.write(s);
            w.close();
            return bab.newInputStream();
        } catch (IOException e) {
            throw new RuntimeException("should not happen");
        }
    }

    static final TransformerFactory transformerFactory = newTransformerFactory();

    static final SAXParserFactory saxParserFactory = newSAXParserFactory(true);

    static {
        saxParserFactory.setNamespaceAware(true);
    }

    public static Transformer newTransformer() {
        try {
            return transformerFactory.newTransformer();
        } catch (TransformerConfigurationException tex) {
            throw new IllegalStateException("Unable to create a JAXP transformer");
        }
    }

    public static <T extends Result> T identityTransform(Source src, T result) throws TransformerException, SAXException, ParserConfigurationException, IOException {
        if (src instanceof StreamSource) {
            StreamSource ssrc = (StreamSource) src;
            TransformerHandler th = ((SAXTransformerFactory) transformerFactory).newTransformerHandler();
            th.setResult(result);
            XMLReader reader = saxParserFactory.newSAXParser().getXMLReader();
            reader.setContentHandler(th);
            reader.setProperty(LEXICAL_HANDLER_PROPERTY, th);
            reader.parse(toInputSource(ssrc));
        } else {
            newTransformer().transform(src, result);
        }
        return result;
    }

    private static InputSource toInputSource(StreamSource src) {
        InputSource is = new InputSource();
        is.setByteStream(src.getInputStream());
        is.setCharacterStream(src.getReader());
        is.setPublicId(src.getPublicId());
        is.setSystemId(src.getSystemId());
        return is;
    }

    public static EntityResolver createEntityResolver(@Nullable URL catalogUrl) {
        CatalogManager manager = new CatalogManager();
        manager.setIgnoreMissingProperties(true);
        manager.setUseStaticCatalog(false);
        Catalog catalog = manager.getCatalog();
        try {
            if (catalogUrl != null) {
                catalog.parseCatalog(catalogUrl);
            }
        } catch (IOException e) {
            throw new ServerRtException("server.rt.err", e);
        }
        return workaroundCatalogResolver(catalog);
    }

    public static EntityResolver createDefaultCatalogResolver() {
        CatalogManager manager = new CatalogManager();
        manager.setIgnoreMissingProperties(true);
        manager.setUseStaticCatalog(false);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> catalogEnum;
        Catalog catalog = manager.getCatalog();
        try {
            if (cl == null) {
                catalogEnum = ClassLoader.getSystemResources("META-INF/jax-ws-catalog.xml");
            } else {
                catalogEnum = cl.getResources("META-INF/jax-ws-catalog.xml");
            }
            while (catalogEnum.hasMoreElements()) {
                URL url = catalogEnum.nextElement();
                catalog.parseCatalog(url);
            }
        } catch (IOException e) {
            throw new WebServiceException(e);
        }
        return workaroundCatalogResolver(catalog);
    }

    private static CatalogResolver workaroundCatalogResolver(final Catalog catalog) {
        CatalogManager manager = new CatalogManager() {

            @Override
            public Catalog getCatalog() {
                return catalog;
            }
        };
        manager.setIgnoreMissingProperties(true);
        manager.setUseStaticCatalog(false);
        return new CatalogResolver(manager);
    }

    public static final ErrorHandler DRACONIAN_ERROR_HANDLER = new ErrorHandler() {

        @Override
        public void warning(SAXParseException exception) {
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }
    };

    public static DocumentBuilderFactory newDocumentBuilderFactory() {
        return newDocumentBuilderFactory(true);
    }

    public static DocumentBuilderFactory newDocumentBuilderFactory(boolean secureXmlProcessing) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, isXMLSecurityDisabled(secureXmlProcessing));
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.WARNING, "Factory [{0}] doesn't support secure xml processing!", new Object[] { factory.getClass().getName() });
        }
        return factory;
    }

    public static TransformerFactory newTransformerFactory(boolean secureXmlProcessingEnabled) {
        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, isXMLSecurityDisabled(secureXmlProcessingEnabled));
        } catch (TransformerConfigurationException e) {
            LOGGER.log(Level.WARNING, "Factory [{0}] doesn't support secure xml processing!", new Object[] { factory.getClass().getName() });
        }
        return factory;
    }

    public static TransformerFactory newTransformerFactory() {
        return newTransformerFactory(true);
    }

    public static SAXParserFactory newSAXParserFactory(boolean secureXmlProcessingEnabled) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, isXMLSecurityDisabled(secureXmlProcessingEnabled));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Factory [{0}] doesn't support secure xml processing!", new Object[] { factory.getClass().getName() });
        }
        return factory;
    }

    public static XPathFactory newXPathFactory(boolean secureXmlProcessingEnabled) {
        XPathFactory factory = XPathFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, isXMLSecurityDisabled(secureXmlProcessingEnabled));
        } catch (XPathFactoryConfigurationException e) {
            LOGGER.log(Level.WARNING, "Factory [{0}] doesn't support secure xml processing!", new Object[] { factory.getClass().getName() });
        }
        return factory;
    }

    public static XMLInputFactory newXMLInputFactory(boolean secureXmlProcessingEnabled) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        if (isXMLSecurityDisabled(secureXmlProcessingEnabled)) {
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        }
        return factory;
    }

    private static boolean isXMLSecurityDisabled(boolean runtimeDisabled) {
        return XML_SECURITY_DISABLED || runtimeDisabled;
    }

    public static SchemaFactory allowExternalAccess(SchemaFactory sf, String value, boolean disableSecureProcessing) {
        if (isXMLSecurityDisabled(disableSecureProcessing)) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Xml Security disabled, no JAXP xsd external access configuration necessary.");
            }
            return sf;
        }
        if (System.getProperty("javax.xml.accessExternalSchema") != null) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Detected explicitly JAXP configuration, no JAXP xsd external access configuration necessary.");
            }
            return sf;
        }
        try {
            sf.setProperty(ACCESS_EXTERNAL_SCHEMA, value);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Property \"{0}\" is supported and has been successfully set by used JAXP implementation.", new Object[] { ACCESS_EXTERNAL_SCHEMA });
            }
        } catch (SAXException ignored) {
            if (LOGGER.isLoggable(Level.CONFIG)) {
                LOGGER.log(Level.CONFIG, "Property \"{0}\" is not supported by used JAXP implementation.", new Object[] { ACCESS_EXTERNAL_SCHEMA });
            }
        }
        return sf;
    }
}
