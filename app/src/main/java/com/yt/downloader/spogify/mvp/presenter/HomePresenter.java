package com.yt.downloader.spogify.mvp.presenter;

import androidx.annotation.NonNull;

import com.yt.downloader.spogify.Retrofit.Callback;
import com.yt.downloader.spogify.mvp.bean.OpmBean;
import com.yt.downloader.spogify.mvp.bean.RAndBbean;
import com.yt.downloader.spogify.mvp.bean.TopArtistBean;
import com.yt.downloader.spogify.mvp.bean.TopTrackBean;
import com.yt.downloader.spogify.mvp.contract.HomeContract;
import com.yt.downloader.spogify.mvp.model.HomeModel;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class HomePresenter implements HomeContract.Presenter {
    private HomeContract.View view;

    public HomePresenter(HomeContract.View view) {
        this.view = view;
    }


    @Override
    public void getTopArtistQuery(String method, String apiKey, String format) {
        view.showLoading();

        HomeModel.getTopArtist(method, apiKey, format, new Callback<TopArtistBean>() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void returnResult(TopArtistBean apiBean) {
                view.hideLoading();
                view.getTopArtistSuccess(apiBean);
            }

            @Override
            public void returnError(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }

    @Override
    public void getTopTrackQuery(String method, String apiKey, String format) {
        view.showLoading();

        HomeModel.getTopTracks(method, apiKey, format, new Callback<TopTrackBean>() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void returnResult(TopTrackBean apiBean) {
                view.hideLoading();
                view.getTopTracksSuccess(apiBean);
            }

            @Override
            public void returnError(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }

    @Override
    public void getOpmQuery(String method, String tag,String apiKey, String format) {
        view.showLoading();

        HomeModel.getOpm(method, tag,apiKey, format, new Callback<OpmBean>() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void returnResult(OpmBean apiBean) {
                view.hideLoading();
                view.getOpmSuccess(apiBean);
            }

            @Override
            public void returnError(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }

    @Override
    public void getRnBQuery(String method, String tag, String apiKey, String format) {
        view.showLoading();

        HomeModel.getRnB(method, tag,apiKey, format, new Callback<RAndBbean>() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void returnResult(RAndBbean apiBean) {
                view.hideLoading();
                view.getRnBSuccess(apiBean);
            }

            @Override
            public void returnError(String message) {
                view.hideLoading();
                view.showError(message);
            }
        });
    }
}
