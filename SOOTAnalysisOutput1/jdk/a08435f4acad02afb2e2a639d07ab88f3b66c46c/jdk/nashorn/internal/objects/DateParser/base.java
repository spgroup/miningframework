package jdk.nashorn.internal.objects;

import static java.lang.Character.DECIMAL_DIGIT_NUMBER;
import static java.lang.Character.LOWERCASE_LETTER;
import static java.lang.Character.OTHER_PUNCTUATION;
import static java.lang.Character.SPACE_SEPARATOR;
import static java.lang.Character.UPPERCASE_LETTER;
import java.util.HashMap;

public class DateParser {

    public final static int YEAR = 0;

    public final static int MONTH = 1;

    public final static int DAY = 2;

    public final static int HOUR = 3;

    public final static int MINUTE = 4;

    public final static int SECOND = 5;

    public final static int MILLISECOND = 6;

    public final static int TIMEZONE = 7;

    private enum Token {

        UNKNOWN,
        NUMBER,
        SEPARATOR,
        PARENTHESIS,
        NAME,
        SIGN,
        END
    }

    private final String string;

    private final int length;

    private final Integer[] fields;

    private int pos = 0;

    private Token token;

    private int tokenLength;

    private Name nameValue;

    private int numValue;

    private int currentField = YEAR;

    private int yearSign = 0;

    private boolean namedMonth = false;

    private final static HashMap<String, Name> names = new HashMap<>();

    static {
        addName("monday", Name.DAY_OF_WEEK, 0);
        addName("tuesday", Name.DAY_OF_WEEK, 0);
        addName("wednesday", Name.DAY_OF_WEEK, 0);
        addName("thursday", Name.DAY_OF_WEEK, 0);
        addName("friday", Name.DAY_OF_WEEK, 0);
        addName("saturday", Name.DAY_OF_WEEK, 0);
        addName("sunday", Name.DAY_OF_WEEK, 0);
        addName("january", Name.MONTH_NAME, 1);
        addName("february", Name.MONTH_NAME, 2);
        addName("march", Name.MONTH_NAME, 3);
        addName("april", Name.MONTH_NAME, 4);
        addName("may", Name.MONTH_NAME, 5);
        addName("june", Name.MONTH_NAME, 6);
        addName("july", Name.MONTH_NAME, 7);
        addName("august", Name.MONTH_NAME, 8);
        addName("september", Name.MONTH_NAME, 9);
        addName("october", Name.MONTH_NAME, 10);
        addName("november", Name.MONTH_NAME, 11);
        addName("december", Name.MONTH_NAME, 12);
        addName("am", Name.AM_PM, 0);
        addName("pm", Name.AM_PM, 12);
        addName("z", Name.TIMEZONE_ID, 0);
        addName("gmt", Name.TIMEZONE_ID, 0);
        addName("ut", Name.TIMEZONE_ID, 0);
        addName("utc", Name.TIMEZONE_ID, 0);
        addName("est", Name.TIMEZONE_ID, -5 * 60);
        addName("edt", Name.TIMEZONE_ID, -4 * 60);
        addName("cst", Name.TIMEZONE_ID, -6 * 60);
        addName("cdt", Name.TIMEZONE_ID, -5 * 60);
        addName("mst", Name.TIMEZONE_ID, -7 * 60);
        addName("mdt", Name.TIMEZONE_ID, -6 * 60);
        addName("pst", Name.TIMEZONE_ID, -8 * 60);
        addName("pdt", Name.TIMEZONE_ID, -7 * 60);
        addName("t", Name.TIME_SEPARATOR, 0);
    }

    public DateParser(final String string) {
        this.string = string;
        this.length = string.length();
        this.fields = new Integer[TIMEZONE + 1];
    }

    public boolean parse() {
        return parseEcmaDate() || parseLegacyDate();
    }

    public boolean parseEcmaDate() {
        if (token == null) {
            token = next();
        }
        while (token != Token.END) {
            switch(token) {
                case NUMBER:
                    if (currentField == YEAR && yearSign != 0) {
                        if (tokenLength != 6) {
                            return false;
                        }
                        numValue *= yearSign;
                    } else if (!checkEcmaField(currentField, numValue)) {
                        return false;
                    }
                    if (!skipEcmaDelimiter()) {
                        return false;
                    }
                    if (currentField < TIMEZONE) {
                        set(currentField++, numValue);
                    }
                    break;
                case NAME:
                    if (nameValue == null) {
                        return false;
                    }
                    switch(nameValue.type) {
                        case Name.TIME_SEPARATOR:
                            if (currentField == YEAR || currentField > HOUR) {
                                return false;
                            }
                            currentField = HOUR;
                            break;
                        case Name.TIMEZONE_ID:
                            if (!nameValue.key.equals("z") || !setTimezone(nameValue.value, false)) {
                                return false;
                            }
                            break;
                        default:
                            return false;
                    }
                    break;
                case SIGN:
                    if (currentField == YEAR) {
                        yearSign = numValue;
                    } else if (currentField < SECOND || !setTimezone(readTimeZoneOffset(), true)) {
                        return false;
                    }
                    break;
                default:
                    return false;
            }
            token = next();
        }
        return patchResult(true);
    }

    public boolean parseLegacyDate() {
        if (yearSign != 0 || currentField > DAY) {
            return false;
        }
        if (token == null) {
            token = next();
        }
        while (token != Token.END) {
            switch(token) {
                case NUMBER:
                    if (skip(':')) {
                        if (!setTimeField(numValue)) {
                            return false;
                        }
                        do {
                            token = next();
                            if (token != Token.NUMBER || !setTimeField(numValue)) {
                                return false;
                            }
                        } while (skip(isSet(SECOND) ? '.' : ':'));
                    } else {
                        if (!setDateField(numValue)) {
                            return false;
                        }
                        skip('-');
                    }
                    break;
                case NAME:
                    if (nameValue == null) {
                        return false;
                    }
                    switch(nameValue.type) {
                        case Name.AM_PM:
                            if (!setAmPm(nameValue.value)) {
                                return false;
                            }
                            break;
                        case Name.MONTH_NAME:
                            if (!setMonth(nameValue.value)) {
                                return false;
                            }
                            break;
                        case Name.TIMEZONE_ID:
                            if (!setTimezone(nameValue.value, false)) {
                                return false;
                            }
                            break;
                        case Name.TIME_SEPARATOR:
                            return false;
                        default:
                            break;
                    }
                    if (nameValue.type != Name.TIMEZONE_ID) {
                        skip('-');
                    }
                    break;
                case SIGN:
                    if (!setTimezone(readTimeZoneOffset(), true)) {
                        return false;
                    }
                    break;
                case PARENTHESIS:
                    if (!skipParentheses()) {
                        return false;
                    }
                    break;
                case SEPARATOR:
                    break;
                default:
                    return false;
            }
            token = next();
        }
        return patchResult(false);
    }

    public Integer[] getDateFields() {
        return fields;
    }

    private boolean isSet(final int field) {
        return fields[field] != null;
    }

    private Integer get(final int field) {
        return fields[field];
    }

    private void set(final int field, final int value) {
        fields[field] = value;
    }

    private int peek() {
        return pos < length ? string.charAt(pos) : -1;
    }

    private boolean skip(final char c) {
        if (pos < length && string.charAt(pos) == c) {
            token = null;
            pos++;
            return true;
        }
        return false;
    }

    private Token next() {
        if (pos >= length) {
            tokenLength = 0;
            return Token.END;
        }
        final char c = string.charAt(pos);
        if (c > 0x80) {
            tokenLength = 1;
            pos++;
            return Token.UNKNOWN;
        }
        final int type = Character.getType(c);
        switch(type) {
            case DECIMAL_DIGIT_NUMBER:
                numValue = readNumber(6);
                return Token.NUMBER;
            case SPACE_SEPARATOR:
            case OTHER_PUNCTUATION:
                tokenLength = 1;
                pos++;
                return Token.SEPARATOR;
            case UPPERCASE_LETTER:
            case LOWERCASE_LETTER:
                nameValue = readName();
                return Token.NAME;
            default:
                tokenLength = 1;
                pos++;
                switch(c) {
                    case '(':
                        return Token.PARENTHESIS;
                    case '-':
                    case '+':
                        numValue = c == '-' ? -1 : 1;
                        return Token.SIGN;
                    default:
                        return Token.UNKNOWN;
                }
        }
    }

    private static boolean checkLegacyField(final int field, final int value) {
        switch(field) {
            case HOUR:
                return isHour(value);
            case MINUTE:
            case SECOND:
                return isMinuteOrSecond(value);
            case MILLISECOND:
                return isMillisecond(value);
            default:
                return true;
        }
    }

    private boolean checkEcmaField(final int field, final int value) {
        switch(field) {
            case YEAR:
                return tokenLength == 4;
            case MONTH:
                return tokenLength == 2 && isMonth(value);
            case DAY:
                return tokenLength == 2 && isDay(value);
            case HOUR:
                return tokenLength == 2 && isHour(value);
            case MINUTE:
            case SECOND:
                return tokenLength == 2 && isMinuteOrSecond(value);
            case MILLISECOND:
                return tokenLength < 4 && isMillisecond(value);
            default:
                return true;
        }
    }

    private boolean skipEcmaDelimiter() {
        switch(currentField) {
            case YEAR:
            case MONTH:
                return skip('-') || peek() == 'T' || peek() == -1;
            case DAY:
                return peek() == 'T' || peek() == -1;
            case HOUR:
            case MINUTE:
                return skip(':') || endOfTime();
            case SECOND:
                return skip('.') || endOfTime();
            default:
                return true;
        }
    }

    private boolean endOfTime() {
        final int c = peek();
        return c == -1 || c == 'Z' || c == '-' || c == '+' || c == ' ';
    }

    private static boolean isAsciiLetter(final char ch) {
        return ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z');
    }

    private static boolean isAsciiDigit(final char ch) {
        return '0' <= ch && ch <= '9';
    }

    private int readNumber(final int maxDigits) {
        final int start = pos;
        int n = 0;
        final int max = Math.min(length, pos + maxDigits);
        while (pos < max && isAsciiDigit(string.charAt(pos))) {
            n = n * 10 + string.charAt(pos++) - '0';
        }
        tokenLength = pos - start;
        return n;
    }

    private Name readName() {
        final int start = pos;
        final int limit = Math.min(pos + 3, length);
        while (pos < limit && isAsciiLetter(string.charAt(pos))) {
            pos++;
        }
        final String key = string.substring(start, pos).toLowerCase();
        final Name name = names.get(key);
        while (pos < length && isAsciiLetter(string.charAt(pos))) {
            pos++;
        }
        tokenLength = pos - start;
        if (name != null && name.matches(string, start, tokenLength)) {
            return name;
        }
        return null;
    }

    private int readTimeZoneOffset() {
        final int sign = string.charAt(pos - 1) == '+' ? 1 : -1;
        int offset = readNumber(2);
        skip(':');
        offset = offset * 60 + readNumber(2);
        return sign * offset;
    }

    private boolean skipParentheses() {
        int parenCount = 1;
        while (pos < length && parenCount != 0) {
            final char c = string.charAt(pos++);
            if (c == '(') {
                parenCount++;
            } else if (c == ')') {
                parenCount--;
            }
        }
        return true;
    }

    private static int getDefaultValue(final int field) {
        switch(field) {
            case MONTH:
            case DAY:
                return 1;
            default:
                return 0;
        }
    }

    private static boolean isDay(final int n) {
        return 1 <= n && n <= 31;
    }

    private static boolean isMonth(final int n) {
        return 1 <= n && n <= 12;
    }

    private static boolean isHour(final int n) {
        return 0 <= n && n <= 24;
    }

    private static boolean isMinuteOrSecond(final int n) {
        return 0 <= n && n < 60;
    }

    private static boolean isMillisecond(final int n) {
        return 0 <= n && n < 1000;
    }

    private boolean setMonth(final int m) {
        if (!isSet(MONTH)) {
            namedMonth = true;
            set(MONTH, m);
            return true;
        }
        return false;
    }

    private boolean setDateField(final int n) {
        for (int field = YEAR; field != HOUR; field++) {
            if (!isSet(field)) {
                set(field, n);
                return true;
            }
        }
        return false;
    }

    private boolean setTimeField(final int n) {
        for (int field = HOUR; field != TIMEZONE; field++) {
            if (!isSet(field)) {
                if (checkLegacyField(field, n)) {
                    set(field, n);
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    private boolean setTimezone(final int offset, final boolean asNumericOffset) {
        if (!isSet(TIMEZONE) || (asNumericOffset && get(TIMEZONE) == 0)) {
            set(TIMEZONE, offset);
            return true;
        }
        return false;
    }

    private boolean setAmPm(final int offset) {
        if (!isSet(HOUR)) {
            return false;
        }
        final int hour = get(HOUR);
        if (hour >= 0 && hour <= 12) {
            set(HOUR, hour + offset);
        }
        return true;
    }

    private boolean patchResult(final boolean strict) {
        if (!isSet(YEAR) && !isSet(HOUR)) {
            return false;
        }
        if (isSet(HOUR) && !isSet(MINUTE)) {
            return false;
        }
        for (int field = YEAR; field <= TIMEZONE; field++) {
            if (get(field) == null) {
                if (field == TIMEZONE && !strict) {
                    continue;
                }
                final int value = getDefaultValue(field);
                set(field, value);
            }
        }
        if (!strict) {
            if (isDay(get(YEAR))) {
                final int d = get(YEAR);
                set(YEAR, get(DAY));
                if (namedMonth) {
                    set(DAY, d);
                } else {
                    final int d2 = get(MONTH);
                    set(MONTH, d);
                    set(DAY, d2);
                }
            }
            if (!isMonth(get(MONTH)) || !isDay(get(DAY))) {
                return false;
            }
            final int year = get(YEAR);
            if (year >= 0 && year < 100) {
                set(YEAR, year >= 50 ? 1900 + year : 2000 + year);
            }
        } else {
            if (get(HOUR) == 24 && (get(MINUTE) != 0 || get(SECOND) != 0 || get(MILLISECOND) != 0)) {
                return false;
            }
        }
        set(MONTH, get(MONTH) - 1);
        return true;
    }

    private static void addName(final String str, final int type, final int value) {
        final Name name = new Name(str, type, value);
        names.put(name.key, name);
    }

    private static class Name {

        final String name;

        final String key;

        final int value;

        final int type;

        final static int DAY_OF_WEEK = -1;

        final static int MONTH_NAME = 0;

        final static int AM_PM = 1;

        final static int TIMEZONE_ID = 2;

        final static int TIME_SEPARATOR = 3;

        Name(final String name, final int type, final int value) {
            assert name != null;
            assert name.equals(name.toLowerCase());
            this.name = name;
            this.key = name.substring(0, Math.min(3, name.length()));
            this.type = type;
            this.value = value;
        }

        public boolean matches(final String str, final int offset, final int len) {
            return name.regionMatches(true, 0, str, offset, len);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
