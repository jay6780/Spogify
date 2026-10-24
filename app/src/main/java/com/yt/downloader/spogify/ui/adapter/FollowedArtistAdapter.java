package com.yt.downloader.spogify.ui.adapter;

import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.yt.downloader.R;
import com.yt.downloader.spogify.base.BaseQuickAdapter;
import com.yt.downloader.spogify.base.BaseViewHolder;
import com.yt.downloader.spogify.mvp.bean.FollowedArtist;

public class FollowedArtistAdapter extends BaseQuickAdapter<FollowedArtist, BaseViewHolder> {

    public interface OnArtistClickListener {
        void onArtistClick(FollowedArtist artist);
    }

    private OnArtistClickListener listener;

    public FollowedArtistAdapter(int layoutResId) {
        super(layoutResId);
    }

    public void setOnArtistClickListener(OnArtistClickListener listener) {
        this.listener = listener;
    }

    @Override
    protected void convert(BaseViewHolder helper, FollowedArtist item) {
        TextView tvArtistName = helper.getView(R.id.tvArtistName);
        TextView tvPlayCount = helper.getView(R.id.tvPlayCount);
        ImageView ivArtistImage = helper.getView(R.id.ivArtistImage);

        tvArtistName.setText(item.getArtistsName());
        tvPlayCount.setText(item.getTotalListeners());

        Glide.with(mContext)
                .load(item.getImage())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.noimage)
                        .error(R.drawable.noimage))
                .into(ivArtistImage);

        helper.getView(R.id.ll_artist).setOnClickListener(v -> {
            if (listener != null) {
                listener.onArtistClick(item);
            }
        });
    }
}