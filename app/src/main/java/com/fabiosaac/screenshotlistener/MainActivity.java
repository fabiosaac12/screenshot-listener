package com.fabiosaac.screenshotlistener;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentActivity;

public class MainActivity extends FragmentActivity {
  public static final String CHANNEL_SCREENSHOT_OBSERVER_SERVICE =
    "screenshot_observer_service_notification_channel_id";
  public static final String CHANNEL_NEW_SCREENSHOT =
    "new_screenshot_notification_channel_id";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    createNotificationChannels();

    initializeServiceSwitch();

    if (SettingsProvider.getInstance(this).getServiceEnabled()) {
      ScreenshotNotifierService.handleStart(this, null);
    }

    Window window = getWindow();
    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
      WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

    if (!Settings.canDrawOverlays(this)) {
      Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:" + getPackageName()));

      handleAskCanDrawOverlayPermission();
    }
  }

  private void handleAskCanDrawOverlayPermission() {
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          // There are no request codes
          Intent data = result.getData();

        }
      });
  }

  private void initializeServiceSwitch() {
    SwitchCompat serviceSwitch = findViewById(R.id.serviceSwitch);

    serviceSwitch.setChecked(SettingsProvider.getInstance(this).getServiceEnabled());

    serviceSwitch.setOnCheckedChangeListener(
      (view, value) -> {
        SettingsProvider.getInstance(this).setServiceEnabled(value);

        if (value) {
          ScreenshotNotifierService.handleStart(this, null);
        } else {
          ScreenshotNotifierService.handleStop(this);
        }
      });
  }

  private void createNotificationChannels() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      NotificationManager notificationManager = this.getSystemService(NotificationManager.class);

      // SCREENSHOT OBSERVER SERVICE NOTIFICATION CHANNEL
      String screenshotObserverServiceChannelName = "Screenshot Observer Service";

      NotificationChannel screenshotObserverServiceChannel = new NotificationChannel(
        CHANNEL_SCREENSHOT_OBSERVER_SERVICE, screenshotObserverServiceChannelName,
        NotificationManager.IMPORTANCE_MIN);

      screenshotObserverServiceChannel.setSound(null, null);
      screenshotObserverServiceChannel.setVibrationPattern(null);

      // NEW SCREENSHOT NOTIFICATION CHANNEL
      String newScreenshotChannelName = "New Screenshot";

      NotificationChannel newScreenshotChannel = new NotificationChannel(
        CHANNEL_NEW_SCREENSHOT, newScreenshotChannelName, NotificationManager.IMPORTANCE_DEFAULT);

      newScreenshotChannel.setSound(null, null);
      newScreenshotChannel.setVibrationPattern(null);

      // CREATING CHANNELS
      notificationManager.createNotificationChannel(newScreenshotChannel);
      notificationManager.createNotificationChannel(screenshotObserverServiceChannel);
    }
  }
}