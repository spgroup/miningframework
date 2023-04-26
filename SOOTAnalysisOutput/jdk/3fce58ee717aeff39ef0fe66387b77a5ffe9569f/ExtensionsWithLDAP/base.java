import java.io.IOException;
import java.io.StringBufferInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ExtensionsWithLDAP {

    private static final String CA_CERT = "" + "-----BEGIN CERTIFICATE-----\n" + "MIIC8TCCAdmgAwIBAgIJAJsSNtj5wdqqMA0GCSqGSIb3DQEBDQUAMA8xDTALBgNV\n" + "BAMMBFJvb3QwHhcNMTUwOTAxMTgwMzU5WhcNNDMwMTE3MTgwMzU5WjAPMQ0wCwYD\n" + "VQQDDARSb290MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvj892vPm\n" + "bB++x9QqqyBveP+ZqQ2B1stV7vh5JmDnOTevkZUOcemp3SXu/esNLSbpL+fARYXH\n" + "V5ubnrfip6RbvcxPfVIIDJrRTLIIsU6W7M6/LJLbLkEVGy4ZV4IHkOw9W2O92rcv\n" + "BkoqhzZnOTGR6uT3rRcKx4RevEKBKhZO+OPPf//lnckOybmYL7t7yQrajzHro76b\n" + "QTXYjAUq/DKhglXfC7vF/JzlAvG2IunGmIfjGcnuDo/9X3Bxef/q5TxCS35fvb7t\n" + "svC+g2QhTcBkQh4uNW2jSjlTIVp1uErCfP5aCjLaez5mqmb1hxPIlcvsNR23HwU6\n" + "bQO7z7NBo9Do6QIDAQABo1AwTjAdBgNVHQ4EFgQUmLZNOBBkqdYoElyxklPYHmAb\n" + "QXIwHwYDVR0jBBgwFoAUmLZNOBBkqdYoElyxklPYHmAbQXIwDAYDVR0TBAUwAwEB\n" + "/zANBgkqhkiG9w0BAQ0FAAOCAQEAYV4fOhDi5q7+XNXCxO8Eil2frR9jqdP4LaQp\n" + "3L0evW0gvPX68s2WmkPWzIu4TJcpdGFQqxyQFSXuKBXjthyiln77QItGTHWeafES\n" + "q5ESrKdSaJZq1bTIrrReCIP74f+fY/F4Tnb3dCqzaljXfzpdbeRsIW6gF71xcOUQ\n" + "nnPEjGVPLUegN+Wn/jQpeLxxIB7FmNXncdRUfMfZ43xVSKuMCy1UUYqJqTa/pXZj\n" + "jCMeRPThRjRqHlJ69jStfWUQATbLyj9KN09rUaJxzmUSt61UqJi7sjcGySaCjAJc\n" + "IcCdVmX/DmRLsdv8W36O3MgrvpT1zR3kaAlv2d8HppnBqcL3xg==\n" + "-----END CERTIFICATE-----";

    private static final String EE_CERT = "" + "-----BEGIN CERTIFICATE-----\n" + "MIIDHTCCAgWgAwIBAgIBBzANBgkqhkiG9w0BAQ0FADAPMQ0wCwYDVQQDDARSb290\n" + "MB4XDTE1MDkwMTE4MDM1OVoXDTQzMDExNzE4MDM1OVowDTELMAkGA1UEAwwCRUUw\n" + "ggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCpyz97liuWPDYcLH9TX8Bi\n" + "T78olCmAfmevvch6ncXUVuCzbdaKuKXwn4EVbDszsVJLoK5zdtP+X3iDhutj+IgK\n" + "mLhuczF3M9VIcWr+JJUyTH4+3h/RT8cjCDZOmk9iXkb5ifruVsLqzb9g+Vp140Oz\n" + "7leikne7KmclHvTfvFd0WDI7Gb9vo4f5rT717BXJ/n+M6pNk8DLpLiEu6eziYvXR\n" + "v5x+t5Go3x0eCXdaxEQUf2j876Wfr2qHRJK7lDfFe1DDsMg/KpKGiILYZ+g2qtVM\n" + "ZSxtp5BZEtfB5qV/IE5kWO+mCIAGpXSZIdbERR6pZUq8GLEe1T9e+sO6H24w2F19\n" + "AgMBAAGjgYUwgYIwNAYDVR0fBC0wKzApoCegJYYjbGRhcDovL2xkYXAuaG9zdC5m\n" + "b3IuY3JsZHAvbWFpbi5jcmwwSgYIKwYBBQUHAQEEPjA8MDoGCCsGAQUFBzAChi5s\n" + "ZGFwOi8vbGRhcC5ob3N0LmZvci5haWEvZGM9Um9vdD9jQUNlcnRpZmljYXRlMA0G\n" + "CSqGSIb3DQEBDQUAA4IBAQBWDfZHpuUx0yn5d3+BuztFqoks1MkGdk+USlH0TB1/\n" + "gWWBd+4S4PCKlpSur0gj2rMW4fP5HQfNlHci8JV8/bG4KuKRAXW56dg1818Hl3pc\n" + "iIrUSRn8uUjH3p9qb+Rb/u3mmVQRyJjN2t/zceNsO8/+Dd808OB9aEwGs8lMT0nn\n" + "ZYaaAqYz1GIY/Ecyx1vfEZEQ1ljo6i/r70C3igbypBUShxSiGsleiVTLOGNA+MN1\n" + "/a/Qh0bkaQyTGqK3bwvzzMeQVqWu2EWTBD/PmND5ExkpRICdv8LBVXfLnpoBr4lL\n" + "hnxn9+e0Ah+t8dS5EKfn44w5bI5PCu2bqxs6RCTxNjcY\n" + "-----END CERTIFICATE-----";

    private static final String LDAP_HOST_CRLDP = "ldap.host.for.crldp";

    private static final String LDAP_HOST_AIA = "ldap.host.for.aia";

    static final Date validationDate;

    static {
        try {
            validationDate = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).parse("Sep 02, 2015");
        } catch (ParseException e) {
            throw new RuntimeException("Couldn't parse date", e);
        }
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("com.sun.security.enableCRLDP", "true");
        System.setProperty("com.sun.security.enableAIAcaIssuers", "true");
        String hostsFileName = System.getProperty("test.src", ".") + "/ExtensionsWithLDAPHosts";
        System.setProperty("jdk.net.hosts.file", hostsFileName);
        X509Certificate trustedCert = loadCertificate(CA_CERT);
        X509Certificate eeCert = loadCertificate(EE_CERT);
        Set<TrustAnchor> trustedCertsSet = new HashSet<>();
        trustedCertsSet.add(new TrustAnchor(trustedCert, null));
        CertPath cp = (CertPath) CertificateFactory.getInstance("X509").generateCertPath(Arrays.asList(eeCert));
        PKIXParameters params = new PKIXParameters(trustedCertsSet);
        params.setDate(validationDate);
        try {
            CertPathValidator.getInstance("PKIX").validate(cp, params);
            throw new RuntimeException("CertPathValidatorException not thrown");
        } catch (CertPathValidatorException cpve) {
            System.out.println("Expected exception: " + cpve);
        }
        if (!LocalNameService.requestedHosts.contains(LDAP_HOST_CRLDP)) {
            throw new RuntimeException("A hostname from CRLDP extension not requested");
        }
        if (!LocalNameService.requestedHosts.contains(LDAP_HOST_AIA)) {
            throw new RuntimeException("A hostname from AIA extension not requested");
        }
        System.out.println("Test passed");
    }

    public static X509Certificate loadCertificate(String s) throws IOException, CertificateException {
        try (StringBufferInputStream is = new StringBufferInputStream(s)) {
            return (X509Certificate) CertificateFactory.getInstance("X509").generateCertificate(is);
        }
    }

    public static class LocalNameService {

        static final List<String> requestedHosts = new ArrayList<>();
    }
}
