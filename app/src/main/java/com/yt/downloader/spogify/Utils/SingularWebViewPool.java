package com.yt.downloader.spogify.Utils;

import android.content.Context;
import android.content.MutableContextWrapper;
import android.webkit.WebView;

import androidx.annotation.NonNull;

public class SingularWebViewPool implements WebViewPool {

        private WebView mCachedInstance;
        private volatile int mBorrower;
        private final int mSignature;
        private final Context mAppCtx;

        public SingularWebViewPool(Context appCtx) {
            this.mAppCtx = appCtx;
            this.mCachedInstance = new WebView(appCtx);
            // We will match this every time, we flip to app context to ensure a different web view is
            // not handed over to us.
            this.mSignature = mCachedInstance.hashCode();
        }

        @Override
        @NonNull
        public WebView obtain(@NonNull Context activity) {
            if (mCachedInstance != null) {
                Context ctx = mCachedInstance.getContext();
                if (ctx instanceof MutableContextWrapper) {
                    ((MutableContextWrapper) ctx).setBaseContext(activity);
                } else {
                    // We should not reach here!
                    throw new IllegalStateException("Cached web view stored without a mutable context wrapper.");
                }
                WebView temp = mCachedInstance;
                mCachedInstance = null;
                this.mBorrower = activity.hashCode();
                return temp;
            } else {
                throw new IllegalStateException("Pool not having a cached web view instance when obtain() was called.");
            }
        }

        @Override
        public boolean release(@NonNull WebView webView, @NonNull Context borrower) {
            // Validate the last borrower.
            if (borrower.hashCode() != this.mBorrower) {
                return false;
            }
            Context ctx = webView.getContext();
          
            if (ctx instanceof MutableContextWrapper) {
                ((MutableContextWrapper) ctx).setBaseContext(mAppCtx);
            } else {
                throw new IllegalStateException("Cached web view stored without a mutable context wrapper.");
            }

            // match the signature.
            if (mSignature != webView.hashCode()) {
                throw new IllegalStateException("A different web view is released other than what we have given out.");
            }

            mCachedInstance = webView;
            mBorrower = 0;
            return true;
        }
    }