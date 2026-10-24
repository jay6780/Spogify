package com.yt.downloader.spogify.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Random;

public class EqualizerView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] barHeights = new float[]{0.3f, 0.7f, 0.5f};
    private final Random random = new Random();
    private boolean isPlaying = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable animateRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying) {
                for (int i = 0; i < barHeights.length; i++) {
                    barHeights[i] = 0.2f + random.nextFloat() * 0.8f;
                }
                invalidate();
                handler.postDelayed(this, 150);
            }
        }
    };

    public EqualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.parseColor("#1DB954")); // Spotify Green
        paint.setStyle(Paint.Style.FILL);
    }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
        if (playing) {
            handler.removeCallbacks(animateRunnable);
            handler.post(animateRunnable);
        } else {
            handler.removeCallbacks(animateRunnable);
            barHeights[0] = 0.4f;
            barHeights[1] = 0.8f;
            barHeights[2] = 0.5f;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int numBars = 3;
        float barWidth = width / (float) (numBars * 2 - 1);

        for (int i = 0; i < numBars; i++) {
            float left = i * barWidth * 2;
            float barHeight = height * barHeights[i];
            float top = height - barHeight;
            canvas.drawRect(left, top, left + barWidth, height, paint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(animateRunnable);
    }
}