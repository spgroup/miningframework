package com.android.launcher3.dynamicui;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.util.Log;
import com.android.launcher3.Utilities;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.dynamicui.colorextraction.ColorExtractor;
import java.util.ArrayList;
import java.util.Arrays;

public class ExtractedColors {

    private static final String TAG = "ExtractedColors";

    public static final int DEFAULT_LIGHT = Color.WHITE;

    public static final int DEFAULT_DARK = Color.BLACK;

    public static final int VERSION_INDEX = 0;

    public static final int HOTSEAT_INDEX = 1;

    public static final int STATUS_BAR_INDEX = 2;

    public static final int WALLPAPER_VIBRANT_INDEX = 3;

    public static final int ALLAPPS_GRADIENT_MAIN_INDEX = 4;

    public static final int ALLAPPS_GRADIENT_SECONDARY_INDEX = 5;

    private static final int VERSION;

    private static final int[] DEFAULT_VALUES;

    static {
        if (FeatureFlags.LAUNCHER3_GRADIENT_ALL_APPS) {
            VERSION = 3;
            DEFAULT_VALUES = new int[] { VERSION, 0x40FFFFFF, DEFAULT_DARK, 0xFFCCCCCC, 0xFF000000, 0xFF000000 };
        } else if (FeatureFlags.QSB_IN_HOTSEAT) {
            VERSION = 2;
            DEFAULT_VALUES = new int[] { VERSION, 0x40FFFFFF, DEFAULT_DARK, 0xFFCCCCCC };
        } else {
            VERSION = 1;
            DEFAULT_VALUES = new int[] { VERSION, 0x40FFFFFF, DEFAULT_DARK };
        }
    }

    private static final String COLOR_SEPARATOR = ",";

    private final ArrayList<OnChangeListener> mListeners = new ArrayList<>();

    private final int[] mColors;

    public ExtractedColors() {
        mColors = Arrays.copyOf(DEFAULT_VALUES, DEFAULT_VALUES.length);
    }

    public void setColorAtIndex(int index, int color) {
        if (index > VERSION_INDEX && index < mColors.length) {
            mColors[index] = color;
        } else {
            Log.e(TAG, "Attempted to set a color at an invalid index " + index);
        }
    }

    String encodeAsString() {
        StringBuilder colorsStringBuilder = new StringBuilder();
        for (int color : mColors) {
            colorsStringBuilder.append(color).append(COLOR_SEPARATOR);
        }
        return colorsStringBuilder.toString();
    }

    public void load(Context context) {
        String encodedString = Utilities.getPrefs(context).getString(ExtractionUtils.EXTRACTED_COLORS_PREFERENCE_KEY, VERSION + "");
        String[] splitColorsString = encodedString.split(COLOR_SEPARATOR);
        if (splitColorsString.length == DEFAULT_VALUES.length && Integer.parseInt(splitColorsString[VERSION_INDEX]) == VERSION) {
            for (int i = 0; i < mColors.length; i++) {
                mColors[i] = Integer.parseInt(splitColorsString[i]);
            }
        } else {
            ExtractionUtils.startColorExtractionService(context);
        }
    }

    public int getColor(int index) {
        return mColors[index];
    }

    public void updateHotseatPalette(Palette hotseatPalette) {
        int hotseatColor;
        if (hotseatPalette != null && ExtractionUtils.isSuperLight(hotseatPalette)) {
            hotseatColor = ColorUtils.setAlphaComponent(Color.BLACK, (int) (0.12f * 255));
        } else if (hotseatPalette != null && ExtractionUtils.isSuperDark(hotseatPalette)) {
            hotseatColor = ColorUtils.setAlphaComponent(Color.WHITE, (int) (0.18f * 255));
        } else {
            hotseatColor = DEFAULT_VALUES[HOTSEAT_INDEX];
        }
        setColorAtIndex(HOTSEAT_INDEX, hotseatColor);
    }

    public void updateStatusBarPalette(Palette statusBarPalette) {
        setColorAtIndex(STATUS_BAR_INDEX, ExtractionUtils.isSuperLight(statusBarPalette) ? DEFAULT_LIGHT : DEFAULT_DARK);
    }

    public void updateWallpaperThemePalette(@Nullable Palette wallpaperPalette) {
        int defaultColor = DEFAULT_VALUES[WALLPAPER_VIBRANT_INDEX];
        setColorAtIndex(WALLPAPER_VIBRANT_INDEX, wallpaperPalette == null ? defaultColor : wallpaperPalette.getVibrantColor(defaultColor));
    }

    public void updateAllAppsGradientPalette(Context context) {
        try {
            WallpaperManager.class.getDeclaredMethod("getWallpaperColors", int.class);
            ColorExtractor extractor = new ColorExtractor(context);
            ColorExtractor.GradientColors colors = extractor.getColors(WallpaperManager.FLAG_SYSTEM);
            setColorAtIndex(ALLAPPS_GRADIENT_MAIN_INDEX, colors.getMainColor());
            setColorAtIndex(ALLAPPS_GRADIENT_SECONDARY_INDEX, colors.getSecondaryColor());
        } catch (NoSuchMethodException e) {
            setColorAtIndex(ALLAPPS_GRADIENT_MAIN_INDEX, Color.WHITE);
            setColorAtIndex(ALLAPPS_GRADIENT_SECONDARY_INDEX, Color.WHITE);
        }
    }

    public void addOnChangeListener(OnChangeListener listener) {
        mListeners.add(listener);
    }

    public void notifyChange() {
        for (OnChangeListener listener : mListeners) {
            listener.onExtractedColorsChanged();
        }
    }

    public interface OnChangeListener {

        void onExtractedColorsChanged();
    }
}
