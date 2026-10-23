package com.yt.downloader.spogify.mvp.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TopTrackBean {

    @SerializedName("tracks")
    private TracksContainer tracks;

    public TracksContainer getTracks() {
        return tracks;
    }

    public void setTracks(TracksContainer tracks) {
        this.tracks = tracks;
    }

    // --- Inner Classes ---

    public static class TracksContainer {

        @SerializedName("track")
        private List<Track> track;

        @SerializedName("@attr")
        private Attributes attr;

        public List<Track> getTrack() {
            return track;
        }

        public void setTrack(java.util.List<Track> track) {
            this.track = track;
        }

        public Attributes getAttr() {
            return attr;
        }

        public void setAttr(Attributes attr) {
            this.attr = attr;
        }
    }

    public static class Track {

        @SerializedName("name")
        private String name;

        @SerializedName("duration")
        private String duration;

        @SerializedName("playcount")
        private String playcount;

        @SerializedName("listeners")
        private String listeners;

        @SerializedName("mbid")
        private String mbid;

        @SerializedName("url")
        private String url;

        @SerializedName("streamable")
        private Streamable streamable;

        @SerializedName("artist")
        private Artist artist;

        @SerializedName("image")
        private java.util.List<TrackImage> images;

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

        public String getPlaycount() {
            return playcount;
        }

        public void setPlaycount(String playcount) {
            this.playcount = playcount;
        }

        public String getListeners() {
            return listeners;
        }

        public void setListeners(String listeners) {
            this.listeners = listeners;
        }

        public String getMbid() {
            return mbid;
        }

        public void setMbid(String mbid) {
            this.mbid = mbid;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Streamable getStreamable() {
            return streamable;
        }

        public void setStreamable(Streamable streamable) {
            this.streamable = streamable;
        }

        public Artist getArtist() {
            return artist;
        }

        public void setArtist(Artist artist) {
            this.artist = artist;
        }

        public java.util.List<TrackImage> getImages() {
            return images;
        }

        public void setImages(java.util.List<TrackImage> images) {
            this.images = images;
        }
    }

    public static class Artist {

        @SerializedName("name")
        private String name;

        @SerializedName("mbid")
        private String mbid;

        @SerializedName("url")
        private String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMbid() {
            return mbid;
        }

        public void setMbid(String mbid) {
            this.mbid = mbid;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Streamable {

        @SerializedName("#text")
        private String text;

        @SerializedName("fulltrack")
        private String fulltrack;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getFulltrack() {
            return fulltrack;
        }

        public void setFulltrack(String fulltrack) {
            this.fulltrack = fulltrack;
        }
    }

    public static class TrackImage {

        @SerializedName("#text")
        private String text;

        @SerializedName("size")
        private String size;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }
    }

    public static class Attributes {

        @SerializedName("page")
        private String page;

        @SerializedName("perPage")
        private String perPage;

        @SerializedName("totalPages")
        private String totalPages;

        @SerializedName("total")
        private String total;

        public String getPage() {
            return page;
        }

        public void setPage(String page) {
            this.page = page;
        }

        public String getPerPage() {
            return perPage;
        }

        public void setPerPage(String perPage) {
            this.perPage = perPage;
        }

        public String getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(String totalPages) {
            this.totalPages = totalPages;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }
    }
}