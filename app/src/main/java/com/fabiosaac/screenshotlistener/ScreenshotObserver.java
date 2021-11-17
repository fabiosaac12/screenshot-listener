package com.fabiosaac.screenshotlistener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Handler;

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
        Bitmap bitmap = BitmapFactory.decodeFile(path);

        Intent notificationIntent = new Intent(context, ScreenshotObserverService.class);
        notificationIntent.putExtra(ScreenshotObserverService.EXTRA_SCREENSHOT_PATH, path);

        PendingIntent notificationPendingIntent = PendingIntent.getService(context, NOTIFICATION_ID,
          notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(context,
          MainActivity.NOTIFICATION_NEW_SCREENSHOT_CHANNEL_ID)
          .setSmallIcon(R.mipmap.ic_launcher)
          .setContentTitle("New Screenshot")
          .setContentText("Remember to have your gallery organized!")
          .setContentIntent(notificationPendingIntent)
          .setLargeIcon(bitmap)
          .setAutoCancel(true)
          .setStyle(new Notification.BigPictureStyle()
            .bigPicture(bitmap)
            .bigLargeIcon((Bitmap) null))
          .addAction(getShareAction(path))
          .addAction(getDeleteAction(path))
          .addAction(getSaveAction(path));

        NotificationManager notificationManager =
          context.getSystemService(NotificationManager.class);

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        NOTIFICATION_ID++;
      }
    }

    super.onChange(selfChange, uri);
  }

  private Notification.Action getShareAction(String path) {
    Intent shareIntent = new Intent(context, BroadcastActionReceiver.class);
    shareIntent.setAction(BroadcastActionReceiver.ACTION_NOTIFICATION_SHARE);
    shareIntent.putExtra(BroadcastActionReceiver.EXTRA_SCREENSHOT_PATH, path);
    shareIntent.putExtra(BroadcastActionReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID);

    PendingIntent sharePendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID,
      shareIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    Notification.Action shareAction = new Notification.Action.Builder(
      Icon.createWithResource(context, R.drawable.ic_share), "Share",
      sharePendingIntent).build();

    return  shareAction;
  }

  private Notification.Action getDeleteAction(String path) {
    Intent deleteIntent = new Intent(context, BroadcastActionReceiver.class);
    deleteIntent.setAction(BroadcastActionReceiver.ACTION_NOTIFICATION_DELETE);
    deleteIntent.putExtra(BroadcastActionReceiver.EXTRA_SCREENSHOT_PATH, path);
    deleteIntent.putExtra(BroadcastActionReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID);

    PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID,
      deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    Notification.Action deleteAction = new Notification.Action.Builder(
      Icon.createWithResource(context, R.drawable.ic_delete), "Delete",
      deletePendingIntent).build();

    return deleteAction;
  }

  private Notification.Action getSaveAction(String path) {
    RemoteInput remoteInputForSaveAction = new RemoteInput.Builder(
      BroadcastActionReceiver.EXTRA_ALBUM_NAME
    )
      .setLabel("Save in / Create album...")
      .setChoices(new CharSequence[] {"Fotoz", "Picturez"})
      .build();

    Intent saveIntent = new Intent(context, BroadcastActionReceiver.class);
    saveIntent.setAction(BroadcastActionReceiver.ACTION_NOTIFICATION_SAVE);
    saveIntent.putExtra(BroadcastActionReceiver.EXTRA_SCREENSHOT_PATH, path);
    saveIntent.putExtra(BroadcastActionReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID);

    PendingIntent savePendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID,
      saveIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    Notification.Action saveAction = new Notification.Action.Builder(
      Icon.createWithResource(context, R.drawable.ic_delete), "Save in...", savePendingIntent)
      .setAllowGeneratedReplies(false)
      .addRemoteInput(remoteInputForSaveAction)
      .build();

    return saveAction;
  }
}
