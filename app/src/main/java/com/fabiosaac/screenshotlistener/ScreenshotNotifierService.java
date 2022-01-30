package com.fabiosaac.screenshotlistener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.annotation.Nullable;

import java.io.File;

public class ScreenshotNotifierService extends Service {
  public static final String EXTRA_ACTION = "EXTRA_ACTION";
  public static final String EXTRA_SCREENSHOT_PATH = "EXTRA_SCREENSHOT_PATH";
  public static final String ACTION_ASK_STORAGE_PERMISSION = "EXTRA_ACTION_ASK_STORAGE_PERMISSION";
  public static final String ACTION_OPEN_SCREENSHOT_WINDOW = "EXTRA_ACTION_OPEN_SCREENSHOT_WINDOW";
  private static final int FOREGROUND_SERVICE_ID = 101;
  private ScreenshotNotifier screenshotNotifier;

  @Override
  public void onCreate() {
    super.onCreate();

    HandlerThread handlerThread = new HandlerThread("screenshot_listener_thread");
    handlerThread.start();

    final Handler handler = new Handler(handlerThread.getLooper());

    screenshotNotifier = new ScreenshotNotifier(handler, this);

    getContentResolver().registerContentObserver(
      MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
      true,
      screenshotNotifier
    );

    handleStartForeground();
  }

  private void handleStartForeground() {
    Intent notificationIntent = new Intent(this, MainActivity.class);
    PendingIntent pendingIntent =
      PendingIntent.getActivity(this, 0, notificationIntent, 0);

    Notification notification =
      new Notification.Builder(this,
        MainActivity.CHANNEL_SCREENSHOT_OBSERVER_SERVICE)
        .setContentTitle("Screenshot Listener")
        .setContentText("Listening to screenshots")
        .setSmallIcon(R.drawable.app_icon)
        .setContentIntent(pendingIntent)
        .build();

    startForeground(FOREGROUND_SERVICE_ID, notification);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      String action = intent.getStringExtra(EXTRA_ACTION);

      if (action != null) {
        String screenshotPath = intent.getStringExtra(EXTRA_SCREENSHOT_PATH);

        if (action.equals(ACTION_OPEN_SCREENSHOT_WINDOW)) {
          openScreenshotWindow(screenshotPath);
        } else if (action.equals(ACTION_ASK_STORAGE_PERMISSION)) {
          startPermissionsActivity(screenshotPath, InvisiblePermissionsActivity.STORAGE);
        }
      }
    }
    return START_STICKY;
  }

  private void openScreenshotWindow(String screenshotPath) {
    if (screenshotPath != null) {
      if (new File(screenshotPath).exists()) {
        if (Settings.canDrawOverlays(this)) {
          ScreenshotWindow screenshotWindow = new ScreenshotWindow(this, screenshotPath);

          screenshotWindow.open();
        } else {
          startPermissionsActivity(screenshotPath, InvisiblePermissionsActivity.CAN_DRAW_OVERLAY);
        }
      }
    }
  }

  private void startPermissionsActivity(String screenshotPath, int permission) {
    Intent permissionsActivityIntent = new Intent(this, InvisiblePermissionsActivity.class);
    permissionsActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    permissionsActivityIntent.putExtra(InvisiblePermissionsActivity.EXTRA_PERMISSION_CODE, permission);
    permissionsActivityIntent.putExtra(InvisiblePermissionsActivity.EXTRA_SCREENSHOT_PATH,
      screenshotPath);

    this.startActivity(permissionsActivityIntent);
  }

  @Override
  public void onDestroy() {
    try {
      getContentResolver().unregisterContentObserver(screenshotNotifier);
    } catch (Exception ignored) {
    }

    screenshotNotifier = null;

    NotificationManager notificationManager =
      this.getSystemService(NotificationManager.class);

    notificationManager.cancelAll();

    Process.killProcess(Process.myPid());
  }

  public static void handleStart(Context _context, @Nullable String path) {
    Context context = _context.getApplicationContext();

    if (SettingsProvider.getInstance(context).getServiceEnabled()) {
      Intent intent = new Intent(context, ScreenshotNotifierService.class);
      intent.putExtra(EXTRA_SCREENSHOT_PATH, path);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent);
      } else {
        context.startService(intent);
      }
    }
  }

  public static void handleStop(Context context) {
    context.stopService(new Intent(context, ScreenshotNotifierService.class));
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
