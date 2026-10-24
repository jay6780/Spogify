package com.yt.downloader.spogify.Utils;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.Window;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

@SuppressWarnings("deprecation")
public class GlobalWindowUtils {
    public GlobalWindowUtils(Activity activity, boolean isHome) {
        Window window = activity.getWindow();
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(window, window.getDecorView());

        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        window.setNavigationBarColor(isHome ? Color.parseColor("#262626") : Color.parseColor("#313647"));
        activity.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(
                () -> applyWindowState(windowInsetsController,activity));
    }

    private void applyWindowState(WindowInsetsControllerCompat controller,Activity activity) {
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            controller.show(WindowInsetsCompat.Type.navigationBars());
        }else{
            controller.hide(WindowInsetsCompat.Type.navigationBars());
        }

    }
}
