package com.fabiosaac.screenshotlistener;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class InvisiblePermissionsActivity extends ComponentActivity {
  public static final int CAN_DRAW_OVERLAY = 1;
  public static final int STORAGE = 2;
  public static final String EXTRA_PERMISSION_CODE = "EXTRA_PERMISSION_CODE";
  public static final String EXTRA_SCREENSHOT_PATH = "EXTRA_SCREENSHOT_PATH";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();

    int permissionCode = intent.getIntExtra(EXTRA_PERMISSION_CODE, 0);

    switch (permissionCode) {
      case CAN_DRAW_OVERLAY:
        ActivityResultLauncher<Intent> canDrawOverlayPermissionLauncher = registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {}
        );

        canDrawOverlayPermissionLauncher.launch(
          new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
          Uri.parse("package:" + getPackageName()))
        );
      case STORAGE:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          requestPermissions(new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 1);
        } else {
          requestPermissions(new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
          }, 1);
        }
        break;
    }

    finish();
  }
}