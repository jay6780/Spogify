package com.yt.downloader.spogify.mvp.bean;

public class SearchTotalBean {
    private String thumbnail;
    private String name;
    private String duration;
    private String videoId;

    private boolean isArtist;


    public SearchTotalBean(String thumbnail,String name,String duration,String videoId,boolean isArtist){
        this.thumbnail = thumbnail;
        this.name = name;
        this.duration = duration;
        this.isArtist = isArtist;
        this.videoId = videoId;

    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public boolean isArtist() {
        return isArtist;
    }

    public void setArtist(boolean artist) {
        isArtist = artist;
    }
}
