package com.yt.downloader.spogify.ui.adapter;

import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.yt.downloader.R;
import com.yt.downloader.spogify.base.BaseQuickAdapter;
import com.yt.downloader.spogify.base.BaseViewHolder;
import com.yt.downloader.spogify.mvp.bean.FavoriteTrack;

public class FavoriteTrackAdapter extends BaseQuickAdapter<FavoriteTrack, BaseViewHolder> {

    public interface OnFavoriteClickListener {
        void onFavoriteClick(FavoriteTrack track);
    }

    private OnFavoriteClickListener listener;

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

        Glide.with(mContext)
                .load(item.getImageThumb())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.noimage)
                        .error(R.drawable.noimage))
                .into(ivArtistImage);

        helper.getView(R.id.ll_artist).setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(item);
            }
        });
    }
}