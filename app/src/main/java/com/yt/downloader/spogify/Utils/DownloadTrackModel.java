package com.yt.downloader.spogify.Utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class DownloadTrackModel {

    public interface DownloadCallback {
        void onProgress(int progress);
        void onSuccess(String filePath);
        void onError(String error);
    }

    public static void downloadMp3File(Context context, String fileUrl, String fileName, DownloadCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;

            String cleanFileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_") + ".mp3";

            try {
                URL url = new URL(fileUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    callback.onError("Server returned HTTP " + connection.getResponseCode());
                    return;
                }

                int fileLength = connection.getContentLength();
                input = connection.getInputStream();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentResolver resolver = context.getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.Audio.Media.DISPLAY_NAME, cleanFileName);
                    contentValues.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");
                    contentValues.put(MediaStore.Audio.Media.IS_PENDING, 1);

                    // FIX: Use DIRECTORY_MUSIC instead of DIRECTORY_DCIM for audio files
                    contentValues.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/downloaded_music");

                    Uri uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);

                    if (uri != null) {
                        output = resolver.openOutputStream(uri);

                        if (output != null) {
                            byte[] data = new byte[4096];
                            long total = 0;
                            int count;

                            while ((count = input.read(data)) != -1) {
                                total += count;
                                if (fileLength > 0) {
                                    int progress = (int) (total * 100 / fileLength);
                                    callback.onProgress(progress);
                                }
                                output.write(data, 0, count);
                            }

                            output.flush();

                            // Clear IS_PENDING flag so media scanners and music apps can read the file immediately
                            contentValues.clear();
                            contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0);
                            resolver.update(uri, contentValues, null, null);

                            callback.onSuccess(cleanFileName);
                            return;
                        }
                    }
                } else {
                    // Legacy Storage (Android 9 and lower)
                    File musicFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                    File targetFolder = new File(musicFolder, "downloaded_music");
                    if (!targetFolder.exists()) {
                        targetFolder.mkdirs();
                    }

                    File musicFile = new File(targetFolder, cleanFileName);
                    output = new FileOutputStream(musicFile);

                    byte[] data = new byte[4096];
                    long total = 0;
                    int count;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        if (fileLength > 0) {
                            int progress = (int) (total * 100 / fileLength);
                            callback.onProgress(progress);
                        }
                        output.write(data, 0, count);
                    }

                    output.flush();

                    MediaScannerConnection.scanFile(context, new String[]{musicFile.getAbsolutePath()}, new String[]{"audio/mpeg"}, null);

                    callback.onSuccess(cleanFileName);
                    return;
                }

                callback.onError("Unable to write file stream.");

            } catch (Exception e) {
                callback.onError("Download failed: " + e.getLocalizedMessage());
            } finally {
                try {
                    if (output != null) output.close();
                    if (input != null) input.close();
                } catch (Exception ignored) {
                }
                if (connection != null) connection.disconnect();
            }
        });
    }
}