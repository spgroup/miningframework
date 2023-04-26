package sun.security.pkcs;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.util.Locale;
import java.util.Date;
import java.util.Hashtable;
import sun.security.x509.CertificateExtensions;
import sun.security.util.Debug;
import sun.security.util.DerEncoder;
import sun.security.util.DerValue;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.ObjectIdentifier;
import sun.misc.HexDumpEncoder;

public class PKCS9Attribute implements DerEncoder {

    private static final Debug debug = Debug.getInstance("jar");

    static final ObjectIdentifier[] PKCS9_OIDS = new ObjectIdentifier[18];

    private final static Class<?> BYTE_ARRAY_CLASS;

    static {
        for (int i = 1; i < PKCS9_OIDS.length - 2; i++) {
            PKCS9_OIDS[i] = ObjectIdentifier.newInternal(new int[] { 1, 2, 840, 113549, 1, 9, i });
        }
        PKCS9_OIDS[PKCS9_OIDS.length - 2] = ObjectIdentifier.newInternal(new int[] { 1, 2, 840, 113549, 1, 9, 16, 2, 12 });
        PKCS9_OIDS[PKCS9_OIDS.length - 1] = ObjectIdentifier.newInternal(new int[] { 1, 2, 840, 113549, 1, 9, 16, 2, 14 });
        try {
            BYTE_ARRAY_CLASS = Class.forName("[B");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e.toString());
        }
    }

    public static final ObjectIdentifier EMAIL_ADDRESS_OID = PKCS9_OIDS[1];

    public static final ObjectIdentifier UNSTRUCTURED_NAME_OID = PKCS9_OIDS[2];

    public static final ObjectIdentifier CONTENT_TYPE_OID = PKCS9_OIDS[3];

    public static final ObjectIdentifier MESSAGE_DIGEST_OID = PKCS9_OIDS[4];

    public static final ObjectIdentifier SIGNING_TIME_OID = PKCS9_OIDS[5];

    public static final ObjectIdentifier COUNTERSIGNATURE_OID = PKCS9_OIDS[6];

    public static final ObjectIdentifier CHALLENGE_PASSWORD_OID = PKCS9_OIDS[7];

    public static final ObjectIdentifier UNSTRUCTURED_ADDRESS_OID = PKCS9_OIDS[8];

    public static final ObjectIdentifier EXTENDED_CERTIFICATE_ATTRIBUTES_OID = PKCS9_OIDS[9];

    public static final ObjectIdentifier ISSUER_SERIALNUMBER_OID = PKCS9_OIDS[10];

    public static final ObjectIdentifier EXTENSION_REQUEST_OID = PKCS9_OIDS[14];

    public static final ObjectIdentifier SMIME_CAPABILITY_OID = PKCS9_OIDS[15];

    public static final ObjectIdentifier SIGNING_CERTIFICATE_OID = PKCS9_OIDS[16];

    public static final ObjectIdentifier SIGNATURE_TIMESTAMP_TOKEN_OID = PKCS9_OIDS[17];

    public static final String EMAIL_ADDRESS_STR = "EmailAddress";

    public static final String UNSTRUCTURED_NAME_STR = "UnstructuredName";

    public static final String CONTENT_TYPE_STR = "ContentType";

    public static final String MESSAGE_DIGEST_STR = "MessageDigest";

    public static final String SIGNING_TIME_STR = "SigningTime";

    public static final String COUNTERSIGNATURE_STR = "Countersignature";

    public static final String CHALLENGE_PASSWORD_STR = "ChallengePassword";

    public static final String UNSTRUCTURED_ADDRESS_STR = "UnstructuredAddress";

    public static final String EXTENDED_CERTIFICATE_ATTRIBUTES_STR = "ExtendedCertificateAttributes";

    public static final String ISSUER_SERIALNUMBER_STR = "IssuerAndSerialNumber";

    private static final String RSA_PROPRIETARY_STR = "RSAProprietary";

    private static final String SMIME_SIGNING_DESC_STR = "SMIMESigningDesc";

    public static final String EXTENSION_REQUEST_STR = "ExtensionRequest";

    public static final String SMIME_CAPABILITY_STR = "SMIMECapability";

    public static final String SIGNING_CERTIFICATE_STR = "SigningCertificate";

    public static final String SIGNATURE_TIMESTAMP_TOKEN_STR = "SignatureTimestampToken";

    private static final Hashtable<String, ObjectIdentifier> NAME_OID_TABLE = new Hashtable<String, ObjectIdentifier>(18);

    static {
        NAME_OID_TABLE.put("emailaddress", PKCS9_OIDS[1]);
        NAME_OID_TABLE.put("unstructuredname", PKCS9_OIDS[2]);
        NAME_OID_TABLE.put("contenttype", PKCS9_OIDS[3]);
        NAME_OID_TABLE.put("messagedigest", PKCS9_OIDS[4]);
        NAME_OID_TABLE.put("signingtime", PKCS9_OIDS[5]);
        NAME_OID_TABLE.put("countersignature", PKCS9_OIDS[6]);
        NAME_OID_TABLE.put("challengepassword", PKCS9_OIDS[7]);
        NAME_OID_TABLE.put("unstructuredaddress", PKCS9_OIDS[8]);
        NAME_OID_TABLE.put("extendedcertificateattributes", PKCS9_OIDS[9]);
        NAME_OID_TABLE.put("issuerandserialnumber", PKCS9_OIDS[10]);
        NAME_OID_TABLE.put("rsaproprietary", PKCS9_OIDS[11]);
        NAME_OID_TABLE.put("rsaproprietary", PKCS9_OIDS[12]);
        NAME_OID_TABLE.put("signingdescription", PKCS9_OIDS[13]);
        NAME_OID_TABLE.put("extensionrequest", PKCS9_OIDS[14]);
        NAME_OID_TABLE.put("smimecapability", PKCS9_OIDS[15]);
        NAME_OID_TABLE.put("signingcertificate", PKCS9_OIDS[16]);
        NAME_OID_TABLE.put("signaturetimestamptoken", PKCS9_OIDS[17]);
    }

    private static final Hashtable<ObjectIdentifier, String> OID_NAME_TABLE = new Hashtable<ObjectIdentifier, String>(16);

    static {
        OID_NAME_TABLE.put(PKCS9_OIDS[1], EMAIL_ADDRESS_STR);
        OID_NAME_TABLE.put(PKCS9_OIDS[2], UNSTRUCTURED_NAME_STR);
        OID_NAME_TABLE.put(PKCS9_OIDS[3], CONTENT_TYPE_STR);
        OID_NAME_TABLE.put(PKCS9_OIDS[4], MESSAGE_DIGEST_STR);
        OID_NAME_TABLE.put(PKCS9_OIDS[5], SIGNING_TIME_STR);
        OID_NAME_TABLE.put(PKCS9_OIDS[6], COUNTERSIGNATURE_STR);
        OID_NAME_TABLE.put(PKCS9_OIDS[7], CHALLENGE_PASSWORD_STR);
        OID_NAME_TABLE.put(PKCS9_OIDS[8], UNSTRUCTURED_ADDRESS_STR);
        OID_NAME_TABLE.put(PKCS9_OIDS[9], EXTENDED_CERTIFICATE_ATTRIBUTES_STR);
        OID_NAME_TABLE.put(PKCS9_OIDS[10], ISSUER_SERIALNUMBER_STR);
        OID_NAME_TABLE.put(PKCS9_OIDS[11], RSA_PROPRIETARY_STR);
        OID_NAME_TABLE.put(PKCS9_OIDS[12], RSA_PROPRIETARY_STR);
        OID_NAME_TABLE.put(PKCS9_OIDS[13], SMIME_SIGNING_DESC_STR);
        OID_NAME_TABLE.put(PKCS9_OIDS[14], EXTENSION_REQUEST_STR);
        OID_NAME_TABLE.put(PKCS9_OIDS[15], SMIME_CAPABILITY_STR);
        OID_NAME_TABLE.put(PKCS9_OIDS[16], SIGNING_CERTIFICATE_STR);
        OID_NAME_TABLE.put(PKCS9_OIDS[17], SIGNATURE_TIMESTAMP_TOKEN_STR);
    }

    private static final Byte[][] PKCS9_VALUE_TAGS = { null, { new Byte(DerValue.tag_IA5String) }, { new Byte(DerValue.tag_IA5String), new Byte(DerValue.tag_PrintableString) }, { new Byte(DerValue.tag_ObjectId) }, { new Byte(DerValue.tag_OctetString) }, { new Byte(DerValue.tag_UtcTime) }, { new Byte(DerValue.tag_Sequence) }, { new Byte(DerValue.tag_PrintableString), new Byte(DerValue.tag_T61String) }, { new Byte(DerValue.tag_PrintableString), new Byte(DerValue.tag_T61String) }, { new Byte(DerValue.tag_SetOf) }, { new Byte(DerValue.tag_Sequence) }, null, null, null, { new Byte(DerValue.tag_Sequence) }, { new Byte(DerValue.tag_Sequence) }, { new Byte(DerValue.tag_Sequence) }, { new Byte(DerValue.tag_Sequence) } };

    private static final Class<?>[] VALUE_CLASSES = new Class<?>[18];

    static {
        try {
            Class<?> str = Class.forName("[Ljava.lang.String;");
            VALUE_CLASSES[0] = null;
            VALUE_CLASSES[1] = str;
            VALUE_CLASSES[2] = str;
            VALUE_CLASSES[3] = Class.forName("sun.security.util.ObjectIdentifier");
            VALUE_CLASSES[4] = BYTE_ARRAY_CLASS;
            VALUE_CLASSES[5] = Class.forName("java.util.Date");
            VALUE_CLASSES[6] = Class.forName("[Lsun.security.pkcs.SignerInfo;");
            VALUE_CLASSES[7] = Class.forName("java.lang.String");
            VALUE_CLASSES[8] = str;
            VALUE_CLASSES[9] = null;
            VALUE_CLASSES[10] = null;
            VALUE_CLASSES[11] = null;
            VALUE_CLASSES[12] = null;
            VALUE_CLASSES[13] = null;
            VALUE_CLASSES[14] = Class.forName("sun.security.x509.CertificateExtensions");
            VALUE_CLASSES[15] = null;
            VALUE_CLASSES[16] = null;
            VALUE_CLASSES[17] = BYTE_ARRAY_CLASS;
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e.toString());
        }
    }

    private static final boolean[] SINGLE_VALUED = { false, false, false, true, true, true, false, true, false, false, true, false, false, false, true, true, true, true };

    private ObjectIdentifier oid;

    private int index;

    private Object value;

    public PKCS9Attribute(ObjectIdentifier oid, Object value) throws IllegalArgumentException {
        init(oid, value);
    }

    public PKCS9Attribute(String name, Object value) throws IllegalArgumentException {
        ObjectIdentifier oid = getOID(name);
        if (oid == null)
            throw new IllegalArgumentException("Unrecognized attribute name " + name + " constructing PKCS9Attribute.");
        init(oid, value);
    }

    private void init(ObjectIdentifier oid, Object value) throws IllegalArgumentException {
        this.oid = oid;
        index = indexOf(oid, PKCS9_OIDS, 1);
        Class<?> clazz = index == -1 ? BYTE_ARRAY_CLASS : VALUE_CLASSES[index];
        if (!clazz.isInstance(value)) {
            throw new IllegalArgumentException("Wrong value class " + " for attribute " + oid + " constructing PKCS9Attribute; was " + value.getClass().toString() + ", should be " + clazz.toString());
        }
        this.value = value;
    }

    public PKCS9Attribute(DerValue derVal) throws IOException {
        DerInputStream derIn = new DerInputStream(derVal.toByteArray());
        DerValue[] val = derIn.getSequence(2);
        if (derIn.available() != 0)
            throw new IOException("Excess data parsing PKCS9Attribute");
        if (val.length != 2)
            throw new IOException("PKCS9Attribute doesn't have two components");
        oid = val[0].getOID();
        byte[] content = val[1].toByteArray();
        DerValue[] elems = new DerInputStream(content).getSet(1);
        index = indexOf(oid, PKCS9_OIDS, 1);
        if (index == -1) {
            if (debug != null) {
                debug.println("Unsupported signer attribute: " + oid);
            }
            value = content;
            return;
        }
        if (SINGLE_VALUED[index] && elems.length > 1)
            throwSingleValuedException();
        Byte tag;
        for (int i = 0; i < elems.length; i++) {
            tag = new Byte(elems[i].tag);
            if (indexOf(tag, PKCS9_VALUE_TAGS[index], 0) == -1)
                throwTagException(tag);
        }
        switch(index) {
            case 1:
            case 2:
            case 8:
                {
                    String[] values = new String[elems.length];
                    for (int i = 0; i < elems.length; i++) values[i] = elems[i].getAsString();
                    value = values;
                }
                break;
            case 3:
                value = elems[0].getOID();
                break;
            case 4:
                value = elems[0].getOctetString();
                break;
            case 5:
                value = (new DerInputStream(elems[0].toByteArray())).getUTCTime();
                break;
            case 6:
                {
                    SignerInfo[] values = new SignerInfo[elems.length];
                    for (int i = 0; i < elems.length; i++) values[i] = new SignerInfo(elems[i].toDerInputStream());
                    value = values;
                }
                break;
            case 7:
                value = elems[0].getAsString();
                break;
            case 9:
                throw new IOException("PKCS9 extended-certificate " + "attribute not supported.");
            case 10:
                throw new IOException("PKCS9 IssuerAndSerialNumber" + "attribute not supported.");
            case 11:
            case 12:
                throw new IOException("PKCS9 RSA DSI attributes" + "11 and 12, not supported.");
            case 13:
                throw new IOException("PKCS9 attribute #13 not supported.");
            case 14:
                value = new CertificateExtensions(new DerInputStream(elems[0].toByteArray()));
                break;
            case 15:
                throw new IOException("PKCS9 SMIMECapability " + "attribute not supported.");
            case 16:
                value = new SigningCertificateInfo(elems[0].toByteArray());
                break;
            case 17:
                value = elems[0].toByteArray();
                break;
            default:
        }
    }

    public void derEncode(OutputStream out) throws IOException {
        DerOutputStream temp = new DerOutputStream();
        temp.putOID(oid);
        switch(index) {
            case -1:
                temp.write((byte[]) value);
                break;
            case 1:
            case 2:
                {
                    String[] values = (String[]) value;
                    DerOutputStream[] temps = new DerOutputStream[values.length];
                    for (int i = 0; i < values.length; i++) {
                        temps[i] = new DerOutputStream();
                        temps[i].putIA5String(values[i]);
                    }
                    temp.putOrderedSetOf(DerValue.tag_Set, temps);
                }
                break;
            case 3:
                {
                    DerOutputStream temp2 = new DerOutputStream();
                    temp2.putOID((ObjectIdentifier) value);
                    temp.write(DerValue.tag_Set, temp2.toByteArray());
                }
                break;
            case 4:
                {
                    DerOutputStream temp2 = new DerOutputStream();
                    temp2.putOctetString((byte[]) value);
                    temp.write(DerValue.tag_Set, temp2.toByteArray());
                }
                break;
            case 5:
                {
                    DerOutputStream temp2 = new DerOutputStream();
                    temp2.putUTCTime((Date) value);
                    temp.write(DerValue.tag_Set, temp2.toByteArray());
                }
                break;
            case 6:
                temp.putOrderedSetOf(DerValue.tag_Set, (DerEncoder[]) value);
                break;
            case 7:
                {
                    DerOutputStream temp2 = new DerOutputStream();
                    temp2.putPrintableString((String) value);
                    temp.write(DerValue.tag_Set, temp2.toByteArray());
                }
                break;
            case 8:
                {
                    String[] values = (String[]) value;
                    DerOutputStream[] temps = new DerOutputStream[values.length];
                    for (int i = 0; i < values.length; i++) {
                        temps[i] = new DerOutputStream();
                        temps[i].putPrintableString(values[i]);
                    }
                    temp.putOrderedSetOf(DerValue.tag_Set, temps);
                }
                break;
            case 9:
                throw new IOException("PKCS9 extended-certificate " + "attribute not supported.");
            case 10:
                throw new IOException("PKCS9 IssuerAndSerialNumber" + "attribute not supported.");
            case 11:
            case 12:
                throw new IOException("PKCS9 RSA DSI attributes" + "11 and 12, not supported.");
            case 13:
                throw new IOException("PKCS9 attribute #13 not supported.");
            case 14:
                {
                    DerOutputStream temp2 = new DerOutputStream();
                    CertificateExtensions exts = (CertificateExtensions) value;
                    try {
                        exts.encode(temp2, true);
                    } catch (CertificateException ex) {
                        throw new IOException(ex.toString());
                    }
                    temp.write(DerValue.tag_Set, temp2.toByteArray());
                }
                break;
            case 15:
                throw new IOException("PKCS9 attribute #15 not supported.");
            case 16:
                throw new IOException("PKCS9 SigningCertificate attribute not supported.");
            case 17:
                temp.write(DerValue.tag_Set, (byte[]) value);
                break;
            default:
        }
        DerOutputStream derOut = new DerOutputStream();
        derOut.write(DerValue.tag_Sequence, temp.toByteArray());
        out.write(derOut.toByteArray());
    }

    public boolean isKnown() {
        return index != -1;
    }

    public Object getValue() {
        return value;
    }

    public boolean isSingleValued() {
        return index == -1 || SINGLE_VALUED[index];
    }

    public ObjectIdentifier getOID() {
        return oid;
    }

    public String getName() {
        return index == -1 ? oid.toString() : OID_NAME_TABLE.get(PKCS9_OIDS[index]);
    }

    public static ObjectIdentifier getOID(String name) {
        return NAME_OID_TABLE.get(name.toLowerCase(Locale.ENGLISH));
    }

    public static String getName(ObjectIdentifier oid) {
        return OID_NAME_TABLE.get(oid);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(100);
        buf.append("[");
        if (index == -1) {
            buf.append(oid.toString());
        } else {
            buf.append(OID_NAME_TABLE.get(PKCS9_OIDS[index]));
        }
        buf.append(": ");
        if (index == -1 || SINGLE_VALUED[index]) {
            if (value instanceof byte[]) {
                HexDumpEncoder hexDump = new HexDumpEncoder();
                buf.append(hexDump.encodeBuffer((byte[]) value));
            } else {
                buf.append(value.toString());
            }
            buf.append("]");
            return buf.toString();
        } else {
            boolean first = true;
            Object[] values = (Object[]) value;
            for (int j = 0; j < values.length; j++) {
                if (first)
                    first = false;
                else
                    buf.append(", ");
                buf.append(values[j].toString());
            }
            return buf.toString();
        }
    }

    static int indexOf(Object obj, Object[] a, int start) {
        for (int i = start; i < a.length; i++) {
            if (obj.equals(a[i]))
                return i;
        }
        return -1;
    }

    private void throwSingleValuedException() throws IOException {
        throw new IOException("Single-value attribute " + oid + " (" + getName() + ")" + " has multiple values.");
    }

    private void throwTagException(Byte tag) throws IOException {
        Byte[] expectedTags = PKCS9_VALUE_TAGS[index];
        StringBuffer msg = new StringBuffer(100);
        msg.append("Value of attribute ");
        msg.append(oid.toString());
        msg.append(" (");
        msg.append(getName());
        msg.append(") has wrong tag: ");
        msg.append(tag.toString());
        msg.append(".  Expected tags: ");
        msg.append(expectedTags[0].toString());
        for (int i = 1; i < expectedTags.length; i++) {
            msg.append(", ");
            msg.append(expectedTags[i].toString());
        }
        msg.append(".");
        throw new IOException(msg.toString());
    }
}