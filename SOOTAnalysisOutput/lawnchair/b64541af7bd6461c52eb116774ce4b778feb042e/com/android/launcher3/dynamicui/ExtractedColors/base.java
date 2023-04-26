package com.android.launcher3.dynamicui;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.util.Log;
import com.android.launcher3.Utilities;

public class ExtractedColors {

    private static final String TAG = "ExtractedColors";

    public static final int DEFAULT_LIGHT = Color.WHITE;

    public static final int DEFAULT_DARK = Color.BLACK;

    public static final int DEFAULT_COLOR = DEFAULT_LIGHT;

    public static final int VERSION_INDEX = 0;

    public static final int HOTSEAT_INDEX = 1;

    public static final int STATUS_BAR_INDEX = 2;

    public static final int NUM_COLOR_PROFILES = 2;

    private static final int VERSION = 1;

    private static final String COLOR_SEPARATOR = ",";

    private int[] mColors;

    public ExtractedColors() {
        mColors = new int[NUM_COLOR_PROFILES + 1];
        mColors[VERSION_INDEX] = VERSION;
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

    void decodeFromString(String colorsString) {
        String[] splitColorsString = colorsString.split(COLOR_SEPARATOR);
        mColors = new int[splitColorsString.length];
        for (int i = 0; i < mColors.length; i++) {
            mColors[i] = Integer.parseInt(splitColorsString[i]);
        }
    }

    public void load(Context context) {
        String encodedString = Utilities.getPrefs(context).getString(ExtractionUtils.EXTRACTED_COLORS_PREFERENCE_KEY, VERSION + "");
        decodeFromString(encodedString);
        if (mColors[VERSION_INDEX] != VERSION) {
            ExtractionUtils.startColorExtractionService(context);
        }
    }

    public int getColor(int index, int defaultColor) {
        if (index > VERSION_INDEX && index < mColors.length) {
            return mColors[index];
        }
        return defaultColor;
    }

    public void updateHotseatPalette(Palette hotseatPalette) {
        int hotseatColor;
        if (hotseatPalette != null && ExtractionUtils.isSuperLight(hotseatPalette)) {
            hotseatColor = ColorUtils.setAlphaComponent(Color.BLACK, (int) (0.12f * 255));
        } else if (hotseatPalette != null && ExtractionUtils.isSuperDark(hotseatPalette)) {
            hotseatColor = ColorUtils.setAlphaComponent(Color.WHITE, (int) (0.18f * 255));
        } else {
            hotseatColor = ColorUtils.setAlphaComponent(Color.WHITE, (int) (0.25f * 255));
        }
        setColorAtIndex(HOTSEAT_INDEX, hotseatColor);
    }

    public void updateStatusBarPalette(Palette statusBarPalette) {
        setColorAtIndex(STATUS_BAR_INDEX, ExtractionUtils.isSuperLight(statusBarPalette) ? DEFAULT_LIGHT : DEFAULT_DARK);
    }
}
