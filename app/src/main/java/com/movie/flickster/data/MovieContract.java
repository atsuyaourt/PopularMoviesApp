package com.movie.flickster.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the movie database.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.movie.flickster.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    public static final class MovieEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String FILTER_POPULAR = "popular";
        public static final String FILTER_TOP_RATED = "top_rated";
        public static final String FILTER_FAVORITE = "favorite";

        public static final String TABLE_NAME = "movie";

        // Title is stored as a String
        public static final String COLUMN_TITLE = "title";
        // Plot synopsis is stored as a String
        public static final String COLUMN_PLOT_SYNOPSIS = "plot_synopsis";
        // Poster path is stored as a String
        public static final String COLUMN_POSTER_PATH = "poster_path";
        // User rating is stored as a float
        public static final String COLUMN_USER_RATING = "user_rating";
        // User rating is stored as a float
        public static final String COLUMN_POPULARITY = "popularity";
        // Release date is stored as long in milliseconds since the epoch
        public static final String COLUMN_RELEASE_DATE = "release_date";

        // Popular is stored as boolean
        public static final String COLUMN_POPULAR = "popular";
        // Top rated is stored as boolean
        public static final String COLUMN_TOP_RATED = "top_rated";
        // Favorite is stored as boolean
        public static final String COLUMN_FAVORITE = "favorite";

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieWithFilter(String filter) {
            return CONTENT_URI.buildUpon()
                    .appendPath(filter).build();
        }

        public static String getFilterFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static long getIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }

        public static String getTableViewFromFilter(String filter) {
            return TABLE_NAME + "_" + filter;
        }
    }
}
