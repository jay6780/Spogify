package com.yt.downloader.spogify.mvp.bean;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DeezerTrackBean {

    @SerializedName("data")
    private List<TrackData> data;

    public List<TrackData> getData() {
        return data;
    }

    public void setData(List<TrackData> data) {
        this.data = data;
    }

    public static class TrackData {

        @SerializedName("id")
        private long id;

        @SerializedName("title")
        private String title;

        @SerializedName("album")
        private Album album;

        public long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public Album getAlbum() {
            return album;
        }

        public static class Album {

            @SerializedName("id")
            private long id;

            @SerializedName("title")
            private String title;

            @SerializedName("cover")
            private String cover;

            @SerializedName("cover_small")
            private String coverSmall;

            @SerializedName("cover_medium")
            private String coverMedium;

            @SerializedName("cover_big")
            private String coverBig;

            @SerializedName("cover_xl")
            private String coverXl;

            public long getId() {
                return id;
            }

            public String getTitle() {
                return title;
            }

            public String getCover() {
                return cover;
            }

            public String getCoverSmall() {
                return coverSmall;
            }

            public String getCoverMedium() {
                return coverMedium;
            }

            public String getCoverBig() {
                return coverBig;
            }

            public String getCoverXl() {
                return coverXl;
            }
        }
    }
}