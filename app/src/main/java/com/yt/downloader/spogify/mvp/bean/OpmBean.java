package com.yt.downloader.spogify.mvp.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OpmBean {

    @SerializedName("topartists")
    private TopArtistsContainer topArtists;

    public TopArtistsContainer getTopArtists() {
        return topArtists;
    }

    public void setTopArtists(TopArtistsContainer topArtists) {
        this.topArtists = topArtists;
    }

    // --- Inner Classes ---

    public static class TopArtistsContainer {

        @SerializedName("artist")
        private List<Artist> artist;

        @SerializedName("@attr")
        private Attributes attr;

        public List<Artist> getArtist() {
            return artist;
        }

        public void setArtist(List<Artist> artist) {
            this.artist = artist;
        }

        public Attributes getAttr() {
            return attr;
        }

        public void setAttr(Attributes attr) {
            this.attr = attr;
        }
    }

    public static class Artist {

        @SerializedName("name")
        private String name;

        @SerializedName("mbid")
        private String mbid;

        @SerializedName("url")
        private String url;

        @SerializedName("streamable")
        private String streamable;

        @SerializedName("image")
        private List<ArtistImage> images;

        @SerializedName("@attr")
        private ArtistRank attr;

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

        public String getStreamable() {
            return streamable;
        }

        public void setStreamable(String streamable) {
            this.streamable = streamable;
        }

        public List<ArtistImage> getImages() {
            return images;
        }

        public void setImages(List<ArtistImage> images) {
            this.images = images;
        }

        public ArtistRank getAttr() {
            return attr;
        }

        public void setAttr(ArtistRank attr) {
            this.attr = attr;
        }
    }

    public static class ArtistImage {

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

    public static class ArtistRank {

        @SerializedName("rank")
        private String rank;

        public String getRank() {
            return rank;
        }

        public void setRank(String rank) {
            this.rank = rank;
        }
    }

    public static class Attributes {

        @SerializedName("tag")
        private String tag;

        @SerializedName("page")
        private String page;

        @SerializedName("perPage")
        private String perPage;

        @SerializedName("totalPages")
        private String totalPages;

        @SerializedName("total")
        private String total;

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
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