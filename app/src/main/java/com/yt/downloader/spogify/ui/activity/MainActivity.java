package com.yt.downloader.spogify.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

import com.yt.downloader.R;
import com.yt.downloader.databinding.ActivityMainBinding;
import com.yt.downloader.spogify.Utils.LrcParser;
import com.yt.downloader.spogify.Utils.PlayerState;
import com.yt.downloader.spogify.mvp.bean.LyricsBean;
import com.yt.downloader.spogify.mvp.contract.LyricsContract;
import com.yt.downloader.spogify.mvp.presenter.LyricsPresenter;
import com.yt.downloader.spogify.ui.fragment.HomeFragment;
import com.yt.downloader.spogify.ui.fragment.PlayListFragment;
import com.yt.downloader.spogify.ui.fragment.SearchFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends MiniPlayerActivity implements View.OnClickListener, LyricsContract.View {

    private ActivityMainBinding binding;

    private Fragment homeFragment, searchFragment, playlistFragment;
    private String currentLyricsTrackKey = "";
    private boolean mainDestroyed = false;
    public static final int TAB_HOME = 1, TAB_SEARCH = 2, TAB_PLAYLIST = 3;

    private LyricsPresenter lyricsPresenter;
    private String musicName = "";
    private String ArtistName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bindMiniPlayer(binding.layoutMiniPlayer);

        lyricsPresenter = new LyricsPresenter(this);

        askNotificationPermission();

        binding.llHome.setOnClickListener(this);
        binding.llPlaylist.setOnClickListener(this);
        binding.llSearch.setOnClickListener(this);

        initFragments(savedInstanceState);

        checkCurrentTrackLyrics();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) {
                    Toast.makeText(this, "Enable notifications to control background playback",
                            Toast.LENGTH_LONG).show();
                }
            });

    private void initFragments(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            homeFragment = getSupportFragmentManager().findFragmentByTag("homeFragment");
            searchFragment = getSupportFragmentManager().findFragmentByTag("searchFragment");
            playlistFragment = getSupportFragmentManager().findFragmentByTag("PlayListFragment");
        }
        switchTab(TAB_HOME);
    }

    private void hideAllFragments(FragmentTransaction transaction) {
        if (homeFragment != null && homeFragment.isAdded()) {
            transaction.hide(homeFragment);
            transaction.setMaxLifecycle(homeFragment, Lifecycle.State.STARTED);
        }
        if (searchFragment != null && searchFragment.isAdded()) {
            transaction.hide(searchFragment);
            transaction.setMaxLifecycle(searchFragment, Lifecycle.State.STARTED);
        }
        if (playlistFragment != null && playlistFragment.isAdded()) {
            transaction.hide(playlistFragment);
            transaction.setMaxLifecycle(playlistFragment, Lifecycle.State.STARTED);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ll_home) {
            switchTab(TAB_HOME);
        } else if (id == R.id.ll_search) {
            switchTab(TAB_SEARCH);
        } else if (id == R.id.ll_playlist) {
            switchTab(TAB_PLAYLIST);
        }
    }

    private void switchTab(int tab) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        hideAllFragments(transaction);

        switch (tab) {
            case TAB_HOME:
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                    transaction.add(R.id.homeFragment, homeFragment, "homeFragment");
                } else {
                    transaction.show(homeFragment);
                }
                transaction.setMaxLifecycle(homeFragment, Lifecycle.State.RESUMED);
                break;

            case TAB_SEARCH:
                if (searchFragment == null) {
                    searchFragment = new SearchFragment();
                    transaction.add(R.id.homeFragment, searchFragment, "searchFragment");
                } else {
                    transaction.show(searchFragment);
                }
                transaction.setMaxLifecycle(searchFragment, Lifecycle.State.RESUMED);
                break;

            case TAB_PLAYLIST:
                if (playlistFragment == null) {
                    playlistFragment = new PlayListFragment();
                    transaction.add(R.id.homeFragment, playlistFragment, "PlayListFragment");
                } else {
                    transaction.show(playlistFragment);
                }
                transaction.setMaxLifecycle(playlistFragment, Lifecycle.State.RESUMED);
                break;

            default:
                return;
        }
        changeTabUi(tab);
        transaction.commitAllowingStateLoss();
    }

    private void changeTabUi(int tabNum) {
        switch (tabNum) {
            case TAB_HOME:
                binding.tvHome.setTextColor(Color.parseColor("#ffffff"));
                binding.tvSearch.setTextColor(Color.parseColor("#C0C4CC"));
                binding.tvPlaylist.setTextColor(Color.parseColor("#C0C4CC"));

                binding.ivHome.setImageResource(R.mipmap.home_white);
                binding.ivSearch.setImageResource(R.mipmap.search_grey);
                binding.ivPlaylist.setImageResource(R.mipmap.playlist_grey);
                break;

            case TAB_SEARCH:
                binding.tvHome.setTextColor(Color.parseColor("#C0C4CC"));
                binding.tvSearch.setTextColor(Color.parseColor("#ffffff"));
                binding.tvPlaylist.setTextColor(Color.parseColor("#C0C4CC"));

                binding.ivHome.setImageResource(R.mipmap.home_grey);
                binding.ivSearch.setImageResource(R.mipmap.search_white);
                binding.ivPlaylist.setImageResource(R.mipmap.playlist_grey);
                break;

            case TAB_PLAYLIST:
                binding.tvHome.setTextColor(Color.parseColor("#C0C4CC"));
                binding.tvSearch.setTextColor(Color.parseColor("#C0C4CC"));
                binding.tvPlaylist.setTextColor(Color.parseColor("#ffffff"));

                binding.ivHome.setImageResource(R.mipmap.home_grey);
                binding.ivSearch.setImageResource(R.mipmap.search_grey);
                binding.ivPlaylist.setImageResource(R.mipmap.playlist_white);
                break;
        }
    }

    @Override
    public void onTrackChanged() {
        super.onTrackChanged();
        checkCurrentTrackLyrics();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkCurrentTrackLyrics();
    }

    @Override
    protected void onDestroy() {
        mainDestroyed = true;
        super.onDestroy();
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void showError(String error) {
        if (!mainDestroyed) {
//            Log.e("LYRICS_DEBUG", "Lyrics error: " + error);
//            Toast.makeText(getApplicationContext(),"Lyrics not found",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void getLyricsSuccess(List<LyricsBean> lyricsList) {

        if (mainDestroyed) {
            return;
        }

        PlayerState s = PlayerState.get();

        if (!s.hasTrack) {
            return;
        }

        String playingKey = s.isLocal
                ? "local:" + s.localPath
                : "online:" + s.videoId;

        if (!TextUtils.equals(currentLyricsTrackKey, playingKey)) {
            return;
        }
        applyLyrics(lyricsList, musicName, ArtistName);
    }

    private void checkCurrentTrackLyrics() {
        if (!isNetworkAvailable()) {
            clearCurrentLyrics();
            return;
        }
        PlayerState state = PlayerState.get();

        if (!state.hasTrack) {
            currentLyricsTrackKey = "";
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

                if (matches && !TextUtils.isEmpty(item.name)) {
                    title = item.name;
                }

                if (matches && !TextUtils.isEmpty(item.artist)) {
                    artist = item.artist;
                }
            }
        }

        String musicTitle = cleanMusicTitle(title);

        if (TextUtils.isEmpty(musicTitle)) {
            clearCurrentLyrics();
            return;
        }

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
        if (TextUtils.isEmpty(title)) {
            return "";
        }

        String clean = title.trim();

        clean = clean.replaceAll("(?i)\\([^)]*(official|lyrics?|audio|video|visualizer|remaster)[^)]*\\)", "");

        clean = clean.replaceAll("(?i)\\[[^]]*(official|lyrics?|audio|video|visualizer|remaster)[^]]*\\]", "");

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

        clearCurrentLyrics();

//        Log.d("LYRICS_DEBUG", "Searching music: " + musicName + " | Artist: " + ArtistName);

        if (lyricsPresenter != null && !TextUtils.isEmpty(musicName)) {
            lyricsPresenter.getLyrics(musicName);
        }
    }

    private void applyLyrics(List<LyricsBean> lyricsList, String musicTitle, String artistName) {
        if (lyricsList == null || lyricsList.isEmpty()) {
            clearCurrentLyrics();

//            Log.d("LYRICS_DEBUG", "No lyrics found for " + musicTitle);
            return;
        }
        String syncedLyrics = "";

        for (LyricsBean bean : lyricsList) {

            if (bean == null || TextUtils.isEmpty(bean.getSyncedLyrics())) {
                continue;
            }

            String apiTitle = cleanMusicTitle(bean.getTrackName());

            String apiArtist = bean.getArtistName() == null ? "" : bean.getArtistName();

            boolean titleMatches = apiTitle.equalsIgnoreCase(musicTitle);

            boolean artistMatches = TextUtils.isEmpty(artistName)
                    || (!TextUtils.isEmpty(apiArtist)
                    && (apiArtist.toLowerCase(Locale.ROOT).contains(artistName.toLowerCase(Locale.ROOT))
                    || artistName.toLowerCase(Locale.ROOT).contains(apiArtist.toLowerCase(Locale.ROOT))));

            if (titleMatches && artistMatches) {
                syncedLyrics = bean.getSyncedLyrics();
                break;
            }
        }

        if (TextUtils.isEmpty(syncedLyrics)) {
            for (LyricsBean bean : lyricsList) {

                if (bean == null || TextUtils.isEmpty(bean.getSyncedLyrics())) {
                    continue;
                }

                if (cleanMusicTitle(bean.getTrackName()).equalsIgnoreCase(musicTitle)) {
                    syncedLyrics = bean.getSyncedLyrics();
                    break;
                }
            }
        }
        if (TextUtils.isEmpty(syncedLyrics)) {

            clearCurrentLyrics();

//            Log.d("LYRICS_DEBUG", "No synced lyrics for " + musicTitle);
            return;
        }

        PlayerState state = PlayerState.get();

        state.parsedLyrics = LrcParser.parse(syncedLyrics);

        super.onTrackChanged();
//
//        Log.d("LYRICS_DEBUG", "Lyrics updated: " + musicTitle);
    }

    private void clearCurrentLyrics() {

        PlayerState state = PlayerState.get();

        state.parsedLyrics = new ArrayList<>();

        if (karaokeLyricsController != null) {
            karaokeLyricsController.clear();
        }

        if (mini != null && mini.tvMiniLyrics != null) {
            mini.tvMiniLyrics.setVisibility(View.GONE);
        }
    }

}