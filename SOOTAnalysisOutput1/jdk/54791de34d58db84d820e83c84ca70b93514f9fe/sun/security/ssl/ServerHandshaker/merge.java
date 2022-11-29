package sun.security.ssl;

import java.io.*;
import java.util.*;
import java.security.*;
import java.security.cert.*;
import java.security.interfaces.*;
import java.security.spec.ECParameterSpec;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.*;
import javax.security.auth.Subject;
import sun.security.util.KeyUtil;
import sun.security.action.GetPropertyAction;
import sun.security.ssl.HandshakeMessage.*;
import sun.security.ssl.CipherSuite.*;
import sun.security.ssl.SignatureAndHashAlgorithm.*;
import static sun.security.ssl.CipherSuite.KeyExchange.*;

final class ServerHandshaker extends Handshaker {

    private byte doClientAuth;

    private X509Certificate[] certs;

    private PrivateKey privateKey;

    private Object serviceCreds;

    private boolean needClientVerify = false;

    private PrivateKey tempPrivateKey;

    private PublicKey tempPublicKey;

    private DHCrypt dh;

    private ECDHCrypt ecdh;

    private ProtocolVersion clientRequestedVersion;

    private SupportedEllipticCurvesExtension supportedCurves;

    SignatureAndHashAlgorithm preferableSignatureAlgorithm;

    private static final boolean useSmartEphemeralDHKeys;

    private static final boolean useLegacyEphemeralDHKeys;

    private static final int customizedDHKeySize;

    static {
        String property = AccessController.doPrivileged(new GetPropertyAction("jdk.tls.ephemeralDHKeySize"));
        if (property == null || property.length() == 0) {
            useLegacyEphemeralDHKeys = false;
            useSmartEphemeralDHKeys = false;
            customizedDHKeySize = -1;
        } else if ("matched".equals(property)) {
            useLegacyEphemeralDHKeys = false;
            useSmartEphemeralDHKeys = true;
            customizedDHKeySize = -1;
        } else if ("legacy".equals(property)) {
            useLegacyEphemeralDHKeys = true;
            useSmartEphemeralDHKeys = false;
            customizedDHKeySize = -1;
        } else {
            useLegacyEphemeralDHKeys = false;
            useSmartEphemeralDHKeys = false;
            try {
                customizedDHKeySize = Integer.parseUnsignedInt(property);
                if (customizedDHKeySize < 1024 || customizedDHKeySize > 2048) {
                    throw new IllegalArgumentException("Customized DH key size should be positive integer " + "between 1024 and 2048 bits, inclusive");
                }
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Invalid system property jdk.tls.ephemeralDHKeySize");
            }
        }
    }

    ServerHandshaker(SSLSocketImpl socket, SSLContextImpl context, ProtocolList enabledProtocols, byte clientAuth, ProtocolVersion activeProtocolVersion, boolean isInitialHandshake, boolean secureRenegotiation, byte[] clientVerifyData, byte[] serverVerifyData) {
        super(socket, context, enabledProtocols, (clientAuth != SSLEngineImpl.clauth_none), false, activeProtocolVersion, isInitialHandshake, secureRenegotiation, clientVerifyData, serverVerifyData);
        doClientAuth = clientAuth;
    }

    ServerHandshaker(SSLEngineImpl engine, SSLContextImpl context, ProtocolList enabledProtocols, byte clientAuth, ProtocolVersion activeProtocolVersion, boolean isInitialHandshake, boolean secureRenegotiation, byte[] clientVerifyData, byte[] serverVerifyData) {
        super(engine, context, enabledProtocols, (clientAuth != SSLEngineImpl.clauth_none), false, activeProtocolVersion, isInitialHandshake, secureRenegotiation, clientVerifyData, serverVerifyData);
        doClientAuth = clientAuth;
    }

    void setClientAuth(byte clientAuth) {
        doClientAuth = clientAuth;
    }

    @Override
    void processMessage(byte type, int message_len) throws IOException {
        if ((state >= type) && (state != HandshakeMessage.ht_client_key_exchange && type != HandshakeMessage.ht_certificate_verify)) {
            throw new SSLProtocolException("Handshake message sequence violation, state = " + state + ", type = " + type);
        }
        switch(type) {
            case HandshakeMessage.ht_client_hello:
                ClientHello ch = new ClientHello(input, message_len);
                this.clientHello(ch);
                break;
            case HandshakeMessage.ht_certificate:
                if (doClientAuth == SSLEngineImpl.clauth_none) {
                    fatalSE(Alerts.alert_unexpected_message, "client sent unsolicited cert chain");
                }
                this.clientCertificate(new CertificateMsg(input));
                break;
            case HandshakeMessage.ht_client_key_exchange:
                SecretKey preMasterSecret;
                switch(keyExchange) {
                    case K_RSA:
                    case K_RSA_EXPORT:
                        RSAClientKeyExchange pms = new RSAClientKeyExchange(protocolVersion, clientRequestedVersion, sslContext.getSecureRandom(), input, message_len, privateKey);
                        preMasterSecret = this.clientKeyExchange(pms);
                        break;
                    case K_KRB5:
                    case K_KRB5_EXPORT:
                        preMasterSecret = this.clientKeyExchange(new KerberosClientKeyExchange(protocolVersion, clientRequestedVersion, sslContext.getSecureRandom(), input, this.getAccSE(), serviceCreds));
                        break;
                    case K_DHE_RSA:
                    case K_DHE_DSS:
                    case K_DH_ANON:
                        preMasterSecret = this.clientKeyExchange(new DHClientKeyExchange(input));
                        break;
                    case K_ECDH_RSA:
                    case K_ECDH_ECDSA:
                    case K_ECDHE_RSA:
                    case K_ECDHE_ECDSA:
                    case K_ECDH_ANON:
                        preMasterSecret = this.clientKeyExchange(new ECDHClientKeyExchange(input));
                        break;
                    default:
                        throw new SSLProtocolException("Unrecognized key exchange: " + keyExchange);
                }
                calculateKeys(preMasterSecret, clientRequestedVersion);
                break;
            case HandshakeMessage.ht_certificate_verify:
                this.clientCertificateVerify(new CertificateVerify(input, localSupportedSignAlgs, protocolVersion));
                break;
            case HandshakeMessage.ht_finished:
                this.clientFinished(new Finished(protocolVersion, input, cipherSuite));
                break;
            default:
                throw new SSLProtocolException("Illegal server handshake msg, " + type);
        }
        if (state < type) {
            if (type == HandshakeMessage.ht_certificate_verify) {
                state = type + 2;
            } else {
                state = type;
            }
        }
    }

    private void clientHello(ClientHello mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        if (rejectClientInitiatedRenego && !isInitialHandshake && state != HandshakeMessage.ht_hello_request) {
            fatalSE(Alerts.alert_handshake_failure, "Client initiated renegotiation is not allowed");
        }
        ServerNameExtension clientHelloSNIExt = (ServerNameExtension) mesg.extensions.get(ExtensionType.EXT_SERVER_NAME);
        if (!sniMatchers.isEmpty()) {
            if (clientHelloSNIExt != null && !clientHelloSNIExt.isMatched(sniMatchers)) {
                fatalSE(Alerts.alert_unrecognized_name, "Unrecognized server name indication");
            }
        }
        boolean renegotiationIndicated = false;
        CipherSuiteList cipherSuites = mesg.getCipherSuites();
        if (cipherSuites.contains(CipherSuite.C_SCSV)) {
            renegotiationIndicated = true;
            if (isInitialHandshake) {
                secureRenegotiation = true;
            } else {
                if (secureRenegotiation) {
                    fatalSE(Alerts.alert_handshake_failure, "The SCSV is present in a secure renegotiation");
                } else {
                    fatalSE(Alerts.alert_handshake_failure, "The SCSV is present in a insecure renegotiation");
                }
            }
        }
        RenegotiationInfoExtension clientHelloRI = (RenegotiationInfoExtension) mesg.extensions.get(ExtensionType.EXT_RENEGOTIATION_INFO);
        if (clientHelloRI != null) {
            renegotiationIndicated = true;
            if (isInitialHandshake) {
                if (!clientHelloRI.isEmpty()) {
                    fatalSE(Alerts.alert_handshake_failure, "The renegotiation_info field is not empty");
                }
                secureRenegotiation = true;
            } else {
                if (!secureRenegotiation) {
                    fatalSE(Alerts.alert_handshake_failure, "The renegotiation_info is present in a insecure " + "renegotiation");
                }
                if (!Arrays.equals(clientVerifyData, clientHelloRI.getRenegotiatedConnection())) {
                    fatalSE(Alerts.alert_handshake_failure, "Incorrect verify data in ClientHello " + "renegotiation_info message");
                }
            }
        } else if (!isInitialHandshake && secureRenegotiation) {
            fatalSE(Alerts.alert_handshake_failure, "Inconsistent secure renegotiation indication");
        }
        if (!renegotiationIndicated || !secureRenegotiation) {
            if (isInitialHandshake) {
                if (!allowLegacyHelloMessages) {
                    fatalSE(Alerts.alert_handshake_failure, "Failed to negotiate the use of secure renegotiation");
                }
                if (debug != null && Debug.isOn("handshake")) {
                    System.out.println("Warning: No renegotiation " + "indication in ClientHello, allow legacy ClientHello");
                }
            } else if (!allowUnsafeRenegotiation) {
                if (activeProtocolVersion.v >= ProtocolVersion.TLS10.v) {
                    warningSE(Alerts.alert_no_renegotiation);
                    invalidated = true;
                    if (input.available() > 0) {
                        fatalSE(Alerts.alert_unexpected_message, "ClientHello followed by an unexpected  " + "handshake message");
                    }
                    return;
                } else {
                    fatalSE(Alerts.alert_handshake_failure, "Renegotiation is not allowed");
                }
            } else {
                if (debug != null && Debug.isOn("handshake")) {
                    System.out.println("Warning: continue with insecure renegotiation");
                }
            }
        }
        input.digestNow();
        ServerHello m1 = new ServerHello();
        clientRequestedVersion = mesg.protocolVersion;
        ProtocolVersion selectedVersion = selectProtocolVersion(clientRequestedVersion);
        if (selectedVersion == null || selectedVersion.v == ProtocolVersion.SSL20Hello.v) {
            fatalSE(Alerts.alert_handshake_failure, "Client requested protocol " + clientRequestedVersion + " not enabled or not supported");
        }
        handshakeHash.protocolDetermined(selectedVersion);
        setVersion(selectedVersion);
        m1.protocolVersion = protocolVersion;
        clnt_random = mesg.clnt_random;
        svr_random = new RandomCookie(sslContext.getSecureRandom());
        m1.svr_random = svr_random;
        session = null;
        if (mesg.sessionId.length() != 0) {
            SSLSessionImpl previous = ((SSLSessionContextImpl) sslContext.engineGetServerSessionContext()).get(mesg.sessionId.getId());
            if (previous != null) {
                resumingSession = previous.isRejoinable();
                if (resumingSession) {
                    ProtocolVersion oldVersion = previous.getProtocolVersion();
                    if (oldVersion != protocolVersion) {
                        resumingSession = false;
                    }
                }
                if (resumingSession) {
                    List<SNIServerName> oldServerNames = previous.getRequestedServerNames();
                    if (clientHelloSNIExt != null) {
                        if (!clientHelloSNIExt.isIdentical(oldServerNames)) {
                            resumingSession = false;
                        }
                    } else if (!oldServerNames.isEmpty()) {
                        resumingSession = false;
                    }
                    if (!resumingSession && debug != null && Debug.isOn("handshake")) {
                        System.out.println("The requested server name indication " + "is not identical to the previous one");
                    }
                }
                if (resumingSession && (doClientAuth == SSLEngineImpl.clauth_required)) {
                    try {
                        previous.getPeerPrincipal();
                    } catch (SSLPeerUnverifiedException e) {
                        resumingSession = false;
                    }
                }
                if (resumingSession) {
                    CipherSuite suite = previous.getSuite();
                    if (suite.keyExchange == K_KRB5 || suite.keyExchange == K_KRB5_EXPORT) {
                        Principal localPrincipal = previous.getLocalPrincipal();
                        Subject subject = null;
                        try {
                            subject = AccessController.doPrivileged(new PrivilegedExceptionAction<Subject>() {

                                @Override
                                public Subject run() throws Exception {
                                    return Krb5Helper.getServerSubject(getAccSE());
                                }
                            });
                        } catch (PrivilegedActionException e) {
                            subject = null;
                            if (debug != null && Debug.isOn("session")) {
                                System.out.println("Attempt to obtain" + " subject failed!");
                            }
                        }
                        if (subject != null) {
                            if (Krb5Helper.isRelated(subject, localPrincipal)) {
                                if (debug != null && Debug.isOn("session"))
                                    System.out.println("Subject can" + " provide creds for princ");
                            } else {
                                resumingSession = false;
                                if (debug != null && Debug.isOn("session"))
                                    System.out.println("Subject cannot" + " provide creds for princ");
                            }
                        } else {
                            resumingSession = false;
                            if (debug != null && Debug.isOn("session"))
                                System.out.println("Kerberos credentials are" + " not present in the current Subject;" + " check if " + " javax.security.auth.useSubjectAsCreds" + " system property has been set to false");
                        }
                    }
                }
                if (resumingSession) {
                    CipherSuite suite = previous.getSuite();
                    if ((isNegotiable(suite) == false) || (mesg.getCipherSuites().contains(suite) == false)) {
                        resumingSession = false;
                    } else {
                        setCipherSuite(suite);
                    }
                }
                if (resumingSession) {
                    session = previous;
                    if (debug != null && (Debug.isOn("handshake") || Debug.isOn("session"))) {
                        System.out.println("%% Resuming " + session);
                    }
                }
            }
        }
        if (session == null) {
            if (!enableNewSession) {
                throw new SSLException("Client did not resume a session");
            }
            supportedCurves = (SupportedEllipticCurvesExtension) mesg.extensions.get(ExtensionType.EXT_ELLIPTIC_CURVES);
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                SignatureAlgorithmsExtension signAlgs = (SignatureAlgorithmsExtension) mesg.extensions.get(ExtensionType.EXT_SIGNATURE_ALGORITHMS);
                if (signAlgs != null) {
                    Collection<SignatureAndHashAlgorithm> peerSignAlgs = signAlgs.getSignAlgorithms();
                    if (peerSignAlgs == null || peerSignAlgs.isEmpty()) {
                        throw new SSLHandshakeException("No peer supported signature algorithms");
                    }
                    Collection<SignatureAndHashAlgorithm> supportedPeerSignAlgs = SignatureAndHashAlgorithm.getSupportedAlgorithms(peerSignAlgs);
                    if (supportedPeerSignAlgs.isEmpty()) {
                        throw new SSLHandshakeException("No supported signature and hash algorithm " + "in common");
                    }
                    setPeerSupportedSignAlgs(supportedPeerSignAlgs);
                }
            }
            session = new SSLSessionImpl(protocolVersion, CipherSuite.C_NULL, getLocalSupportedSignAlgs(), sslContext.getSecureRandom(), getHostAddressSE(), getPortSE());
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                if (peerSupportedSignAlgs != null) {
                    session.setPeerSupportedSignatureAlgorithms(peerSupportedSignAlgs);
                }
            }
            List<SNIServerName> clientHelloSNI = Collections.<SNIServerName>emptyList();
            if (clientHelloSNIExt != null) {
                clientHelloSNI = clientHelloSNIExt.getServerNames();
            }
            session.setRequestedServerNames(clientHelloSNI);
            setHandshakeSessionSE(session);
            chooseCipherSuite(mesg);
            session.setSuite(cipherSuite);
            session.setLocalPrivateKey(privateKey);
        } else {
            setHandshakeSessionSE(session);
        }
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            handshakeHash.setFinishedAlg(cipherSuite.prfAlg.getPRFHashAlg());
        }
        m1.cipherSuite = cipherSuite;
        m1.sessionId = session.getSessionId();
        m1.compression_method = session.getCompression();
        if (secureRenegotiation) {
            HelloExtension serverHelloRI = new RenegotiationInfoExtension(clientVerifyData, serverVerifyData);
            m1.extensions.add(serverHelloRI);
        }
        if (!sniMatchers.isEmpty() && clientHelloSNIExt != null) {
            if (!resumingSession) {
                ServerNameExtension serverHelloSNI = new ServerNameExtension();
                m1.extensions.add(serverHelloSNI);
            }
        }
        if (debug != null && Debug.isOn("handshake")) {
            m1.print(System.out);
            System.out.println("Cipher suite:  " + session.getSuite());
        }
        m1.write(output);
        if (resumingSession) {
            calculateConnectionKeys(session.getMasterSecret());
            sendChangeCipherAndFinish(false);
            return;
        }
        if (keyExchange == K_KRB5 || keyExchange == K_KRB5_EXPORT) {
        } else if ((keyExchange != K_DH_ANON) && (keyExchange != K_ECDH_ANON)) {
            if (certs == null) {
                throw new RuntimeException("no certificates");
            }
            CertificateMsg m2 = new CertificateMsg(certs);
            session.setLocalCertificates(certs);
            if (debug != null && Debug.isOn("handshake")) {
                m2.print(System.out);
            }
            m2.write(output);
        } else {
            if (certs != null) {
                throw new RuntimeException("anonymous keyexchange with certs");
            }
        }
        ServerKeyExchange m3;
        switch(keyExchange) {
            case K_RSA:
            case K_KRB5:
            case K_KRB5_EXPORT:
                m3 = null;
                break;
            case K_RSA_EXPORT:
                if (JsseJce.getRSAKeyLength(certs[0].getPublicKey()) > 512) {
                    try {
                        m3 = new RSA_ServerKeyExchange(tempPublicKey, privateKey, clnt_random, svr_random, sslContext.getSecureRandom());
                        privateKey = tempPrivateKey;
                    } catch (GeneralSecurityException e) {
                        throwSSLException("Error generating RSA server key exchange", e);
                        m3 = null;
                    }
                } else {
                    m3 = null;
                }
                break;
            case K_DHE_RSA:
            case K_DHE_DSS:
                try {
                    m3 = new DH_ServerKeyExchange(dh, privateKey, clnt_random.random_bytes, svr_random.random_bytes, sslContext.getSecureRandom(), preferableSignatureAlgorithm, protocolVersion);
                } catch (GeneralSecurityException e) {
                    throwSSLException("Error generating DH server key exchange", e);
                    m3 = null;
                }
                break;
            case K_DH_ANON:
                m3 = new DH_ServerKeyExchange(dh, protocolVersion);
                break;
            case K_ECDHE_RSA:
            case K_ECDHE_ECDSA:
            case K_ECDH_ANON:
                try {
                    m3 = new ECDH_ServerKeyExchange(ecdh, privateKey, clnt_random.random_bytes, svr_random.random_bytes, sslContext.getSecureRandom(), preferableSignatureAlgorithm, protocolVersion);
                } catch (GeneralSecurityException e) {
                    throwSSLException("Error generating ECDH server key exchange", e);
                    m3 = null;
                }
                break;
            case K_ECDH_RSA:
            case K_ECDH_ECDSA:
                m3 = null;
                break;
            default:
                throw new RuntimeException("internal error: " + keyExchange);
        }
        if (m3 != null) {
            if (debug != null && Debug.isOn("handshake")) {
                m3.print(System.out);
            }
            m3.write(output);
        }
        if (doClientAuth != SSLEngineImpl.clauth_none && keyExchange != K_DH_ANON && keyExchange != K_ECDH_ANON && keyExchange != K_KRB5 && keyExchange != K_KRB5_EXPORT) {
            CertificateRequest m4;
            X509Certificate[] caCerts;
            Collection<SignatureAndHashAlgorithm> localSignAlgs = null;
            if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                localSignAlgs = getLocalSupportedSignAlgs();
                if (localSignAlgs.isEmpty()) {
                    throw new SSLHandshakeException("No supported signature algorithm");
                }
                Set<String> localHashAlgs = SignatureAndHashAlgorithm.getHashAlgorithmNames(localSignAlgs);
                if (localHashAlgs.isEmpty()) {
                    throw new SSLHandshakeException("No supported signature algorithm");
                }
            }
            caCerts = sslContext.getX509TrustManager().getAcceptedIssuers();
            m4 = new CertificateRequest(caCerts, keyExchange, localSignAlgs, protocolVersion);
            if (debug != null && Debug.isOn("handshake")) {
                m4.print(System.out);
            }
            m4.write(output);
        }
        ServerHelloDone m5 = new ServerHelloDone();
        if (debug != null && Debug.isOn("handshake")) {
            m5.print(System.out);
        }
        m5.write(output);
        output.flush();
    }

    private void chooseCipherSuite(ClientHello mesg) throws IOException {
        CipherSuiteList prefered;
        CipherSuiteList proposed;
        if (preferLocalCipherSuites) {
            prefered = getActiveCipherSuites();
            proposed = mesg.getCipherSuites();
        } else {
            prefered = mesg.getCipherSuites();
            proposed = getActiveCipherSuites();
        }
        for (CipherSuite suite : prefered.collection()) {
            if (isNegotiable(proposed, suite) == false) {
                continue;
            }
            if (doClientAuth == SSLEngineImpl.clauth_required) {
                if ((suite.keyExchange == K_DH_ANON) || (suite.keyExchange == K_ECDH_ANON)) {
                    continue;
                }
            }
            if (trySetCipherSuite(suite) == false) {
                continue;
            }
            return;
        }
        fatalSE(Alerts.alert_handshake_failure, "no cipher suites in common");
    }

    boolean trySetCipherSuite(CipherSuite suite) {
        if (resumingSession) {
            return true;
        }
        if (suite.isNegotiable() == false) {
            return false;
        }
        if (protocolVersion.v >= suite.obsoleted) {
            return false;
        }
        if (protocolVersion.v < suite.supported) {
            return false;
        }
        KeyExchange keyExchange = suite.keyExchange;
        privateKey = null;
        certs = null;
        dh = null;
        tempPrivateKey = null;
        tempPublicKey = null;
        Collection<SignatureAndHashAlgorithm> supportedSignAlgs = null;
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            if (peerSupportedSignAlgs != null) {
                supportedSignAlgs = peerSupportedSignAlgs;
            } else {
                SignatureAndHashAlgorithm algorithm = null;
                switch(keyExchange) {
                    case K_RSA:
                    case K_DHE_RSA:
                    case K_DH_RSA:
                    case K_ECDH_RSA:
                    case K_ECDHE_RSA:
                        algorithm = SignatureAndHashAlgorithm.valueOf(HashAlgorithm.SHA1.value, SignatureAlgorithm.RSA.value, 0);
                        break;
                    case K_DHE_DSS:
                    case K_DH_DSS:
                        algorithm = SignatureAndHashAlgorithm.valueOf(HashAlgorithm.SHA1.value, SignatureAlgorithm.DSA.value, 0);
                        break;
                    case K_ECDH_ECDSA:
                    case K_ECDHE_ECDSA:
                        algorithm = SignatureAndHashAlgorithm.valueOf(HashAlgorithm.SHA1.value, SignatureAlgorithm.ECDSA.value, 0);
                        break;
                    default:
                }
                if (algorithm == null) {
                    supportedSignAlgs = Collections.<SignatureAndHashAlgorithm>emptySet();
                } else {
                    supportedSignAlgs = new ArrayList<SignatureAndHashAlgorithm>(1);
                    supportedSignAlgs.add(algorithm);
                }
                session.setPeerSupportedSignatureAlgorithms(supportedSignAlgs);
            }
        }
        switch(keyExchange) {
            case K_RSA:
                if (setupPrivateKeyAndChain("RSA") == false) {
                    return false;
                }
                break;
            case K_RSA_EXPORT:
                if (setupPrivateKeyAndChain("RSA") == false) {
                    return false;
                }
                try {
                    if (JsseJce.getRSAKeyLength(certs[0].getPublicKey()) > 512) {
                        if (!setupEphemeralRSAKeys(suite.exportable)) {
                            return false;
                        }
                    }
                } catch (RuntimeException e) {
                    return false;
                }
                break;
            case K_DHE_RSA:
                if (setupPrivateKeyAndChain("RSA") == false) {
                    return false;
                }
                if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    preferableSignatureAlgorithm = SignatureAndHashAlgorithm.getPreferableAlgorithm(supportedSignAlgs, "RSA", privateKey);
                    if (preferableSignatureAlgorithm == null) {
                        return false;
                    }
                }
                setupEphemeralDHKeys(suite.exportable, privateKey);
                break;
            case K_ECDHE_RSA:
                if (setupPrivateKeyAndChain("RSA") == false) {
                    return false;
                }
                if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    preferableSignatureAlgorithm = SignatureAndHashAlgorithm.getPreferableAlgorithm(supportedSignAlgs, "RSA", privateKey);
                    if (preferableSignatureAlgorithm == null) {
                        return false;
                    }
                }
                if (setupEphemeralECDHKeys() == false) {
                    return false;
                }
                break;
            case K_DHE_DSS:
                if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    preferableSignatureAlgorithm = SignatureAndHashAlgorithm.getPreferableAlgorithm(supportedSignAlgs, "DSA");
                    if (preferableSignatureAlgorithm == null) {
                        return false;
                    }
                }
                if (setupPrivateKeyAndChain("DSA") == false) {
                    return false;
                }
                setupEphemeralDHKeys(suite.exportable, privateKey);
                break;
            case K_ECDHE_ECDSA:
                if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
                    preferableSignatureAlgorithm = SignatureAndHashAlgorithm.getPreferableAlgorithm(supportedSignAlgs, "ECDSA");
                    if (preferableSignatureAlgorithm == null) {
                        return false;
                    }
                }
                if (setupPrivateKeyAndChain("EC_EC") == false) {
                    return false;
                }
                if (setupEphemeralECDHKeys() == false) {
                    return false;
                }
                break;
            case K_ECDH_RSA:
                if (setupPrivateKeyAndChain("EC_RSA") == false) {
                    return false;
                }
                setupStaticECDHKeys();
                break;
            case K_ECDH_ECDSA:
                if (setupPrivateKeyAndChain("EC_EC") == false) {
                    return false;
                }
                setupStaticECDHKeys();
                break;
            case K_KRB5:
            case K_KRB5_EXPORT:
                if (!setupKerberosKeys()) {
                    return false;
                }
                break;
            case K_DH_ANON:
                setupEphemeralDHKeys(suite.exportable, null);
                break;
            case K_ECDH_ANON:
                if (setupEphemeralECDHKeys() == false) {
                    return false;
                }
                break;
            default:
                throw new RuntimeException("Unrecognized cipherSuite: " + suite);
        }
        setCipherSuite(suite);
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            if (peerSupportedSignAlgs == null) {
                setPeerSupportedSignAlgs(supportedSignAlgs);
            }
        }
        return true;
    }

    private boolean setupEphemeralRSAKeys(boolean export) {
        KeyPair kp = sslContext.getEphemeralKeyManager().getRSAKeyPair(export, sslContext.getSecureRandom());
        if (kp == null) {
            return false;
        } else {
            tempPublicKey = kp.getPublic();
            tempPrivateKey = kp.getPrivate();
            return true;
        }
    }

    private void setupEphemeralDHKeys(boolean export, Key key) {
        int keySize = export ? 512 : 1024;
        if (!export) {
            if (useLegacyEphemeralDHKeys) {
                keySize = 768;
            } else if (useSmartEphemeralDHKeys) {
                if (key != null) {
                    int ks = KeyUtil.getKeySize(key);
                    keySize = ks <= 1024 ? 1024 : 2048;
                }
            } else if (customizedDHKeySize > 0) {
                keySize = customizedDHKeySize;
            }
        }
        dh = new DHCrypt(keySize, sslContext.getSecureRandom());
    }

    private boolean setupEphemeralECDHKeys() {
        int index = -1;
        if (supportedCurves != null) {
            for (int curveId : supportedCurves.curveIds()) {
                if (SupportedEllipticCurvesExtension.isSupported(curveId)) {
                    index = curveId;
                    break;
                }
            }
            if (index < 0) {
                return false;
            }
        } else {
            index = SupportedEllipticCurvesExtension.DEFAULT.curveIds()[0];
        }
        String oid = SupportedEllipticCurvesExtension.getCurveOid(index);
        ecdh = new ECDHCrypt(oid, sslContext.getSecureRandom());
        return true;
    }

    private void setupStaticECDHKeys() {
        ecdh = new ECDHCrypt(privateKey, certs[0].getPublicKey());
    }

    private boolean setupPrivateKeyAndChain(String algorithm) {
        X509ExtendedKeyManager km = sslContext.getX509KeyManager();
        String alias;
        if (conn != null) {
            alias = km.chooseServerAlias(algorithm, null, conn);
        } else {
            alias = km.chooseEngineServerAlias(algorithm, null, engine);
        }
        if (alias == null) {
            return false;
        }
        PrivateKey tempPrivateKey = km.getPrivateKey(alias);
        if (tempPrivateKey == null) {
            return false;
        }
        X509Certificate[] tempCerts = km.getCertificateChain(alias);
        if ((tempCerts == null) || (tempCerts.length == 0)) {
            return false;
        }
        String keyAlgorithm = algorithm.split("_")[0];
        PublicKey publicKey = tempCerts[0].getPublicKey();
        if ((tempPrivateKey.getAlgorithm().equals(keyAlgorithm) == false) || (publicKey.getAlgorithm().equals(keyAlgorithm) == false)) {
            return false;
        }
        if (keyAlgorithm.equals("EC")) {
            if (publicKey instanceof ECPublicKey == false) {
                return false;
            }
            ECParameterSpec params = ((ECPublicKey) publicKey).getParams();
            int index = SupportedEllipticCurvesExtension.getCurveIndex(params);
            if (SupportedEllipticCurvesExtension.isSupported(index) == false) {
                return false;
            }
            if ((supportedCurves != null) && !supportedCurves.contains(index)) {
                return false;
            }
        }
        this.privateKey = tempPrivateKey;
        this.certs = tempCerts;
        return true;
    }

    private boolean setupKerberosKeys() {
        if (serviceCreds != null) {
            return true;
        }
        try {
            final AccessControlContext acc = getAccSE();
            serviceCreds = AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

                @Override
                public Object run() throws Exception {
                    return Krb5Helper.getServiceCreds(acc);
                }
            });
            if (serviceCreds != null) {
                if (debug != null && Debug.isOn("handshake")) {
                    System.out.println("Using Kerberos creds");
                }
                String serverPrincipal = Krb5Helper.getServerPrincipalName(serviceCreds);
                if (serverPrincipal != null) {
                    SecurityManager sm = System.getSecurityManager();
                    try {
                        if (sm != null) {
                            sm.checkPermission(Krb5Helper.getServicePermission(serverPrincipal, "accept"), acc);
                        }
                    } catch (SecurityException se) {
                        serviceCreds = null;
                        if (debug != null && Debug.isOn("handshake")) {
                            System.out.println("Permission to access Kerberos" + " secret key denied");
                        }
                        return false;
                    }
                }
            }
            return serviceCreds != null;
        } catch (PrivilegedActionException e) {
            if (debug != null && Debug.isOn("handshake")) {
                System.out.println("Attempt to obtain Kerberos key failed: " + e.toString());
            }
            return false;
        }
    }

    private SecretKey clientKeyExchange(KerberosClientKeyExchange mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        session.setPeerPrincipal(mesg.getPeerPrincipal());
        session.setLocalPrincipal(mesg.getLocalPrincipal());
        byte[] b = mesg.getUnencryptedPreMasterSecret();
        return new SecretKeySpec(b, "TlsPremasterSecret");
    }

    private SecretKey clientKeyExchange(DHClientKeyExchange mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        return dh.getAgreedSecret(mesg.getClientPublicKey(), false);
    }

    private SecretKey clientKeyExchange(ECDHClientKeyExchange mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        return ecdh.getAgreedSecret(mesg.getEncodedPoint());
    }

    private void clientCertificateVerify(CertificateVerify mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        if (protocolVersion.v >= ProtocolVersion.TLS12.v) {
            SignatureAndHashAlgorithm signAlg = mesg.getPreferableSignatureAlgorithm();
            if (signAlg == null) {
                throw new SSLHandshakeException("Illegal CertificateVerify message");
            }
            String hashAlg = SignatureAndHashAlgorithm.getHashAlgorithmName(signAlg);
            if (hashAlg == null || hashAlg.length() == 0) {
                throw new SSLHandshakeException("No supported hash algorithm");
            }
        }
        try {
            PublicKey publicKey = session.getPeerCertificates()[0].getPublicKey();
            boolean valid = mesg.verify(protocolVersion, handshakeHash, publicKey, session.getMasterSecret());
            if (valid == false) {
                fatalSE(Alerts.alert_bad_certificate, "certificate verify message signature error");
            }
        } catch (GeneralSecurityException e) {
            fatalSE(Alerts.alert_bad_certificate, "certificate verify format error", e);
        }
        needClientVerify = false;
    }

    private void clientFinished(Finished mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        if (doClientAuth == SSLEngineImpl.clauth_required) {
            session.getPeerPrincipal();
        }
        if (needClientVerify) {
            fatalSE(Alerts.alert_handshake_failure, "client did not send certificate verify message");
        }
        boolean verified = mesg.verify(handshakeHash, Finished.CLIENT, session.getMasterSecret());
        if (!verified) {
            fatalSE(Alerts.alert_handshake_failure, "client 'finished' message doesn't verify");
        }
        if (secureRenegotiation) {
            clientVerifyData = mesg.getVerifyData();
        }
        if (!resumingSession) {
            input.digestNow();
            sendChangeCipherAndFinish(true);
        }
        session.setLastAccessedTime(System.currentTimeMillis());
        if (!resumingSession && session.isRejoinable()) {
            ((SSLSessionContextImpl) sslContext.engineGetServerSessionContext()).put(session);
            if (debug != null && Debug.isOn("session")) {
                System.out.println("%% Cached server session: " + session);
            }
        } else if (!resumingSession && debug != null && Debug.isOn("session")) {
            System.out.println("%% Didn't cache non-resumable server session: " + session);
        }
    }

    private void sendChangeCipherAndFinish(boolean finishedTag) throws IOException {
        output.flush();
        Finished mesg = new Finished(protocolVersion, handshakeHash, Finished.SERVER, session.getMasterSecret(), cipherSuite);
        sendChangeCipherSpec(mesg, finishedTag);
        if (secureRenegotiation) {
            serverVerifyData = mesg.getVerifyData();
        }
        if (finishedTag) {
            state = HandshakeMessage.ht_finished;
        }
    }

    @Override
    HandshakeMessage getKickstartMessage() {
        return new HelloRequest();
    }

    @Override
    void handshakeAlert(byte description) throws SSLProtocolException {
        String message = Alerts.alertDescription(description);
        if (debug != null && Debug.isOn("handshake")) {
            System.out.println("SSL -- handshake alert:  " + message);
        }
        if ((description == Alerts.alert_no_certificate) && (doClientAuth == SSLEngineImpl.clauth_requested)) {
            return;
        }
        throw new SSLProtocolException("handshake alert: " + message);
    }

    private SecretKey clientKeyExchange(RSAClientKeyExchange mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        return mesg.preMaster;
    }

    private void clientCertificate(CertificateMsg mesg) throws IOException {
        if (debug != null && Debug.isOn("handshake")) {
            mesg.print(System.out);
        }
        X509Certificate[] peerCerts = mesg.getCertificateChain();
        if (peerCerts.length == 0) {
            if (doClientAuth == SSLEngineImpl.clauth_requested) {
                return;
            } else {
                fatalSE(Alerts.alert_bad_certificate, "null cert chain");
            }
        }
        X509TrustManager tm = sslContext.getX509TrustManager();
        try {
            PublicKey key = peerCerts[0].getPublicKey();
            String keyAlgorithm = key.getAlgorithm();
            String authType;
            if (keyAlgorithm.equals("RSA")) {
                authType = "RSA";
            } else if (keyAlgorithm.equals("DSA")) {
                authType = "DSA";
            } else if (keyAlgorithm.equals("EC")) {
                authType = "EC";
            } else {
                authType = "UNKNOWN";
            }
            if (tm instanceof X509ExtendedTrustManager) {
                if (conn != null) {
                    ((X509ExtendedTrustManager) tm).checkClientTrusted(peerCerts.clone(), authType, conn);
                } else {
                    ((X509ExtendedTrustManager) tm).checkClientTrusted(peerCerts.clone(), authType, engine);
                }
            } else {
                throw new CertificateException("Improper X509TrustManager implementation");
            }
        } catch (CertificateException e) {
            fatalSE(Alerts.alert_certificate_unknown, e);
        }
        needClientVerify = true;
        session.setPeerCertificates(peerCerts);
    }
}
