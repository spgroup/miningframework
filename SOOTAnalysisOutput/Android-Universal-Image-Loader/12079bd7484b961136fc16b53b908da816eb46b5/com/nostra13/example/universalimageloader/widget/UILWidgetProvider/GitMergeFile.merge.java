package com.nostra13.example.universalimageloader.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.RemoteViews;
import com.nostra13.example.universalimageloader.R;
import com.nostra13.example.universalimageloader.UILApplication;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import static com.nostra13.example.universalimageloader.Constants.IMAGES;

public class UILWidgetProvider extends AppWidgetProvider {

    private static DisplayImageOptions displayOptions;

    static {
        displayOptions = DisplayImageOptions.createSimple();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        UILApplication.initImageLoader(context);
        final int widgetCount = appWidgetIds.length;
        for (int i = 0; i < widgetCount; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        ImageSize minImageSize = new ImageSize(70, 70);
        ImageLoader.getInstance().loadImage(IMAGES[0], minImageSize, displayOptions, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                views.setImageViewBitmap(R.id.image_left, loadedImage);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        });
        ImageLoader.getInstance().loadImage(IMAGES[1], minImageSize, displayOptions, new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                views.setImageViewBitmap(R.id.image_right, loadedImage);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        });
    }
}
