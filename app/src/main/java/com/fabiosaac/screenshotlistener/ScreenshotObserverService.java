package com.fabiosaac.screenshotlistener;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

public class ScreenshotObserverService extends Service {
  private static final int ONGOING_NOTIFICATION_ID = 101;
  private ScreenshotObserver screenshotObserver;

  public static boolean running = false;

  @Override
  public void onCreate() {
    super.onCreate();

    running = true;

    HandlerThread handlerThread = new HandlerThread("screenshot_listener_thread");
    handlerThread.start();

    final Handler handler = new Handler(handlerThread.getLooper());

    screenshotObserver = new ScreenshotObserver(handler);

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
      new Notification.Builder(this, createNotificationChannel())
        .setContentTitle("Screenshot Listener")
        .setContentText("Listening to screenshots")
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentIntent(pendingIntent)
        .setTicker("Text for ticker idk")
        .build();

    startForeground(ONGOING_NOTIFICATION_ID, notification);
  }

  private String createNotificationChannel() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

      NotificationManager notificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

      String channelId = "screenshot_observer_service_notification_channel_id";
      String channelName = "Screenshot Listener";

      NotificationChannel channel =
        new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);

      channel.setImportance(NotificationManager.IMPORTANCE_NONE);
      channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

      notificationManager.createNotificationChannel(channel);

      return channelId;
    } else {
      return "";
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    try {
      getContentResolver().unregisterContentObserver(screenshotObserver);
    } catch (Exception ignored) {
    }

    screenshotObserver = null;
    running = false;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
