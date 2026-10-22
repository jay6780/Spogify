package com.yt.downloader.spogify.mvp.bean;

import java.util.List;

public class YoutubeSearchBean {

    private List<ItemsBean> items;

    public List<ItemsBean> getItems() {
        return items;
    }

    public void setItems(List<ItemsBean> items) {
        this.items = items;
    }

    public static class ItemsBean {
        /**
         * duration : 3:25
         * id : mhoSmDvePV4
         * thumbnail : https://i.ytimg.com/vi/mhoSmDvePV4/hq720.jpg?sqp=-oaymwEgCOgCEMoBSFXyq4qpAxIIARUAAIhCGAFwAcABBrgC8xg=&rs=AOn4CLCCtVpbpcsqNwdHJ-BUAVKoxXROow
         * title : Rockstar - Parting Time (Lyrics)
         * url : https://www.youtube.com/watch?v=mhoSmDvePV4
         */

        private String duration;
        private String id;
        private String thumbnail;
        private String title;
        private String url;

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
