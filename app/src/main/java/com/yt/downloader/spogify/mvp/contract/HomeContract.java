package com.yt.downloader.spogify.mvp.contract;

import com.yt.downloader.spogify.mvp.bean.OpmBean;
import com.yt.downloader.spogify.mvp.bean.RAndBbean;
import com.yt.downloader.spogify.mvp.bean.TopArtistBean;
import com.yt.downloader.spogify.mvp.bean.TopTrackBean;

public interface HomeContract {
    interface View {
        void showLoading();

        void showError(String error);

        void hideLoading();

        void getTopArtistSuccess(TopArtistBean topArtistBean);
        void getTopTracksSuccess(TopTrackBean topTrackBean);
        void getOpmSuccess(OpmBean opmBean);
        void getRnBSuccess(RAndBbean rAndBbean);

    }

    interface Presenter {
        void getTopArtistQuery(String method, String apiKey, String format);
        void getTopTrackQuery(String method, String apiKey, String format);
        void getOpmQuery(String method,String tag, String apiKey, String format);
        void getRnBQuery(String method,String tag, String apiKey, String format);
    }
}
