package com.yt.downloader.spogify.mvp.bean;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TrackInfoBean {

    @SerializedName("toptracks")
    private TopTracks topTracks;

    public TopTracks getTopTracks() {
        return topTracks;
    }

    public void setTopTracks(TopTracks topTracks) {
        this.topTracks = topTracks;
    }


    public static class TopTracks {

        @SerializedName("track")
        private List<Track> trackList;

        @SerializedName("@attr")
        private TopTracksAttr attributes;

        public List<Track> getTrackList() {
            return trackList;
        }

        public void setTrackList(List<Track> trackList) {
            this.trackList = trackList;
        }

        public TopTracksAttr getAttributes() {
            return attributes;
        }

        public void setAttributes(TopTracksAttr attributes) {
            this.attributes = attributes;
        }
    }

    public static class Track {

        @SerializedName("name")
        private String name;

        @SerializedName("playcount")
        private String playcount;

        @SerializedName("listeners")
        private String listeners;

        @SerializedName("mbid")
        private String mbid;

        @SerializedName("url")
        private String url;

        @SerializedName("streamable")
        private String streamable;

        @SerializedName("artist")
        private Artist artist;

        @SerializedName("image")
        private List<Image> imageList;

        @SerializedName("@attr")
        private TrackAttr attributes;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public String getStreamable() {
            return streamable;
        }

        public void setStreamable(String streamable) {
            this.streamable = streamable;
        }

        public Artist getArtist() {
            return artist;
        }

        public void setArtist(Artist artist) {
            this.artist = artist;
        }

        public List<Image> getImageList() {
            return imageList;
        }

        public void setImageList(List<Image> imageList) {
            this.imageList = imageList;
        }

        public TrackAttr getAttributes() {
            return attributes;
        }

        public void setAttributes(TrackAttr attributes) {
            this.attributes = attributes;
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

    public static class Image {

        @SerializedName("#text")
        private String url;

        @SerializedName("size")
        private String size;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }
    }

    public static class TrackAttr {

        @SerializedName("rank")
        private String rank;

        public String getRank() {
            return rank;
        }

        public void setRank(String rank) {
            this.rank = rank;
        }
    }

    public static class TopTracksAttr {

        @SerializedName("artist")
        private String artist;

        @SerializedName("page")
        private String page;

        @SerializedName("perPage")
        private String perPage;

        @SerializedName("totalPages")
        private String totalPages;

        @SerializedName("total")
        private String total;

        public String getArtist() {
            return artist;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

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