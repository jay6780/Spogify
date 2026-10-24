package com.yt.downloader.spogify.mvp.model;

import com.yt.downloader.spogify.Retrofit.Callback;
import com.yt.downloader.spogify.Retrofit.NetworkingUtils;
import com.yt.downloader.spogify.mvp.bean.SearchArtistBean;
import com.yt.downloader.spogify.mvp.bean.TrackInfoBean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SearchArtistModel {
    public static void getSearchArtist(String method,String artist, String apiKey, String format, final Callback<SearchArtistBean> callback) {
        NetworkingUtils.getLastFmService()
                .getSearchArtist(method,artist,apiKey,format)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<SearchArtistBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(SearchArtistBean data) {
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
