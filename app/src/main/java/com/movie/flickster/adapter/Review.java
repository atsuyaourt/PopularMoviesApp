package com.movie.flickster.adapter;

/**
 * Created by yoh268 on 7/14/2016.
 */
public class Review {
    final int PREVIEW_LENGTH = 250;
    final String ELLIPSIS = "...";

    String author;
    String content;
    String url;

    public Review(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public String getPreviewContent() {
        if(content.length() <= PREVIEW_LENGTH)
            return content;
        else
            return content.substring(0,PREVIEW_LENGTH-1) + ELLIPSIS;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
