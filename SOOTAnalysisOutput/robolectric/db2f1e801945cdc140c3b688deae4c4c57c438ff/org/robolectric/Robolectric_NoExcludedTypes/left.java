package org.robolectric;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.internal.ShadowProvider;

@Generated("org.robolectric.annotation.processing.RobolectricProcessor")
@SuppressWarnings({ "unchecked", "deprecation" })
public class Shadows implements ShadowProvider {

    private static final Map<String, String> SHADOW_MAP = new HashMap<>(1);

    static {
        SHADOW_MAP.put("org.robolectric.annotation.processing.objects.Dummy", "org.robolectric.annotation.processing.shadows.ShadowExcludedFromAndroidSdk");
    }

    public void reset() {
    }

    @Override
    public Map<String, String> getShadowMap() {
        return SHADOW_MAP;
    }

    @Override
    public String[] getProvidedPackageNames() {
        return new String[] {};
    }
}
