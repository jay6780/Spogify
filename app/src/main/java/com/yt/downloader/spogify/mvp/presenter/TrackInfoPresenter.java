package com.yt.downloader.spogify.mvp.presenter;

import androidx.annotation.NonNull;

import com.yt.downloader.spogify.Retrofit.Callback;
import com.yt.downloader.spogify.mvp.bean.TrackInfoBean;
import com.yt.downloader.spogify.mvp.contract.TrackInfoContract;
import com.yt.downloader.spogify.mvp.model.TrackInfoModel;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class TrackInfoPresenter implements TrackInfoContract.Presenter {
    private TrackInfoContract.View view;

    public TrackInfoPresenter(TrackInfoContract.View view) {
        this.view = view;
    }


    @Override
    public void getTrackArtistQuery(String method, String artist, String apiKey, String format) {
        view.showLoading();

        TrackInfoModel.getTrackInfoQuery(method, artist, apiKey, format, new Callback<TrackInfoBean>() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void returnResult(TrackInfoBean apiBean) {
                view.hideLoading();
                view.getArtistTrackSuccess(apiBean);
            }

            @Override
            public void returnError(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }
}
