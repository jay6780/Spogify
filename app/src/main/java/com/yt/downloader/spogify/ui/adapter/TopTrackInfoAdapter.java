package com.yt.downloader.spogify.ui.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.yt.downloader.R;
import com.yt.downloader.spogify.Utils.EqualizerView;
import com.yt.downloader.spogify.Utils.FavoriteDatabaseHelper;
import com.yt.downloader.spogify.Utils.fileUtils.FilesExtractor;
import com.yt.downloader.spogify.base.BaseQuickAdapter;
import com.yt.downloader.spogify.base.BaseViewHolder;
import com.yt.downloader.spogify.mvp.bean.DeezerTrackBean;
import com.yt.downloader.spogify.mvp.bean.TrackInfoBean;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class TopTrackInfoAdapter extends BaseQuickAdapter<TrackInfoBean.Track, BaseViewHolder> {

    public interface OnTrackClickListener {
        void onTrackClick(TrackInfoBean.Track track, int position, boolean isDownloaded);
    }

    public interface OnFavoriteRemovedListener {
        void onFavoriteRemoved();
    }

    public interface OnDownloadClickListener {
        void onDownloadClick(TrackInfoBean.Track track, int position);
    }

    private OnTrackClickListener listener;
    private OnFavoriteRemovedListener favoriteRemovedListener;
    private OnDownloadClickListener downloadClickListener;
    private String artistName = "";
    private final Map<String, String> imageCache = new ConcurrentHashMap<>();
    private final Map<String, Integer> downloadProgressMap = new ConcurrentHashMap<>();
    private FavoriteDatabaseHelper favDbHelper;
    private FilesExtractor filesExtractor;

    private String currentPlayingTrackName = "";
    private boolean isPlaying = false;

    public TopTrackInfoAdapter() {
        super(R.layout.item_popular_track);
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setOnTrackClickListener(OnTrackClickListener listener) {
        this.listener = listener;
    }

    public void setOnFavoriteRemovedListener(OnFavoriteRemovedListener listener) {
        this.favoriteRemovedListener = listener;
    }

    public void setOnDownloadClickListener(OnDownloadClickListener listener) {
        this.downloadClickListener = listener;
    }

    public void updateDownloadProgress(String trackName, int progress) {
        if (progress >= 100) {
            downloadProgressMap.remove(trackName);
        } else {
            downloadProgressMap.put(trackName, progress);
        }
        notifyDataSetChanged();
    }

    public void setPlayingTrackName(String trackName, boolean playing) {
        if (TextUtils.equals(this.currentPlayingTrackName, trackName) && this.isPlaying == playing) {
            return;
        }
        this.currentPlayingTrackName = trackName != null ? trackName : "";
        this.isPlaying = playing;

        if (getRecyclerView() != null) {
            getRecyclerView().post(this::notifyDataSetChanged);
        } else {
            notifyDataSetChanged();
        }
    }

    @Override
    protected void convert(BaseViewHolder helper, TrackInfoBean.Track item) {
        if (favDbHelper == null) {
            favDbHelper = new FavoriteDatabaseHelper(mContext);
        }
        if (filesExtractor == null) {
            filesExtractor = new FilesExtractor(mContext);
        }

        TextView tv_rank = helper.getView(R.id.tv_rank);
        TextView tv_track_title = helper.getView(R.id.tv_track_title);
        TextView tv_play_count = helper.getView(R.id.tv_play_count);
        ImageView iv_track_cover = helper.getView(R.id.iv_track_cover);
        ImageView iv_add_favorite = helper.getView(R.id.iv_add_favorite);
        ImageView iv_download = helper.getView(R.id.iv_download);
        ProgressBar pb_download_progress = helper.getView(R.id.pb_download_progress);
        EqualizerView equalizerView = helper.getView(R.id.equalizer_view);

        int position = helper.getAdapterPosition();

        boolean isDownloaded = filesExtractor.isFileDownloaded(item.getName());
        boolean isCurrentPlaying = item != null
                && !TextUtils.isEmpty(item.getName())
                && item.getName().equalsIgnoreCase(currentPlayingTrackName);

        if (item.getAttributes() != null) {
            tv_rank.setText(item.getAttributes().getRank());
        }
        tv_track_title.setText(item.getName());
        tv_play_count.setText(item.getPlaycount());

        if (isCurrentPlaying) {
            tv_track_title.setTextColor(Color.parseColor("#1DB954"));
            tv_rank.setVisibility(View.GONE);
            equalizerView.setVisibility(View.VISIBLE);
            equalizerView.setPlaying(true);
        } else {
            tv_track_title.setTextColor(Color.WHITE);
            tv_rank.setVisibility(View.VISIBLE);
            equalizerView.setVisibility(View.GONE);
            equalizerView.setPlaying(false);
        }


        if (isDownloaded) {
            iv_download.setImageResource(android.R.drawable.stat_sys_download_done);
            iv_download.setColorFilter(Color.parseColor("#1DB954"));
        } else {
            iv_download.setImageResource(android.R.drawable.stat_sys_download);
            iv_download.setColorFilter(Color.parseColor("#B3B3B3"));
        }

        boolean isFavorite = favDbHelper.isTrackFavorite(item.getName());
        updateAddButtonIcon(iv_add_favorite, isFavorite);

        iv_add_favorite.setOnClickListener(v -> {
            boolean currentlyFav = favDbHelper.isTrackFavorite(item.getName());
            if (currentlyFav) {
                favDbHelper.removeFavoriteTrack(item.getName());
                Toast.makeText(mContext, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                updateAddButtonIcon(iv_add_favorite, false);

                if (favoriteRemovedListener != null) {
                    favoriteRemovedListener.onFavoriteRemoved();
                }
            } else {
                String imageThumb = !TextUtils.isEmpty(item.getImageUrl()) ? item.getImageUrl() : imageCache.getOrDefault(item.getName(), "");
                String totalListeners = !TextUtils.isEmpty(item.getListeners()) ? item.getListeners() : item.getPlaycount();

                favDbHelper.addFavoriteTrack(item.getName(), artistName, imageThumb, totalListeners);
                Toast.makeText(mContext, "Added to Favorites", Toast.LENGTH_SHORT).show();
                updateAddButtonIcon(iv_add_favorite, true);
            }
        });

        iv_download.setOnClickListener(v -> {
            if (isDownloaded) {
                Toast.makeText(mContext, "Already downloaded", Toast.LENGTH_SHORT).show();
            } else if (downloadClickListener != null) {
                Toast.makeText(mContext, "Downloading: "+item.getName(), Toast.LENGTH_SHORT).show();
                downloadClickListener.onDownloadClick(item, position);
            }
        });

        if (downloadProgressMap.containsKey(item.getName())) {
            int progress = downloadProgressMap.get(item.getName());
            pb_download_progress.setVisibility(View.VISIBLE);
            pb_download_progress.setProgress(progress);
        } else {
            pb_download_progress.setVisibility(View.GONE);
        }

        String trackTitle = item.getName();
        iv_track_cover.setTag(trackTitle);

        helper.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTrackClick(item, position, isDownloaded);
            }
        });

        if (item.getImageUrl() != null && item.getImageUrl().contains("i.ytimg.com")) {
            loadImage(iv_track_cover, item.getImageUrl());
        } else {
            if (imageCache.containsKey(trackTitle)) {
                String cachedUrl = imageCache.get(trackTitle);
                loadImage(iv_track_cover, cachedUrl);
            } else {
                Glide.with(mContext)
                        .load(R.mipmap.app_logo)
                        .into(iv_track_cover);

                fetchDeezerTrackCover(trackTitle, iv_track_cover);
            }
        }
    }

    private void updateAddButtonIcon(ImageView imageView, boolean isFavorite) {
        if (isFavorite) {
            imageView.setImageResource(android.R.drawable.checkbox_on_background);
            imageView.setImageResource(android.R.drawable.ic_menu_add);
        } else {
            imageView.setImageResource(android.R.drawable.ic_input_add);
            imageView.setColorFilter(Color.parseColor("#B3B3B3"));
        }
    }

    private void fetchDeezerTrackCover(String trackTitle, ImageView imageView) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String query = URLEncoder.encode(trackTitle, "UTF-8");
                String deezerUrl = "https://api.deezer.com/search/track?q=" + query;

                URL url = new URL(deezerUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                    DeezerTrackBean response = new Gson().fromJson(reader, DeezerTrackBean.class);
                    reader.close();

                    if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                        DeezerTrackBean.TrackData firstTrack = response.getData().get(0);
                        if (firstTrack.getAlbum() != null) {
                            String imageUrl = firstTrack.getAlbum().getCoverBig();
                            if (imageUrl != null) {
                                imageCache.put(trackTitle, imageUrl);

                                imageView.post(() -> {
                                    if (trackTitle.equals(imageView.getTag())) {
                                        loadImage(imageView, imageUrl);
                                    }
                                });
                            }
                        }
                    }
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void loadImage(ImageView imageView, String imageUrl) {
        Glide.with(mContext)
                .asBitmap()
                .apply(new RequestOptions()
                        .placeholder(R.drawable.noimage)
                        .error(R.drawable.noimage))
                .load(imageUrl)
                .into(imageView);
    }
}