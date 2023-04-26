package org.apache.cxf.transport.http_jetty;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.classloader.ClassLoaderUtils.ClassLoaderHolder;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.common.util.ReflectionUtil;
import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.security.CertificateConstraintsType;
import org.apache.cxf.continuations.ContinuationProvider;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CopyingOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.http_jetty.continuations.JettyContinuationProvider;
import org.apache.cxf.transport.https.CertConstraintsJaxBUtils;
import org.eclipse.jetty.http.Generator;
import org.eclipse.jetty.io.AbstractConnection;
import org.eclipse.jetty.server.AbstractHttpConnection.Output;
import org.eclipse.jetty.server.Request;
import org.springframework.util.ClassUtils;

public class JettyHTTPDestination extends AbstractHTTPDestination {

    private static final Logger LOG = LogUtils.getL7dLogger(JettyHTTPDestination.class);

    protected JettyHTTPServerEngine engine;

    protected JettyHTTPServerEngineFactory serverEngineFactory;

    protected ServletContext servletContext;

    protected URL nurl;

    protected ClassLoader loader;

    private boolean configFinalized;

    public JettyHTTPDestination(Bus bus, DestinationRegistry registry, EndpointInfo ei, JettyHTTPServerEngineFactory serverEngineFactory) throws IOException {
        super(bus, registry, ei, getAddressValue(ei, true).getAddress(), true);
        this.serverEngineFactory = serverEngineFactory;
        nurl = new URL(getAddress(endpointInfo));
        loader = bus.getExtension(ClassLoader.class);
    }

    protected Logger getLogger() {
        return LOG;
    }

    public void setServletContext(ServletContext sc) {
        servletContext = sc;
    }

    protected void retrieveEngine() throws GeneralSecurityException, IOException {
        engine = serverEngineFactory.retrieveJettyHTTPServerEngine(nurl.getPort());
        if (engine == null) {
            engine = serverEngineFactory.createJettyHTTPServerEngine(nurl.getHost(), nurl.getPort(), nurl.getProtocol());
        }
        assert engine != null;
        TLSServerParameters serverParameters = engine.getTlsServerParameters();
        if (serverParameters != null && serverParameters.getCertConstraints() != null) {
            CertificateConstraintsType constraints = serverParameters.getCertConstraints();
            if (constraints != null) {
                certConstraints = CertConstraintsJaxBUtils.createCertConstraints(constraints);
            }
        }
        if (!nurl.getProtocol().equals(engine.getProtocol())) {
            throw new IllegalStateException("Port " + engine.getPort() + " is configured with wrong protocol \"" + engine.getProtocol() + "\" for \"" + nurl + "\"");
        }
    }

    public void finalizeConfig() {
        assert !configFinalized;
        try {
            retrieveEngine();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        configFinalized = true;
    }

    protected String getAddress(EndpointInfo endpointInfo) {
        return endpointInfo.getAddress();
    }

    protected void activate() {
        super.activate();
        LOG.log(Level.FINE, "Activating receipt of incoming messages");
        URL url = null;
        try {
            url = new URL(getAddress(endpointInfo));
        } catch (Exception e) {
            throw new Fault(e);
        }
        JettyHTTPHandler jhd = createJettyHTTPHandler(this, contextMatchOnExact());
        engine.addServant(url, jhd);
    }

    protected JettyHTTPHandler createJettyHTTPHandler(JettyHTTPDestination jhd, boolean cmExact) {
        return new JettyHTTPHandler(jhd, cmExact);
    }

    protected void deactivate() {
        super.deactivate();
        LOG.log(Level.FINE, "Deactivating receipt of incoming messages");
        engine.removeServant(nurl);
    }

    protected String getBasePathForFullAddress(String addr) {
        try {
            return new URL(addr).getPath();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    protected void doService(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doService(servletContext, req, resp);
    }

    static AbstractConnection getConnectionForRequest(Request r) {
        try {
            return (AbstractConnection) r.getClass().getMethod("getConnection").invoke(r);
        } catch (Exception ex) {
            return null;
        }
    }

    private void setHeadFalse(AbstractConnection con) {
        try {
            Generator gen = (Generator) con.getClass().getMethod("getGenerator").invoke(con);
            gen.setHead(false);
        } catch (Exception ex) {
        }
    }

    protected void doService(ServletContext context, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (context == null) {
            context = servletContext;
        }
        Request baseRequest = (req instanceof Request) ? (Request) req : getCurrentRequest();
        if (!"HEAD".equals(req.getMethod())) {
            AbstractConnection c = getConnectionForRequest(baseRequest);
            if (c != null) {
                setHeadFalse(c);
            }
        }
        if (getServer().isSetRedirectURL()) {
            resp.sendRedirect(getServer().getRedirectURL());
            resp.flushBuffer();
            baseRequest.setHandled(true);
            return;
        }
        ClassLoaderHolder origLoader = null;
        Bus origBus = BusFactory.getAndSetThreadDefaultBus(bus);
        try {
            if (loader != null) {
                origLoader = ClassLoaderUtils.setThreadContextClassloader(loader);
            }
            invoke(null, context, req, resp);
        } finally {
            if (origBus != bus) {
                BusFactory.setThreadDefaultBus(origBus);
            }
            if (origLoader != null) {
                origLoader.reset();
            }
        }
    }

    protected void invokeComplete(final ServletContext context, final HttpServletRequest req, final HttpServletResponse resp, Message m) throws IOException {
        resp.flushBuffer();
        Request baseRequest = (req instanceof Request) ? (Request) req : getCurrentRequest();
        if (baseRequest != null) {
            baseRequest.setHandled(true);
        }
        super.invokeComplete(context, req, resp, m);
    }

    protected OutputStream flushHeaders(Message outMessage, boolean getStream) throws IOException {
        OutputStream out = super.flushHeaders(outMessage, getStream);
        return wrapOutput(out);
    }

    private OutputStream wrapOutput(OutputStream out) {
        try {
            if (out instanceof Output) {
                out = new JettyOutputStream((Output) out);
            }
        } catch (Throwable t) {
        }
        return out;
    }

    static class JettyOutputStream extends FilterOutputStream implements CopyingOutputStream {

        final Output out;

        boolean written;

        public JettyOutputStream(Output o) {
            super(o);
            out = o;
        }

        @Override
        public int copyFrom(InputStream in) throws IOException {
            if (written) {
                return IOUtils.copy(in, out);
            }
            CountingInputStream c = new CountingInputStream(in);
            out.sendContent(c);
            return c.getCount();
        }

        public void write(int b) throws IOException {
            written = true;
            out.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            written = true;
            out.write(b, off, len);
        }

        @Override
        public void close() throws IOException {
            out.close();
        }
    }

    static class CountingInputStream extends FilterInputStream {

        int count;

        public CountingInputStream(InputStream in) {
            super(in);
        }

        public int getCount() {
            return count;
        }

        @Override
        public int read() throws IOException {
            int i = super.read();
            if (i != -1) {
                ++count;
            }
            return i;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int i = super.read(b);
            if (i != -1) {
                count += i;
            }
            return i;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int i = super.read(b, off, len);
            if (i != -1) {
                count += i;
            }
            return i;
        }
    }

    public ServerEngine getEngine() {
        return engine;
    }

    protected Message retrieveFromContinuation(HttpServletRequest req) {
        return (Message) req.getAttribute(CXF_CONTINUATION_MESSAGE);
    }

    protected void setupContinuation(Message inMessage, final HttpServletRequest req, final HttpServletResponse resp) {
        if (engine.getContinuationsEnabled()) {
            inMessage.put(ContinuationProvider.class.getName(), new JettyContinuationProvider(req, resp, inMessage));
        }
    }

    private AbstractConnection getCurrentConnection() {
        Class<?> cls = null;
        try {
            cls = ClassUtils.forName("org.eclipse.jetty.server.AbstractHttpConnection", AbstractConnection.class.getClassLoader());
        } catch (Exception e) {
        }
        if (cls == null) {
            try {
                cls = ClassUtils.forName("org.eclipse.jetty.server.HttpConnection", AbstractConnection.class.getClassLoader());
            } catch (Exception e) {
            }
        }
        try {
            return (AbstractConnection) ReflectionUtil.setAccessible(cls.getMethod("getCurrentConnection")).invoke(null);
        } catch (Exception e) {
        }
        return null;
    }

    private Request getCurrentRequest() {
        AbstractConnection con = getCurrentConnection();
        try {
            return (Request) ReflectionUtil.setAccessible(con.getClass().getMethod("getRequest")).invoke(con);
        } catch (Exception e) {
        }
        return null;
    }
}