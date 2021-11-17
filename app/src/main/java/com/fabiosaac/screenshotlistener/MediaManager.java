package com.fabiosaac.screenshotlistener;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;

public class MediaManager {

  @SuppressLint("InlinedApi")
  public static ArrayList<String> getAlbumList(Context context) {
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

  public static void refreshFileInGallery(Context context, File file) {
    MediaScannerConnection.scanFile(
      context.getApplicationContext(),
      new String[]{file.toString()},
      null,
      null
    );
  }

  public static boolean deleteImage(Context context, File file) throws Exception {
    if (file.exists()) {
      if (file.delete()) {
        refreshFileInGallery(context, file);

        return true;
      }
    }

    throw new Exception("Not able to delete image");
  }

  public static File copyImageToCache(Context context, File file) throws Exception {
    File cacheFile = File.createTempFile(
      System.currentTimeMillis() + "-temporary", ".jpg", context.getCacheDir()
    );

    OutputStream fos = new FileOutputStream(cacheFile);

    BitmapFactory.decodeFile(file.getAbsolutePath())
      .compress(Bitmap.CompressFormat.JPEG, 100, fos);

    fos.flush();
    fos.close();

    return cacheFile;
  }

  public static void saveImageInAlbum(Context context, File file, String album) throws Exception {
    String outputDirectoryPath = Environment.getExternalStoragePublicDirectory(
      Environment.DIRECTORY_DCIM
    ).getAbsolutePath() + File.separator + album;
    String newFileName = context.getString(R.string.app_name_abbreviation) + System.currentTimeMillis() + ".jpg";
    OutputStream fos;

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
      File outputDirectory = new File(outputDirectoryPath);

      if (!outputDirectory.exists()) {
        if (!outputDirectory.mkdir()) {
          throw new Exception("Can't create album");
        }
      }

      File newImage = new File(outputDirectoryPath, newFileName);

      fos = new FileOutputStream(newImage);
    } else {
      ContentResolver resolver = context.getContentResolver();

      ContentValues contentValues = new ContentValues();
      contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, newFileName);
      contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
      contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + album);

      Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

      fos = resolver.openOutputStream(imageUri);
    }

    BitmapFactory.decodeFile(file.getAbsolutePath())
      .compress(Bitmap.CompressFormat.JPEG, 100, fos);

    fos.flush();
    fos.close();

    refreshFileInGallery(context, new File(outputDirectoryPath, newFileName));
  }

  public static String getPathFromUri(Context context, Uri uri) {
    String path;

    try {
      Cursor cursor =
        context.getContentResolver()
          .query(uri, null, null, null, null);

      if (cursor == null) {
        path = uri.getPath();
      } else {
        cursor.moveToFirst();

        int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

        path = cursor.getString(index);

        cursor.close();
      }
    } catch (Exception exception) {
      path = "";
    }

    return path;
  }
}
