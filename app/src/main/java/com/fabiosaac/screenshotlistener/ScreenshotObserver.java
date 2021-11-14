package com.fabiosaac.screenshotlistener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;

public class ScreenshotObserver extends ContentObserver {
  private static int NOTIFICATION_ID = 102;
  private final Context context;

  public ScreenshotObserver(Handler handler, Context context) {
    super(handler);

    this.context = context;
  }

  @Override
  public void onChange(boolean selfChange, @Nullable Uri uri) {
    if (uri != null) {
      String path = getPathFromUri(uri);

      if (
        path.toLowerCase().contains("screenshot")
        && !path.contains(context.getString(R.string.app_name_abbreviation))
      ) {
        Intent notificationIntent = new Intent(context, ScreenshotObserverService.class);
        notificationIntent.putExtra(ScreenshotObserverService.PATH_KEY, path);

        PendingIntent pendingIntent =
          PendingIntent.getService(context, 8, notificationIntent, 0);

        Notification.Builder builder = new Notification.Builder(context,
          MainActivity.NOTIFICATION_NEW_SCREENSHOT_CHANNEL_ID)
          .setSmallIcon(R.mipmap.ic_launcher)
          .setContentTitle("New Screenshot")
          .setContentText(path)
          .setContentIntent(pendingIntent)
          .setAutoCancel(true);

        NotificationManager notificationManager =
          context.getSystemService(NotificationManager.class);

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        NOTIFICATION_ID++;
      }
    }

    super.onChange(selfChange, uri);
  }

  private String getPathFromUri(Uri uri) {
    String path;

    try {
      Cursor cursor =
        context.getContentResolver()
          .query(uri, null, null, null, null);

      if (cursor == null) {
        path = uri.getPath();
      } else {
        cursor.moveToFirst();

        int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

        path = cursor.getString(index);

        cursor.close();
      }
    } catch (Exception exception) {
      path = "";
    }

    return path;
  }
}
