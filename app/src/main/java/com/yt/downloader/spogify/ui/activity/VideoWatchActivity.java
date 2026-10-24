package com.yt.downloader.spogify.ui.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.yt.downloader.R;
import com.yt.downloader.spogify.Retrofit.AppConstant;
import com.yt.downloader.spogify.Service.PlaybackService;
import com.yt.downloader.spogify.SharedPreference.SPUtils;
import com.yt.downloader.spogify.mvp.bean.SearchTotalBean;
import com.yt.downloader.spogify.ui.adapter.MusicListAdapter;
import com.yt.downloader.databinding.ActivityVideoWatchBinding;
import com.yt.downloader.spogify.mvp.bean.YoutubeSearchBean;
import com.yt.downloader.spogify.mvp.contract.MusicListContract;
import com.yt.downloader.spogify.mvp.presenter.MusicListPresenter;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class VideoWatchActivity extends AppCompatActivity implements MusicListContract.View {
    private ActivityVideoWatchBinding binding;
    private String videoId, title, tumbnail;
    private YouTubePlayerView youTubePlayerView;
    private View myfullview;
    private YouTubePlayer player;
    private boolean receiverRegistered = false;
    private MusicListAdapter musicListAdapter;
    private MusicListPresenter musicListPresenter;
    private List<SearchTotalBean> itemsBeans = new ArrayList<>();
    private String recentSearch = "";
    private Function0<Unit> exitFullscreenCallback;
    private final java.util.Random random = new java.util.Random();
    private String currentVideoId = "";
    private boolean shuffleEnabled = true;

    private float currentSecond = 0f;
    private float totalDurationSec = 0f;
    private long lastStateSyncTime = 0;

    private final BroadcastReceiver controlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (player == null || intent == null || intent.getAction() == null) return;
            String action = intent.getAction();
            if (PlaybackService.ACTION_PLAY.equals(action)) {
                player.play();
            } else if (PlaybackService.ACTION_PAUSE.equals(action)) {
                player.pause();
            } else if (PlaybackService.ACTION_NEXT.equals(action)) {
                playRandomNext();
            } else if (PlaybackService.ACTION_SEEK.equals(action)) {
                long posMs = intent.getLongExtra(PlaybackService.EXTRA_CURRENT_POSITION, 0);
                player.seekTo(posMs / 1000f);
            }
        }
    };

    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
        if (!granted) {
            Toast.makeText(this, "Enable notifications to control background playback", Toast.LENGTH_LONG).show();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoWatchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        videoId = getIntent().getStringExtra("videoId");
        title = getIntent().getStringExtra("title");
        tumbnail = getIntent().getStringExtra("tumbnail");
        binding.ivBack.setOnClickListener(v -> finish());
        binding.tvTitle.setText(TextUtils.isEmpty(title) ? "" : title);

        youTubePlayerView = binding.youtubePlayerView;

        if (TextUtils.isEmpty(videoId)) {
            Toast.makeText(getApplicationContext(), "Video not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initRecycler();
        musicListPresenter = new MusicListPresenter(this);
        recentSearch = SPUtils.getInstance().getString(AppConstant.recentSearch);
        musicListPresenter.getSearchListQuery(TextUtils.isEmpty(recentSearch) ? "" : recentSearch);

        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackService.ACTION_PLAY);
        filter.addAction(PlaybackService.ACTION_PAUSE);
        filter.addAction(PlaybackService.ACTION_NEXT);
        filter.addAction(PlaybackService.ACTION_SEEK);
        ContextCompat.registerReceiver(this, controlReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        receiverRegistered = true;

        youTubePlayerView.enableBackgroundPlayback(true);

        IFramePlayerOptions iFramePlayerOptions = new IFramePlayerOptions.Builder(this).controls(1).fullscreen(1).autoplay(1).build();

        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                player = youTubePlayer;
                startPlayer(videoId, title, tumbnail);
            }

            @Override
            public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float second) {
                currentSecond = second;
                long now = System.currentTimeMillis();
                if (now - lastStateSyncTime >= 1000) { // Sync duration & position every 1 second
                    lastStateSyncTime = now;
                    sendState(true);
                }
            }

            @Override
            public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float duration) {
                totalDurationSec = duration;
                sendState(true); // Sync duration immediately when duration loads
            }

            @Override
            public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
                if (state == PlayerConstants.PlayerState.PLAYING) {
                    sendState(true);
                } else if (state == PlayerConstants.PlayerState.PAUSED) {
                    sendState(false);
                } else if (state == PlayerConstants.PlayerState.ENDED) {
                    sendState(false);
                    if (shuffleEnabled) {
                        playRandomNext();
                    }
                }
            }
        }, iFramePlayerOptions);

        youTubePlayerView.addFullscreenListener(new FullscreenListener() {
            @Override
            public void onEnterFullscreen(@NonNull View fullscreenView, @NonNull Function0<Unit> exitFullscreen) {
                myfullview = fullscreenView;
                exitFullscreenCallback = exitFullscreen;
                showFull(true, fullscreenView);
            }

            @Override
            public void onExitFullscreen() {
                exitFullscreenCallback = null;
                showFull(false, myfullview);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (myfullview != null && exitFullscreenCallback != null) {
                    exitFullscreenCallback.invoke();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        binding.swipeRefresh.setOnRefreshListener(this::refreshData);
    }

    private void playRandomNext() {
        List<SearchTotalBean> data = musicListAdapter.getData();
        if (data == null || data.isEmpty()) return;
        SearchTotalBean next = null;
        for (int attempt = 0; attempt < 5; attempt++) {
            SearchTotalBean candidate = data.get(random.nextInt(data.size()));
            if (candidate != null
                    && !TextUtils.isEmpty(candidate.getVideoId())
                    && !candidate.getVideoId().equals(currentVideoId)) {
                next = candidate;
                break;
            }
        }
        if (next == null) return;

        startPlayer(next.getVideoId(), next.getName(), next.getThumbnail());
    }

    private void initRecycler() {
        binding.rvMusicList.setLayoutManager(new LinearLayoutManager(this));
        musicListAdapter = new MusicListAdapter();
        binding.rvMusicList.setAdapter(musicListAdapter);
        musicListAdapter.setEmptyView(R.layout.layout_empty_music, binding.rvMusicList);
        musicListAdapter.setOnItemClickListener((adapter, view, position) -> {
            String vId = musicListAdapter.getData().get(position).getVideoId();
            String vTitle = musicListAdapter.getData().get(position).getName();
            String vThumb = musicListAdapter.getData().get(position).getThumbnail();

            if (TextUtils.isEmpty(vId) && TextUtils.isEmpty(vTitle) && TextUtils.isEmpty(vThumb)) {
                return;
            }
            startPlayer(vId, vTitle, vThumb);
        });
    }

    private void startPlayer(String videoId, String title, String tumbnail) {
        currentVideoId = videoId;
        currentSecond = 0f;
        totalDurationSec = 0f;

        if (player != null) {
            player.loadVideo(videoId, 0);
        }
        Intent i = new Intent(VideoWatchActivity.this, PlaybackService.class);
        i.setAction(PlaybackService.ACTION_START);
        i.putExtra("videoId", videoId);
        i.putExtra("title", title);
        i.putExtra("thumbnail", tumbnail);
        i.putExtra("tumbnail", tumbnail);
        i.putExtra(PlaybackService.EXTRA_TARGET_ACTIVITY, VideoWatchActivity.class.getName());
        ContextCompat.startForegroundService(VideoWatchActivity.this, i);

        binding.tvTitle.setText(title);
    }

    private void sendState(boolean playing) {
        try {
            Intent i = new Intent(this, PlaybackService.class);
            i.setAction(PlaybackService.ACTION_STATE);
            i.putExtra(PlaybackService.EXTRA_IS_PLAYING, playing);
            i.putExtra(PlaybackService.EXTRA_CURRENT_POSITION, (long) (currentSecond * 1000));
            i.putExtra(PlaybackService.EXTRA_DURATION, (long) (totalDurationSec * 1000));
            startService(i);
        } catch (IllegalStateException ignored) {
        }
    }


    private void showFull(boolean isfull, View fullscreenView) {
        if (isfull) {
            ViewGroup root = findViewById(android.R.id.content);
            root.addView(fullscreenView);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            binding.rlTitle.setVisibility(View.GONE);
            binding.rvMusicList.setVisibility(View.GONE);
            binding.tvResult.setVisibility(View.GONE);
        } else {
            binding.rlTitle.setVisibility(View.VISIBLE);
            binding.rvMusicList.setVisibility(View.VISIBLE);
            binding.tvResult.setVisibility(View.VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (fullscreenView != null) {
                ViewGroup root = findViewById(android.R.id.content);
                root.removeView(fullscreenView);
                myfullview = null;
            }
        }
    }

    private void refreshData() {
        if (!isNetworkAvailable()) {
            if (binding.swipeRefresh.isRefreshing()) {
                binding.swipeRefresh.setRefreshing(false);
            }
            Toast.makeText(this, "Please check network and try again", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(recentSearch)) {
            return;
        }

        if (!itemsBeans.isEmpty()) {
            itemsBeans.clear();
            musicListAdapter.setNewData(itemsBeans);
        }
        musicListPresenter.getSearchListQuery(recentSearch);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiverRegistered) {
            unregisterReceiver(controlReceiver);
            receiverRegistered = false;
        }
        player = null;
        stopService(new Intent(this, PlaybackService.class));
        if (youTubePlayerView != null) {
            youTubePlayerView.release();
        }
    }

    @Override
    public void showLoading() {
        binding.swipeRefresh.setRefreshing(true);
    }

    @Override
    public void showError(String error) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Toast.makeText(getApplicationContext(), "Error fetching data: " + error, Toast.LENGTH_SHORT).show();
            binding.swipeRefresh.setRefreshing(false);
        }, 500);
    }

    @Override
    public void hideLoading() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> binding.swipeRefresh.setRefreshing(false), 500);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("MissingPermission")
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void getSearchList(YoutubeSearchBean youtubeSearchBean) {
        if (youtubeSearchBean != null && !youtubeSearchBean.getItems().isEmpty()) {
            for(YoutubeSearchBean.ItemsBean itemsBean :youtubeSearchBean.getItems()){
                SearchTotalBean bean = new SearchTotalBean(
                        itemsBean.getThumbnail(),
                        itemsBean.getTitle(),
                        itemsBean.getDuration(),
                        itemsBean.getId(),
                        false
                );
                itemsBeans.add(bean);
            }
            musicListAdapter.setNewData(itemsBeans);
        }
    }
}