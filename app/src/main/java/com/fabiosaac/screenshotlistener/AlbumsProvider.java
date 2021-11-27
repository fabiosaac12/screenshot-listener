package com.fabiosaac.screenshotlistener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.MediaStore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class AlbumsProvider {
  private static final String PREF_FILE = "com.fabiosaac.screenshotlistener.PREF_ALBUMS_FILE";
  private static final String PREF_RECENT_ALBUMS = "PREF_RECENT_ALBUMS";
  private static final String PREF_ALBUMS_USAGE = "PREF_ALBUMS_USAGE";

  public static synchronized void updateSharedPreferencesForRecentAlbums(
    Context context,
    SharedPreferences.Editor editor,
    Gson gson,
    String album
  ) {
    ArrayList<String> recentAlbums = getRecentAlbums(context);
    ArrayList<String> newRecentAlbums = new ArrayList<>();

    newRecentAlbums.add(album);

    if (!recentAlbums.contains(album)) {
      for (int i = 0; i < Math.min(4, recentAlbums.size()); i++) {
        newRecentAlbums.add(recentAlbums.get(i));
      }
    } else {
      for (String recentAlbum : recentAlbums) {
        if (!album.equals(recentAlbum)) {
          newRecentAlbums.add(recentAlbum);
        }
      }
    }

    editor.remove(PREF_RECENT_ALBUMS);
    editor.putString(PREF_RECENT_ALBUMS, gson.toJson(newRecentAlbums));
  }

  public static synchronized void updateSharedPreferencesForMostUsedAlbums(
    Context context,
    SharedPreferences.Editor editor,
    Gson gson,
    String album
  ) {
    Map<String, Integer> albumsUsage = getAlbumsUsage(context);

    Integer currentValue = albumsUsage.getOrDefault(album, 0);

    albumsUsage.put(album, currentValue == null ? 1 : currentValue + 1);

    editor.remove(PREF_ALBUMS_USAGE);
    editor.putString(PREF_ALBUMS_USAGE, gson.toJson(albumsUsage));
  }

  public static synchronized void updateSharedPreferencesWithUsedAlbum(Context context, String album) {
    Gson gson = new Gson();

    SharedPreferences sharedPreferences =
      context.getApplicationContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

    SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();

    updateSharedPreferencesForRecentAlbums(context, sharedPreferencesEditor, gson, album);
    updateSharedPreferencesForMostUsedAlbums(context, sharedPreferencesEditor, gson, album);

    sharedPreferencesEditor.apply();
  }

  public static ArrayList<String> getRecentAlbums(Context context) {
    Gson gson = new Gson();

    SharedPreferences sharedPreferences =
      context.getApplicationContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

    String recentAlbumsString =
      sharedPreferences.getString(PREF_RECENT_ALBUMS, null);

    if (recentAlbumsString == null) {
      return new ArrayList<>();
    }

    Type type = new TypeToken<ArrayList<String>>() {
    }.getType();

    return gson.fromJson(recentAlbumsString, type);
  }

  public static Map<String, Integer> getAlbumsUsage(Context context) {
    Gson gson = new Gson();

    SharedPreferences sharedPreferences =
      context.getApplicationContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

    String albumsUsageString =
      sharedPreferences.getString(PREF_ALBUMS_USAGE, null);

    if (albumsUsageString == null) {
      return new HashMap<>();
    }

    Type type = new TypeToken<Map<String, Integer>>() {
    }.getType();

    return gson.fromJson(albumsUsageString, type);
  }

  public static ArrayList<String> getMostUsedAlbums(Context context) {
    Map<String, Integer> albumsUsageMap = getAlbumsUsage(context);

    List<Map.Entry<String, Integer>> list = new ArrayList<>(albumsUsageMap.entrySet());
    list.sort(Map.Entry.comparingByValue());

    ArrayList<String> albumsOrderedByUsage = new ArrayList<>();

    for (Map.Entry<String, Integer> entry : list) {
      albumsOrderedByUsage.add(entry.getKey());
    }

    ArrayList<String> mostUsedAlbums = new ArrayList<>();

    for (
      int i = albumsOrderedByUsage.size() - 1;
      i >= Math.max(0, albumsOrderedByUsage.size() - 5);
      i--) {
      mostUsedAlbums.add(albumsOrderedByUsage.get(i));
    }

    return mostUsedAlbums;
  }

  @SuppressLint("InlinedApi")
  public static ArrayList<String> getAllAlbums(Context context) {
    String[] projection = new String[]{MediaStore.MediaColumns.BUCKET_DISPLAY_NAME};

    Cursor cursor = context.getContentResolver()
      .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null,
        null, null);

    HashSet<String> albumsHashSet = new HashSet<>();

    while (cursor.moveToNext()) {
      albumsHashSet.add(cursor.getString(
        (cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME))
      ));
    }

    cursor.close();

    return new ArrayList<>(albumsHashSet);
  }

  public static ArrayList<String> getAlbumList(Context context) {
    ArrayList<String> recentAlbums = getRecentAlbums(context);
    ArrayList<String> mostUsedAlbums = getMostUsedAlbums(context);
    ArrayList<String> allAlbums = getAllAlbums(context);

    ArrayList<String> albumList = new ArrayList<>(recentAlbums);

    for (String album : mostUsedAlbums) {
      if (!albumList.contains(album)) {
        albumList.add(album);
      }
    }

    for (String album : allAlbums) {
      if (!albumList.contains(album)) {
        albumList.add(album);
      }
    }

    return albumList;
  }
}
