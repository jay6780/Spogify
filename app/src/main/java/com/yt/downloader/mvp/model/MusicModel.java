package com.yt.downloader.mvp.model;

import com.yt.downloader.Retrofit.Callback;
import com.yt.downloader.Retrofit.NetworkingUtils;
import com.yt.downloader.mvp.bean.YoutubeSearchBean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MusicModel {
    public static void getMusicListSearch(String search, final Callback<YoutubeSearchBean> callback) {
        NetworkingUtils.getApiService()
                .getSearchList(search)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<YoutubeSearchBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(YoutubeSearchBean data) {
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
