package com.yt.downloader.spogify.Retrofit;


import com.yt.downloader.spogify.mvp.api.YoutubeApi;

public class NetworkingUtils {

    private static YoutubeApi apiService;


    public static YoutubeApi getApiService() {
        if (apiService == null)
            apiService = YoutubeDownloaderAdapter.getInstance().create(YoutubeApi.class);

        return apiService;
    }
}