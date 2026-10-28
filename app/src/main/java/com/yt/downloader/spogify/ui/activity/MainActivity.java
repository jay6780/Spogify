package com.yt.downloader.spogify.ui.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.yt.downloader.spogify.LyricsEvent;
import com.yt.downloader.spogify.Utils.LrcParser;
import com.yt.downloader.spogify.Utils.PlayerState;
import com.yt.downloader.spogify.mvp.bean.LyricsBean;
import com.yt.downloader.spogify.ui.fragment.HomeFragment;
import com.yt.downloader.spogify.ui.fragment.PlayListFragment;
import com.yt.downloader.spogify.ui.fragment.SearchFragment;

import java.util.ArrayList;

import com.yt.downloader.spogify.Utils.LyricsDatabaseHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends MiniPlayerActivity implements View.OnClickListener {

    private ActivityMainBinding binding;
    private LyricsDatabaseHelper lyricsDatabaseHelper;
    private Fragment homeFragment, searchFragment, playlistFragment;
    private String currentLyricsTrackKey = "";
    public static final int TAB_HOME = 1, TAB_SEARCH = 2, TAB_PLAYLIST = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bindMiniPlayer(binding.layoutMiniPlayer);
        lyricsDatabaseHelper = new LyricsDatabaseHelper(this);
        askNotificationPermission();
        binding.llHome.setOnClickListener(this);
        binding.llPlaylist.setOnClickListener(this);
        binding.llSearch.setOnClickListener(this);
        initFragments(savedInstanceState);
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
        if (!granted) {
            Toast.makeText(this, "Enable notifications to control background playback", Toast.LENGTH_LONG).show();
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

    private void clearCurrentLyrics() {
        currentLyricsTrackKey = "";
        PlayerState state = PlayerState.get();
        state.parsedLyrics = new ArrayList<>();
        state.lyricsDurationSec = 0f;
        if (karaokeLyricsController != null) {
            karaokeLyricsController.clear();
            mini.tvMiniLyrics.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndLoadSavedLyrics();
        onTrackChanged();
    }
    private void checkAndLoadSavedLyrics() {
        PlayerState state = PlayerState.get();
        if (!state.hasTrack || TextUtils.isEmpty(state.title)) {
            clearCurrentLyrics();
            return;
        }
        String trackKey = state.isLocal ? "local:" + state.localPath : "online:" + state.videoId;
        if (TextUtils.equals(currentLyricsTrackKey, trackKey)) {
            return;
        }
        currentLyricsTrackKey = trackKey;
        if (lyricsDatabaseHelper != null) {
            String title = state.title;
            String artist = state.artist == null ? "" : state.artist;
            if (state.queue != null && state.queueIndex >= 0 && state.queueIndex < state.queue.size()) {
                PlayerState.Item item = state.queue.get(state.queueIndex);
                if (item != null) {
                    if (!TextUtils.isEmpty(item.name)) title = item.name;
                    if (!TextUtils.isEmpty(item.artist)) artist = item.artist;
                }
            }
            LyricsDatabaseHelper.CachedLyrics cached = lyricsDatabaseHelper.getLyrics(artist, title);
            if (cached == null) {
                cached = lyricsDatabaseHelper.getLyricsByMusicName(title);
            }

            if (cached != null && !TextUtils.isEmpty(cached.syncedLyrics)) {
                state.parsedLyrics = LrcParser.parse(cached.syncedLyrics);
                state.setLyrics(cached.syncedLyrics);
                state.setDuration(cached.duration);
                state.lyricsDurationSec = (float) cached.duration;
                super.onTrackChanged();
                return;
            }
        }
        clearCurrentLyrics();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLyricsEvent(LyricsEvent event) {
        if (event != null) {
            applyLyrics(event.getBean(), event.getMusicTitle());
        }
    }
    private void applyLyrics(LyricsBean bean, String musicTitle) {
        if (bean == null || TextUtils.isEmpty(bean.getSyncedLyrics())) {
            clearCurrentLyrics();
            return;
        }
        String syncedLyrics = bean.getSyncedLyrics();
        double duration = bean.getDuration();

        if (lyricsDatabaseHelper != null) {
            lyricsDatabaseHelper.saveLyrics(
                    bean.getArtistName(),
                    musicTitle,
                    syncedLyrics,
                    duration
            );
        }

        PlayerState s = PlayerState.get();
        s.parsedLyrics = LrcParser.parse(syncedLyrics);
        s.setLyrics(syncedLyrics);
        s.setDuration(duration);
        s.lyricsDurationSec = (float) duration;

        super.onTrackChanged();
    }

    @Override
    protected void onDestroy() {
        if (lyricsDatabaseHelper != null) {
            lyricsDatabaseHelper.close();
            lyricsDatabaseHelper = null;
        }
        super.onDestroy();
    }
}