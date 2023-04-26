package java.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.spi.CurrencyNameProvider;
import java.util.spi.LocaleServiceProvider;
import sun.util.LocaleServiceProviderPool;
import sun.util.logging.PlatformLogger;
import sun.util.resources.LocaleData;
import sun.util.resources.OpenListResourceBundle;

public final class Currency implements Serializable {

    private static final long serialVersionUID = -158308464356906721L;

    private final String currencyCode;

    transient private final int defaultFractionDigits;

    transient private final int numericCode;

    private static ConcurrentMap<String, Currency> instances = new ConcurrentHashMap<>(7);

    private static HashSet<Currency> available;

    static int formatVersion;

    static int dataVersion;

    static int[] mainTable;

    static long[] scCutOverTimes;

    static String[] scOldCurrencies;

    static String[] scNewCurrencies;

    static int[] scOldCurrenciesDFD;

    static int[] scNewCurrenciesDFD;

    static int[] scOldCurrenciesNumericCode;

    static int[] scNewCurrenciesNumericCode;

    static String otherCurrencies;

    static int[] otherCurrenciesDFD;

    static int[] otherCurrenciesNumericCode;

    private static final int MAGIC_NUMBER = 0x43757244;

    private static final int A_TO_Z = ('Z' - 'A') + 1;

    private static final int INVALID_COUNTRY_ENTRY = 0x007F;

    private static final int COUNTRY_WITHOUT_CURRENCY_ENTRY = 0x0080;

    private static final int SIMPLE_CASE_COUNTRY_MASK = 0x0000;

    private static final int SIMPLE_CASE_COUNTRY_FINAL_CHAR_MASK = 0x001F;

    private static final int SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_MASK = 0x0060;

    private static final int SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_SHIFT = 5;

    private static final int SPECIAL_CASE_COUNTRY_MASK = 0x0080;

    private static final int SPECIAL_CASE_COUNTRY_INDEX_MASK = 0x001F;

    private static final int SPECIAL_CASE_COUNTRY_INDEX_DELTA = 1;

    private static final int COUNTRY_TYPE_MASK = SIMPLE_CASE_COUNTRY_MASK | SPECIAL_CASE_COUNTRY_MASK;

    private static final int NUMERIC_CODE_MASK = 0x0003FF00;

    private static final int NUMERIC_CODE_SHIFT = 8;

    private static final int VALID_FORMAT_VERSION = 1;

    static {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            public Object run() {
                String homeDir = System.getProperty("java.home");
                try {
                    String dataFile = homeDir + File.separator + "lib" + File.separator + "currency.data";
                    DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(dataFile)));
                    if (dis.readInt() != MAGIC_NUMBER) {
                        throw new InternalError("Currency data is possibly corrupted");
                    }
                    formatVersion = dis.readInt();
                    if (formatVersion != VALID_FORMAT_VERSION) {
                        throw new InternalError("Currency data format is incorrect");
                    }
                    dataVersion = dis.readInt();
                    mainTable = readIntArray(dis, A_TO_Z * A_TO_Z);
                    int scCount = dis.readInt();
                    scCutOverTimes = readLongArray(dis, scCount);
                    scOldCurrencies = readStringArray(dis, scCount);
                    scNewCurrencies = readStringArray(dis, scCount);
                    scOldCurrenciesDFD = readIntArray(dis, scCount);
                    scNewCurrenciesDFD = readIntArray(dis, scCount);
                    scOldCurrenciesNumericCode = readIntArray(dis, scCount);
                    scNewCurrenciesNumericCode = readIntArray(dis, scCount);
                    int ocCount = dis.readInt();
                    otherCurrencies = dis.readUTF();
                    otherCurrenciesDFD = readIntArray(dis, ocCount);
                    otherCurrenciesNumericCode = readIntArray(dis, ocCount);
                    dis.close();
                } catch (IOException e) {
                    throw new InternalError(e);
                }
                try {
                    File propFile = new File(homeDir + File.separator + "lib" + File.separator + "currency.properties");
                    if (propFile.exists()) {
                        Properties props = new Properties();
                        try (FileReader fr = new FileReader(propFile)) {
                            props.load(fr);
                        }
                        Set<String> keys = props.stringPropertyNames();
                        Pattern propertiesPattern = Pattern.compile("([A-Z]{3})\\s*,\\s*(\\d{3})\\s*,\\s*([0-3])");
                        for (String key : keys) {
                            replaceCurrencyData(propertiesPattern, key.toUpperCase(Locale.ROOT), props.getProperty(key).toUpperCase(Locale.ROOT));
                        }
                    }
                } catch (IOException e) {
                    info("currency.properties is ignored because of an IOException", e);
                }
                return null;
            }
        });
    }

    private static final int SYMBOL = 0;

    private static final int DISPLAYNAME = 1;

    private Currency(String currencyCode, int defaultFractionDigits, int numericCode) {
        this.currencyCode = currencyCode;
        this.defaultFractionDigits = defaultFractionDigits;
        this.numericCode = numericCode;
    }

    public static Currency getInstance(String currencyCode) {
        return getInstance(currencyCode, Integer.MIN_VALUE, 0);
    }

    private static Currency getInstance(String currencyCode, int defaultFractionDigits, int numericCode) {
        Currency instance = instances.get(currencyCode);
        if (instance != null) {
            return instance;
        }
        if (defaultFractionDigits == Integer.MIN_VALUE) {
            if (currencyCode.length() != 3) {
                throw new IllegalArgumentException();
            }
            char char1 = currencyCode.charAt(0);
            char char2 = currencyCode.charAt(1);
            int tableEntry = getMainTableEntry(char1, char2);
            if ((tableEntry & COUNTRY_TYPE_MASK) == SIMPLE_CASE_COUNTRY_MASK && tableEntry != INVALID_COUNTRY_ENTRY && currencyCode.charAt(2) - 'A' == (tableEntry & SIMPLE_CASE_COUNTRY_FINAL_CHAR_MASK)) {
                defaultFractionDigits = (tableEntry & SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_MASK) >> SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_SHIFT;
                numericCode = (tableEntry & NUMERIC_CODE_MASK) >> NUMERIC_CODE_SHIFT;
            } else {
                if (currencyCode.charAt(2) == '-') {
                    throw new IllegalArgumentException();
                }
                int index = otherCurrencies.indexOf(currencyCode);
                if (index == -1) {
                    throw new IllegalArgumentException();
                }
                defaultFractionDigits = otherCurrenciesDFD[index / 4];
                numericCode = otherCurrenciesNumericCode[index / 4];
            }
        }
        Currency currencyVal = new Currency(currencyCode, defaultFractionDigits, numericCode);
        instance = instances.putIfAbsent(currencyCode, currencyVal);
        return (instance != null ? instance : currencyVal);
    }

    public static Currency getInstance(Locale locale) {
        String country = locale.getCountry();
        if (country == null) {
            throw new NullPointerException();
        }
        if (country.length() != 2) {
            throw new IllegalArgumentException();
        }
        char char1 = country.charAt(0);
        char char2 = country.charAt(1);
        int tableEntry = getMainTableEntry(char1, char2);
        if ((tableEntry & COUNTRY_TYPE_MASK) == SIMPLE_CASE_COUNTRY_MASK && tableEntry != INVALID_COUNTRY_ENTRY) {
            char finalChar = (char) ((tableEntry & SIMPLE_CASE_COUNTRY_FINAL_CHAR_MASK) + 'A');
            int defaultFractionDigits = (tableEntry & SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_MASK) >> SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_SHIFT;
            int numericCode = (tableEntry & NUMERIC_CODE_MASK) >> NUMERIC_CODE_SHIFT;
            StringBuffer sb = new StringBuffer(country);
            sb.append(finalChar);
            return getInstance(sb.toString(), defaultFractionDigits, numericCode);
        } else {
            if (tableEntry == INVALID_COUNTRY_ENTRY) {
                throw new IllegalArgumentException();
            }
            if (tableEntry == COUNTRY_WITHOUT_CURRENCY_ENTRY) {
                return null;
            } else {
                int index = (tableEntry & SPECIAL_CASE_COUNTRY_INDEX_MASK) - SPECIAL_CASE_COUNTRY_INDEX_DELTA;
                if (scCutOverTimes[index] == Long.MAX_VALUE || System.currentTimeMillis() < scCutOverTimes[index]) {
                    return getInstance(scOldCurrencies[index], scOldCurrenciesDFD[index], scOldCurrenciesNumericCode[index]);
                } else {
                    return getInstance(scNewCurrencies[index], scNewCurrenciesDFD[index], scNewCurrenciesNumericCode[index]);
                }
            }
        }
    }

    public static Set<Currency> getAvailableCurrencies() {
        synchronized (Currency.class) {
            if (available == null) {
                available = new HashSet<>(256);
                for (char c1 = 'A'; c1 <= 'Z'; c1++) {
                    for (char c2 = 'A'; c2 <= 'Z'; c2++) {
                        int tableEntry = getMainTableEntry(c1, c2);
                        if ((tableEntry & COUNTRY_TYPE_MASK) == SIMPLE_CASE_COUNTRY_MASK && tableEntry != INVALID_COUNTRY_ENTRY) {
                            char finalChar = (char) ((tableEntry & SIMPLE_CASE_COUNTRY_FINAL_CHAR_MASK) + 'A');
                            int defaultFractionDigits = (tableEntry & SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_MASK) >> SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_SHIFT;
                            int numericCode = (tableEntry & NUMERIC_CODE_MASK) >> NUMERIC_CODE_SHIFT;
                            StringBuilder sb = new StringBuilder();
                            sb.append(c1);
                            sb.append(c2);
                            sb.append(finalChar);
                            available.add(getInstance(sb.toString(), defaultFractionDigits, numericCode));
                        }
                    }
                }
                StringTokenizer st = new StringTokenizer(otherCurrencies, "-");
                while (st.hasMoreElements()) {
                    available.add(getInstance((String) st.nextElement()));
                }
            }
        }
        @SuppressWarnings("unchecked")
        Set<Currency> result = (Set<Currency>) available.clone();
        return result;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getSymbol() {
        return getSymbol(Locale.getDefault(Locale.Category.DISPLAY));
    }

    public String getSymbol(Locale locale) {
        try {
            LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(CurrencyNameProvider.class);
            if (pool.hasProviders()) {
                String symbol = pool.getLocalizedObject(CurrencyNameGetter.INSTANCE, locale, (OpenListResourceBundle) null, currencyCode, SYMBOL);
                if (symbol != null) {
                    return symbol;
                }
            }
            ResourceBundle bundle = LocaleData.getCurrencyNames(locale);
            return bundle.getString(currencyCode);
        } catch (MissingResourceException e) {
            return currencyCode;
        }
    }

    public int getDefaultFractionDigits() {
        return defaultFractionDigits;
    }

    public int getNumericCode() {
        return numericCode;
    }

    public String getDisplayName() {
        return getDisplayName(Locale.getDefault(Locale.Category.DISPLAY));
    }

    public String getDisplayName(Locale locale) {
        try {
            OpenListResourceBundle bundle = LocaleData.getCurrencyNames(locale);
            String result = null;
            String bundleKey = currencyCode.toLowerCase(Locale.ROOT);
            LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(CurrencyNameProvider.class);
            if (pool.hasProviders()) {
                result = pool.getLocalizedObject(CurrencyNameGetter.INSTANCE, locale, bundleKey, bundle, currencyCode, DISPLAYNAME);
            }
            if (result == null) {
                result = bundle.getString(bundleKey);
            }
            if (result != null) {
                return result;
            }
        } catch (MissingResourceException e) {
        }
        return currencyCode;
    }

    public String toString() {
        return currencyCode;
    }

    private Object readResolve() {
        return getInstance(currencyCode);
    }

    private static int getMainTableEntry(char char1, char char2) {
        if (char1 < 'A' || char1 > 'Z' || char2 < 'A' || char2 > 'Z') {
            throw new IllegalArgumentException();
        }
        return mainTable[(char1 - 'A') * A_TO_Z + (char2 - 'A')];
    }

    private static void setMainTableEntry(char char1, char char2, int entry) {
        if (char1 < 'A' || char1 > 'Z' || char2 < 'A' || char2 > 'Z') {
            throw new IllegalArgumentException();
        }
        mainTable[(char1 - 'A') * A_TO_Z + (char2 - 'A')] = entry;
    }

    private static class CurrencyNameGetter implements LocaleServiceProviderPool.LocalizedObjectGetter<CurrencyNameProvider, String> {

        private static final CurrencyNameGetter INSTANCE = new CurrencyNameGetter();

        public String getObject(CurrencyNameProvider currencyNameProvider, Locale locale, String key, Object... params) {
            assert params.length == 1;
            int type = (Integer) params[0];
            switch(type) {
                case SYMBOL:
                    return currencyNameProvider.getSymbol(key, locale);
                case DISPLAYNAME:
                    return currencyNameProvider.getDisplayName(key, locale);
                default:
                    assert false;
            }
            return null;
        }
    }

    private static int[] readIntArray(DataInputStream dis, int count) throws IOException {
        int[] ret = new int[count];
        for (int i = 0; i < count; i++) {
            ret[i] = dis.readInt();
        }
        return ret;
    }

    private static long[] readLongArray(DataInputStream dis, int count) throws IOException {
        long[] ret = new long[count];
        for (int i = 0; i < count; i++) {
            ret[i] = dis.readLong();
        }
        return ret;
    }

    private static String[] readStringArray(DataInputStream dis, int count) throws IOException {
        String[] ret = new String[count];
        for (int i = 0; i < count; i++) {
            ret[i] = dis.readUTF();
        }
        return ret;
    }

    private static void replaceCurrencyData(Pattern pattern, String ctry, String curdata) {
        if (ctry.length() != 2) {
            String message = new StringBuilder().append("The entry in currency.properties for ").append(ctry).append(" is ignored because of the invalid country code.").toString();
            info(message, null);
            return;
        }
        Matcher m = pattern.matcher(curdata);
        if (!m.find()) {
            String message = new StringBuilder().append("The entry in currency.properties for ").append(ctry).append(" is ignored because the value format is not recognized.").toString();
            info(message, null);
            return;
        }
        String code = m.group(1);
        int numeric = Integer.parseInt(m.group(2));
        int fraction = Integer.parseInt(m.group(3));
        int entry = numeric << NUMERIC_CODE_SHIFT;
        int index;
        for (index = 0; index < scOldCurrencies.length; index++) {
            if (scOldCurrencies[index].equals(code)) {
                break;
            }
        }
        if (index == scOldCurrencies.length) {
            entry |= (fraction << SIMPLE_CASE_COUNTRY_DEFAULT_DIGITS_SHIFT) | (code.charAt(2) - 'A');
        } else {
            entry |= SPECIAL_CASE_COUNTRY_MASK | (index + SPECIAL_CASE_COUNTRY_INDEX_DELTA);
        }
        setMainTableEntry(ctry.charAt(0), ctry.charAt(1), entry);
    }

    private static void info(String message, Throwable t) {
        PlatformLogger logger = PlatformLogger.getLogger("java.util.Currency");
        if (logger.isLoggable(PlatformLogger.INFO)) {
            if (t != null) {
                logger.info(message, t);
            } else {
                logger.info(message);
            }
        }
    }
}
