package com.yt.downloader.spogify.mvp.contract;

import com.yt.downloader.spogify.mvp.bean.SearchArtistBean;
public interface SearchArtistContract {
    interface View {
        void showLoading();
        void showError(String error);
        void hideLoading();
        void getSearchAristSuccess(SearchArtistBean searchArtistBean);

    }

    interface Presenter {
        void getSearchArtistQuery(String method,String artist, String apiKey, String format);
    }
}
