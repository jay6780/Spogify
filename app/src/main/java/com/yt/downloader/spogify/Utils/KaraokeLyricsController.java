package com.yt.downloader.spogify.Utils;

import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class KaraokeLyricsController {

    private static final int SUNG_COLOR = 0xFF00E676;   // green (already your lyric color)
    private static final int UNSUNG_COLOR = 0xFFB3B3B3; // grey

    private final TextView tv;
    private List<LrcParser.Line> lines = Collections.emptyList();

    /** Positive = lyrics appear earlier, negative = later. Use to fix offset per track. */
    private long offsetMs = 0;

    private int lastIdx = -2;
    private int lastChars = -1;

    private float lastPosSec = -1f;
    private long lastPosStamp = 0;

    public KaraokeLyricsController(TextView tv) {
        this.tv = tv;
    }

    public void setLines(List<LrcParser.Line> newLines) {
        lines = newLines == null ? Collections.<LrcParser.Line>emptyList() : newLines;
        lastIdx = -2;
        lastChars = -1;
        tv.setVisibility(lines.isEmpty() ? View.GONE : View.VISIBLE);
        if (lines.isEmpty()) tv.setText("");
    }

    public void clear() {
        setLines(null);
    }

    public boolean hasLyrics() {
        return !lines.isEmpty();
    }

    public void setOffsetMs(long ms) {
        offsetMs = ms;
    }

    /** Call every ~100ms with the current player position. */
    public void tick(float positionSec, boolean playing) {
        if (lines.isEmpty()) return;

        // Interpolate between player updates (YouTube only reports a few times per second)
        long now = SystemClock.elapsedRealtime();
        if (positionSec != lastPosSec) {
            lastPosSec = positionSec;
            lastPosStamp = now;
        }
        long extra = playing ? Math.min(now - lastPosStamp, 600) : 0;
        long t = (long) (positionSec * 1000) + extra + offsetMs;

        int idx = findIndex(t);

        if (idx < 0) {                       // before first line (intro)
            showPlain(idx, "♪");
            return;
        }

        LrcParser.Line cur = lines.get(idx);
        if (cur.text.isEmpty()) {            // instrumental / gap
            showPlain(idx, "♪ ♪ ♪");
            return;
        }

        long start = cur.timeMs;
        long nextStart = idx + 1 < lines.size() ? lines.get(idx + 1).timeMs : start + 4000;

        // Cap singing time so lines followed by a long gap don't fill super slowly
        long maxSing = Math.max(1500, cur.text.length() * 110L);
        long dur = Math.max(1, Math.min(nextStart - start, maxSing));

        float frac = Math.max(0f, Math.min(1f, (t - start) / (float) dur));
        int chars = Math.round(cur.text.length() * frac);

        if (idx == lastIdx && chars == lastChars) return;   // nothing changed
        lastIdx = idx;
        lastChars = chars;

        SpannableString ss = new SpannableString(cur.text);
        if (chars > 0) {
            ss.setSpan(new ForegroundColorSpan(SUNG_COLOR), 0, chars,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (chars < cur.text.length()) {
            ss.setSpan(new ForegroundColorSpan(UNSUNG_COLOR), chars, cur.text.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tv.setText(ss);
    }

    private void showPlain(int idx, String s) {
        if (lastIdx == idx && lastChars == -99) return;
        lastIdx = idx;
        lastChars = -99;
        tv.setText(s);
        tv.setTextColor(UNSUNG_COLOR);
    }

    /** Last line whose time <= t, or -1. Binary search so seeking works instantly. */
    private int findIndex(long t) {
        int lo = 0, hi = lines.size() - 1, ans = -1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (lines.get(mid).timeMs <= t) {
                ans = mid;
                lo = mid + 1;
            } else {
                hi = mid - 1;
            }
        }
        return ans;
    }
}