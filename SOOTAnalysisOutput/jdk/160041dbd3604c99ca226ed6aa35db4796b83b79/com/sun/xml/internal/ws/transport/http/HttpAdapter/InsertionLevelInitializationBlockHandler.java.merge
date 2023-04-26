package com.sun.xml.internal.ws.transport.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.ws.Binding;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPBinding;
import com.oracle.webservices.internal.api.message.PropertySet;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import com.sun.xml.internal.ws.api.Component;
import com.sun.xml.internal.ws.api.EndpointAddress;
import com.sun.xml.internal.ws.api.SOAPVersion;
import com.sun.xml.internal.ws.api.addressing.AddressingVersion;
import com.sun.xml.internal.ws.api.addressing.NonAnonymousResponseProcessor;
import com.sun.xml.internal.ws.api.ha.HaInfo;
import com.sun.xml.internal.ws.api.message.ExceptionHasMessage;
import com.sun.xml.internal.ws.api.message.Message;
import com.sun.xml.internal.ws.api.message.Packet;
import com.sun.xml.internal.ws.api.pipe.Codec;
import com.sun.xml.internal.ws.api.pipe.ContentType;
import com.sun.xml.internal.ws.api.server.AbstractServerAsyncTransport;
import com.sun.xml.internal.ws.api.server.Adapter;
import com.sun.xml.internal.ws.api.server.BoundEndpoint;
import com.sun.xml.internal.ws.api.server.DocumentAddressResolver;
import com.sun.xml.internal.ws.api.server.Module;
import com.sun.xml.internal.ws.api.server.PortAddressResolver;
import com.sun.xml.internal.ws.api.server.SDDocument;
import com.sun.xml.internal.ws.api.server.ServiceDefinition;
import com.sun.xml.internal.ws.api.server.TransportBackChannel;
import com.sun.xml.internal.ws.api.server.WSEndpoint;
import com.sun.xml.internal.ws.api.server.WebServiceContextDelegate;
import com.sun.xml.internal.ws.fault.SOAPFaultBuilder;
import com.sun.xml.internal.ws.resources.WsservletMessages;
import com.sun.xml.internal.ws.server.UnsupportedMediaException;
import com.sun.xml.internal.ws.util.ByteArrayBuffer;
import com.sun.xml.internal.ws.util.Pool;

public class HttpAdapter extends Adapter<HttpAdapter.HttpToolkit> {

    private static final Logger LOGGER = Logger.getLogger(HttpAdapter.class.getName());

    protected Map<String, SDDocument> wsdls;

    private Map<SDDocument, String> revWsdls;

    private ServiceDefinition serviceDefinition = null;

    public final HttpAdapterList<? extends HttpAdapter> owner;

    public final String urlPattern;

    protected boolean stickyCookie;

    protected boolean disableJreplicaCookie = false;

    public static HttpAdapter createAlone(WSEndpoint endpoint) {
        return new DummyList().createAdapter("", "", endpoint);
    }

    protected HttpAdapter(WSEndpoint endpoint, HttpAdapterList<? extends HttpAdapter> owner) {
        this(endpoint, owner, null);
    }

    protected HttpAdapter(WSEndpoint endpoint, HttpAdapterList<? extends HttpAdapter> owner, String urlPattern) {
        super(endpoint);
        this.owner = owner;
        this.urlPattern = urlPattern;
        initWSDLMap(endpoint.getServiceDefinition());
    }

    public ServiceDefinition getServiceDefinition() {
        return this.serviceDefinition;
    }

    public final void initWSDLMap(ServiceDefinition sdef) {
        this.serviceDefinition = sdef;
        if (sdef == null) {
            wsdls = Collections.emptyMap();
            revWsdls = Collections.emptyMap();
        } else {
            wsdls = new HashMap<String, SDDocument>();
            Map<String, SDDocument> systemIds = new TreeMap<String, SDDocument>();
            for (SDDocument sdd : sdef) {
                if (sdd == sdef.getPrimary()) {
                    wsdls.put("wsdl", sdd);
                    wsdls.put("WSDL", sdd);
                } else {
                    systemIds.put(sdd.getURL().toString(), sdd);
                }
            }
            int wsdlnum = 1;
            int xsdnum = 1;
            for (Entry<String, SDDocument> e : systemIds.entrySet()) {
                SDDocument sdd = e.getValue();
                if (sdd.isWSDL()) {
                    wsdls.put("wsdl=" + (wsdlnum++), sdd);
                }
                if (sdd.isSchema()) {
                    wsdls.put("xsd=" + (xsdnum++), sdd);
                }
            }
            revWsdls = new HashMap<SDDocument, String>();
            for (Entry<String, SDDocument> e : wsdls.entrySet()) {
                if (!e.getKey().equals("WSDL")) {
                    revWsdls.put(e.getValue(), e.getKey());
                }
            }
        }
    }

    public String getValidPath() {
        if (urlPattern.endsWith("/*")) {
            return urlPattern.substring(0, urlPattern.length() - 2);
        } else {
            return urlPattern;
        }
    }

    @Override
    protected HttpToolkit createToolkit() {
        return new HttpToolkit();
    }

    public void handle(@NotNull WSHTTPConnection connection) throws IOException {
        if (handleGet(connection)) {
            return;
        }
        final Pool<HttpToolkit> currentPool = getPool();
        final HttpToolkit tk = currentPool.take();
        try {
            tk.handle(connection);
        } finally {
            currentPool.recycle(tk);
        }
    }

    public boolean handleGet(@NotNull WSHTTPConnection connection) throws IOException {
        if (connection.getRequestMethod().equals("GET")) {
            for (Component c : endpoint.getComponents()) {
                HttpMetadataPublisher spi = c.getSPI(HttpMetadataPublisher.class);
                if (spi != null && spi.handleMetadataRequest(this, connection)) {
                    return true;
                }
            }
            if (isMetadataQuery(connection.getQueryString())) {
                publishWSDL(connection);
                return true;
            }
            Binding binding = getEndpoint().getBinding();
            if (!(binding instanceof HTTPBinding)) {
                writeWebServicesHtmlPage(connection);
                return true;
            }
        } else if (connection.getRequestMethod().equals("HEAD")) {
            connection.getInput().close();
            Binding binding = getEndpoint().getBinding();
            if (isMetadataQuery(connection.getQueryString())) {
                SDDocument doc = wsdls.get(connection.getQueryString());
                connection.setStatus(doc != null ? HttpURLConnection.HTTP_OK : HttpURLConnection.HTTP_NOT_FOUND);
                connection.getOutput().close();
                connection.close();
                return true;
            } else if (!(binding instanceof HTTPBinding)) {
                connection.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
                connection.getOutput().close();
                connection.close();
                return true;
            }
        }
        return false;
    }

    private Packet decodePacket(@NotNull WSHTTPConnection con, @NotNull Codec codec) throws IOException {
        String ct = con.getRequestHeader("Content-Type");
        InputStream in = con.getInput();
        Packet packet = new Packet();
        packet.soapAction = fixQuotesAroundSoapAction(con.getRequestHeader("SOAPAction"));
        packet.wasTransportSecure = con.isSecure();
        packet.acceptableMimeTypes = con.getRequestHeader("Accept");
        packet.addSatellite(con);
        addSatellites(packet);
        packet.isAdapterDeliversNonAnonymousResponse = true;
        packet.component = this;
        packet.transportBackChannel = new Oneway(con);
        packet.webServiceContextDelegate = con.getWebServiceContextDelegate();
        packet.setState(Packet.State.ServerRequest);
        if (dump || LOGGER.isLoggable(Level.FINER)) {
            ByteArrayBuffer buf = new ByteArrayBuffer();
            buf.write(in);
            in.close();
            dump(buf, "HTTP request", con.getRequestHeaders());
            in = buf.newInputStream();
        }
        codec.decode(in, ct, packet);
        return packet;
    }

    protected void addSatellites(Packet packet) {
    }

    static public String fixQuotesAroundSoapAction(String soapAction) {
        if (soapAction != null && (!soapAction.startsWith("\"") || !soapAction.endsWith("\""))) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "Received WS-I BP non-conformant Unquoted SoapAction HTTP header: {0}", soapAction);
            }
            String fixedSoapAction = soapAction;
            if (!soapAction.startsWith("\"")) {
                fixedSoapAction = "\"" + fixedSoapAction;
            }
            if (!soapAction.endsWith("\"")) {
                fixedSoapAction = fixedSoapAction + "\"";
            }
            return fixedSoapAction;
        }
        return soapAction;
    }

    protected NonAnonymousResponseProcessor getNonAnonymousResponseProcessor() {
        return NonAnonymousResponseProcessor.getDefault();
    }

    protected void writeClientError(int connStatus, @NotNull OutputStream os, @NotNull Packet packet) throws IOException {
    }

    private boolean isClientErrorStatus(int connStatus) {
        return (connStatus == HttpURLConnection.HTTP_FORBIDDEN);
    }

    private boolean isNonAnonymousUri(EndpointAddress addr) {
        return (addr != null) && !addr.toString().equals(AddressingVersion.W3C.anonymousUri) && !addr.toString().equals(AddressingVersion.MEMBER.anonymousUri);
    }

    private void encodePacket(@NotNull Packet packet, @NotNull WSHTTPConnection con, @NotNull Codec codec) throws IOException {
        if (isNonAnonymousUri(packet.endpointAddress) && packet.getMessage() != null) {
            try {
                packet = getNonAnonymousResponseProcessor().process(packet);
            } catch (RuntimeException re) {
                SOAPVersion soapVersion = packet.getBinding().getSOAPVersion();
                Message faultMsg = SOAPFaultBuilder.createSOAPFaultMessage(soapVersion, null, re);
                packet = packet.createServerResponse(faultMsg, packet.endpoint.getPort(), null, packet.endpoint.getBinding());
            }
        }
        if (con.isClosed()) {
            return;
        }
        Message responseMessage = packet.getMessage();
        addStickyCookie(con);
        addReplicaCookie(con, packet);
        if (responseMessage == null) {
            if (!con.isClosed()) {
                if (con.getStatus() == 0) {
                    con.setStatus(WSHTTPConnection.ONEWAY);
                }
                OutputStream os = con.getProtocol().contains("1.1") ? con.getOutput() : new Http10OutputStream(con);
                if (dump || LOGGER.isLoggable(Level.FINER)) {
                    ByteArrayBuffer buf = new ByteArrayBuffer();
                    codec.encode(packet, buf);
                    dump(buf, "HTTP response " + con.getStatus(), con.getResponseHeaders());
                    buf.writeTo(os);
                } else {
                    codec.encode(packet, os);
                }
                try {
                    os.close();
                } catch (IOException e) {
                    throw new WebServiceException(e);
                }
            }
        } else {
            if (con.getStatus() == 0) {
                con.setStatus(responseMessage.isFault() ? HttpURLConnection.HTTP_INTERNAL_ERROR : HttpURLConnection.HTTP_OK);
            }
            if (isClientErrorStatus(con.getStatus())) {
                OutputStream os = con.getOutput();
                if (dump || LOGGER.isLoggable(Level.FINER)) {
                    ByteArrayBuffer buf = new ByteArrayBuffer();
                    writeClientError(con.getStatus(), buf, packet);
                    dump(buf, "HTTP response " + con.getStatus(), con.getResponseHeaders());
                    buf.writeTo(os);
                } else {
                    writeClientError(con.getStatus(), os, packet);
                }
                os.close();
                return;
            }
            ContentType contentType = codec.getStaticContentType(packet);
            if (contentType != null) {
                con.setContentTypeResponseHeader(contentType.getContentType());
                OutputStream os = con.getProtocol().contains("1.1") ? con.getOutput() : new Http10OutputStream(con);
                if (dump || LOGGER.isLoggable(Level.FINER)) {
                    ByteArrayBuffer buf = new ByteArrayBuffer();
                    codec.encode(packet, buf);
                    dump(buf, "HTTP response " + con.getStatus(), con.getResponseHeaders());
                    buf.writeTo(os);
                } else {
                    codec.encode(packet, os);
                }
                os.close();
            } else {
                ByteArrayBuffer buf = new ByteArrayBuffer();
                contentType = codec.encode(packet, buf);
                con.setContentTypeResponseHeader(contentType.getContentType());
                if (dump || LOGGER.isLoggable(Level.FINER)) {
                    dump(buf, "HTTP response " + con.getStatus(), con.getResponseHeaders());
                }
                OutputStream os = con.getOutput();
                buf.writeTo(os);
                os.close();
            }
        }
    }

    private void addStickyCookie(WSHTTPConnection con) {
        if (stickyCookie) {
            String proxyJroute = con.getRequestHeader("proxy-jroute");
            if (proxyJroute == null) {
                return;
            }
            String jrouteId = con.getCookie("JROUTE");
            if (jrouteId == null || !jrouteId.equals(proxyJroute)) {
                con.setCookie("JROUTE", proxyJroute);
            }
        }
    }

    private void addReplicaCookie(WSHTTPConnection con, Packet packet) {
        if (stickyCookie) {
            HaInfo haInfo = null;
            if (packet.supports(Packet.HA_INFO)) {
                haInfo = (HaInfo) packet.get(Packet.HA_INFO);
            }
            if (haInfo != null) {
                con.setCookie("METRO_KEY", haInfo.getKey());
                if (!disableJreplicaCookie) {
                    con.setCookie("JREPLICA", haInfo.getReplicaInstance());
                }
            }
        }
    }

    public void invokeAsync(final WSHTTPConnection con) throws IOException {
        invokeAsync(con, NO_OP_COMPLETION_CALLBACK);
    }

    public void invokeAsync(final WSHTTPConnection con, final CompletionCallback callback) throws IOException {
        if (handleGet(con)) {
            callback.onCompletion();
            return;
        }
        final Pool<HttpToolkit> currentPool = getPool();
        final HttpToolkit tk = currentPool.take();
        final Packet request;
        try {
            request = decodePacket(con, tk.codec);
        } catch (ExceptionHasMessage e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            Packet response = new Packet();
            response.setMessage(e.getFaultMessage());
            encodePacket(response, con, tk.codec);
            currentPool.recycle(tk);
            con.close();
            callback.onCompletion();
            return;
        } catch (UnsupportedMediaException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            Packet response = new Packet();
            con.setStatus(WSHTTPConnection.UNSUPPORTED_MEDIA);
            encodePacket(response, con, tk.codec);
            currentPool.recycle(tk);
            con.close();
            callback.onCompletion();
            return;
        }
        endpoint.process(request, new WSEndpoint.CompletionCallback() {

            @Override
            public void onCompletion(@NotNull Packet response) {
                try {
                    try {
                        encodePacket(response, con, tk.codec);
                    } catch (IOException ioe) {
                        LOGGER.log(Level.SEVERE, ioe.getMessage(), ioe);
                    }
                    currentPool.recycle(tk);
                } finally {
                    con.close();
                    callback.onCompletion();
                }
            }
        }, null);
    }

    public static final CompletionCallback NO_OP_COMPLETION_CALLBACK = new CompletionCallback() {

        @Override
        public void onCompletion() {
        }
    };

    public interface CompletionCallback {

        void onCompletion();
    }

    final class AsyncTransport extends AbstractServerAsyncTransport<WSHTTPConnection> {

        public AsyncTransport() {
            super(endpoint);
        }

        public void handleAsync(WSHTTPConnection con) throws IOException {
            super.handle(con);
        }

        @Override
        protected void encodePacket(WSHTTPConnection con, @NotNull Packet packet, @NotNull Codec codec) throws IOException {
            HttpAdapter.this.encodePacket(packet, con, codec);
        }

        @Override
        @Nullable
        protected String getAcceptableMimeTypes(WSHTTPConnection con) {
            return null;
        }

        @Override
        @Nullable
        protected TransportBackChannel getTransportBackChannel(WSHTTPConnection con) {
            return new Oneway(con);
        }

        @Override
        @NotNull
        protected PropertySet getPropertySet(WSHTTPConnection con) {
            return con;
        }

        @Override
        @NotNull
        protected WebServiceContextDelegate getWebServiceContextDelegate(WSHTTPConnection con) {
            return con.getWebServiceContextDelegate();
        }
    }

    static final class Oneway implements TransportBackChannel {

        WSHTTPConnection con;

        boolean closed;

        Oneway(WSHTTPConnection con) {
            this.con = con;
        }

        @Override
        public void close() {
            if (!closed) {
                closed = true;
                if (con.getStatus() == 0) {
                    con.setStatus(WSHTTPConnection.ONEWAY);
                }
                OutputStream output = null;
                try {
                    output = con.getOutput();
                } catch (IOException e) {
                }
                if (dump || LOGGER.isLoggable(Level.FINER)) {
                    try {
                        ByteArrayBuffer buf = new ByteArrayBuffer();
                        dump(buf, "HTTP response " + con.getStatus(), con.getResponseHeaders());
                    } catch (Exception e) {
                        throw new WebServiceException(e.toString(), e);
                    }
                }
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        throw new WebServiceException(e);
                    }
                }
                con.close();
            }
        }
    }

    final class HttpToolkit extends Adapter.Toolkit {

        public void handle(WSHTTPConnection con) throws IOException {
            try {
                boolean invoke = false;
                Packet packet;
                try {
                    packet = decodePacket(con, codec);
                    invoke = true;
                } catch (Exception e) {
                    packet = new Packet();
                    if (e instanceof ExceptionHasMessage) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        packet.setMessage(((ExceptionHasMessage) e).getFaultMessage());
                    } else if (e instanceof UnsupportedMediaException) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        con.setStatus(WSHTTPConnection.UNSUPPORTED_MEDIA);
                    } else {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        con.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
                    }
                }
                if (invoke) {
                    try {
                        packet = head.process(packet, con.getWebServiceContextDelegate(), packet.transportBackChannel);
                    } catch (Throwable e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        if (!con.isClosed()) {
                            writeInternalServerError(con);
                        }
                        return;
                    }
                }
                encodePacket(packet, con, codec);
            } finally {
                if (!con.isClosed()) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Closing HTTP Connection with status: {0}", con.getStatus());
                    }
                    con.close();
                }
            }
        }
    }

    private boolean isMetadataQuery(String query) {
        return query != null && (query.equals("WSDL") || query.startsWith("wsdl") || query.startsWith("xsd="));
    }

    public void publishWSDL(@NotNull WSHTTPConnection con) throws IOException {
        con.getInput().close();
        SDDocument doc = wsdls.get(con.getQueryString());
        if (doc == null) {
            writeNotFoundErrorPage(con, "Invalid Request");
            return;
        }
        con.setStatus(HttpURLConnection.HTTP_OK);
        con.setContentTypeResponseHeader("text/xml;charset=utf-8");
        OutputStream os = con.getProtocol().contains("1.1") ? con.getOutput() : new Http10OutputStream(con);
        PortAddressResolver portAddressResolver = getPortAddressResolver(con.getBaseAddress());
        DocumentAddressResolver resolver = getDocumentAddressResolver(portAddressResolver);
        doc.writeTo(portAddressResolver, resolver, os);
        os.close();
    }

    public PortAddressResolver getPortAddressResolver(String baseAddress) {
        return owner.createPortAddressResolver(baseAddress, endpoint.getImplementationClass());
    }

    public DocumentAddressResolver getDocumentAddressResolver(PortAddressResolver portAddressResolver) {
        final String address = portAddressResolver.getAddressFor(endpoint.getServiceName(), endpoint.getPortName().getLocalPart());
        assert address != null;
        return new DocumentAddressResolver() {

            @Override
            public String getRelativeAddressFor(@NotNull SDDocument current, @NotNull SDDocument referenced) {
                assert revWsdls.containsKey(referenced);
                return address + '?' + revWsdls.get(referenced);
            }
        };
    }

    private final static class Http10OutputStream extends ByteArrayBuffer {

        private final WSHTTPConnection con;

        Http10OutputStream(WSHTTPConnection con) {
            this.con = con;
        }

        @Override
        public void close() throws IOException {
            super.close();
            con.setContentLengthResponseHeader(size());
            OutputStream os = con.getOutput();
            writeTo(os);
            os.close();
        }
    }

    private void writeNotFoundErrorPage(WSHTTPConnection con, String message) throws IOException {
        con.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
        con.setContentTypeResponseHeader("text/html; charset=utf-8");
        PrintWriter out = new PrintWriter(new OutputStreamWriter(con.getOutput(), "UTF-8"));
        out.println("<html>");
        out.println("<head><title>");
        out.println(WsservletMessages.SERVLET_HTML_TITLE());
        out.println("</title></head>");
        out.println("<body>");
        out.println(WsservletMessages.SERVLET_HTML_NOT_FOUND(message));
        out.println("</body>");
        out.println("</html>");
        out.close();
    }

    private void writeInternalServerError(WSHTTPConnection con) throws IOException {
        con.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        con.getOutput().close();
    }

    private static final class DummyList extends HttpAdapterList<HttpAdapter> {

        @Override
        protected HttpAdapter createHttpAdapter(String name, String urlPattern, WSEndpoint<?> endpoint) {
            return new HttpAdapter(endpoint, this, urlPattern);
        }
    }

    private static void dump(ByteArrayBuffer buf, String caption, Map<String, List<String>> headers) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos, true);
        pw.println("---[" + caption + "]---");
        if (headers != null) {
            for (Entry<String, List<String>> header : headers.entrySet()) {
                if (header.getValue().isEmpty()) {
                    pw.println(header.getValue());
                } else {
                    for (String value : header.getValue()) {
                        pw.println(header.getKey() + ": " + value);
                    }
                }
            }
        }
        if (buf.size() > dump_threshold) {
            byte[] b = buf.getRawData();
            baos.write(b, 0, dump_threshold);
            pw.println();
            pw.println(WsservletMessages.MESSAGE_TOO_LONG(HttpAdapter.class.getName() + ".dumpTreshold"));
        } else {
            buf.writeTo(baos);
        }
        pw.println("--------------------");
        String msg = baos.toString();
        if (dump) {
            System.out.println(msg);
        }
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(Level.FINER, msg);
        }
    }

    private void writeWebServicesHtmlPage(WSHTTPConnection con) throws IOException {
        if (!publishStatusPage) {
            return;
        }
        con.getInput().close();
        con.setStatus(WSHTTPConnection.OK);
        con.setContentTypeResponseHeader("text/html; charset=utf-8");
        PrintWriter out = new PrintWriter(new OutputStreamWriter(con.getOutput(), "UTF-8"));
        out.println("<html>");
        out.println("<head><title>");
        out.println(WsservletMessages.SERVLET_HTML_TITLE());
        out.println("</title></head>");
        out.println("<body>");
        out.println(WsservletMessages.SERVLET_HTML_TITLE_2());
        Module module = getEndpoint().getContainer().getSPI(Module.class);
        List<BoundEndpoint> endpoints = Collections.emptyList();
        if (module != null) {
            endpoints = module.getBoundEndpoints();
        }
        if (endpoints.isEmpty()) {
            out.println(WsservletMessages.SERVLET_HTML_NO_INFO_AVAILABLE());
        } else {
            out.println("<table width='100%' border='1'>");
            out.println("<tr>");
            out.println("<td>");
            out.println(WsservletMessages.SERVLET_HTML_COLUMN_HEADER_PORT_NAME());
            out.println("</td>");
            out.println("<td>");
            out.println(WsservletMessages.SERVLET_HTML_COLUMN_HEADER_INFORMATION());
            out.println("</td>");
            out.println("</tr>");
            for (BoundEndpoint a : endpoints) {
                String endpointAddress = a.getAddress(con.getBaseAddress()).toString();
                out.println("<tr>");
                out.println("<td>");
                out.println(WsservletMessages.SERVLET_HTML_ENDPOINT_TABLE(a.getEndpoint().getServiceName(), a.getEndpoint().getPortName()));
                out.println("</td>");
                out.println("<td>");
                out.println(WsservletMessages.SERVLET_HTML_INFORMATION_TABLE(endpointAddress, a.getEndpoint().getImplementationClass().getName()));
                out.println("</td>");
                out.println("</tr>");
            }
            out.println("</table>");
        }
        out.println("</body>");
        out.println("</html>");
        out.close();
    }

    public static volatile boolean dump = false;

    public static volatile int dump_threshold = 4096;

    public static volatile boolean publishStatusPage = true;

    public static synchronized void setPublishStatus(boolean publish) {
        publishStatusPage = publish;
    }

    static {
        try {
            dump = Boolean.getBoolean(HttpAdapter.class.getName() + ".dump");
        } catch (SecurityException se) {
            if (LOGGER.isLoggable(Level.CONFIG)) {
                LOGGER.log(Level.CONFIG, "Cannot read ''{0}'' property, using defaults.", new Object[] { HttpAdapter.class.getName() + ".dump" });
            }
        }
        try {
            dump_threshold = Integer.getInteger(HttpAdapter.class.getName() + ".dumpTreshold", 4096);
        } catch (SecurityException se) {
            if (LOGGER.isLoggable(Level.CONFIG)) {
                LOGGER.log(Level.CONFIG, "Cannot read ''{0}'' property, using defaults.", new Object[] { HttpAdapter.class.getName() + ".dumpTreshold" });
            }
        }
        try {
            setPublishStatus(Boolean.getBoolean(HttpAdapter.class.getName() + ".publishStatusPage"));
        } catch (SecurityException se) {
            if (LOGGER.isLoggable(Level.CONFIG)) {
                LOGGER.log(Level.CONFIG, "Cannot read ''{0}'' property, using defaults.", new Object[] { HttpAdapter.class.getName() + ".publishStatusPage" });
            }
        }
    }

    public static void setDump(boolean dumpMessages) {
        HttpAdapter.dump = dumpMessages;
    }
}