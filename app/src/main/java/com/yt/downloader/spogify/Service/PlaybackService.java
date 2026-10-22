package com.yt.downloader.spogify.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.yt.downloader.spogify.ui.activity.VideoWatchActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlaybackService extends Service {
    public static final String ACTION_START = "com.yt.downloader.action.START";
    public static final String ACTION_STATE = "com.yt.downloader.action.STATE";
    public static final String ACTION_PLAY = "com.yt.downloader.action.PLAY";
    public static final String ACTION_PAUSE = "com.yt.downloader.action.PAUSE";
    public static final String EXTRA_IS_PLAYING = "isPlaying";

    private static final String CHANNEL_ID = "yt_playback";
    private static final int NOTIF_ID = 101;

    private String title = "";
    private String videoId = "";
    private volatile Bitmap thumbnail;
    private String musicPic = "";
    private volatile boolean isPlaying = true;
    private volatile boolean started = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_STATE.equals(action)) {
            if (!started) {
                stopSelf();
                return START_NOT_STICKY;
            }
            isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, true);
            updateNotification();
            return START_NOT_STICKY;
        }

        if (intent != null) {
            String t = intent.getStringExtra("title");
            String v = intent.getStringExtra("videoId");
            String p = intent.getStringExtra("tumbnail");
            title = (t == null || t.isEmpty()) ? "Playing" : t;
            videoId = v == null ? "" : v;
            musicPic = p == null ? "" : p;
        }
        started = true;
        createChannel();

        Notification n = buildNotification();
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIF_ID, n);
        }

        loadThumbnail();
        return START_NOT_STICKY;
    }

    private Notification buildNotification() {
        // Tap the notification -> back to the video screen
        Intent open = new Intent(this, VideoWatchActivity.class)
                .putExtra("videoId", videoId)
                .putExtra("title", title)
                .putExtra("tumbnail", musicPic)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent contentPi = PendingIntent.getActivity(
                this, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag());

        // Play / Pause button -> broadcast received by the activity
        String toggleAction = isPlaying ? ACTION_PAUSE : ACTION_PLAY;
        Intent toggle = new Intent(toggleAction).setPackage(getPackageName());
        PendingIntent togglePi = PendingIntent.getBroadcast(
                this, 1, toggle, PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag());

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play) // use your own icon
                .setContentTitle(title)
                .setContentText(isPlaying ? "Playing" : "Paused")
                .setLargeIcon(thumbnail)
                .setContentIntent(contentPi)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(
                        isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        isPlaying ? "Pause" : "Play",
                        togglePi)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0))
                .build();
    }

    private void updateNotification() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) nm.notify(NOTIF_ID, buildNotification());
    }

    private void loadThumbnail() {
        final String thumbUrl = !musicPic.isEmpty()
                ? musicPic
                : (videoId.isEmpty() ? "" : "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg");
        if (thumbUrl.isEmpty()) return;

        new Thread(() -> {
            HttpURLConnection c = null;
            try {
                c = (HttpURLConnection) new URL(thumbUrl).openConnection();
                c.setConnectTimeout(8000);
                c.setReadTimeout(8000);
                try (InputStream in = c.getInputStream()) {
                    Bitmap bmp = BitmapFactory.decodeStream(in);
                    if (bmp != null && started) {
                        thumbnail = bmp;
                        updateNotification();
                    }
                }
            } catch (Exception e) {
                android.util.Log.e("PlaybackService", "Thumbnail failed", e);
            } finally {
                if (c != null) c.disconnect();
            }
        }).start();
    }


    private int immutableFlag() {
        return Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Playback", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    @Override
    public void onDestroy() {
        started = false;
        thumbnail = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}