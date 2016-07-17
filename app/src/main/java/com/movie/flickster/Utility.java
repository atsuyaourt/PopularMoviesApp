package com.movie.flickster;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.movie.flickster.data.MovieContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yoh268 on 7/10/2016.
 */
public class Utility {
    final static String TMDB_IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185";

    public static long convertToJulian(String dateStr, String format) {
        long retVal = -1;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
            Date date = formatter.parse(dateStr);
            retVal = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    public static long nearestDay(Date date) {
        long retVal = -1;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateStr = formatter.format(date);
            retVal = formatter.parse(dateStr).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    public static String getDateStr(long date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        Date dDate = new Date(date);
        return formatter.format(dDate);
    }

    public static String getPreferredFilterType(Context context) {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(
                context.getString(R.string.pref_filter_key),
                context.getString(R.string.pref_filter_value_popular));
    }

    /**
     * Check for internet connectivity
     * Obtained from: http://stackoverflow.com/questions/1560788/
     * how-to-check-internet-access-on-android-inetaddress-never-timeouts
     * @param context
     * @return Boolean indicating internet connectivity status
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static String buildTMDBPosterUrl(String imgPath) {
        return TMDB_IMAGE_BASE_URL + imgPath;
    }


    public static void cleanDb(Context context) {
        StringBuilder inQuery = new StringBuilder();
        int entries = 0;

        inQuery.append("(");

        Cursor cursorTop = context.getContentResolver()
                .query(MovieContract.MovieEntry.buildMovieWithFilter(MovieContract.MovieEntry.FILTER_TOP_RATED),
                        new String[]{MovieContract.MovieEntry._ID},
                        null, null, null);

        for (int i = 0; i < cursorTop.getCount(); i++) {
            cursorTop.moveToPosition(i);
            if (i == 0) {
                inQuery.append("'").append(cursorTop.getLong(0)).append("'");
            } else {
                inQuery.append(", '").append(cursorTop.getLong(0)).append("'");
            }
        }
        entries = cursorTop.getCount();
        cursorTop.close();

        Cursor cursorPop = context.getContentResolver()
                .query(MovieContract.MovieEntry.buildMovieWithFilter(MovieContract.MovieEntry.FILTER_POPULAR),
                        new String[]{MovieContract.MovieEntry._ID},
                        null, null, null);

        for (int i = 0; i < cursorPop.getCount(); i++) {
            cursorPop.moveToPosition(i);
            if (i == 0 && entries == 0) {
                inQuery.append("'").append(cursorPop.getLong(0)).append("'");
            } else {
                inQuery.append(", '").append(cursorPop.getLong(0)).append("'");
            }
        }
        entries += cursorPop.getCount();
        cursorPop.close();

        inQuery.append(")");

        int deleted = context.getContentResolver().delete(
                MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry._ID + " NOT IN "+ inQuery.toString() +
                        " AND " +
                        " ( " + MovieContract.MovieEntry.COLUMN_FAVORITE + " = 0 OR " +
                        MovieContract.MovieEntry.COLUMN_FAVORITE + " IS NULL )",
                null);
    }
}
