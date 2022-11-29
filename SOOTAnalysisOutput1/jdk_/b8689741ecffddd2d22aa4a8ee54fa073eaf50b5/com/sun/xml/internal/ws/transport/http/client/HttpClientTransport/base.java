package com.sun.xml.internal.ws.transport.http.client;

import com.sun.xml.internal.ws.api.EndpointAddress;
import com.sun.xml.internal.ws.api.message.Packet;
import com.sun.xml.internal.ws.client.BindingProviderProperties;
import static com.sun.xml.internal.ws.client.BindingProviderProperties.*;
import com.sun.xml.internal.ws.client.ClientTransportException;
import com.sun.xml.internal.ws.resources.ClientMessages;
import com.sun.xml.internal.ws.transport.Headers;
import com.sun.xml.internal.ws.developer.JAXWSProperties;
import com.sun.istack.internal.Nullable;
import com.sun.istack.internal.NotNull;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

public class HttpClientTransport {

    private static final byte[] THROW_AWAY_BUFFER = new byte[8192];

    static {
        try {
            JAXBContext.newInstance().createUnmarshaller();
        } catch (JAXBException je) {
        }
    }

    int statusCode;

    String statusMessage;

    int contentLength;

    private final Map<String, List<String>> reqHeaders;

    private Map<String, List<String>> respHeaders = null;

    private OutputStream outputStream;

    private boolean https;

    private HttpURLConnection httpConnection = null;

    private final EndpointAddress endpoint;

    private final Packet context;

    private final Integer chunkSize;

    public HttpClientTransport(@NotNull Packet packet, @NotNull Map<String, List<String>> reqHeaders) {
        endpoint = packet.endpointAddress;
        context = packet;
        this.reqHeaders = reqHeaders;
        chunkSize = (Integer) context.invocationProperties.get(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE);
    }

    OutputStream getOutput() {
        try {
            createHttpConnection();
            if (requiresOutputStream()) {
                outputStream = httpConnection.getOutputStream();
                if (chunkSize != null) {
                    outputStream = new WSChunkedOuputStream(outputStream, chunkSize);
                }
                List<String> contentEncoding = reqHeaders.get("Content-Encoding");
                if (contentEncoding != null && contentEncoding.get(0).contains("gzip")) {
                    outputStream = new GZIPOutputStream(outputStream);
                }
            }
            httpConnection.connect();
        } catch (Exception ex) {
            throw new ClientTransportException(ClientMessages.localizableHTTP_CLIENT_FAILED(ex), ex);
        }
        return outputStream;
    }

    void closeOutput() throws IOException {
        if (outputStream != null) {
            outputStream.close();
            outputStream = null;
        }
    }

    @Nullable
    InputStream getInput() {
        InputStream in;
        try {
            in = readResponse();
            if (in != null) {
                String contentEncoding = httpConnection.getContentEncoding();
                if (contentEncoding != null && contentEncoding.contains("gzip")) {
                    in = new GZIPInputStream(in);
                }
            }
        } catch (IOException e) {
            throw new ClientTransportException(ClientMessages.localizableHTTP_STATUS_CODE(statusCode, statusMessage), e);
        }
        return in;
    }

    public Map<String, List<String>> getHeaders() {
        if (respHeaders != null) {
            return respHeaders;
        }
        respHeaders = new Headers();
        respHeaders.putAll(httpConnection.getHeaderFields());
        return respHeaders;
    }

    @Nullable
    protected InputStream readResponse() {
        InputStream is;
        try {
            is = httpConnection.getInputStream();
        } catch (IOException ioe) {
            is = httpConnection.getErrorStream();
        }
        if (is == null) {
            return is;
        }
        final InputStream temp = is;
        return new FilterInputStream(temp) {

            boolean closed;

            @Override
            public void close() throws IOException {
                if (!closed) {
                    closed = true;
                    while (temp.read(THROW_AWAY_BUFFER) != -1) ;
                    super.close();
                }
            }
        };
    }

    protected void readResponseCodeAndMessage() {
        try {
            statusCode = httpConnection.getResponseCode();
            statusMessage = httpConnection.getResponseMessage();
            contentLength = httpConnection.getContentLength();
        } catch (IOException ioe) {
            throw new WebServiceException(ioe);
        }
    }

    protected HttpURLConnection openConnection(Packet packet) {
        return null;
    }

    protected boolean checkHTTPS(HttpURLConnection connection) {
        if (connection instanceof HttpsURLConnection) {
            String verificationProperty = (String) context.invocationProperties.get(HOSTNAME_VERIFICATION_PROPERTY);
            if (verificationProperty != null) {
                if (verificationProperty.equalsIgnoreCase("true")) {
                    ((HttpsURLConnection) connection).setHostnameVerifier(new HttpClientVerifier());
                }
            }
            HostnameVerifier verifier = (HostnameVerifier) context.invocationProperties.get(JAXWSProperties.HOSTNAME_VERIFIER);
            if (verifier != null) {
                ((HttpsURLConnection) connection).setHostnameVerifier(verifier);
            }
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) context.invocationProperties.get(JAXWSProperties.SSL_SOCKET_FACTORY);
            if (sslSocketFactory != null) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
            }
            return true;
        }
        return false;
    }

    private void createHttpConnection() throws IOException {
        httpConnection = openConnection(context);
        if (httpConnection == null)
            httpConnection = (HttpURLConnection) endpoint.openConnection();
        String scheme = endpoint.getURI().getScheme();
        if (scheme.equals("https")) {
            https = true;
        }
        if (checkHTTPS(httpConnection))
            https = true;
        httpConnection.setAllowUserInteraction(true);
        httpConnection.setDoOutput(true);
        httpConnection.setDoInput(true);
        String requestMethod = (String) context.invocationProperties.get(MessageContext.HTTP_REQUEST_METHOD);
        String method = (requestMethod != null) ? requestMethod : "POST";
        httpConnection.setRequestMethod(method);
        Integer reqTimeout = (Integer) context.invocationProperties.get(BindingProviderProperties.REQUEST_TIMEOUT);
        if (reqTimeout != null) {
            httpConnection.setReadTimeout(reqTimeout);
        }
        Integer connectTimeout = (Integer) context.invocationProperties.get(JAXWSProperties.CONNECT_TIMEOUT);
        if (connectTimeout != null) {
            httpConnection.setConnectTimeout(connectTimeout);
        }
        Integer chunkSize = (Integer) context.invocationProperties.get(JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE);
        if (chunkSize != null) {
            httpConnection.setChunkedStreamingMode(chunkSize);
        }
        for (Map.Entry<String, List<String>> entry : reqHeaders.entrySet()) {
            if ("Content-Length".equals(entry.getKey()))
                continue;
            for (String value : entry.getValue()) {
                httpConnection.addRequestProperty(entry.getKey(), value);
            }
        }
    }

    boolean isSecure() {
        return https;
    }

    protected void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    private boolean requiresOutputStream() {
        return !(httpConnection.getRequestMethod().equalsIgnoreCase("GET") || httpConnection.getRequestMethod().equalsIgnoreCase("HEAD") || httpConnection.getRequestMethod().equalsIgnoreCase("DELETE"));
    }

    @Nullable
    String getContentType() {
        return httpConnection.getContentType();
    }

    public int getContentLength() {
        return httpConnection.getContentLength();
    }

    private static class HttpClientVerifier implements HostnameVerifier {

        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }

    private static class LocalhostHttpClientVerifier implements HostnameVerifier {

        public boolean verify(String s, SSLSession sslSession) {
            return "localhost".equalsIgnoreCase(s) || "127.0.0.1".equals(s);
        }
    }

    private static final class WSChunkedOuputStream extends FilterOutputStream {

        final int chunkSize;

        WSChunkedOuputStream(OutputStream actual, int chunkSize) {
            super(actual);
            this.chunkSize = chunkSize;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            while (len > 0) {
                int sent = (len > chunkSize) ? chunkSize : len;
                out.write(b, off, sent);
                len -= sent;
                off += sent;
            }
        }
    }
}
