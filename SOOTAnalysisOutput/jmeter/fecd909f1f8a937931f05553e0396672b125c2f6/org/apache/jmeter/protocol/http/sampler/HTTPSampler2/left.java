package org.apache.jmeter.protocol.http.sampler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import org.apache.commons.httpclient.ConnectMethod;
import org.apache.commons.httpclient.DefaultMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class HTTPSampler2 extends HTTPSamplerBase implements ThreadListener {

    transient private static Logger log = LoggingManager.getLoggerForClass();

    static {
        if (System.getProperty("org.apache.commons.logging.Log") == null) {
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.LogKitLogger");
        }
    }

    private transient HttpConnection httpConn = null;

    private transient HttpMethodBase httpMethod = null;

    private transient HttpState httpState = null;

    private static boolean basicAuth = JMeterUtils.getPropDefault("httpsampler2.basicauth", false);

    static {
        log.info("httpsampler2.basicauth=" + basicAuth);
    }

    public HTTPSampler2() {
    }

    private void setPostHeaders(PostMethod post) throws IOException {
    }

    private void sendPostData(HttpMethod connection) throws IOException {
        sendPostData((PostMethod) connection, this);
    }

    public void sendPostData(PostMethod post, HTTPSampler2 sampler) throws IOException {
        PropertyIterator args = sampler.getArguments().iterator();
        while (args.hasNext()) {
            Argument arg = (Argument) args.next().getObjectValue();
            post.addParameter(arg.getName(), arg.getValue());
        }
        String filename = sampler.getFilename();
        if ((filename != null) && (filename.trim().length() > 0)) {
            File input = new File(filename);
            if (input.length() < Integer.MAX_VALUE) {
                post.setRequestContentLength((int) input.length());
            } else {
                post.setRequestContentLength(EntityEnclosingMethod.CONTENT_LENGTH_CHUNKED);
            }
            post.setRequestHeader("Content-Disposition", "form-data; name=\"" + encode(sampler.getFileField()) + "\"; filename=\"" + encode(filename) + "\"");
            post.setRequestHeader("Content-Type", sampler.getMimetype());
            post.setRequestBody(new FileInputStream(input));
        }
    }

    private String encode(String value) {
        StringBuffer newValue = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '\\') {
                newValue.append("\\\\");
            } else {
                newValue.append(chars[i]);
            }
        }
        return newValue.toString();
    }

    private HttpConnection setupConnection(URL u, String method, HTTPSampleResult res) throws IOException {
        String urlStr = u.toString();
        org.apache.commons.httpclient.URI uri = new org.apache.commons.httpclient.URI(urlStr);
        String schema = uri.getScheme();
        if ((schema == null) || (schema.equals(""))) {
            schema = "http";
        }
        Protocol protocol = Protocol.getProtocol(schema);
        String host = uri.getHost();
        int port = uri.getPort();
        HostConfiguration hc = new HostConfiguration();
        hc.setHost(host, port, protocol);
        if (httpConn != null && hc.hostEquals(httpConn)) {
        } else {
            httpConn = new HttpConnection(hc);
            httpConn.setProxyHost(System.getProperty("http.proxyHost"));
            httpConn.setProxyPort(Integer.parseInt(System.getProperty("http.proxyPort", "80")));
        }
        if (method.equals(POST)) {
            httpMethod = new PostMethod(urlStr);
        } else {
            httpMethod = new GetMethod(urlStr);
            new DefaultMethodRetryHandler();
        }
        httpMethod.setHttp11(!JMeterUtils.getPropDefault("httpclient.version", "1.1").equals("1.0"));
        httpConn.setSoTimeout(JMeterUtils.getPropDefault("httpclient.timeout", 0));
        httpState = new HttpState();
        if (httpConn.isProxied() && httpConn.isSecure()) {
            httpMethod = new ConnectMethod(httpMethod);
        }
        httpMethod.setFollowRedirects(getPropertyAsBoolean(AUTO_REDIRECTS));
        if (getUseKeepAlive()) {
            httpMethod.setRequestHeader("Connection", "keep-alive");
        } else {
            httpMethod.setRequestHeader("Connection", "close");
        }
        String hdrs = setConnectionHeaders(httpMethod, u, getHeaderManager());
        String cookies = setConnectionCookie(httpMethod, u, getCookieManager());
        if (res != null) {
            StringBuffer sb = new StringBuffer();
            if (method.equals(POST)) {
                String q = this.getQueryString();
                res.setQueryString(q);
                sb.append("Query data:\n");
                sb.append(q);
                sb.append('\n');
            }
            if (cookies != null) {
                res.setCookies(cookies);
                sb.append("\nCookie Data:\n");
                sb.append(cookies);
                sb.append('\n');
            }
            res.setSamplerData(sb.toString());
            res.setURL(u);
            res.setHTTPMethod(method);
            res.setRequestHeaders(hdrs);
            res.setQueryString(getQueryString());
        }
        setConnectionAuthorization(httpMethod, u, getAuthManager());
        if (method.equals(POST)) {
            setPostHeaders((PostMethod) httpMethod);
        }
        return httpConn;
    }

    protected String getResponseHeaders(HttpMethod method) throws IOException {
        StringBuffer headerBuf = new StringBuffer();
        org.apache.commons.httpclient.Header[] rh = method.getResponseHeaders();
        headerBuf.append(method.getStatusLine());
        headerBuf.append("\n");
        for (int i = 0; i < rh.length; i++) {
            String key = rh[i].getName();
            if (!key.equalsIgnoreCase("transfer-encoding")) {
                headerBuf.append(key);
                headerBuf.append(": ");
                headerBuf.append(rh[i].getValue());
                headerBuf.append("\n");
            }
        }
        return headerBuf.toString();
    }

    private String setConnectionCookie(HttpMethod method, URL u, CookieManager cookieManager) {
        String cookieHeader = null;
        if (cookieManager != null) {
            cookieHeader = cookieManager.getCookieHeaderForURL(u);
            if (cookieHeader != null) {
                method.setRequestHeader("Cookie", cookieHeader);
            }
        }
        return cookieHeader;
    }

    private String setConnectionHeaders(HttpMethod method, URL u, HeaderManager headerManager) {
        StringBuffer hdrs = new StringBuffer(100);
        if (headerManager != null) {
            CollectionProperty headers = headerManager.getHeaders();
            if (headers != null) {
                PropertyIterator i = headers.iterator();
                while (i.hasNext()) {
                    org.apache.jmeter.protocol.http.control.Header header = (org.apache.jmeter.protocol.http.control.Header) i.next().getObjectValue();
                    String n = header.getName();
                    String v = header.getValue();
                    method.setRequestHeader(n, v);
                    hdrs.append(n);
                    hdrs.append(": ");
                    hdrs.append(v);
                    hdrs.append("\n");
                }
            }
        }
        return hdrs.toString();
    }

    private void setConnectionAuthorization(HttpMethod method, URL u, AuthManager authManager) {
        if (authManager != null) {
            if (basicAuth) {
                String authHeader = authManager.getAuthHeaderForURL(u);
                if (authHeader != null) {
                    method.setRequestHeader("Authorization", authHeader);
                }
            } else {
                Authorization auth = authManager.getAuthForURL(u);
                if (auth != null) {
                    httpState.setCredentials(null, auth.getURL(), new NTCredentials(auth.getUser(), auth.getPass(), null, null));
                }
            }
        }
    }

    protected HTTPSampleResult sample(URL url, String method, boolean areFollowingRedirect, int frameDepth) {
        String urlStr = url.toString();
        log.debug("Start : sample" + urlStr);
        log.debug("method" + method);
        httpMethod = null;
        HTTPSampleResult res = new HTTPSampleResult();
        if (this.getPropertyAsBoolean(MONITOR)) {
            res.setMonitor(true);
        } else {
            res.setMonitor(false);
        }
        res.setSampleLabel(urlStr);
        res.sampleStart();
        try {
            HttpConnection connection = setupConnection(url, method, res);
            if (method.equals(POST)) {
                sendPostData(httpMethod);
            }
            int statusCode = httpMethod.execute(httpState, connection);
            byte[] responseData = httpMethod.getResponseBody();
            res.sampleEnd();
            res.setSampleLabel(httpMethod.getURI().toString());
            res.setResponseData(responseData);
            res.setResponseCode(Integer.toString(statusCode));
            res.setSuccessful(200 <= statusCode && statusCode <= 399);
            res.setResponseMessage(httpMethod.getStatusText());
            String ct = null;
            org.apache.commons.httpclient.Header h = httpMethod.getResponseHeader("Content-Type");
            if (h != null) {
                ct = h.getValue();
                res.setContentType(ct);
            }
            if (ct != null) {
                String de = ct.toLowerCase();
                final String cs = "charset=";
                int cset = de.indexOf(cs);
                if (cset >= 0) {
                    res.setDataEncoding(de.substring(cset + cs.length()));
                }
                if (ct.startsWith("image/")) {
                    res.setDataType(HTTPSampleResult.BINARY);
                } else {
                    res.setDataType(HTTPSampleResult.TEXT);
                }
            }
            res.setResponseHeaders(getResponseHeaders(httpMethod));
            if (res.isRedirect()) {
                res.setRedirectLocation(httpMethod.getResponseHeader("Location").getValue());
            }
            saveConnectionCookies(httpState, getCookieManager());
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
            if (httpMethod != null)
                httpMethod.releaseConnection();
        }
    }

    private void saveConnectionCookies(HttpState state, CookieManager cookieManager) {
        if (cookieManager != null) {
            org.apache.commons.httpclient.Cookie[] c = state.getCookies();
            for (int i = 0; i < c.length; i++) {
                Date exp = c[i].getExpiryDate();
                cookieManager.add(new org.apache.jmeter.protocol.http.control.Cookie(c[i].getName(), c[i].getValue(), c[i].getDomain(), c[i].getPath(), c[i].getSecure(), exp == null ? 0 : exp.getTime() / 1000));
            }
        }
    }

    public void threadStarted() {
        log.debug("Thread Started");
    }

    public void threadFinished() {
        log.debug("Thread Finished");
        if (httpConn != null)
            httpConn.close();
    }
}
