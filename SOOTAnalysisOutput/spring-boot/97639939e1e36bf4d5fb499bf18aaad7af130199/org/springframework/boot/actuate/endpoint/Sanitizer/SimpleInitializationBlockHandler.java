package org.springframework.boot.actuate.endpoint;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class Sanitizer {

    private static final String[] REGEX_PARTS = { "*", "$", "^", "+" };

    private static final Set<String> DEFAULT_KEYS_TO_SANITIZE = new LinkedHashSet<>(Arrays.asList("password", "secret", "key", "token", ".*credentials.*", "vcap_services", "sun.java.command"));

    private static final Set<String> URI_USERINFO_KEYS = new LinkedHashSet<>(Arrays.asList("uri", "uris", "address", "addresses"));

    private static final Pattern URI_USERINFO_PATTERN = Pattern.compile("[A-Za-z]+://.+:(.*)@.+$");

    private Pattern[] keysToSanitize;

    static {
        DEFAULT_KEYS_TO_SANITIZE.addAll(URI_USERINFO_KEYS);
    }

    public Sanitizer() {
        this(DEFAULT_KEYS_TO_SANITIZE.toArray(new String[0]));
    }

    public Sanitizer(String... keysToSanitize) {
        setKeysToSanitize(keysToSanitize);
    }

    public void setKeysToSanitize(String... keysToSanitize) {
        Assert.notNull(keysToSanitize, "KeysToSanitize must not be null");
        this.keysToSanitize = new Pattern[keysToSanitize.length];
        for (int i = 0; i < keysToSanitize.length; i++) {
            this.keysToSanitize[i] = getPattern(keysToSanitize[i]);
        }
    }

    private Pattern getPattern(String value) {
        if (isRegex(value)) {
            return Pattern.compile(value, Pattern.CASE_INSENSITIVE);
        }
        return Pattern.compile(".*" + value + "$", Pattern.CASE_INSENSITIVE);
    }

    private boolean isRegex(String value) {
        for (String part : REGEX_PARTS) {
            if (value.contains(part)) {
                return true;
            }
        }
        return false;
    }

    public Object sanitize(String key, Object value) {
        if (value == null) {
            return null;
        }
        for (Pattern pattern : this.keysToSanitize) {
            if (pattern.matcher(key).matches()) {
                if (keyIsUriWithUserInfo(pattern)) {
                    return sanitizeUris(value.toString());
                }
                return "******";
            }
        }
        return value;
    }

    private boolean keyIsUriWithUserInfo(Pattern pattern) {
        for (String uriKey : URI_USERINFO_KEYS) {
            if (pattern.matcher(uriKey).matches()) {
                return true;
            }
        }
        return false;
    }

    private Object sanitizeUris(String value) {
        return Arrays.stream(value.split(",")).map(this::sanitizeUri).collect(Collectors.joining(","));
    }

    private String sanitizeUri(String value) {
        Matcher matcher = URI_USERINFO_PATTERN.matcher(value);
        String password = matcher.matches() ? matcher.group(1) : null;
        if (password != null) {
            return StringUtils.replace(value, ":" + password + "@", ":******@");
        }
        return value;
    }
}