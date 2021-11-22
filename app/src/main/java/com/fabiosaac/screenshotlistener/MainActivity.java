package com.fabiosaac.screenshotlistener;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

public class MainActivity extends FragmentActivity {
  public static final String NOTIFICATION_SCREENSHOT_OBSERVER_SERVICE_CHANNEL_ID =
    "screenshot_observer_service_notification_channel_id";
  public static final String NOTIFICATION_NEW_SCREENSHOT_CHANNEL_ID =
    "new_screenshot_notification_channel_id";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    createNotificationChannels();

    startScreenshotObserverService();
  }

  private void startScreenshotObserverService() {
    Intent screenshotObserverService = new Intent(getBaseContext(),
      ScreenshotObserverService.class);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      getBaseContext().startForegroundService(screenshotObserverService);
    } else {
      getBaseContext().startService(screenshotObserverService);
    }
  }

  private void createNotificationChannels() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      NotificationManager notificationManager = this.getSystemService(NotificationManager.class);

      // SCREENSHOT OBSERVER SERVICE NOTIFICATION CHANNEL
      String screenshotObserverServiceChannelId =
        "screenshot_observer_service_notification_channel_id";
      String screenshotObserverServiceChannelName = "Screenshot Observer Service";

      NotificationChannel screenshotObserverServiceChannel = new NotificationChannel(
        screenshotObserverServiceChannelId, screenshotObserverServiceChannelName,
        NotificationManager.IMPORTANCE_MIN);

      screenshotObserverServiceChannel.setSound(null, null);
      screenshotObserverServiceChannel.setVibrationPattern(null);

      // NEW SCREENSHOT NOTIFICATION CHANNEL
      String newScreenshotChannelId = "new_screenshot_notification_channel_id";
      String newScreenshotChannelName = "New Screenshot";

      NotificationChannel newScreenshotChannel = new NotificationChannel(
        newScreenshotChannelId, newScreenshotChannelName, NotificationManager.IMPORTANCE_DEFAULT);

      newScreenshotChannel.setSound(null, null);
      newScreenshotChannel.setVibrationPattern(null);

      // CREATING CHANNELS
      notificationManager.createNotificationChannel(newScreenshotChannel);
      notificationManager.createNotificationChannel(screenshotObserverServiceChannel);
    }
  }
}