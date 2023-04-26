package com.android.gallery3d.common;

import android.database.Cursor;
import android.graphics.RectF;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import java.io.Closeable;
import java.io.IOException;

public class Utils {

    private static final String TAG = "Utils";

    public static void assertTrue(boolean cond) {
        if (!cond) {
            throw new AssertionError();
        }
    }

    public static int nextPowerOf2(int n) {
        if (n <= 0 || n > (1 << 30))
            throw new IllegalArgumentException("n is invalid: " + n);
        n -= 1;
        n |= n >> 16;
        n |= n >> 8;
        n |= n >> 4;
        n |= n >> 2;
        n |= n >> 1;
        return n + 1;
    }

    public static int prevPowerOf2(int n) {
        if (n <= 0)
            throw new IllegalArgumentException();
        return Integer.highestOneBit(n);
    }

    public static int clamp(int x, int min, int max) {
        if (x > max)
            return max;
        if (x < min)
            return min;
        return x;
    }

    public static int ceilLog2(float value) {
        int i;
        for (i = 0; i < 31; i++) {
            if ((1 << i) >= value)
                break;
        }
        return i;
    }

    public static int floorLog2(float value) {
        int i;
        for (i = 0; i < 31; i++) {
            if ((1 << i) > value)
                break;
        }
        return i - 1;
    }

    public static void closeSilently(Closeable c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (IOException t) {
            Log.w(TAG, "close fail ", t);
        }
    }

    public static void closeSilently(ParcelFileDescriptor fd) {
        try {
            if (fd != null)
                fd.close();
        } catch (Throwable t) {
            Log.w(TAG, "fail to close", t);
        }
    }

    public static void closeSilently(Cursor cursor) {
        try {
            if (cursor != null)
                cursor.close();
        } catch (Throwable t) {
            Log.w(TAG, "fail to close", t);
        }
    }

    public static RectF getMaxCropRect(int inWidth, int inHeight, int outWidth, int outHeight, boolean leftAligned) {
        RectF cropRect = new RectF();
        if (inWidth / (float) inHeight > outWidth / (float) outHeight) {
            cropRect.top = 0;
            cropRect.bottom = inHeight;
            cropRect.left = (inWidth - (outWidth / (float) outHeight) * inHeight) / 2;
            cropRect.right = inWidth - cropRect.left;
            if (leftAligned) {
                cropRect.right -= cropRect.left;
                cropRect.left = 0;
            }
        } else {
            cropRect.left = 0;
            cropRect.right = inWidth;
            cropRect.top = (inHeight - (outHeight / (float) outWidth) * inWidth) / 2;
            cropRect.bottom = inHeight - cropRect.top;
        }
        return cropRect;
    }
}
