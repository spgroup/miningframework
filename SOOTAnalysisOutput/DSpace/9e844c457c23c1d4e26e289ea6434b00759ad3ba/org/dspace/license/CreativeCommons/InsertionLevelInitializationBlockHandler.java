package org.dspace.license;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.Bundle;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.Utils;

public class CreativeCommons {

    public static final String CC_BUNDLE_NAME = "CC-LICENSE";

    private static final String CC_BS_SOURCE = "org.dspace.license.CreativeCommons";

    private static final String BSN_LICENSE_URL = "license_url";

    private static final String BSN_LICENSE_TEXT = "license_text";

    private static final String BSN_LICENSE_RDF = "license_rdf";

    protected static Templates templates = null;

    private static boolean enabled_p;

    static {
        enabled_p = ConfigurationManager.getBooleanProperty("webui.submit.enable-cc");
        if (enabled_p) {
            String proxyHost = ConfigurationManager.getProperty("http.proxy.host");
            String proxyPort = ConfigurationManager.getProperty("http.proxy.port");
            if ((proxyHost != null) && (proxyPort != null)) {
                System.setProperty("http.proxyHost", proxyHost);
                System.setProperty("http.proxyPort", proxyPort);
            }
        }
        try {
            templates = TransformerFactory.newInstance().newTemplates(new StreamSource(CreativeCommons.class.getResourceAsStream("CreativeCommons.xsl")));
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static boolean isEnabled() {
        return enabled_p;
    }

    private static Bundle getCcBundle(Item item) throws AuthorizeException, IOException {
        Bundle[] bundles = item.getBundles(CC_BUNDLE_NAME);
        if ((bundles.length > 0) && (bundles[0] != null)) {
            item.removeBundle(bundles[0]);
        }
        return item.createBundle(CC_BUNDLE_NAME);
    }

    public static void setLicense(Context context, Item item, String cc_license_url) throws IOException, AuthorizeException {
        Bundle bundle = getCcBundle(item);
        String license_text = fetchLicenseText(cc_license_url);
        String license_rdf = fetchLicenseRDF(cc_license_url);
        BitstreamFormat bs_format = BitstreamFormat.findByShortDescription(context, "License");
        setBitstreamFromBytes(item, bundle, BSN_LICENSE_URL, bs_format, cc_license_url.getBytes());
        setBitstreamFromBytes(item, bundle, BSN_LICENSE_TEXT, bs_format, license_text.getBytes());
        setBitstreamFromBytes(item, bundle, BSN_LICENSE_RDF, bs_format, license_rdf.getBytes());
    }

    public static void setLicense(Context context, Item item, InputStream licenseStm, String mimeType) throws IOException, AuthorizeException {
        Bundle bundle = getCcBundle(item);
        BitstreamFormat bs_format = BitstreamFormat.findByShortDescription(context, "License");
        Bitstream bs = bundle.createBitstream(licenseStm);
        bs.setSource(CC_BS_SOURCE);
        bs.setName((mimeType != null && (mimeType.equalsIgnoreCase("text/xml") || mimeType.equalsIgnoreCase("text/rdf"))) ? BSN_LICENSE_RDF : BSN_LICENSE_TEXT);
        bs.setFormat(bs_format);
        bs.update();
    }

    public static void removeLicense(Context context, Item item) throws IOException, AuthorizeException {
        Bundle[] bundles = item.getBundles(CC_BUNDLE_NAME);
        if ((bundles.length > 0) && (bundles[0] != null)) {
            item.removeBundle(bundles[0]);
        }
    }

    public static boolean hasLicense(Context context, Item item) throws IOException {
        Bundle[] bundles = item.getBundles(CC_BUNDLE_NAME);
        if (bundles.length == 0) {
            return false;
        }
        try {
            if ((getLicenseURL(item) == null) || (getLicenseText(item) == null) || (getLicenseRDF(item) == null)) {
                return false;
            }
        } catch (AuthorizeException ae) {
            return false;
        }
        return true;
    }

    public static String getLicenseURL(Item item) throws IOException, AuthorizeException {
        return getStringFromBitstream(item, BSN_LICENSE_URL);
    }

    public static String getLicenseText(Item item) throws IOException, AuthorizeException {
        return getStringFromBitstream(item, BSN_LICENSE_TEXT);
    }

    public static String getLicenseRDF(Item item) throws IOException, AuthorizeException {
        return getStringFromBitstream(item, BSN_LICENSE_RDF);
    }

    public static Bitstream getLicenseRdfBitstream(Item item) throws IOException, AuthorizeException {
        return getBitstream(item, BSN_LICENSE_RDF);
    }

    public static Bitstream getLicenseTextBitstream(Item item) throws IOException, AuthorizeException {
        return getBitstream(item, BSN_LICENSE_TEXT);
    }

    public static String fetchLicenseText(String license_url) {
        String text_url = license_url;
        byte[] urlBytes = fetchURL(text_url);
        return (urlBytes != null) ? new String(urlBytes) : "";
    }

    public static String fetchLicenseRDF(String license_url) {
        StringWriter result = new StringWriter();
        try {
            templates.newTransformer().transform(new StreamSource(license_url + "rdf"), new StreamResult(result));
        } catch (TransformerException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return result.getBuffer().toString();
    }

    private static void setBitstreamFromBytes(Item item, Bundle bundle, String bitstream_name, BitstreamFormat format, byte[] bytes) throws IOException, AuthorizeException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Bitstream bs = bundle.createBitstream(bais);
        bs.setName(bitstream_name);
        bs.setSource(CC_BS_SOURCE);
        bs.setFormat(format);
        bs.update();
    }

    private static String getStringFromBitstream(Item item, String bitstream_name) throws IOException, AuthorizeException {
        byte[] bytes = getBytesFromBitstream(item, bitstream_name);
        if (bytes == null) {
            return null;
        }
        return new String(bytes);
    }

    private static Bitstream getBitstream(Item item, String bitstream_name) throws IOException, AuthorizeException {
        Bundle cc_bundle = null;
        try {
            Bundle[] bundles = item.getBundles(CC_BUNDLE_NAME);
            if ((bundles != null) && (bundles.length > 0)) {
                cc_bundle = bundles[0];
            } else {
                return null;
            }
        } catch (Exception exc) {
            return null;
        }
        return cc_bundle.getBitstreamByName(bitstream_name);
    }

    private static byte[] getBytesFromBitstream(Item item, String bitstream_name) throws IOException, AuthorizeException {
        Bitstream bs = getBitstream(item, bitstream_name);
        if (bs == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Utils.copy(bs.retrieve(), baos);
        return baos.toByteArray();
    }

    private static byte[] fetchURL(String url_string) {
        try {
            URL url = new URL(url_string);
            URLConnection connection = url.openConnection();
            byte[] bytes = new byte[connection.getContentLength()];
            int offset = 0;
            while (true) {
                int len = connection.getInputStream().read(bytes, offset, bytes.length - offset);
                if (len == -1) {
                    break;
                }
                offset += len;
            }
            return bytes;
        } catch (Exception exc) {
            return null;
        }
    }
}