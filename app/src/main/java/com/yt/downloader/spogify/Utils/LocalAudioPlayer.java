package com.yt.downloader.spogify.Utils;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import java.io.File;

public class LocalAudioPlayer {
    private static LocalAudioPlayer instance;
    private MediaPlayer mediaPlayer;
    private String currentPlayingPath = "";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable progressRunnable;

    public interface PlayerStateListener {
        void onProgressUpdate(float currentSecond, float totalDuration);
        void onStateChanged(boolean isPlaying);
        void onCompletion();
    }

    private PlayerStateListener listener;

    public static synchronized LocalAudioPlayer getInstance() {
        if (instance == null) {
            instance = new LocalAudioPlayer();
        }
        return instance;
    }

    public void setPlayerStateListener(PlayerStateListener listener) {
        this.listener = listener;
    }

    public void playFile(File audioFile) {
        if (audioFile == null || !audioFile.exists()) return;

        try {
            stop();

            currentPlayingPath = audioFile.getAbsolutePath();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            if (listener != null) {
                listener.onStateChanged(true);
            }

            startProgressTracker();

            mediaPlayer.setOnCompletionListener(mp -> {
                stopProgressTracker();
                if (listener != null) {
                    listener.onStateChanged(false);
                    listener.onCompletion();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void togglePlayPause() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            pause();
        } else {
            resume();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            stopProgressTracker();
            if (listener != null) {
                listener.onStateChanged(false);
            }
        }
    }

    public void resume() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            startProgressTracker();
            if (listener != null) {
                listener.onStateChanged(true);
            }
        }
    }

    public void seekTo(long positionMs) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo((int) positionMs);
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public String getCurrentPlayingPath() {
        return currentPlayingPath;
    }

    public void stop() {
        stopProgressTracker();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            currentPlayingPath = "";
        }
    }

    private void startProgressTracker() {
        stopProgressTracker();
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying() && listener != null) {
                    float currentSec = mediaPlayer.getCurrentPosition() / 1000f;
                    float totalDurationSec = mediaPlayer.getDuration() / 1000f;
                    listener.onProgressUpdate(currentSec, totalDurationSec);
                    handler.postDelayed(this, 500);
                }
            }
        };
        handler.post(progressRunnable);
    }

    private void stopProgressTracker() {
        if (progressRunnable != null) {
            handler.removeCallbacks(progressRunnable);
            progressRunnable = null;
        }
    }
}