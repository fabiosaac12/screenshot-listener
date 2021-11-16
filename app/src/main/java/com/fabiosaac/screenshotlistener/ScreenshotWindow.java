package com.fabiosaac.screenshotlistener;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
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
      WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
      PixelFormat.TRANSLUCENT);

    displayMetrics = new DisplayMetrics();
    windowManager.getDefaultDisplay().getMetrics(displayMetrics);

    initializeWindowsParams();
    initializeWindow();
  }

  private void initializeWindowsParams() {
    windowParams.gravity = Gravity.TOP | Gravity.LEFT;
    windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
    windowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
    windowParams.x = 0;
    windowParams.y = 0;
    windowParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
  }

  private void initializeWindow() {
    // Initialization of layout elements
    MaterialCardView cardView = container.findViewById(R.id.card);
    ImageButton closeButton = container.findViewById(R.id.closeButton);
    ImageButton shareButton = container.findViewById(R.id.shareButton);
    RelativeLayout imageWrapper = container.findViewById(R.id.imageWrapper);
    ImageView screenshotImageView = container.findViewById(R.id.screenshotImageView);
    RecyclerView albumList = container.findViewById(R.id.albumList);
    EditText albumNameInput = container.findViewById(R.id.albumNameInput);
    MaterialButton saveButton = container.findViewById(R.id.saveButton);

    // Album Name Input text changed event
    albumNameInput.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
      }

      @Override
      public void afterTextChanged(Editable editable) {
        String text = editable.toString();

        saveButton.setEnabled(text.length() > 0 && text.trim().equals(text));
      }
    });

    // Save Button event to save image
    saveButton.setOnClickListener(button -> handleSaveImage(albumNameInput.getText().toString()));

    // Share Button event to share image
    shareButton.setOnClickListener(button -> {
      Intent shareActivityIntent = new Intent(context, InvisibleShareActivity.class);
      shareActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      shareActivityIntent.putExtra(InvisibleShareActivity.SCREENSHOT_PATH_KEY, screenshotPath);

      context.startActivity(shareActivityIntent);
      close();
    });

    // Card View width depending on device's screen width
    cardView.getLayoutParams().width = (int) Math.min(
      (displayMetrics.widthPixels * 0.95),
      (400) * displayMetrics.density
    );

    // Setting Album Recycler View adapter and layout manager
    albumList.setAdapter(new AlbumListAdapter(context, this::handleSaveImage));
    albumList.setLayoutManager(
      new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    );

    // Setting the screenshot in the ImageView
    File imageFile = new File(screenshotPath);

    if (imageFile.exists()) {
      Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

      screenshotImageView.setImageBitmap(bitmap);
    }

    // Close Button event to close the window
    closeButton.setOnClickListener(view -> close());
  }

  public void handleSaveImage(String albumName) {
    try {
      MediaManager
        .saveImageInAlbum(context, new File(screenshotPath), albumName);
      MediaManager.deleteImage(context, new File(screenshotPath));

      Toast.makeText(context, "Screenshot saved successfully", Toast.LENGTH_SHORT).show();
    } catch (Exception exception) {
      exception.printStackTrace();

      Toast.makeText(context, "An error has occurred :c", Toast.LENGTH_SHORT).show();
    }

    close();
  }

  public void open() {
    try {
      windowManager.addView(container, windowParams);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public void close() {
    try {
      windowManager.removeView(container);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
