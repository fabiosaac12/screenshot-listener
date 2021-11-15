package com.fabiosaac.screenshotlistener;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {

  @SuppressLint("UnsafeProtectedBroadcastReceiver")
  @Override
  public void onReceive(Context context, Intent intent) {
    startScreenshotObserverService(context);
  }

  private static void startScreenshotObserverService(Context context) {
    Intent screenshotObserverService = new Intent(context, ScreenshotObserverService.class);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      context.startForegroundService(screenshotObserverService);
    } else {
      context.startService(screenshotObserverService);
    }
  }
}