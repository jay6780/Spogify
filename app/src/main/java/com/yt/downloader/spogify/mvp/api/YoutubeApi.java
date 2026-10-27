package com.yt.downloader.spogify.mvp.api;

import com.yt.downloader.spogify.mvp.bean.LyricsBean;
import com.yt.downloader.spogify.mvp.bean.Mp3downloadBean;
import com.yt.downloader.spogify.mvp.bean.OpmBean;
import com.yt.downloader.spogify.mvp.bean.RAndBbean;
import com.yt.downloader.spogify.mvp.bean.SearchArtistBean;
import com.yt.downloader.spogify.mvp.bean.TopArtistBean;
import com.yt.downloader.spogify.mvp.bean.TopTrackBean;
import com.yt.downloader.spogify.mvp.bean.TrackInfoBean;
import com.yt.downloader.spogify.mvp.bean.YoutubeSearchBean;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface YoutubeApi {
    @GET("mp3juice/api")
    Observable<YoutubeSearchBean> getSearchList(@Query("search") String search);

    @GET("mp3juice/api")
    Observable<Mp3downloadBean> getDownloadUrl(
            @Query("download") String download,
            @Query("type") String type,
            @Query("quality") int quality);
    @GET(".")
    Observable<TopArtistBean> getTopArtists(
            @Query("method") String method,
            @Query("api_key") String apiKey,
            @Query("format") String format
    );

    @GET(".")
    Observable<TopTrackBean> getTopTracks(
            @Query("method") String method,
            @Query("api_key") String apiKey,
            @Query("format") String format
    );

    @GET(".")
    Observable<OpmBean> getOpm(
            @Query("method") String method,
            @Query("tag") String tag,
            @Query("api_key") String apiKey,
            @Query("format") String format
    );

    @GET(".")
    Observable<RAndBbean> getRnB(
            @Query("method") String method,
            @Query("tag") String tag,
            @Query("api_key") String apiKey,
            @Query("format") String format
    );

    @GET(".")
    Observable<TrackInfoBean> getPlayTrackInfo(
            @Query("method") String method,
            @Query("artist") String artist,
            @Query("api_key") String apiKey,
            @Query("format") String format
    );

    @GET(".")
    Observable<SearchArtistBean> getSearchArtist(
            @Query("method") String method,
            @Query("artist") String artist,
            @Query("api_key") String apiKey,
            @Query("format") String format
    );

    @GET("api/get")
    Observable<LyricsBean> getLyrics(
            @Query("track_name") String track_name,
            @Query("artist_name") String artist_name);
}
