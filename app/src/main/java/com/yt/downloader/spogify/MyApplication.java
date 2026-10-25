package com.yt.downloader.spogify;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.webkit.WebView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.yt.downloader.spogify.Utils.SingularWebViewPool;
import com.yt.downloader.spogify.Utils.WebViewPool;

import java.io.File;

//import leakcanary.LeakCanary;

public class MyApplication extends Application {
    private static MyApplication instance;
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
//        LeakCanary.Config config = LeakCanary.getConfig().newBuilder() .retainedVisibleThreshold(3) .build();
//        LeakCanary.setConfig(config);
        initGlide();
        deleteCache(instance);
    }

    public static void deleteCache(Context context) {
        if(context instanceof Activity){
            WebViewPool pool = new SingularWebViewPool((context));
            WebView cachedView = pool.obtain((context));
            if(!pool.release(cachedView ,(context))) {
            }
        }
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) { e.printStackTrace();}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private void initGlide() {
        RequestOptions defaultOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .skipMemoryCache(false);

        Glide.init(this, new GlideBuilder()
                .setDefaultRequestOptions(defaultOptions));
        clearAllGlideCache();
    }
    public static void clearGlideMemoryCache() {
        Glide.get(instance).clearMemory();
    }

    public static void clearGlideDiskCache() {
        new Thread(() -> Glide.get(instance).clearDiskCache()).start();
    }

    public static void clearAllGlideCache() {
        clearGlideMemoryCache();
        clearGlideDiskCache();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).clearMemory();
    }
}