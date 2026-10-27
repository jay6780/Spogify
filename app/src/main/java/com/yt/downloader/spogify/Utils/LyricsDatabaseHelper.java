package com.yt.downloader.spogify.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.Locale;

public class LyricsDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "lyrics_cache.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_LYRICS = "lyrics";

    private static final String COL_ID = "id";
    private static final String COL_ARTIST = "artist_name";
    private static final String COL_MUSIC = "music_name";
    private static final String COL_LYRICS = "synced_lyrics";
    private static final String COL_DURATION = "duration";

    public LyricsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_LYRICS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ARTIST + " TEXT NOT NULL, "
                + COL_MUSIC + " TEXT NOT NULL, "
                + COL_LYRICS + " TEXT NOT NULL, "
                + COL_DURATION + " REAL DEFAULT 0, "
                + "UNIQUE(" + COL_ARTIST + ", " + COL_MUSIC + ")"
                + ")";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Add future migrations here without deleting saved lyrics.
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public boolean saveLyrics(String artistName, String musicName, String syncedLyrics, double duration) {

        if (TextUtils.isEmpty(musicName) || TextUtils.isEmpty(syncedLyrics)) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_ARTIST, normalize(artistName));
        values.put(COL_MUSIC, normalize(musicName));
        values.put(COL_LYRICS, syncedLyrics);
        values.put(COL_DURATION, duration);

        long result = db.insertWithOnConflict(
                TABLE_LYRICS,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );

        return result != -1;
    }

    public CachedLyrics getLyrics(String artistName, String musicName) {

        if (TextUtils.isEmpty(musicName)) {
            return null;
        }

        SQLiteDatabase db = getReadableDatabase();

        String selection = COL_ARTIST + " = ? AND " + COL_MUSIC + " = ?";

        String[] selectionArgs = {
                normalize(artistName),
                normalize(musicName)
        };

        try (Cursor cursor = db.query(
                TABLE_LYRICS,
                new String[]{COL_LYRICS, COL_DURATION},
                selection,
                selectionArgs,
                null,
                null,
                null
        )) {

            if (cursor != null && cursor.moveToFirst()) {

                String lyrics = cursor.getString(0);
                double duration = cursor.getDouble(1);

                if (!TextUtils.isEmpty(lyrics)) {
                    return new CachedLyrics(lyrics, duration);
                }
            }
        }

        return null;
    }

    public boolean hasLyrics(String artistName, String musicName) {
        return getLyrics(artistName, musicName) != null;
    }

    public boolean deleteLyrics(String artistName, String musicName) {

        SQLiteDatabase db = getWritableDatabase();

        int result = db.delete(
                TABLE_LYRICS,
                COL_ARTIST + " = ? AND " + COL_MUSIC + " = ?",
                new String[]{
                        normalize(artistName),
                        normalize(musicName)
                }
        );

        return result > 0;
    }

    public static class CachedLyrics {

        public final String syncedLyrics;
        public final double duration;

        public CachedLyrics(String syncedLyrics, double duration) {
            this.syncedLyrics = syncedLyrics;
            this.duration = duration;
        }
    }

    public CachedLyrics getLyricsByMusicName(String musicName) {

        if (TextUtils.isEmpty(musicName)) {
            return null;
        }

        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(
                TABLE_LYRICS,
                new String[]{COL_LYRICS, COL_DURATION},
                COL_MUSIC + " = ?",
                new String[]{normalize(musicName)},
                null,
                null,
                null
        )) {

            if (cursor != null && cursor.moveToFirst()) {

                String lyrics = cursor.getString(0);
                double duration = cursor.getDouble(1);

                if (cursor.moveToNext()) {
                    return null;
                }

                if (!TextUtils.isEmpty(lyrics)) {
                    return new CachedLyrics(lyrics, duration);
                }
            }
        }

        return null;
    }
}