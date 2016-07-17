package com.movie.flickster.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.movie.flickster.data.MovieContract.MovieEntry;

/**
 * Manages a local database for movie data.
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final int VIEW_LIMIT = 20;

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_PLOT_SYNOPSIS + " TEXT, " +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT, " +
                MovieEntry.COLUMN_USER_RATING + " REAL, " +
                MovieEntry.COLUMN_POPULARITY + " REAL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " INTEGER, " +
                MovieEntry.COLUMN_POPULAR + " INTEGER, " +
                MovieEntry.COLUMN_TOP_RATED + " INTEGER, " +
                MovieEntry.COLUMN_FAVORITE + " INTEGER " +
                " );";

        final String SQL_CREATE_POP_MOVIE_VIEW = "CREATE VIEW IF NOT EXISTS " +
                MovieEntry.getTableViewFromFilter(MovieEntry.FILTER_POPULAR) + " AS " +
                " SELECT " +
                    MovieEntry._ID + ", " +
                    MovieEntry.COLUMN_TITLE + ", " +
                    MovieEntry.COLUMN_PLOT_SYNOPSIS + ", " +
                    MovieEntry.COLUMN_POSTER_PATH + ", " +
                    MovieEntry.COLUMN_USER_RATING + ", " +
                    MovieEntry.COLUMN_POPULARITY + ", " +
                    MovieEntry.COLUMN_RELEASE_DATE + ", " +
                    MovieEntry.COLUMN_FAVORITE +
                " FROM " + MovieEntry.TABLE_NAME +
                " WHERE " + MovieEntry.COLUMN_POPULAR + " = 1" +
                " LIMIT " + VIEW_LIMIT +
                ";";

        final String SQL_CREATE_TOP_MOVIE_VIEW = "CREATE VIEW IF NOT EXISTS " +
                MovieEntry.getTableViewFromFilter(MovieEntry.FILTER_TOP_RATED) + " AS " +
                " SELECT " +
                    MovieEntry._ID + ", " +
                    MovieEntry.COLUMN_TITLE + ", " +
                    MovieEntry.COLUMN_PLOT_SYNOPSIS + ", " +
                    MovieEntry.COLUMN_POSTER_PATH + ", " +
                    MovieEntry.COLUMN_USER_RATING + ", " +
                    MovieEntry.COLUMN_POPULARITY + ", " +
                    MovieEntry.COLUMN_RELEASE_DATE + ", " +
                    MovieEntry.COLUMN_FAVORITE +
                " FROM " + MovieEntry.TABLE_NAME +
                " WHERE " + MovieEntry.COLUMN_TOP_RATED + " = 1" +
                " LIMIT " + VIEW_LIMIT +
                ";";

        final String SQL_CREATE_FAV_MOVIE_VIEW = "CREATE VIEW IF NOT EXISTS " +
                MovieEntry.getTableViewFromFilter(MovieEntry.FILTER_FAVORITE) + " AS " +
                " SELECT " +
                MovieEntry._ID + ", " +
                MovieEntry.COLUMN_TITLE + ", " +
                MovieEntry.COLUMN_PLOT_SYNOPSIS + ", " +
                MovieEntry.COLUMN_POSTER_PATH + ", " +
                MovieEntry.COLUMN_USER_RATING + ", " +
                MovieEntry.COLUMN_POPULARITY + ", " +
                MovieEntry.COLUMN_RELEASE_DATE + ", " +
                MovieEntry.COLUMN_FAVORITE +
                " FROM " + MovieEntry.TABLE_NAME +
                " WHERE " + MovieEntry.COLUMN_FAVORITE + " = 1" +
                ";";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_POP_MOVIE_VIEW);
        sqLiteDatabase.execSQL(SQL_CREATE_TOP_MOVIE_VIEW);
        sqLiteDatabase.execSQL(SQL_CREATE_FAV_MOVIE_VIEW);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP VIEW IF EXISTS " +
                MovieEntry.getTableViewFromFilter(MovieEntry.FILTER_POPULAR));
        sqLiteDatabase.execSQL("DROP VIEW IF EXISTS " +
                MovieEntry.getTableViewFromFilter(MovieEntry.FILTER_TOP_RATED));
        sqLiteDatabase.execSQL("DROP VIEW IF EXISTS " +
                MovieEntry.getTableViewFromFilter(MovieEntry.FILTER_FAVORITE));
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
