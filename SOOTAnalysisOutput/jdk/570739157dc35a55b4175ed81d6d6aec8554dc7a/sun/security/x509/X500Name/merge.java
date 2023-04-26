package sun.security.x509;

import java.lang.reflect.*;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.security.AccessController;
import java.security.Principal;
import java.util.*;
import sun.security.util.*;
import javax.security.auth.x500.X500Principal;

public class X500Name implements GeneralNameInterface, Principal {

    private String dn;

    private String rfc1779Dn;

    private String rfc2253Dn;

    private String canonicalDn;

    private RDN[] names;

    private X500Principal x500Principal;

    private byte[] encoded;

    private volatile List<RDN> rdnList;

    private volatile List<AVA> allAvaList;

    public X500Name(String dname) throws IOException {
        this(dname, Collections.<String, String>emptyMap());
    }

    public X500Name(String dname, Map<String, String> keywordMap) throws IOException {
        parseDN(dname, keywordMap);
    }

    public X500Name(String dname, String format) throws IOException {
        if (dname == null) {
            throw new NullPointerException("Name must not be null");
        }
        if (format.equalsIgnoreCase("RFC2253")) {
            parseRFC2253DN(dname);
        } else if (format.equalsIgnoreCase("DEFAULT")) {
            parseDN(dname, Collections.<String, String>emptyMap());
        } else {
            throw new IOException("Unsupported format " + format);
        }
    }

    public X500Name(String commonName, String organizationUnit, String organizationName, String country) throws IOException {
        names = new RDN[4];
        names[3] = new RDN(1);
        names[3].assertion[0] = new AVA(commonName_oid, new DerValue(commonName));
        names[2] = new RDN(1);
        names[2].assertion[0] = new AVA(orgUnitName_oid, new DerValue(organizationUnit));
        names[1] = new RDN(1);
        names[1].assertion[0] = new AVA(orgName_oid, new DerValue(organizationName));
        names[0] = new RDN(1);
        names[0].assertion[0] = new AVA(countryName_oid, new DerValue(country));
    }

    public X500Name(String commonName, String organizationUnit, String organizationName, String localityName, String stateName, String country) throws IOException {
        names = new RDN[6];
        names[5] = new RDN(1);
        names[5].assertion[0] = new AVA(commonName_oid, new DerValue(commonName));
        names[4] = new RDN(1);
        names[4].assertion[0] = new AVA(orgUnitName_oid, new DerValue(organizationUnit));
        names[3] = new RDN(1);
        names[3].assertion[0] = new AVA(orgName_oid, new DerValue(organizationName));
        names[2] = new RDN(1);
        names[2].assertion[0] = new AVA(localityName_oid, new DerValue(localityName));
        names[1] = new RDN(1);
        names[1].assertion[0] = new AVA(stateName_oid, new DerValue(stateName));
        names[0] = new RDN(1);
        names[0].assertion[0] = new AVA(countryName_oid, new DerValue(country));
    }

    public X500Name(RDN[] rdnArray) throws IOException {
        if (rdnArray == null) {
            names = new RDN[0];
        } else {
            names = rdnArray.clone();
            for (int i = 0; i < names.length; i++) {
                if (names[i] == null) {
                    throw new IOException("Cannot create an X500Name");
                }
            }
        }
    }

    public X500Name(DerValue value) throws IOException {
        this(value.toDerInputStream());
    }

    public X500Name(DerInputStream in) throws IOException {
        parseDER(in);
    }

    public X500Name(byte[] name) throws IOException {
        DerInputStream in = new DerInputStream(name);
        parseDER(in);
    }

    public List<RDN> rdns() {
        List<RDN> list = rdnList;
        if (list == null) {
            list = Collections.unmodifiableList(Arrays.asList(names));
            rdnList = list;
        }
        return list;
    }

    public int size() {
        return names.length;
    }

    public List<AVA> allAvas() {
        List<AVA> list = allAvaList;
        if (list == null) {
            list = new ArrayList<AVA>();
            for (int i = 0; i < names.length; i++) {
                list.addAll(names[i].avas());
            }
        }
        return list;
    }

    public int avaSize() {
        return allAvas().size();
    }

    public boolean isEmpty() {
        int n = names.length;
        if (n == 0) {
            return true;
        }
        for (int i = 0; i < n; i++) {
            if (names[i].assertion.length != 0) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return getRFC2253CanonicalName().hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof X500Name == false) {
            return false;
        }
        X500Name other = (X500Name) obj;
        if ((this.canonicalDn != null) && (other.canonicalDn != null)) {
            return this.canonicalDn.equals(other.canonicalDn);
        }
        int n = this.names.length;
        if (n != other.names.length) {
            return false;
        }
        for (int i = 0; i < n; i++) {
            RDN r1 = this.names[i];
            RDN r2 = other.names[i];
            if (r1.assertion.length != r2.assertion.length) {
                return false;
            }
        }
        String thisCanonical = this.getRFC2253CanonicalName();
        String otherCanonical = other.getRFC2253CanonicalName();
        return thisCanonical.equals(otherCanonical);
    }

    private String getString(DerValue attribute) throws IOException {
        if (attribute == null)
            return null;
        String value = attribute.getAsString();
        if (value == null)
            throw new IOException("not a DER string encoding, " + attribute.tag);
        else
            return value;
    }

    public int getType() {
        return (GeneralNameInterface.NAME_DIRECTORY);
    }

    public String getCountry() throws IOException {
        DerValue attr = findAttribute(countryName_oid);
        return getString(attr);
    }

    public String getOrganization() throws IOException {
        DerValue attr = findAttribute(orgName_oid);
        return getString(attr);
    }

    public String getOrganizationalUnit() throws IOException {
        DerValue attr = findAttribute(orgUnitName_oid);
        return getString(attr);
    }

    public String getCommonName() throws IOException {
        DerValue attr = findAttribute(commonName_oid);
        return getString(attr);
    }

    public String getLocality() throws IOException {
        DerValue attr = findAttribute(localityName_oid);
        return getString(attr);
    }

    public String getState() throws IOException {
        DerValue attr = findAttribute(stateName_oid);
        return getString(attr);
    }

    public String getDomain() throws IOException {
        DerValue attr = findAttribute(DOMAIN_COMPONENT_OID);
        return getString(attr);
    }

    public String getDNQualifier() throws IOException {
        DerValue attr = findAttribute(DNQUALIFIER_OID);
        return getString(attr);
    }

    public String getSurname() throws IOException {
        DerValue attr = findAttribute(SURNAME_OID);
        return getString(attr);
    }

    public String getGivenName() throws IOException {
        DerValue attr = findAttribute(GIVENNAME_OID);
        return getString(attr);
    }

    public String getInitials() throws IOException {
        DerValue attr = findAttribute(INITIALS_OID);
        return getString(attr);
    }

    public String getGeneration() throws IOException {
        DerValue attr = findAttribute(GENERATIONQUALIFIER_OID);
        return getString(attr);
    }

    public String getIP() throws IOException {
        DerValue attr = findAttribute(ipAddress_oid);
        return getString(attr);
    }

    public String toString() {
        if (dn == null) {
            generateDN();
        }
        return dn;
    }

    public String getRFC1779Name() {
        return getRFC1779Name(Collections.<String, String>emptyMap());
    }

    public String getRFC1779Name(Map<String, String> oidMap) throws IllegalArgumentException {
        if (oidMap.isEmpty()) {
            if (rfc1779Dn != null) {
                return rfc1779Dn;
            } else {
                rfc1779Dn = generateRFC1779DN(oidMap);
                return rfc1779Dn;
            }
        }
        return generateRFC1779DN(oidMap);
    }

    public String getRFC2253Name() {
        return getRFC2253Name(Collections.<String, String>emptyMap());
    }

    public String getRFC2253Name(Map<String, String> oidMap) {
        if (oidMap.isEmpty()) {
            if (rfc2253Dn != null) {
                return rfc2253Dn;
            } else {
                rfc2253Dn = generateRFC2253DN(oidMap);
                return rfc2253Dn;
            }
        }
        return generateRFC2253DN(oidMap);
    }

    private String generateRFC2253DN(Map<String, String> oidMap) {
        if (names.length == 0) {
            return "";
        }
        StringBuilder fullname = new StringBuilder(48);
        for (int i = names.length - 1; i >= 0; i--) {
            if (i < names.length - 1) {
                fullname.append(',');
            }
            fullname.append(names[i].toRFC2253String(oidMap));
        }
        return fullname.toString();
    }

    public String getRFC2253CanonicalName() {
        if (canonicalDn != null) {
            return canonicalDn;
        }
        if (names.length == 0) {
            canonicalDn = "";
            return canonicalDn;
        }
        StringBuilder fullname = new StringBuilder(48);
        for (int i = names.length - 1; i >= 0; i--) {
            if (i < names.length - 1) {
                fullname.append(',');
            }
            fullname.append(names[i].toRFC2253String(true));
        }
        canonicalDn = fullname.toString();
        return canonicalDn;
    }

    public String getName() {
        return toString();
    }

    private DerValue findAttribute(ObjectIdentifier attribute) {
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                DerValue value = names[i].findAttribute(attribute);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    public DerValue findMostSpecificAttribute(ObjectIdentifier attribute) {
        if (names != null) {
            for (int i = names.length - 1; i >= 0; i--) {
                DerValue value = names[i].findAttribute(attribute);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    private void parseDER(DerInputStream in) throws IOException {
        DerValue[] nameseq = null;
        byte[] derBytes = in.toByteArray();
        try {
            nameseq = in.getSequence(5);
        } catch (IOException ioe) {
            if (derBytes == null) {
                nameseq = null;
            } else {
                DerValue derVal = new DerValue(DerValue.tag_Sequence, derBytes);
                derBytes = derVal.toByteArray();
                nameseq = new DerInputStream(derBytes).getSequence(5);
            }
        }
        if (nameseq == null) {
            names = new RDN[0];
        } else {
            names = new RDN[nameseq.length];
            for (int i = 0; i < nameseq.length; i++) {
                names[i] = new RDN(nameseq[i]);
            }
        }
    }

    @Deprecated
    public void emit(DerOutputStream out) throws IOException {
        encode(out);
    }

    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        for (int i = 0; i < names.length; i++) {
            names[i].encode(tmp);
        }
        out.write(DerValue.tag_Sequence, tmp);
    }

    public byte[] getEncodedInternal() throws IOException {
        if (encoded == null) {
            DerOutputStream out = new DerOutputStream();
            DerOutputStream tmp = new DerOutputStream();
            for (int i = 0; i < names.length; i++) {
                names[i].encode(tmp);
            }
            out.write(DerValue.tag_Sequence, tmp);
            encoded = out.toByteArray();
        }
        return encoded;
    }

    public byte[] getEncoded() throws IOException {
        return getEncodedInternal().clone();
    }

    private void parseDN(String input, Map<String, String> keywordMap) throws IOException {
        if (input == null || input.length() == 0) {
            names = new RDN[0];
            return;
        }
        List<RDN> dnVector = new ArrayList<>();
        int dnOffset = 0;
        int rdnEnd;
        String rdnString;
        int quoteCount = 0;
        String dnString = input;
        int searchOffset = 0;
        int nextComma = dnString.indexOf(',');
        int nextSemiColon = dnString.indexOf(';');
        while (nextComma >= 0 || nextSemiColon >= 0) {
            if (nextSemiColon < 0) {
                rdnEnd = nextComma;
            } else if (nextComma < 0) {
                rdnEnd = nextSemiColon;
            } else {
                rdnEnd = Math.min(nextComma, nextSemiColon);
            }
            quoteCount += countQuotes(dnString, searchOffset, rdnEnd);
            if (rdnEnd >= 0 && quoteCount != 1 && !escaped(rdnEnd, searchOffset, dnString)) {
                rdnString = dnString.substring(dnOffset, rdnEnd);
                RDN rdn = new RDN(rdnString, keywordMap);
                dnVector.add(rdn);
                dnOffset = rdnEnd + 1;
                quoteCount = 0;
            }
            searchOffset = rdnEnd + 1;
            nextComma = dnString.indexOf(',', searchOffset);
            nextSemiColon = dnString.indexOf(';', searchOffset);
        }
        rdnString = dnString.substring(dnOffset);
        RDN rdn = new RDN(rdnString, keywordMap);
        dnVector.add(rdn);
        Collections.reverse(dnVector);
        names = dnVector.toArray(new RDN[dnVector.size()]);
    }

    private void parseRFC2253DN(String dnString) throws IOException {
        if (dnString.length() == 0) {
            names = new RDN[0];
            return;
        }
        List<RDN> dnVector = new ArrayList<>();
        int dnOffset = 0;
        String rdnString;
        int searchOffset = 0;
        int rdnEnd = dnString.indexOf(',');
        while (rdnEnd >= 0) {
            if (rdnEnd > 0 && !escaped(rdnEnd, searchOffset, dnString)) {
                rdnString = dnString.substring(dnOffset, rdnEnd);
                RDN rdn = new RDN(rdnString, "RFC2253");
                dnVector.add(rdn);
                dnOffset = rdnEnd + 1;
            }
            searchOffset = rdnEnd + 1;
            rdnEnd = dnString.indexOf(',', searchOffset);
        }
        rdnString = dnString.substring(dnOffset);
        RDN rdn = new RDN(rdnString, "RFC2253");
        dnVector.add(rdn);
        Collections.reverse(dnVector);
        names = dnVector.toArray(new RDN[dnVector.size()]);
    }

    static int countQuotes(String string, int from, int to) {
        int count = 0;
        for (int i = from; i < to; i++) {
            if ((string.charAt(i) == '"' && i == from) || (string.charAt(i) == '"' && string.charAt(i - 1) != '\\')) {
                count++;
            }
        }
        return count;
    }

    private static boolean escaped(int rdnEnd, int searchOffset, String dnString) {
        if (rdnEnd == 1 && dnString.charAt(rdnEnd - 1) == '\\') {
            return true;
        } else if (rdnEnd > 1 && dnString.charAt(rdnEnd - 1) == '\\' && dnString.charAt(rdnEnd - 2) != '\\') {
            return true;
        } else if (rdnEnd > 1 && dnString.charAt(rdnEnd - 1) == '\\' && dnString.charAt(rdnEnd - 2) == '\\') {
            int count = 0;
            rdnEnd--;
            while (rdnEnd >= searchOffset) {
                if (dnString.charAt(rdnEnd) == '\\') {
                    count++;
                }
                rdnEnd--;
            }
            return (count % 2) != 0 ? true : false;
        } else {
            return false;
        }
    }

    private void generateDN() {
        if (names.length == 1) {
            dn = names[0].toString();
            return;
        }
        StringBuilder sb = new StringBuilder(48);
        if (names != null) {
            for (int i = names.length - 1; i >= 0; i--) {
                if (i != names.length - 1) {
                    sb.append(", ");
                }
                sb.append(names[i].toString());
            }
        }
        dn = sb.toString();
    }

    private String generateRFC1779DN(Map<String, String> oidMap) {
        if (names.length == 1) {
            return names[0].toRFC1779String(oidMap);
        }
        StringBuilder sb = new StringBuilder(48);
        if (names != null) {
            for (int i = names.length - 1; i >= 0; i--) {
                if (i != names.length - 1) {
                    sb.append(", ");
                }
                sb.append(names[i].toRFC1779String(oidMap));
            }
        }
        return sb.toString();
    }

    static ObjectIdentifier intern(ObjectIdentifier oid) {
        ObjectIdentifier interned = internedOIDs.get(oid);
        if (interned != null) {
            return interned;
        }
        internedOIDs.put(oid, oid);
        return oid;
    }

    private static final Map<ObjectIdentifier, ObjectIdentifier> internedOIDs = new HashMap<ObjectIdentifier, ObjectIdentifier>();

    private static final int[] commonName_data = { 2, 5, 4, 3 };

    private static final int[] SURNAME_DATA = { 2, 5, 4, 4 };

    private static final int[] SERIALNUMBER_DATA = { 2, 5, 4, 5 };

    private static final int[] countryName_data = { 2, 5, 4, 6 };

    private static final int[] localityName_data = { 2, 5, 4, 7 };

    private static final int[] stateName_data = { 2, 5, 4, 8 };

    private static final int[] streetAddress_data = { 2, 5, 4, 9 };

    private static final int[] orgName_data = { 2, 5, 4, 10 };

    private static final int[] orgUnitName_data = { 2, 5, 4, 11 };

    private static final int[] title_data = { 2, 5, 4, 12 };

    private static final int[] GIVENNAME_DATA = { 2, 5, 4, 42 };

    private static final int[] INITIALS_DATA = { 2, 5, 4, 43 };

    private static final int[] GENERATIONQUALIFIER_DATA = { 2, 5, 4, 44 };

    private static final int[] DNQUALIFIER_DATA = { 2, 5, 4, 46 };

    private static final int[] ipAddress_data = { 1, 3, 6, 1, 4, 1, 42, 2, 11, 2, 1 };

    private static final int[] DOMAIN_COMPONENT_DATA = { 0, 9, 2342, 19200300, 100, 1, 25 };

    private static final int[] userid_data = { 0, 9, 2342, 19200300, 100, 1, 1 };

    public static final ObjectIdentifier commonName_oid;

    public static final ObjectIdentifier countryName_oid;

    public static final ObjectIdentifier localityName_oid;

    public static final ObjectIdentifier orgName_oid;

    public static final ObjectIdentifier orgUnitName_oid;

    public static final ObjectIdentifier stateName_oid;

    public static final ObjectIdentifier streetAddress_oid;

    public static final ObjectIdentifier title_oid;

    public static final ObjectIdentifier DNQUALIFIER_OID;

    public static final ObjectIdentifier SURNAME_OID;

    public static final ObjectIdentifier GIVENNAME_OID;

    public static final ObjectIdentifier INITIALS_OID;

    public static final ObjectIdentifier GENERATIONQUALIFIER_OID;

    public static final ObjectIdentifier ipAddress_oid;

    public static final ObjectIdentifier DOMAIN_COMPONENT_OID;

    public static final ObjectIdentifier userid_oid;

    public static final ObjectIdentifier SERIALNUMBER_OID;

    static {
        commonName_oid = intern(ObjectIdentifier.newInternal(commonName_data));
        SERIALNUMBER_OID = intern(ObjectIdentifier.newInternal(SERIALNUMBER_DATA));
        countryName_oid = intern(ObjectIdentifier.newInternal(countryName_data));
        localityName_oid = intern(ObjectIdentifier.newInternal(localityName_data));
        orgName_oid = intern(ObjectIdentifier.newInternal(orgName_data));
        orgUnitName_oid = intern(ObjectIdentifier.newInternal(orgUnitName_data));
        stateName_oid = intern(ObjectIdentifier.newInternal(stateName_data));
        streetAddress_oid = intern(ObjectIdentifier.newInternal(streetAddress_data));
        title_oid = intern(ObjectIdentifier.newInternal(title_data));
        DNQUALIFIER_OID = intern(ObjectIdentifier.newInternal(DNQUALIFIER_DATA));
        SURNAME_OID = intern(ObjectIdentifier.newInternal(SURNAME_DATA));
        GIVENNAME_OID = intern(ObjectIdentifier.newInternal(GIVENNAME_DATA));
        INITIALS_OID = intern(ObjectIdentifier.newInternal(INITIALS_DATA));
        GENERATIONQUALIFIER_OID = intern(ObjectIdentifier.newInternal(GENERATIONQUALIFIER_DATA));
        ipAddress_oid = intern(ObjectIdentifier.newInternal(ipAddress_data));
        DOMAIN_COMPONENT_OID = intern(ObjectIdentifier.newInternal(DOMAIN_COMPONENT_DATA));
        userid_oid = intern(ObjectIdentifier.newInternal(userid_data));
    }

    public int constrains(GeneralNameInterface inputName) throws UnsupportedOperationException {
        int constraintType;
        if (inputName == null) {
            constraintType = NAME_DIFF_TYPE;
        } else if (inputName.getType() != NAME_DIRECTORY) {
            constraintType = NAME_DIFF_TYPE;
        } else {
            X500Name inputX500 = (X500Name) inputName;
            if (inputX500.equals(this)) {
                constraintType = NAME_MATCH;
            } else if (inputX500.names.length == 0) {
                constraintType = NAME_WIDENS;
            } else if (this.names.length == 0) {
                constraintType = NAME_NARROWS;
            } else if (inputX500.isWithinSubtree(this)) {
                constraintType = NAME_NARROWS;
            } else if (isWithinSubtree(inputX500)) {
                constraintType = NAME_WIDENS;
            } else {
                constraintType = NAME_SAME_TYPE;
            }
        }
        return constraintType;
    }

    private boolean isWithinSubtree(X500Name other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other.names.length == 0) {
            return true;
        }
        if (this.names.length == 0) {
            return false;
        }
        if (names.length < other.names.length) {
            return false;
        }
        for (int i = 0; i < other.names.length; i++) {
            if (!names[i].equals(other.names[i])) {
                return false;
            }
        }
        return true;
    }

    public int subtreeDepth() throws UnsupportedOperationException {
        return names.length;
    }

    public X500Name commonAncestor(X500Name other) {
        if (other == null) {
            return null;
        }
        int otherLen = other.names.length;
        int thisLen = this.names.length;
        if (thisLen == 0 || otherLen == 0) {
            return null;
        }
        int minLen = (thisLen < otherLen) ? thisLen : otherLen;
        int i = 0;
        for (; i < minLen; i++) {
            if (!names[i].equals(other.names[i])) {
                if (i == 0) {
                    return null;
                } else {
                    break;
                }
            }
        }
        RDN[] ancestor = new RDN[i];
        for (int j = 0; j < i; j++) {
            ancestor[j] = names[j];
        }
        X500Name commonAncestor = null;
        try {
            commonAncestor = new X500Name(ancestor);
        } catch (IOException ioe) {
            return null;
        }
        return commonAncestor;
    }

    private static final Constructor<X500Principal> principalConstructor;

    private static final Field principalField;

    static {
        PrivilegedExceptionAction<Object[]> pa = new PrivilegedExceptionAction<Object[]>() {

            public Object[] run() throws Exception {
                Class<X500Principal> pClass = X500Principal.class;
                Class<?>[] args = new Class<?>[] { X500Name.class };
                Constructor<X500Principal> cons = pClass.getDeclaredConstructor(args);
                cons.setAccessible(true);
                Field field = pClass.getDeclaredField("thisX500Name");
                field.setAccessible(true);
                return new Object[] { cons, field };
            }
        };
        try {
            Object[] result = AccessController.doPrivileged(pa);
            @SuppressWarnings("unchecked")
            Constructor<X500Principal> constr = (Constructor<X500Principal>) result[0];
            principalConstructor = constr;
            principalField = (Field) result[1];
        } catch (Exception e) {
            throw new InternalError("Could not obtain X500Principal access", e);
        }
    }

    public X500Principal asX500Principal() {
        if (x500Principal == null) {
            try {
                Object[] args = new Object[] { this };
                x500Principal = principalConstructor.newInstance(args);
            } catch (Exception e) {
                throw new RuntimeException("Unexpected exception", e);
            }
        }
        return x500Principal;
    }

    public static X500Name asX500Name(X500Principal p) {
        try {
            X500Name name = (X500Name) principalField.get(p);
            name.x500Principal = p;
            return name;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
}
