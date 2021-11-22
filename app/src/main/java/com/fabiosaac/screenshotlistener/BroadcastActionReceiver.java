package com.fabiosaac.screenshotlistener;

import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;

public class BroadcastActionReceiver extends BroadcastReceiver {
  public static final String ACTION_SHARE = "com.fabiosaac.action.SHARE";
  public static final String ACTION_DELETE = "com.fabiosaac.action.DELETE";
  public static final String ACTION_SAVE = "com.fabiosaac.action.SAVE";

  public static final String EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID";
  public static final String EXTRA_SCREENSHOT_PATH = "EXTRA_SCREENSHOT_PATH";
  public static final String EXTRA_ALBUM_NAME = "EXTRA_ALBUM_NAME";

  @Override
  public void onReceive(Context context, Intent intent) {
    switch (intent.getAction()) {
      case Intent.ACTION_BOOT_COMPLETED:
        startScreenshotObserverService(context);
        break;
      case ACTION_SHARE:
        shareScreenshot(context, intent);
        break;
      case ACTION_DELETE:
        deleteScreenshot(context, intent);
        break;
      case ACTION_SAVE:
        saveScreenshot(context, intent);
        break;
      default:
        break;
    }

    clearNotification(context, intent);
  }

  private void saveScreenshot(Context context, Intent intent) {
    Bundle remoteInputForAlbumName = RemoteInput.getResultsFromIntent(intent);
    String screenshotPath = getScreenshotPathFromIntent(intent);

    if (remoteInputForAlbumName != null && screenshotPath != null) {
      String albumName = String.valueOf(remoteInputForAlbumName.getCharSequence(EXTRA_ALBUM_NAME));

      if (albumName.length() > 1) {
        albumName = albumName.trim();
      }

      try {
        MediaManager
          .saveImageInAlbum(context, new File(screenshotPath), albumName);
        MediaManager.deleteImage(context, new File(screenshotPath));

        Toast.makeText(context, "Screenshot saved successfully", Toast.LENGTH_SHORT).show();

        AlbumsProvider.updateSharedPreferencesWithUsedAlbum(context, albumName);
      } catch (Exception exception) {
        exception.printStackTrace();

        Toast.makeText(context, "An error has occurred :c", Toast.LENGTH_SHORT).show();
      }
    }
  }

  private void shareScreenshot(Context context, Intent intent) {
    String screenshotPath = getScreenshotPathFromIntent(intent);

    if (screenshotPath != null) {
      Intent shareActivityIntent = new Intent(context, InvisibleShareActivity.class);
      shareActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      shareActivityIntent.putExtra(InvisibleShareActivity.EXTRA_SCREENSHOT_PATH, screenshotPath);

      context.startActivity(shareActivityIntent);

      collapseNotificationsPanel(context);
    }
  }

  private void deleteScreenshot(Context context, Intent intent) {
    String screenshotPath = getScreenshotPathFromIntent(intent);

    if (screenshotPath != null) {
      try {
        MediaManager.deleteImage(context, new File(screenshotPath));

        Toast.makeText(context, "Screenshot deleted successfully", Toast.LENGTH_SHORT).show();
      } catch (Exception exception) {
        exception.printStackTrace();

        Toast.makeText(context, "Unable to delete screenshot", Toast.LENGTH_SHORT).show();
      }
    }
  }

  private String getScreenshotPathFromIntent(Intent intent) {
    if (intent != null) {
      return intent.getStringExtra(EXTRA_SCREENSHOT_PATH);
    }

    return null;
  }

  private void clearNotification(Context context, Intent intent) {
    if (intent != null) {
      NotificationManager notificationManager =
        context.getSystemService(NotificationManager.class);

      int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);

      if (notificationId != -1) {
        notificationManager.cancel(notificationId);
      }
    }
  }

  private void collapseNotificationsPanel(Context context) {
    Intent closeNotificationsPanelIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    context.sendBroadcast(closeNotificationsPanelIntent);
  }

  private void startScreenshotObserverService(Context context) {
    Intent screenshotObserverService = new Intent(context, ScreenshotObserverService.class);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      context.startForegroundService(screenshotObserverService);
    } else {
      context.startService(screenshotObserverService);
    }
  }
}