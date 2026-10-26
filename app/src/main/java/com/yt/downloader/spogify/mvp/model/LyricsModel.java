package com.yt.downloader.spogify.mvp.model;

import com.yt.downloader.spogify.Retrofit.Callback;
import com.yt.downloader.spogify.Retrofit.NetworkingUtils;
import com.yt.downloader.spogify.mvp.bean.LyricsBean;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LyricsModel {
    public static void getLyricsQuery(String q, final Callback<List<LyricsBean>> callback) {
        NetworkingUtils.getLyrics()
                .getLyrics(q)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<LyricsBean>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(List<LyricsBean> data) {
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
