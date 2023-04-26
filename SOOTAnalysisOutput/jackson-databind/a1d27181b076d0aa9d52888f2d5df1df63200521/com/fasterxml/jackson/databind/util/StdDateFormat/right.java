package com.fasterxml.jackson.databind.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import com.fasterxml.jackson.core.io.NumberInput;

@SuppressWarnings("serial")
public class StdDateFormat extends DateFormat {

    public final static String DATE_FORMAT_STR_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    protected final static String DATE_FORMAT_STR_ISO8601_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    protected final static String DATE_FORMAT_STR_ISO8601_NO_TZ = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    protected final static String DATE_FORMAT_STR_PLAIN = "yyyy-MM-dd";

    protected final static String DATE_FORMAT_STR_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";

    protected final static String[] ALL_FORMATS = new String[] { DATE_FORMAT_STR_ISO8601, DATE_FORMAT_STR_ISO8601_Z, DATE_FORMAT_STR_ISO8601_NO_TZ, DATE_FORMAT_STR_RFC1123, DATE_FORMAT_STR_PLAIN };

    private final static TimeZone DEFAULT_TIMEZONE;

    static {
        DEFAULT_TIMEZONE = TimeZone.getTimeZone("UTC");
    }

    private final static Locale DEFAULT_LOCALE = Locale.US;

    protected final static DateFormat DATE_FORMAT_RFC1123;

    protected final static DateFormat DATE_FORMAT_ISO8601;

    protected final static DateFormat DATE_FORMAT_ISO8601_Z;

    protected final static DateFormat DATE_FORMAT_ISO8601_NO_TZ;

    protected final static DateFormat DATE_FORMAT_PLAIN;

    static {
        DATE_FORMAT_RFC1123 = new SimpleDateFormat(DATE_FORMAT_STR_RFC1123, DEFAULT_LOCALE);
        DATE_FORMAT_RFC1123.setTimeZone(DEFAULT_TIMEZONE);
        DATE_FORMAT_ISO8601 = new SimpleDateFormat(DATE_FORMAT_STR_ISO8601, DEFAULT_LOCALE);
        DATE_FORMAT_ISO8601.setTimeZone(DEFAULT_TIMEZONE);
        DATE_FORMAT_ISO8601_Z = new SimpleDateFormat(DATE_FORMAT_STR_ISO8601_Z, DEFAULT_LOCALE);
        DATE_FORMAT_ISO8601_Z.setTimeZone(DEFAULT_TIMEZONE);
        DATE_FORMAT_ISO8601_NO_TZ = new SimpleDateFormat(DATE_FORMAT_STR_ISO8601_NO_TZ, DEFAULT_LOCALE);
        DATE_FORMAT_ISO8601_NO_TZ.setTimeZone(DEFAULT_TIMEZONE);
        DATE_FORMAT_PLAIN = new SimpleDateFormat(DATE_FORMAT_STR_PLAIN, DEFAULT_LOCALE);
        DATE_FORMAT_PLAIN.setTimeZone(DEFAULT_TIMEZONE);
    }

    public final static StdDateFormat instance = new StdDateFormat();

    protected transient TimeZone _timezone;

    protected final Locale _locale;

    protected Boolean _lenient;

    protected transient DateFormat _formatRFC1123;

    protected transient DateFormat _formatISO8601;

    protected transient DateFormat _formatISO8601_z;

    protected transient DateFormat _formatISO8601_noTz;

    protected transient DateFormat _formatPlain;

    public StdDateFormat() {
        _locale = DEFAULT_LOCALE;
    }

    @Deprecated
    public StdDateFormat(TimeZone tz, Locale loc) {
        _timezone = tz;
        _locale = loc;
    }

    protected StdDateFormat(TimeZone tz, Locale loc, Boolean lenient) {
        _timezone = tz;
        _locale = loc;
        _lenient = lenient;
    }

    public static TimeZone getDefaultTimeZone() {
        return DEFAULT_TIMEZONE;
    }

    public StdDateFormat withTimeZone(TimeZone tz) {
        if (tz == null) {
            tz = DEFAULT_TIMEZONE;
        }
        if ((tz == _timezone) || tz.equals(_timezone)) {
            return this;
        }
        return new StdDateFormat(tz, _locale, _lenient);
    }

    public StdDateFormat withLocale(Locale loc) {
        if (loc.equals(_locale)) {
            return this;
        }
        return new StdDateFormat(_timezone, loc, _lenient);
    }

    @Override
    public StdDateFormat clone() {
        return new StdDateFormat(_timezone, _locale, _lenient);
    }

    @Deprecated
    public static DateFormat getISO8601Format(TimeZone tz) {
        return getISO8601Format(tz, DEFAULT_LOCALE);
    }

    public static DateFormat getISO8601Format(TimeZone tz, Locale loc) {
        return _cloneFormat(DATE_FORMAT_ISO8601, DATE_FORMAT_STR_ISO8601, tz, loc, null);
    }

    public static DateFormat getRFC1123Format(TimeZone tz, Locale loc) {
        return _cloneFormat(DATE_FORMAT_RFC1123, DATE_FORMAT_STR_RFC1123, tz, loc, null);
    }

    @Deprecated
    public static DateFormat getRFC1123Format(TimeZone tz) {
        return getRFC1123Format(tz, DEFAULT_LOCALE);
    }

    @Override
    public TimeZone getTimeZone() {
        return _timezone;
    }

    @Override
    public void setTimeZone(TimeZone tz) {
        if (!tz.equals(_timezone)) {
            _clearFormats();
            _timezone = tz;
        }
    }

    @Override
    public void setLenient(boolean enabled) {
        Boolean newValue = enabled;
        if (_lenient != newValue) {
            _lenient = newValue;
            _clearFormats();
        }
    }

    @Override
    public boolean isLenient() {
        if (_lenient == null) {
            return true;
        }
        return _lenient.booleanValue();
    }

    @Override
    public Date parse(String dateStr) throws ParseException {
        dateStr = dateStr.trim();
        ParsePosition pos = new ParsePosition(0);
        Date dt;
        if (looksLikeISO8601(dateStr)) {
            dt = parseAsISO8601(dateStr, pos, true);
        } else {
            int i = dateStr.length();
            while (--i >= 0) {
                char ch = dateStr.charAt(i);
                if (ch < '0' || ch > '9') {
                    if (i > 0 || ch != '-') {
                        break;
                    }
                }
            }
            if ((i < 0) && (dateStr.charAt(0) == '-' || NumberInput.inLongRange(dateStr, false))) {
                dt = new Date(Long.parseLong(dateStr));
            } else {
                dt = parseAsRFC1123(dateStr, pos);
            }
        }
        if (dt != null) {
            return dt;
        }
        StringBuilder sb = new StringBuilder();
        for (String f : ALL_FORMATS) {
            if (sb.length() > 0) {
                sb.append("\", \"");
            } else {
                sb.append('"');
            }
            sb.append(f);
        }
        sb.append('"');
        throw new ParseException(String.format("Can not parse date \"%s\": not compatible with any of standard forms (%s)", dateStr, sb.toString()), pos.getErrorIndex());
    }

    @Override
    public Date parse(String dateStr, ParsePosition pos) {
        if (looksLikeISO8601(dateStr)) {
            try {
                return parseAsISO8601(dateStr, pos, false);
            } catch (ParseException e) {
                return null;
            }
        }
        int i = dateStr.length();
        while (--i >= 0) {
            char ch = dateStr.charAt(i);
            if (ch < '0' || ch > '9') {
                if (i > 0 || ch != '-') {
                    break;
                }
            }
        }
        if (i < 0) {
            if (dateStr.charAt(0) == '-' || NumberInput.inLongRange(dateStr, false)) {
                return new Date(Long.parseLong(dateStr));
            }
        }
        return parseAsRFC1123(dateStr, pos);
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        if (_formatISO8601 == null) {
            _formatISO8601 = _cloneFormat(DATE_FORMAT_ISO8601, DATE_FORMAT_STR_ISO8601, _timezone, _locale, _lenient);
        }
        return _formatISO8601.format(date, toAppendTo, fieldPosition);
    }

    @Override
    public String toString() {
        String str = "DateFormat " + getClass().getName();
        TimeZone tz = _timezone;
        if (tz != null) {
            str += " (timezone: " + tz + ")";
        }
        str += "(locale: " + _locale + ")";
        return str;
    }

    @Override
    public boolean equals(Object o) {
        return (o == this);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    protected boolean looksLikeISO8601(String dateStr) {
        if (dateStr.length() >= 5 && Character.isDigit(dateStr.charAt(0)) && Character.isDigit(dateStr.charAt(3)) && dateStr.charAt(4) == '-') {
            return true;
        }
        return false;
    }

    protected Date parseAsISO8601(String dateStr, ParsePosition pos, boolean throwErrors) throws ParseException {
        int len = dateStr.length();
        char c = dateStr.charAt(len - 1);
        DateFormat df;
        String formatStr;
        if (len <= 10 && Character.isDigit(c)) {
            df = _formatPlain;
            formatStr = DATE_FORMAT_STR_PLAIN;
            if (df == null) {
                df = _formatPlain = _cloneFormat(DATE_FORMAT_PLAIN, formatStr, _timezone, _locale, _lenient);
            }
        } else if (c == 'Z') {
            df = _formatISO8601_z;
            formatStr = DATE_FORMAT_STR_ISO8601_Z;
            if (df == null) {
                df = _formatISO8601_z = _cloneFormat(DATE_FORMAT_ISO8601_Z, formatStr, DEFAULT_TIMEZONE, _locale, _lenient);
            }
            if (dateStr.charAt(len - 4) == ':') {
                StringBuilder sb = new StringBuilder(dateStr);
                sb.insert(len - 1, ".000");
                dateStr = sb.toString();
            }
        } else {
            if (hasTimeZone(dateStr)) {
                c = dateStr.charAt(len - 3);
                if (c == ':') {
                    StringBuilder sb = new StringBuilder(dateStr);
                    sb.delete(len - 3, len - 2);
                    dateStr = sb.toString();
                } else if (c == '+' || c == '-') {
                    dateStr += "00";
                }
                len = dateStr.length();
                int timeLen = len - dateStr.lastIndexOf('T') - 6;
                if (timeLen < 12) {
                    int offset = len - 5;
                    StringBuilder sb = new StringBuilder(dateStr);
                    switch(timeLen) {
                        case 11:
                            sb.insert(offset, '0');
                            break;
                        case 10:
                            sb.insert(offset, "00");
                            break;
                        case 9:
                            sb.insert(offset, "000");
                            break;
                        case 8:
                            sb.insert(offset, ".000");
                            break;
                        case 7:
                            break;
                        case 6:
                            sb.insert(offset, "00.000");
                        case 5:
                            sb.insert(offset, ":00.000");
                    }
                    dateStr = sb.toString();
                }
                df = _formatISO8601;
                formatStr = DATE_FORMAT_STR_ISO8601;
                if (_formatISO8601 == null) {
                    df = _formatISO8601 = _cloneFormat(DATE_FORMAT_ISO8601, formatStr, _timezone, _locale, _lenient);
                }
            } else {
                int timeLen = len - dateStr.lastIndexOf('T') - 1;
                if (timeLen < 12) {
                    StringBuilder sb = new StringBuilder(dateStr);
                    switch(timeLen) {
                        case 11:
                            sb.append('0');
                        case 10:
                            sb.append('0');
                        case 9:
                            sb.append('0');
                            break;
                        default:
                            sb.append(".000");
                    }
                    dateStr = sb.toString();
                }
                df = _formatISO8601_noTz;
                formatStr = DATE_FORMAT_STR_ISO8601_NO_TZ;
                if (df == null) {
                    df = _formatISO8601_noTz = _cloneFormat(DATE_FORMAT_ISO8601_NO_TZ, formatStr, _timezone, _locale, _lenient);
                }
            }
        }
        Date dt = df.parse(dateStr, pos);
        if (dt == null) {
            throw new ParseException(String.format("Can not parse date \"%s\": while it seems to fit format '%s', parsing fails (leniency? %s)", dateStr, formatStr, _lenient), pos.getErrorIndex());
        }
        return dt;
    }

    protected Date parseAsRFC1123(String dateStr, ParsePosition pos) {
        if (_formatRFC1123 == null) {
            _formatRFC1123 = _cloneFormat(DATE_FORMAT_RFC1123, DATE_FORMAT_STR_RFC1123, _timezone, _locale, _lenient);
        }
        return _formatRFC1123.parse(dateStr, pos);
    }

    private final static boolean hasTimeZone(String str) {
        int len = str.length();
        if (len >= 6) {
            char c = str.charAt(len - 6);
            if (c == '+' || c == '-')
                return true;
            c = str.charAt(len - 5);
            if (c == '+' || c == '-')
                return true;
            c = str.charAt(len - 3);
            if (c == '+' || c == '-')
                return true;
        }
        return false;
    }

    private final static DateFormat _cloneFormat(DateFormat df, String format, TimeZone tz, Locale loc, Boolean lenient) {
        if (!loc.equals(DEFAULT_LOCALE)) {
            df = new SimpleDateFormat(format, loc);
            df.setTimeZone((tz == null) ? DEFAULT_TIMEZONE : tz);
        } else {
            df = (DateFormat) df.clone();
            if (tz != null) {
                df.setTimeZone(tz);
            }
        }
        if (lenient != null) {
            df.setLenient(lenient.booleanValue());
        }
        return df;
    }

    protected void _clearFormats() {
        _formatRFC1123 = null;
        _formatISO8601 = null;
        _formatISO8601_z = null;
        _formatISO8601_noTz = null;
        _formatPlain = null;
    }
}
