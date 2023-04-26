package org.apache.jmeter.protocol.http.sampler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.ProtocolException;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.TraceMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.params.HttpParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.protocol.http.util.EncoderCache;
import org.apache.jmeter.protocol.http.util.SlowHttpClientSocketFactory;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

public class HTTPSampler2 extends HTTPSamplerBase implements ThreadListener {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String HTTP_AUTHENTICATION_PREEMPTIVE = "http.authentication.preemptive";

    private static boolean canSetPreEmptive;

    static final String PROXY_HOST = System.getProperty("http.proxyHost", "");

    private static final String NONPROXY_HOSTS = System.getProperty("http.nonProxyHosts", "");

    static final int PROXY_PORT = Integer.parseInt(System.getProperty("http.proxyPort", "0"));

    private static final boolean PROXY_DEFINED = PROXY_HOST.length() > 0 && PROXY_PORT > 0;

    static final String PROXY_USER = JMeterUtils.getPropDefault(JMeter.HTTP_PROXY_USER, "");

    static final String PROXY_PASS = JMeterUtils.getPropDefault(JMeter.HTTP_PROXY_PASS, "");

    private static final String PROXY_DOMAIN = JMeterUtils.getPropDefault("http.proxyDomain", "");

    static InetAddress localAddress = null;

    private static final String localHost;

    static final ThreadLocal httpClients = new ThreadLocal();

    private static Set nonProxyHostFull = new HashSet();

    private static List nonProxyHostSuffix = new ArrayList();

    private static final int nonProxyHostSuffixSize;

    static boolean isNonProxy(String host) {
        return nonProxyHostFull.contains(host) || isPartialMatch(host);
    }

    private static boolean isPartialMatch(String host) {
        for (int i = 0; i < nonProxyHostSuffixSize; i++) {
            if (host.endsWith((String) nonProxyHostSuffix.get(i)))
                return true;
        }
        return false;
    }

    static {
        if (NONPROXY_HOSTS.length() > 0) {
            StringTokenizer s = new StringTokenizer(NONPROXY_HOSTS, "|");
            while (s.hasMoreTokens()) {
                String t = s.nextToken();
                if (t.indexOf("*") == 0) {
                    nonProxyHostSuffix.add(t.substring(1));
                } else {
                    nonProxyHostFull.add(t);
        }
    }
        }
        nonProxyHostSuffixSize = nonProxyHostSuffix.size();
        int cps = JMeterUtils.getPropDefault("httpclient.socket.http.cps", 0);
        if (cps > 0) {
            log.info("Setting up HTTP SlowProtocol, cps=" + cps);
            Protocol.registerProtocol(PROTOCOL_HTTP, new Protocol(PROTOCOL_HTTP, new SlowHttpClientSocketFactory(cps), DEFAULT_HTTP_PORT));
        }
        String localHostOrIP = JMeterUtils.getPropDefault("httpclient.localaddress", "");
        if (localHostOrIP.length() > 0) {
            try {
                localAddress = InetAddress.getByName(localHostOrIP);
                log.info("Using localAddress " + localAddress.getHostAddress());
            } catch (UnknownHostException e) {
                log.warn(e.getLocalizedMessage());
            }
        } else {
            try {
                InetAddress addr = InetAddress.getLocalHost();
                localHostOrIP = addr.getHostName();
            } catch (UnknownHostException e) {
                log.warn("Cannot determine localhost name, and httpclient.localaddress was not specified");
            }
        }
        localHost = localHostOrIP;
        log.info("Local host = " + localHost);
        setDefaultParams();
    }

    private static void setDefaultParams() {
        HttpParams params = DefaultHttpParams.getDefaultParams();
        String file = JMeterUtils.getProperty("httpclient.parameters.file");
        if (file != null) {
            HttpClientDefaultParameters.load(file, params);
        }
        canSetPreEmptive = params.getParameter(HTTP_AUTHENTICATION_PREEMPTIVE) == null;
        String ver = JMeterUtils.getPropDefault("httpclient.version", "1.1");
        try {
            params.setParameter(HttpMethodParams.PROTOCOL_VERSION, HttpVersion.parse("HTTP/" + ver));
        } catch (ProtocolException e) {
            log.warn("Problem setting protocol version " + e.getLocalizedMessage());
        }
        String to = JMeterUtils.getProperty("httpclient.timeout");
        if (to != null) {
            params.setIntParameter(HttpMethodParams.SO_TIMEOUT, Integer.parseInt(to));
        }
        params.setParameter(HttpMethodParams.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
    }

    public HTTPSampler2() {
    }

    public HTTPSampler2() {
    }

    private String sendPostData(PostMethod post) throws IOException {
        StringBuffer postedBody = new StringBuffer(1000);
        if (getUseMultipartForPost()) {
            String contentEncoding = getContentEncoding();
            if (contentEncoding != null && contentEncoding.length() == 0) {
                contentEncoding = null;
            }
            int noParts = getArguments().getArgumentCount();
            if (hasUploadableFiles()) {
                noParts++;
            }
            Part[] parts = new Part[noParts];
            int partNo = 0;
            PropertyIterator args = getArguments().iterator();
            while (args.hasNext()) {
                HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
                parts[partNo++] = new StringPart(arg.getName(), arg.getValue(), contentEncoding);
            }
            if (hasUploadableFiles()) {
                File inputFile = new File(getFilename());
                ViewableFilePart filePart = new ViewableFilePart(getFileField(), inputFile, getMimetype(), null);
                filePart.setCharSet(null);
                parts[partNo++] = filePart;
            }
            MultipartRequestEntity multiPart = new MultipartRequestEntity(parts, post.getParams());
            post.setRequestEntity(multiPart);
            String multiPartContentType = multiPart.getContentType();
            post.setRequestHeader(HEADER_CONTENT_TYPE, multiPartContentType);
            if (multiPart.isRepeatable()) {
                for (int i = 0; i < partNo; i++) {
                    if (parts[i] instanceof ViewableFilePart) {
                        ((ViewableFilePart) parts[i]).setHideFileData(true);
                    }
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                multiPart.writeRequest(bos);
                bos.flush();
                postedBody.append(new String(bos.toByteArray(), "UTF-8"));
                bos.close();
                for (int i = 0; i < partNo; i++) {
                    if (parts[i] instanceof ViewableFilePart) {
                        ((ViewableFilePart) parts[i]).setHideFileData(false);
                    }
                }
            } else {
                postedBody.append("<Multipart was not repeatable, cannot view what was sent>");
            }
        } else {
            Header contentTypeHeader = post.getRequestHeader(HEADER_CONTENT_TYPE);
            boolean hasContentTypeHeader = contentTypeHeader != null && contentTypeHeader.getValue() != null && contentTypeHeader.getValue().length() > 0;
            if (getArguments().getArgumentCount() == 0 && getSendFileAsPostBody()) {
                if (!hasContentTypeHeader) {
                    if (getMimetype() != null && getMimetype().length() > 0) {
                        post.setRequestHeader(HEADER_CONTENT_TYPE, getMimetype());
                    } else {
                        post.setRequestHeader(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
                    }
                }
                FileRequestEntity fileRequestEntity = new FileRequestEntity(new File(getFilename()), null);
                post.setRequestEntity(fileRequestEntity);
                postedBody.append("<actual file content, not shown here>");
            } else {
                if (!hasContentTypeHeader) {
                    post.setRequestHeader(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
                }
                final String contentEncoding = getContentEncoding();
                if (contentEncoding != null && contentEncoding.trim().length() > 0) {
                    post.getParams().setContentCharset(contentEncoding);
                }
                if (!getSendParameterValuesAsPostBody()) {
                    PropertyIterator args = getArguments().iterator();
                    while (args.hasNext()) {
                        HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
                        String parameterName = arg.getName();
                        String parameterValue = arg.getValue();
                        if (!arg.isAlwaysEncoded()) {
                            String urlContentEncoding = contentEncoding;
                            if (urlContentEncoding == null || urlContentEncoding.length() == 0) {
                                urlContentEncoding = EncoderCache.URL_ARGUMENT_ENCODING;
                            }
                            parameterName = URLDecoder.decode(parameterName, urlContentEncoding);
                            parameterValue = URLDecoder.decode(parameterValue, urlContentEncoding);
                        }
                        post.addParameter(parameterName, parameterValue);
                    }
                } else {
                    StringBuffer postBody = new StringBuffer();
                    PropertyIterator args = getArguments().iterator();
                    while (args.hasNext()) {
                        HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
                        postBody.append(arg.getValue());
                    }
                    StringRequestEntity requestEntity = new StringRequestEntity(postBody.toString(), post.getRequestHeader(HEADER_CONTENT_TYPE).getValue(), post.getRequestCharSet());
                    post.setRequestEntity(requestEntity);
                }
                if (post.getRequestEntity().isRepeatable()) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    post.getRequestEntity().writeRequest(bos);
                    bos.flush();
                    postedBody.append(new String(bos.toByteArray(), "UTF-8"));
                    bos.close();
                } else {
                    postedBody.append("<Multipart was not repeatable, cannot view what was sent>");
                }
            }
        }
        post.setRequestHeader(HEADER_CONTENT_LENGTH, Long.toString(post.getRequestEntity().getContentLength()));
        return postedBody.toString();
    }

    HttpClient setupConnection(URL u, HttpMethodBase httpMethod, HTTPSampleResult res) throws IOException {
        String urlStr = u.toString();
        org.apache.commons.httpclient.URI uri = new org.apache.commons.httpclient.URI(urlStr, false);
        String schema = uri.getScheme();
        if ((schema == null) || (schema.length() == 0)) {
            schema = PROTOCOL_HTTP;
        }
        if (PROTOCOL_HTTPS.equalsIgnoreCase(schema)) {
            SSLManager.getInstance();
        }
        Protocol protocol = Protocol.getProtocol(schema);
        String host = uri.getHost();
        int port = uri.getPort();
        HostConfiguration hc = new HostConfiguration();
        hc.setHost(host, port, protocol);
        if (localAddress != null) {
            hc.setLocalAddress(localAddress);
        }
        boolean useProxy = PROXY_DEFINED && !isNonProxy(host);
        if (useProxy) {
            if (log.isDebugEnabled()) {
                log.debug("Setting proxy: " + PROXY_HOST + ":" + PROXY_PORT);
            }
            hc.setProxy(PROXY_HOST, PROXY_PORT);
        }
        Map map = (Map) httpClients.get();
        HttpClient httpClient = (HttpClient) map.get(hc);
        if (httpClient == null) {
            httpClient = new HttpClient(new SimpleHttpConnectionManager());
            httpClient.setHostConfiguration(hc);
            map.put(hc, httpClient);
            if (useProxy) {
                if (PROXY_USER.length() > 0) {
                    httpClient.getState().setProxyCredentials(new AuthScope(PROXY_HOST, PROXY_PORT, null, AuthScope.ANY_SCHEME), new NTCredentials(PROXY_USER, PROXY_PASS, localHost, PROXY_DOMAIN));
                }
            }
        }
        httpMethod.setFollowRedirects(getAutoRedirects());
        if (getUseKeepAlive()) {
            httpMethod.setRequestHeader(HEADER_CONNECTION, KEEP_ALIVE);
        } else {
            httpMethod.setRequestHeader(HEADER_CONNECTION, CONNECTION_CLOSE);
        }
        setConnectionHeaders(httpMethod, u, getHeaderManager());
        String cookies = setConnectionCookie(httpMethod, u, getCookieManager());
        setConnectionAuthorization(httpClient, u, getAuthManager());
        if (res != null) {
            res.setURL(u);
            res.setCookies(cookies);
        }
        return httpClient;
    }

    protected String getResponseHeaders(HttpMethod method) {
        StringBuffer headerBuf = new StringBuffer();
        org.apache.commons.httpclient.Header[] rh = method.getResponseHeaders();
        headerBuf.append(method.getStatusLine());
        headerBuf.append("\n");
        for (int i = 0; i < rh.length; i++) {
            String key = rh[i].getName();
            if (!key.equalsIgnoreCase(TRANSFER_ENCODING)) {
                headerBuf.append(key);
                headerBuf.append(": ");
                headerBuf.append(rh[i].getValue());
                headerBuf.append("\n");
            }
        }
        return headerBuf.toString();
    }

    String setConnectionCookie(HttpMethod method, URL u, CookieManager cookieManager) {
        String cookieHeader = null;
        if (cookieManager != null) {
            cookieHeader = cookieManager.getCookieHeaderForURL(u);
            if (cookieHeader != null) {
                method.setRequestHeader(HEADER_COOKIE, cookieHeader);
            }
        }
        return cookieHeader;
    }

    private void setConnectionHeaders(HttpMethod method, URL u, HeaderManager headerManager) {
        if (headerManager != null) {
            CollectionProperty headers = headerManager.getHeaders();
            if (headers != null) {
                PropertyIterator i = headers.iterator();
                while (i.hasNext()) {
                    org.apache.jmeter.protocol.http.control.Header header = (org.apache.jmeter.protocol.http.control.Header) i.next().getObjectValue();
                    String n = header.getName();
                    if (!HEADER_CONTENT_LENGTH.equalsIgnoreCase(n)) {
                    String v = header.getValue();
                        method.addRequestHeader(n, v);
                }
            }
        }
        }
    }

    private String getConnectionHeaders(HttpMethod method) {
        StringBuffer hdrs = new StringBuffer(100);
        Header[] requestHeaders = method.getRequestHeaders();
        for (int i = 0; i < requestHeaders.length; i++) {
            if (!HEADER_COOKIE.equalsIgnoreCase(requestHeaders[i].getName())) {
                hdrs.append(requestHeaders[i].getName());
                hdrs.append(": ");
                hdrs.append(requestHeaders[i].getValue());
                hdrs.append("\n");
            }
        }
        return hdrs.toString();
    }

    void setConnectionAuthorization(HttpClient client, URL u, AuthManager authManager) {
        HttpParams params = client.getParams();
        if (authManager != null) {
            Authorization auth = authManager.getAuthForURL(u);
            if (auth != null) {
                String username = auth.getUser();
                String realm = auth.getRealm();
                String domain = auth.getDomain();
                if (log.isDebugEnabled()) {
                    log.debug(username + " >  D=" + username + " D=" + domain + " R=" + realm);
                }
                client.getState().setCredentials(new AuthScope(u.getHost(), u.getPort(), realm.length() == 0 ? null : realm, AuthScope.ANY_SCHEME), new NTCredentials(username, auth.getPass(), localHost, domain));
                if (canSetPreEmptive) {
                    log.debug("Setting Pre-emptive authentication");
                    params.setBooleanParameter(HTTP_AUTHENTICATION_PREEMPTIVE, true);
                }
            } else {
                client.getState().clearCredentials();
                if (canSetPreEmptive) {
                    params.setBooleanParameter(HTTP_AUTHENTICATION_PREEMPTIVE, false);
                }
            }
        } else {
            client.getState().clearCredentials();
        }
    }

    protected HTTPSampleResult sample(URL url, String method, boolean areFollowingRedirect, int frameDepth) {
        String urlStr = url.toString();
        log.debug("Start : sample" + urlStr);
        log.debug("method" + method);
        HttpMethodBase httpMethod = null;
        HTTPSampleResult res = new HTTPSampleResult();
        res.setMonitor(isMonitor());
        res.setSampleLabel(urlStr);
        res.setHTTPMethod(method);
        res.sampleStart();
        HttpClient client = null;
        InputStream instream = null;
        try {
            if (method.equals(POST)) {
                httpMethod = new PostMethod(urlStr);
            } else if (method.equals(PUT)) {
                httpMethod = new PutMethod(urlStr);
            } else if (method.equals(HEAD)) {
                httpMethod = new HeadMethod(urlStr);
            } else if (method.equals(TRACE)) {
                httpMethod = new TraceMethod(urlStr);
            } else if (method.equals(OPTIONS)) {
                httpMethod = new OptionsMethod(urlStr);
            } else if (method.equals(DELETE)) {
                httpMethod = new DeleteMethod(urlStr);
            } else if (method.equals(GET)) {
                httpMethod = new GetMethod(urlStr);
            } else {
                log.error("Unexpected method (converted to GET): " + method);
                httpMethod = new GetMethod(urlStr);
            }
            client = setupConnection(url, httpMethod, res);
            if (method.equals(POST)) {
                String postBody = sendPostData((PostMethod) httpMethod);
                res.setQueryString(postBody);
            } else if (method.equals(PUT)) {
                setPutHeaders((PutMethod) httpMethod);
            }
            res.setRequestHeaders(getConnectionHeaders(httpMethod));
            int statusCode = client.executeMethod(httpMethod);
            instream = httpMethod.getResponseBodyAsStream();
            if (instream != null) {
                Header responseHeader = httpMethod.getResponseHeader(HEADER_CONTENT_ENCODING);
                if (responseHeader != null && ENCODING_GZIP.equals(responseHeader.getValue())) {
                    instream = new GZIPInputStream(instream);
                }
                ByteArrayOutputStream outstream = new ByteArrayOutputStream(4 * 1024);
                byte[] buffer = new byte[4096];
                int len;
                boolean first = true;
                while ((len = instream.read(buffer)) > 0) {
                    if (first) {
                        res.latencyEnd();
                        first = false;
                    }
                    outstream.write(buffer, 0, len);
                }
                res.setResponseData(outstream.toByteArray());
                outstream.close();
            }
            res.sampleEnd();
            res.setSampleLabel(httpMethod.getURI().toString());
            res.setResponseCode(Integer.toString(statusCode));
            res.setSuccessful(isSuccessCode(statusCode));
            res.setResponseMessage(httpMethod.getStatusText());
            String ct = null;
            org.apache.commons.httpclient.Header h = httpMethod.getResponseHeader(HEADER_CONTENT_TYPE);
            if (h != null) {
                ct = h.getValue();
                res.setContentType(ct);
                res.setEncodingAndType(ct);
            }
            res.setResponseHeaders(getResponseHeaders(httpMethod));
            if (res.isRedirect()) {
                final Header headerLocation = httpMethod.getResponseHeader(HEADER_LOCATION);
                if (headerLocation == null) {
                    throw new IllegalArgumentException("Missing location header");
            }
                res.setRedirectLocation(headerLocation.getValue());
            }
            if (getAutoRedirects()) {
                res.setURL(new URL(httpMethod.getURI().toString()));
            }
            saveConnectionCookies(httpMethod, res.getURL(), getCookieManager());
            res = resultProcessing(areFollowingRedirect, frameDepth, res);
            log.debug("End : sample");
            if (httpMethod != null)
                httpMethod.releaseConnection();
            return res;
        } catch (IllegalArgumentException e) {
            res.sampleEnd();
            HTTPSampleResult err = errorResult(e, res);
            err.setSampleLabel("Error: " + url.toString());
            return err;
        } catch (IOException e) {
            res.sampleEnd();
            HTTPSampleResult err = errorResult(e, res);
            err.setSampleLabel("Error: " + url.toString());
            return err;
        } finally {
            JOrphanUtils.closeQuietly(instream);
            if (httpMethod != null)
                httpMethod.releaseConnection();
        }
    }

    private void setPutHeaders(PutMethod put) {
        String filename = getFilename();
        if ((filename != null) && (filename.trim().length() > 0)) {
            RequestEntity requestEntity = new FileRequestEntity(new File(filename), getMimetype());
            put.setRequestEntity(requestEntity);
        }
    }

    private class FileRequestEntity implements RequestEntity {

        final File file;

        final String contentType;

        public FileRequestEntity(final File file, final String contentType) {
            super();
            if (file == null) {
                throw new IllegalArgumentException("File may not be null");
            }
            this.file = file;
            this.contentType = contentType;
        }

        public long getContentLength() {
            return this.file.length();
        }

        public String getContentType() {
            return this.contentType;
        }

        public boolean isRepeatable() {
            return true;
        }

        public void writeRequest(OutputStream out) throws IOException {
            InputStream in = new FileInputStream(this.file);
            try {
                int l;
                byte[] buffer = new byte[1024];
                while ((l = in.read(buffer)) != -1) {
                    out.write(buffer, 0, l);
                }
            } finally {
                in.close();
            }
        }
    }

    private class ViewableFilePart extends FilePart {

        private boolean hideFileData;

        public ViewableFilePart(String name, File file, String contentType, String charset) throws FileNotFoundException {
            super(name, file, contentType, charset);
            this.hideFileData = false;
        }

        public void setHideFileData(boolean hideFileData) {
            this.hideFileData = hideFileData;
        }

        protected void sendData(OutputStream out) throws IOException {
            if (hideFileData) {
                out.write("<actual file content, not shown here>".getBytes("UTF-8"));
            } else {
                super.sendData(out);
            }
        }
    }

    void saveConnectionCookies(HttpMethod method, URL u, CookieManager cookieManager) {
        if (cookieManager != null) {
            Header[] hdr = method.getResponseHeaders(HEADER_SET_COOKIE);
            for (int i = 0; i < hdr.length; i++) {
                cookieManager.addCookieFromHeader(hdr[i].getValue(), u);
            }
        }
    }

    public void threadStarted() {
        log.debug("Thread Started");
<<<<<<< MINE
=======
        httpClients.set(new HashMap());
>>>>>>> YOURS
    }

    public void threadFinished() {
        log.debug("Thread Finished");
<<<<<<< MINE
        if (httpConn != null)
            httpConn.close();
=======
        Map map = (Map) httpClients.get();
        if (map != null) {
            for (Iterator it = map.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                HttpClient cl = (HttpClient) entry.getValue();
                cl.getHttpConnectionManager().closeIdleConnections(-1000);
            }
            map.clear();
        }
>>>>>>> YOURS
    }
}