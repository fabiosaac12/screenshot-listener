package com.fabiosaac.screenshotlistener;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

public class ScreenshotObserver extends ContentObserver {

  public ScreenshotObserver(Handler handler) {
    super(handler);
  }

  @Override
  public void onChange(boolean selfChange, @Nullable Uri uri) {
    if (uri != null) {
      Log.d("uri",uri.toString());
    }

    super.onChange(selfChange, uri);
  }
}
