package com.yt.downloader.spogify.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.yt.downloader.R;
import com.yt.downloader.databinding.ActivityTrackInfoBinding;
import com.yt.downloader.spogify.Retrofit.AppConstant;
import com.yt.downloader.spogify.Service.PlaybackService;
import com.yt.downloader.spogify.Utils.FollowDatabaseHelper;
import com.yt.downloader.spogify.Utils.WindowUtils;
import com.yt.downloader.spogify.mvp.bean.DeezerArtistBean;
import com.yt.downloader.spogify.mvp.bean.TrackInfoBean;
import com.yt.downloader.spogify.mvp.bean.YoutubeSearchBean;
import com.yt.downloader.spogify.mvp.contract.MusicListContract;
import com.yt.downloader.spogify.mvp.contract.TrackInfoContract;
import com.yt.downloader.spogify.mvp.presenter.MusicListPresenter;
import com.yt.downloader.spogify.mvp.presenter.TrackInfoPresenter;
import com.yt.downloader.spogify.ui.adapter.TopTrackInfoAdapter;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class TrackInfoActivity extends AppCompatActivity implements View.OnClickListener, TrackInfoContract.View, MusicListContract.View {

    private ActivityTrackInfoBinding binding;
    private String artistName;
    private String currentArtistImageUrl = "";
    private String totalListenersText = "0 listeners";

    private final Map<String, String> imageCache = new ConcurrentHashMap<>();
    private TrackInfoPresenter trackInfoPresenter;
    private MusicListPresenter musicListPresenter;
    private TopTrackInfoAdapter trackInfoAdapter;
    private FollowDatabaseHelper dbHelper;

    private YouTubePlayerView miniYouTubePlayerView;
    private YouTubePlayer miniPlayer;
    private boolean isPlaying = false;
    private String pendingVideoId = "";
    private String currentPlayingTitle = "";
    private String currentPlayingVideoId = "";
    private String currentPlayingThumb = "";
    private float totalDuration = 0f;
    private float currentSecond = 0f;
    private boolean receiverRegistered = false;

    private List<TrackInfoBean.Track> currentTrackList = new ArrayList<>();
    private int currentPlayingIndex = 0;

    private final BroadcastReceiver controlReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;
            String action = intent.getAction();
            if (PlaybackService.ACTION_PLAY.equals(action)) {
                if (miniPlayer != null) miniPlayer.play();
            } else if (PlaybackService.ACTION_PAUSE.equals(action)) {
                if (miniPlayer != null) miniPlayer.pause();
            } else if (PlaybackService.ACTION_NEXT.equals(action)) {
                playNextTrack();
            } else if (PlaybackService.ACTION_SEEK.equals(action)) {
                long posMs = intent.getLongExtra(PlaybackService.EXTRA_CURRENT_POSITION, 0);
                if (miniPlayer != null) miniPlayer.seekTo(posMs / 1000f);
            } else if (PlaybackService.ACTION_STATE.equals(action)) {
                isPlaying = intent.getBooleanExtra(PlaybackService.EXTRA_IS_PLAYING, false);
                updateMiniPlayerPlayPauseIcon();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTrackInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new FollowDatabaseHelper(this);
        artistName = getIntent().getStringExtra("ArtistName");

        binding.ivBack.setOnClickListener(this);
        binding.fabPlay.setOnClickListener(this);
        if (binding.btnFollow != null) {
            binding.btnFollow.setOnClickListener(this);
        }

        String name = TextUtils.isEmpty(artistName) ? "" : artistName;
        binding.tvArtistName.setText(name);
        binding.tvToolbarTitle.setText(name);

        initAppBarLayoutScrollListener();
        initBigImage();
        initRecycler();
        initMiniPlayer();
        updateFollowButtonUI();

        trackInfoPresenter = new TrackInfoPresenter(this);
        musicListPresenter = new MusicListPresenter(this);

        trackInfoPresenter.getTrackArtistQuery(AppConstant.TopTracksInfo, artistName, AppConstant.Api_Key, "json");

        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackService.ACTION_PLAY);
        filter.addAction(PlaybackService.ACTION_PAUSE);
        filter.addAction(PlaybackService.ACTION_NEXT);
        filter.addAction(PlaybackService.ACTION_SEEK);
        filter.addAction(PlaybackService.ACTION_STATE);
        ContextCompat.registerReceiver(this, controlReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        receiverRegistered = true;
    }

    private void updateFollowButtonUI() {
        if (binding.btnFollow == null || TextUtils.isEmpty(artistName)) return;
        boolean isFollowed = dbHelper.isArtistFollowed(artistName);
        if (isFollowed) {
            binding.btnFollow.setText("Following");
        } else {
            binding.btnFollow.setText("Follow");
        }
    }

    private void toggleFollowStatus() {
        if (TextUtils.isEmpty(artistName)) return;

        boolean isFollowed = dbHelper.isArtistFollowed(artistName);
        if (isFollowed) {
            boolean success = dbHelper.unfollowArtist(artistName);
            if (success) {
                Toast.makeText(this, "Unfollowed " + artistName, Toast.LENGTH_SHORT).show();
            }
        } else {
            boolean success = dbHelper.followArtist(artistName, currentArtistImageUrl, totalListenersText);
            if (success) {
                Toast.makeText(this, "Following " + artistName, Toast.LENGTH_SHORT).show();
            }
        }
        updateFollowButtonUI();
    }

    private void initRecycler() {
        trackInfoAdapter = new TopTrackInfoAdapter();
        binding.rvPopularTracks.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPopularTracks.setAdapter(trackInfoAdapter);
        trackInfoAdapter.setEmptyView(R.layout.layout_empty_music, binding.rvPopularTracks);

        trackInfoAdapter.setOnTrackClickListener((track, position) -> {
            currentPlayingIndex = position;
            String searchQuery = track.getName() + " " + artistName;
            showLoading();
            musicListPresenter.getSearchListQuery(searchQuery);
        });
    }

    private void initMiniPlayer() {
        miniYouTubePlayerView = binding.layoutMiniPlayer.miniYoutubePlayerView;

        miniYouTubePlayerView.enableBackgroundPlayback(true);
        getLifecycle().removeObserver(miniYouTubePlayerView);

        IFramePlayerOptions options = new IFramePlayerOptions.Builder(this)
                .controls(0)
                .autoplay(1)
                .build();

        miniYouTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                miniPlayer = youTubePlayer;
                if (!TextUtils.isEmpty(pendingVideoId)) {
                    miniPlayer.loadVideo(pendingVideoId, 0);
                    pendingVideoId = "";
                }
            }

            @Override
            public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float second) {
                currentSecond = second;
                if (totalDuration > 0) {
                    int progress = (int) ((second / totalDuration) * 1000);
                    binding.layoutMiniPlayer.miniProgressBar.setProgress(progress);
                }
                sendStateToService(isPlaying);
            }

            @Override
            public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float duration) {
                totalDuration = duration;
                sendStateToService(isPlaying);
            }

            @Override
            public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
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
                    binding.layoutMiniPlayer.miniProgressBar.setProgress(1000);
                    sendStateToService(false);
                    playNextTrack();
                }
            }
        }, options);

        binding.layoutMiniPlayer.ivMiniPlayPause.setOnClickListener(v -> {
            if (miniPlayer == null) return;
            if (isPlaying) {
                miniPlayer.pause();
            } else {
                miniPlayer.play();
            }
        });

        binding.layoutMiniPlayer.vMiniClickOverlay.setOnClickListener(v -> openFullPlayer());
        binding.layoutMiniPlayer.tvMiniTitle.setOnClickListener(v -> openFullPlayer());
    }

    private void playNextTrack() {
        if (currentTrackList != null && !currentTrackList.isEmpty()) {
            currentPlayingIndex = (currentPlayingIndex + 1) % currentTrackList.size();
            TrackInfoBean.Track nextTrack = currentTrackList.get(currentPlayingIndex);
            String searchQuery = nextTrack.getName() + " " + artistName;
            showLoading();
            musicListPresenter.getSearchListQuery(searchQuery);
        }
    }

    private void openFullPlayer() {
        if (!TextUtils.isEmpty(currentPlayingVideoId)) {
            Intent intent = new Intent(this, VideoWatchActivity.class);
            intent.putExtra("videoId", currentPlayingVideoId);
            intent.putExtra("title", currentPlayingTitle);
            intent.putExtra("tumbnail", currentPlayingThumb);
            startActivity(intent);
        }
    }

    private void playVideoInMiniPlayer(String videoId, String title, String thumbUrl) {
        currentPlayingVideoId = videoId;
        currentPlayingTitle = title;
        currentPlayingThumb = thumbUrl;

        binding.layoutMiniPlayer.rlMiniPlayer.setVisibility(View.VISIBLE);
        binding.layoutMiniPlayer.tvMiniTitle.setText(title);
        binding.layoutMiniPlayer.tvMiniArtist.setText(artistName);

        if (miniPlayer != null) {
            miniPlayer.loadVideo(videoId, 0);
        } else {
            pendingVideoId = videoId;
        }

        isPlaying = true;
        if (trackInfoAdapter != null) {
            trackInfoAdapter.setPlayingPosition(currentPlayingIndex, true);
        }

        Intent i = new Intent(this, PlaybackService.class);
        i.setAction(PlaybackService.ACTION_START);
        i.putExtra("videoId", videoId);
        i.putExtra("title", title);
        i.putExtra("tumbnail", thumbUrl);
        ContextCompat.startForegroundService(this, i);
    }

    private void sendStateToService(boolean playing) {
        try {
            Intent i = new Intent(this, PlaybackService.class);
            i.setAction(PlaybackService.ACTION_STATE);
            i.putExtra(PlaybackService.EXTRA_IS_PLAYING, playing);
            i.putExtra(PlaybackService.EXTRA_CURRENT_POSITION, (long) (currentSecond * 1000));
            i.putExtra(PlaybackService.EXTRA_DURATION, (long) (totalDuration * 1000));
            startService(i);
        } catch (Exception ignored) {
        }
    }

    private void updateMiniPlayerPlayPauseIcon() {
        if (isPlaying) {
            binding.layoutMiniPlayer.ivMiniPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            binding.layoutMiniPlayer.ivMiniPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }
        updateFabPlayPauseIcon();
        if (trackInfoAdapter != null) {
            trackInfoAdapter.setPlayingPosition(currentPlayingIndex, isPlaying);
        }
    }

    private void updateFabPlayPauseIcon() {
        if (isPlaying) {
            binding.fabPlay.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            binding.fabPlay.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    @Override
    public void getSearchList(YoutubeSearchBean youtubeSearchBean) {
        hideLoading();
        if (youtubeSearchBean != null && youtubeSearchBean.getItems() != null && !youtubeSearchBean.getItems().isEmpty()) {
            YoutubeSearchBean.ItemsBean item = youtubeSearchBean.getItems().get(0);
            playVideoInMiniPlayer(item.getId(), item.getTitle(), item.getThumbnail());
        } else {
            Toast.makeText(this, "Track not found on YouTube", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatListenerCount(long count) {
        if (count < 1000) {
            return count + " listeners";
        } else if (count < 1000000) {
            double thousands = count / 1000.0;
            return String.format(Locale.US, thousands % 1 == 0 ? "%.0fK listeners" : "%.1fK listeners", thousands);
        } else if (count < 1000000000L) {
            double millions = count / 1000000.0;
            return String.format(Locale.US, millions % 1 == 0 ? "%.0fM listeners" : "%.1fM listeners", millions);
        } else {
            double billions = count / 1000000000.0;
            return String.format(Locale.US, billions % 1 == 0 ? "%.0fB listeners" : "%.1fB listeners", billions);
        }
    }

    private void initAppBarLayoutScrollListener() {
        binding.appBar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int totalScrollRange = appBarLayout.getTotalScrollRange();
            float percentage = (float) Math.abs(verticalOffset) / (float) totalScrollRange;
            if (percentage >= 0.8f) {
                float alpha = (percentage - 0.8f) / 0.2f;
                binding.tvToolbarTitle.setAlpha(alpha);
            } else {
                binding.tvToolbarTitle.setAlpha(0f);
            }
        });
    }

    private void initBigImage() {
        binding.ivBig.setTag(artistName);
        if (imageCache.containsKey(artistName)) {
            currentArtistImageUrl = imageCache.get(artistName);
            loadImage(binding.ivBig, currentArtistImageUrl);
        } else {
            try {
                Glide.with(this).load(R.mipmap.app_logo).into(binding.ivBig);
            } catch (Exception e) {
                e.printStackTrace();
            }
            fetchDeezerImage(artistName, binding.ivBig);
        }
    }

    private void loadImage(ImageView imageView, String imageUrl) {
        try {
            Glide.with(this).asBitmap().apply(new RequestOptions().placeholder(R.drawable.noimage).error(R.drawable.noimage)).load(imageUrl).into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchDeezerImage(String name, ImageView imageView) {
        if (TextUtils.isEmpty(name)) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String query = URLEncoder.encode(name, "UTF-8");
                String deezerUrl = "https://api.deezer.com/search/artist?q=" + query;

                URL url = new URL(deezerUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    DeezerArtistBean response = new Gson().fromJson(reader, DeezerArtistBean.class);
                    reader.close();

                    if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                        String imageUrl = response.getData().get(0).getPictureBig();
                        imageCache.put(name, imageUrl);
                        currentArtistImageUrl = imageUrl;

                        imageView.post(() -> {
                            if (name.equals(imageView.getTag())) {
                                loadImage(imageView, imageUrl);
                            }
                        });
                    }
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        new WindowUtils(this, false, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiverRegistered) {
            unregisterReceiver(controlReceiver);
            receiverRegistered = false;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        } else if (v.getId() == R.id.btn_follow || (binding.btnFollow != null && v.getId() == binding.btnFollow.getId())) {
            toggleFollowStatus();
        } else if (v.getId() == R.id.fab_play || v.getId() == binding.fabPlay.getId()) {
            if (miniPlayer != null && !TextUtils.isEmpty(currentPlayingVideoId)) {
                if (isPlaying) {
                    miniPlayer.pause();
                } else {
                    miniPlayer.play();
                }
                return;
            }
            if (currentTrackList != null && !currentTrackList.isEmpty()) {
                currentPlayingIndex = 0;
                TrackInfoBean.Track firstTrack = currentTrackList.get(0);
                String searchQuery = firstTrack.getName() + " " + artistName;
                showLoading();
                musicListPresenter.getSearchListQuery(searchQuery);
            } else {
                Toast.makeText(this, "No tracks available to play", Toast.LENGTH_SHORT).show();
            }
        }
    }

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
    public void getArtistTrackSuccess(TrackInfoBean trackInfoBean) {
        if (trackInfoBean != null && trackInfoBean.getTopTracks() != null && trackInfoBean.getTopTracks().getTrackList() != null) {
            List<TrackInfoBean.Track> trackList = trackInfoBean.getTopTracks().getTrackList();
            if (!trackList.isEmpty()) {
                currentTrackList = trackList;
                trackInfoAdapter.setNewData(trackList);

                long totalListeners = 0;
                for (TrackInfoBean.Track track : trackList) {
                    String countStr = !TextUtils.isEmpty(track.getListeners()) ? track.getListeners() : track.getPlaycount();
                    if (!TextUtils.isEmpty(countStr)) {
                        try {
                            totalListeners += Long.parseLong(countStr.trim());
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                if (totalListeners > 0) {
                    totalListenersText = formatListenerCount(totalListeners);
                    binding.tvListeners.setText(totalListenersText);
                }
            }
        }
    }
}