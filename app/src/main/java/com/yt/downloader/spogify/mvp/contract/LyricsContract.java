package com.yt.downloader.spogify.mvp.contract;

import com.yt.downloader.spogify.mvp.bean.LyricsBean;

import java.util.List;

public interface LyricsContract {
    interface View {
        void showLoading();
        void showError(String error);
        void hideLoading();
        void getLyricsSuccess(LyricsBean lyricsBean);

    }

    interface Presenter {
        void getLyrics(String track_name,String artist_name,int duration);
    }
}
