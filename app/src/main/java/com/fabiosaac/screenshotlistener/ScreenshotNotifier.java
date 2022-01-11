package com.fabiosaac.screenshotlistener;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.util.List;

public class ScreenshotNotifier extends ContentObserver {
  private static int NOTIFICATION_ID = 102;
  private final Context context;

  public ScreenshotNotifier(Handler handler, Context context) {
    super(handler);

    this.context = context;
  }

  public static void sendNotification(Context context, String path) {
    Log.d("    PERMISSION", String.valueOf(verifyStoragePermission(context)));

    if (!verifyStoragePermission(context)) {
      startPermissionsActivity(context, path);
      return;
    }

    if (path != null) {
      File file = new File(path);

      if (
        path.toLowerCase().contains("screenshot")
          && !path.contains(context.getString(R.string.app_name_abbreviation))
          && file.exists()
          && file.lastModified() >= System.currentTimeMillis() - 10000
      ) {
        if (SettingsProvider.getInstance(context).getNotificationDisabled()) {
          ScreenshotNotifierService.handleStart(context, path);
        } else {
          Bitmap bitmap = BitmapFactory.decodeFile(path);

          Intent notificationIntent = new Intent(context, ScreenshotNotifierService.class);
          notificationIntent.putExtra(ScreenshotNotifierService.EXTRA_SCREENSHOT_PATH, path);

          PendingIntent notificationPendingIntent = PendingIntent.getService(context,
            NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

          Notification.Builder notificationBuilder = new Notification.Builder(context,
            MainActivity.CHANNEL_NEW_SCREENSHOT)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("New Screenshot")
            .setContentText("Remember to have your gallery organized!")
            .setContentIntent(notificationPendingIntent)
            .setLargeIcon(bitmap)
            .setAutoCancel(true)
            .setStyle(new Notification.BigPictureStyle()
              .bigPicture(bitmap)
              .bigLargeIcon((Bitmap) null))
            .addAction(getShareAction(context, path))
            .addAction(getDeleteAction(context, path))
            .addAction(getSaveAction(context, path));

          Bundle notificationExtras = new Bundle();
          notificationExtras.putString(ScreenshotNotifierService.EXTRA_SCREENSHOT_PATH, path);
          notificationBuilder.addExtras(notificationExtras);

          NotificationManager notificationManager =
            context.getSystemService(NotificationManager.class);

          int notificationId = SettingsProvider.getInstance(context).getAccumulateNotifications()
            ? NOTIFICATION_ID++ : NOTIFICATION_ID;

          notificationManager.notify(notificationId, notificationBuilder.build());

          new android.os.Handler(Looper.getMainLooper()).postDelayed(
            () -> {
              StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();

              for (StatusBarNotification notification : activeNotifications) {
                String imagePath = notification.getNotification()
                  .extras.getString(ScreenshotNotifierService.EXTRA_SCREENSHOT_PATH);

                if (imagePath != null) {
                  if (!(new File(imagePath).exists())) {
                    notificationManager.cancel(notification.getId());
                  }
                }
              }
            }, 1000);
        }
      }
    }
  }

  private static void startPermissionsActivity(Context context, String screenshotPath) {
    Intent permissionsActivityIntent = new Intent(context, InvisiblePermissionsActivity.class);
    permissionsActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    permissionsActivityIntent.putExtra(InvisiblePermissionsActivity.EXTRA_PERMISSION_CODE,
      InvisiblePermissionsActivity.STORAGE);
    permissionsActivityIntent.putExtra(InvisiblePermissionsActivity.EXTRA_SCREENSHOT_PATH,
      screenshotPath);

    context.startActivity(permissionsActivityIntent);
  }

  private static boolean verifyStoragePermission(Context context) {
    boolean granted;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      granted = !Environment.isExternalStorageManager();
    } else {
      granted = !(ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED
        || ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED);
    }

    return granted;
  }

  private static Notification.Action getShareAction(Context context, String path) {
    Intent shareIntent = new Intent(context, BroadcastActionReceiver.class);
    shareIntent.setAction(BroadcastActionReceiver.ACTION_SHARE);
    shareIntent.putExtra(BroadcastActionReceiver.EXTRA_SCREENSHOT_PATH, path);
    shareIntent.putExtra(BroadcastActionReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID);

    PendingIntent sharePendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID,
      shareIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    return new Notification.Action.Builder(
      Icon.createWithResource(context, R.drawable.ic_share), "Share",
      sharePendingIntent).build();
  }

  private static Notification.Action getDeleteAction(Context context, String path) {
    Intent deleteIntent = new Intent(context, BroadcastActionReceiver.class);
    deleteIntent.setAction(BroadcastActionReceiver.ACTION_DELETE);
    deleteIntent.putExtra(BroadcastActionReceiver.EXTRA_SCREENSHOT_PATH, path);
    deleteIntent.putExtra(BroadcastActionReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID);

    PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID,
      deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    return new Notification.Action.Builder(
      Icon.createWithResource(context, R.drawable.ic_delete), "Delete",
      deletePendingIntent).build();
  }

  private static Notification.Action getSaveAction(Context context, String path) {
    List<String> albums =
      SettingsProvider.getInstance(context).getNotificationAlbums().equals("mostUsed")
        ? AlbumsProvider.getMostUsedAlbums(context.getApplicationContext()) : AlbumsProvider.getRecentAlbums(context.getApplicationContext());

    CharSequence[] choices = albums.toArray(new CharSequence[0]);

    RemoteInput remoteInputForSaveAction = new RemoteInput.Builder(
      BroadcastActionReceiver.EXTRA_ALBUM_NAME
    )
      .setLabel("Save in / Create album...")
      .setChoices(choices)
      .build();

    Intent saveIntent = new Intent(context, BroadcastActionReceiver.class);
    saveIntent.setAction(BroadcastActionReceiver.ACTION_SAVE);
    saveIntent.putExtra(BroadcastActionReceiver.EXTRA_SCREENSHOT_PATH, path);
    saveIntent.putExtra(BroadcastActionReceiver.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID);

    PendingIntent savePendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_ID,
      saveIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    return new Notification.Action.Builder(
      Icon.createWithResource(context, R.drawable.ic_delete), "Save in...", savePendingIntent)
      .setAllowGeneratedReplies(false)
      .addRemoteInput(remoteInputForSaveAction)
      .build();
  }

  @Override
  public void onChange(boolean selfChange, @Nullable Uri uri) {
    if (uri != null) {
      String path = MediaManager.getPathFromUri(context, uri);

      sendNotification(context, path);
    }

    super.onChange(selfChange, uri);
  }
}
