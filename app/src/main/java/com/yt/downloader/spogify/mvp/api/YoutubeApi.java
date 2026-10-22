package com.yt.downloader.spogify.mvp.api;

import com.yt.downloader.spogify.mvp.bean.YoutubeSearchBean;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YoutubeApi {
    @GET("mp3juice/api")
    Observable<YoutubeSearchBean> getSearchList(@Query("search") String search);
}
