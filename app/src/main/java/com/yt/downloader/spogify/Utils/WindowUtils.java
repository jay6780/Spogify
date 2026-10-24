package com.yt.downloader.spogify.Utils;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
@SuppressWarnings("deprecation")
public class WindowUtils {
    public WindowUtils (Activity activity,boolean isSeries,boolean isMovieStatus){
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        activity.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(
                () -> applyWindowState(activity, windowInsetsController,isSeries,isMovieStatus));
    }

    private void applyWindowState(Activity activity, WindowInsetsControllerCompat controller, boolean isSeries,boolean isMovieStatus) {
        if(isSeries) {
            if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                controller.show(WindowInsetsCompat.Type.statusBars());
                activity.getWindow().setStatusBarColor(isMovieStatus ?Color.parseColor("#313647") : Color.parseColor("#000000"));
            } else {
                controller.hide(WindowInsetsCompat.Type.statusBars());
            }
        }else {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        }

    }
}
