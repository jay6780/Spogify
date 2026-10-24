package com.yt.downloader.spogify.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.yt.downloader.R;
import com.yt.downloader.databinding.FragmentPlayListBinding;
import com.yt.downloader.spogify.Utils.FavoriteDatabaseHelper;
import com.yt.downloader.spogify.Utils.FollowDatabaseHelper;
import com.yt.downloader.spogify.mvp.bean.FavoriteTrack;
import com.yt.downloader.spogify.mvp.bean.FollowedArtist;
import com.yt.downloader.spogify.ui.activity.TrackInfoActivity;
import com.yt.downloader.spogify.ui.adapter.FavoriteTrackAdapter;
import com.yt.downloader.spogify.ui.adapter.FollowedArtistAdapter;

import java.util.List;

public class PlayListFragment extends Fragment {

    private FragmentPlayListBinding binding;
    private FollowDatabaseHelper followDbHelper;
    private FavoriteDatabaseHelper favDbHelper;
    private FollowedArtistAdapter followAdapter;
    private FavoriteTrackAdapter favAdapter;

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

        initRecyclers();
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
            startActivity(intent);
        });
    }

    private void loadData() {
        List<FollowedArtist> artistList = followDbHelper.getAllFollowedArtists();
        List<FavoriteTrack> favoriteList = favDbHelper.getAllFavoriteTracks();

        boolean hasArtists = artistList != null && !artistList.isEmpty();
        boolean hasFavorites = favoriteList != null && !favoriteList.isEmpty();

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

        if (!hasArtists && !hasFavorites) {
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
        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}