package com.yt.downloader.spogify.mvp.bean;

public class FavoriteTrack {
    private String musicName;
    private String artistName;
    private String imageThumb;
    private String totalListeners;
    private boolean isFromFavorite;

    public FavoriteTrack(String musicName, String artistName, String imageThumb, String totalListeners, boolean isFromFavorite) {
        this.musicName = musicName;
        this.artistName = artistName;
        this.imageThumb = imageThumb;
        this.totalListeners = totalListeners;
        this.isFromFavorite = isFromFavorite;
    }

    public String getMusicName() { return musicName; }
    public String getArtistName() { return artistName; }
    public String getImageThumb() { return imageThumb; }
    public String getTotalListeners() { return totalListeners; }
    public boolean isFromFavorite() { return isFromFavorite; }
}