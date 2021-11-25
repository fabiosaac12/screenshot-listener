package com.fabiosaac.screenshotlistener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SettingsProvider {
  private static final String PREF_FILE = "com.fabiosaac.screenshotlistener.PREF_SETTINGS_FILE";
  private static final String PREF_SERVICE_ENABLED = "PREF_SERVICE_ENABLED";
  private static final String PREF_NOTIFICATION_DISABLED = "PREF_NOTIFICATION_DISABLED";
  private static final String PREF_DELETE_ON_SHARE = "PREF_DELETE_ON_SHARE";
  private static final String PREF_DELETE_ON_SAVE = "PREF_DELETE_ON_SAVE";
  private static final String PREF_ACCUMULATE_NOTIFICATIONS = "PREF_ACCUMULATE_NOTIFICATIONS";
  private static final String PREF_NOTIFICATION_ALBUMS = "PREF_NOTIFICATION_ALBUMS";

  private final SharedPreferences sharedPreferences;
  private final SharedPreferences.Editor editor;

  private static SettingsProvider instance;

  @SuppressLint("CommitPrefEdits")
  private SettingsProvider(Context context) {
    this.sharedPreferences =
      context.getApplicationContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
    this.editor = this.sharedPreferences.edit();
  }

  public static synchronized SettingsProvider getInstance(Context context) {
    if (instance == null) {
      instance = new SettingsProvider(context.getApplicationContext());
    }

    return instance;
  }

  public boolean getServiceEnabled() {
    return sharedPreferences.getBoolean(PREF_SERVICE_ENABLED, true);
  }

  public boolean setServiceEnabled(boolean value) {
    try {
      editor.putBoolean(PREF_SERVICE_ENABLED, value);
      editor.apply();

      return true;
    } catch (Exception ignored) {
      return false;
    }
  }

  public boolean getNotificationDisabled() {
    return sharedPreferences.getBoolean(PREF_NOTIFICATION_DISABLED, false);
  }

  public boolean setNotificationDisabled(boolean value) {
    try {
      editor.putBoolean(PREF_NOTIFICATION_DISABLED, value);
      editor.apply();

      return true;
    } catch (Exception ignored) {
      return false;
    }
  }

  public boolean getDeleteOnShare() {
    return sharedPreferences.getBoolean(PREF_DELETE_ON_SHARE, true);
  }

  public boolean setDeleteOnShare(boolean value) {
    try {
      editor.putBoolean(PREF_DELETE_ON_SHARE, value);
      editor.apply();

      return true;
    } catch (Exception ignored) {
      return false;
    }
  }

  public boolean getDeleteOnSave() {
    return sharedPreferences.getBoolean(PREF_DELETE_ON_SAVE, true);
  }

  public boolean setDeleteOnSave(boolean value) {
    try {
      editor.putBoolean(PREF_DELETE_ON_SAVE, value);
      editor.apply();

      return true;
    } catch (Exception ignored) {
      return false;
    }
  }

  public boolean getAccumulateNotifications() {
    return sharedPreferences.getBoolean(PREF_ACCUMULATE_NOTIFICATIONS, true);
  }

  public boolean setAccumulateNotifications(boolean value) {
    try {
      editor.putBoolean(PREF_ACCUMULATE_NOTIFICATIONS, value);
      editor.apply();

      return true;
    } catch (Exception ignored) {
      return false;
    }
  }

  public String getNotificationAlbums() {
    return sharedPreferences.getString(PREF_NOTIFICATION_ALBUMS, "recents");
  }

  public boolean setNotificationAlbums(String value) {
    try {
      editor.putString(PREF_NOTIFICATION_ALBUMS, value);
      editor.apply();

      return true;
    } catch (Exception ignored) {
      return false;
    }
  }
}
