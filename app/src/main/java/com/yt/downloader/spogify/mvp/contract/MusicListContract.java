package com.yt.downloader.spogify.mvp.contract;

import com.yt.downloader.spogify.mvp.bean.YoutubeSearchBean;

public interface MusicListContract {
    interface View {
        void showLoading();
        void showError(String error);
        void hideLoading();
        void getSearchList(YoutubeSearchBean youtubeSearchBean);

    }

    interface Presenter {
        void getSearchListQuery(String search);
    }
}
