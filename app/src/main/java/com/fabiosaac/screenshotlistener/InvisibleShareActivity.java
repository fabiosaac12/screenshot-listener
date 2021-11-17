package com.fabiosaac.screenshotlistener;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;

public class InvisibleShareActivity extends Activity {
  public static String EXTRA_SCREENSHOT_PATH = "EXTRA_SCREENSHOT_PATH";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();

    if (intent != null) {
      String screenshotPath = intent.getStringExtra(EXTRA_SCREENSHOT_PATH);

      if (screenshotPath != null) {
        File screenshotFile = new File(screenshotPath);
        File screenshotCacheFile;

        try {
          screenshotCacheFile = MediaManager.copyImageToCache(this, screenshotFile);
        } catch (Exception e) {
          e.printStackTrace();

          Toast.makeText(
            this,
            "Unable to create screenshot cache file",
            Toast.LENGTH_SHORT
          ).show();

          finish();

          return;
        }

        Uri screenshotUri = FileProvider.getUriForFile(
          this,
          this.getApplicationContext().getPackageName() + ".provider", 
          screenshotCacheFile
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setDataAndType(screenshotUri, getContentResolver().getType(screenshotUri));

        Intent chooser = Intent.createChooser(shareIntent, "Select");

        startActivity(chooser);

        try {
          MediaManager.deleteImage(this, screenshotFile);

          Toast.makeText(this, "Screenshot deleted successfully", Toast.LENGTH_SHORT)
            .show();
        } catch (Exception exception) {
          exception.printStackTrace();

          Toast.makeText(this, "Unable to delete the screenshot", Toast.LENGTH_SHORT)
            .show();
        }
      }
    }

    finish();
  }
}