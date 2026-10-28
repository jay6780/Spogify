package com.yt.downloader.spogify.ui.adapter;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.yt.downloader.R;
import com.yt.downloader.spogify.base.BaseQuickAdapter;
import com.yt.downloader.spogify.base.BaseViewHolder;
import com.yt.downloader.spogify.mvp.bean.DeezerTrackBean;
import com.yt.downloader.spogify.mvp.bean.FavoriteTrack;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class FavoriteTrackAdapter extends BaseQuickAdapter<FavoriteTrack, BaseViewHolder> {

    public interface OnFavoriteClickListener {
        void onFavoriteClick(FavoriteTrack track);
    }

    private OnFavoriteClickListener listener;
    private final Map<String, String> imageCache = new ConcurrentHashMap<>();

    public FavoriteTrackAdapter(int layoutResId) {
        super(layoutResId);
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void convert(BaseViewHolder helper, FavoriteTrack item) {
        TextView tvArtistName = helper.getView(R.id.tvArtistName);
        TextView tvPlayCount = helper.getView(R.id.tvPlayCount);
        TextView tvCategory = helper.getView(R.id.tvCategory);
        ImageView ivArtistImage = helper.getView(R.id.ivArtistImage);

        tvCategory.setText("Song");
        tvArtistName.setText(item.getMusicName());
        tvPlayCount.setText(item.getArtistName());

        String trackTitle = item.getMusicName();
        ivArtistImage.setTag(trackTitle);

        String thumbUrl = item.getImageThumb();

        if (!TextUtils.isEmpty(thumbUrl)) {
            loadImage(ivArtistImage, thumbUrl);
        } else if (imageCache.containsKey(trackTitle)) {
            loadImage(ivArtistImage, imageCache.get(trackTitle));
        } else {
            loadImage(ivArtistImage, null);
            fetchDeezerTrackCover(trackTitle, ivArtistImage);
        }

        helper.getView(R.id.ll_artist).setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(item);
            }
        });
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
        try {
            Glide.with(mContext)
                    .load(imageUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.noimage)
                            .error(R.mipmap.app_logo))
                    .into(imageView);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}