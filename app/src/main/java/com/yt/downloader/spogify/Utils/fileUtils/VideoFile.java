package com.yt.downloader.spogify.Utils.fileUtils;

public class VideoFile {
    public String name, path;
    public long size, lastModified;
    private boolean selected = false;

    public VideoFile(String name, String path, long size, long lastModified) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.lastModified = lastModified;
    }

    public long getSize() {
        return size;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public long getLastModified() {
        return lastModified;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}