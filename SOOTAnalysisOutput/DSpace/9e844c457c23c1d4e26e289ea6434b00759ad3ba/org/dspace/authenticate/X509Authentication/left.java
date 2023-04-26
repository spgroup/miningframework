package org.dspace.authenticate;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.dspace.eperson.EPerson;

public class X509Authentication implements AuthenticationMethod {

    private static Logger log = Logger.getLogger(X509Authentication.class);

    private static PublicKey caPublicKey = null;

    private static KeyStore caCertKeyStore = null;

    private static String loginPageTitle = null;

    private static String loginPageURL = null;

    static {
        loginPageTitle = ConfigurationManager.getProperty("authentication.x509.chooser.title.key");
        loginPageURL = ConfigurationManager.getProperty("authentication.x509.chooser.uri");
        String keystorePath = ConfigurationManager.getProperty("authentication.x509.keystore.path");
        String keystorePassword = ConfigurationManager.getProperty("authentication.x509.keystore.password");
        String caCertPath = ConfigurationManager.getProperty("authentication.x509.ca.cert");
        if (caCertPath == null)
            caCertPath = ConfigurationManager.getProperty("webui.cert.ca");
        if (keystorePath != null) {
            FileInputStream fis = null;
            if (keystorePassword == null)
                keystorePassword = "";
            try {
                KeyStore ks = KeyStore.getInstance("JKS");
                fis = new FileInputStream(keystorePath);
                ks.load(fis, keystorePassword.toCharArray());
                caCertKeyStore = ks;
            } catch (IOException e) {
                log.error("X509Authentication: Failed to load CA keystore, file=" + keystorePath + ", error=" + e.toString());
            } catch (GeneralSecurityException e) {
                log.error("X509Authentication: Failed to extract CA keystore, file=" + keystorePath + ", error=" + e.toString());
            } finally {
                if (fis != null)
                    try {
                        fis.close();
                    } catch (IOException ioe) {
                    }
            }
        }
        if (caCertPath != null) {
            InputStream is = null;
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(caCertPath);
                is = new BufferedInputStream(fis);
                X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is);
                if (cert != null)
                    caPublicKey = cert.getPublicKey();
            } catch (IOException e) {
                log.error("X509Authentication: Failed to load CA cert, file=" + caCertPath + ", error=" + e.toString());
            } catch (CertificateException e) {
                log.error("X509Authentication: Failed to extract CA cert, file=" + caCertPath + ", error=" + e.toString());
            } finally {
                if (is != null)
                    try {
                        is.close();
                    } catch (IOException ioe) {
                    }
                if (fis != null)
                    try {
                        fis.close();
                    } catch (IOException ioe) {
                    }
            }
        }
    }

    private static String getEmail(X509Certificate certificate) {
        Principal principal = certificate.getSubjectDN();
        if (principal == null)
            return null;
        String dn = principal.getName();
        if (dn == null)
            return null;
        StringTokenizer tokenizer = new StringTokenizer(dn, ",");
        String token = null;
        while (tokenizer.hasMoreTokens()) {
            int len = "emailaddress=".length();
            token = (String) tokenizer.nextToken();
            if (token.toLowerCase().startsWith("emailaddress=")) {
                if (token.length() <= len)
                    return null;
                return token.substring(len).toLowerCase();
            }
        }
        return null;
    }

    private static boolean isValid(Context context, X509Certificate certificate) {
        if (certificate == null)
            return false;
        try {
            certificate.checkValidity();
        } catch (CertificateException e) {
            log.info(LogManager.getHeader(context, "authentication", "X.509 Certificate is EXPIRED or PREMATURE: " + e.toString()));
            return false;
        }
        if (caPublicKey != null) {
            try {
                certificate.verify(caPublicKey);
                return true;
            } catch (GeneralSecurityException e) {
                log.info(LogManager.getHeader(context, "authentication", "X.509 Certificate FAILED SIGNATURE check: " + e.toString()));
            }
        }
        if (caCertKeyStore != null) {
            try {
                Enumeration ke = caCertKeyStore.aliases();
                while (ke.hasMoreElements()) {
                    String alias = (String) ke.nextElement();
                    if (caCertKeyStore.isCertificateEntry(alias)) {
                        Certificate ca = caCertKeyStore.getCertificate(alias);
                        try {
                            certificate.verify(ca.getPublicKey());
                            return true;
                        } catch (CertificateException ce) {
                        }
                    }
                }
                log.info(LogManager.getHeader(context, "authentication", "Keystore method FAILED SIGNATURE check on client cert."));
            } catch (GeneralSecurityException e) {
                log.info(LogManager.getHeader(context, "authentication", "X.509 Certificate FAILED SIGNATURE check: " + e.toString()));
            }
        }
        return false;
    }

    public boolean canSelfRegister(Context context, HttpServletRequest request, String username) {
        return ConfigurationManager.getBooleanProperty("authentication.x509.autoregister");
    }

    public void initEPerson(Context context, HttpServletRequest request, EPerson eperson) {
    }

    public boolean allowSetPassword(Context context, HttpServletRequest request, String username) {
        return false;
    }

    public boolean isImplicit() {
        return true;
    }

    public int[] getSpecialGroups(Context context, HttpServletRequest request) {
        return new int[0];
    }

    public int authenticate(Context context, String username, String password, String realm, HttpServletRequest request) {
        X509Certificate[] certs = null;
        if (request != null)
            certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        if ((certs == null) || (certs.length == 0))
            return BAD_ARGS;
        else {
            try {
                if (!isValid(context, certs[0])) {
                    log.warn(LogManager.getHeader(context, "authenticate", "type=x509certificate, status=BAD_CREDENTIALS (not valid)"));
                    return BAD_CREDENTIALS;
                }
                String email = getEmail(certs[0]);
                EPerson eperson = null;
                if (email != null)
                    eperson = EPerson.findByEmail(context, email);
                if (eperson == null) {
                    if (email != null && canSelfRegister(context, request, null)) {
                        log.info(LogManager.getHeader(context, "autoregister", "from=x.509, email=" + email));
                        context.setIgnoreAuthorization(true);
                        eperson = EPerson.create(context);
                        eperson.setEmail(email);
                        eperson.setCanLogIn(true);
                        AuthenticationManager.initEPerson(context, request, eperson);
                        eperson.update();
                        try {
                            context.commit();
                        } catch (java.sql.SQLException sqle) {
                            throw new RuntimeException(sqle);
                        }
                        context.setIgnoreAuthorization(false);
                        context.setCurrentUser(eperson);
                        return SUCCESS;
                    } else {
                        log.warn(LogManager.getHeader(context, "authenticate", "type=cert_but_no_record, cannot auto-register"));
                        return NO_SUCH_USER;
                    }
                } else if (!eperson.canLogIn()) {
                    log.warn(LogManager.getHeader(context, "authenticate", "type=x509certificate, email=" + email + ", canLogIn=false, rejecting."));
                    return BAD_ARGS;
                } else {
                    log.info(LogManager.getHeader(context, "login", "type=x509certificate"));
                    context.setCurrentUser(eperson);
                    return SUCCESS;
                }
            } catch (AuthorizeException ce) {
                log.warn(LogManager.getHeader(context, "authorize_exception", ""), ce);
            }
            return BAD_ARGS;
        }
    }

    public String loginPageURL(Context context, HttpServletRequest request, HttpServletResponse response) {
        return loginPageURL;
    }

    public String loginPageTitle(Context context) {
        return loginPageTitle;
    }
}
