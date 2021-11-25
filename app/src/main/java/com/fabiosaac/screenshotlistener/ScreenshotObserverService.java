package com.fabiosaac.screenshotlistener;

import android.app.Notification;
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
import android.util.Log;

import androidx.annotation.Nullable;

public class ScreenshotObserverService extends Service {
  public static final String EXTRA_SCREENSHOT_PATH = "EXTRA_SCREENSHOT_PATH";
  private static final int FOREGROUND_SERVICE_ID = 101;
  private ScreenshotObserver screenshotObserver;

  @Override
  public void onCreate() {
    super.onCreate();

    HandlerThread handlerThread = new HandlerThread("screenshot_listener_thread");
    handlerThread.start();

    final Handler handler = new Handler(handlerThread.getLooper());

    screenshotObserver = new ScreenshotObserver(handler, this);

    getContentResolver().registerContentObserver(
      MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
      true,
      screenshotObserver
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
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentIntent(pendingIntent)
        .build();

    startForeground(FOREGROUND_SERVICE_ID, notification);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      String path = intent.getStringExtra(EXTRA_SCREENSHOT_PATH);

      if (path != null) {
        ScreenshotWindow screenshotWindow = new ScreenshotWindow(this, path);

        screenshotWindow.open();
      }
    }

    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    try {
      getContentResolver().unregisterContentObserver(screenshotObserver);
    } catch (Exception ignored) {
    }

    screenshotObserver = null;

    Process.killProcess(Process.myPid());
  }

  public static void handleStart(Context _context, @Nullable String path) {
    Context context = _context.getApplicationContext();

    if (SettingsProvider.getInstance(context).getServiceEnabled()) {
      Intent intent = new Intent(context, ScreenshotObserverService.class);
      intent.putExtra(EXTRA_SCREENSHOT_PATH, path);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent);
      } else {
        context.startService(intent);
      }
    }
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
