package com.yt.downloader.spogify.mvp.contract;

import com.yt.downloader.spogify.mvp.bean.Mp3downloadBean;
import com.yt.downloader.spogify.mvp.bean.TrackInfoBean;

public interface TrackInfoContract {
    interface View {
        void showLoading();
        void showError(String error);
        void hideLoading();
        void getArtistTrackSuccess(TrackInfoBean trackInfoBean);
        void getDownloadSuccess(Mp3downloadBean mp3downloadBean);

    }

    interface Presenter {
        void getTrackArtistQuery(String method,String artist, String apiKey, String format);
        void getDownloadQuery(String download,String type, int quality);
    }
}
