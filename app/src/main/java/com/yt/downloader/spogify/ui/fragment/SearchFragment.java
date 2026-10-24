package com.yt.downloader.spogify.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.yt.downloader.R;
import com.yt.downloader.databinding.FragmentSearchBinding;
import com.yt.downloader.spogify.Retrofit.AppConstant;
import com.yt.downloader.spogify.SharedPreference.SPUtils;
import com.yt.downloader.spogify.base.BaseQuickAdapter;
import com.yt.downloader.spogify.mvp.bean.DeezerArtistBean;
import com.yt.downloader.spogify.mvp.bean.SearchArtistBean;
import com.yt.downloader.spogify.mvp.bean.SearchTotalBean;
import com.yt.downloader.spogify.mvp.bean.YoutubeSearchBean;
import com.yt.downloader.spogify.mvp.contract.MusicListContract;
import com.yt.downloader.spogify.mvp.contract.SearchArtistContract;
import com.yt.downloader.spogify.mvp.presenter.MusicListPresenter;
import com.yt.downloader.spogify.mvp.presenter.SearchArtistPresenter;
import com.yt.downloader.spogify.ui.activity.TrackInfoActivity;
import com.yt.downloader.spogify.ui.activity.VideoWatchActivity;
import com.yt.downloader.spogify.ui.adapter.MusicListAdapter;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class SearchFragment extends Fragment implements View.OnClickListener, MusicListContract.View, SearchArtistContract.View {
    private FragmentSearchBinding binding;
    private MusicListAdapter musicListAdapter;
    private MusicListPresenter musicListPresenter;
    private List<SearchTotalBean> itemsBeans = new ArrayList<>();

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private SearchArtistPresenter searchArtistPresenter;
    private String ArtistQuery = "";
    private final Map<String, String> imageCache = new ConcurrentHashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        binding.ivBtnSearch.setOnClickListener(this);

        initRecycler();
        initTextWatcher();
        initKeyboardSearch();

        musicListPresenter = new MusicListPresenter(this);
        searchArtistPresenter = new SearchArtistPresenter(this);
        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        return binding.getRoot();
    }

    private void initKeyboardSearch() {
        binding.etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }

                    hideKeyboard(getActivity());
                    refreshData();
                    return true;
                }
                return false;
            }
        });
    }

    private void initTextWatcher() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    binding.ivBtnSearch.setVisibility(View.VISIBLE);
                } else {
                    binding.ivBtnSearch.setVisibility(View.GONE);
                }

                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(final Editable s) {
                searchRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (!TextUtils.isEmpty(s.toString().trim())) {
                            refreshData();
                        }
                    }
                };
                searchHandler.postDelayed(searchRunnable, 1000);
            }
        });
    }

    private void initRecycler() {
        binding.rvMusicList.setLayoutManager(new LinearLayoutManager(getContext()));
        musicListAdapter = new MusicListAdapter();
        binding.rvMusicList.setAdapter(musicListAdapter);
        musicListAdapter.setEmptyView(R.layout.layout_empty_music, binding.rvMusicList);
        musicListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                SearchTotalBean selectedItem = musicListAdapter.getData().get(position);
                if (selectedItem.isArtist()) {
                    startActivity(new Intent(getContext(), TrackInfoActivity.class)
                            .putExtra("ArtistName", selectedItem.getName()));
                } else {
                    startActivity(new Intent(getContext(), VideoWatchActivity.class)
                            .putExtra("videoId", selectedItem.getVideoId())
                            .putExtra("title", selectedItem.getName())
                            .putExtra("tumbnail", selectedItem.getThumbnail()));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (musicListAdapter != null) {
            musicListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_btn_search) {
            binding.etSearch.setText("");
            binding.ivBtnSearch.setVisibility(View.GONE);
            itemsBeans.clear();
            if (musicListAdapter != null) {
                musicListAdapter.setNewData(itemsBeans);
            }
        }
    }

    private void refreshData() {
        String musicQuery = binding.etSearch.getText().toString().trim();
        if (TextUtils.isEmpty(musicQuery)) {
            binding.swipeRefresh.setRefreshing(false);
            return;
        }

        if (!isNetworkAvailable()) {
            if (binding.swipeRefresh.isRefreshing()) {
                binding.swipeRefresh.setRefreshing(false);
            }
            Toast.makeText(getContext(), "Please check network and try again", Toast.LENGTH_SHORT).show();
            return;
        }

        this.ArtistQuery = musicQuery;
        SPUtils.getInstance().put(AppConstant.recentSearch, musicQuery);
        if (!itemsBeans.isEmpty()) {
            itemsBeans.clear();
            musicListAdapter.setNewData(itemsBeans);
        }
        musicListPresenter.getSearchListQuery(musicQuery);
        searchArtistPresenter.getSearchArtistQuery(AppConstant.SearchArtist, musicQuery, AppConstant.Api_Key, "json");
    }

    private static void hideKeyboard(Activity activity) {
        if (activity == null) return;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("MissingPermission")
    private boolean isNetworkAvailable() {
        if (getActivity() == null) return false;
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
    public void getSearchAristSuccess(SearchArtistBean searchArtistBean) {
        if (searchArtistBean != null
                && searchArtistBean.getResults() != null
                && searchArtistBean.getResults().getArtistMatches() != null
                && searchArtistBean.getResults().getArtistMatches().getArtistList() != null
                && !searchArtistBean.getResults().getArtistMatches().getArtistList().isEmpty()) {

            List<SearchArtistBean.Artist> artistList = searchArtistBean.getResults().getArtistMatches().getArtistList();
            SearchArtistBean.Artist matchedArtist = null;

            for (SearchArtistBean.Artist artist : artistList) {
                if (artist.getName() != null && artist.getName().equalsIgnoreCase(ArtistQuery)) {
                    matchedArtist = artist;
                    break;
                }
            }

            if (matchedArtist == null) {
                matchedArtist = artistList.get(0);
            }

            final String artistName = matchedArtist.getName();

            if (imageCache.containsKey(artistName)) {
                addArtistToAdapter(artistName, imageCache.get(artistName));
            } else {
                fetchDeezerArtist(artistName);
            }
        }
    }

    private void fetchDeezerArtist(String artistName) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String query = URLEncoder.encode(artistName, "UTF-8");
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

                        imageCache.put(artistName, imageUrl);

                        new Handler(Looper.getMainLooper()).post(() -> addArtistToAdapter(artistName, imageUrl));
                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> addArtistToAdapter(artistName, ""));
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> addArtistToAdapter(artistName, ""));
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> addArtistToAdapter(artistName, ""));
            }
        });
    }

    private void addArtistToAdapter(String artistName, String imageUrl) {
        SearchTotalBean artistBean = new SearchTotalBean(
                imageUrl,
                artistName,
                "Artist",
                "",
                true
        );

        if (!itemsBeans.isEmpty() && itemsBeans.get(0).isArtist()) {
            itemsBeans.set(0, artistBean);
        } else {
            itemsBeans.add(0, artistBean);
        }

        if (musicListAdapter != null) {
            musicListAdapter.setNewData(new ArrayList<>(itemsBeans));
        }
    }

    @Override
    public void getSearchList(YoutubeSearchBean youtubeSearchBean) {
        if (youtubeSearchBean != null && youtubeSearchBean.getItems() != null && !youtubeSearchBean.getItems().isEmpty()) {
            SearchTotalBean existingArtist = (!itemsBeans.isEmpty() && itemsBeans.get(0).isArtist())
                    ? itemsBeans.get(0)
                    : null;

            itemsBeans.clear();
            if (existingArtist != null) {
                itemsBeans.add(existingArtist);
            }

            List<YoutubeSearchBean.ItemsBean> fetchedItems = youtubeSearchBean.getItems();
            for (YoutubeSearchBean.ItemsBean item : fetchedItems) {
                SearchTotalBean bean = new SearchTotalBean(
                        item.getThumbnail(),
                        item.getTitle(),
                        item.getDuration(),
                        item.getId(),
                        false
                );
                itemsBeans.add(bean);
            }

            musicListAdapter.setNewData(new ArrayList<>(itemsBeans));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        binding = null;
    }
}