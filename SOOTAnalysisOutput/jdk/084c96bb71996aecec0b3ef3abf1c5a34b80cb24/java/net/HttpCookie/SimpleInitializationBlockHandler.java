package java.net;

import java.util.List;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public final class HttpCookie implements Cloneable {

    private final String name;

    private String value;

    private String comment;

    private String commentURL;

    private boolean toDiscard;

    private String domain;

    private long maxAge = MAX_AGE_UNSPECIFIED;

    private String path;

    private String portlist;

    private boolean secure;

    private boolean httpOnly;

    private int version = 1;

    private final String header;

    private final long whenCreated;

    private final static long MAX_AGE_UNSPECIFIED = -1;

    private final static String[] COOKIE_DATE_FORMATS = { "EEE',' dd-MMM-yyyy HH:mm:ss 'GMT'", "EEE',' dd MMM yyyy HH:mm:ss 'GMT'", "EEE MMM dd yyyy HH:mm:ss 'GMT'Z", "EEE',' dd-MMM-yy HH:mm:ss 'GMT'", "EEE',' dd MMM yy HH:mm:ss 'GMT'", "EEE MMM dd yy HH:mm:ss 'GMT'Z" };

    private final static String SET_COOKIE = "set-cookie:";

    private final static String SET_COOKIE2 = "set-cookie2:";

    public HttpCookie(String name, String value) {
        this(name, value, null);
    }

    private HttpCookie(String name, String value, String header) {
        name = name.trim();
        if (name.length() == 0 || !isToken(name) || name.charAt(0) == '$') {
            throw new IllegalArgumentException("Illegal cookie name");
        }
        this.name = name;
        this.value = value;
        toDiscard = false;
        secure = false;
        whenCreated = System.currentTimeMillis();
        portlist = null;
        this.header = header;
    }

    public static List<HttpCookie> parse(String header) {
        return parse(header, false);
    }

    private static List<HttpCookie> parse(String header, boolean retainHeader) {
        int version = guessCookieVersion(header);
        if (startsWithIgnoreCase(header, SET_COOKIE2)) {
            header = header.substring(SET_COOKIE2.length());
        } else if (startsWithIgnoreCase(header, SET_COOKIE)) {
            header = header.substring(SET_COOKIE.length());
        }
        List<HttpCookie> cookies = new java.util.ArrayList<>();
        if (version == 0) {
            HttpCookie cookie = parseInternal(header, retainHeader);
            cookie.setVersion(0);
            cookies.add(cookie);
        } else {
            List<String> cookieStrings = splitMultiCookies(header);
            for (String cookieStr : cookieStrings) {
                HttpCookie cookie = parseInternal(cookieStr, retainHeader);
                cookie.setVersion(1);
                cookies.add(cookie);
            }
        }
        return cookies;
    }

    public boolean hasExpired() {
        if (maxAge == 0)
            return true;
        if (maxAge == MAX_AGE_UNSPECIFIED)
            return false;
        long deltaSecond = (System.currentTimeMillis() - whenCreated) / 1000;
        if (deltaSecond > maxAge)
            return true;
        else
            return false;
    }

    public void setComment(String purpose) {
        comment = purpose;
    }

    public String getComment() {
        return comment;
    }

    public void setCommentURL(String purpose) {
        commentURL = purpose;
    }

    public String getCommentURL() {
        return commentURL;
    }

    public void setDiscard(boolean discard) {
        toDiscard = discard;
    }

    public boolean getDiscard() {
        return toDiscard;
    }

    public void setPortlist(String ports) {
        portlist = ports;
    }

    public String getPortlist() {
        return portlist;
    }

    public void setDomain(String pattern) {
        if (pattern != null)
            domain = pattern.toLowerCase();
        else
            domain = pattern;
    }

    public String getDomain() {
        return domain;
    }

    public void setMaxAge(long expiry) {
        maxAge = expiry;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setPath(String uri) {
        path = uri;
    }

    public String getPath() {
        return path;
    }

    public void setSecure(boolean flag) {
        secure = flag;
    }

    public boolean getSecure() {
        return secure;
    }

    public String getName() {
        return name;
    }

    public void setValue(String newValue) {
        value = newValue;
    }

    public String getValue() {
        return value;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int v) {
        if (v != 0 && v != 1) {
            throw new IllegalArgumentException("cookie version should be 0 or 1");
        }
        version = v;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public static boolean domainMatches(String domain, String host) {
        if (domain == null || host == null)
            return false;
        boolean isLocalDomain = ".local".equalsIgnoreCase(domain);
        int embeddedDotInDomain = domain.indexOf('.');
        if (embeddedDotInDomain == 0)
            embeddedDotInDomain = domain.indexOf('.', 1);
        if (!isLocalDomain && (embeddedDotInDomain == -1 || embeddedDotInDomain == domain.length() - 1))
            return false;
        int firstDotInHost = host.indexOf('.');
        if (firstDotInHost == -1 && (isLocalDomain || domain.equalsIgnoreCase(host + ".local"))) {
            return true;
        }
        int domainLength = domain.length();
        int lengthDiff = host.length() - domainLength;
        if (lengthDiff == 0) {
            return host.equalsIgnoreCase(domain);
        } else if (lengthDiff > 0) {
            String H = host.substring(0, lengthDiff);
            String D = host.substring(lengthDiff);
            return (H.indexOf('.') == -1 && D.equalsIgnoreCase(domain));
        } else if (lengthDiff == -1) {
            return (domain.charAt(0) == '.' && host.equalsIgnoreCase(domain.substring(1)));
        }
        return false;
    }

    @Override
    public String toString() {
        if (getVersion() > 0) {
            return toRFC2965HeaderString();
        } else {
            return toNetscapeHeaderString();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof HttpCookie))
            return false;
        HttpCookie other = (HttpCookie) obj;
        return equalsIgnoreCase(getName(), other.getName()) && equalsIgnoreCase(getDomain(), other.getDomain()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public int hashCode() {
        int h1 = name.toLowerCase().hashCode();
        int h2 = (domain != null) ? domain.toLowerCase().hashCode() : 0;
        int h3 = (path != null) ? path.hashCode() : 0;
        return h1 + h2 + h3;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static final String tspecials = ",;";

    private static boolean isToken(String value) {
        int len = value.length();
        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);
            if (c < 0x20 || c >= 0x7f || tspecials.indexOf(c) != -1)
                return false;
        }
        return true;
    }

    private static HttpCookie parseInternal(String header, boolean retainHeader) {
        HttpCookie cookie = null;
        String namevaluePair = null;
        StringTokenizer tokenizer = new StringTokenizer(header, ";");
        try {
            namevaluePair = tokenizer.nextToken();
            int index = namevaluePair.indexOf('=');
            if (index != -1) {
                String name = namevaluePair.substring(0, index).trim();
                String value = namevaluePair.substring(index + 1).trim();
                if (retainHeader)
                    cookie = new HttpCookie(name, stripOffSurroundingQuote(value), header);
                else
                    cookie = new HttpCookie(name, stripOffSurroundingQuote(value));
            } else {
                throw new IllegalArgumentException("Invalid cookie name-value pair");
            }
        } catch (NoSuchElementException ignored) {
            throw new IllegalArgumentException("Empty cookie header string");
        }
        while (tokenizer.hasMoreTokens()) {
            namevaluePair = tokenizer.nextToken();
            int index = namevaluePair.indexOf('=');
            String name, value;
            if (index != -1) {
                name = namevaluePair.substring(0, index).trim();
                value = namevaluePair.substring(index + 1).trim();
            } else {
                name = namevaluePair.trim();
                value = null;
            }
            assignAttribute(cookie, name, value);
        }
        return cookie;
    }

    static interface CookieAttributeAssignor {

        public void assign(HttpCookie cookie, String attrName, String attrValue);
    }

    static final java.util.Map<String, CookieAttributeAssignor> assignors = new java.util.HashMap<>();

    static {
        assignors.put("comment", new CookieAttributeAssignor() {

            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                if (cookie.getComment() == null)
                    cookie.setComment(attrValue);
            }
        });
        assignors.put("commenturl", new CookieAttributeAssignor() {

            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                if (cookie.getCommentURL() == null)
                    cookie.setCommentURL(attrValue);
            }
        });
        assignors.put("discard", new CookieAttributeAssignor() {

            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                cookie.setDiscard(true);
            }
        });
        assignors.put("domain", new CookieAttributeAssignor() {

            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                if (cookie.getDomain() == null)
                    cookie.setDomain(attrValue);
            }
        });
        assignors.put("max-age", new CookieAttributeAssignor() {

            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                try {
                    long maxage = Long.parseLong(attrValue);
                    if (cookie.getMaxAge() == MAX_AGE_UNSPECIFIED)
                        cookie.setMaxAge(maxage);
                } catch (NumberFormatException ignored) {
                    throw new IllegalArgumentException("Illegal cookie max-age attribute");
                }
            }
        });
        assignors.put("path", new CookieAttributeAssignor() {

            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                if (cookie.getPath() == null)
                    cookie.setPath(attrValue);
            }
        });
        assignors.put("port", new CookieAttributeAssignor() {

            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                if (cookie.getPortlist() == null)
                    cookie.setPortlist(attrValue == null ? "" : attrValue);
            }
        });
        assignors.put("secure", new CookieAttributeAssignor() {

            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                cookie.setSecure(true);
            }
        });
        assignors.put("httponly", new CookieAttributeAssignor() {

            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                cookie.setHttpOnly(true);
            }
        });
        assignors.put("version", new CookieAttributeAssignor() {

            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                try {
                    int version = Integer.parseInt(attrValue);
                    cookie.setVersion(version);
                } catch (NumberFormatException ignored) {
                }
            }
        });
        assignors.put("expires", new CookieAttributeAssignor() {

            public void assign(HttpCookie cookie, String attrName, String attrValue) {
                if (cookie.getMaxAge() == MAX_AGE_UNSPECIFIED) {
                    cookie.setMaxAge(cookie.expiryDate2DeltaSeconds(attrValue));
                }
            }
        });
    }

    private static void assignAttribute(HttpCookie cookie, String attrName, String attrValue) {
        attrValue = stripOffSurroundingQuote(attrValue);
        CookieAttributeAssignor assignor = assignors.get(attrName.toLowerCase());
        if (assignor != null) {
            assignor.assign(cookie, attrName, attrValue);
        } else {
        }
    }

    static {
        sun.misc.SharedSecrets.setJavaNetHttpCookieAccess(new sun.misc.JavaNetHttpCookieAccess() {

            public List<HttpCookie> parse(String header) {
                return HttpCookie.parse(header, true);
            }

            public String header(HttpCookie cookie) {
                return cookie.header;
            }
        });
    }

    private String header() {
        return header;
    }

    private String toNetscapeHeaderString() {
        return getName() + "=" + getValue();
    }

    private String toRFC2965HeaderString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append("=\"").append(getValue()).append('"');
        if (getPath() != null)
            sb.append(";$Path=\"").append(getPath()).append('"');
        if (getDomain() != null)
            sb.append(";$Domain=\"").append(getDomain()).append('"');
        if (getPortlist() != null)
            sb.append(";$Port=\"").append(getPortlist()).append('"');
        return sb.toString();
    }

    static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private long expiryDate2DeltaSeconds(String dateString) {
        Calendar cal = new GregorianCalendar(GMT);
        for (int i = 0; i < COOKIE_DATE_FORMATS.length; i++) {
            SimpleDateFormat df = new SimpleDateFormat(COOKIE_DATE_FORMATS[i], Locale.US);
            cal.set(1970, 0, 1, 0, 0, 0);
            df.setTimeZone(GMT);
            df.setLenient(false);
            df.set2DigitYearStart(cal.getTime());
            try {
                cal.setTime(df.parse(dateString));
                if (!COOKIE_DATE_FORMATS[i].contains("yyyy")) {
                    int year = cal.get(Calendar.YEAR);
                    year %= 100;
                    if (year < 70) {
                        year += 2000;
                    } else {
                        year += 1900;
                    }
                    cal.set(Calendar.YEAR, year);
                }
                return (cal.getTimeInMillis() - whenCreated) / 1000;
            } catch (Exception e) {
            }
        }
        return 0;
    }

    private static int guessCookieVersion(String header) {
        int version = 0;
        header = header.toLowerCase();
        if (header.indexOf("expires=") != -1) {
            version = 0;
        } else if (header.indexOf("version=") != -1) {
            version = 1;
        } else if (header.indexOf("max-age") != -1) {
            version = 1;
        } else if (startsWithIgnoreCase(header, SET_COOKIE2)) {
            version = 1;
        }
        return version;
    }

    private static String stripOffSurroundingQuote(String str) {
        if (str != null && str.length() > 2 && str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"') {
            return str.substring(1, str.length() - 1);
        }
        if (str != null && str.length() > 2 && str.charAt(0) == '\'' && str.charAt(str.length() - 1) == '\'') {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    private static boolean equalsIgnoreCase(String s, String t) {
        if (s == t)
            return true;
        if ((s != null) && (t != null)) {
            return s.equalsIgnoreCase(t);
        }
        return false;
    }

    private static boolean startsWithIgnoreCase(String s, String start) {
        if (s == null || start == null)
            return false;
        if (s.length() >= start.length() && start.equalsIgnoreCase(s.substring(0, start.length()))) {
            return true;
        }
        return false;
    }

    private static List<String> splitMultiCookies(String header) {
        List<String> cookies = new java.util.ArrayList<String>();
        int quoteCount = 0;
        int p, q;
        for (p = 0, q = 0; p < header.length(); p++) {
            char c = header.charAt(p);
            if (c == '"')
                quoteCount++;
            if (c == ',' && (quoteCount % 2 == 0)) {
                cookies.add(header.substring(q, p));
                q = p + 1;
            }
        }
        cookies.add(header.substring(q));
        return cookies;
    }
}