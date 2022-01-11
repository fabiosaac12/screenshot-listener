package com.fabiosaac.screenshotlistener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.File;

public class ScreenshotWindow {
  private final Context context;
  private final WindowManager windowManager;
  private final ViewGroup container;
  private final WindowManager.LayoutParams windowParams;
  private final String screenshotPath;
  private final DisplayMetrics displayMetrics;

  private ScrollView scrollView;
  private MaterialCardView cardView;
  private ImageButton closeButton;
  private ImageButton shareButton;
  private ImageButton deleteButton;
  private ImageView screenshotImageView;
  private RecyclerView albumList;
  private EditText albumNameInput;
  private MaterialButton saveButton;

  @SuppressLint("InflateParams")
  public ScreenshotWindow(Context context, String screenshotPath) {
    this.context = new ContextThemeWrapper(context, R.style.Theme_ScreenshotListener);
    this.screenshotPath = screenshotPath;
    this.container = (LinearLayout)
      LayoutInflater.from(this.context).inflate(R.layout.screenshot_window, null);

    this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    this.windowParams = new WindowManager.LayoutParams(0, 0, 0, 0,
      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
      WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
      PixelFormat.TRANSLUCENT);
    this.displayMetrics = new DisplayMetrics();
    windowManager.getDefaultDisplay().getMetrics(displayMetrics);

    initializeWindowParams();
    initializeViews();
  }

  private void initializeWindowParams() {
    windowParams.gravity = Gravity.TOP | Gravity.START;
    windowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
    windowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
    windowParams.x = 0;
    windowParams.y = 0;
    windowParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
  }

  private void findViews() {
    cardView = container.findViewById(R.id.card);
    closeButton = container.findViewById(R.id.closeButton);
    shareButton = container.findViewById(R.id.shareButton);
    deleteButton = container.findViewById(R.id.deleteButton);
    screenshotImageView = container.findViewById(R.id.screenshotImageView);
    albumList = container.findViewById(R.id.albumList);
    albumNameInput = container.findViewById(R.id.albumNameInput);
    saveButton = container.findViewById(R.id.saveButton);
    scrollView = container.findViewById(R.id.scrollView);
  }

  private void setButtonOnClickListeners() {
    // Save Button event to save image
    saveButton.setOnClickListener(button -> {
      disableButtons(container);

      handleSaveImage(albumNameInput.getText().toString());
    });

    // Share Button event to share image
    shareButton.setOnClickListener(button -> {
      disableButtons(container);

      Intent shareActivityIntent = new Intent(context, InvisibleShareActivity.class);
      shareActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      shareActivityIntent.putExtra(InvisibleShareActivity.EXTRA_SCREENSHOT_PATH, screenshotPath);

      context.startActivity(shareActivityIntent);
      close();
    });

    // Close Button event to close the window
    closeButton.setOnClickListener(button -> {
      disableButtons(container);

      close();
    });

    // Close Button event to delete screenshot
    deleteButton.setOnClickListener(button -> {
      disableButtons(container);

      try {
        MediaManager.deleteImage(context, new File(screenshotPath));

        Toast.makeText(context, "Screenshot deleted successfully", Toast.LENGTH_SHORT).show();
      } catch (Exception exception) {
        exception.printStackTrace();

        Toast.makeText(context, "Unable to delete screenshot", Toast.LENGTH_SHORT).show();
      }

      close();
    });
  }

  private void initializeViews() {
    findViews();
    setButtonOnClickListeners();

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
  }

  public void handleSaveImage(String albumName) {
    disableButtons(container);

    try {
      MediaManager
        .saveImageInAlbum(context, new File(screenshotPath), albumName);

      if (SettingsProvider.getInstance(context).getDeleteOnSave()) {
        MediaManager.deleteImage(context, new File(screenshotPath));
      }

      Toast.makeText(context, "Screenshot saved successfully", Toast.LENGTH_SHORT).show();

      AlbumsProvider.updateSharedPreferencesWithUsedAlbum(context, albumName);
    } catch (Exception exception) {
      exception.printStackTrace();

      Toast.makeText(context, "An error has occurred :c", Toast.LENGTH_SHORT).show();
    }

    close();
  }

  public void disableButtons(ViewGroup container) {
    int childCount = container.getChildCount();

    for (int i = 0; i < childCount; i++) {
      View view = container.getChildAt(i);

      view.setEnabled(false);

      if (view instanceof ViewGroup) {
        disableButtons((ViewGroup) view);
      }
    }
  }

  public void open() {
    try {
      Animation slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up);
      cardView.setAnimation(slideUp);

      windowManager.addView(container, windowParams);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public void close() {
    try {
      Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);
      slideDown.setAnimationListener(new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
          windowManager.removeView(container);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
      });

      cardView.startAnimation(slideDown);
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
}
