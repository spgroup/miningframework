package com.sun.org.apache.xerces.internal.impl.dv.xs;

import com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import com.sun.org.apache.xerces.internal.util.URI;
import com.sun.org.apache.xerces.internal.impl.dv.ValidationContext;

public class AnyURIDV extends TypeValidator {

    private static final URI BASE_URI;

    static {
        URI uri = null;
        try {
            uri = new URI("abc://def.ghi.jkl");
        } catch (URI.MalformedURIException ex) {
        }
        BASE_URI = uri;
    }

    public short getAllowedFacets() {
        return (XSSimpleTypeDecl.FACET_LENGTH | XSSimpleTypeDecl.FACET_MINLENGTH | XSSimpleTypeDecl.FACET_MAXLENGTH | XSSimpleTypeDecl.FACET_PATTERN | XSSimpleTypeDecl.FACET_ENUMERATION | XSSimpleTypeDecl.FACET_WHITESPACE);
    }

    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException {
        try {
            if (content.length() != 0) {
                final String encoded = encode(content);
                new URI(BASE_URI, encoded);
            }
        } catch (URI.MalformedURIException ex) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[] { content, "anyURI" });
        }
        return content;
    }

    private static boolean[] gNeedEscaping = new boolean[128];

    private static char[] gAfterEscaping1 = new char[128];

    private static char[] gAfterEscaping2 = new char[128];

    private static char[] gHexChs = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    static {
        for (int i = 0; i <= 0x1f; i++) {
            gNeedEscaping[i] = true;
            gAfterEscaping1[i] = gHexChs[i >> 4];
            gAfterEscaping2[i] = gHexChs[i & 0xf];
        }
        gNeedEscaping[0x7f] = true;
        gAfterEscaping1[0x7f] = '7';
        gAfterEscaping2[0x7f] = 'F';
        char[] escChs = { ' ', '<', '>', '"', '{', '}', '|', '\\', '^', '~', '`' };
        int len = escChs.length;
        char ch;
        for (int i = 0; i < len; i++) {
            ch = escChs[i];
            gNeedEscaping[ch] = true;
            gAfterEscaping1[ch] = gHexChs[ch >> 4];
            gAfterEscaping2[ch] = gHexChs[ch & 0xf];
        }
    }

    private static String encode(String anyURI) {
        int len = anyURI.length(), ch;
        StringBuffer buffer = new StringBuffer(len * 3);
        int i = 0;
        for (; i < len; i++) {
            ch = anyURI.charAt(i);
            if (ch >= 128)
                break;
            if (gNeedEscaping[ch]) {
                buffer.append('%');
                buffer.append(gAfterEscaping1[ch]);
                buffer.append(gAfterEscaping2[ch]);
            } else {
                buffer.append((char) ch);
            }
        }
        if (i < len) {
            byte[] bytes = null;
            byte b;
            try {
                bytes = anyURI.substring(i).getBytes("UTF-8");
            } catch (java.io.UnsupportedEncodingException e) {
                return anyURI;
            }
            len = bytes.length;
            for (i = 0; i < len; i++) {
                b = bytes[i];
                if (b < 0) {
                    ch = b + 256;
                    buffer.append('%');
                    buffer.append(gHexChs[ch >> 4]);
                    buffer.append(gHexChs[ch & 0xf]);
                } else if (gNeedEscaping[b]) {
                    buffer.append('%');
                    buffer.append(gAfterEscaping1[b]);
                    buffer.append(gAfterEscaping2[b]);
                } else {
                    buffer.append((char) b);
                }
            }
        }
        if (buffer.length() != len) {
            return buffer.toString();
        } else {
            return anyURI;
        }
    }
}