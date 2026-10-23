package com.yt.downloader.spogify.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.yt.downloader.R;
import com.yt.downloader.databinding.FragmentHomeBinding;
import com.yt.downloader.spogify.Retrofit.AppConstant;
import com.yt.downloader.spogify.mvp.bean.OpmBean;
import com.yt.downloader.spogify.mvp.bean.RAndBbean;
import com.yt.downloader.spogify.mvp.bean.TopArtistBean;
import com.yt.downloader.spogify.mvp.bean.TopTrackBean;
import com.yt.downloader.spogify.mvp.contract.HomeContract;
import com.yt.downloader.spogify.mvp.presenter.HomePresenter;
import com.yt.downloader.spogify.ui.adapter.OpmAdapter;
import com.yt.downloader.spogify.ui.adapter.RnBAdapter;
import com.yt.downloader.spogify.ui.adapter.TopArtistAdapter;
import com.yt.downloader.spogify.ui.adapter.TopTrackAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements HomeContract.View {

    private FragmentHomeBinding binding;
    private HomePresenter homePresenter;
    private TopArtistAdapter topArtistAdapter;
    private TopTrackAdapter topTrackAdapter;
    private OpmAdapter opmAdapter;
    private RnBAdapter rnBAdapter;
    private List<TopArtistBean.ArtistsBean.ArtistBean> topArtistList = new ArrayList<>();
    private List<TopTrackBean.Track> topTrackList = new ArrayList<>();
    private List<OpmBean.Artist> opmList = new ArrayList<>();
    private List<RAndBbean.Artist> RnbList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        homePresenter = new HomePresenter(this);
        initApi();
        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
    }

    private void initApi() {
        homePresenter.getTopTrackQuery(AppConstant.TopTracks, AppConstant.Api_Key, "json");
        homePresenter.getTopArtistQuery(AppConstant.TopArtist, AppConstant.Api_Key, "json");
        homePresenter.getOpmQuery(AppConstant.OPM, "opm", AppConstant.Api_Key, "json");
        homePresenter.getRnBQuery(AppConstant.RNB, "rnb", AppConstant.Api_Key, "json");
    }

    private void refreshData() {
        try {
            if (!isNetworkAvailable()) {
                if (binding.swipeRefresh.isRefreshing()) {
                    binding.swipeRefresh.setRefreshing(false);
                }
                Toast.makeText(getContext(), "Please check network and try again", Toast.LENGTH_SHORT).show();
                return;
            }
            topArtistList.clear();
            topTrackList.clear();
            opmList.clear();
            RnbList.clear();
            topArtistAdapter.setNewData(topArtistList);
            topTrackAdapter.setNewData(topTrackList);
            opmAdapter.setNewData(opmList);
            rnBAdapter.setNewData(RnbList);

            binding.rvTopArtists.scrollToPosition(0);
            binding.rvTopTracks.scrollToPosition(0);
            binding.rvOpm.scrollToPosition(0);
            binding.rvRnb.scrollToPosition(0);
            binding.llTopTracks.setVisibility(View.GONE);
            binding.llTopArtist.setVisibility(View.GONE);
            binding.llOpm.setVisibility(View.GONE);
            binding.llRnb.setVisibility(View.GONE);

            initApi();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("MissingPermission")
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void setupRecyclerView() {
        topArtistAdapter = new TopArtistAdapter();
        topTrackAdapter = new TopTrackAdapter();
        opmAdapter = new OpmAdapter();
        rnBAdapter = new RnBAdapter();

        binding.rvTopArtists.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvTopArtists.setAdapter(topArtistAdapter);

        binding.rvTopTracks.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvTopTracks.setAdapter(topTrackAdapter);

        binding.rvOpm.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvOpm.setAdapter(opmAdapter);

        binding.rvRnb.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvRnb.setAdapter(rnBAdapter);
    }

    @Override
    public void showLoading() {
        binding.swipeRefresh.setRefreshing(true);
    }

    @Override
    public void showError(String error) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "Error fetching data: " + error, Toast.LENGTH_SHORT).show();
                binding.swipeRefresh.setRefreshing(false);
            }
        }, 500);
    }

    @Override
    public void hideLoading() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.swipeRefresh.setRefreshing(false);
            }
        }, 500);
    }


    @Override
    public void getTopArtistSuccess(TopArtistBean bean) {
        if (bean != null && bean.getArtists() != null && !bean.getArtists().getArtist().isEmpty()) {
            topArtistList.addAll(bean.getArtists().getArtist());
            topArtistAdapter.setNewData(topArtistList);
            binding.llTopArtist.setVisibility(View.VISIBLE);
        } else {
            binding.llTopArtist.setVisibility(View.GONE);
        }
    }

    @Override
    public void getTopTracksSuccess(TopTrackBean topTrackBean) {
        if (topTrackBean != null && topTrackBean.getTracks() != null && !topTrackBean.getTracks().getTrack().isEmpty()) {
            topTrackList.addAll(topTrackBean.getTracks().getTrack());
            topTrackAdapter.setNewData(topTrackList);
            binding.llTopTracks.setVisibility(View.VISIBLE);
        } else {
            binding.llTopTracks.setVisibility(View.GONE);
        }
    }

    @Override
    public void getOpmSuccess(OpmBean opmBean) {
        if (opmBean != null && opmBean.getTopArtists() != null && !opmBean.getTopArtists().getArtist().isEmpty()) {
            opmList.addAll(opmBean.getTopArtists().getArtist());
            opmAdapter.setNewData(opmList);
            binding.llOpm.setVisibility(View.VISIBLE);
        } else {
            binding.llOpm.setVisibility(View.GONE);
        }
    }

    @Override
    public void getRnBSuccess(RAndBbean rAndBbean) {
        if (rAndBbean != null && rAndBbean.getTopArtists() != null && !rAndBbean.getTopArtists().getArtist().isEmpty()) {
            RnbList.addAll(rAndBbean.getTopArtists().getArtist());
            rnBAdapter.setNewData(RnbList);
            binding.llRnb.setVisibility(View.VISIBLE);
        } else {
            binding.llRnb.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}