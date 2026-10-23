package com.yt.downloader.spogify.mvp.model;

import com.yt.downloader.spogify.Retrofit.Callback;
import com.yt.downloader.spogify.Retrofit.NetworkingUtils;
import com.yt.downloader.spogify.mvp.bean.OpmBean;
import com.yt.downloader.spogify.mvp.bean.RAndBbean;
import com.yt.downloader.spogify.mvp.bean.TopArtistBean;
import com.yt.downloader.spogify.mvp.bean.TopTrackBean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeModel {
    public static void getTopArtist(String method, String apiKey, String format, final Callback<TopArtistBean> callback) {
        NetworkingUtils.getLastFmService()
                .getTopArtists(method, apiKey, format)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<TopArtistBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(TopArtistBean data) {
                        callback.returnResult(data);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.returnError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }


    public static void getTopTracks(String method, String apiKey, String format, final Callback<TopTrackBean> callback) {
        NetworkingUtils.getLastFmService()
                .getTopTracks(method, apiKey, format)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<TopTrackBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(TopTrackBean data) {
                        callback.returnResult(data);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.returnError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public static void getOpm(String method,String tag, String apiKey, String format, final Callback<OpmBean> callback) {
        NetworkingUtils.getLastFmService()
                .getOpm(method,tag, apiKey, format)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<OpmBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(OpmBean data) {
                        callback.returnResult(data);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.returnError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    public static void getRnB(String method,String tag, String apiKey, String format, final Callback<RAndBbean> callback) {
        NetworkingUtils.getLastFmService()
                .getRnB(method,tag, apiKey, format)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RAndBbean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(RAndBbean data) {
                        callback.returnResult(data);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.returnError(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }
}