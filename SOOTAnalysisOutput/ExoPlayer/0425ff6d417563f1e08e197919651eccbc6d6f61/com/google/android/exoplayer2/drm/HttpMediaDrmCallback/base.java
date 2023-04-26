package com.google.android.exoplayer2.drm;

import android.annotation.TargetApi;
import android.net.Uri;
import android.text.TextUtils;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.drm.ExoMediaDrm.KeyRequest;
import com.google.android.exoplayer2.drm.ExoMediaDrm.ProvisionRequest;
import com.google.android.exoplayer2.upstream.DataSourceInputStream;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource.Factory;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@TargetApi(18)
public final class HttpMediaDrmCallback implements MediaDrmCallback {

    private static final Map<String, String> PLAYREADY_KEY_REQUEST_PROPERTIES;

    static {
        PLAYREADY_KEY_REQUEST_PROPERTIES = new HashMap<>();
        PLAYREADY_KEY_REQUEST_PROPERTIES.put("Content-Type", "text/xml");
        PLAYREADY_KEY_REQUEST_PROPERTIES.put("SOAPAction", "http://schemas.microsoft.com/DRM/2007/03/protocols/AcquireLicense");
    }

    private final HttpDataSource.Factory dataSourceFactory;

    private final String defaultUrl;

    private final Map<String, String> keyRequestProperties;

    public HttpMediaDrmCallback(String defaultUrl, HttpDataSource.Factory dataSourceFactory) {
        this(defaultUrl, dataSourceFactory, null);
    }

    @Deprecated
    public HttpMediaDrmCallback(String defaultUrl, HttpDataSource.Factory dataSourceFactory, Map<String, String> keyRequestProperties) {
        this.dataSourceFactory = dataSourceFactory;
        this.defaultUrl = defaultUrl;
        this.keyRequestProperties = new HashMap<>();
        if (keyRequestProperties != null) {
            this.keyRequestProperties.putAll(keyRequestProperties);
        }
    }

    public void setKeyRequestProperty(String name, String value) {
        Assertions.checkNotNull(name);
        Assertions.checkNotNull(value);
        synchronized (keyRequestProperties) {
            keyRequestProperties.put(name, value);
        }
    }

    public void clearKeyRequestProperty(String name) {
        Assertions.checkNotNull(name);
        synchronized (keyRequestProperties) {
            keyRequestProperties.remove(name);
        }
    }

    public void clearAllKeyRequestProperties() {
        synchronized (keyRequestProperties) {
            keyRequestProperties.clear();
        }
    }

    @Override
    public byte[] executeProvisionRequest(UUID uuid, ProvisionRequest request) throws IOException {
        String url = request.getDefaultUrl() + "&signedRequest=" + new String(request.getData());
        return executePost(dataSourceFactory, url, new byte[0], null);
    }

    @Override
    public byte[] executeKeyRequest(UUID uuid, KeyRequest request) throws Exception {
        String url = request.getDefaultUrl();
        if (TextUtils.isEmpty(url)) {
            url = defaultUrl;
        }
        Map<String, String> requestProperties = new HashMap<>();
        requestProperties.put("Content-Type", "application/octet-stream");
        if (C.PLAYREADY_UUID.equals(uuid)) {
            requestProperties.putAll(PLAYREADY_KEY_REQUEST_PROPERTIES);
        }
        synchronized (keyRequestProperties) {
            requestProperties.putAll(keyRequestProperties);
        }
        return executePost(dataSourceFactory, url, request.getData(), requestProperties);
    }

    private static byte[] executePost(HttpDataSource.Factory dataSourceFactory, String url, byte[] data, Map<String, String> requestProperties) throws IOException {
        HttpDataSource dataSource = dataSourceFactory.createDataSource();
        if (requestProperties != null) {
            for (Map.Entry<String, String> requestProperty : requestProperties.entrySet()) {
                dataSource.setRequestProperty(requestProperty.getKey(), requestProperty.getValue());
            }
        }
        DataSpec dataSpec = new DataSpec(Uri.parse(url), data, 0, 0, C.LENGTH_UNSET, null, DataSpec.FLAG_ALLOW_GZIP);
        DataSourceInputStream inputStream = new DataSourceInputStream(dataSource, dataSpec);
        try {
            return Util.toByteArray(inputStream);
        } finally {
            Util.closeQuietly(inputStream);
        }
    }
}
