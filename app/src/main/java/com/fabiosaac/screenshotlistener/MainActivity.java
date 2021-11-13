package com.fabiosaac.screenshotlistener;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

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
}