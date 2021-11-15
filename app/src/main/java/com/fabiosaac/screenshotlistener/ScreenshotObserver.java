package com.fabiosaac.screenshotlistener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

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
      String path = MediaManager.getPathFromUri(context, uri);

      if (
        path.toLowerCase().contains("screenshot")
        && !path.contains(context.getString(R.string.app_name_abbreviation))
      ) {
        Intent notificationIntent = new Intent(context, ScreenshotObserverService.class);

        notificationIntent.putExtra(ScreenshotObserverService.PATH_KEY, path);

        PendingIntent pendingIntent = PendingIntent.getService(context, NOTIFICATION_ID,
          notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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
}
