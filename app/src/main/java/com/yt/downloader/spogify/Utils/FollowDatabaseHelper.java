package com.yt.downloader.spogify.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.yt.downloader.spogify.mvp.bean.FollowedArtist;

import java.util.ArrayList;
import java.util.List;

public class FollowDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "spogify_follow.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_FOLLOW = "followed_artists";
    public static final String COLUMN_ARTIST_NAME = "artistsName";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_TOTAL_LISTENERS = "totalListeners";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_FOLLOW + " (" +
                    COLUMN_ARTIST_NAME + " TEXT PRIMARY KEY, " +
                    COLUMN_IMAGE + " TEXT, " +
                    COLUMN_TOTAL_LISTENERS + " TEXT);";

    public FollowDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLLOW);
        onCreate(db);
    }

    // Insert artist to follow list
    public boolean followArtist(String name, String image, String listeners) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ARTIST_NAME, name);
        values.put(COLUMN_IMAGE, image);
        values.put(COLUMN_TOTAL_LISTENERS, listeners);

        long result = db.insertWithOnConflict(TABLE_FOLLOW, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return result != -1;
    }

    // Remove artist from follow list
    public boolean unfollowArtist(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_FOLLOW, COLUMN_ARTIST_NAME + "=?", new String[]{name});
        db.close();
        return rows > 0;
    }

    // Check if artist is currently followed
    public boolean isArtistFollowed(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FOLLOW, new String[]{COLUMN_ARTIST_NAME},
                COLUMN_ARTIST_NAME + "=?", new String[]{name}, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    // Get list of all followed artists
    public List<FollowedArtist> getAllFollowedArtists() {
        List<FollowedArtist> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FOLLOW, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ARTIST_NAME));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE));
                String listeners = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_LISTENERS));
                list.add(new FollowedArtist(name, image, listeners));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }
}