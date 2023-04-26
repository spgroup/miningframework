package org.springframework.boot.actuate.trace;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "management.trace")
public class TraceProperties {

    private static final Set<Include> DEFAULT_INCLUDES;

    static {
        Set<Include> defaultIncludes = new LinkedHashSet<Include>();
        defaultIncludes.add(Include.REQUEST_HEADERS);
        defaultIncludes.add(Include.RESPONSE_HEADERS);
        defaultIncludes.add(Include.COOKIES);
        defaultIncludes.add(Include.ERRORS);
        DEFAULT_INCLUDES = Collections.unmodifiableSet(defaultIncludes);
    }

    private Set<Include> include = new HashSet<Include>(DEFAULT_INCLUDES);

    public Set<Include> getInclude() {
        return this.include;
    }

    public void setInclude(Set<Include> include) {
        this.include = include;
    }

    public enum Include {

        REQUEST_HEADERS,
        RESPONSE_HEADERS,
        COOKIES,
        AUTHORIZATION_HEADER,
        ERRORS,
        PATH_INFO,
        PATH_TRANSLATED,
        CONTEXT_PATH,
        USER_PRINCIPAL,
        PARAMETERS,
        QUERY_STRING,
        AUTH_TYPE,
        REMOTE_ADDRESS,
        SESSION_ID,
        REMOTE_USER
    }
}
