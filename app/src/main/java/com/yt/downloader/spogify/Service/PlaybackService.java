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
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.yt.downloader.R;
import com.yt.downloader.spogify.ui.activity.TrackInfoActivity;
import com.yt.downloader.spogify.ui.activity.VideoWatchActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlaybackService extends Service {
    public static final String ACTION_START = "com.yt.downloader.action.START";
    public static final String ACTION_STATE = "com.yt.downloader.action.STATE";
    public static final String ACTION_PLAY = "com.yt.downloader.action.PLAY";
    public static final String ACTION_PAUSE = "com.yt.downloader.action.PAUSE";
    public static final String ACTION_NEXT = "com.yt.downloader.action.NEXT";
    public static final String ACTION_SEEK = "com.yt.downloader.action.SEEK";

    public static final String EXTRA_IS_PLAYING = "isPlaying";
    public static final String EXTRA_CURRENT_POSITION = "currentPosition";
    public static final String EXTRA_DURATION = "duration";
    public static final String EXTRA_TARGET_ACTIVITY = "targetActivity";

    private static final String CHANNEL_ID = "yt_playback";
    private static final int NOTIF_ID = 101;

    private String title = "Spogify";
    private String videoId = "";
    private volatile Bitmap thumbnail;
    private String musicPic = "";
    private String targetActivityClassName = "";
    private volatile boolean isPlaying = true;
    private volatile boolean started = false;
    private long currentPositionMs = 0;
    private long durationMs = 0;

    private MediaSessionCompat mediaSession;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSession = new MediaSessionCompat(this, "PlaybackService");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                sendBroadcast(new Intent(ACTION_PLAY).setPackage(getPackageName()));
            }

            @Override
            public void onPause() {
                sendBroadcast(new Intent(ACTION_PAUSE).setPackage(getPackageName()));
            }

            @Override
            public void onSkipToNext() {
                sendBroadcast(new Intent(ACTION_NEXT).setPackage(getPackageName()));
            }

            @Override
            public void onSeekTo(long pos) {
                Intent intent = new Intent(ACTION_SEEK).setPackage(getPackageName());
                intent.putExtra(EXTRA_CURRENT_POSITION, pos);
                sendBroadcast(intent);
            }
        });

        mediaSession.setActive(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // ALWAYS call startForeground FIRST before doing any logic
        createChannel();
        if (thumbnail == null) {
            try {
                thumbnail = BitmapFactory.decodeResource(getResources(), R.mipmap.app_logo);
            } catch (Exception ignored) {
            }
        }

        Notification initialNotif = buildNotification();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIF_ID, initialNotif, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
            } else {
                startForeground(NOTIF_ID, initialNotif);
            }
        } catch (Exception e) {
            android.util.Log.e("PlaybackService", "Failed startForeground", e);
        }

        String action = intent != null ? intent.getAction() : null;

        if (ACTION_STATE.equals(action)) {
            if (intent != null) {
                isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, isPlaying);
                currentPositionMs = intent.getLongExtra(EXTRA_CURRENT_POSITION, currentPositionMs);
                long newDurationMs = intent.getLongExtra(EXTRA_DURATION, durationMs);

                if (newDurationMs > 0 && newDurationMs != durationMs) {
                    durationMs = newDurationMs;
                    updateMediaSessionMetadata();
                }
            }
            updateMediaSessionState();
            updateNotification();
            return START_NOT_STICKY;
        }

        if (intent != null) {
            String t = intent.getStringExtra("title");
            String v = intent.getStringExtra("videoId");
            String p = intent.getStringExtra("thumbnail");
            if (p == null || p.isEmpty()) {
                p = intent.getStringExtra("tumbnail");
            }

            String target = intent.getStringExtra(EXTRA_TARGET_ACTIVITY);
            if (target != null && !target.isEmpty()) {
                targetActivityClassName = target;
            }

            title = (t == null || t.isEmpty()) ? "Playing Track" : t;
            videoId = v == null ? "" : v;
            musicPic = p == null ? "" : p;
            currentPositionMs = 0;
            durationMs = 0;

            updateMediaSessionActivity();
        }

        started = true;
        updateMediaSessionMetadata();
        updateMediaSessionState();
        updateNotification();

        loadThumbnail();
        return START_NOT_STICKY;
    }

    private Class<?> getTargetActivityClass() {
        if (TrackInfoActivity.class.getName().equals(targetActivityClassName)) {
            return TrackInfoActivity.class;
        }
        return VideoWatchActivity.class;
    }

    private void updateMediaSessionActivity() {
        if (mediaSession == null) return;

        Class<?> targetActivity = getTargetActivityClass();

        Intent openIntent = new Intent(this, targetActivity)
                .putExtra("videoId", videoId)
                .putExtra("title", title)
                .putExtra("thumbnail", musicPic)
                .putExtra("tumbnail", musicPic)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        PendingIntent sessionIntent = PendingIntent.getActivity(
                this,
                0,
                openIntent,
                PendingIntent.FLAG_CANCEL_CURRENT | immutableFlag()
        );

        mediaSession.setSessionActivity(sessionIntent);
    }

    private void updateMediaSessionMetadata() {
        if (mediaSession == null) return;
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Spogify")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs);

        if (thumbnail != null) {
            builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, thumbnail);
        }
        mediaSession.setMetadata(builder.build());
    }

    private void updateMediaSessionState() {
        if (mediaSession == null) return;
        int state = isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_SEEK_TO)
                .setState(state, currentPositionMs, isPlaying ? 1.0f : 0.0f)
                .build();

        mediaSession.setPlaybackState(playbackState);
    }

    private Notification buildNotification() {
        Class<?> targetActivity = getTargetActivityClass();

        Intent open = new Intent(this, targetActivity)
                .putExtra("videoId", videoId)
                .putExtra("title", title)
                .putExtra("thumbnail", musicPic)
                .putExtra("tumbnail", musicPic)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        PendingIntent contentPi = PendingIntent.getActivity(
                this, 0, open, PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag());

        String toggleAction = isPlaying ? ACTION_PAUSE : ACTION_PLAY;
        Intent toggle = new Intent(toggleAction).setPackage(getPackageName());
        PendingIntent togglePi = PendingIntent.getBroadcast(
                this, 1, toggle, PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag());

        Intent next = new Intent(ACTION_NEXT).setPackage(getPackageName());
        PendingIntent nextPi = PendingIntent.getBroadcast(
                this, 2, next, PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag());

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.app_logo)
                .setContentTitle(title)
                .setContentText("Spogify")
                .setLargeIcon(thumbnail)
                .setContentIntent(contentPi)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(
                        isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play,
                        isPlaying ? "Pause" : "Play",
                        togglePi)
                .addAction(android.R.drawable.ic_media_next, "Next", nextPi)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1))
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
                    if (bmp != null) {
                        thumbnail = bmp;
                        updateMediaSessionMetadata();
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
                    CHANNEL_ID, "Playback", NotificationManager.IMPORTANCE_HIGH);
            ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            ch.setShowBadge(true);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) {
                nm.createNotificationChannel(ch);
            }
        }
    }

    @Override
    public void onDestroy() {
        started = false;
        thumbnail = null;
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}