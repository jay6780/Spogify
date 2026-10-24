package com.yt.downloader.spogify.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yt.downloader.spogify.mvp.bean.FavoriteTrack;

import java.util.ArrayList;
import java.util.List;

public class FavoriteDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "spogify_favorites.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_FAVORITES = "favorite_tracks";
    public static final String COLUMN_TRACK_NAME = "musicName";
    public static final String COLUMN_ARTIST_NAME = "artistName";
    public static final String COLUMN_IMAGE_THUMB = "imageThumb";
    public static final String COLUMN_TOTAL_LISTENERS = "totalListeners";
    public static final String COLUMN_IS_FROM_FAVORITE = "isFromFavorite";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_FAVORITES + " (" +
                    COLUMN_TRACK_NAME + " TEXT PRIMARY KEY, " +
                    COLUMN_ARTIST_NAME + " TEXT, " +
                    COLUMN_IMAGE_THUMB + " TEXT, " +
                    COLUMN_TOTAL_LISTENERS + " TEXT, " +
                    COLUMN_IS_FROM_FAVORITE + " INTEGER);";

    public FavoriteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }

    public boolean addFavoriteTrack(String musicName, String artistName, String imageThumb, String totalListeners) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRACK_NAME, musicName);
        values.put(COLUMN_ARTIST_NAME, artistName);
        values.put(COLUMN_IMAGE_THUMB, imageThumb);
        values.put(COLUMN_TOTAL_LISTENERS, totalListeners);
        values.put(COLUMN_IS_FROM_FAVORITE, 1);

        long result = db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return result != -1;
    }

    public boolean removeFavoriteTrack(String musicName) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_FAVORITES, COLUMN_TRACK_NAME + "=?", new String[]{musicName});
        db.close();
        return rows > 0;
    }

    public boolean isTrackFavorite(String musicName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{COLUMN_TRACK_NAME},
                COLUMN_TRACK_NAME + "=?", new String[]{musicName}, null, null, null);
        boolean isFav = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        db.close();
        return isFav;
    }

    public List<FavoriteTrack> getAllFavoriteTracks() {
        List<FavoriteTrack> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FAVORITES, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String musicName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRACK_NAME));
                String artistName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTIST_NAME));
                String imageThumb = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_THUMB));
                String totalListeners = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_LISTENERS));
                list.add(new FavoriteTrack(musicName, artistName, imageThumb, totalListeners, true));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }

    public FavoriteTrack getFavoriteTrackByName(String musicName) {
        FavoriteTrack track = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_FAVORITES,
                null,
                COLUMN_TRACK_NAME + "=?",
                new String[]{musicName},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String trackName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRACK_NAME));
            String artistName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTIST_NAME));
            String imageThumb = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_THUMB));
            String totalListeners = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_LISTENERS));

            track = new FavoriteTrack(trackName, artistName, imageThumb, totalListeners, true);
            cursor.close();
        }
        db.close();
        return track;
    }
}