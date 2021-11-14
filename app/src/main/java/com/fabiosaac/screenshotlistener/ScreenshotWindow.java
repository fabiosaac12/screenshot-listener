package com.fabiosaac.screenshotlistener;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class ScreenshotWindow {
  private final WindowManager windowManager;
  private final LayoutInflater layoutInflater;
  private final View container;
  private final WindowManager.LayoutParams windowParams;

  public ScreenshotWindow(Context context) {
    windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    container = layoutInflater.inflate(R.layout.activity_main, null);

    windowParams = new WindowManager.LayoutParams(0, 0, 0, 0,
      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
      WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
      PixelFormat.TRANSLUCENT);

    initializeWindowsParams();
    initializeWindows();
  }

  private void initializeWindowsParams() {
    DisplayMetrics displayMetrics = new DisplayMetrics();
    windowManager.getDefaultDisplay().getMetrics(displayMetrics);

    windowParams.gravity = Gravity.TOP | Gravity.LEFT;
    windowParams.width = (int) (displayMetrics.widthPixels * 0.9);
    windowParams.height = (int) (displayMetrics.heightPixels * 0.5);
    windowParams.x = (int) (displayMetrics.widthPixels * 0.05);
    windowParams.y = (int) (displayMetrics.heightPixels * 0.25);
  }

  private void initializeWindows() {
    Button closeButton = container.findViewById(R.id.closeButton);

    closeButton.setOnClickListener(this::close);
  }

  public void open() {
    try {
      windowManager.addView(container, windowParams);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public void close(View view) {
    try {
      windowManager.removeView(container);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
