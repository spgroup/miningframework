package de.danoeh.antennapod.core.util;

import android.content.Context;
import android.util.Log;
import org.apache.commons.lang3.StringUtils;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

    private static final String TAG = "DateUtils";

    private static final TimeZone defaultTimezone = TimeZone.getTimeZone("GMT");

    public static Date parse(final String input) {
        if (input == null) {
            throw new IllegalArgumentException("Date must not be null");
        }
        String date = input.trim().replace('/', '-').replaceAll("( ){2,}+", " ");
        if (date.contains(".")) {
            int start = date.indexOf('.');
            int current = start + 1;
            while (current < date.length() && Character.isDigit(date.charAt(current))) {
                current++;
            }
            if (current - start > 4) {
                if (current < date.length() - 1) {
                    date = date.substring(0, start + 4) + date.substring(current);
                } else {
                    date = date.substring(0, start + 4);
                }
            } else if (current - start < 4) {
                if (current < date.length() - 1) {
                    date = date.substring(0, current) + StringUtils.repeat("0", 4 - (current - start)) + date.substring(current);
                } else {
                    date = date.substring(0, current) + StringUtils.repeat("0", 4 - (current - start));
                }
            }
        }
        String[] patterns = { "dd MMM yy HH:mm:ss Z", "dd MMM yy HH:mm Z", "EEE, dd MMM yyyy HH:mm:ss Z", "EEE, dd MMM yyyy HH:mm:ss", "EEE, dd MMMM yyyy HH:mm:ss Z", "EEE, dd MMMM yyyy HH:mm:ss", "EEEE, dd MMM yyyy HH:mm:ss Z", "EEEE, dd MMM yy HH:mm:ss Z", "EEEE, dd MMM yyyy HH:mm:ss", "EEEE, dd MMM yy HH:mm:ss", "EEE MMM d HH:mm:ss yyyy", "EEE, dd MMM yyyy HH:mm Z", "EEE, dd MMM yyyy HH:mm", "EEE, dd MMMM yyyy HH:mm Z", "EEE, dd MMMM yyyy HH:mm", "EEEE, dd MMM yyyy HH:mm Z", "EEEE, dd MMM yy HH:mm Z", "EEEE, dd MMM yyyy HH:mm", "EEEE, dd MMM yy HH:mm", "EEE MMM d HH:mm yyyy", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS Z", "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-ddZ", "yyyy-MM-dd" };
        SimpleDateFormat parser = new SimpleDateFormat("", Locale.US);
        parser.setLenient(false);
        parser.setTimeZone(defaultTimezone);
        ParsePosition pos = new ParsePosition(0);
        for (String pattern : patterns) {
            parser.applyPattern(pattern);
            pos.setIndex(0);
            try {
                Date result = parser.parse(date, pos);
                if (result != null && pos.getIndex() == date.length()) {
                    return result;
                }
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        Log.d(TAG, "Could not parse date string \"" + input + "\" [" + date + "]");
        return null;
    }

    public static long parseTimeString(final String time) {
        String[] parts = time.split(":");
        long result = 0;
        int idx = 0;
        if (parts.length == 3) {
            result += Integer.parseInt(parts[idx]) * 3600000L;
            idx++;
        }
        if (parts.length >= 2) {
            result += Integer.parseInt(parts[idx]) * 60000L;
            idx++;
            result += (Float.parseFloat(parts[idx])) * 1000L;
        }
        return result;
    }

    public static String formatRFC822Date(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd MMM yy HH:mm:ss Z", Locale.US);
        return format.format(date);
    }

    public static String formatRFC3339Local(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        return format.format(date);
    }

    public static String formatRFC3339UTC(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        format.setTimeZone(defaultTimezone);
        return format.format(date);
    }

    public static String formatAbbrev(final Context context, final Date date) {
        if (date == null) {
            return "";
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(GregorianCalendar.YEAR, -1);
        cal.add(GregorianCalendar.DAY_OF_MONTH, 10);
        boolean withinLastYear = date.after(cal.getTime());
        int format = android.text.format.DateUtils.FORMAT_ABBREV_ALL;
        if (withinLastYear) {
            format |= android.text.format.DateUtils.FORMAT_NO_YEAR;
        }
        return android.text.format.DateUtils.formatDateTime(context, date.getTime(), format);
    }
}
