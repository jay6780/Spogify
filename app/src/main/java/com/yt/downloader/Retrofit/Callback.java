package com.yt.downloader.Retrofit;

public abstract class Callback<T> implements okhttp3.Callback {
    public abstract void returnResult(T t);
    public abstract void returnError(String message);
}