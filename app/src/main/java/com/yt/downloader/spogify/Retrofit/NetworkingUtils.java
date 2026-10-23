package com.yt.downloader.spogify.Retrofit;


import com.yt.downloader.spogify.mvp.api.YoutubeApi;

public class NetworkingUtils {

    private static YoutubeApi apiService,lastfmService;


    public static YoutubeApi getApiService() {
        if (apiService == null)
            apiService = YoutubeDownloaderAdapter.getInstance().create(YoutubeApi.class);

        return apiService;
    }
    public static YoutubeApi getLastFmService() {
        if (lastfmService == null)
            lastfmService = LastFmAdapter.getInstance().create(YoutubeApi.class);

        return lastfmService;
    }
}