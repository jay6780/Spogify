package com.yt.downloader.spogify.Utils.fileUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.app.Activity;
import android.app.RecoverableSecurityException;
import android.content.ContentUris;
import android.content.IntentSender;
import android.provider.MediaStore;
import android.database.Cursor;
import java.util.ArrayList;
import java.io.File;
import java.util.ArrayList;

public class FilesExtractor {
    private final Context context;

    public FilesExtractor(Context context) {
        this.context = context;
    }

    public ArrayList<VideoFile> listVideos() {
        ArrayList<VideoFile> videoFiles = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();
        Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        // Query all public audio tracks indexed by Android MediaStore
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = resolver.query(audioUri, null, selection, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
                int pathIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int sizeIndex = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
                int dateIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
                int relativePathIndex = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        ? cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH) : -1;

                do {
                    String fileName = nameIndex != -1 ? cursor.getString(nameIndex) : "";
                    String filePath = pathIndex != -1 ? cursor.getString(pathIndex) : "";
                    long fileSize = sizeIndex != -1 ? cursor.getLong(sizeIndex) : 0;
                    long dateModified = dateIndex != -1 ? cursor.getLong(dateIndex) : 0;
                    String relativePath = relativePathIndex != -1 ? cursor.getString(relativePathIndex) : "";

                    if (TextUtils.isEmpty(fileName)) continue;

                    // Filter tracks matching our downloaded directory or file path
                    boolean isFromDownloadedFolder = false;
                    if (!TextUtils.isEmpty(relativePath) && relativePath.contains("downloaded_music")) {
                        isFromDownloadedFolder = true;
                    } else if (!TextUtils.isEmpty(filePath) && filePath.contains("downloaded_music")) {
                        isFromDownloadedFolder = true;
                    }

                    if (isFromDownloadedFolder && fileName.toLowerCase().endsWith(".mp3")) {
                        videoFiles.add(new VideoFile(fileName, filePath, fileSize, dateModified));
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Direct File Fallback for older Android versions (Android 9 and below)
        if (videoFiles.isEmpty()) {
            File musicFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "downloaded_music");
            if (musicFolder.exists() && musicFolder.isDirectory()) {
                File[] files = musicFolder.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile() && f.getName().toLowerCase().endsWith(".mp3")) {
                            videoFiles.add(new VideoFile(f.getName(), f.getAbsolutePath(), f.length(), f.lastModified()));
                        }
                    }
                }
            }
        }

        return videoFiles;
    }

    public File getDownloadedFile(String fileName) {
        if (TextUtils.isEmpty(fileName)) return null;

        String cleanTarget = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (!cleanTarget.toLowerCase().endsWith(".mp3")) {
            cleanTarget += ".mp3";
        }

        // 1. Query MediaStore for matching file path
        ContentResolver resolver = context.getContentResolver();
        Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String selection = MediaStore.Audio.Media.DISPLAY_NAME + " = ? OR " + MediaStore.Audio.Media.TITLE + " = ?";
        String[] selectionArgs = new String[]{cleanTarget, fileName};

        try (Cursor cursor = resolver.query(audioUri, new String[]{MediaStore.Audio.Media.DATA}, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int pathIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                if (pathIndex != -1) {
                    String path = cursor.getString(pathIndex);
                    if (!TextUtils.isEmpty(path)) {
                        File f = new File(path);
                        if (f.exists() && f.length() > 0) {
                            return f;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Direct File Path Fallback
        File musicFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "downloaded_music");
        if (musicFolder.exists() && musicFolder.isDirectory()) {
            File directFile = new File(musicFolder, cleanTarget);
            if (directFile.exists() && directFile.length() > 0) {
                return directFile;
            }
        }

        return null;
    }

    public boolean isFileDownloaded(String fileName) {
        return getDownloadedFile(fileName) != null;
    }
    public Uri getDownloadedMusicUri(String fileName) {
        if (TextUtils.isEmpty(fileName)) return null;

        File downloadedFile = getDownloadedFile(fileName);
        if (downloadedFile == null) return null;

        Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DATA
        };

        try (Cursor cursor = context.getContentResolver().query(
                audioUri,
                projection,
                MediaStore.Audio.Media.DATA + " = ?",
                new String[]{downloadedFile.getAbsolutePath()},
                null)) {

            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                );

                return ContentUris.withAppendedId(audioUri, id);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}