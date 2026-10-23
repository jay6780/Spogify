package com.yt.downloader.spogify.ui.adapter;


import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.yt.downloader.spogify.base.BaseQuickAdapter;
import com.yt.downloader.spogify.base.BaseViewHolder;
import com.yt.downloader.R;
import com.yt.downloader.spogify.mvp.bean.YoutubeSearchBean;


public class MusicListAdapter extends BaseQuickAdapter<YoutubeSearchBean.ItemsBean, BaseViewHolder> {
    public MusicListAdapter() {
        super(R.layout.music_item);
    }

    @Override
    protected void convert(BaseViewHolder helper, YoutubeSearchBean.ItemsBean item) {

        TextView tv_title = helper.getView(R.id.tv_title);
        TextView tv_duration = helper.getView(R.id.tv_duration);
        ImageView iv_icon = helper.getView(R.id.iv_icon);

        Glide.with(mContext)
                .asBitmap()
                .apply(new RequestOptions().error(R.drawable.noimage).placeholder(R.drawable.noimage))
                .load(item.getThumbnail())
                .into(iv_icon);
        tv_title.setText(item.getTitle());
        tv_duration.setText("Duration: "+item.getDuration());


    }

}