package com.yt.downloader.spogify.ui.adapter;

import android.graphics.Color;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.yt.downloader.R;
import com.yt.downloader.spogify.Utils.FavoriteDatabaseHelper;
import com.yt.downloader.spogify.base.BaseQuickAdapter;
import com.yt.downloader.spogify.base.BaseViewHolder;
import com.yt.downloader.spogify.mvp.bean.YoutubeSearchBean;

import java.util.Random;

public class MusicListAdapter extends BaseQuickAdapter<YoutubeSearchBean.ItemsBean, BaseViewHolder> {

    private FavoriteDatabaseHelper favDbHelper;
    private TopTrackInfoAdapter.OnFavoriteRemovedListener favoriteRemovedListener;

    public void setOnFavoriteRemovedListener(TopTrackInfoAdapter.OnFavoriteRemovedListener listener) {
        this.favoriteRemovedListener = listener;
    }

    public MusicListAdapter() {
        super(R.layout.music_item);
    }

    @Override
    protected void convert(BaseViewHolder helper, YoutubeSearchBean.ItemsBean item) {
        if (favDbHelper == null) {
            favDbHelper = new FavoriteDatabaseHelper(mContext);
        }

        TextView tv_title = helper.getView(R.id.tv_title);
        TextView tv_duration = helper.getView(R.id.tv_duration);
        ImageView iv_icon = helper.getView(R.id.iv_icon);
        ImageView iv_add_favorite = helper.getView(R.id.iv_add_favorite);

        Glide.with(mContext)
                .load(item.getThumbnail())
                .apply(new RequestOptions()
                        .transform(new CenterCrop(), new RoundedCorners(16))
                        .placeholder(R.drawable.noimage)
                        .error(R.drawable.noimage))
                .into(iv_icon);

        tv_title.setText(item.getTitle());
        tv_duration.setText(item.getDuration());


        boolean isFav = favDbHelper.isTrackFavorite(item.getTitle());
        updateAddButtonIcon(iv_add_favorite, isFav);

        iv_add_favorite.setOnClickListener(v -> {
            boolean currentlyFav = favDbHelper.isTrackFavorite(item.getTitle());
            if (currentlyFav) {
                favDbHelper.removeFavoriteTrack(item.getTitle());
                Toast.makeText(mContext, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                updateAddButtonIcon(iv_add_favorite, false);

                if (favoriteRemovedListener != null) {
                    favoriteRemovedListener.onFavoriteRemoved();
                }
            } else {
                String imageThumb = item.getThumbnail();
//                Log.d("ImageThumb","val: "+item.getThumbnail());
                String totalListeners = String.format("%,d", 10000 + new Random().nextInt(4990000));

                favDbHelper.addFavoriteTrack(item.getTitle(), item.getTitle(), imageThumb, totalListeners);
                Toast.makeText(mContext, "Added to Favorites", Toast.LENGTH_SHORT).show();
                updateAddButtonIcon(iv_add_favorite, true);
            }
        });
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
}