package com.yt.downloader.spogify.mvp.presenter;

import androidx.annotation.NonNull;

import com.yt.downloader.spogify.Retrofit.Callback;
import com.yt.downloader.spogify.mvp.bean.SearchArtistBean;
import com.yt.downloader.spogify.mvp.contract.SearchArtistContract;
import com.yt.downloader.spogify.mvp.model.SearchArtistModel;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class SearchArtistPresenter implements SearchArtistContract.Presenter {
    private SearchArtistContract.View view;

    public SearchArtistPresenter(SearchArtistContract.View view) {
        this.view = view;
    }



    @Override
    public void getSearchArtistQuery(String method, String artist, String apiKey, String format) {
        view.showLoading();

        SearchArtistModel.getSearchArtist(method, artist, apiKey, format, new Callback<SearchArtistBean>() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void returnResult(SearchArtistBean apiBean) {
                view.hideLoading();
                view.getSearchAristSuccess(apiBean);
            }

            @Override
            public void returnError(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }
}
