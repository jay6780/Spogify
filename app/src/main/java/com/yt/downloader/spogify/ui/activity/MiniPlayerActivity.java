package com.yt.downloader.spogify.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.yt.downloader.R;
import com.yt.downloader.databinding.LayoutMiniPlayerBinding;
import com.yt.downloader.spogify.Service.PlaybackService;
import com.yt.downloader.spogify.Utils.KaraokeLyricsController;
import com.yt.downloader.spogify.Utils.LocalAudioPlayer;
import com.yt.downloader.spogify.Utils.PlayerState;
import com.yt.downloader.spogify.mvp.bean.YoutubeSearchBean;
import com.yt.downloader.spogify.mvp.contract.MusicListContract;
import com.yt.downloader.spogify.mvp.presenter.MusicListPresenter;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Base activity that owns the mini player (online YouTube + local audio).
 * Subclasses inflate their own layout, then call bindMiniPlayer(...).
 */
public abstract class MiniPlayerActivity extends AppCompatActivity {

    // Karaoke controller for the mini player lyrics view
    protected KaraokeLyricsController karaokeLyricsController;

    // Which activity is actually playing right now / was resumed last.
    private static final WeakReference<MiniPlayerActivity> NONE = new WeakReference<>(null);
    private static WeakReference<MiniPlayerActivity> playbackOwner = NONE;
    private static WeakReference<MiniPlayerActivity> lastActive = NONE;

    // ---- mini player views ----
    protected LayoutMiniPlayerBinding mini;

    // ---- online player ----
    private YouTubePlayerView miniYouTubePlayerView;
    private YouTubePlayer miniPlayer;
    private String pendingVideoId = "";
    private float pendingStartSec = 0f;

    // ---- shared state ----
    protected boolean isPlaying = false;
    protected boolean isLocalPlayback = false;
    protected String currentPlayingTitle = "";
    protected String currentPlayingVideoId = "";
    protected String currentPlayingThumb = "";
    protected float totalDuration = 0f;
    protected float currentSecond = 0f;
    private boolean receiverRegistered = false;

    // ---- queue search (used when Next hits an item that has no videoId yet) ----
    private MusicListPresenter queuePresenter;
    private PlayerState.Item pendingSearchItem;

    private final Handler miniProgressHandler = new Handler(Looper.getMainLooper());
    private String lastMiniTrackKey = "";
    private final Runnable miniProgressRunnable = new Runnable() {
        @Override
        public void run() {
            if (mini != null && PlayerState.get().hasTrack) {
                PlayerState s = PlayerState.get();

                // Tick karaoke lyrics controller on every progress loop
                if (karaokeLyricsController != null && karaokeLyricsController.hasLyrics()) {
                    karaokeLyricsController.tick(s.positionSec, s.isPlaying);
                }

                String trackKey = s.isLocal
                        ? "local:" + s.localPath
                        : "online:" + s.videoId;

                if (!TextUtils.equals(lastMiniTrackKey, trackKey)) {
                    lastMiniTrackKey = trackKey;
                    syncMiniPlayerInfo();
                    onTrackChanged();
                    applyLyricsOffset();
                }

                if (s.durationSec > 0) {
                    int progress = (int) ((s.positionSec / s.durationSec) * 1000);
                    mini.miniProgressBar.setProgress(Math.max(0, Math.min(1000, progress)));
                } else {
                    mini.miniProgressBar.setProgress(0);
                }
                if (isPlaying != s.isPlaying) {
                    isPlaying = s.isPlaying;
                    mini.ivMiniPlayPause.setImageResource(
                            isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                    onPlaybackStateUpdated(isPlaying);
                }
            }

            miniProgressHandler.postDelayed(this, 500);
        }
    };

    private final MusicListContract.View searchView = new MusicListContract.View() {
        @Override
        public void showLoading() {
        }

        @Override
        public void showError(String error) {
        }

        @Override
        public void hideLoading() {
        }

        @Override
        public void getSearchList(YoutubeSearchBean bean) {
            if (pendingSearchItem == null) return;
            if (bean == null || bean.getItems() == null || bean.getItems().isEmpty()) {
                Toast.makeText(MiniPlayerActivity.this, "Track not found on YouTube", Toast.LENGTH_SHORT).show();
                pendingSearchItem = null;
                return;
            }
            YoutubeSearchBean.ItemsBean b = bean.getItems().get(0);
            PlayerState.Item it = pendingSearchItem;
            pendingSearchItem = null;
            it.videoId = b.getId();
            it.thumb = b.getThumbnail();
            playOnline(b.getId(), b.getTitle(), it.artist, b.getThumbnail());
        }
    };

    // ---------------------------------------------------------------
    // Hooks for subclasses
    // ---------------------------------------------------------------

    /**
     * Next pressed / track ended. Default: advance the shared queue.
     */
    public void onNextTrackRequested() {
        PlayerState s = PlayerState.get();
        android.util.Log.d("NEXT", "owner=" + getClass().getSimpleName()
                + " queueSize=" + s.queue.size() + " index=" + s.queueIndex);
        playSharedQueueAt(s.queueIndex + 1);
    }

    public void chooseNextLyrics() {
    }

    /**
     * Called whenever play/pause state changes so the subclass can update its UI.
     */
    public void onPlaybackStateUpdated(boolean playing) {
    }

    /**
     * Class name used by the notification to reopen the right screen.
     */
    public String getTargetActivityName() {
        return getClass().getName();
    }

    // Updated hook to manage lyrics visibility and lines
    public void onTrackChanged() {
        PlayerState s = PlayerState.get();
        if (mini != null && mini.tvMiniLyrics != null) {
            if (s.parsedLyrics != null && !s.parsedLyrics.isEmpty()) {
                mini.tvMiniLyrics.setVisibility(View.VISIBLE);
                if (karaokeLyricsController != null) {
                    karaokeLyricsController.setLines(s.parsedLyrics);
                }
            } else {
                mini.tvMiniLyrics.setVisibility(View.GONE);
                if (karaokeLyricsController != null) {
                    karaokeLyricsController.clear();
                }
            }
        }
    }

    // ---------------------------------------------------------------
    // Ownership helpers
    // ---------------------------------------------------------------

    private boolean isAlive(MiniPlayerActivity a) {
        return a != null && !a.isDestroyed();
    }

    /**
     * True if something is loaded in THIS activity's players and this activity owns playback.
     */
    protected boolean isLoadedHere() {
        return playbackOwner.get() == this
                && (isLocalPlayback || !TextUtils.isEmpty(currentPlayingVideoId));
    }

    /**
     * Only one activity reacts to notification buttons.
     */
    private boolean isBroadcastTarget() {
        MiniPlayerActivity owner = playbackOwner.get();
        if (isAlive(owner)) return owner == this;
        MiniPlayerActivity last = lastActive.get();
        if (isAlive(last)) return last == this;
        return true;
    }

    /**
     * Called right before this activity starts playing: pause whoever played before.
     */
    private void claimPlayback() {
        MiniPlayerActivity prev = playbackOwner.get();
        if (isAlive(prev) && prev != this) {
            prev.pausePlayback();
        }
        playbackOwner = new WeakReference<>(this);
    }

    // ---------------------------------------------------------------
    // Receiver (notification controls)
    // ---------------------------------------------------------------

    private final BroadcastReceiver controlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;
            android.util.Log.d("NEXT", "2. onReceive " + intent.getAction()
                    + " in " + getClass().getSimpleName()
                    + " target=" + isBroadcastTarget());
            if (!isBroadcastTarget()) return;
            String action = intent.getAction();

            if (PlaybackService.ACTION_PLAY.equals(action)) {
                if (!isLoadedHere()) {
                    resumeFromState();
                } else if (isLocalPlayback) {
                    LocalAudioPlayer.getInstance().resume();
                } else if (miniPlayer != null) {
                    miniPlayer.play();
                }
            } else if (PlaybackService.ACTION_PAUSE.equals(action)) {
                if (isLoadedHere()) pausePlayback();
            } else if (PlaybackService.ACTION_PREVIOUS.equals(action)) {
                onPreviousTrackRequested();
            } else if (PlaybackService.ACTION_NEXT.equals(action)) {
                onNextTrackRequested();
            } else if (PlaybackService.ACTION_SEEK.equals(action)) {
                if (!isLoadedHere()) return;
                long posMs = intent.getLongExtra(PlaybackService.EXTRA_CURRENT_POSITION, 0);
                if (isLocalPlayback) {
                    LocalAudioPlayer.getInstance().seekTo(posMs);
                } else if (miniPlayer != null) {
                    miniPlayer.seekTo(posMs / 1000f);
                }
            } else if (PlaybackService.ACTION_STATE.equals(action)) {
                if (!isLoadedHere()) return;
                isPlaying = intent.getBooleanExtra(PlaybackService.EXTRA_IS_PLAYING, false);
                updateMiniPlayerPlayPauseIcon();
            }
        }
    };

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // No layout here. The subclass calls setContentView(...) and then bindMiniPlayer(...).
    }

    /**
     * Call once after setContentView with the included mini player layout binding.
     */
    public void bindMiniPlayer(LayoutMiniPlayerBinding miniBinding) {
        this.mini = miniBinding;
        if (mini.tvMiniLyrics != null) {
            karaokeLyricsController = new KaraokeLyricsController(mini.tvMiniLyrics);
        }
        initMiniPlayer();
        registerControlReceiver();
        restoreFromState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        lastActive = new WeakReference<>(this);

        if (mini != null) {
            if (PlayerState.get().hasTrack && PlayerState.get().isLocal) {
                playbackOwner = new WeakReference<>(this);
                isLocalPlayback = true;
                isPlaying = PlayerState.get().isPlaying;
            }

            attachLocalPlayerListener();
            restoreFromState();
        }

        miniProgressHandler.removeCallbacks(miniProgressRunnable);
        miniProgressHandler.post(miniProgressRunnable);
    }

    @Override
    protected void onDestroy() {
        boolean wasOwner = playbackOwner.get() == this;

        miniProgressHandler.removeCallbacks(miniProgressRunnable);

        if (receiverRegistered) {
            unregisterReceiver(controlReceiver);
            receiverRegistered = false;
        }

        if (miniYouTubePlayerView != null) {
            miniYouTubePlayerView.release();
        }

        if (wasOwner) {
            playbackOwner.clear();
        }

        if (lastActive.get() == this) {
            lastActive.clear();
        }

        super.onDestroy();
    }

    // ---------------------------------------------------------------
    // Setup
    // ---------------------------------------------------------------

    public void registerControlReceiver() {
        if (receiverRegistered) return;
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackService.ACTION_PLAY);
        filter.addAction(PlaybackService.ACTION_PAUSE);
        filter.addAction(PlaybackService.ACTION_NEXT);
        filter.addAction(PlaybackService.ACTION_SEEK);
        filter.addAction(PlaybackService.ACTION_STATE);
        filter.addAction(PlaybackService.ACTION_PREVIOUS);
        ContextCompat.registerReceiver(this, controlReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        receiverRegistered = true;
    }

    private void initMiniPlayer() {
        miniYouTubePlayerView = mini.miniYoutubePlayerView;
        miniYouTubePlayerView.enableBackgroundPlayback(true);
        getLifecycle().removeObserver(miniYouTubePlayerView);
        mini.rlMiniPlayer.setClickable(true);
        mini.rlMiniPlayer.setFocusable(true);
        mini.vMiniClickOverlay.setClickable(true);
        IFramePlayerOptions options = new IFramePlayerOptions.Builder(this)
                .controls(0)
                .autoplay(1)
                .build();
        View.OnClickListener openFull = v -> openNowPlaying();
        mini.llHide.setOnClickListener(openFull);
        mini.rlMiniPlayer.setOnClickListener(openFull);

        mini.llHide.setOnLongClickListener(v -> {
            boolean visible = mini.tvMiniLyrics.getVisibility() == View.VISIBLE;
            mini.tvMiniLyrics.setVisibility(visible ? View.GONE : View.VISIBLE);
            return true;
        });
        
        
        mini.ivMiniPrevious.setOnClickListener(v -> {
            onPreviousTrackRequested();
        });

        mini.ivMiniPlayPause.setOnClickListener(v -> {
            togglePlayPause();
        });

        mini.ivMiniNext.setOnClickListener(v -> {
            onNextTrackRequested();
        });
        miniYouTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                miniPlayer = youTubePlayer;
                if (!isLocalPlayback && !TextUtils.isEmpty(pendingVideoId)) {
                    miniPlayer.loadVideo(pendingVideoId, pendingStartSec);
                    pendingVideoId = "";
                    pendingStartSec = 0f;
                }
            }

            @Override
            public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float second) {
                if (isLocalPlayback || playbackOwner.get() != MiniPlayerActivity.this) return;

                currentSecond = second;

                PlayerState s = PlayerState.get();
                s.positionSec = second;

                if (totalDuration > 0) {
                    mini.miniProgressBar.setProgress(
                            (int) ((second / totalDuration) * 1000)
                    );
                }

                if (karaokeLyricsController != null
                        && karaokeLyricsController.hasLyrics()) {
                    karaokeLyricsController.tick(second, s.isPlaying);
                }

                sendStateToService(isPlaying);
            }
            @Override
            public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float duration) {
                if (isLocalPlayback || playbackOwner.get() != MiniPlayerActivity.this) return;
                totalDuration = duration;
                PlayerState.get().durationSec = duration;
                sendStateToService(isPlaying);
                applyLyricsOffset();
                onTrackChanged();

            }

            @Override
            public void onStateChange(@NonNull YouTubePlayer youTubePlayer,
                                      @NonNull PlayerConstants.PlayerState state) {
                if (isLocalPlayback || playbackOwner.get() != MiniPlayerActivity.this) return;
                if (state == PlayerConstants.PlayerState.PLAYING) {
                    isPlaying = true;
                    updateMiniPlayerPlayPauseIcon();
                    sendStateToService(true);
                } else if (state == PlayerConstants.PlayerState.PAUSED) {
                    isPlaying = false;
                    updateMiniPlayerPlayPauseIcon();
                    sendStateToService(false);
                } else if (state == PlayerConstants.PlayerState.ENDED) {
                    isPlaying = false;
                    updateMiniPlayerPlayPauseIcon();
                    mini.miniProgressBar.setProgress(1000);
                    sendStateToService(false);
                    onNextTrackRequested();
                }
            }
        }, options);


        attachLocalPlayerListener();
    }

    private void openNowPlaying() {
        if (!PlayerState.get().hasTrack || isFinishing()) return;
        if (getSupportFragmentManager().findFragmentByTag("now_playing") != null) return;
        new NowPlayingDialog().show(getSupportFragmentManager(), "now_playing");
    }

    private void attachLocalPlayerListener() {
        LocalAudioPlayer.getInstance().setPlayerStateListener(new LocalAudioPlayer.PlayerStateListener() {
            @Override
            public void onProgressUpdate(float currentSec, float totalDurSec) {
                if (!PlayerState.get().isLocal
                        || playbackOwner.get() != MiniPlayerActivity.this) {
                    return;
                }

                currentSecond = currentSec;
                totalDuration = totalDurSec;

                PlayerState s = PlayerState.get();
                s.positionSec = currentSec;
                s.durationSec = totalDurSec;

                if (mini != null && totalDurSec > 0) {
                    mini.miniProgressBar.setProgress(
                            (int) ((currentSec / totalDurSec) * 1000)
                    );
                }

                if (karaokeLyricsController != null
                        && karaokeLyricsController.hasLyrics()) {
                    karaokeLyricsController.tick(currentSec, s.isPlaying);
                }
                applyLyricsOffset();
                sendStateToService(isPlaying);
            }

            @Override
            public void onStateChanged(boolean playing) {
                if (!PlayerState.get().isLocal || playbackOwner.get() != MiniPlayerActivity.this)
                    return;

                isPlaying = playing;
                PlayerState.get().isPlaying = playing;

                updateMiniPlayerPlayPauseIcon();
                sendStateToService(playing);
            }

            @Override
            public void onCompletion() {
                if (!PlayerState.get().isLocal || playbackOwner.get() != MiniPlayerActivity.this)
                    return;

                isPlaying = false;
                PlayerState.get().isPlaying = false;

                updateMiniPlayerPlayPauseIcon();
                onNextTrackRequested();
            }
        });
    }

    // ---------------------------------------------------------------
    // Public playback API
    // ---------------------------------------------------------------

    public void playOnline(String videoId, String title, String artist, String thumbUrl) {
        playOnline(videoId, title, artist, thumbUrl, 0f);
    }

    /**
     * Play a YouTube video in the mini player.
     */
    public void playOnline(String videoId, String title, String artist, String thumbUrl, float startSec) {
        claimPlayback();

        if (isLocalPlayback) {
            isLocalPlayback = false;
            LocalAudioPlayer.getInstance().stop();
        }

        mini.ivMiniCover.setVisibility(View.VISIBLE);
        mini.miniYoutubePlayerView.setVisibility(View.GONE);

        currentPlayingVideoId = videoId;
        currentPlayingTitle = title;
        currentPlayingThumb = thumbUrl;

        PlayerState s = PlayerState.get();
        s.hasTrack = true;
        s.isLocal = false;
        s.videoId = videoId;
        s.localPath = "";
        s.title = title;
        s.artist = artist == null ? "" : artist;
        s.thumb = thumbUrl == null ? "" : thumbUrl;
        s.positionSec = startSec;
        mini.rlMiniPlayer.setVisibility(View.VISIBLE);
        mini.tvMiniTitle.setText(title);
        mini.tvMiniArtist.setText(TextUtils.isEmpty(artist) ? "Music" : artist);

        if (miniPlayer != null) {
            miniPlayer.loadVideo(videoId, startSec);
        } else {
            pendingVideoId = videoId;
            pendingStartSec = startSec;
        }

        isPlaying = true;
        updateMiniPlayerPlayPauseIcon();
        startPlaybackService(videoId, title, thumbUrl);
    }

    /**
     * Play a downloaded audio file in the mini player.
     */
    public void playLocal(File file, String title, String artist) {

        if (file == null || !file.exists() || !file.isFile()) {
            Toast.makeText(this, "Audio file not found", Toast.LENGTH_SHORT).show();
            return;
        }

        claimPlayback();

        isLocalPlayback = true;
        currentPlayingVideoId = "";
        currentPlayingTitle = title;
        currentPlayingThumb = "";

        PlayerState s = PlayerState.get();
        s.hasTrack = true;
        s.isLocal = true;
        s.localPath = file.getAbsolutePath();
        s.videoId = "";
        s.title = title;
        s.artist = artist == null ? "" : artist;
        s.thumb = "";
        s.positionSec = 0f;
        s.durationSec = 0f;
        s.isPlaying = false;

        if (miniPlayer != null) {
            miniPlayer.pause();
        }

        registerControlReceiver();
        attachLocalPlayerListener();


        mini.miniYoutubePlayerView.setVisibility(View.GONE);
        mini.ivMiniCover.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(R.mipmap.app_logo)
                .into(mini.ivMiniCover);

        mini.rlMiniPlayer.setVisibility(View.VISIBLE);
        mini.tvMiniTitle.setText(title);
        mini.tvMiniArtist.setText("Downloaded Track");
        mini.miniProgressBar.setProgress(0);

        isPlaying = false;
        updateMiniPlayerPlayPauseIcon();

        android.util.Log.d("LOCAL_PLAYER", "Playing file: " + file.getAbsolutePath());
        android.util.Log.d("LOCAL_PLAYER", "File size: " + file.length());

        LocalAudioPlayer.getInstance().playFile(file);

        startPlaybackService("local_file", title, "");
    }

    /**
     * Play item {@code index} of the shared queue (wraps around).
     */
    public void playSharedQueueAt(int index) {
        PlayerState s = PlayerState.get();
        int n = s.queue.size();
        if (n == 0) return;
        int i = ((index % n) + n) % n;
        s.queueIndex = i;
        PlayerState.Item it = s.queue.get(i);

        if (!TextUtils.isEmpty(it.localPath) && new File(it.localPath).exists()) {
            playLocal(new File(it.localPath), it.name, it.artist);
        } else if (!TextUtils.isEmpty(it.videoId)) {
            playOnline(it.videoId, it.name, it.artist, it.thumb);
        } else {
            pendingSearchItem = it;
            if (queuePresenter == null) queuePresenter = new MusicListPresenter(searchView);
            queuePresenter.getSearchListQuery((it.name + " " + it.artist).trim() + " official audio");
        }
    }
    /**
     * Pause whichever source is active.
     */
    public void pausePlayback() {
        if (isLocalPlayback) {
            LocalAudioPlayer.getInstance().pause();
        } else if (miniPlayer != null) {
            miniPlayer.pause();
        }
    }

    /**
     * True if the online player is loaded with something that can be resumed.
     */
    public boolean hasOnlineTrackLoaded() {
        return miniPlayer != null && !TextUtils.isEmpty(currentPlayingVideoId);
    }

    // ---------------------------------------------------------------
    // Restore / resume across screens
    // ---------------------------------------------------------------

    /**
     * Sync this screen's mini player with the shared PlayerState.
     */
    public void restoreFromState() {
        PlayerState s = PlayerState.get();

        // Sync lyrics visibility on restore
        if (mini != null && mini.tvMiniLyrics != null) {
            if (s.parsedLyrics != null && !s.parsedLyrics.isEmpty()) {
                mini.tvMiniLyrics.setVisibility(View.VISIBLE);
                if (karaokeLyricsController != null) {
                    karaokeLyricsController.setLines(s.parsedLyrics);
                }
            } else {
                mini.tvMiniLyrics.setVisibility(View.GONE);
                if (karaokeLyricsController != null) {
                    karaokeLyricsController.clear();
                }
            }
        }

        if (mini == null || !s.hasTrack) return;
        if (s.resumeOnRestore && !isLoadedHere()) {
            s.resumeOnRestore = false;
            if (resumeFromState()) return;
        }
        if (isLoadedHere()) {
            syncMiniPlayerInfo();

            if (s.durationSec > 0) {
                mini.miniProgressBar.setProgress((int) ((s.positionSec / s.durationSec) * 1000));
            }

            updateMiniPlayerPlayPauseIcon();
            return;
        }

        isLocalPlayback = s.isLocal;
        currentPlayingVideoId = "";
        isPlaying = s.isPlaying;
        syncMiniPlayerInfo();
        mini.rlMiniPlayer.setVisibility(View.VISIBLE);
        mini.tvMiniTitle.setText(s.title);
        mini.tvMiniArtist.setText(TextUtils.isEmpty(s.artist)
                ? (s.isLocal ? "Downloaded Track" : "Music") : s.artist);

        mini.miniYoutubePlayerView.setVisibility(View.GONE);
        mini.ivMiniCover.setVisibility(View.VISIBLE);
        if (!s.isLocal && !TextUtils.isEmpty(s.thumb)) {
            Glide.with(this).load(s.thumb).placeholder(R.mipmap.app_logo).into(mini.ivMiniCover);
        } else {
            Glide.with(this).load(R.mipmap.app_logo).into(mini.ivMiniCover);
        }

        if (s.durationSec > 0) {
            mini.miniProgressBar.setProgress((int) ((s.positionSec / s.durationSec) * 1000));
        }
        mini.ivMiniPlayPause.setImageResource(
                isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        onPlaybackStateUpdated(isPlaying);
    }

    private void syncMiniPlayerInfo() {
        if (mini == null) return;

        PlayerState s = PlayerState.get();
        if (!s.hasTrack) return;

        if (mini.rlMiniPlayer.getBackground() != null) {
            mini.rlMiniPlayer.getBackground().mutate()
                    .setTint(NowPlayingDialog.colorFor(s.title));
        }

        if (mini != null && mini.tvMiniLyrics != null) {
            if (s.parsedLyrics != null && !s.parsedLyrics.isEmpty()) {
                mini.tvMiniLyrics.setVisibility(View.VISIBLE);
                if (karaokeLyricsController != null) {
                    karaokeLyricsController.setLines(s.parsedLyrics);
                }
            } else {
                mini.tvMiniLyrics.setVisibility(View.GONE);
                if (karaokeLyricsController != null) {
                    karaokeLyricsController.clear();
                }
            }
        }

        mini.rlMiniPlayer.setVisibility(View.VISIBLE);
        mini.tvMiniTitle.setText(s.title);

        if (s.isLocal) {
            mini.tvMiniArtist.setText("Downloaded Track");

            mini.miniYoutubePlayerView.setVisibility(View.GONE);
            mini.ivMiniCover.setVisibility(View.VISIBLE);

            Glide.with(this)
                    .load(R.mipmap.app_logo)
                    .into(mini.ivMiniCover);
        } else {
            mini.tvMiniArtist.setText(TextUtils.isEmpty(s.artist) ? "Music" : s.artist);

            if (!TextUtils.isEmpty(s.thumb)) {
                mini.ivMiniCover.setVisibility(View.VISIBLE);

                Glide.with(this)
                        .load(s.thumb)
                        .placeholder(R.mipmap.app_logo)
                        .into(mini.ivMiniCover);
            } else {
                Glide.with(this)
                        .load(R.mipmap.app_logo)
                        .into(mini.ivMiniCover);
            }
        }
    }

    /**
     * Load the saved track into THIS activity's player and play it.
     */
    private boolean resumeFromState() {
        PlayerState s = PlayerState.get();
        if (mini == null || !s.hasTrack) return false;
        if (s.isLocal) {
            File f = new File(s.localPath);
            if (!f.exists()) return false;
            playLocal(f, s.title, s.artist);
        } else {
            playOnline(s.videoId, s.title, s.artist, s.thumb, s.positionSec);
        }
        return true;
    }

    // ---------------------------------------------------------------
    // Service + UI helpers
    // ---------------------------------------------------------------

    public void startPlaybackService(String videoId, String title, String thumb) {
        Intent i = new Intent(this, PlaybackService.class);
        i.setAction(PlaybackService.ACTION_START);
        i.putExtra("videoId", videoId);
        i.putExtra("title", title);
        i.putExtra("thumbnail", thumb);
        i.putExtra("tumbnail", thumb);
        i.putExtra(PlaybackService.EXTRA_TARGET_ACTIVITY, getTargetActivityName());
        ContextCompat.startForegroundService(this, i);
    }

    public void sendStateToService(boolean playing) {
        try {
            Intent i = new Intent(this, PlaybackService.class);
            i.setAction(PlaybackService.ACTION_STATE);
            i.putExtra(PlaybackService.EXTRA_IS_PLAYING, playing);
            i.putExtra(PlaybackService.EXTRA_CURRENT_POSITION, (long) (currentSecond * 1000));
            i.putExtra(PlaybackService.EXTRA_DURATION, (long) (totalDuration * 1000));
            ContextCompat.startForegroundService(this, i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateMiniPlayerPlayPauseIcon() {
        if (mini == null) return;
        PlayerState.get().isPlaying = isPlaying;
        mini.ivMiniPlayPause.setImageResource(
                isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        onPlaybackStateUpdated(isPlaying);
    }

    @Override
    protected void onPause() {
        miniProgressHandler.removeCallbacks(miniProgressRunnable);

        if (playbackOwner.get() == this && isFinishing() && isPlaying) {
            PlayerState s = PlayerState.get();
            s.positionSec = currentSecond;

            if (isLocalPlayback) {
                s.resumeOnRestore = false;
            } else {
                s.resumeOnRestore = true;
                pausePlayback();
            }
        }

        super.onPause();
    }

    public static void pauseMiniPlayerForVideo() {
        MiniPlayerActivity owner = playbackOwner.get();
        PlayerState s = PlayerState.get();

        if (!s.hasTrack) return;

        if (s.isLocal) {
            LocalAudioPlayer.getInstance().pause();
        } else if (owner != null && owner.miniPlayer != null) {
            owner.miniPlayer.pause();
        }

        s.isPlaying = false;
        s.resumeOnRestore = false;

        if (owner != null) {
            owner.isPlaying = false;
            owner.updateMiniPlayerPlayPauseIcon();
            owner.sendStateToService(false);
        }
    }

    public void onPreviousTrackRequested() {
        PlayerState s = PlayerState.get();

        if (s.queue == null || s.queue.isEmpty()) return;

        playSharedQueueAt(s.queueIndex - 1);
    }
    protected void applyLyricsOffset() {
        if (karaokeLyricsController == null) return;
        PlayerState s = PlayerState.get();

        long offsetMs = 0;
        if (s.lyricsDurationSec > 0 && s.durationSec > 0) {
            float diff = s.durationSec - s.lyricsDurationSec;   // + = video longer
            if (Math.abs(diff) >= 0.5f && Math.abs(diff) <= 15f) {
                offsetMs = (long) (-diff * 1000);
            }
        }
        karaokeLyricsController.setOffsetMs(offsetMs);
        s.lyricsOffsetMs = offsetMs;
    }


    private MiniPlayerActivity target() {
        MiniPlayerActivity o = playbackOwner.get();
        return (o != null && o != this && isAlive(o) && o.isLoadedHere()) ? o : this;
    }

    public void togglePlayPause() {
        MiniPlayerActivity t = target();
        if (t != this) { t.togglePlayPause(); return; }

        if (!isLoadedHere() && resumeFromState()) return;

        if (isLocalPlayback) {
            LocalAudioPlayer.getInstance().togglePlayPause();
        } else if (miniPlayer != null) {
            if (isPlaying) miniPlayer.pause();
            else miniPlayer.play();
        }
    }

    public void seekToSec(float sec) {
        MiniPlayerActivity t = target();
        if (t != this) { t.seekToSec(sec); return; }

        if (isLocalPlayback) {
            LocalAudioPlayer.getInstance().seekTo((long) (sec * 1000));
        } else if (miniPlayer != null) {
            miniPlayer.seekTo(sec);
        }
        currentSecond = sec;
        PlayerState.get().positionSec = sec;
    }
}