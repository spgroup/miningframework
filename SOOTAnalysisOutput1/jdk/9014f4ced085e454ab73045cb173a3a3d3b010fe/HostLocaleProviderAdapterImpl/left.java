package sun.util.locale.provider;

import java.lang.ref.SoftReference;
import java.text.*;
import java.text.spi.DateFormatProvider;
import java.text.spi.DateFormatSymbolsProvider;
import java.text.spi.DecimalFormatSymbolsProvider;
import java.text.spi.NumberFormatProvider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle.Control;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.spi.CalendarDataProvider;
import java.util.spi.CurrencyNameProvider;
import java.util.spi.LocaleNameProvider;
import java.util.spi.TimeZoneNameProvider;

public class HostLocaleProviderAdapterImpl {

    private static ConcurrentMap<Locale, SoftReference<AtomicReferenceArray<String>>> dateFormatPatternsMap = new ConcurrentHashMap<>(2);

    private static ConcurrentMap<Locale, SoftReference<AtomicReferenceArray<String>>> numberFormatPatternsMap = new ConcurrentHashMap<>(2);

    private static ConcurrentMap<Locale, SoftReference<DateFormatSymbols>> dateFormatSymbolsMap = new ConcurrentHashMap<>(2);

    private static ConcurrentMap<Locale, SoftReference<DecimalFormatSymbols>> decimalFormatSymbolsMap = new ConcurrentHashMap<>(2);

    private static final int CAT_DISPLAY = 0;

    private static final int CAT_FORMAT = 1;

    private static final int NF_NUMBER = 0;

    private static final int NF_CURRENCY = 1;

    private static final int NF_PERCENT = 2;

    private static final int NF_INTEGER = 3;

    private static final int NF_MAX = NF_INTEGER;

    private static final int CD_FIRSTDAYOFWEEK = 0;

    private static final int CD_MINIMALDAYSINFIRSTWEEK = 1;

    private static final int DN_LOCALE_LANGUAGE = 0;

    private static final int DN_LOCALE_SCRIPT = 1;

    private static final int DN_LOCALE_REGION = 2;

    private static final int DN_LOCALE_VARIANT = 3;

    private static final int DN_CURRENCY_CODE = 4;

    private static final int DN_CURRENCY_SYMBOL = 5;

    private static final int DN_TZ_SHORT_STANDARD = 0;

    private static final int DN_TZ_SHORT_DST = 1;

    private static final int DN_TZ_LONG_STANDARD = 2;

    private static final int DN_TZ_LONG_DST = 3;

    private static final Set<Locale> supportedLocaleSet;

    static {
        Set<Locale> tmpSet = new HashSet<Locale>();
        Locale l = Locale.forLanguageTag(getDefaultLocale(CAT_FORMAT).replaceAll("_", "-"));
        tmpSet.addAll(Control.getNoFallbackControl(Control.FORMAT_DEFAULT).getCandidateLocales("", l));
        l = Locale.forLanguageTag(getDefaultLocale(CAT_DISPLAY).replaceAll("_", "-"));
        tmpSet.addAll(Control.getNoFallbackControl(Control.FORMAT_DEFAULT).getCandidateLocales("", l));
        supportedLocaleSet = Collections.unmodifiableSet(tmpSet);
    }

    private final static Locale[] supportedLocale = supportedLocaleSet.toArray(new Locale[0]);

    public static DateFormatProvider getDateFormatProvider() {
        return new DateFormatProvider() {

            @Override
            public Locale[] getAvailableLocales() {
                return getSupportedCalendarLocales();
            }

            @Override
            public boolean isSupportedLocale(Locale locale) {
                return isSupportedCalendarLocale(locale);
            }

            @Override
            public DateFormat getDateInstance(int style, Locale locale) {
                return new SimpleDateFormat(getDateTimePattern(style, -1, locale), getCalendarLocale(locale));
            }

            @Override
            public DateFormat getTimeInstance(int style, Locale locale) {
                return new SimpleDateFormat(getDateTimePattern(-1, style, locale), getCalendarLocale(locale));
            }

            @Override
            public DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale locale) {
                return new SimpleDateFormat(getDateTimePattern(dateStyle, timeStyle, locale), getCalendarLocale(locale));
            }

            private String getDateTimePattern(int dateStyle, int timeStyle, Locale locale) {
                AtomicReferenceArray<String> dateFormatPatterns;
                SoftReference<AtomicReferenceArray<String>> ref = dateFormatPatternsMap.get(locale);
                if (ref == null || (dateFormatPatterns = ref.get()) == null) {
                    dateFormatPatterns = new AtomicReferenceArray<>(5 * 5);
                    ref = new SoftReference<>(dateFormatPatterns);
                    dateFormatPatternsMap.put(locale, ref);
                }
                int index = (dateStyle + 1) * 5 + timeStyle + 1;
                String pattern = dateFormatPatterns.get(index);
                if (pattern == null) {
                    String langTag = locale.toLanguageTag();
                    pattern = translateDateFormatLetters(getCalendarID(langTag), getDateTimePatternNative(dateStyle, timeStyle, langTag));
                    if (!dateFormatPatterns.compareAndSet(index, null, pattern)) {
                        pattern = dateFormatPatterns.get(index);
                    }
                }
                return pattern;
            }
        };
    }

    public static DateFormatSymbolsProvider getDateFormatSymbolsProvider() {
        return new DateFormatSymbolsProvider() {

            @Override
            public Locale[] getAvailableLocales() {
                if (isSupportedLocale(Locale.getDefault(Locale.Category.FORMAT))) {
                    return supportedLocale;
                }
                return new Locale[0];
            }

            @Override
            public boolean isSupportedLocale(Locale locale) {
                Locale base = locale.stripExtensions();
                if (supportedLocaleSet.contains(base)) {
                    return getCalendarID(locale.toLanguageTag()).equals("gregorian");
                }
                return false;
            }

            @Override
            public DateFormatSymbols getInstance(Locale locale) {
                DateFormatSymbols dateFormatSymbols;
                SoftReference<DateFormatSymbols> ref = dateFormatSymbolsMap.get(locale);
                if (ref == null || (dateFormatSymbols = ref.get()) == null) {
                    dateFormatSymbols = new DateFormatSymbols(locale);
                    String langTag = locale.toLanguageTag();
                    dateFormatSymbols.setAmPmStrings(getAmPmStrings(langTag, dateFormatSymbols.getAmPmStrings()));
                    dateFormatSymbols.setEras(getEras(langTag, dateFormatSymbols.getEras()));
                    dateFormatSymbols.setMonths(getMonths(langTag, dateFormatSymbols.getMonths()));
                    dateFormatSymbols.setShortMonths(getShortMonths(langTag, dateFormatSymbols.getShortMonths()));
                    dateFormatSymbols.setWeekdays(getWeekdays(langTag, dateFormatSymbols.getWeekdays()));
                    dateFormatSymbols.setShortWeekdays(getShortWeekdays(langTag, dateFormatSymbols.getShortWeekdays()));
                    ref = new SoftReference<>(dateFormatSymbols);
                    dateFormatSymbolsMap.put(locale, ref);
                }
                return (DateFormatSymbols) dateFormatSymbols.clone();
            }
        };
    }

    public static NumberFormatProvider getNumberFormatProvider() {
        return new NumberFormatProvider() {

            @Override
            public Locale[] getAvailableLocales() {
                return supportedLocale;
            }

            @Override
            public boolean isSupportedLocale(Locale locale) {
                return supportedLocaleSet.contains(locale.stripExtensions());
            }

            @Override
            public NumberFormat getCurrencyInstance(Locale locale) {
                return new DecimalFormat(getNumberPattern(NF_CURRENCY, locale), DecimalFormatSymbols.getInstance(locale));
            }

            @Override
            public NumberFormat getIntegerInstance(Locale locale) {
                return new DecimalFormat(getNumberPattern(NF_INTEGER, locale), DecimalFormatSymbols.getInstance(locale));
            }

            @Override
            public NumberFormat getNumberInstance(Locale locale) {
                return new DecimalFormat(getNumberPattern(NF_NUMBER, locale), DecimalFormatSymbols.getInstance(locale));
            }

            @Override
            public NumberFormat getPercentInstance(Locale locale) {
                return new DecimalFormat(getNumberPattern(NF_PERCENT, locale), DecimalFormatSymbols.getInstance(locale));
            }

            private String getNumberPattern(int style, Locale locale) {
                AtomicReferenceArray<String> numberFormatPatterns;
                SoftReference<AtomicReferenceArray<String>> ref = numberFormatPatternsMap.get(locale);
                if (ref == null || (numberFormatPatterns = ref.get()) == null) {
                    numberFormatPatterns = new AtomicReferenceArray<>(4);
                    ref = new SoftReference<>(numberFormatPatterns);
                    numberFormatPatternsMap.put(locale, ref);
                }
                String pattern = numberFormatPatterns.get(style);
                if (pattern == null) {
                    pattern = getNumberPatternNative(style, locale.toLanguageTag());
                    if (!numberFormatPatterns.compareAndSet(style, null, pattern)) {
                        pattern = numberFormatPatterns.get(style);
                    }
                }
                return pattern;
            }
        };
    }

    public static DecimalFormatSymbolsProvider getDecimalFormatSymbolsProvider() {
        return new DecimalFormatSymbolsProvider() {

            @Override
            public Locale[] getAvailableLocales() {
                return supportedLocale;
            }

            @Override
            public boolean isSupportedLocale(Locale locale) {
                return supportedLocaleSet.contains(locale.stripExtensions());
            }

            @Override
            public DecimalFormatSymbols getInstance(Locale locale) {
                DecimalFormatSymbols decimalFormatSymbols;
                SoftReference<DecimalFormatSymbols> ref = decimalFormatSymbolsMap.get(locale);
                if (ref == null || (decimalFormatSymbols = ref.get()) == null) {
                    decimalFormatSymbols = new DecimalFormatSymbols(locale);
                    String langTag = locale.toLanguageTag();
                    decimalFormatSymbols.setInternationalCurrencySymbol(getInternationalCurrencySymbol(langTag, decimalFormatSymbols.getInternationalCurrencySymbol()));
                    decimalFormatSymbols.setCurrencySymbol(getCurrencySymbol(langTag, decimalFormatSymbols.getCurrencySymbol()));
                    decimalFormatSymbols.setDecimalSeparator(getDecimalSeparator(langTag, decimalFormatSymbols.getDecimalSeparator()));
                    decimalFormatSymbols.setGroupingSeparator(getGroupingSeparator(langTag, decimalFormatSymbols.getGroupingSeparator()));
                    decimalFormatSymbols.setInfinity(getInfinity(langTag, decimalFormatSymbols.getInfinity()));
                    decimalFormatSymbols.setMinusSign(getMinusSign(langTag, decimalFormatSymbols.getMinusSign()));
                    decimalFormatSymbols.setMonetaryDecimalSeparator(getMonetaryDecimalSeparator(langTag, decimalFormatSymbols.getMonetaryDecimalSeparator()));
                    decimalFormatSymbols.setNaN(getNaN(langTag, decimalFormatSymbols.getNaN()));
                    decimalFormatSymbols.setPercent(getPercent(langTag, decimalFormatSymbols.getPercent()));
                    decimalFormatSymbols.setPerMill(getPerMill(langTag, decimalFormatSymbols.getPerMill()));
                    decimalFormatSymbols.setZeroDigit(getZeroDigit(langTag, decimalFormatSymbols.getZeroDigit()));
                    decimalFormatSymbols.setExponentSeparator(getExponentSeparator(langTag, decimalFormatSymbols.getExponentSeparator()));
                    ref = new SoftReference<>(decimalFormatSymbols);
                    decimalFormatSymbolsMap.put(locale, ref);
                }
                return (DecimalFormatSymbols) decimalFormatSymbols.clone();
            }
        };
    }

    public static CalendarDataProvider getCalendarDataProvider() {
        return new CalendarDataProvider() {

            @Override
            public Locale[] getAvailableLocales() {
                return getSupportedCalendarLocales();
            }

            @Override
            public boolean isSupportedLocale(Locale locale) {
                return isSupportedCalendarLocale(locale);
            }

            @Override
            public String getDisplayName(String calType, int field, int value, int style, Locale locale) {
                return null;
            }

            @Override
            public Map<String, Integer> getDisplayNames(String calType, int field, int style, Locale locale) {
                return null;
            }

            @Override
            public int getFirstDayOfWeek(Locale locale) {
                return getCalendarInt(locale.toLanguageTag(), CD_FIRSTDAYOFWEEK);
            }

            @Override
            public int getMinimalDaysInFirstWeek(Locale locale) {
                return getCalendarInt(locale.toLanguageTag(), CD_MINIMALDAYSINFIRSTWEEK);
            }
        };
    }

    public static CurrencyNameProvider getCurrencyNameProvider() {
        return new CurrencyNameProvider() {

            @Override
            public Locale[] getAvailableLocales() {
                return supportedLocale;
            }

            @Override
            public boolean isSupportedLocale(Locale locale) {
                return supportedLocaleSet.contains(locale.stripExtensions());
            }

            @Override
            public String getDisplayName(String code, Locale locale) {
                return getDisplayString(locale.toLanguageTag(), DN_CURRENCY_CODE, code);
            }

            @Override
            public String getSymbol(String code, Locale locale) {
                return getDisplayString(locale.toLanguageTag(), DN_CURRENCY_SYMBOL, code);
            }
        };
    }

    public static LocaleNameProvider getLocaleNameProvider() {
        return new LocaleNameProvider() {

            @Override
            public Locale[] getAvailableLocales() {
                return supportedLocale;
            }

            @Override
            public boolean isSupportedLocale(Locale locale) {
                return supportedLocaleSet.contains(locale.stripExtensions());
            }

            @Override
            public String getDisplayLanguage(String languageCode, Locale locale) {
                return getDisplayString(locale.toLanguageTag(), DN_LOCALE_LANGUAGE, languageCode);
            }

            @Override
            public String getDisplayCountry(String countryCode, Locale locale) {
                return getDisplayString(locale.toLanguageTag(), DN_LOCALE_REGION, countryCode);
            }

            @Override
            public String getDisplayScript(String scriptCode, Locale locale) {
                return getDisplayString(locale.toLanguageTag(), DN_LOCALE_SCRIPT, scriptCode);
            }

            @Override
            public String getDisplayVariant(String variantCode, Locale locale) {
                return getDisplayString(locale.toLanguageTag(), DN_LOCALE_VARIANT, variantCode);
            }
        };
    }

    public static TimeZoneNameProvider getTimeZoneNameProvider() {
        return new TimeZoneNameProvider() {

            @Override
            public Locale[] getAvailableLocales() {
                return supportedLocale;
            }

            @Override
            public boolean isSupportedLocale(Locale locale) {
                return supportedLocaleSet.contains(locale.stripExtensions());
            }

            @Override
            public String getDisplayName(String ID, boolean daylight, int style, Locale locale) {
                return getTimeZoneDisplayString(locale.toLanguageTag(), style * 2 + (daylight ? 1 : 0), ID);
            }
        };
    }

    private static Locale[] getSupportedCalendarLocales() {
        if (supportedLocale.length != 0 && supportedLocaleSet.contains(Locale.JAPAN) && isJapaneseCalendar()) {
            Locale[] sup = new Locale[supportedLocale.length + 1];
            sup[0] = JRELocaleConstants.JA_JP_JP;
            System.arraycopy(supportedLocale, 0, sup, 1, supportedLocale.length);
            return sup;
        }
        return supportedLocale;
    }

    private static boolean isSupportedCalendarLocale(Locale locale) {
        if (JRELocaleConstants.JA_JP_JP.equals(locale)) {
            return isJapaneseCalendar();
        }
        Locale base = locale.stripExtensions();
        if (!supportedLocaleSet.contains(base)) {
            return false;
        }
        String caltype = locale.getUnicodeLocaleType("ca");
        if (caltype == null) {
            return true;
        }
        return caltype.replaceFirst("gregory", "gregorian").equals(getCalendarID(locale.toLanguageTag()));
    }

    private static boolean isJapaneseCalendar() {
        return getCalendarID("ja-JP").equals("japanese");
    }

    private static Locale getCalendarLocale(Locale locale) {
        Locale.Builder lb = new Locale.Builder().setLocale(locale);
        String calid = getCalendarID(locale.toLanguageTag());
        switch(calid) {
            case "gregorian":
                calid = "gregory";
            case "japanese":
            case "buddhist":
                lb.setUnicodeLocaleKeyword("ca", calid);
                return lb.build();
            default:
                return locale;
        }
    }

    private static String translateDateFormatLetters(String calendarType, String cldrFormat) {
        String pattern = cldrFormat;
        int length = pattern.length();
        boolean inQuote = false;
        StringBuilder jrePattern = new StringBuilder(length);
        int count = 0;
        char lastLetter = 0;
        for (int i = 0; i < length; i++) {
            char c = pattern.charAt(i);
            if (c == '\'') {
                if ((i + 1) < length) {
                    char nextc = pattern.charAt(i + 1);
                    if (nextc == '\'') {
                        i++;
                        if (count != 0) {
                            convert(calendarType, lastLetter, count, jrePattern);
                            lastLetter = 0;
                            count = 0;
                        }
                        jrePattern.append("''");
                        continue;
                    }
                }
                if (!inQuote) {
                    if (count != 0) {
                        convert(calendarType, lastLetter, count, jrePattern);
                        lastLetter = 0;
                        count = 0;
                    }
                    inQuote = true;
                } else {
                    inQuote = false;
                }
                jrePattern.append(c);
                continue;
            }
            if (inQuote) {
                jrePattern.append(c);
                continue;
            }
            if (!(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z')) {
                if (count != 0) {
                    convert(calendarType, lastLetter, count, jrePattern);
                    lastLetter = 0;
                    count = 0;
                }
                jrePattern.append(c);
                continue;
            }
            if (lastLetter == 0 || lastLetter == c) {
                lastLetter = c;
                count++;
                continue;
            }
            convert(calendarType, lastLetter, count, jrePattern);
            lastLetter = c;
            count = 1;
        }
        if (count != 0) {
            convert(calendarType, lastLetter, count, jrePattern);
        }
        if (cldrFormat.contentEquals(jrePattern)) {
            return cldrFormat;
        }
        return jrePattern.toString();
    }

    private static void convert(String calendarType, char cldrLetter, int count, StringBuilder sb) {
        switch(cldrLetter) {
            case 'G':
                if (!calendarType.equals("gregorian")) {
                    if (count == 5) {
                        count = 1;
                    } else if (count == 1) {
                        count = 4;
                    }
                }
                appendN(cldrLetter, count, sb);
                break;
            case 'c':
            case 'e':
                switch(count) {
                    case 1:
                        sb.append('u');
                        break;
                    case 3:
                    case 4:
                        appendN('E', count, sb);
                        break;
                    case 5:
                        appendN('E', 3, sb);
                        break;
                }
                break;
            case 'v':
            case 'V':
                appendN('z', count, sb);
                break;
            case 'Z':
                if (count == 4 || count == 5) {
                    sb.append("XXX");
                }
                break;
            case 'u':
            case 'U':
            case 'q':
            case 'Q':
            case 'l':
            case 'g':
            case 'j':
            case 'A':
                sb.append('\'');
                sb.append(cldrLetter);
                sb.append('\'');
                break;
            default:
                appendN(cldrLetter, count, sb);
                break;
        }
    }

    private static void appendN(char c, int n, StringBuilder sb) {
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
    }

    private static native String getDefaultLocale(int cat);

    private static native String getDateTimePatternNative(int dateStyle, int timeStyle, String langtag);

    private static native String getCalendarID(String langTag);

    private static native String getNumberPatternNative(int style, String langtag);

    private static native String[] getAmPmStrings(String langTag, String[] ampm);

    private static native String[] getEras(String langTag, String[] eras);

    private static native String[] getMonths(String langTag, String[] months);

    private static native String[] getShortMonths(String langTag, String[] smonths);

    private static native String[] getWeekdays(String langTag, String[] wdays);

    private static native String[] getShortWeekdays(String langTag, String[] swdays);

    private static native String getCurrencySymbol(String langTag, String currencySymbol);

    private static native char getDecimalSeparator(String langTag, char decimalSeparator);

    private static native char getGroupingSeparator(String langTag, char groupingSeparator);

    private static native String getInfinity(String langTag, String infinity);

    private static native String getInternationalCurrencySymbol(String langTag, String internationalCurrencySymbol);

    private static native char getMinusSign(String langTag, char minusSign);

    private static native char getMonetaryDecimalSeparator(String langTag, char monetaryDecimalSeparator);

    private static native String getNaN(String langTag, String nan);

    private static native char getPercent(String langTag, char percent);

    private static native char getPerMill(String langTag, char perMill);

    private static native char getZeroDigit(String langTag, char zeroDigit);

    private static native String getExponentSeparator(String langTag, String exponent);

    private static native int getCalendarInt(String langTag, int type);

    private static native String getDisplayString(String langTag, int key, String value);

    private static native String getTimeZoneDisplayString(String langTag, int style, String value);
}
