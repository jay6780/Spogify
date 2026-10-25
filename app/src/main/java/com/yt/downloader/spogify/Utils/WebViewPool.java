package com.yt.downloader.spogify.Utils;

import android.content.Context;
import android.webkit.WebView;

import androidx.annotation.NonNull;

public interface WebViewPool {
    /**
     * Provides a web-view for the given activity context.
     * @param activity activity for which web view is asked.
     * @return cached web view instance
     */
    WebView obtain(@NonNull Context activity);

    /**
     * Release the cached web view back to the pool. Implementations should make sure that
     * no web view that is a not a part of the pool is released.
     * @param webView cached web view that we want to return
     * @param borrower context that was used for obtaining the web view.
     * @return true if released successfully, false otherwise
     */
    boolean release(@NonNull WebView webView, @NonNull Context borrower);
    
}