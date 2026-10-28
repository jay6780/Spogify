package com.yt.downloader.spogify;


import com.yt.downloader.spogify.mvp.bean.LyricsBean;

public class LyricsEvent {
    private final LyricsBean bean;
    private final String musicTitle;

    public LyricsEvent(LyricsBean bean, String musicTitle) {
        this.bean = bean;
        this.musicTitle = musicTitle;
    }

    public LyricsBean getBean() {
        return bean;
    }

    public String getMusicTitle() {
        return musicTitle;
    }
}