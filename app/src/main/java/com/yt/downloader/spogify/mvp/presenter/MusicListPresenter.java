package com.yt.downloader.spogify.mvp.presenter;

import androidx.annotation.NonNull;

import com.yt.downloader.spogify.Retrofit.Callback;
import com.yt.downloader.spogify.mvp.bean.YoutubeSearchBean;
import com.yt.downloader.spogify.mvp.contract.MusicListContract;
import com.yt.downloader.spogify.mvp.model.MusicModel;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class MusicListPresenter implements MusicListContract.Presenter {
    private MusicListContract.View view;

    public MusicListPresenter(MusicListContract.View view) {
        this.view = view;
    }

    @Override
    public void getSearchListQuery(String search) {
        view.showLoading();

        MusicModel.getMusicListSearch(search, new Callback<YoutubeSearchBean>() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void returnResult(YoutubeSearchBean apiBean) {
                view.hideLoading();
                view.getSearchList(apiBean);
            }

            @Override
            public void returnError(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }
}
