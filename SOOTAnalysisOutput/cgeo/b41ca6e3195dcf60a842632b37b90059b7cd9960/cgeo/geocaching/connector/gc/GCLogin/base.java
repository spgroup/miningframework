package cgeo.geocaching.connector.gc;

import cgeo.geocaching.CgeoApplication;
import cgeo.geocaching.R;
import cgeo.geocaching.connector.AbstractLogin;
import cgeo.geocaching.enumerations.StatusCode;
import cgeo.geocaching.network.Cookies;
import cgeo.geocaching.network.HtmlImage;
import cgeo.geocaching.network.Network;
import cgeo.geocaching.network.Parameters;
import cgeo.geocaching.settings.Settings;
import cgeo.geocaching.utils.Log;
import cgeo.geocaching.utils.MatcherWrapper;
import cgeo.geocaching.utils.TextUtils;
import ch.boye.httpclientandroidlib.HttpResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import rx.Observable;
import android.graphics.drawable.Drawable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GCLogin extends AbstractLogin {

    private static final String DEFAULT_CUSTOM_DATE_FORMAT = "MM/dd/yyyy";

    private final static String ENGLISH = "<a href=\"#\">English &#9660;</a>";

    private final static Map<String, SimpleDateFormat> GC_CUSTOM_DATE_FORMATS;

    public static final String LANGUAGE_CHANGE_URI = "http://www.geocaching.com/my/souvenirs.aspx";

    static {
        final String[] formats = new String[] { DEFAULT_CUSTOM_DATE_FORMAT, "yyyy-MM-dd", "yyyy/MM/dd", "dd.MM.yyyy", "dd/MMM/yyyy", "dd.MMM.yyyy", "MMM/dd/yyyy", "dd MMM yy", "dd/MM/yyyy" };
        final Map<String, SimpleDateFormat> map = new HashMap<>();
        for (final String format : formats) {
            map.put(format, new SimpleDateFormat(format, Locale.ENGLISH));
        }
        GC_CUSTOM_DATE_FORMATS = Collections.unmodifiableMap(map);
    }

    private GCLogin() {
    }

    public static GCLogin getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {

        private static final GCLogin INSTANCE = new GCLogin();
    }

    @Override
    protected StatusCode login(boolean retry) {
        final ImmutablePair<String, String> credentials = Settings.getGcCredentials();
        final String username = credentials.left;
        final String password = credentials.right;
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            clearLoginInfo();
            Log.e("Login.login: No login information stored");
            return StatusCode.NO_LOGIN_INFO_STORED;
        }
        setActualStatus(CgeoApplication.getInstance().getString(R.string.init_login_popup_working));
        HttpResponse loginResponse = Network.getRequest("https://www.geocaching.com/login/default.aspx");
        String loginData = Network.getResponseData(loginResponse);
        if (loginResponse != null && loginResponse.getStatusLine().getStatusCode() == 503 && TextUtils.matches(loginData, GCConstants.PATTERN_MAINTENANCE)) {
            return StatusCode.MAINTENANCE;
        }
        if (StringUtils.isBlank(loginData)) {
            Log.e("Login.login: Failed to retrieve login page (1st)");
            return StatusCode.CONNECTION_FAILED;
        }
        if (getLoginStatus(loginData)) {
            Log.i("Already logged in Geocaching.com as " + username + " (" + Settings.getGCMemberStatus() + ')');
            if (switchToEnglish(loginData) && retry) {
                return login(false);
            }
            return StatusCode.NO_ERROR;
        }
        Cookies.clearCookies();
        Settings.setCookieStore(null);
        final Parameters params = new Parameters("__EVENTTARGET", "", "__EVENTARGUMENT", "", "ctl00$ContentBody$tbUsername", username, "ctl00$ContentBody$tbPassword", password, "ctl00$ContentBody$cbRememberMe", "on", "ctl00$ContentBody$btnSignIn", "Login");
        final String[] viewstates = GCLogin.getViewstates(loginData);
        if (isEmpty(viewstates)) {
            Log.e("Login.login: Failed to find viewstates");
            return StatusCode.LOGIN_PARSE_ERROR;
        }
        GCLogin.putViewstates(params, viewstates);
        loginResponse = Network.postRequest("https://www.geocaching.com/login/default.aspx", params);
        loginData = Network.getResponseData(loginResponse);
        if (StringUtils.isBlank(loginData)) {
            Log.e("Login.login: Failed to retrieve login page (2nd)");
            return StatusCode.COMMUNICATION_ERROR;
        }
        assert loginData != null;
        if (getLoginStatus(loginData)) {
            if (switchToEnglish(loginData) && retry) {
                return login(false);
            }
            Log.i("Successfully logged in Geocaching.com as " + username + " (" + Settings.getGCMemberStatus() + ')');
            Settings.setCookieStore(Cookies.dumpCookieStore());
            return StatusCode.NO_ERROR;
        }
        if (loginData.contains("Your username/password combination does not match.")) {
            Log.i("Failed to log in Geocaching.com as " + username + " because of wrong username/password");
            return StatusCode.WRONG_LOGIN_DATA;
        }
        if (loginData.contains("You must validate your account before you can log in.")) {
            Log.i("Failed to log in Geocaching.com as " + username + " because account needs to be validated first");
            return StatusCode.UNVALIDATED_ACCOUNT;
        }
        Log.i("Failed to log in Geocaching.com as " + username + " for some unknown reason");
        if (retry) {
            switchToEnglish(loginData);
            return login(false);
        }
        return StatusCode.UNKNOWN_ERROR;
    }

    public StatusCode logout() {
        final HttpResponse logoutResponse = Network.getRequest("https://www.geocaching.com/login/default.aspx?RESET=Y&redir=http%3a%2f%2fwww.geocaching.com%2fdefault.aspx%3f");
        final String logoutData = Network.getResponseData(logoutResponse);
        if (logoutResponse != null && logoutResponse.getStatusLine().getStatusCode() == 503 && TextUtils.matches(logoutData, GCConstants.PATTERN_MAINTENANCE)) {
            return StatusCode.MAINTENANCE;
        }
        resetLoginStatus();
        return StatusCode.NO_ERROR;
    }

    public boolean getLoginStatus(@Nullable final String page) {
        if (StringUtils.isBlank(page)) {
            Log.e("Login.checkLogin: No page given");
            return false;
        }
        assert page != null;
        if (TextUtils.matches(page, GCConstants.PATTERN_MAP_LOGGED_IN)) {
            return true;
        }
        setActualStatus(CgeoApplication.getInstance().getString(R.string.init_login_popup_ok));
        setActualLoginStatus(TextUtils.matches(page, GCConstants.PATTERN_LOGIN_NAME));
        if (isActualLoginStatus()) {
            setActualUserName(TextUtils.getMatch(page, GCConstants.PATTERN_LOGIN_NAME, true, "???"));
            int cachesCount = 0;
            try {
                cachesCount = Integer.parseInt(TextUtils.getMatch(page, GCConstants.PATTERN_CACHES_FOUND, true, "0").replaceAll("[,.]", ""));
            } catch (final NumberFormatException e) {
                Log.e("getLoginStatus: bad cache count", e);
            }
            setActualCachesFound(cachesCount);
            Settings.setGCMemberStatus(TextUtils.getMatch(page, GCConstants.PATTERN_MEMBER_STATUS, true, null));
            if (page.contains(GCConstants.MEMBER_STATUS_RENEW)) {
                Settings.setGCMemberStatus(GCConstants.MEMBER_STATUS_PM);
            }
            return true;
        }
        setActualLoginStatus(TextUtils.matches(page, GCConstants.PATTERN_LOGIN_NAME_LOGIN_PAGE));
        if (isActualLoginStatus()) {
            setActualUserName(Settings.getUsername());
            return true;
        }
        setActualStatus(CgeoApplication.getInstance().getString(R.string.init_login_popup_failed));
        return false;
    }

    private boolean switchToEnglish(String previousPage) {
        if (previousPage != null && previousPage.contains(ENGLISH)) {
            Log.i("Geocaching.com language already set to English");
            getLoginStatus(Network.getResponseData(Network.getRequest("http://www.geocaching.com/email/")));
        } else {
            final String page = Network.getResponseData(Network.getRequest(LANGUAGE_CHANGE_URI));
            getLoginStatus(page);
            if (page == null) {
                Log.e("Failed to read viewstates to set geocaching.com language");
            }
            final Parameters params = new Parameters("__EVENTTARGET", "ctl00$uxLocaleList$uxLocaleList$ctl00$uxLocaleItem", "__EVENTARGUMENT", "");
            GCLogin.transferViewstates(page, params);
            final HttpResponse response = Network.postRequest(LANGUAGE_CHANGE_URI, params, new Parameters("Referer", LANGUAGE_CHANGE_URI));
            if (Network.isSuccess(response)) {
                Log.i("changed language on geocaching.com to English");
                return true;
            }
            Log.e("Failed to set geocaching.com language to English");
        }
        return false;
    }

    public Observable<Drawable> downloadAvatarAndGetMemberStatus() {
        try {
            final String responseData = StringUtils.defaultString(Network.getResponseData(Network.getRequest("http://www.geocaching.com/my/")));
            final String profile = TextUtils.replaceWhitespace(responseData);
            Settings.setGCMemberStatus(TextUtils.getMatch(profile, GCConstants.PATTERN_MEMBER_STATUS, true, null));
            if (profile.contains(GCConstants.MEMBER_STATUS_RENEW)) {
                Settings.setGCMemberStatus(GCConstants.MEMBER_STATUS_PM);
            }
            setActualCachesFound(Integer.parseInt(TextUtils.getMatch(profile, GCConstants.PATTERN_CACHES_FOUND, true, "-1").replaceAll("[,.]", "")));
            final String avatarURL = TextUtils.getMatch(profile, GCConstants.PATTERN_AVATAR_IMAGE_PROFILE_PAGE, false, null);
            if (avatarURL != null) {
                final HtmlImage imgGetter = new HtmlImage("", false, 0, false);
                return imgGetter.fetchDrawable(avatarURL.replace("avatar", "user/large")).cast(Drawable.class);
            }
            Log.d("No avatar set for user");
        } catch (final Exception e) {
            Log.w("Error when retrieving user avatar", e);
        }
        return null;
    }

    public static void detectGcCustomDate() {
        final String result = Network.getResponseData(Network.getRequest("http://www.geocaching.com/account/ManagePreferences.aspx"));
        if (null == result) {
            Log.w("Login.detectGcCustomDate: result is null");
            return;
        }
        final String customDate = TextUtils.getMatch(result, GCConstants.PATTERN_CUSTOMDATE, true, null);
        if (null != customDate) {
            Settings.setGcCustomDate(customDate);
        }
    }

    public static Date parseGcCustomDate(final String input, final String format) throws ParseException {
        if (StringUtils.isBlank(input)) {
            throw new ParseException("Input is null", 0);
        }
        final String trimmed = input.trim();
        if (GC_CUSTOM_DATE_FORMATS.containsKey(format)) {
            try {
                return GC_CUSTOM_DATE_FORMATS.get(format).parse(trimmed);
            } catch (final ParseException e) {
            }
        }
        for (final SimpleDateFormat sdf : GC_CUSTOM_DATE_FORMATS.values()) {
            try {
                return sdf.parse(trimmed);
            } catch (final ParseException e) {
            }
        }
        throw new ParseException("No matching pattern", 0);
    }

    public static Date parseGcCustomDate(final String input) throws ParseException {
        return parseGcCustomDate(input, Settings.getGcCustomDate());
    }

    public static SimpleDateFormat getCustomGcDateFormat() {
        final String format = Settings.getGcCustomDate();
        if (GC_CUSTOM_DATE_FORMATS.containsKey(format)) {
            return GC_CUSTOM_DATE_FORMATS.get(format);
        }
        return GC_CUSTOM_DATE_FORMATS.get(DEFAULT_CUSTOM_DATE_FORMAT);
    }

    public static boolean isEmpty(String[] a) {
        if (a == null) {
            return true;
        }
        for (final String s : a) {
            if (StringUtils.isNotEmpty(s)) {
                return false;
            }
        }
        return true;
    }

    public static String[] getViewstates(String page) {
        if (page == null) {
            return null;
        }
        int count = 1;
        final MatcherWrapper matcherViewstateCount = new MatcherWrapper(GCConstants.PATTERN_VIEWSTATEFIELDCOUNT, page);
        if (matcherViewstateCount.find()) {
            try {
                count = Integer.parseInt(matcherViewstateCount.group(1));
            } catch (final NumberFormatException e) {
                Log.e("getViewStates", e);
            }
        }
        final String[] viewstates = new String[count];
        final MatcherWrapper matcherViewstates = new MatcherWrapper(GCConstants.PATTERN_VIEWSTATES, page);
        while (matcherViewstates.find()) {
            final String sno = matcherViewstates.group(1);
            int no;
            if (StringUtils.isEmpty(sno)) {
                no = 0;
            } else {
                try {
                    no = Integer.parseInt(sno);
                } catch (final NumberFormatException e) {
                    Log.e("getViewStates", e);
                    no = 0;
                }
            }
            viewstates[no] = matcherViewstates.group(2);
        }
        if (viewstates.length != 1 || viewstates[0] != null) {
            return viewstates;
        }
        return null;
    }

    public static void putViewstates(final Parameters params, final String[] viewstates) {
        if (ArrayUtils.isEmpty(viewstates)) {
            return;
        }
        params.put("__VIEWSTATE", viewstates[0]);
        if (viewstates.length > 1) {
            for (int i = 1; i < viewstates.length; i++) {
                params.put("__VIEWSTATE" + i, viewstates[i]);
            }
            params.put("__VIEWSTATEFIELDCOUNT", String.valueOf(viewstates.length));
        }
    }

    public static void transferViewstates(final String page, final Parameters params) {
        putViewstates(params, getViewstates(page));
    }

    public String postRequestLogged(final String uri, final Parameters params) {
        final String data = Network.getResponseData(Network.postRequest(uri, params));
        if (getLoginStatus(data)) {
            return data;
        }
        if (login() == StatusCode.NO_ERROR) {
            return Network.getResponseData(Network.postRequest(uri, params));
        }
        Log.i("Working as guest.");
        return data;
    }

    @Nullable
    public String getRequestLogged(@NonNull final String uri, @Nullable final Parameters params) {
        final HttpResponse response = Network.getRequest(uri, params);
        final String data = Network.getResponseData(response, canRemoveWhitespace(uri));
        if (Network.isPageNotFound(response) || getLoginStatus(data)) {
            return data;
        }
        if (login() == StatusCode.NO_ERROR) {
            return Network.getResponseData(Network.getRequest(uri, params), canRemoveWhitespace(uri));
        }
        Log.w("Working as guest.");
        return data;
    }

    private static boolean canRemoveWhitespace(final String uri) {
        return !StringUtils.contains(uri, "cache_details");
    }

    @NonNull
    public MapTokens getMapTokens() {
        final String data = getRequestLogged(GCConstants.URL_LIVE_MAP, null);
        final String userSession = TextUtils.getMatch(data, GCConstants.PATTERN_USERSESSION, "");
        final String sessionToken = TextUtils.getMatch(data, GCConstants.PATTERN_SESSIONTOKEN, "");
        return new MapTokens(userSession, sessionToken);
    }
}
