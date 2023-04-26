package com.android.launcher3.util;

import android.content.SharedPreferences;
import android.util.ArrayMap;
import androidx.annotation.StringDef;
import com.android.launcher3.Launcher;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Map;

public class OnboardingPrefs<T extends Launcher> {

    public static final String HOME_BOUNCE_SEEN = "launcher.apps_view_shown";

    public static final String SHELF_BOUNCE_SEEN = "launcher.shelf_bounce_seen";

    public static final String HOME_BOUNCE_COUNT = "launcher.home_bounce_count";

    public static final String SHELF_BOUNCE_COUNT = "launcher.shelf_bounce_count";

    public static final String ALL_APPS_COUNT = "launcher.all_apps_count";

    public static final String HOTSEAT_DISCOVERY_TIP_COUNT = "launcher.hotseat_discovery_tip_count";

    public static final String HOTSEAT_LONGPRESS_TIP_SEEN = "launcher.hotseat_longpress_tip_seen";

    @StringDef(value = { HOME_BOUNCE_SEEN, SHELF_BOUNCE_SEEN })
    @Retention(RetentionPolicy.SOURCE)
    public @interface EventBoolKey {
    }

    @StringDef(value = { HOME_BOUNCE_COUNT, SHELF_BOUNCE_COUNT, ALL_APPS_COUNT, HOTSEAT_DISCOVERY_TIP_COUNT })
    @Retention(RetentionPolicy.SOURCE)
    public @interface EventCountKey {
    }

    private static final Map<String, Integer> MAX_COUNTS;

    static {
        Map<String, Integer> maxCounts = new ArrayMap<>(4);
        maxCounts.put(HOME_BOUNCE_COUNT, 3);
        maxCounts.put(SHELF_BOUNCE_COUNT, 3);
        maxCounts.put(ALL_APPS_COUNT, 5);
        maxCounts.put(HOTSEAT_DISCOVERY_TIP_COUNT, 5);
        MAX_COUNTS = Collections.unmodifiableMap(maxCounts);
    }

    protected final T mLauncher;

    protected final SharedPreferences mSharedPrefs;

    public OnboardingPrefs(T launcher, SharedPreferences sharedPrefs) {
        mLauncher = launcher;
        mSharedPrefs = sharedPrefs;
    }

    public int getCount(@EventCountKey String key) {
        return mSharedPrefs.getInt(key, 0);
    }

    public boolean hasReachedMaxCount(@EventCountKey String eventKey) {
        return hasReachedMaxCount(getCount(eventKey), eventKey);
    }

    private boolean hasReachedMaxCount(int count, @EventCountKey String eventKey) {
        return count >= MAX_COUNTS.get(eventKey);
    }

    public boolean getBoolean(@EventBoolKey String key) {
        return mSharedPrefs.getBoolean(key, false);
    }

    public void markChecked(String flag) {
        mSharedPrefs.edit().putBoolean(flag, true).apply();
    }

    public boolean incrementEventCount(@EventCountKey String eventKey) {
        int count = getCount(eventKey);
        if (hasReachedMaxCount(count, eventKey)) {
            return true;
        }
        count++;
        mSharedPrefs.edit().putInt(eventKey, count).apply();
        return hasReachedMaxCount(count, eventKey);
    }
}
