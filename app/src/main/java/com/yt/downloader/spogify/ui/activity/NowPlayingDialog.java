package com.yt.downloader.spogify.ui.activity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.yt.downloader.databinding.DialogNowPlayingBinding;
import com.yt.downloader.spogify.Utils.LrcParser;
import com.yt.downloader.spogify.Utils.PlayerState;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class NowPlayingDialog extends DialogFragment {

    /** Same title always gives the same colour, so mini player and full screen match. */
    public static int colorFor(String seed) {
        int h = (seed == null ? "" : seed).hashCode() & 0x7fffffff;
        float hue = h % 360;
        float sat = 0.55f + ((h / 360) % 30) / 100f;
        return Color.HSVToColor(new float[]{hue, sat, 0.45f});
    }

    private DialogNowPlayingBinding b;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final LyricsAdapter adapter = new LyricsAdapter();
    private LinearLayoutManager lm;
    private List<LrcParser.Line> shown;
    private int curIdx = -1;
    private int color = 0;
    private boolean dragging = false;

    private MiniPlayerActivity host() {
        return (MiniPlayerActivity) requireActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        b = DialogNowPlayingBinding.inflate(inflater, container, false);

        lm = new LinearLayoutManager(requireContext());
        b.rvLyrics.setLayoutManager(lm);
        b.rvLyrics.setAdapter(adapter);

        b.ivCollapse.setOnClickListener(v -> dismiss());
        b.ivPlay.setOnClickListener(v -> host().togglePlayPause());
        b.ivNext.setOnClickListener(v -> host().onNextTrackRequested());
        b.ivPrev.setOnClickListener(v -> host().onPreviousTrackRequested());

        b.seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int p, boolean fromUser) {
                if (fromUser) b.tvPos.setText(fmt(p / 1000f));
            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {
                dragging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar sb) {
                dragging = false;
                host().seekToSec(sb.getProgress() / 1000f);
            }
        });
        return b.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            Window w = getDialog().getWindow();

            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            w.setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
            );

            int c = colorFor(PlayerState.get().title);
            color = c;

            if (b != null) {
                b.root.setBackgroundColor(c);
            }
        }

        handler.removeCallbacks(tick);
        handler.post(tick);
    }

    @Override
    public void onStop() {
        handler.removeCallbacks(tick);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacks(tick);
        b = null;
        super.onDestroyView();
    }

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (b == null) return;
            PlayerState s = PlayerState.get();
            if (!s.hasTrack) {
                dismiss();
                return;
            }

            int c = colorFor(s.title);
            if (c != color) {
                color = c;
                b.root.setBackgroundColor(c);
            }

            b.tvTitle.setText(s.title);
            b.tvArtist.setText(s.artist);
            b.ivPlay.setImageResource(s.isPlaying
                    ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);

            // lyrics (they arrive async, so compare the list instance)
            if (s.parsedLyrics != shown) {
                shown = s.parsedLyrics;
                adapter.setData(shown);
                curIdx = -1;
            }
            boolean hasLyrics = shown != null && !shown.isEmpty();
            b.tvEmpty.setVisibility(hasLyrics ? View.GONE : View.VISIBLE);

            if (hasLyrics) {
                long t = (long) (s.positionSec * 1000) + s.lyricsOffsetMs;
                int idx = findIndex(shown, t);
                if (idx != curIdx) {
                    curIdx = idx;
                    adapter.setCurrent(idx);
                    if (idx >= 0) centerOn(idx);
                }
            }

            if (!dragging) {
                b.seek.setMax((int) (s.durationSec * 1000));
                b.seek.setProgress((int) (s.positionSec * 1000));
                b.tvPos.setText(fmt(s.positionSec));
            }
            b.tvDur.setText(fmt(s.durationSec));

            handler.postDelayed(this, 250);
        }
    };

    private void centerOn(int pos) {
        LinearSmoothScroller sc = new LinearSmoothScroller(requireContext()) {
            @Override
            public int calculateDtToFit(int vs, int ve, int bs, int be, int snap) {
                return (bs + (be - bs) / 2) - (vs + (ve - vs) / 2);   // put the line in the middle
            }
        };
        sc.setTargetPosition(pos);
        lm.startSmoothScroll(sc);
    }

    private static int findIndex(List<LrcParser.Line> lines, long t) {
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

    private static String fmt(float sec) {
        int s = Math.max(0, (int) sec);
        return String.format(Locale.US, "%d:%02d", s / 60, s % 60);
    }

    // ------------------------------------------------------------------

    private class LyricsAdapter extends RecyclerView.Adapter<LyricsAdapter.VH> {
        private List<LrcParser.Line> data = Collections.emptyList();
        private int cur = -1;

        void setData(List<LrcParser.Line> d) {
            data = d == null ? Collections.<LrcParser.Line>emptyList() : d;
            cur = -1;
            notifyDataSetChanged();
        }

        void setCurrent(int idx) {
            int old = cur;
            cur = idx;
            if (old >= 0) notifyItemChanged(old);
            if (idx >= 0) notifyItemChanged(idx);
        }

        class VH extends RecyclerView.ViewHolder {
            final TextView tv;

            VH(TextView v) {
                super(v);
                tv = v;
            }
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            int pad = (int) (10 * parent.getResources().getDisplayMetrics().density);
            tv.setPadding(0, pad, 0, pad);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            return new VH(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            LrcParser.Line line = data.get(pos);
            boolean on = pos == cur;
            h.tv.setText(line.text.isEmpty() ? "♪" : line.text);
            h.tv.setTextSize(on ? 24 : 18);
            h.tv.setTextColor(on ? 0xFFFFFFFF : 0x99FFFFFF);
            // tap a line to jump to it
            h.tv.setOnClickListener(v ->
                    host().seekToSec(Math.max(0, line.timeMs - PlayerState.get().lyricsOffsetMs) / 1000f));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}