package com.sun.xml.internal.ws.api.streaming;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import com.sun.xml.internal.ws.streaming.XMLReaderException;
import com.sun.xml.internal.ws.util.xml.XmlUtil;
import org.xml.sax.InputSource;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("StaticNonFinalUsedInInitialization")
public abstract class XMLStreamReaderFactory {

    private static final Logger LOGGER = Logger.getLogger(XMLStreamReaderFactory.class.getName());

    @NotNull
    private static volatile XMLStreamReaderFactory theInstance;

    static {
        XMLInputFactory xif = getXMLInputFactory();
        XMLStreamReaderFactory f = null;
        if (!getProperty(XMLStreamReaderFactory.class.getName() + ".noPool")) {
            f = Zephyr.newInstance(xif);
        }
        if (f == null) {
            if (xif.getClass().getName().equals("com.ctc.wstx.stax.WstxInputFactory")) {
                f = new Woodstox(xif);
            }
        }
        if (f == null) {
            f = new Default();
        }
        theInstance = f;
        LOGGER.log(Level.FINE, "XMLStreamReaderFactory instance is = {0}", theInstance);
    }

    private static XMLInputFactory getXMLInputFactory() {
        XMLInputFactory xif = null;
        if (getProperty(XMLStreamReaderFactory.class.getName() + ".woodstox")) {
            try {
                xif = (XMLInputFactory) Class.forName("com.ctc.wstx.stax.WstxInputFactory").newInstance();
            } catch (Exception e) {
            }
        }
        if (xif == null) {
            xif = XmlUtil.newXMLInputFactory(true);
        }
        xif.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        xif.setProperty(XMLInputFactory.IS_COALESCING, true);
        return xif;
    }

    public static void set(XMLStreamReaderFactory f) {
        if (f == null) {
            throw new IllegalArgumentException();
        }
        theInstance = f;
    }

    public static XMLStreamReaderFactory get() {
        return theInstance;
    }

    public static XMLStreamReader create(InputSource source, boolean rejectDTDs) {
        try {
            if (source.getCharacterStream() != null) {
                return get().doCreate(source.getSystemId(), source.getCharacterStream(), rejectDTDs);
            }
            if (source.getByteStream() != null) {
                return get().doCreate(source.getSystemId(), source.getByteStream(), rejectDTDs);
            }
            return get().doCreate(source.getSystemId(), new URL(source.getSystemId()).openStream(), rejectDTDs);
        } catch (IOException e) {
            throw new XMLReaderException("stax.cantCreate", e);
        }
    }

    public static XMLStreamReader create(@Nullable String systemId, InputStream in, boolean rejectDTDs) {
        return get().doCreate(systemId, in, rejectDTDs);
    }

    public static XMLStreamReader create(@Nullable String systemId, InputStream in, @Nullable String encoding, boolean rejectDTDs) {
        return (encoding == null) ? create(systemId, in, rejectDTDs) : get().doCreate(systemId, in, encoding, rejectDTDs);
    }

    public static XMLStreamReader create(@Nullable String systemId, Reader reader, boolean rejectDTDs) {
        return get().doCreate(systemId, reader, rejectDTDs);
    }

    public static void recycle(XMLStreamReader r) {
        get().doRecycle(r);
        if (r instanceof RecycleAware) {
            ((RecycleAware) r).onRecycled();
        }
    }

    public abstract XMLStreamReader doCreate(String systemId, InputStream in, boolean rejectDTDs);

    private XMLStreamReader doCreate(String systemId, InputStream in, @NotNull String encoding, boolean rejectDTDs) {
        Reader reader;
        try {
            reader = new InputStreamReader(in, encoding);
        } catch (UnsupportedEncodingException ue) {
            throw new XMLReaderException("stax.cantCreate", ue);
        }
        return doCreate(systemId, reader, rejectDTDs);
    }

    public abstract XMLStreamReader doCreate(String systemId, Reader reader, boolean rejectDTDs);

    public abstract void doRecycle(XMLStreamReader r);

    public interface RecycleAware {

        void onRecycled();
    }

    private static final class Zephyr extends XMLStreamReaderFactory {

        private final XMLInputFactory xif;

        private final ThreadLocal<XMLStreamReader> pool = new ThreadLocal<XMLStreamReader>();

        private final Method setInputSourceMethod;

        private final Method resetMethod;

        private final Class zephyrClass;

        @Nullable
        public static XMLStreamReaderFactory newInstance(XMLInputFactory xif) {
            try {
                Class<?> clazz = xif.createXMLStreamReader(new StringReader("<foo/>")).getClass();
                if (!(clazz.getName().startsWith("com.sun.xml.internal.stream.")))
                    return null;
                return new Zephyr(xif, clazz);
            } catch (NoSuchMethodException e) {
                return null;
            } catch (XMLStreamException e) {
                return null;
            }
        }

        public Zephyr(XMLInputFactory xif, Class clazz) throws NoSuchMethodException {
            zephyrClass = clazz;
            setInputSourceMethod = clazz.getMethod("setInputSource", InputSource.class);
            resetMethod = clazz.getMethod("reset");
            try {
                xif.setProperty("reuse-instance", false);
            } catch (IllegalArgumentException e) {
            }
            this.xif = xif;
        }

        @Nullable
        private XMLStreamReader fetch() {
            XMLStreamReader sr = pool.get();
            if (sr == null)
                return null;
            pool.set(null);
            return sr;
        }

        public void doRecycle(XMLStreamReader r) {
            if (zephyrClass.isInstance(r))
                pool.set(r);
        }

        public XMLStreamReader doCreate(String systemId, InputStream in, boolean rejectDTDs) {
            try {
                XMLStreamReader xsr = fetch();
                if (xsr == null)
                    return xif.createXMLStreamReader(systemId, in);
                InputSource is = new InputSource(systemId);
                is.setByteStream(in);
                reuse(xsr, is);
                return xsr;
            } catch (IllegalAccessException e) {
                throw new XMLReaderException("stax.cantCreate", e);
            } catch (InvocationTargetException e) {
                throw new XMLReaderException("stax.cantCreate", e);
            } catch (XMLStreamException e) {
                throw new XMLReaderException("stax.cantCreate", e);
            }
        }

        public XMLStreamReader doCreate(String systemId, Reader in, boolean rejectDTDs) {
            try {
                XMLStreamReader xsr = fetch();
                if (xsr == null)
                    return xif.createXMLStreamReader(systemId, in);
                InputSource is = new InputSource(systemId);
                is.setCharacterStream(in);
                reuse(xsr, is);
                return xsr;
            } catch (IllegalAccessException e) {
                throw new XMLReaderException("stax.cantCreate", e);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause == null) {
                    cause = e;
                }
                throw new XMLReaderException("stax.cantCreate", cause);
            } catch (XMLStreamException e) {
                throw new XMLReaderException("stax.cantCreate", e);
            }
        }

        private void reuse(XMLStreamReader xsr, InputSource in) throws IllegalAccessException, InvocationTargetException {
            resetMethod.invoke(xsr);
            setInputSourceMethod.invoke(xsr, in);
        }
    }

    public static final class Default extends XMLStreamReaderFactory {

        private final ThreadLocal<XMLInputFactory> xif = new ThreadLocal<XMLInputFactory>() {

            @Override
            public XMLInputFactory initialValue() {
                return getXMLInputFactory();
            }
        };

        public XMLStreamReader doCreate(String systemId, InputStream in, boolean rejectDTDs) {
            try {
                return xif.get().createXMLStreamReader(systemId, in);
            } catch (XMLStreamException e) {
                throw new XMLReaderException("stax.cantCreate", e);
            }
        }

        public XMLStreamReader doCreate(String systemId, Reader in, boolean rejectDTDs) {
            try {
                return xif.get().createXMLStreamReader(systemId, in);
            } catch (XMLStreamException e) {
                throw new XMLReaderException("stax.cantCreate", e);
            }
        }

        public void doRecycle(XMLStreamReader r) {
        }
    }

    public static class NoLock extends XMLStreamReaderFactory {

        private final XMLInputFactory xif;

        public NoLock(XMLInputFactory xif) {
            this.xif = xif;
        }

        public XMLStreamReader doCreate(String systemId, InputStream in, boolean rejectDTDs) {
            try {
                return xif.createXMLStreamReader(systemId, in);
            } catch (XMLStreamException e) {
                throw new XMLReaderException("stax.cantCreate", e);
            }
        }

        public XMLStreamReader doCreate(String systemId, Reader in, boolean rejectDTDs) {
            try {
                return xif.createXMLStreamReader(systemId, in);
            } catch (XMLStreamException e) {
                throw new XMLReaderException("stax.cantCreate", e);
            }
        }

        public void doRecycle(XMLStreamReader r) {
        }
    }

    public static final class Woodstox extends NoLock {

        public Woodstox(XMLInputFactory xif) {
            super(xif);
            xif.setProperty("org.codehaus.stax2.internNsUris", true);
        }

        public XMLStreamReader doCreate(String systemId, InputStream in, boolean rejectDTDs) {
            return super.doCreate(systemId, in, rejectDTDs);
        }

        public XMLStreamReader doCreate(String systemId, Reader in, boolean rejectDTDs) {
            return super.doCreate(systemId, in, rejectDTDs);
        }
    }

    private static Boolean getProperty(final String prop) {
        return AccessController.doPrivileged(new java.security.PrivilegedAction<Boolean>() {

            public Boolean run() {
                String value = System.getProperty(prop);
                return value != null ? Boolean.valueOf(value) : Boolean.FALSE;
            }
        });
    }
}