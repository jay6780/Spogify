package com.yt.downloader.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.yt.downloader.AdapterUtils.base.BaseQuickAdapter;
import com.yt.downloader.R;
import com.yt.downloader.Retrofit.AppConstant;
import com.yt.downloader.SharedPreference.SPUtils;
import com.yt.downloader.adapter.MusicListAdapter;
import com.yt.downloader.databinding.FragmentHomeBinding;
import com.yt.downloader.mvp.bean.YoutubeSearchBean;
import com.yt.downloader.mvp.contract.MusicListContract;
import com.yt.downloader.mvp.presenter.MusicListPresenter;
import com.yt.downloader.ui.activity.VideoWatchActivity;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements View.OnClickListener, MusicListContract.View {
    private FragmentHomeBinding binding;
    private MusicListAdapter musicListAdapter;
    private MusicListPresenter musicListPresenter;
    private List<YoutubeSearchBean.ItemsBean> itemsBeans = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.ivBtnSearch.setOnClickListener(this);
        initRecycler();
        musicListPresenter = new MusicListPresenter(this);


        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        return binding.getRoot();
    }

    private void initRecycler() {
        binding.rvMusicList.setLayoutManager(new LinearLayoutManager(getContext()));
        musicListAdapter = new MusicListAdapter();
        binding.rvMusicList.setAdapter(musicListAdapter);
        musicListAdapter.setEmptyView(R.layout.layout_empty_music, binding.rvMusicList);
        musicListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                String videoId = musicListAdapter.getData().get(position).getId();
                String title = musicListAdapter.getData().get(position).getTitle();
                String thumbnail = musicListAdapter.getData().get(position).getThumbnail();
                startActivity(new Intent(getContext(), VideoWatchActivity.class)
                        .putExtra("videoId", videoId)
                        .putExtra("title", title)
                        .putExtra("tumbnail",thumbnail));
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_btn_search) {
            hideKeyboard(getActivity());
            refreshData();
        }

    }

    private void refreshData() {

        String musicQuery = binding.etSearch.getText().toString().trim();
        if (TextUtils.isEmpty(musicQuery)) {
            binding.swipeRefresh.setRefreshing(false);
            return;
        }

        if (TextUtils.isEmpty(musicQuery)) {
            Toast.makeText(getContext(), "Please enter music name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isNetworkAvailable()) {
            if (binding.swipeRefresh.isRefreshing()) {
                binding.swipeRefresh.setRefreshing(false);
            }
            Toast.makeText(getContext(), "Please check network and try again", Toast.LENGTH_SHORT).show();
            return;
        }

        SPUtils.getInstance().put(AppConstant.recentSearch,musicQuery);
        if (!itemsBeans.isEmpty()) {
            itemsBeans.clear();
            musicListAdapter.setNewData(itemsBeans);
        }
        musicListPresenter.getSearchListQuery(musicQuery);
    }

    private static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @SuppressWarnings("deprecation")
    @SuppressLint("MissingPermission")
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
    public void getSearchList(YoutubeSearchBean youtubeSearchBean) {
        if (youtubeSearchBean != null && !youtubeSearchBean.getItems().isEmpty()) {
            itemsBeans.addAll(youtubeSearchBean.getItems());
            musicListAdapter.setNewData(itemsBeans);
        }

    }
}