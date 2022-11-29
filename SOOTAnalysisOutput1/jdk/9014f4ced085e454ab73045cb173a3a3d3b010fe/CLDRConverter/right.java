package build.tools.cldrconverter;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class CLDRConverter {

    static final String LDML_DTD_SYSTEM_ID = "http://www.unicode.org/cldr/dtd/2.0/ldml.dtd";

    static final String SPPL_LDML_DTD_SYSTEM_ID = "http://www.unicode.org/cldr/dtd/2.0/ldmlSupplemental.dtd";

    private static String CLDR_BASE = "../CLDR/21.0.1/";

    static String LOCAL_LDML_DTD;

    static String LOCAL_SPPL_LDML_DTD;

    private static String SOURCE_FILE_DIR;

    private static String SPPL_SOURCE_FILE;

    private static String NUMBERING_SOURCE_FILE;

    private static String METAZONES_SOURCE_FILE;

    static String DESTINATION_DIR = "build/gensrc";

    static final String LOCALE_NAME_PREFIX = "locale.displayname.";

    static final String CURRENCY_SYMBOL_PREFIX = "currency.symbol.";

    static final String CURRENCY_NAME_PREFIX = "currency.displayname.";

    static final String TIMEZONE_ID_PREFIX = "timezone.id.";

    static final String TIMEZONE_NAME_PREFIX = "timezone.displayname.";

    static final String METAZONE_ID_PREFIX = "metazone.id.";

    static final String METAZONE_NAME_PREFIX = "metazone.displayname.";

    private static SupplementDataParseHandler handlerSuppl;

    static NumberingSystemsParseHandler handlerNumbering;

    static MetaZonesParseHandler handlerMetaZones;

    private static BundleGenerator bundleGenerator;

    static int draftType;

    private static final String DRAFT_UNCONFIRMED = "unconfirmed";

    private static final String DRAFT_PROVISIONAL = "provisional";

    private static final String DRAFT_CONTRIBUTED = "contributed";

    private static final String DRAFT_APPROVED = "approved";

    private static final String DRAFT_TRUE = "true";

    private static final String DRAFT_FALSE = "false";

    private static final String DRAFT_DEFAULT = DRAFT_APPROVED;

    static final Map<String, Integer> DRAFT_MAP = new HashMap<>();

    static {
        DRAFT_MAP.put(DRAFT_UNCONFIRMED, 0);
        DRAFT_MAP.put(DRAFT_PROVISIONAL, 1);
        DRAFT_MAP.put(DRAFT_CONTRIBUTED, 2);
        DRAFT_MAP.put(DRAFT_APPROVED, 3);
        DRAFT_MAP.put(DRAFT_TRUE, 0);
        DRAFT_MAP.put(DRAFT_FALSE, 2);
        draftType = DRAFT_MAP.get(DRAFT_DEFAULT);
    }

    static boolean USE_UTF8 = false;

    private static boolean verbose;

    private CLDRConverter() {
    }

    @SuppressWarnings("AssignmentToForLoopParameter")
    public static void main(String[] args) throws Exception {
        if (args.length != 0) {
            String currentArg = null;
            try {
                for (int i = 0; i < args.length; i++) {
                    currentArg = args[i];
                    switch(currentArg) {
                        case "-draft":
                            String draftDataType = args[++i];
                            try {
                                draftType = DRAFT_MAP.get(draftDataType);
                            } catch (NullPointerException e) {
                                severe("Error: incorrect draft value: %s%n", draftDataType);
                                System.exit(1);
                            }
                            info("Using the specified data type: %s%n", draftDataType);
                            break;
                        case "-base":
                            CLDR_BASE = args[++i];
                            if (!CLDR_BASE.endsWith("/")) {
                                CLDR_BASE += "/";
                            }
                            break;
                        case "-o":
                            DESTINATION_DIR = args[++i];
                            break;
                        case "-utf8":
                            USE_UTF8 = true;
                            break;
                        case "-verbose":
                            verbose = true;
                            break;
                        case "-help":
                            usage();
                            System.exit(0);
                            break;
                        default:
                            throw new RuntimeException();
                    }
                }
            } catch (RuntimeException e) {
                severe("unknown or imcomplete arg(s): " + currentArg);
                usage();
                System.exit(1);
            }
        }
        LOCAL_LDML_DTD = CLDR_BASE + "common/dtd/ldml.dtd";
        LOCAL_SPPL_LDML_DTD = CLDR_BASE + "common/dtd/ldmlSupplemental.dtd";
        SOURCE_FILE_DIR = CLDR_BASE + "common/main";
        SPPL_SOURCE_FILE = CLDR_BASE + "common/supplemental/supplementalData.xml";
        NUMBERING_SOURCE_FILE = CLDR_BASE + "common/supplemental/numberingSystems.xml";
        METAZONES_SOURCE_FILE = CLDR_BASE + "common/supplemental/metaZones.xml";
        bundleGenerator = new ResourceBundleGenerator();
        List<Bundle> bundles = readBundleList();
        convertBundles(bundles);
    }

    private static void usage() {
        errout("Usage: java CLDRConverter [options]%n" + "\t-help          output this usage message and exit%n" + "\t-verbose       output information%n" + "\t-draft [approved | provisional | unconfirmed]%n" + "\t\t       draft level for using data (default: approved)%n" + "\t-base dir      base directory for CLDR input files%n" + "\t-o dir         output directory (defaut: ./build/gensrc)%n" + "\t-utf8          use UTF-8 rather than \\uxxxx (for debug)%n");
    }

    static void info(String fmt, Object... args) {
        if (verbose) {
            System.out.printf(fmt, args);
        }
    }

    static void info(String msg) {
        if (verbose) {
            System.out.println(msg);
        }
    }

    static void warning(String fmt, Object... args) {
        System.err.print("Warning: ");
        System.err.printf(fmt, args);
    }

    static void warning(String msg) {
        System.err.print("Warning: ");
        errout(msg);
    }

    static void severe(String fmt, Object... args) {
        System.err.print("Error: ");
        System.err.printf(fmt, args);
    }

    static void severe(String msg) {
        System.err.print("Error: ");
        errout(msg);
    }

    private static void errout(String msg) {
        if (msg.contains("%n")) {
            System.err.printf(msg);
        } else {
            System.err.println(msg);
        }
    }

    private static List<Bundle> readBundleList() throws Exception {
        ResourceBundle.Control defCon = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT);
        List<Bundle> retList = new ArrayList<>();
        Path path = FileSystems.getDefault().getPath(SOURCE_FILE_DIR);
        try (DirectoryStream<Path> dirStr = Files.newDirectoryStream(path)) {
            for (Path entry : dirStr) {
                String fileName = entry.getFileName().toString();
                if (fileName.endsWith(".xml")) {
                    String id = fileName.substring(0, fileName.indexOf('.'));
                    Locale cldrLoc = Locale.forLanguageTag(toLanguageTag(id));
                    List<Locale> candList = defCon.getCandidateLocales("", cldrLoc);
                    StringBuilder sb = new StringBuilder();
                    for (Locale loc : candList) {
                        if (!loc.equals(Locale.ROOT)) {
                            sb.append(toLocaleName(loc.toLanguageTag()));
                            sb.append(",");
                        }
                    }
                    if (sb.indexOf("root") == -1) {
                        sb.append("root");
                    }
                    retList.add(new Bundle(id, sb.toString(), null, null));
                }
            }
        }
        return retList;
    }

    private static Map<String, Map<String, Object>> cldrBundles = new HashMap<>();

    static Map<String, Object> getCLDRBundle(String id) throws Exception {
        Map<String, Object> bundle = cldrBundles.get(id);
        if (bundle != null) {
            return bundle;
        }
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);
        SAXParser parser = factory.newSAXParser();
        LDMLParseHandler handler = new LDMLParseHandler(id);
        File file = new File(SOURCE_FILE_DIR + File.separator + id + ".xml");
        if (!file.exists()) {
            return Collections.emptyMap();
        }
        info("..... main directory .....");
        info("Reading file " + file);
        parser.parse(file, handler);
        bundle = handler.getData();
        cldrBundles.put(id, bundle);
        String country = getCountryCode(id);
        if (country != null) {
            bundle = handlerSuppl.getData(country);
            if (bundle != null) {
                Map<String, Object> temp = cldrBundles.remove(id);
                bundle.putAll(temp);
                cldrBundles.put(id, bundle);
            }
        }
        return bundle;
    }

    private static void convertBundles(List<Bundle> bundles) throws Exception {
        SAXParserFactory factorySuppl = SAXParserFactory.newInstance();
        factorySuppl.setValidating(true);
        SAXParser parserSuppl = factorySuppl.newSAXParser();
        handlerSuppl = new SupplementDataParseHandler();
        File fileSupply = new File(SPPL_SOURCE_FILE);
        parserSuppl.parse(fileSupply, handlerSuppl);
        SAXParserFactory numberingParser = SAXParserFactory.newInstance();
        numberingParser.setValidating(true);
        SAXParser parserNumbering = numberingParser.newSAXParser();
        handlerNumbering = new NumberingSystemsParseHandler();
        File fileNumbering = new File(NUMBERING_SOURCE_FILE);
        parserNumbering.parse(fileNumbering, handlerNumbering);
        SAXParserFactory metazonesParser = SAXParserFactory.newInstance();
        metazonesParser.setValidating(true);
        SAXParser parserMetaZones = metazonesParser.newSAXParser();
        handlerMetaZones = new MetaZonesParseHandler();
        File fileMetaZones = new File(METAZONES_SOURCE_FILE);
        parserNumbering.parse(fileMetaZones, handlerMetaZones);
        Map<String, SortedSet<String>> metaInfo = new HashMap<>();
        metaInfo.put("LocaleNames", new TreeSet<String>());
        metaInfo.put("CurrencyNames", new TreeSet<String>());
        metaInfo.put("CalendarData", new TreeSet<String>());
        metaInfo.put("FormatData", new TreeSet<String>());
        for (Bundle bundle : bundles) {
            Map<String, Object> targetMap = bundle.getTargetMap();
            EnumSet<Bundle.Type> bundleTypes = bundle.getBundleTypes();
            if (bundle.isRoot()) {
                Map<String, Object> enData = new HashMap<>();
                enData.putAll(Bundle.getBundle("en").getTargetMap());
                enData.putAll(Bundle.getBundle("en_US").getTargetMap());
                for (String key : enData.keySet()) {
                    if (!targetMap.containsKey(key)) {
                        targetMap.put(key, enData.get(key));
                    }
                }
                targetMap.put("DateTimePatternChars", "GyMdkHmsSEDFwWahKzZ");
            }
            if (bundleTypes.contains(Bundle.Type.LOCALENAMES)) {
                Map<String, Object> localeNamesMap = extractLocaleNames(targetMap, bundle.getID());
                if (!localeNamesMap.isEmpty() || bundle.isRoot()) {
                    metaInfo.get("LocaleNames").add(toLanguageTag(bundle.getID()));
                    bundleGenerator.generateBundle("util", "LocaleNames", bundle.getID(), true, localeNamesMap, true);
                }
            }
            if (bundleTypes.contains(Bundle.Type.CURRENCYNAMES)) {
                Map<String, Object> currencyNamesMap = extractCurrencyNames(targetMap, bundle.getID(), bundle.getCurrencies());
                if (!currencyNamesMap.isEmpty() || bundle.isRoot()) {
                    metaInfo.get("CurrencyNames").add(toLanguageTag(bundle.getID()));
                    bundleGenerator.generateBundle("util", "CurrencyNames", bundle.getID(), true, currencyNamesMap, true);
                }
            }
            if (bundleTypes.contains(Bundle.Type.TIMEZONENAMES)) {
                Map<String, Object> zoneNamesMap = extractZoneNames(targetMap, bundle.getID());
            }
            if (bundleTypes.contains(Bundle.Type.CALENDARDATA)) {
                Map<String, Object> calendarDataMap = extractCalendarData(targetMap, bundle.getID());
                if (!calendarDataMap.isEmpty() || bundle.isRoot()) {
                    metaInfo.get("CalendarData").add(toLanguageTag(bundle.getID()));
                    bundleGenerator.generateBundle("util", "CalendarData", bundle.getID(), true, calendarDataMap, false);
                }
            }
            if (bundleTypes.contains(Bundle.Type.FORMATDATA)) {
                Map<String, Object> formatDataMap = extractFormatData(targetMap, bundle.getID());
                if (!formatDataMap.isEmpty() || bundle.isRoot()) {
                    metaInfo.get("FormatData").add(toLanguageTag(bundle.getID()));
                    bundleGenerator.generateBundle("text", "FormatData", bundle.getID(), true, formatDataMap, false);
                }
            }
            SortedSet<String> allLocales = new TreeSet<>();
            allLocales.addAll(metaInfo.get("CurrencyNames"));
            allLocales.addAll(metaInfo.get("LocaleNames"));
            allLocales.addAll(metaInfo.get("CalendarData"));
            allLocales.addAll(metaInfo.get("FormatData"));
            metaInfo.put("All", allLocales);
        }
        bundleGenerator.generateMetaInfo(metaInfo);
    }

    static String getLanguageCode(String id) {
        int index = id.indexOf('_');
        String lang = null;
        if (index != -1) {
            lang = id.substring(0, index);
        } else {
            lang = "root".equals(id) ? "" : id;
        }
        return lang;
    }

    private static String getCountryCode(String id) {
        if (id.indexOf('@') != -1) {
            id = id.substring(0, id.indexOf('@'));
        }
        String[] tokens = id.split("_");
        for (int index = 1; index < tokens.length; ++index) {
            if (tokens[index].length() == 2 && Character.isLetter(tokens[index].charAt(0)) && Character.isLetter(tokens[index].charAt(1))) {
                return tokens[index];
            }
        }
        return null;
    }

    private static class KeyComparator implements Comparator<String> {

        static KeyComparator INSTANCE = new KeyComparator();

        private KeyComparator() {
        }

        public int compare(String o1, String o2) {
            int len1 = o1.length();
            int len2 = o2.length();
            if (!isDigit(o1.charAt(0)) && !isDigit(o2.charAt(0))) {
                if (len1 < len2) {
                    return -1;
                }
                if (len1 > len2) {
                    return 1;
                }
            }
            return o1.compareTo(o2);
        }

        private boolean isDigit(char c) {
            return c >= '0' && c <= '9';
        }
    }

    private static Map<String, Object> extractLocaleNames(Map<String, Object> map, String id) {
        Map<String, Object> localeNames = new TreeMap<>(KeyComparator.INSTANCE);
        for (String key : map.keySet()) {
            if (key.startsWith(LOCALE_NAME_PREFIX)) {
                localeNames.put(key.substring(LOCALE_NAME_PREFIX.length()), map.get(key));
            }
        }
        return localeNames;
    }

    @SuppressWarnings("AssignmentToForLoopParameter")
    private static Map<String, Object> extractCurrencyNames(Map<String, Object> map, String id, String names) throws Exception {
        Map<String, Object> currencyNames = new TreeMap<>(KeyComparator.INSTANCE);
        for (String key : map.keySet()) {
            if (key.startsWith(CURRENCY_NAME_PREFIX)) {
                currencyNames.put(key.substring(CURRENCY_NAME_PREFIX.length()), map.get(key));
            } else if (key.startsWith(CURRENCY_SYMBOL_PREFIX)) {
                currencyNames.put(key.substring(CURRENCY_SYMBOL_PREFIX.length()), map.get(key));
            }
        }
        return currencyNames;
    }

    private static Map<String, Object> extractZoneNames(Map<String, Object> map, String id) {
        return null;
    }

    private static Map<String, Object> extractCalendarData(Map<String, Object> map, String id) {
        Map<String, Object> calendarData = new LinkedHashMap<>();
        copyIfPresent(map, "firstDayOfWeek", calendarData);
        copyIfPresent(map, "minimalDaysInFirstWeek", calendarData);
        return calendarData;
    }

    private static Map<String, Object> extractFormatData(Map<String, Object> map, String id) {
        Map<String, Object> formatData = new LinkedHashMap<>();
        for (CalendarType calendarType : CalendarType.values()) {
            String prefix = calendarType.keyElementName();
            copyIfPresent(map, prefix + "MonthNames", formatData);
            copyIfPresent(map, prefix + "standalone.MonthNames", formatData);
            copyIfPresent(map, prefix + "MonthAbbreviations", formatData);
            copyIfPresent(map, prefix + "standalone.MonthAbbreviations", formatData);
            copyIfPresent(map, prefix + "DayNames", formatData);
            copyIfPresent(map, prefix + "DayAbbreviations", formatData);
            copyIfPresent(map, prefix + "AmPmMarkers", formatData);
            copyIfPresent(map, prefix + "Eras", formatData);
            copyIfPresent(map, prefix + "short.Eras", formatData);
            copyIfPresent(map, prefix + "TimePatterns", formatData);
            copyIfPresent(map, prefix + "DatePatterns", formatData);
            copyIfPresent(map, prefix + "DateTimePatterns", formatData);
            copyIfPresent(map, prefix + "DateTimePatternChars", formatData);
        }
        copyIfPresent(map, "DefaultNumberingSystem", formatData);
        String defaultScript = (String) map.get("DefaultNumberingSystem");
        @SuppressWarnings("unchecked")
        List<String> numberingScripts = (List<String>) map.remove("numberingScripts");
        if (numberingScripts != null) {
            for (String script : numberingScripts) {
                copyIfPresent(map, script + "." + "NumberElements", formatData);
            }
        } else {
            copyIfPresent(map, "NumberElements", formatData);
        }
        copyIfPresent(map, "NumberPatterns", formatData);
        return formatData;
    }

    private static void copyIfPresent(Map<String, Object> src, String key, Map<String, Object> dest) {
        Object value = src.get(key);
        if (value != null) {
            dest.put(key, value);
        }
    }

    private static final String specialSaveCharsJava = "\"";

    private static final String specialSaveCharsProperties = "=: \t\r\n\f#!";

    static String saveConvert(String theString, boolean useJava) {
        if (theString == null) {
            return "";
        }
        String specialSaveChars;
        if (useJava) {
            specialSaveChars = specialSaveCharsJava;
        } else {
            specialSaveChars = specialSaveCharsProperties;
        }
        boolean escapeSpace = false;
        int len = theString.length();
        StringBuilder outBuffer = new StringBuilder(len * 2);
        Formatter formatter = new Formatter(outBuffer, Locale.ROOT);
        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            switch(aChar) {
                case ' ':
                    if (x == 0 || escapeSpace) {
                        outBuffer.append('\\');
                    }
                    outBuffer.append(' ');
                    break;
                case '\\':
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    break;
                case '\t':
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;
                case '\n':
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;
                case '\r':
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;
                case '\f':
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;
                default:
                    if (!USE_UTF8 && ((aChar < 0x0020) || (aChar > 0x007e))) {
                        formatter.format("\\u%04x", (int) aChar);
                    } else {
                        if (specialSaveChars.indexOf(aChar) != -1) {
                            outBuffer.append('\\');
                        }
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    private static String toLanguageTag(String locName) {
        if (locName.indexOf('_') == -1) {
            return locName;
        }
        String tag = locName.replaceAll("_", "-");
        Locale loc = Locale.forLanguageTag(tag);
        return loc.toLanguageTag();
    }

    private static String toLocaleName(String tag) {
        if (tag.indexOf('-') == -1) {
            return tag;
        }
        return tag.replaceAll("-", "_");
    }
}
