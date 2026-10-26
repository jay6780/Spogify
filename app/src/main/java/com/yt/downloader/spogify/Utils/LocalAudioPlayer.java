
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
        if (audioFile == null || !audioFile.exists() || !audioFile.isFile()) return;

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

            mediaPlayer.setOnCompletionListener(mp -> {
                stopProgressTracker();

                if (listener != null) {
                    listener.onStateChanged(false);
                    listener.onCompletion();
                }
            });

            mediaPlayer.start();

            if (listener != null) {
                listener.onStateChanged(true);
            }

            startProgressTracker();

        } catch (Exception e) {
            e.printStackTrace();
            stop();

            if (listener != null) {
                listener.onStateChanged(false);
            }
        }
    }

    public void togglePlayPause() {
        if (mediaPlayer == null) return;

        try {
            if (mediaPlayer.isPlaying()) {
                pause();
            } else {
                resume();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (mediaPlayer == null) return;

        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                stopProgressTracker();

                notifyProgress();

                if (listener != null) {
                    listener.onStateChanged(false);
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        if (mediaPlayer == null) return;

        try {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();

                if (listener != null) {
                    listener.onStateChanged(true);
                }

                startProgressTracker();
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void seekTo(long positionMs) {
        if (mediaPlayer == null) return;

        try {
            int duration = mediaPlayer.getDuration();

            int target = (int) Math.max(
                    0,
                    Math.min(positionMs, duration)
            );

            MediaPlayer player = mediaPlayer;

            player.setOnSeekCompleteListener(mp -> {
                if (mp == mediaPlayer) {
                    notifyProgress();
                }
            });

            player.seekTo(target);

        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public boolean isPlaying() {
        if (mediaPlayer == null) return false;

        try {
            return mediaPlayer.isPlaying();
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public String getCurrentPlayingPath() {
        return currentPlayingPath;
    }

    public void stop() {
        stopProgressTracker();

        if (mediaPlayer != null) {
            MediaPlayer oldPlayer = mediaPlayer;
            mediaPlayer = null;

            try {
                oldPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        currentPlayingPath = "";
    }

    private void notifyProgress() {
        if (mediaPlayer == null || listener == null) return;

        try {
            float currentSec = mediaPlayer.getCurrentPosition() / 1000f;
            float totalDurationSec = mediaPlayer.getDuration() / 1000f;

            listener.onProgressUpdate(currentSec, totalDurationSec);

        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private void startProgressTracker() {
        stopProgressTracker();

        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer == null) return;

                notifyProgress();

                if (mediaPlayer != null && isPlaying()) {
                    handler.postDelayed(this, 100);
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
