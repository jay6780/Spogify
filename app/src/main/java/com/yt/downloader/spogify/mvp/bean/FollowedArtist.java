package com.yt.downloader.spogify.mvp.bean;

public class FollowedArtist {
    private String artistsName;
    private String image;
    private String totalListeners;

    public FollowedArtist(String artistsName, String image, String totalListeners) {
        this.artistsName = artistsName;
        this.image = image;
        this.totalListeners = totalListeners;
    }

    public String getArtistsName() {
        return artistsName;
    }

    public void setArtistsName(String artistsName) {
        this.artistsName = artistsName;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTotalListeners() {
        return totalListeners;
    }

    public void setTotalListeners(String totalListeners) {
        this.totalListeners = totalListeners;
    }
}