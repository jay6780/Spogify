package com.yt.downloader.spogify.Utils;

import java.util.ArrayList;
import java.util.List;

public class PlayerState {
    public boolean resumeOnRestore = false;
    private static final PlayerState INSTANCE = new PlayerState();
    public static PlayerState get() { return INSTANCE; }

    /** One entry in the shared queue. */
    public static class Item {
        public String name, artist, videoId, thumb, localPath;
        public List<LrcParser.Line> parsedLyrics = new ArrayList<>();

        public Item(String name, String artist, String videoId, String thumb, String localPath) {
            this.name = name == null ? "" : name;
            this.artist = artist == null ? "" : artist;
            this.videoId = videoId == null ? "" : videoId;
            this.thumb = thumb == null ? "" : thumb;
            this.localPath = localPath == null ? "" : localPath;
        }

        public Item(String name, String artist, String videoId, String thumb, String localPath, List<LrcParser.Line> parsedLyrics) {
            this.name = name == null ? "" : name;
            this.artist = artist == null ? "" : artist;
            this.videoId = videoId == null ? "" : videoId;
            this.thumb = thumb == null ? "" : thumb;
            this.localPath = localPath == null ? "" : localPath;
            this.parsedLyrics = parsedLyrics == null ? new ArrayList<>() : parsedLyrics;
        }
    }

    public boolean hasTrack = false;
    public boolean isLocal = false;
    public boolean isPlaying = false;
    public String videoId = "";
    public String localPath = "";
    public String title = "";
    public String artist = "";
    public String thumb = "";
    public float positionSec = 0f;
    public float durationSec = 0f;
    public float lyricsDurationSec = 0f;
    public long lyricsOffsetMs = 0;
    // Active track lyrics
    public List<LrcParser.Line> parsedLyrics = new ArrayList<>();

    // Shared queue: used by Next from any screen or from the notification
    public final List<Item> queue = new ArrayList<>();
    public int queueIndex = -1;
}