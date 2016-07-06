package com.movie.flickster;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A class to hold information about a movie
 */
public class Movie {
    private String title; // movie title
    private String plotSynopsis; // plot synopsis
    private String posterPath; // movie poster
    private double userRating; // user rating
    private Date releaseDate; // release date

    public Movie(String title) {
        this.title = title;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public void setPlotSynopsis(String plot_synopsis) {
        this.plotSynopsis = plot_synopsis;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public String getReleaseDateStr(String format) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(format);
        return dateFormatter.format(this.releaseDate);
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getUserRating() {
        return userRating;
    }

    public void setUserRating(double userRating) {
        this.userRating = userRating;
    }
}
