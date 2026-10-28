package com.yt.downloader.spogify.ui.activity;

import static android.widget.Toast.LENGTH_SHORT;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.yt.downloader.R;
import com.yt.downloader.databinding.ActivityTrackInfoBinding;
import com.yt.downloader.spogify.Retrofit.AppConstant;
import com.yt.downloader.spogify.Utils.DownloadTrackModel;
import com.yt.downloader.spogify.Utils.FavoriteDatabaseHelper;
import com.yt.downloader.spogify.Utils.FollowDatabaseHelper;
import com.yt.downloader.spogify.Utils.FormatCountUtil;
import com.yt.downloader.spogify.Utils.LrcParser;
import com.yt.downloader.spogify.Utils.PlayerState;
import com.yt.downloader.spogify.Utils.WindowUtils;
import com.yt.downloader.spogify.Utils.fileUtils.FilesExtractor;
import com.yt.downloader.spogify.Utils.fileUtils.VideoFile;
import com.yt.downloader.spogify.mvp.bean.DeezerArtistBean;
import com.yt.downloader.spogify.mvp.bean.FavoriteTrack;
import com.yt.downloader.spogify.mvp.bean.LyricsBean;
import com.yt.downloader.spogify.mvp.bean.Mp3downloadBean;
import com.yt.downloader.spogify.mvp.bean.TrackInfoBean;
import com.yt.downloader.spogify.mvp.bean.YoutubeSearchBean;
import com.yt.downloader.spogify.mvp.contract.LyricsContract;
import com.yt.downloader.spogify.mvp.contract.MusicListContract;
import com.yt.downloader.spogify.mvp.contract.TrackInfoContract;
import com.yt.downloader.spogify.mvp.presenter.LyricsPresenter;
import com.yt.downloader.spogify.mvp.presenter.MusicListPresenter;
import com.yt.downloader.spogify.mvp.presenter.TrackInfoPresenter;
import com.yt.downloader.spogify.ui.adapter.TopTrackInfoAdapter;

import java.io.File;
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
import com.yt.downloader.spogify.Utils.LyricsDatabaseHelper;
public class TrackInfoActivity extends MiniPlayerActivity
        implements View.OnClickListener, TrackInfoContract.View, MusicListContract.View, LyricsContract.View {

    private ActivityTrackInfoBinding binding;
    private String artistName;
    private boolean isFromFavorite = false;
    private boolean isFromDownload = false;
    private String selectedTrackName = "";
    private String currentArtistImageUrl = "";
    private String totalListenersText = "0 listeners";

    private final Map<String, String> imageCache = new ConcurrentHashMap<>();
    private TrackInfoPresenter trackInfoPresenter;
    private MusicListPresenter musicListPresenter;
    private TopTrackInfoAdapter trackInfoAdapter;
    private FollowDatabaseHelper dbHelper;
    private FavoriteDatabaseHelper favDbHelper;
    private LyricsDatabaseHelper lyricsDatabaseHelper;
    private List<TrackInfoBean.Track> currentTrackList = new ArrayList<>();
    private int currentPlayingIndex = 0;
    private boolean isDownloadingProcess = false;
    private TrackInfoBean.Track currentDownloadTrack;
    private FilesExtractor filesExtractor;
    private KProgressHUD hud;
    private LyricsPresenter lyricsPresenter;
    private String musicName = "";
    private String ArtistName = "";
    private boolean mainDestroyed = false;
    private String currentLyricsTrackKey = "";
    private final ActivityResultLauncher<String> storagePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    if (currentDownloadTrack != null) {
                        startDownloadFlow(currentDownloadTrack);
                    }
                } else {
                    Toast.makeText(this, "Permission required to save audio to Music folder", LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // Permission outcome handled
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTrackInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new FollowDatabaseHelper(this);
        favDbHelper = new FavoriteDatabaseHelper(this);
        filesExtractor = new FilesExtractor(this);

        artistName = getIntent().getStringExtra("ArtistName");
        isFromFavorite = getIntent().getBooleanExtra("isFromFavorite", false);
        isFromDownload = getIntent().getBooleanExtra("isFromDownload", false);
        selectedTrackName = getIntent().getStringExtra("SelectedTrackName");

        binding.ivBack.setOnClickListener(this);
        binding.fabPlay.setOnClickListener(this);
        if (binding.btnFollow != null) {
            binding.btnFollow.setOnClickListener(this);
        }

        String name = TextUtils.isEmpty(artistName) ? "" : artistName;
        if (isFromDownload) {
            binding.tvArtistName.setText("Downloads");
            binding.tvToolbarTitle.setText("Downloads");
        } else if (isFromFavorite) {
            binding.tvArtistName.setText("Favorites");
            binding.tvToolbarTitle.setText("Favorites");
        } else {
            binding.tvArtistName.setText(name);
            binding.tvToolbarTitle.setText(name);
        }

        checkNotificationPermission();
        initAppBarLayoutScrollListener();
        initRecycler();

        // Mini player (online + local) lives in MiniPlayerActivity
        bindMiniPlayer(binding.layoutMiniPlayer);

        updateFollowButtonUI();
        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please wait");
        trackInfoPresenter = new TrackInfoPresenter(this);
        musicListPresenter = new MusicListPresenter(this);
        lyricsPresenter = new LyricsPresenter(this);
        lyricsDatabaseHelper = new LyricsDatabaseHelper(this);
        if (isFromDownload) {
            loadDownloadedTracksOnly();
        } else if (isFromFavorite) {
            loadFavoriteTracksOnly();
        } else {
            initBigImage(artistName);
            trackInfoPresenter.getTrackArtistQuery(AppConstant.TopTracksInfo, artistName, AppConstant.Api_Key, "json");
        }

        binding.tvVerified.setVisibility((isFromFavorite || isFromDownload) ? View.GONE : View.VISIBLE);
        binding.btnFollow.setVisibility((isFromFavorite || isFromDownload) ? View.GONE : View.VISIBLE);
        binding.ivVerified.setVisibility((isFromFavorite || isFromDownload) ? View.GONE : View.VISIBLE);
    }
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void loadDownloadedTracksOnly() {
        ArrayList<VideoFile> downloadedFiles = filesExtractor.listVideos();
        List<TrackInfoBean.Track> trackList = new ArrayList<>();

        int rank = 1;

        for (int i = 0; i < downloadedFiles.size(); i++) {
            VideoFile vf = downloadedFiles.get(i);
            String rawName = vf.getName();
            String cleanName = rawName.endsWith(".mp3") ? rawName.substring(0, rawName.length() - 4) : rawName;

            TrackInfoBean.Track t = new TrackInfoBean.Track();
            t.setName(cleanName);
            t.setImageUrl("");
            t.setPlaycount("Local Audio");

            TrackInfoBean.TrackAttr attr = new TrackInfoBean.TrackAttr();
            attr.setRank(String.valueOf(rank++));
            t.setAttributes(attr);

            trackList.add(t);
        }

        try {
            Glide.with(this).load(R.mipmap.app_logo).into(binding.ivBig);
        }catch (Exception e){
            e.printStackTrace();
        }
        binding.tvListeners.setText(downloadedFiles.size() + " downloaded tracks");

        currentTrackList = trackList;
        trackInfoAdapter.setNewData(trackList);

        currentPlayingIndex = trackList.isEmpty() ? -1 : 0;
        updateAdapterTrackState();
    }

    private void loadFavoriteTracksOnly() {
        List<FavoriteTrack> favList = favDbHelper.getAllFavoriteTracks();
        List<TrackInfoBean.Track> trackList = new ArrayList<>();

        int rank = 1;
        String favHeaderImage = "";

        for (int i = 0; i < favList.size(); i++) {
            FavoriteTrack fav = favList.get(i);
            TrackInfoBean.Track t = new TrackInfoBean.Track();
            t.setName(fav.getMusicName());
            t.setImageUrl(fav.getImageThumb());
            t.setPlaycount(fav.getTotalListeners());

            TrackInfoBean.TrackAttr attr = new TrackInfoBean.TrackAttr();
            attr.setRank(String.valueOf(rank++));
            t.setAttributes(attr);

            trackList.add(t);

            if (!TextUtils.isEmpty(selectedTrackName) && fav.getMusicName().equalsIgnoreCase(selectedTrackName)) {
                favHeaderImage = fav.getImageThumb();
            }
        }

        if (TextUtils.isEmpty(favHeaderImage) && !favList.isEmpty()) {
            favHeaderImage = favList.get(0).getImageThumb();
        }

        if (!TextUtils.isEmpty(favHeaderImage)) {
            currentArtistImageUrl = favHeaderImage;
            loadImage(binding.ivBig, favHeaderImage);
        }

        currentTrackList = trackList;
        trackInfoAdapter.setNewData(trackList);

        currentPlayingIndex = -1;
        updateAdapterTrackState();
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
                Toast.makeText(this, "Unfollowed " + artistName, LENGTH_SHORT).show();
            }
        } else {
            boolean success = dbHelper.followArtist(artistName, currentArtistImageUrl, totalListenersText);
            if (success) {
                Toast.makeText(this, "Following " + artistName, LENGTH_SHORT).show();
            }
        }
        updateFollowButtonUI();
    }

    private void initRecycler() {
        trackInfoAdapter = new TopTrackInfoAdapter();
        trackInfoAdapter.setArtistName(artistName);
        binding.rvPopularTracks.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPopularTracks.setAdapter(trackInfoAdapter);
        trackInfoAdapter.setEmptyView(R.layout.layout_empty_music, binding.rvPopularTracks);

        trackInfoAdapter.setOnTrackClickListener((track, position, isDownloaded) -> {
            currentPlayingIndex = position;
            if (isDownloaded || isFromDownload) {

                File localAudio = filesExtractor.getDownloadedFile(track.getName());

                if (localAudio != null && localAudio.exists()) {

                    playLocalAudioFile(localAudio, track.getName());

                } else {

                    Toast.makeText(this, "Audio file not found", LENGTH_SHORT).show();

                }
            } else {

                isDownloadingProcess = false;
                String searchQuery = buildSearchQuery(track);
                musicListPresenter.getSearchListQuery(searchQuery);

            }
        });

        trackInfoAdapter.setOnDownloadClickListener((track, position) -> {
            checkPermissionAndDownload(track);
        });

        trackInfoAdapter.setOnFavoriteRemovedListener(() -> {
            if (isFromFavorite) {
                loadFavoriteTracksOnly();
            }
        });
    }

    private void playLocalAudioFile(File file, String trackTitle) {
        syncQueueToState();

        String localArtist = artistName == null ? "" : artistName;
        requestLyrics(trackTitle,localArtist);

        if (isFromFavorite) {
            FavoriteTrack fav = favDbHelper.getFavoriteTrackByName(trackTitle);
            if (fav != null && !TextUtils.isEmpty(fav.getArtistName())) {
                localArtist = fav.getArtistName();
            }
        }

        PlayerState state = PlayerState.get();

        if (TextUtils.isEmpty(localArtist) && state.queue != null
                && currentPlayingIndex >= 0
                && currentPlayingIndex < state.queue.size()) {

            PlayerState.Item item = state.queue.get(currentPlayingIndex);

            if (item != null && !TextUtils.isEmpty(item.artist)) {
                localArtist = item.artist;
            }
        }

        playLocal(file, trackTitle, localArtist);

        checkCurrentTrackLyrics();

        updateAdapterTrackState();
    }
    private void playVideoInMiniPlayer(String videoId, String title, String thumbUrl) {
        String displayArtist = artistName;
        if (isFromFavorite && currentTrackList != null && currentPlayingIndex < currentTrackList.size()) {
            FavoriteTrack fav = favDbHelper.getFavoriteTrackByName(currentTrackList.get(currentPlayingIndex).getName());
            if (fav != null && !TextUtils.isEmpty(fav.getArtistName())) {
                displayArtist = fav.getArtistName();
            } else {
                displayArtist = "Favorite Track";
            }
        }
        syncQueueToState();
        playOnline(videoId, title, displayArtist, thumbUrl);
    }

    private void checkPermissionAndDownload(TrackInfoBean.Track track) {
        currentDownloadTrack = track;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                return;
            }
        }
        startDownloadFlow(track);
    }

    private void startDownloadFlow(TrackInfoBean.Track track) {
        isDownloadingProcess = true;
        currentDownloadTrack = track;
        String searchQuery = buildSearchQuery(track);
        musicListPresenter.getSearchListQuery(searchQuery);
    }

    private String buildSearchQuery(TrackInfoBean.Track track) {
        if (track == null) return "";
        String trackName = track.getName();

        if (isFromFavorite) {
            FavoriteTrack favTrack = favDbHelper.getFavoriteTrackByName(trackName);
            if (favTrack != null && !TextUtils.isEmpty(favTrack.getArtistName())) {
                return trackName + " " + favTrack.getArtistName() + " official audio";
            }
            return trackName + " official audio";
        } else {
            if (!TextUtils.isEmpty(artistName)) {
                return trackName + " " + artistName + " official audio";
            }
            return trackName + " official audio";
        }
    }
    private void updateBigHeaderImage(TrackInfoBean.Track track) {
        if (track == null) return;

        if (isFromDownload) {
            try {
                Glide.with(this).load(R.mipmap.app_logo).into(binding.ivBig);
            }catch (Exception e){
                e.printStackTrace();
            }
            return;
        }

        if (isFromFavorite) {
            FavoriteTrack favTrack = favDbHelper.getFavoriteTrackByName(track.getName());
            if (favTrack != null && !TextUtils.isEmpty(favTrack.getImageThumb())) {
                currentArtistImageUrl = favTrack.getImageThumb();
                loadImage(binding.ivBig, currentArtistImageUrl);
                return;
            }

            if (!TextUtils.isEmpty(track.getImageUrl())) {
                currentArtistImageUrl = track.getImageUrl();
                loadImage(binding.ivBig, currentArtistImageUrl);
                return;
            }
        }

        String imageUrl = track.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl) && (imageUrl.startsWith("http") || imageUrl.contains("i.ytimg.com"))) {
            currentArtistImageUrl = imageUrl;
            loadImage(binding.ivBig, currentArtistImageUrl);
        } else {
            String trackTitle = track.getName();
            if (imageCache.containsKey(trackTitle)) {
                currentArtistImageUrl = imageCache.get(trackTitle);
                loadImage(binding.ivBig, currentArtistImageUrl);
            } else {
                fetchDeezerImage(trackTitle, binding.ivBig);
            }
        }
    }

    private void updateAdapterTrackState() {
        if (trackInfoAdapter == null) return;
        syncIndexFromPlayerState();
        PlayerState state = PlayerState.get();

        if (!state.hasTrack || currentTrackList == null
                || currentTrackList.isEmpty()) {
            trackInfoAdapter.setPlayingTrackName("", false);
            return;
        }
        int index = findPlayingIndex();
        if (index >= 0) {
            TrackInfoBean.Track track = currentTrackList.get(index);
            if (track != null && !TextUtils.isEmpty(track.getName())) {
                currentPlayingIndex = index;
                updateBigHeaderImage(track);
                trackInfoAdapter.setPlayingTrackName(track.getName(), true);
            } else {
                trackInfoAdapter.setPlayingTrackName("", false);
            }
        } else {
            trackInfoAdapter.setPlayingTrackName("", false);
        }

    }

    private String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /** Index in THIS screen's list matching the given name, or -1. Prefers the current index on ties. */

    private int findTrackIndexByName(String playingName) {
        if (TextUtils.isEmpty(playingName)
                || currentTrackList == null
                || currentTrackList.isEmpty()) {
            return -1;
        }

        String target = normalize(playingName);

        if (target.isEmpty()) return -1;

        for (int i = 0; i < currentTrackList.size(); i++) {
            TrackInfoBean.Track track = currentTrackList.get(i);

            if (track == null || TextUtils.isEmpty(track.getName())) {
                continue;
            }

            if (normalize(track.getName()).equals(target)) {
                return i;
            }
        }

        return -1;
    }

    /** Index of the currently playing track within this screen's list, or -1. */
    private int findPlayingIndex() {
        PlayerState s = PlayerState.get();
        if (!s.hasTrack) return -1;

        if (s.queueIndex >= 0 && s.queueIndex < s.queue.size()) {
            return findTrackIndexByName(s.queue.get(s.queueIndex).name);
        }
        return findTrackIndexByName(s.title);
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

    private void initBigImage(String imageOrName) {
        if (TextUtils.isEmpty(imageOrName)) return;

        binding.ivBig.setTag(imageOrName);

        if (imageOrName.contains("i.ytimg.com")) {
            currentArtistImageUrl = imageOrName;
            loadImage(binding.ivBig, currentArtistImageUrl);
        } else {
            if (imageCache.containsKey(imageOrName)) {
                currentArtistImageUrl = imageCache.get(imageOrName);
                loadImage(binding.ivBig, currentArtistImageUrl);
            } else {
                try {
                    Glide.with(this).load(R.mipmap.app_logo).into(binding.ivBig);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                fetchDeezerImage(imageOrName, binding.ivBig);
            }
        }
    }

    private void loadImage(ImageView imageView, String imageUrl) {
        try {
            Glide.with(this)
                    .asBitmap()
                    .apply(new RequestOptions().placeholder(R.drawable.noimage).error(R.drawable.noimage))
                    .load(imageUrl)
                    .into(imageView);
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
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        } else if (v.getId() == R.id.btn_follow || (binding.btnFollow != null && v.getId() == binding.btnFollow.getId())) {
            toggleFollowStatus();
        } else if (v.getId() == R.id.fab_play || v.getId() == binding.fabPlay.getId()) {
            onFabPlayClicked();
        }
    }

    private void onFabPlayClicked() {

        if (currentTrackList == null || currentTrackList.isEmpty()) {
            Toast.makeText(this, "No tracks available to play", LENGTH_SHORT).show();
            return;
        }

        PlayerState s = PlayerState.get();

        if (isLoadedHere()) {
            togglePlayPause();
            return;
        }

        if (currentPlayingIndex < 0 || currentPlayingIndex >= currentTrackList.size()) {
            currentPlayingIndex = 0;
        }

        TrackInfoBean.Track activeTrack = currentTrackList.get(currentPlayingIndex);

        if (activeTrack == null) {
            return;
        }

        boolean isDownloaded = filesExtractor != null
                && filesExtractor.isFileDownloaded(activeTrack.getName());

        if (isDownloaded || isFromDownload) {

            File localAudio = filesExtractor.getDownloadedFile(activeTrack.getName());

            if (localAudio != null && localAudio.exists()) {

                if (s.hasTrack && s.isLocal
                        && TextUtils.equals(s.localPath, localAudio.getAbsolutePath())) {

                    togglePlayPause();

                } else {

                    playLocalAudioFile(localAudio, activeTrack.getName());

                }

            } else {
                Toast.makeText(this, "Downloaded audio file not found", LENGTH_SHORT).show();
            }

            return;
        }

        if (s.hasTrack && findPlayingIndex() >= 0) {
            togglePlayPause();
            return;
        }
        isDownloadingProcess = false;
        String searchQuery = buildSearchQuery(activeTrack);
        musicListPresenter.getSearchListQuery(searchQuery);
    }


    @Override
    public void showLoading() {
        if(!isFromDownload){
            if(hud !=null){
                hud.show();
            }
        }
    }

    @Override
    public void showError(String error) {
        clearCurrentLyrics();
        if(hud !=null){
            hud.dismiss();
        }
        if (!mainDestroyed) {
//            Toast.makeText(getApplicationContext(),"Lyrics not found",Toast.LENGTH_SHORT).show();
//            Log.e("LYRICS_DEBUG", "Lyrics error: " + error);
        }
    }
    @Override
    public void hideLoading() {
        if (hud != null && hud.isShowing()) {
            hud.dismiss();
        }
    }

    @Override
    public void getLyricsSuccess(LyricsBean bean) {
        if (mainDestroyed) return;
        PlayerState s = PlayerState.get();
        if (!s.hasTrack) return;
        String playingKey = s.isLocal ? "local:" + s.localPath : "online:" + s.videoId;
        if (!TextUtils.equals(currentLyricsTrackKey, playingKey)) return;
        applyLyrics(bean, musicName);
    }

    @Override
    protected void onDestroy() {
        mainDestroyed = true;
        if (lyricsDatabaseHelper != null) {
            lyricsDatabaseHelper.close();
            lyricsDatabaseHelper = null;
        }
        super.onDestroy();
    }

    @Override
    public void getSearchList(YoutubeSearchBean youtubeSearchBean) {
        if (youtubeSearchBean != null && youtubeSearchBean.getItems() != null && youtubeSearchBean.getItems().size() !=0) {
            YoutubeSearchBean.ItemsBean item = youtubeSearchBean.getItems().get(0);
            if (isDownloadingProcess && currentDownloadTrack != null) {
                String ytUrl = "https://www.youtube.com/watch?v=" + item.getId();
                trackInfoPresenter.getDownloadQuery(ytUrl, "mp3", 128);
            } else {
                playVideoInMiniPlayer(item.getId(), item.getTitle(), item.getThumbnail());
            }
        } else {
            Toast.makeText(this, "Track not found on YouTube", LENGTH_SHORT).show();
        }
    }

    @Override
    public void getDownloadSuccess(Mp3downloadBean mp3downloadBean) {
        if (mp3downloadBean != null &&  mp3downloadBean.getDownloadUrl() !=null) {
            isDownloadingProcess = false;
            String downloadUrl = mp3downloadBean.getDownloadUrl();
            String trackName = currentDownloadTrack != null ? currentDownloadTrack.getName() : "downloaded_track";

            Toast.makeText(this, "Starting download for: " + trackName, LENGTH_SHORT).show();

            DownloadTrackModel.downloadMp3File(this, downloadUrl, trackName, new DownloadTrackModel.DownloadCallback() {
                @Override
                public void onProgress(int progress) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (trackInfoAdapter != null) {
                            trackInfoAdapter.updateDownloadProgress(trackName, progress);
                        }
                    });
                }

                @Override
                public void onSuccess(String filePath) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (trackInfoAdapter != null) {
                            trackInfoAdapter.updateDownloadProgress(trackName, 100);
                        }
                        Toast.makeText(TrackInfoActivity.this, "Saved to Music/downloaded_music: " + filePath, Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onError(String error) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (trackInfoAdapter != null) {
                            trackInfoAdapter.updateDownloadProgress(trackName, 100);
                        }
                        isDownloadingProcess = false;
                        Toast.makeText(TrackInfoActivity.this, "Download Error: " + error, LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            isDownloadingProcess = false;
            Toast.makeText(this, "Failed to resolve download link", LENGTH_SHORT).show();
        }
    }

    @Override
    public void getArtistTrackSuccess(TrackInfoBean trackInfoBean) {
        if (!isFromFavorite && !isFromDownload && trackInfoBean != null && trackInfoBean.getTopTracks() != null && trackInfoBean.getTopTracks().getTrackList() != null) {
            if(trackInfoBean.getTopTracks().getTrackList().size() == 0){
                return;
            }
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
                            ignored.printStackTrace();
                        }
                    }
                }
                if (totalListeners > 0) {
                    try {
                        totalListenersText = new FormatCountUtil().formatListenerCount(totalListeners);
                        binding.tvListeners.setText(totalListenersText);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                syncIndexFromPlayerState();
                updateAdapterTrackState();
            }
        }
    }
    @Override
    public String getTargetActivityName() {
        return MainActivity.class.getName();
    }
    private void syncQueueToState() {
        if (currentTrackList == null || currentTrackList.isEmpty()) return;
        PlayerState s = PlayerState.get();

        s.queue.clear();
        for (TrackInfoBean.Track t : currentTrackList) {
            String name = t.getName();
            String artist = artistName;
            if (isFromFavorite) {
                FavoriteTrack fav = favDbHelper.getFavoriteTrackByName(name);
                artist = (fav != null && !TextUtils.isEmpty(fav.getArtistName())) ? fav.getArtistName() : "";
            }
            String path = "";
            if (isFromDownload || filesExtractor.isFileDownloaded(name)) {
                File f = filesExtractor.getDownloadedFile(name);
                if (f != null) path = f.getAbsolutePath();
            }
            s.queue.add(new PlayerState.Item(name, artist, "", t.getImageUrl(), path));
        }
        s.queueIndex = currentPlayingIndex;
    }
    private void checkCurrentTrackLyrics() {
        PlayerState state = PlayerState.get();
        if (TextUtils.isEmpty(state.artist) && TextUtils.isEmpty(state.title) && !state.hasTrack) {
            currentLyricsTrackKey = "";
            clearCurrentLyrics();
            return;
        }
        String trackKey = state.isLocal ? "local:" + state.localPath : "online:" + state.videoId;
        if (TextUtils.equals(currentLyricsTrackKey, trackKey)) {
            return;
        }
        currentLyricsTrackKey = trackKey;
        String title = state.title;
        String artist = state.artist == null ? "" : state.artist;
        if (state.queue != null && state.queueIndex >= 0 && state.queueIndex < state.queue.size()) {
            PlayerState.Item item = state.queue.get(state.queueIndex);
            if (item != null) {
                boolean matches = state.isLocal
                        ? TextUtils.equals(item.localPath, state.localPath)
                        : TextUtils.equals(item.videoId, state.videoId);
                if (matches && !TextUtils.isEmpty(item.name) && !TextUtils.isEmpty(item.artist)) {
                    title = item.name;
                    artist = item.artist;
                }else{
                    clearCurrentLyrics();
                }
            }
        }
        String musicTitle = cleanMusicTitle(title);
//        Log.d("LYRICS_CACHE", "Track check: "
//                + "Title=" + musicTitle
//                + " | Artist=" + artist
//                + " | Local=" + state.isLocal
//                + " | Key=" + trackKey);

        requestLyrics(musicTitle, artist);
    }
    @SuppressWarnings("deprecation")
    @SuppressLint("MissingPermission")
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private String cleanMusicTitle(String title) {
        if (TextUtils.isEmpty(title)) return "";

        String clean = title.trim();

        String junk = "official|lyrics?|audio|video|visualizer|remaster(ed)?|feat\\.?|ft\\.?";
        clean = clean.replaceAll("(?i)\\([^)]*(" + junk + ")[^)]*\\)", "");
        clean = clean.replaceAll("(?i)\\[[^]]*(" + junk + ")[^]]*\\]", "");

        if (clean.contains(" - ")) {
            String[] parts = clean.split(" - ", 2);
            clean = parts[1].trim();
        }

        clean = clean.replaceAll("(?i)\\s+(official\\s+)?(lyrics?|music video|audio|visualizer)\\s*$", "");

        return clean.trim();
    }
    private void requestLyrics(String musicTitle, String artistName) {
        musicName = musicTitle;
        ArtistName = artistName == null ? "" : artistName;
        if (TextUtils.isEmpty(musicName) && TextUtils.isEmpty(ArtistName)) {
            clearCurrentLyrics();
            return;
        }
        if (!isNetworkAvailable()) {
//            Log.d("LYRICS_CACHE", "No cached lyrics and no internet: " + musicName);
            return;
        }
//        Log.d("LYRICS_CACHE", "Requesting API: " + musicName + " | Artist: " + ArtistName);
        if (lyricsPresenter != null) {
            lyricsPresenter.getLyrics(musicName, ArtistName);
        }
    }
    private void applyLyrics(LyricsBean bean, String musicTitle) {
        if (bean == null || TextUtils.isEmpty(bean.getSyncedLyrics())) {
            clearCurrentLyrics();
            Toast.makeText(getApplicationContext(), "No synced lyrics: " + musicTitle, LENGTH_SHORT).show();
            return;
        }
        String syncedLyrics = bean.getSyncedLyrics();
        double duration = bean.getDuration();
        boolean saved = lyricsDatabaseHelper.saveLyrics(
                ArtistName,
                musicTitle,
                syncedLyrics,
                duration
        );
        if (saved) {
//            Log.d("LYRICS_CACHE", "Lyrics saved to SQLite: " + musicTitle);
        }
        PlayerState s = PlayerState.get();
        s.parsedLyrics = LrcParser.parse(syncedLyrics);
        s.lyricsDurationSec = (float) duration;
        super.onTrackChanged();
    }

    private void clearCurrentLyrics() {
        PlayerState state = PlayerState.get();
        state.parsedLyrics = new ArrayList<>();
        state.lyricsDurationSec = 0f;
        if (karaokeLyricsController != null) {
            karaokeLyricsController.clear();
        }
        mini.tvMiniLyrics.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (trackInfoAdapter != null) {
            if(isFromDownload){
                loadDownloadedTracksOnly();
                trackInfoAdapter.onDeleteMusicResult(requestCode, resultCode);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        new WindowUtils(this, false, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(hud !=null){
            hud.dismiss();
        }
        checkCurrentTrackLyrics();
        updateAdapterTrackState();
    }

    @Override
    public void onTrackChanged() {
        checkCurrentTrackLyrics();
        updateAdapterTrackState();
    }
    private void syncIndexFromPlayerState() {
        int idx = findPlayingIndex();
        if (idx >= 0 && idx != currentPlayingIndex) {
            currentPlayingIndex = idx;
        }
    }
    @Override
    public void onPlaybackStateUpdated(boolean playing) {
        if (binding != null) {
            updateFabPlayPauseIcon();
        }
        syncIndexFromPlayerState();
    }
    private void updateFabPlayPauseIcon() {
        if (isPlaying) {
            binding.fabPlay.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            binding.fabPlay.setImageResource(android.R.drawable.ic_media_play);
        }
    }

}