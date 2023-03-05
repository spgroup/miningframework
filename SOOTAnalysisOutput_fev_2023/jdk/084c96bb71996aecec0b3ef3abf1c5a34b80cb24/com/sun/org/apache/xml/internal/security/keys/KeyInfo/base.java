package com.sun.org.apache.xml.internal.security.keys;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.crypto.SecretKey;
import com.sun.org.apache.xml.internal.security.encryption.EncryptedKey;
import com.sun.org.apache.xml.internal.security.encryption.XMLCipher;
import com.sun.org.apache.xml.internal.security.encryption.XMLEncryptionException;
import com.sun.org.apache.xml.internal.security.exceptions.XMLSecurityException;
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
import com.sun.org.apache.xml.internal.security.utils.EncryptionConstants;
import com.sun.org.apache.xml.internal.security.utils.Constants;
import com.sun.org.apache.xml.internal.security.utils.IdResolver;
import com.sun.org.apache.xml.internal.security.utils.SignatureElementProxy;
import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KeyInfo extends SignatureElementProxy {

    static java.util.logging.Logger log = java.util.logging.Logger.getLogger(KeyInfo.class.getName());

    List<X509Data> x509Datas = null;

    List<EncryptedKey> encryptedKeys = null;

    static final List<StorageResolver> nullList;

    static {
        List<StorageResolver> list = new ArrayList<StorageResolver>(1);
        list.add(null);
        nullList = Collections.unmodifiableList(list);
    }

    public KeyInfo(Document doc) {
        super(doc);
        XMLUtils.addReturnToElement(this._constructionElement);
    }

    public KeyInfo(Element element, String BaseURI) throws XMLSecurityException {
        super(element, BaseURI);
        Attr attr = element.getAttributeNodeNS(null, "Id");
        if (attr != null) {
            element.setIdAttributeNode(attr, true);
        }
    }

    public void setId(String Id) {
        if (Id != null) {
            setLocalIdAttribute(Constants._ATT_ID, Id);
        }
    }

    public String getId() {
        return this._constructionElement.getAttributeNS(null, Constants._ATT_ID);
    }

    public void addKeyName(String keynameString) {
        this.add(new KeyName(this._doc, keynameString));
    }

    public void add(KeyName keyname) {
        this._constructionElement.appendChild(keyname.getElement());
        XMLUtils.addReturnToElement(this._constructionElement);
    }

    public void addKeyValue(PublicKey pk) {
        this.add(new KeyValue(this._doc, pk));
    }

    public void addKeyValue(Element unknownKeyValueElement) {
        this.add(new KeyValue(this._doc, unknownKeyValueElement));
    }

    public void add(DSAKeyValue dsakeyvalue) {
        this.add(new KeyValue(this._doc, dsakeyvalue));
    }

    public void add(RSAKeyValue rsakeyvalue) {
        this.add(new KeyValue(this._doc, rsakeyvalue));
    }

    public void add(PublicKey pk) {
        this.add(new KeyValue(this._doc, pk));
    }

    public void add(KeyValue keyvalue) {
        this._constructionElement.appendChild(keyvalue.getElement());
        XMLUtils.addReturnToElement(this._constructionElement);
    }

    public void addMgmtData(String mgmtdata) {
        this.add(new MgmtData(this._doc, mgmtdata));
    }

    public void add(MgmtData mgmtdata) {
        this._constructionElement.appendChild(mgmtdata.getElement());
        XMLUtils.addReturnToElement(this._constructionElement);
    }

    public void add(PGPData pgpdata) {
        this._constructionElement.appendChild(pgpdata.getElement());
        XMLUtils.addReturnToElement(this._constructionElement);
    }

    public void addRetrievalMethod(String URI, Transforms transforms, String Type) {
        this.add(new RetrievalMethod(this._doc, URI, transforms, Type));
    }

    public void add(RetrievalMethod retrievalmethod) {
        this._constructionElement.appendChild(retrievalmethod.getElement());
        XMLUtils.addReturnToElement(this._constructionElement);
    }

    public void add(SPKIData spkidata) {
        this._constructionElement.appendChild(spkidata.getElement());
        XMLUtils.addReturnToElement(this._constructionElement);
    }

    public void add(X509Data x509data) {
        if (x509Datas == null)
            x509Datas = new ArrayList<X509Data>();
        x509Datas.add(x509data);
        this._constructionElement.appendChild(x509data.getElement());
        XMLUtils.addReturnToElement(this._constructionElement);
    }

    public void add(EncryptedKey encryptedKey) throws XMLEncryptionException {
        if (encryptedKeys == null)
            encryptedKeys = new ArrayList<EncryptedKey>();
        encryptedKeys.add(encryptedKey);
        XMLCipher cipher = XMLCipher.getInstance();
        this._constructionElement.appendChild(cipher.martial(encryptedKey));
    }

    public void addUnknownElement(Element element) {
        this._constructionElement.appendChild(element);
        XMLUtils.addReturnToElement(this._constructionElement);
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

    public int lengthUnknownElement() {
        int res = 0;
        NodeList nl = this._constructionElement.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node current = nl.item(i);
            if ((current.getNodeType() == Node.ELEMENT_NODE) && current.getNamespaceURI().equals(Constants.SignatureSpecNS)) {
                res++;
            }
        }
        return res;
    }

    public KeyName itemKeyName(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this._constructionElement.getFirstChild(), Constants._TAG_KEYNAME, i);
        if (e != null) {
            return new KeyName(e, this._baseURI);
        }
        return null;
    }

    public KeyValue itemKeyValue(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this._constructionElement.getFirstChild(), Constants._TAG_KEYVALUE, i);
        if (e != null) {
            return new KeyValue(e, this._baseURI);
        }
        return null;
    }

    public MgmtData itemMgmtData(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this._constructionElement.getFirstChild(), Constants._TAG_MGMTDATA, i);
        if (e != null) {
            return new MgmtData(e, this._baseURI);
        }
        return null;
    }

    public PGPData itemPGPData(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this._constructionElement.getFirstChild(), Constants._TAG_PGPDATA, i);
        if (e != null) {
            return new PGPData(e, this._baseURI);
        }
        return null;
    }

    public RetrievalMethod itemRetrievalMethod(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this._constructionElement.getFirstChild(), Constants._TAG_RETRIEVALMETHOD, i);
        if (e != null) {
            return new RetrievalMethod(e, this._baseURI);
        }
        return null;
    }

    public SPKIData itemSPKIData(int i) throws XMLSecurityException {
        Element e = XMLUtils.selectDsNode(this._constructionElement.getFirstChild(), Constants._TAG_SPKIDATA, i);
        if (e != null) {
            return new SPKIData(e, this._baseURI);
        }
        return null;
    }

    public X509Data itemX509Data(int i) throws XMLSecurityException {
        if (x509Datas != null) {
            return x509Datas.get(i);
        }
        Element e = XMLUtils.selectDsNode(this._constructionElement.getFirstChild(), Constants._TAG_X509DATA, i);
        if (e != null) {
            return new X509Data(e, this._baseURI);
        }
        return null;
    }

    public EncryptedKey itemEncryptedKey(int i) throws XMLSecurityException {
        if (encryptedKeys != null) {
            return encryptedKeys.get(i);
        }
        Element e = XMLUtils.selectXencNode(this._constructionElement.getFirstChild(), EncryptionConstants._TAG_ENCRYPTEDKEY, i);
        if (e != null) {
            XMLCipher cipher = XMLCipher.getInstance();
            cipher.init(XMLCipher.UNWRAP_MODE, null);
            return cipher.loadEncryptedKey(e);
        }
        return null;
    }

    public Element itemUnknownElement(int i) {
        NodeList nl = this._constructionElement.getChildNodes();
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
        return this._constructionElement.getFirstChild() == null;
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

    public PublicKey getPublicKey() throws KeyResolverException {
        PublicKey pk = this.getPublicKeyFromInternalResolvers();
        if (pk != null) {
            log.log(java.util.logging.Level.FINE, "I could find a key using the per-KeyInfo key resolvers");
            return pk;
        }
        log.log(java.util.logging.Level.FINE, "I couldn't find a key using the per-KeyInfo key resolvers");
        pk = this.getPublicKeyFromStaticResolvers();
        if (pk != null) {
            log.log(java.util.logging.Level.FINE, "I could find a key using the system-wide key resolvers");
            return pk;
        }
        log.log(java.util.logging.Level.FINE, "I couldn't find a key using the system-wide key resolvers");
        return null;
    }

    PublicKey getPublicKeyFromStaticResolvers() throws KeyResolverException {
        Iterator<KeyResolverSpi> it = KeyResolver.iterator();
        while (it.hasNext()) {
            KeyResolverSpi keyResolver = it.next();
            Node currentChild = this._constructionElement.getFirstChild();
            String uri = this.getBaseURI();
            while (currentChild != null) {
                if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
                    for (StorageResolver storage : _storageResolvers) {
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
        int length = lengthInternalKeyResolver();
        int storageLength = this._storageResolvers.size();
        for (int i = 0; i < length; i++) {
            KeyResolverSpi keyResolver = this.itemInternalKeyResolver(i);
            if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "Try " + keyResolver.getClass().getName());
            Node currentChild = this._constructionElement.getFirstChild();
            String uri = this.getBaseURI();
            while (currentChild != null) {
                if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
                    for (int k = 0; k < storageLength; k++) {
                        StorageResolver storage = this._storageResolvers.get(k);
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
            log.log(java.util.logging.Level.FINE, "I could find a X509Certificate using the per-KeyInfo key resolvers");
            return cert;
        }
        log.log(java.util.logging.Level.FINE, "I couldn't find a X509Certificate using the per-KeyInfo key resolvers");
        cert = this.getX509CertificateFromStaticResolvers();
        if (cert != null) {
            log.log(java.util.logging.Level.FINE, "I could find a X509Certificate using the system-wide key resolvers");
            return cert;
        }
        log.log(java.util.logging.Level.FINE, "I couldn't find a X509Certificate using the system-wide key resolvers");
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
            X509Certificate cert = applyCurrentResolver(uri, keyResolver);
            if (cert != null) {
                return cert;
            }
        }
        return null;
    }

    private X509Certificate applyCurrentResolver(String uri, KeyResolverSpi keyResolver) throws KeyResolverException {
        Node currentChild = this._constructionElement.getFirstChild();
        while (currentChild != null) {
            if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
                for (StorageResolver storage : _storageResolvers) {
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
        for (KeyResolverSpi keyResolver : _internalKeyResolvers) {
            if (log.isLoggable(java.util.logging.Level.FINE)) {
                log.log(java.util.logging.Level.FINE, "Try " + keyResolver.getClass().getName());
            }
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
            log.log(java.util.logging.Level.FINE, "I could find a secret key using the per-KeyInfo key resolvers");
            return sk;
        }
        log.log(java.util.logging.Level.FINE, "I couldn't find a secret key using the per-KeyInfo key resolvers");
        sk = this.getSecretKeyFromStaticResolvers();
        if (sk != null) {
            log.log(java.util.logging.Level.FINE, "I could find a secret key using the system-wide key resolvers");
            return sk;
        }
        log.log(java.util.logging.Level.FINE, "I couldn't find a secret key using the system-wide key resolvers");
        return null;
    }

    SecretKey getSecretKeyFromStaticResolvers() throws KeyResolverException {
        final int length = KeyResolver.length();
        int storageLength = this._storageResolvers.size();
        Iterator<KeyResolverSpi> it = KeyResolver.iterator();
        for (int i = 0; i < length; i++) {
            KeyResolverSpi keyResolver = it.next();
            Node currentChild = this._constructionElement.getFirstChild();
            String uri = this.getBaseURI();
            while (currentChild != null) {
                if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
                    for (int k = 0; k < storageLength; k++) {
                        StorageResolver storage = this._storageResolvers.get(k);
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
        int storageLength = this._storageResolvers.size();
        for (int i = 0; i < this.lengthInternalKeyResolver(); i++) {
            KeyResolverSpi keyResolver = this.itemInternalKeyResolver(i);
            if (log.isLoggable(java.util.logging.Level.FINE))
                log.log(java.util.logging.Level.FINE, "Try " + keyResolver.getClass().getName());
            Node currentChild = this._constructionElement.getFirstChild();
            String uri = this.getBaseURI();
            while (currentChild != null) {
                if (currentChild.getNodeType() == Node.ELEMENT_NODE) {
                    for (int k = 0; k < storageLength; k++) {
                        StorageResolver storage = this._storageResolvers.get(k);
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

    List<KeyResolverSpi> _internalKeyResolvers = new ArrayList<KeyResolverSpi>();

    public void registerInternalKeyResolver(KeyResolverSpi realKeyResolver) {
        if (_internalKeyResolvers == null) {
            _internalKeyResolvers = new ArrayList<KeyResolverSpi>();
        }
        this._internalKeyResolvers.add(realKeyResolver);
    }

    int lengthInternalKeyResolver() {
        if (_internalKeyResolvers == null)
            return 0;
        return this._internalKeyResolvers.size();
    }

    KeyResolverSpi itemInternalKeyResolver(int i) {
        return this._internalKeyResolvers.get(i);
    }

    private List<StorageResolver> _storageResolvers = nullList;

    public void addStorageResolver(StorageResolver storageResolver) {
        if (_storageResolvers == nullList) {
            _storageResolvers = new ArrayList<StorageResolver>();
        }
        this._storageResolvers.add(storageResolver);
    }

    static boolean _alreadyInitialized = false;

    public static void init() {
        if (!KeyInfo._alreadyInitialized) {
            if (KeyInfo.log == null) {
                KeyInfo.log = java.util.logging.Logger.getLogger(KeyInfo.class.getName());
                log.log(java.util.logging.Level.SEVERE, "Had to assign log in the init() function");
            }
            KeyInfo._alreadyInitialized = true;
        }
    }

    public String getBaseLocalName() {
        return Constants._TAG_KEYINFO;
    }
}
