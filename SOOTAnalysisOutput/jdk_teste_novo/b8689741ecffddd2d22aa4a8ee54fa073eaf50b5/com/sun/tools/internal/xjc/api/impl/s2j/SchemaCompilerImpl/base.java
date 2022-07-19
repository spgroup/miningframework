package com.sun.tools.internal.xjc.api.impl.s2j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.SchemaFactory;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.SAXParseException2;
import com.sun.tools.internal.xjc.ErrorReceiver;
import com.sun.tools.internal.xjc.ModelLoader;
import com.sun.tools.internal.xjc.Options;
import com.sun.tools.internal.xjc.api.ClassNameAllocator;
import com.sun.tools.internal.xjc.api.ErrorListener;
import com.sun.tools.internal.xjc.api.SchemaCompiler;
import com.sun.tools.internal.xjc.api.SpecVersion;
import com.sun.tools.internal.xjc.model.Model;
import com.sun.tools.internal.xjc.outline.Outline;
import com.sun.tools.internal.xjc.reader.internalizer.DOMForest;
import com.sun.tools.internal.xjc.reader.internalizer.SCDBasedBindingSet;
import com.sun.tools.internal.xjc.reader.xmlschema.parser.LSInputSAXWrapper;
import com.sun.tools.internal.xjc.reader.xmlschema.parser.XMLSchemaInternalizationLogic;
import com.sun.xml.internal.bind.unmarshaller.DOMScanner;
import com.sun.xml.internal.bind.v2.util.XmlFactory;
import com.sun.xml.internal.xsom.XSSchemaSet;
import org.w3c.dom.Element;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

public final class SchemaCompilerImpl extends ErrorReceiver implements SchemaCompiler {

    private ErrorListener errorListener;

    protected final Options opts = new Options();

    @NotNull
    protected DOMForest forest;

    private boolean hadError;

    public SchemaCompilerImpl() {
        opts.compatibilityMode = Options.EXTENSION;
        resetSchema();
        if (System.getProperty("xjc-api.test") != null) {
            opts.debugMode = true;
            opts.verbose = true;
        }
    }

    @NotNull
    public Options getOptions() {
        return opts;
    }

    public ContentHandler getParserHandler(String systemId) {
        return forest.getParserHandler(systemId, true);
    }

    public void parseSchema(String systemId, Element element) {
        checkAbsoluteness(systemId);
        try {
            DOMScanner scanner = new DOMScanner();
            LocatorImpl loc = new LocatorImpl();
            loc.setSystemId(systemId);
            scanner.setLocator(loc);
            scanner.setContentHandler(getParserHandler(systemId));
            scanner.scan(element);
        } catch (SAXException e) {
            fatalError(new SAXParseException2(e.getMessage(), null, systemId, -1, -1, e));
        }
    }

    public void parseSchema(InputSource source) {
        checkAbsoluteness(source.getSystemId());
        try {
            forest.parse(source, true);
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public void setTargetVersion(SpecVersion version) {
        if (version == null)
            version = SpecVersion.LATEST;
        opts.target = version;
    }

    public void parseSchema(String systemId, XMLStreamReader reader) throws XMLStreamException {
        checkAbsoluteness(systemId);
        forest.parse(systemId, reader, true);
    }

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private void checkAbsoluteness(String systemId) {
        try {
            new URL(systemId);
        } catch (MalformedURLException mue) {
            try {
                new URI(systemId);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("system ID '" + systemId + "' isn't absolute", e);
            }
        }
    }

    public void setEntityResolver(EntityResolver entityResolver) {
        forest.setEntityResolver(entityResolver);
        opts.entityResolver = entityResolver;
    }

    public void setDefaultPackageName(String packageName) {
        opts.defaultPackage2 = packageName;
    }

    public void forcePackageName(String packageName) {
        opts.defaultPackage = packageName;
    }

    public void setClassNameAllocator(ClassNameAllocator allocator) {
        opts.classNameAllocator = allocator;
    }

    public void resetSchema() {
        forest = new DOMForest(new XMLSchemaInternalizationLogic(), opts);
        forest.setErrorHandler(this);
        forest.setEntityResolver(opts.entityResolver);
    }

    public JAXBModelImpl bind() {
        for (InputSource is : opts.getBindFiles()) parseSchema(is);
        SCDBasedBindingSet scdBasedBindingSet = forest.transform(opts.isExtensionMode());
        if (!NO_CORRECTNESS_CHECK) {
            SchemaFactory sf = XmlFactory.createSchemaFactory(XMLConstants.W3C_XML_SCHEMA_NS_URI, opts.disableXmlSecurity);
            if (opts.entityResolver != null) {
                sf.setResourceResolver(new LSResourceResolver() {

                    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
                        try {
                            InputSource is = opts.entityResolver.resolveEntity(namespaceURI, systemId);
                            if (is == null)
                                return null;
                            return new LSInputSAXWrapper(is);
                        } catch (SAXException e) {
                            return null;
                        } catch (IOException e) {
                            return null;
                        }
                    }
                });
            }
            sf.setErrorHandler(new DowngradingErrorHandler(this));
            forest.weakSchemaCorrectnessCheck(sf);
            if (hadError)
                return null;
        }
        JCodeModel codeModel = new JCodeModel();
        ModelLoader gl = new ModelLoader(opts, codeModel, this);
        try {
            XSSchemaSet result = gl.createXSOM(forest, scdBasedBindingSet);
            if (result == null)
                return null;
            Model model = gl.annotateXMLSchema(result);
            if (model == null)
                return null;
            if (hadError)
                return null;
            model.setPackageLevelAnnotations(opts.packageLevelAnnotations);
            Outline context = model.generateCode(opts, this);
            if (context == null)
                return null;
            if (hadError)
                return null;
            return new JAXBModelImpl(context);
        } catch (SAXException e) {
            return null;
        }
    }

    public void setErrorListener(ErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    public void info(SAXParseException exception) {
        if (errorListener != null)
            errorListener.info(exception);
    }

    public void warning(SAXParseException exception) {
        if (errorListener != null)
            errorListener.warning(exception);
    }

    public void error(SAXParseException exception) {
        hadError = true;
        if (errorListener != null)
            errorListener.error(exception);
    }

    public void fatalError(SAXParseException exception) {
        hadError = true;
        if (errorListener != null)
            errorListener.fatalError(exception);
    }

    private static boolean NO_CORRECTNESS_CHECK = false;

    static {
        try {
            NO_CORRECTNESS_CHECK = Boolean.getBoolean(SchemaCompilerImpl.class.getName() + ".noCorrectnessCheck");
        } catch (Throwable t) {
        }
    }
}
