package com.yt.downloader.spogify.ui.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.yt.downloader.R;
import com.yt.downloader.spogify.Utils.EqualizerView;
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
        void onTrackClick(TrackInfoBean.Track track, int position);
    }

    private OnTrackClickListener listener;
    private final Map<String, String> imageCache = new ConcurrentHashMap<>();

    private int currentPlayingPosition = -1;
    private boolean isPlaying = false;

    public TopTrackInfoAdapter() {
        super(R.layout.item_popular_track);
    }

    public void setOnTrackClickListener(OnTrackClickListener listener) {
        this.listener = listener;
    }

    public void setPlayingPosition(int position, boolean playing) {
        this.currentPlayingPosition = position;
        this.isPlaying = playing;
        notifyDataSetChanged();
    }

    @Override
    protected void convert(BaseViewHolder helper, TrackInfoBean.Track item) {
        TextView tv_rank = helper.getView(R.id.tv_rank);
        TextView tv_track_title = helper.getView(R.id.tv_track_title);
        TextView tv_play_count = helper.getView(R.id.tv_play_count);
        ImageView iv_track_cover = helper.getView(R.id.iv_track_cover);
        EqualizerView equalizerView = helper.getView(R.id.equalizer_view);

        int position = helper.getAdapterPosition();
        boolean isCurrentItem = (position == currentPlayingPosition);

        tv_rank.setText(item.getAttributes().getRank());
        tv_track_title.setText(item.getName());
        tv_play_count.setText(item.getPlaycount());

        if (isCurrentItem) {
            tv_track_title.setTextColor(Color.parseColor("#1DB954"));
            tv_rank.setVisibility(View.GONE);
            equalizerView.setVisibility(View.VISIBLE);
            equalizerView.setPlaying(isPlaying);
        } else {
            tv_track_title.setTextColor(Color.WHITE);
            tv_rank.setVisibility(View.VISIBLE);
            equalizerView.setVisibility(View.GONE);
            equalizerView.setPlaying(false);
        }

        String trackTitle = item.getName();
        iv_track_cover.setTag(trackTitle);

        helper.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTrackClick(item, position);
            }
        });

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