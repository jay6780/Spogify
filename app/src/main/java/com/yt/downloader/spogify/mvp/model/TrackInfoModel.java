package com.yt.downloader.spogify.mvp.model;

import com.yt.downloader.spogify.Retrofit.Callback;
import com.yt.downloader.spogify.Retrofit.NetworkingUtils;
import com.yt.downloader.spogify.mvp.bean.Mp3downloadBean;
import com.yt.downloader.spogify.mvp.bean.TrackInfoBean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class TrackInfoModel {
    public static void getTrackInfoQuery(String method,String artist, String apiKey, String format, final Callback<TrackInfoBean> callback) {
        NetworkingUtils.getLastFmService()
                .getPlayTrackInfo(method,artist,apiKey,format)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<TrackInfoBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(TrackInfoBean data) {
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

    public static void getDownloadQuery(String download,String type, int quality, final Callback<Mp3downloadBean> callback) {
        NetworkingUtils.getApiService()
                .getDownloadUrl(download,type,quality)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Mp3downloadBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Mp3downloadBean data) {
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
