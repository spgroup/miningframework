package sun.security.krb5;

import sun.security.krb5.internal.*;
import sun.security.util.*;
import java.net.*;
import java.util.Vector;
import java.util.Locale;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import sun.security.krb5.internal.ccache.CCacheOutputStream;
import sun.security.krb5.internal.util.KerberosString;

public class PrincipalName implements Cloneable {

    public static final int KRB_NT_UNKNOWN = 0;

    public static final int KRB_NT_PRINCIPAL = 1;

    public static final int KRB_NT_SRV_INST = 2;

    public static final int KRB_NT_SRV_HST = 3;

    public static final int KRB_NT_SRV_XHST = 4;

    public static final int KRB_NT_UID = 5;

    public static final String TGS_DEFAULT_SRV_NAME = "krbtgt";

    public static final int TGS_DEFAULT_NT = KRB_NT_SRV_INST;

    public static final char NAME_COMPONENT_SEPARATOR = '/';

    public static final char NAME_REALM_SEPARATOR = '@';

    public static final char REALM_COMPONENT_SEPARATOR = '.';

    public static final String NAME_COMPONENT_SEPARATOR_STR = "/";

    public static final String NAME_REALM_SEPARATOR_STR = "@";

    public static final String REALM_COMPONENT_SEPARATOR_STR = ".";

    private final int nameType;

    private final String[] nameStrings;

    private final Realm nameRealm;

    private final boolean realmDeduced;

    private transient String salt = null;

    public PrincipalName(int nameType, String[] nameStrings, Realm nameRealm) {
        if (nameRealm == null) {
            throw new IllegalArgumentException("Null realm not allowed");
        }
        validateNameStrings(nameStrings);
        this.nameType = nameType;
        this.nameStrings = nameStrings.clone();
        this.nameRealm = nameRealm;
        this.realmDeduced = false;
    }

    public PrincipalName(String[] nameParts, String realm) throws RealmException {
        this(KRB_NT_UNKNOWN, nameParts, new Realm(realm));
    }

    private static void validateNameStrings(String[] ns) {
        if (ns == null) {
            throw new IllegalArgumentException("Null nameStrings not allowed");
        }
        if (ns.length == 0) {
            throw new IllegalArgumentException("Empty nameStrings not allowed");
        }
        for (String s : ns) {
            if (s == null) {
                throw new IllegalArgumentException("Null nameString not allowed");
            }
            if (s.isEmpty()) {
                throw new IllegalArgumentException("Empty nameString not allowed");
            }
        }
    }

    public Object clone() {
        try {
            PrincipalName pName = (PrincipalName) super.clone();
            UNSAFE.putObject(this, NAME_STRINGS_OFFSET, nameStrings.clone());
            return pName;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError("Should never happen");
        }
    }

    private static final long NAME_STRINGS_OFFSET;

    private static final jdk.internal.misc.Unsafe UNSAFE;

    static {
        try {
            jdk.internal.misc.Unsafe unsafe = jdk.internal.misc.Unsafe.getUnsafe();
            NAME_STRINGS_OFFSET = unsafe.objectFieldOffset(PrincipalName.class.getDeclaredField("nameStrings"));
            UNSAFE = unsafe;
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof PrincipalName) {
            PrincipalName other = (PrincipalName) o;
            return nameRealm.equals(other.nameRealm) && Arrays.equals(nameStrings, other.nameStrings);
        }
        return false;
    }

    public PrincipalName(DerValue encoding, Realm realm) throws Asn1Exception, IOException {
        if (realm == null) {
            throw new IllegalArgumentException("Null realm not allowed");
        }
        realmDeduced = false;
        nameRealm = realm;
        DerValue der;
        if (encoding == null) {
            throw new IllegalArgumentException("Null encoding not allowed");
        }
        if (encoding.getTag() != DerValue.tag_Sequence) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        der = encoding.getData().getDerValue();
        if ((der.getTag() & 0x1F) == 0x00) {
            BigInteger bint = der.getData().getBigInteger();
            nameType = bint.intValue();
        } else {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
        der = encoding.getData().getDerValue();
        if ((der.getTag() & 0x01F) == 0x01) {
            DerValue subDer = der.getData().getDerValue();
            if (subDer.getTag() != DerValue.tag_SequenceOf) {
                throw new Asn1Exception(Krb5.ASN1_BAD_ID);
            }
            Vector<String> v = new Vector<>();
            DerValue subSubDer;
            while (subDer.getData().available() > 0) {
                subSubDer = subDer.getData().getDerValue();
                String namePart = new KerberosString(subSubDer).toString();
                v.addElement(namePart);
            }
            nameStrings = new String[v.size()];
            v.copyInto(nameStrings);
            validateNameStrings(nameStrings);
        } else {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        }
    }

    public static PrincipalName parse(DerInputStream data, byte explicitTag, boolean optional, Realm realm) throws Asn1Exception, IOException, RealmException {
        if ((optional) && (((byte) data.peekByte() & (byte) 0x1F) != explicitTag))
            return null;
        DerValue der = data.getDerValue();
        if (explicitTag != (der.getTag() & (byte) 0x1F)) {
            throw new Asn1Exception(Krb5.ASN1_BAD_ID);
        } else {
            DerValue subDer = der.getData().getDerValue();
            if (realm == null) {
                realm = Realm.getDefault();
            }
            return new PrincipalName(subDer, realm);
        }
    }

    private static String[] parseName(String name) {
        Vector<String> tempStrings = new Vector<>();
        String temp = name;
        int i = 0;
        int componentStart = 0;
        String component;
        while (i < temp.length()) {
            if (temp.charAt(i) == NAME_COMPONENT_SEPARATOR) {
                if (i > 0 && temp.charAt(i - 1) == '\\') {
                    temp = temp.substring(0, i - 1) + temp.substring(i, temp.length());
                    continue;
                } else {
                    if (componentStart <= i) {
                        component = temp.substring(componentStart, i);
                        tempStrings.addElement(component);
                    }
                    componentStart = i + 1;
                }
            } else {
                if (temp.charAt(i) == NAME_REALM_SEPARATOR) {
                    if (i > 0 && temp.charAt(i - 1) == '\\') {
                        temp = temp.substring(0, i - 1) + temp.substring(i, temp.length());
                        continue;
                    } else {
                        if (componentStart < i) {
                            component = temp.substring(componentStart, i);
                            tempStrings.addElement(component);
                        }
                        componentStart = i + 1;
                        break;
                    }
                }
            }
            i++;
        }
        if (i == temp.length()) {
            component = temp.substring(componentStart, i);
            tempStrings.addElement(component);
        }
        String[] result = new String[tempStrings.size()];
        tempStrings.copyInto(result);
        return result;
    }

    public PrincipalName(String name, int type, String realm) throws RealmException {
        if (name == null) {
            throw new IllegalArgumentException("Null name not allowed");
        }
        String[] nameParts = parseName(name);
        validateNameStrings(nameParts);
        if (realm == null) {
            realm = Realm.parseRealmAtSeparator(name);
        }
        realmDeduced = realm == null;
        switch(type) {
            case KRB_NT_SRV_HST:
                if (nameParts.length >= 2) {
                    String hostName = nameParts[1];
                    try {
                        String canonicalized = (InetAddress.getByName(hostName)).getCanonicalHostName();
                        if (canonicalized.toLowerCase(Locale.ENGLISH).startsWith(hostName.toLowerCase(Locale.ENGLISH) + ".")) {
                            hostName = canonicalized;
                        }
                    } catch (UnknownHostException | SecurityException e) {
                    }
                    nameParts[1] = hostName.toLowerCase(Locale.ENGLISH);
                }
                nameStrings = nameParts;
                nameType = type;
                if (realm != null) {
                    nameRealm = new Realm(realm);
                } else {
                    String mapRealm = mapHostToRealm(nameParts[1]);
                    if (mapRealm != null) {
                        nameRealm = new Realm(mapRealm);
                    } else {
                        nameRealm = Realm.getDefault();
                    }
                }
                break;
            case KRB_NT_UNKNOWN:
            case KRB_NT_PRINCIPAL:
            case KRB_NT_SRV_INST:
            case KRB_NT_SRV_XHST:
            case KRB_NT_UID:
                nameStrings = nameParts;
                nameType = type;
                if (realm != null) {
                    nameRealm = new Realm(realm);
                } else {
                    nameRealm = Realm.getDefault();
                }
                break;
            default:
                throw new IllegalArgumentException("Illegal name type");
        }
    }

    public PrincipalName(String name, int type) throws RealmException {
        this(name, type, (String) null);
    }

    public PrincipalName(String name) throws RealmException {
        this(name, KRB_NT_UNKNOWN);
    }

    public PrincipalName(String name, String realm) throws RealmException {
        this(name, KRB_NT_UNKNOWN, realm);
    }

    public static PrincipalName tgsService(String r1, String r2) throws KrbException {
        return new PrincipalName(PrincipalName.KRB_NT_SRV_INST, new String[] { PrincipalName.TGS_DEFAULT_SRV_NAME, r1 }, new Realm(r2));
    }

    public String getRealmAsString() {
        return getRealmString();
    }

    public String getPrincipalNameAsString() {
        StringBuilder temp = new StringBuilder(nameStrings[0]);
        for (int i = 1; i < nameStrings.length; i++) temp.append(nameStrings[i]);
        return temp.toString();
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public String getName() {
        return toString();
    }

    public int getNameType() {
        return nameType;
    }

    public String[] getNameStrings() {
        return nameStrings.clone();
    }

    public byte[][] toByteArray() {
        byte[][] result = new byte[nameStrings.length][];
        for (int i = 0; i < nameStrings.length; i++) {
            result[i] = new byte[nameStrings[i].length()];
            result[i] = nameStrings[i].getBytes();
        }
        return result;
    }

    public String getRealmString() {
        return nameRealm.toString();
    }

    public Realm getRealm() {
        return nameRealm;
    }

    public String getSalt() {
        if (salt == null) {
            StringBuilder salt = new StringBuilder();
            salt.append(nameRealm.toString());
            for (int i = 0; i < nameStrings.length; i++) {
                salt.append(nameStrings[i]);
            }
            return salt.toString();
        }
        return salt;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < nameStrings.length; i++) {
            if (i > 0)
                str.append("/");
            str.append(nameStrings[i]);
        }
        str.append("@");
        str.append(nameRealm.toString());
        return str.toString();
    }

    public String getNameString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < nameStrings.length; i++) {
            if (i > 0)
                str.append("/");
            str.append(nameStrings[i]);
        }
        return str.toString();
    }

    public byte[] asn1Encode() throws Asn1Exception, IOException {
        DerOutputStream bytes = new DerOutputStream();
        DerOutputStream temp = new DerOutputStream();
        BigInteger bint = BigInteger.valueOf(this.nameType);
        temp.putInteger(bint);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) 0x00), temp);
        temp = new DerOutputStream();
        DerValue[] der = new DerValue[nameStrings.length];
        for (int i = 0; i < nameStrings.length; i++) {
            der[i] = new KerberosString(nameStrings[i]).toDerValue();
        }
        temp.putSequence(der);
        bytes.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) 0x01), temp);
        temp = new DerOutputStream();
        temp.write(DerValue.tag_Sequence, bytes);
        return temp.toByteArray();
    }

    public boolean match(PrincipalName pname) {
        boolean matched = true;
        if ((this.nameRealm != null) && (pname.nameRealm != null)) {
            if (!(this.nameRealm.toString().equalsIgnoreCase(pname.nameRealm.toString()))) {
                matched = false;
            }
        }
        if (this.nameStrings.length != pname.nameStrings.length) {
            matched = false;
        } else {
            for (int i = 0; i < this.nameStrings.length; i++) {
                if (!(this.nameStrings[i].equalsIgnoreCase(pname.nameStrings[i]))) {
                    matched = false;
                }
            }
        }
        return matched;
    }

    public void writePrincipal(CCacheOutputStream cos) throws IOException {
        cos.write32(nameType);
        cos.write32(nameStrings.length);
        byte[] realmBytes = null;
        realmBytes = nameRealm.toString().getBytes();
        cos.write32(realmBytes.length);
        cos.write(realmBytes, 0, realmBytes.length);
        byte[] bytes = null;
        for (int i = 0; i < nameStrings.length; i++) {
            bytes = nameStrings[i].getBytes();
            cos.write32(bytes.length);
            cos.write(bytes, 0, bytes.length);
        }
    }

    public String getInstanceComponent() {
        if (nameStrings != null && nameStrings.length >= 2) {
            return new String(nameStrings[1]);
        }
        return null;
    }

    static String mapHostToRealm(String name) {
        String result = null;
        try {
            String subname = null;
            Config c = Config.getInstance();
            if ((result = c.get("domain_realm", name)) != null)
                return result;
            else {
                for (int i = 1; i < name.length(); i++) {
                    if ((name.charAt(i) == '.') && (i != name.length() - 1)) {
                        subname = name.substring(i);
                        result = c.get("domain_realm", subname);
                        if (result != null) {
                            break;
                        } else {
                            subname = name.substring(i + 1);
                            result = c.get("domain_realm", subname);
                            if (result != null) {
                                break;
                            }
                        }
                    }
                }
            }
        } catch (KrbException e) {
        }
        return result;
    }

    public boolean isRealmDeduced() {
        return realmDeduced;
    }
}
