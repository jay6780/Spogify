package com.yt.downloader.spogify.mvp.bean;

public class Mp3downloadBean {

    /**
     * downloadUrl : https://cdn403.savetube.vip/media/Y8EEnKpD4nY/wrong-about-forever-128-ytshorts.savetube.me.mp3
     * title : Wrong About Forever
     * thumbnail : https://i.ytimg.com/vi_webp/Y8EEnKpD4nY/maxresdefault.webp
     * type : mp3
     * quality : 128
     */

    private String downloadUrl;
    private String title;
    private String thumbnail;
    private String type;
    private int quality;

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }
}
