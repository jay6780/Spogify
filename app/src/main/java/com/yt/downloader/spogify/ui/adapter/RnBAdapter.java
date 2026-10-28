package com.yt.downloader.spogify.ui.adapter;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.yt.downloader.R;
import com.yt.downloader.spogify.base.BaseQuickAdapter;
import com.yt.downloader.spogify.base.BaseViewHolder;
import com.yt.downloader.spogify.mvp.bean.DeezerArtistBean;
import com.yt.downloader.spogify.mvp.bean.OpmBean;
import com.yt.downloader.spogify.mvp.bean.RAndBbean;
import com.yt.downloader.spogify.ui.activity.TrackInfoActivity;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class RnBAdapter extends BaseQuickAdapter<RAndBbean.Artist, BaseViewHolder> {

    private final Map<String, String> imageCache = new ConcurrentHashMap<>();

    public RnBAdapter() {
        super(R.layout.item_top_artist);
    }

    @Override
    protected void convert(BaseViewHolder helper, RAndBbean.Artist item) {
        TextView tvCategory = helper.getView(R.id.tvCategory);
        TextView tvArtistName = helper.getView(R.id.tvArtistName);
        TextView tvPlayCount = helper.getView(R.id.tvPlayCount);
        tvPlayCount.setVisibility(View.GONE);
        ImageView ivArtistImage = helper.getView(R.id.ivArtistImage);

        tvCategory.setText("Artist");
        tvArtistName.setText(item.getName());


        String artistName = item.getName();

        ivArtistImage.setTag(artistName);

        if (imageCache.containsKey(artistName)) {
            String cachedUrl = imageCache.get(artistName);
            loadImage(ivArtistImage, cachedUrl);
        } else {
            try {
                Glide.with(mContext)
                        .load(R.mipmap.app_logo)
                        .into(ivArtistImage);

            }catch (Exception e){
                e.printStackTrace();
            }
            fetchDeezerImage(artistName, ivArtistImage);
        }

        helper.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, TrackInfoActivity.class)
                        .putExtra("ArtistName",item.getName()));
            }
        });
    }

    private void fetchDeezerImage(String artistName, ImageView imageView) {
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

                        imageView.post(() -> {
                            if (artistName.equals(imageView.getTag())) {
                                loadImage(imageView, imageUrl);
                            }
                        });
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
                    .asBitmap()
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.noimage)
                            .error(R.drawable.noimage))
                    .load(imageUrl)
                    .into(imageView);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}