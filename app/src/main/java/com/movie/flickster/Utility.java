package com.movie.flickster;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yoh268 on 7/10/2016.
 */
public class Utility {
    final static String TMDB_IMAGE_BASE_URL = "http://image.tmdb.org/t/p/w185";

    public static long convertToJulian(String dateStr, String format) {
        long retVal = -1;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
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
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = formatter.format(date);
            retVal = formatter.parse(dateStr).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    public static String getDateStr(long date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date dDate = new Date(date);
        return formatter.format(dDate);
    }

    public static String getPreferredFilterType(Context context) {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(context);
        String filterType = sharedPrefs.getString(
                context.getString(R.string.pref_filter_key),
                context.getString(R.string.pref_filter_value_popular));

        return filterType;
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
}
