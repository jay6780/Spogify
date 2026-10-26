package com.yt.downloader.spogify.mvp.bean;

public class LyricsBean {
    private int id;
    private String name;
    private String trackName;
    private String artistName;
    private String albumName;
    private double duration;
    private boolean instrumental;
    private boolean hasWordSync;
    private String plainLyrics;
    private String syncedLyrics;
    private String lyricsfile;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }


    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }
    public boolean isInstrumental() {
        return instrumental;
    }

    public void setInstrumental(boolean instrumental) {
        this.instrumental = instrumental;
    }

    public boolean isHasWordSync() {
        return hasWordSync;
    }

    public void setHasWordSync(boolean hasWordSync) {
        this.hasWordSync = hasWordSync;
    }

    public String getPlainLyrics() {
        return plainLyrics;
    }

    public void setPlainLyrics(String plainLyrics) {
        this.plainLyrics = plainLyrics;
    }

    public String getSyncedLyrics() {
        return syncedLyrics;
    }

    public void setSyncedLyrics(String syncedLyrics) {
        this.syncedLyrics = syncedLyrics;
    }

    public String getLyricsfile() {
        return lyricsfile;
    }

    public void setLyricsfile(String lyricsfile) {
        this.lyricsfile = lyricsfile;
    }
}
