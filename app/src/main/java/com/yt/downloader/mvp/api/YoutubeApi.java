package com.yt.downloader.mvp.api;

import com.yt.downloader.mvp.bean.YoutubeSearchBean;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YoutubeApi {
    @GET("mp3juice/api")
    Observable<YoutubeSearchBean> getSearchList(@Query("search") String search);
}
