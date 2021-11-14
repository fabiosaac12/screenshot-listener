package com.fabiosaac.screenshotlistener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.io.File;

public class ScreenshotWindow {
  private final Context context;
  private final WindowManager windowManager;
  private final View container;
  private final WindowManager.LayoutParams windowParams;
  private final String screenshotPath;
  private final DisplayMetrics displayMetrics;

  public ScreenshotWindow(Context context, String screenshotPath) {
    Context themedContext = new ContextThemeWrapper(context, R.style.Theme_ScreenshotListener);
    this.context = themedContext;

    windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

    container = LayoutInflater.from(themedContext).inflate(R.layout.screenshot_window, null);

    this.screenshotPath = screenshotPath;

    windowParams = new WindowManager.LayoutParams(0, 0, 0, 0,
      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
      WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
      PixelFormat.TRANSLUCENT);

    displayMetrics = new DisplayMetrics();
    windowManager.getDefaultDisplay().getMetrics(displayMetrics);

    initializeWindowsParams();
    initializeWindow();
  }

  private void initializeWindowsParams() {
    windowParams.gravity = Gravity.TOP | Gravity.LEFT;
    windowParams.width = displayMetrics.widthPixels;
    windowParams.height = displayMetrics.heightPixels;
    windowParams.x = 0;
    windowParams.y = 0;
  }

  private void initializeWindow() {
    MaterialCardView cardView = container.findViewById(R.id.card);
    ImageButton closeButton = container.findViewById(R.id.closeButton);
    ImageView screenshotImageView = container.findViewById(R.id.screenshotImageView);
    File imageFile = new File(screenshotPath);
    RecyclerView albumList = container.findViewById(R.id.albumList);

    cardView.getLayoutParams().width = (int) (windowParams.width * 0.9);

    albumList.setAdapter(new AlbumListAdapter(context));
    albumList.setLayoutManager(
      new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    );

    Log.d("        screenshot path", screenshotPath);

    if(imageFile.exists()){
      Bitmap myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
      screenshotImageView.setImageBitmap(myBitmap);
    }

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
