package com.yt.downloader.mvp.contract;

import com.yt.downloader.mvp.bean.YoutubeSearchBean;

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
