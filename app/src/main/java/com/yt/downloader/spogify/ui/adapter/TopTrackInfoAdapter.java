package com.yt.downloader.spogify.ui.adapter;

import android.annotation.SuppressLint;
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
import com.yt.downloader.spogify.Utils.FormatCountUtil;
import com.yt.downloader.spogify.Utils.fileUtils.FilesExtractor;
import com.yt.downloader.spogify.base.BaseQuickAdapter;
import com.yt.downloader.spogify.base.BaseViewHolder;
import com.yt.downloader.spogify.mvp.bean.DeezerTrackBean;
import com.yt.downloader.spogify.mvp.bean.TrackInfoBean;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import android.app.AlertDialog;
import android.content.IntentSender;
import android.app.RecoverableSecurityException;
import android.os.Build;
import android.provider.MediaStore;
import android.net.Uri;
import android.app.Activity;
import java.io.File;

public class TopTrackInfoAdapter extends BaseQuickAdapter<TrackInfoBean.Track, BaseViewHolder> {
    private static final int DELETE_REQUEST_CODE = 5012;

    private String pendingDeleteTrackName = "";
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

    @SuppressLint("SetTextI18n")
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
        String playCount = item.getPlaycount();

        if(isDownloaded){
            tv_play_count.setText("Downloaded");
        }else{
            if (playCount != null) {
                try {
                    long count = Long.parseLong(playCount.trim());
                    tv_play_count.setText(new FormatCountUtil().formatListenerCount(count));
                } catch (NumberFormatException e) {
                    tv_play_count.setText("0 listeners");
                }
            } else {
                tv_play_count.setText("0 listeners");
            }

        }
        if (isCurrentPlaying && isPlaying) {
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
            iv_download.setImageResource(R.drawable.baseline_delete_24);
            iv_download.setColorFilter(Color.parseColor("#B3B3B3"));
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
            if (filesExtractor.isFileDownloaded(item.getName())) {
                showDeleteDialog(item.getName());
            } else if (downloadClickListener != null) {
                Toast.makeText(mContext, "Downloading: " + item.getName(), Toast.LENGTH_SHORT).show();
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
                try {
                    Glide.with(mContext)
                            .load(R.mipmap.app_logo)
                            .into(iv_track_cover);
                }catch (Exception e){
                    e.printStackTrace();
                }
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

    private void showDeleteDialog(String trackName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View dialogView = View.inflate(mContext, R.layout.delete_notice, null);

        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        TextView tvMessage = dialogView.findViewById(R.id.tvMessage);
        TextView btnCancel = dialogView.findViewById(R.id.btnCancel);
        TextView btnOk = dialogView.findViewById(R.id.btnOk);

        tvTitle.setText("Delete file?");
        tvMessage.setText("Are you sure you want to delete \"" + trackName + "\" from your downloaded music?");

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        btnOk.setOnClickListener(v -> {
            dialog.dismiss();

            deleteDownloadedMusic(trackName);
        });

        dialog.show();
    }

    private void deleteDownloadedMusic(String trackName) {
        File file = filesExtractor.getDownloadedFile(trackName);

        if (file == null || !file.exists()) {
            Toast.makeText(mContext, "File not found", Toast.LENGTH_SHORT).show();
            notifyDataSetChanged();
            return;
        }

        Uri uri = filesExtractor.getDownloadedMusicUri(trackName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && uri != null) {
            if (!(mContext instanceof Activity)) {
                Toast.makeText(mContext, "Cannot request delete permission", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                ArrayList<Uri> uris = new ArrayList<>();
                uris.add(uri);

                pendingDeleteTrackName = trackName;

                IntentSender intentSender = MediaStore.createDeleteRequest(
                        mContext.getContentResolver(),
                        uris
                ).getIntentSender();

                ((Activity) mContext).startIntentSenderForResult(
                        intentSender,
                        DELETE_REQUEST_CODE,
                        null,
                        0,
                        0,
                        0
                );

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, "Unable to request deletion", Toast.LENGTH_SHORT).show();
            }

            return;
        }

        if (uri != null) {
            try {
                int deleted = mContext.getContentResolver().delete(uri, null, null);

                if (deleted > 0) {
                    onMusicDeleted(trackName);
                    return;
                }

            } catch (SecurityException e) {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q
                        && e instanceof RecoverableSecurityException
                        && mContext instanceof Activity) {

                    try {
                        pendingDeleteTrackName = trackName;

                        RecoverableSecurityException recoverable =
                                (RecoverableSecurityException) e;

                        ((Activity) mContext).startIntentSenderForResult(
                                recoverable.getUserAction().getActionIntent().getIntentSender(),
                                DELETE_REQUEST_CODE,
                                null,
                                0,
                                0,
                                0
                        );

                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }

                    return;
                }

                Toast.makeText(mContext, "Permission denied", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (file.delete()) {
            onMusicDeleted(trackName);
        } else {
            Toast.makeText(mContext, "Failed to delete music", Toast.LENGTH_SHORT).show();
        }
    }

    private void onMusicDeleted(String trackName) {
        Toast.makeText(mContext, "Music deleted successfully", Toast.LENGTH_SHORT).show();

        downloadProgressMap.remove(trackName);

        notifyDataSetChanged();
    }

    public void onDeleteMusicResult(int requestCode, int resultCode) {
        if (requestCode != DELETE_REQUEST_CODE) return;

        String trackName = pendingDeleteTrackName;
        pendingDeleteTrackName = "";

        if (resultCode == Activity.RESULT_OK) {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                deleteDownloadedMusic(trackName);
            } else {
                onMusicDeleted(trackName);
            }
        } else {
            Toast.makeText(mContext, "Deletion cancelled", Toast.LENGTH_SHORT).show();
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