package hudson.util;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import jenkins.util.SystemProperties;
import java.util.Arrays;
import jenkins.model.Jenkins;
import hudson.Util;
import jenkins.security.CryptoConfidentialKey;
import org.kohsuke.stapler.Stapler;
import javax.crypto.Cipher;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class Secret implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(Secret.class.getName());

    private static final byte PAYLOAD_V1 = 1;

    @Nonnull
    private final String value;

    private byte[] iv;

    Secret(String value) {
        this.value = value;
    }

    Secret(String value, byte[] iv) {
        this.value = value;
        this.iv = iv;
    }

    @Override
    @Deprecated
    public String toString() {
        final String from = new Throwable().getStackTrace()[1].toString();
        LOGGER.warning("Use of toString() on hudson.util.Secret from " + from + ". Prefer getPlainText() or getEncryptedValue() depending your needs. see https://jenkins.io/redirect/hudson.util.Secret/");
        return value;
    }

    @Nonnull
    public String getPlainText() {
        return value;
    }

    @Override
    public boolean equals(Object that) {
        return that instanceof Secret && value.equals(((Secret) that).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public String getEncryptedValue() {
        try {
            synchronized (this) {
                if (iv == null) {
                    iv = KEY.newIv();
                }
            }
            Cipher cipher = KEY.encrypt(iv);
            byte[] encrypted = cipher.doFinal(this.value.getBytes(UTF_8));
            byte[] payload = new byte[1 + 8 + iv.length + encrypted.length];
            int pos = 0;
            payload[pos++] = PAYLOAD_V1;
            payload[pos++] = (byte) (iv.length >> 24);
            payload[pos++] = (byte) (iv.length >> 16);
            payload[pos++] = (byte) (iv.length >> 8);
            payload[pos++] = (byte) (iv.length);
            payload[pos++] = (byte) (encrypted.length >> 24);
            payload[pos++] = (byte) (encrypted.length >> 16);
            payload[pos++] = (byte) (encrypted.length >> 8);
            payload[pos++] = (byte) (encrypted.length);
            System.arraycopy(iv, 0, payload, pos, iv.length);
            pos += iv.length;
            System.arraycopy(encrypted, 0, payload, pos, encrypted.length);
            return "{" + new String(Base64.getEncoder().encode(payload)) + "}";
        } catch (GeneralSecurityException e) {
            throw new Error(e);
        }
    }

    @Restricted(NoExternalUse.class)
    public static final Pattern ENCRYPTED_VALUE_PATTERN = Pattern.compile("\\{?[A-Za-z0-9+/]+={0,2}}?");

    @CheckForNull
    public static Secret decrypt(@CheckForNull String data) {
        if (!isValidData(data))
            return null;
        if (data.startsWith("{") && data.endsWith("}")) {
            byte[] payload;
            try {
                payload = Base64.getDecoder().decode(data.substring(1, data.length() - 1));
            } catch (IllegalArgumentException e) {
                return null;
            }
            switch(payload[0]) {
                case PAYLOAD_V1:
                    int ivLength = ((payload[1] & 0xff) << 24) | ((payload[2] & 0xff) << 16) | ((payload[3] & 0xff) << 8) | (payload[4] & 0xff);
                    int dataLength = ((payload[5] & 0xff) << 24) | ((payload[6] & 0xff) << 16) | ((payload[7] & 0xff) << 8) | (payload[8] & 0xff);
                    if (payload.length != 1 + 8 + ivLength + dataLength) {
                        return null;
                    }
                    byte[] iv = Arrays.copyOfRange(payload, 9, 9 + ivLength);
                    byte[] code = Arrays.copyOfRange(payload, 9 + ivLength, payload.length);
                    String text;
                    try {
                        text = new String(KEY.decrypt(iv).doFinal(code), UTF_8);
                    } catch (GeneralSecurityException e) {
                        return null;
                    }
                    return new Secret(text, iv);
                default:
                    return null;
            }
        } else {
            try {
                return HistoricalSecrets.decrypt(data, KEY);
            } catch (UnsupportedEncodingException e) {
                throw new Error(e);
            } catch (GeneralSecurityException | IOException e) {
                return null;
            }
        }
    }

    private static boolean isValidData(String data) {
        if (data == null || "{}".equals(data) || "".equals(data.trim()))
            return false;
        if (data.startsWith("{") && data.endsWith("}")) {
            return !"".equals(data.substring(1, data.length() - 1).trim());
        }
        return true;
    }

    public static Cipher getCipher(String algorithm) throws GeneralSecurityException {
        return PROVIDER != null ? Cipher.getInstance(algorithm, PROVIDER) : Cipher.getInstance(algorithm);
    }

    @Nonnull
    public static Secret fromString(@CheckForNull String data) {
        data = Util.fixNull(data);
        Secret s = decrypt(data);
        if (s == null)
            s = new Secret(data);
        return s;
    }

    @Nonnull
    public static String toString(@CheckForNull Secret s) {
        return s == null ? "" : s.value;
    }

    public static final class ConverterImpl implements Converter {

        public ConverterImpl() {
        }

        public boolean canConvert(Class type) {
            return type == Secret.class;
        }

        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            Secret src = (Secret) source;
            writer.setValue(src.getEncryptedValue());
        }

        public Object unmarshal(HierarchicalStreamReader reader, final UnmarshallingContext context) {
            return fromString(reader.getValue());
        }
    }

    private static final String PROVIDER = SystemProperties.getString(Secret.class.getName() + ".provider");

    static String SECRET = null;

    private static final CryptoConfidentialKey KEY = new CryptoConfidentialKey(Secret.class.getName());

    private static final long serialVersionUID = 1L;

    static {
        Stapler.CONVERT_UTILS.register(new org.apache.commons.beanutils.Converter() {

            public Secret convert(Class type, Object value) {
                return Secret.fromString(value.toString());
            }
        }, Secret.class);
    }
}
