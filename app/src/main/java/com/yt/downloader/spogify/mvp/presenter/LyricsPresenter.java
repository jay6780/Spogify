package com.yt.downloader.spogify.mvp.presenter;

import androidx.annotation.NonNull;

import com.yt.downloader.spogify.Retrofit.Callback;
import com.yt.downloader.spogify.mvp.bean.LyricsBean;
import com.yt.downloader.spogify.mvp.contract.LyricsContract;
import com.yt.downloader.spogify.mvp.model.LyricsModel;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class LyricsPresenter implements LyricsContract.Presenter {
    private LyricsContract.View view;

    public LyricsPresenter(LyricsContract.View view) {
        this.view = view;
    }


    @Override
    public void getLyrics(String track_name,String artist_name,int duration) {
        view.showLoading();

        LyricsModel.getLyricsQuery(track_name,artist_name,duration, new Callback<LyricsBean>() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void returnResult(LyricsBean apiBean) {
                view.hideLoading();
                view.getLyricsSuccess(apiBean);
            }

            @Override
            public void returnError(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }
}
