package com.yt.downloader.spogify.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yt.downloader.R;
import com.yt.downloader.databinding.FragmentPlayListBinding;
import com.yt.downloader.spogify.Utils.FavoriteDatabaseHelper;
import com.yt.downloader.spogify.Utils.FollowDatabaseHelper;
import com.yt.downloader.spogify.Utils.fileUtils.FilesExtractor;
import com.yt.downloader.spogify.Utils.fileUtils.VideoFile;
import com.yt.downloader.spogify.mvp.bean.FavoriteTrack;
import com.yt.downloader.spogify.mvp.bean.FollowedArtist;
import com.yt.downloader.spogify.ui.activity.TrackInfoActivity;
import com.yt.downloader.spogify.ui.adapter.FavoriteTrackAdapter;
import com.yt.downloader.spogify.ui.adapter.FollowedArtistAdapter;

import java.util.ArrayList;
import java.util.List;

public class PlayListFragment extends Fragment {

    private FragmentPlayListBinding binding;
    private FollowDatabaseHelper followDbHelper;
    private FavoriteDatabaseHelper favDbHelper;
    private FilesExtractor filesExtractor;

    private FollowedArtistAdapter followAdapter;
    private FavoriteTrackAdapter favAdapter;
    private FavoriteTrackAdapter downloadAdapter;

    private final ActivityResultLauncher<String[]> storagePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                loadData();
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPlayListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        followDbHelper = new FollowDatabaseHelper(requireContext());
        favDbHelper = new FavoriteDatabaseHelper(requireContext());
        filesExtractor = new FilesExtractor(requireContext());

        initRecyclers();
        checkStoragePermissionAndLoad();
    }

    private void checkStoragePermissionAndLoad() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                storagePermissionLauncher.launch(new String[]{
                        Manifest.permission.READ_MEDIA_AUDIO
                });
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                storagePermissionLauncher.launch(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                });
                return;
            }
        }
        loadData();
    }

    private void initRecyclers() {
        followAdapter = new FollowedArtistAdapter(R.layout.item_top_artist);
        binding.rvfollowArtist.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvfollowArtist.setAdapter(followAdapter);

        followAdapter.setOnArtistClickListener(artist -> {
            Intent intent = new Intent(requireContext(), TrackInfoActivity.class);
            intent.putExtra("ArtistName", artist.getArtistsName());
            intent.putExtra("isFromFavorite", false);
            intent.putExtra("isFromDownload", false);
            startActivity(intent);
        });

        favAdapter = new FavoriteTrackAdapter(R.layout.item_top_artist);
        binding.rvFavoriteMusic.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvFavoriteMusic.setAdapter(favAdapter);

        favAdapter.setOnFavoriteClickListener(track -> {
            Intent intent = new Intent(requireContext(), TrackInfoActivity.class);
            intent.putExtra("ArtistName", track.getArtistName());
            intent.putExtra("SelectedTrackName", track.getMusicName());
            intent.putExtra("isFromFavorite", true);
            intent.putExtra("isFromDownload", false);
            startActivity(intent);
        });

        downloadAdapter = new FavoriteTrackAdapter(R.layout.item_top_artist);
        binding.rvDownloadedMusic.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.rvDownloadedMusic.setAdapter(downloadAdapter);

        downloadAdapter.setOnFavoriteClickListener(track -> {
            Intent intent = new Intent(requireContext(), TrackInfoActivity.class);
            intent.putExtra("ArtistName", "Downloaded");
            intent.putExtra("SelectedTrackName", track.getMusicName());
            intent.putExtra("isFromFavorite", false);
            intent.putExtra("isFromDownload", true);
            startActivity(intent);
        });
    }

    private void loadData() {
        List<FollowedArtist> artistList = followDbHelper.getAllFollowedArtists();
        List<FavoriteTrack> favoriteList = favDbHelper.getAllFavoriteTracks();

        ArrayList<VideoFile> downloadedFiles = filesExtractor.listVideos();
        List<FavoriteTrack> downloadedTrackList = new ArrayList<>();

        if (downloadedFiles != null) {
            for (VideoFile vf : downloadedFiles) {
                String rawName = vf.getName();
                String cleanName = rawName.endsWith(".mp3") ? rawName.substring(0, rawName.length() - 4) : rawName;
                downloadedTrackList.add(new FavoriteTrack(cleanName, "Local Track", "", "Downloaded"));
            }
        }

        boolean hasArtists = artistList != null && !artistList.isEmpty();
        boolean hasFavorites = favoriteList != null && !favoriteList.isEmpty();
        boolean hasDownloads = !downloadedTrackList.isEmpty();

        if (hasArtists) {
            binding.llFollowArtist.setVisibility(View.VISIBLE);
            followAdapter.setNewData(artistList);
        } else {
            binding.llFollowArtist.setVisibility(View.GONE);
            followAdapter.setNewData(null);
        }

        if (hasFavorites) {
            binding.llFavoriteMusic.setVisibility(View.VISIBLE);
            favAdapter.setNewData(favoriteList);
        } else {
            binding.llFavoriteMusic.setVisibility(View.GONE);
            favAdapter.setNewData(null);
        }

        if (hasDownloads) {
            binding.llDownloadedMusic.setVisibility(View.VISIBLE);
            downloadAdapter.setNewData(downloadedTrackList);
        } else {
            binding.llDownloadedMusic.setVisibility(View.GONE);
            downloadAdapter.setNewData(null);
        }

        if (!hasArtists && !hasFavorites && !hasDownloads) {
            binding.llEmpty.setVisibility(View.VISIBLE);
            binding.llFullContent.setVisibility(View.GONE);
        } else {
            binding.llEmpty.setVisibility(View.GONE);
            binding.llFullContent.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkStoragePermissionAndLoad();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}