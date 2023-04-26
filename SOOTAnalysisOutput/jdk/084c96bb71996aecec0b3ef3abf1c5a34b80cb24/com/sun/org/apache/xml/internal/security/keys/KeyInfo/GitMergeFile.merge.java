package com.sun.org.apache.xml.internal.security.keys;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.crypto.SecretKey;
import com.sun.org.apache.xml.internal.security.encryption.EncryptedKey;
import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import com.sun.org.apache.xml.internal.security.encryption.XMLEncryptionException;
import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
import com.sun.org.apache.xml.internal.security.keys.content.DEREncodedKeyValue;
import com.sun.org.apache.xml.internal.security.keys.content.KeyInfoReference;
import com.sun.org.apache.xml.internal.security.keys.content.KeyName;
import com.sun.org.apache.xml.internal.security.keys.content.KeyValue;
import com.sun.org.apache.xml.internal.security.keys.content.MgmtData;
import com.sun.org.apache.xml.internal.security.keys.content.PGPData;
import com.sun.org.apache.xml.internal.security.keys.content.RetrievalMethod;
import com.sun.org.apache.xml.internal.security.keys.content.SPKIData;
import com.sun.org.apache.xml.internal.security.keys.content.X509Data;
import com.sun.org.apache.xml.internal.security.keys.content.keyvalues.DSAKeyValue;
import com.sun.org.apache.xml.internal.security.keys.content.keyvalues.RSAKeyValue;
import com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolver;
import com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolverException;
import com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolverSpi;
import com.sun.org.apache.xml.internal.security.keys.storage.StorageResolver;
import com.sun.org.apache.xml.internal.security.transforms.Transforms;
import com.sun.org.apache.xml.internal.security.utils.Constants;
import com.sun.org.apache.xml.internal.security.utils.EncryptionConstants;
import com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy;
import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KeyInfo extends SignatureElementProxy {

    private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(KeyInfo.class.getName());

    private List<X509Data> x509Datas = null;

    private List<EncryptedKey> encryptedKeys = null;

    private static final List<StorageResolver> nullList;

    static {
        List<StorageResolver> list = new ArrayList<StorageResolver>(1);
        list.add(null);
        nullList = java.util.Collections.unmodifiableList(list);
    }

    private List<StorageResolver> storageResolvers = nullList;

    private List<KeyResolverSpi> internalKeyResolvers = new ArrayList<KeyResolverSpi>();

    private boolean secureValidation;

    public KeyInfo(Document doc) {
        super(doc);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public KeyInfo(Element element, String baseURI) throws XMLSecurityException {
        super(element, baseURI);
        Attr attr = element.getAttributeNodeNS(null, "Id");
        if (attr != null) {
            element.setIdAttributeNode(attr, true);
        }
    }

    public void setSecureValidation(boolean secureValidation) {
        this.secureValidation = secureValidation;
    }

    public void setId(String id) {
        if (id != null) {
            this.constructionElement.setAttributeNS(null, Constants._ATT_ID, id);
            this.constructionElement.setIdAttributeNS(null, Constants._ATT_ID, true);
        }
    }

    public String getId() {
        return this.constructionElement.getAttributeNS(null, Constants._ATT_ID);
    }

    public void addKeyName(String keynameString) {
        this.add(new KeyName(this.doc, keynameString));
    }

    public void add(KeyName keyname) {
        this.constructionElement.appendChild(keyname.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addKeyValue(PublicKey pk) {
        this.add(new KeyValue(this.doc, pk));
    }

    public void addKeyValue(Element unknownKeyValueElement) {
        this.add(new KeyValue(this.doc, unknownKeyValueElement));
    }

    public void add(DSAKeyValue dsakeyvalue) {
        this.add(new KeyValue(this.doc, dsakeyvalue));
    }

    public void add(RSAKeyValue rsakeyvalue) {
        this.add(new KeyValue(this.doc, rsakeyvalue));
    }

    public void add(PublicKey pk) {
        this.add(new KeyValue(this.doc, pk));
    }

    public void add(KeyValue keyvalue) {
        this.constructionElement.appendChild(keyvalue.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addMgmtData(String mgmtdata) {
        this.add(new MgmtData(this.doc, mgmtdata));
    }

    public void add(MgmtData mgmtdata) {
        this.constructionElement.appendChild(mgmtdata.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void add(PGPData pgpdata) {
        this.constructionElement.appendChild(pgpdata.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addRetrievalMethod(String uri, Transforms transforms, String Type) {
        this.add(new RetrievalMethod(this.doc, uri, transforms, Type));
    }

    public void add(RetrievalMethod retrievalmethod) {
        this.constructionElement.appendChild(retrievalmethod.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void add(SPKIData spkidata) {
        this.constructionElement.appendChild(spkidata.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void add(X509Data x509data) {
        if (x509Datas == null) {
            x509Datas = new ArrayList<X509Data>();
        }
        x509Datas.add(x509data);
        this.constructionElement.appendChild(x509data.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void add(EncryptedKey encryptedKey) throws XMLEncryptionException {
        if (encryptedKeys == null) {
            encryptedKeys = new ArrayList<EncryptedKey>();
        }
        encryptedKeys.add(encryptedKey);
        XMLCipher cipher = XMLCipher.getInstance();
        this.constructionElement.appendChild(cipher.martial(encryptedKey));
    }

    public void addDEREncodedKeyValue(PublicKey pk) throws XMLSecurityException {
        this.add(new DEREncodedKeyValue(this.doc, pk));
    }

    public void add(DEREncodedKeyValue derEncodedKeyValue) {
        this.constructionElement.appendChild(derEncodedKeyValue.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addKeyInfoReference(String URI) throws XMLSecurityException {
        this.add(new KeyInfoReference(this.doc, URI));
    }

    public void add(KeyInfoReference keyInfoReference) {
        this.constructionElement.appendChild(keyInfoReference.getElement());
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public void addUnknownElement(Element element) {
        this.constructionElement.appendChild(element);
        XMLUtils.addReturnToElement(this.constructionElement);
    }

    public int lengthKeyName() {
        return this.length(Constants.SignatureSpecNS, Constants._TAG_KEYNAME);
    }

    public int lengthKeyValue() {
        return this.length(Constants.SignatureSpecNS, Constants._TAG_KEYVALUE);
    }

    public int lengthMgmtData() {
        return this.length(Constants.SignatureSpecNS, Constants._TAG_MGMTDATA);
    }

    public int lengthPGPData() {
        return this.length(Constants.SignatureSpecNS, Constants._TAG_PGPDATA);
    }

    public int lengthRetrievalMethod() {
        return this.length(Constants.SignatureSpecNS, Constants._TAG_RETRIEVALMETHOD);
    }

    public int lengthSPKIData() {
        return this.length(Constants.SignatureSpecNS, Constants._TAG_SPKIDATA);
    }

    public int lengthX509Data() {
        if (x509Datas != null) {
            return x509Datas.size();
        }
        return this.length(Constants.SignatureSpecNS, Constants._TAG_X509DATA);
    }

    public int lengthDEREncodedKeyValue() {
        return this.length(Constants.SignatureSpec11NS, Constants._TAG_DERENCODEDKEYVALUE);
    }

    public int lengthKeyInfoReference() {
        return this.length(Constants.SignatureSpec11NS, Constants._TAG_KEYINFOREFERENCE);
    }

    public int lengthUnknownElement() {
        int res = 0;
        NodeList nl = this.constructionElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node current = nl.item(i);
            if ((current.getNodeType() == Node.ELEMENT_NODE) && current.getNamespaceURI().equals(Constants.SignatureSpecNS)) {
                res++;
            }
        }
        return res;
    }

    public KeyName itemKeyName(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), Constants._TAG_KEYNAME, i);
        if (e != null) {
            return new KeyName(e, this.baseURI);
        }
        return null;
    }

    public KeyValue itemKeyValue(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), Constants._TAG_KEYVALUE, i);
        if (e != null) {
            return new KeyValue(e, this.baseURI);
        }
        return null;
    }

    public MgmtData itemMgmtData(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), Constants._TAG_MGMTDATA, i);
        if (e != null) {
            return new MgmtData(e, this.baseURI);
        }
        return null;
    }

    public PGPData itemPGPData(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), Constants._TAG_PGPDATA, i);
        if (e != null) {
            return new PGPData(e, this.baseURI);
        }
        return null;
    }

    public RetrievalMethod itemRetrievalMethod(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), Constants._TAG_RETRIEVALMETHOD, i);
        if (e != null) {
            return new RetrievalMethod(e, this.baseURI);
        }
        return null;
    }

    public SPKIData itemSPKIData(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), Constants._TAG_SPKIDATA, i);
        if (e != null) {
            return new SPKIData(e, this.baseURI);
        }
        return null;
    }

    public X509Data itemX509Data(int i) throws XMLSecurityException {
        if (x509Datas != null) {
            return x509Datas.get(i);
        }
        Element e = XMLUtils.selectDsNode(this.constructionElement.getFirstChild(), Constants._TAG_X509DATA, i);
        if (e != null) {
            return new X509Data(e, this.baseURI);
        }
        return null;
    }

    public EncryptedKey itemEncryptedKey(int i) throws XMLSecurityException {
        if (encryptedKeys != null) {
            return encryptedKeys.get(i);
        }
        Element e = XMLUtils.selectXencNode(this.constructionElement.getFirstChild(), EncryptionConstants._TAG_ENCRYPTEDKEY, i);
        if (e != null) {
            XMLCipher cipher = XMLCipher.getInstance();
            cipher.init(XMLCipher.UNWRAP_MODE, null);
            return cipher.loadEncryptedKey(e);
        }
        return null;
    }

    public DEREncodedKeyValue itemDEREncodedKeyValue(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDs11Node(this.constructionElement.getFirstChild(), Constants._TAG_DERENCODEDKEYVALUE, i);
        if (e != null) {
            return new DEREncodedKeyValue(e, this.baseURI);
        }
        return null;
    }

    public KeyInfoReference itemKeyInfoReference(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDs11Node(this.constructionElement.getFirstChild(), Constants._TAG_KEYINFOREFERENCE, i);
        if (e != null) {
            return new KeyInfoReference(e, this.baseURI);
        }
        return null;
    }

    public Element itemUnknownElement(int i) {
        NodeList nl = this.constructionElement.getChildNodes();
        int res = 0;
        for (int j = 0; j < nl.getLength(); j++) {
            Node current = nl.item(j);
            if ((current.getNodeType() == Node.ELEMENT_NODE) && current.getNamespaceURI().equals(Constants.SignatureSpecNS)) {
                res++;
                if (res == i) {
                    return (Element) current;
                }
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return this.constructionElement.getFirstChild() == null;
    }

    public boolean containsKeyName() {
        return this.lengthKeyName() > 0;
    }

    public boolean containsKeyValue() {
        return this.lengthKeyValue() > 0;
    }

    public boolean containsMgmtData() {
        return this.lengthMgmtData() > 0;
    }

    public boolean containsPGPData() {
        return this.lengthPGPData() > 0;
    }

    public boolean containsRetrievalMethod() {
        return this.lengthRetrievalMethod() > 0;
    }

    public boolean containsSPKIData() {
        return this.lengthSPKIData() > 0;
    }

    public boolean containsUnknownElement() {
        return this.lengthUnknownElement() > 0;
    }

    public boolean containsX509Data() {
        return this.lengthX509Data() > 0;
    }

    public boolean containsDEREncodedKeyValue() {
        return this.lengthDEREncodedKeyValue() > 0;
    }

    public boolean containsKeyInfoReference() {
        return this.lengthKeyInfoReference() > 0;
    }

    public PublicKey getPublicKey() throws KeyResolverException {
        PublicKey pk = this.getPublicKeyFromInternalResolvers();
        if (pk != null) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "I could find a key using the per-KeyInfo key resolvers");
            }
            return pk;
        }
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, "I couldn't find a key using the per-KeyInfo key resolvers");
        }
        pk = this.getPublicKeyFromStaticResolvers();
        if (pk != null) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "I could find a key using the system-wide key resolvers");
            }
            return pk;
        }
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, "I couldn't find a key using the system-wide key resolvers");
        }
        return null;
    }

    PublicKey getPublicKeyFromStaticResolvers() throws KeyResolverException {
        Iterator<KeyResolverSpi> it = KeyResolver.iterator();
        while (it.hasNext()) {
            KeyResolverSpi keyResolver = it.next();
            keyResolver.setSecureValidation(secureValidation);
            Node currentChild = this.constructionElement.getFirstChild();
            String uri = this.getBaseURI();
            while (currentChild != null) {
                if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
                    for (StorageResolver storage : storageResolvers) {
                        PublicKey pk = keyResolver.engineLookupAndResolvePublicKey((Element) currentChild, uri, storage);
                        if (pk != null) {
                            return pk;
                        }
                    }
                }
                currentChild = currentChild.getNextSibling();
            }
        }
        return null;
    }

    PublicKey getPublicKeyFromInternalResolvers() throws KeyResolverException {
        for (KeyResolverSpi keyResolver : internalKeyResolvers) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "Try " + keyResolver.getClass().getName());
            }
            keyResolver.setSecureValidation(secureValidation);
            Node currentChild = this.constructionElement.getFirstChild();
            String uri = this.getBaseURI();
            while (currentChild != null) {
                if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
                    for (StorageResolver storage : storageResolvers) {
                        PublicKey pk = keyResolver.engineLookupAndResolvePublicKey((Element) currentChild, uri, storage);
                        if (pk != null) {
                            return pk;
                        }
                    }
                }
                currentChild = currentChild.getNextSibling();
            }
        }
        return null;
    }

    public X509Certificate getX509Certificate() throws KeyResolverException {
        X509Certificate cert = this.getX509CertificateFromInternalResolvers();
        if (cert != null) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "I could find a X509Certificate using the per-KeyInfo key resolvers");
            }
            return cert;
        }
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, "I couldn't find a X509Certificate using the per-KeyInfo key resolvers");
        }
        cert = this.getX509CertificateFromStaticResolvers();
        if (cert != null) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "I could find a X509Certificate using the system-wide key resolvers");
            }
            return cert;
        }
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, "I couldn't find a X509Certificate using the system-wide key resolvers");
        }
        return null;
    }

    X509Certificate getX509CertificateFromStaticResolvers() throws KeyResolverException {
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, "Start getX509CertificateFromStaticResolvers() with " + KeyResolver.length() + " resolvers");
        }
        String uri = this.getBaseURI();
        Iterator<KeyResolverSpi> it = KeyResolver.iterator();
        while (it.hasNext()) {
            KeyResolverSpi keyResolver = it.next();
            keyResolver.setSecureValidation(secureValidation);
            X509Certificate cert = applyCurrentResolver(uri, keyResolver);
            if (cert != null) {
                return cert;
            }
        }
        return null;
    }

    private X509Certificate applyCurrentResolver(String uri, KeyResolverSpi keyResolver) throws KeyResolverException {
        Node currentChild = this.constructionElement.getFirstChild();
        while (currentChild != null) {
            if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
                for (StorageResolver storage : storageResolvers) {
                    X509Certificate cert = keyResolver.engineLookupResolveX509Certificate((Element) currentChild, uri, storage);
                    if (cert != null) {
                        return cert;
                    }
                }
            }
            currentChild = currentChild.getNextSibling();
        }
        return null;
    }

    X509Certificate getX509CertificateFromInternalResolvers() throws KeyResolverException {
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, "Start getX509CertificateFromInternalResolvers() with " + this.lengthInternalKeyResolver() + " resolvers");
        }
        String uri = this.getBaseURI();
        for (KeyResolverSpi keyResolver : internalKeyResolvers) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "Try " + keyResolver.getClass().getName());
            }
            keyResolver.setSecureValidation(secureValidation);
            X509Certificate cert = applyCurrentResolver(uri, keyResolver);
            if (cert != null) {
                return cert;
            }
        }
        return null;
    }

    public SecretKey getSecretKey() throws KeyResolverException {
        SecretKey sk = this.getSecretKeyFromInternalResolvers();
        if (sk != null) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "I could find a secret key using the per-KeyInfo key resolvers");
            }
            return sk;
        }
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, "I couldn't find a secret key using the per-KeyInfo key resolvers");
        }
        sk = this.getSecretKeyFromStaticResolvers();
        if (sk != null) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "I could find a secret key using the system-wide key resolvers");
            }
            return sk;
        }
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, "I couldn't find a secret key using the system-wide key resolvers");
        }
        return null;
    }

    SecretKey getSecretKeyFromStaticResolvers() throws KeyResolverException {
        Iterator<KeyResolverSpi> it = KeyResolver.iterator();
        while (it.hasNext()) {
            KeyResolverSpi keyResolver = it.next();
            keyResolver.setSecureValidation(secureValidation);
            Node currentChild = this.constructionElement.getFirstChild();
            String uri = this.getBaseURI();
            while (currentChild != null) {
                if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
                    for (StorageResolver storage : storageResolvers) {
                        SecretKey sk = keyResolver.engineLookupAndResolveSecretKey((Element) currentChild, uri, storage);
                        if (sk != null) {
                            return sk;
                        }
                    }
                }
                currentChild = currentChild.getNextSibling();
            }
        }
        return null;
    }

    SecretKey getSecretKeyFromInternalResolvers() throws KeyResolverException {
        for (KeyResolverSpi keyResolver : internalKeyResolvers) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "Try " + keyResolver.getClass().getName());
            }
            keyResolver.setSecureValidation(secureValidation);
            Node currentChild = this.constructionElement.getFirstChild();
            String uri = this.getBaseURI();
            while (currentChild != null) {
                if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
                    for (StorageResolver storage : storageResolvers) {
                        SecretKey sk = keyResolver.engineLookupAndResolveSecretKey((Element) currentChild, uri, storage);
                        if (sk != null) {
                            return sk;
                        }
                    }
                }
                currentChild = currentChild.getNextSibling();
            }
        }
        return null;
    }

    public PrivateKey getPrivateKey() throws KeyResolverException {
        PrivateKey pk = this.getPrivateKeyFromInternalResolvers();
        if (pk != null) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "I could find a private key using the per-KeyInfo key resolvers");
            }
            return pk;
        }
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, "I couldn't find a secret key using the per-KeyInfo key resolvers");
        }
        pk = this.getPrivateKeyFromStaticResolvers();
        if (pk != null) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "I could find a private key using the system-wide key resolvers");
            }
            return pk;
        }
        if (log.isLoggable(java.util.logging.Level.FINE)) {
            log.log(java.util.logging.Level.FINE, "I couldn't find a private key using the system-wide key resolvers");
        }
        return null;
    }

    PrivateKey getPrivateKeyFromStaticResolvers() throws KeyResolverException {
        Iterator<KeyResolverSpi> it = KeyResolver.iterator();
        while (it.hasNext()) {
            KeyResolverSpi keyResolver = it.next();
            keyResolver.setSecureValidation(secureValidation);
            Node currentChild = this.constructionElement.getFirstChild();
            String uri = this.getBaseURI();
            while (currentChild != null) {
                if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
                    PrivateKey pk = keyResolver.engineLookupAndResolvePrivateKey((Element) currentChild, uri, null);
                    if (pk != null) {
                        return pk;
                    }
                }
                currentChild = currentChild.getNextSibling();
            }
        }
        return null;
    }

    PrivateKey getPrivateKeyFromInternalResolvers() throws KeyResolverException {
        for (KeyResolverSpi keyResolver : internalKeyResolvers) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "Try " + keyResolver.getClass().getName());
            }
            keyResolver.setSecureValidation(secureValidation);
            Node currentChild = this.constructionElement.getFirstChild();
            String uri = this.getBaseURI();
            while (currentChild != null) {
                if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
                    PrivateKey pk = keyResolver.engineLookupAndResolvePrivateKey((Element) currentChild, uri, null);
                    if (pk != null) {
                        return pk;
                    }
                }
                currentChild = currentChild.getNextSibling();
            }
        }
        return null;
    }

    public void registerInternalKeyResolver(KeyResolverSpi realKeyResolver) {
        this.internalKeyResolvers.add(realKeyResolver);
    }

    int lengthInternalKeyResolver() {
        return this.internalKeyResolvers.size();
    }

    KeyResolverSpi itemInternalKeyResolver(int i) {
        return this.internalKeyResolvers.get(i);
    }

    public void addStorageResolver(StorageResolver storageResolver) {
        if (storageResolvers == nullList) {
            storageResolvers = new ArrayList<StorageResolver>();
        }
        this.storageResolvers.add(storageResolver);
    }

    public String getBaseLocalName() {
        return Constants._TAG_KEYINFO;
    }
}
