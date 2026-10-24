package com.yt.downloader.spogify.mvp.bean;

public class FavoriteTrack {
    private String musicName;
    private String artistName;
    private String imageThumb;
    private String totalListeners;
    private boolean isFromFavorite;

    public FavoriteTrack() {
    }


    public FavoriteTrack(String musicName, String artistName, String imageThumb, String totalListeners) {
        this.musicName = musicName;
        this.artistName = artistName;
        this.imageThumb = imageThumb;
        this.totalListeners = totalListeners;
        this.isFromFavorite = false;
    }


    public FavoriteTrack(String musicName, String artistName, String imageThumb, String totalListeners, boolean isFromFavorite) {
        this.musicName = musicName;
        this.artistName = artistName;
        this.imageThumb = imageThumb;
        this.totalListeners = totalListeners;
        this.isFromFavorite = isFromFavorite;
    }

    public String getMusicName() {
        return musicName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getImageThumb() {
        return imageThumb;
    }

    public String getTotalListeners() {
        return totalListeners;
    }

    public boolean isFromFavorite() {
        return isFromFavorite;
    }
    
    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setImageThumb(String imageThumb) {
        this.imageThumb = imageThumb;
    }

    public void setTotalListeners(String totalListeners) {
        this.totalListeners = totalListeners;
    }

    public void setFromFavorite(boolean fromFavorite) {
        isFromFavorite = fromFavorite;
    }
}