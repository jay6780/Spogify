package com.yt.downloader.spogify.mvp.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TopArtistBean {

    @SerializedName("artists")
    private ArtistsBean artists;

    public ArtistsBean getArtists() {
        return artists;
    }

    public void setArtists(ArtistsBean artists) {
        this.artists = artists;
    }

    public static class ArtistsBean {

        @SerializedName("artist")
        private List<ArtistBean> artist;

        @SerializedName("@attr")
        private AttrBean attr;

        public List<ArtistBean> getArtist() {
            return artist;
        }

        public void setArtist(List<ArtistBean> artist) {
            this.artist = artist;
        }

        public AttrBean getAttr() {
            return attr;
        }

        public void setAttr(AttrBean attr) {
            this.attr = attr;
        }

        public static class AttrBean {

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

        public static class ArtistBean {

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

            @SerializedName("image")
            private List<ImageBean> image;

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

            public List<ImageBean> getImage() {
                return image;
            }

            public void setImage(List<ImageBean> image) {
                this.image = image;
            }

            public static class ImageBean {

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
        }
    }
}