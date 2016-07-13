package com.movie.flickster.adapter;

/**
 * Created by yoh268 on 7/13/2016.
 */
public class Trailer {
    String name;
    String videoKey;

    public Trailer(String name, String videoKey) {
        this.name = name;
        this.videoKey = videoKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVideoKey() {
        return videoKey;
    }

    public void setVideoKey(String videoKey) {
        this.videoKey = videoKey;
    }

    public String buildYouTubeImgUrl() {
        return "http://img.youtube.com/vi/" + this.videoKey + "/0.jpg";
    }

    public String buildYouTubeUrl() {
        return "http://www.youtube.com/watch?v=" + this.videoKey;
    }
}
