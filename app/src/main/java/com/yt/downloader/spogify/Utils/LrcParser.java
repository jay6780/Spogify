package com.yt.downloader.spogify.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LrcParser {

    public static class Line {
        public final long timeMs;
        public final String text;
        public Line(long timeMs, String text) {
            this.timeMs = timeMs;
            this.text = text;
        }
    }

    private static final Pattern TS =
            Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?\\]");

    public static List<Line> parse(String lrc) {
        List<Line> out = new ArrayList<>();
        if (lrc == null || lrc.trim().isEmpty()) return out;

        for (String raw : lrc.split("\\r?\\n")) {
            Matcher m = TS.matcher(raw);
            List<Long> times = new ArrayList<>();
            int lastEnd = 0;
            while (m.find()) {
                long min = Long.parseLong(m.group(1));
                long sec = Long.parseLong(m.group(2));
                String frac = m.group(3);
                long ms = 0;
                if (frac != null) {
                    while (frac.length() < 3) frac += "0";   // "76" -> 760ms
                    ms = Long.parseLong(frac);
                }
                times.add(min * 60000 + sec * 1000 + ms);
                lastEnd = m.end();
            }
            if (times.isEmpty()) continue;
            String text = raw.substring(lastEnd).trim();
            for (long t : times) out.add(new Line(t, text));
        }

        Collections.sort(out, (a, b) -> Long.compare(a.timeMs, b.timeMs));
        return out;
    }
}