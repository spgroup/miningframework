package sun.security.provider.certpath;

import java.net.URI;
import java.util.Collection;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidAlgorithmParameterException;
import java.security.cert.CertStore;
import java.security.cert.X509CertSelector;
import java.security.cert.X509CRLSelector;
import javax.security.auth.x500.X500Principal;
import java.io.IOException;

public interface CertStoreHelper {

    CertStore getCertStore(URI uri) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException;

    X509CertSelector wrap(X509CertSelector selector, X500Principal certSubject, String dn) throws IOException;

    X509CRLSelector wrap(X509CRLSelector selector, Collection<X500Principal> certIssuers, String dn) throws IOException;
}
