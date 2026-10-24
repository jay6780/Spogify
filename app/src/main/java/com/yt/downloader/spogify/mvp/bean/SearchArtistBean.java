package com.yt.downloader.spogify.mvp.bean;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchArtistBean {

    @SerializedName("results")
    private Results results;

    public Results getResults() {
        return results;
    }

    public void setResults(Results results) {
        this.results = results;
    }

    public static class Results {
        @SerializedName("opensearch:Query")
        private Query query;

        @SerializedName("opensearch:totalResults")
        private String totalResults;

        @SerializedName("opensearch:startIndex")
        private String startIndex;

        @SerializedName("opensearch:itemsPerPage")
        private String itemsPerPage;

        @SerializedName("artistmatches")
        private ArtistMatches artistMatches;

        @SerializedName("@attr")
        private Attr attr;

        public Query getQuery() {
            return query;
        }

        public void setQuery(Query query) {
            this.query = query;
        }

        public String getTotalResults() {
            return totalResults;
        }

        public void setTotalResults(String totalResults) {
            this.totalResults = totalResults;
        }

        public String getStartIndex() {
            return startIndex;
        }

        public void setStartIndex(String startIndex) {
            this.startIndex = startIndex;
        }

        public String getItemsPerPage() {
            return itemsPerPage;
        }

        public void setItemsPerPage(String itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
        }

        public ArtistMatches getArtistMatches() {
            return artistMatches;
        }

        public void setArtistMatches(ArtistMatches artistMatches) {
            this.artistMatches = artistMatches;
        }

        public Attr getAttr() {
            return attr;
        }

        public void setAttr(Attr attr) {
            this.attr = attr;
        }
    }

    public static class Query {
        @SerializedName("#text")
        private String text;

        @SerializedName("role")
        private String role;

        @SerializedName("searchTerms")
        private String searchTerms;

        @SerializedName("startPage")
        private String startPage;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getSearchTerms() {
            return searchTerms;
        }

        public void setSearchTerms(String searchTerms) {
            this.searchTerms = searchTerms;
        }

        public String getStartPage() {
            return startPage;
        }

        public void setStartPage(String startPage) {
            this.startPage = startPage;
        }
    }

    public static class ArtistMatches {
        @SerializedName("artist")
        private List<Artist> artistList;

        public List<Artist> getArtistList() {
            return artistList;
        }

        public void setArtistList(List<Artist> artistList) {
            this.artistList = artistList;
        }
    }

    public static class Artist {
        @SerializedName("name")
        private String name;

        @SerializedName("listeners")
        private String listeners;

        @SerializedName("mbid")
        private String mbid;

        @SerializedName("url")
        private String url;

        @SerializedName("streamable")
        private String streamable;

        @SerializedName("image")
        private List<Image> imageList;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public List<Image> getImageList() {
            return imageList;
        }

        public void setImageList(List<Image> imageList) {
            this.imageList = imageList;
        }

        // Helper method to get a non-empty image URL (prefers large/extralarge or falls back to first available)
        public String getLargeImageUrl() {
            if (imageList == null || imageList.isEmpty()) return "";
            for (Image img : imageList) {
                if (("large".equalsIgnoreCase(img.getSize()) || "extralarge".equalsIgnoreCase(img.getSize())) 
                        && img.getText() != null && !img.getText().isEmpty()) {
                    return img.getText();
                }
            }
            // Fallback to any non-empty image text
            for (Image img : imageList) {
                if (img.getText() != null && !img.getText().isEmpty()) {
                    return img.getText();
                }
            }
            return "";
        }
    }

    public static class Image {
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

    public static class Attr {
        @SerializedName("for")
        private String forQuery;

        public String getForQuery() {
            return forQuery;
        }

        public void setForQuery(String forQuery) {
            this.forQuery = forQuery;
        }
    }
}