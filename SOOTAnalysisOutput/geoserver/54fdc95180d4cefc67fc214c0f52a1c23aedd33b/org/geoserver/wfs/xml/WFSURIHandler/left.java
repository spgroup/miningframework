package org.geoserver.wfs.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.URIHandlerImpl;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geoserver.ows.util.KvpMap;
import org.geoserver.ows.util.KvpUtils;
import org.geoserver.platform.Operation;
import org.geoserver.platform.Service;
import org.geoserver.wfs.DescribeFeatureType;
import org.geoserver.wfs.WFSInfo;
import org.geoserver.wfs.WFSInfo.Version;
import org.geoserver.wfs.kvp.DescribeFeatureTypeKvpRequestReader;
import org.geoserver.wfs.request.DescribeFeatureTypeRequest;
import org.geoserver.wfs.xml.v1_1_0.XmlSchemaEncoder;
import org.geotools.util.logging.Logging;

public class WFSURIHandler extends URIHandlerImpl {

    static final Logger LOGGER = Logging.getLogger(WFSURIHandler.class);

    static final Boolean DISABLED;

    static {
        DISABLED = Boolean.getBoolean(WFSURIHandler.class.getName() + ".disabled");
    }

    static final List<InetAddress> ADDRESSES = new ArrayList<InetAddress>();

    static {
        if (!DISABLED) {
            Enumeration<NetworkInterface> e = null;
            try {
                e = NetworkInterface.getNetworkInterfaces();
            } catch (SocketException ex) {
                LOGGER.log(Level.WARNING, "Unable to determine network interface info", ex);
            }
            while (e != null && e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                Enumeration<InetAddress> f = ni.getInetAddresses();
                while (f.hasMoreElements()) {
                    InetAddress add = f.nextElement();
                    add.getHostName();
                    ADDRESSES.add(add);
                }
            }
        }
    }

    GeoServer geoServer;

    public WFSURIHandler(GeoServer geoServer) {
        this.geoServer = geoServer;
    }

    @Override
    public boolean canHandle(URI uri) {
        if (DISABLED)
            return false;
        if (uriIsReflective(uri)) {
            String q = uri.query();
            if (q != null && !"".equals(q.trim())) {
                KvpMap kv = parseQueryString(q);
                if ("DescribeFeatureType".equalsIgnoreCase((String) kv.get("REQUEST")) || (uri.path().endsWith("DescribeFeatureType"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private KvpMap parseQueryString(String q) {
        return KvpUtils.normalize(KvpUtils.parseQueryString("?" + q));
    }

    private boolean uriIsReflective(URI uri) {
        String proxyBaseUrl = geoServer.getGlobal().getProxyBaseUrl();
        if (proxyBaseUrl != null) {
            try {
                URI proxyBaseUri = URI.createURI(proxyBaseUrl);
                if (uri.host().equals(proxyBaseUri.host())) {
                    return true;
                }
            } catch (IllegalArgumentException e) {
                LOGGER.fine("Unable to parse proxy base url to a uri: " + proxyBaseUrl);
            }
        }
        for (InetAddress add : ADDRESSES) {
            if (uri.host().equals(add.getHostAddress()) || uri.host().equals(add.getHostName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public InputStream createInputStream(URI uri, Map<?, ?> options) throws IOException {
        Catalog catalog = geoServer.getCatalog();
        try {
            KvpMap kv = parseQueryString(uri.query());
            WFSInfo.Version ver = WFSInfo.Version.negotiate((String) kv.get("VERSION"));
            if (ver == null) {
                ver = WFSInfo.Version.latest();
            }
            DescribeFeatureTypeKvpRequestReader dftReqReader = null;
            switch(ver) {
                case V_10:
                case V_11:
                    dftReqReader = new DescribeFeatureTypeKvpRequestReader(catalog);
                    break;
                default:
                    dftReqReader = new org.geoserver.wfs.kvp.v2_0.DescribeFeatureTypeKvpRequestReader(catalog);
            }
            KvpMap parsed = new KvpMap(kv);
            KvpUtils.parse(parsed);
            DescribeFeatureTypeRequest request = DescribeFeatureTypeRequest.adapt(dftReqReader.read(dftReqReader.createRequest(), parsed, kv));
            request.setBaseUrl(uri.scheme() + "://" + uri.host() + ":" + uri.port() + uri.path());
            DescribeFeatureType dft = new DescribeFeatureType(geoServer.getService(WFSInfo.class), catalog);
            FeatureTypeInfo[] featureTypes = dft.run(request);
            XmlSchemaEncoder schemaEncoder = null;
            switch(ver) {
                case V_10:
                    schemaEncoder = new XmlSchemaEncoder.V10(geoServer);
                    break;
                case V_11:
                    schemaEncoder = new XmlSchemaEncoder.V11(geoServer);
                    break;
                case V_20:
                default:
                    schemaEncoder = new XmlSchemaEncoder.V20(geoServer);
            }
            Operation op = new Operation("DescribeFeatureType", new Service("WFS", null, null, null), null, new Object[] { request.getAdaptee() });
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            schemaEncoder.write(featureTypes, bout, op);
            return new ByteArrayInputStream(bout.toByteArray());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to handle DescribeFeatureType uri: " + uri, e);
        }
        return super.createInputStream(uri, options);
    }
}
