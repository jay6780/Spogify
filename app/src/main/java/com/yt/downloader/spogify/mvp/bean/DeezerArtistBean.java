package com.yt.downloader.spogify.mvp.bean;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DeezerArtistBean {

    @SerializedName("data")
    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public static class DataBean {
        @SerializedName("picture_medium")
        private String pictureMedium;

        @SerializedName("picture_big")
        private String pictureBig;

        @SerializedName("picture_xl")
        private String pictureXl;

        public String getPictureMedium() {
            return pictureMedium;
        }

        public String getPictureBig() {
            return pictureBig;
        }

        public String getPictureXl() {
            return pictureXl;
        }
    }
}