package com.movie.flickster;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.movie.flickster.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by yoh268 on 7/9/2016.
 */
public class FetchMoviesTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    private final Context mContext;

    public FetchMoviesTask(Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        // Will contain the raw JSON response as a string.
        String movieJsonStr;

        String filterType;
        Uri moviesUri;
        Cursor cur;

        if(params.length == 0) {
            return null;
        }

        filterType = params[0]; // filter preference

        // Do not proceed if there is no internet connection
        if (!Utility.isOnline(mContext)) {
            return null;
        }

        try {
            // Construct the URL for the TheMovieDb query
            final String BASE_URL = "http://api.themoviedb.org/3/movie";
            final String APPID_PARAM = "api_key";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(filterType)
                    .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to the API, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            movieJsonStr = buffer.toString();
            getMovieDataFromJson(movieJsonStr, filterType);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return null;
    }

    /**
     * Process the JSON string to get the needed values
     *
     * @param movieJsonStr The JSON string to be processed
     * @throws JSONException
     */
    private void getMovieDataFromJson(String movieJsonStr, String orderType)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String TMDB_RESULTS = "results";
        final String TMDB_ID = "id";
        final String TMDB_TITLE = "title";
        final String TMDB_PLOT_SYNOPSIS = "overview";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_USER_RATING = "vote_average";
        final String TMDB_POPULARITY = "popularity";
        final String TMDB_RELEASE_DATE = "release_date";

        if (movieJsonStr == null) return;

        try {
            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMDB_RESULTS);

            // Insert the new movie information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(movieArray.length());

            for (int i = 0; i < movieArray.length(); i++) {
                long movieId;
                String title;
                String plotSynopsis;
                String posterPath;
                double userRating;
                double popularity;
                long releaseDate;

                // Get the JSON object representing a movie
                JSONObject movie = movieArray.getJSONObject(i);

                movieId = movie.getLong(TMDB_ID);
                title = movie.getString(TMDB_TITLE);
                plotSynopsis = movie.getString(TMDB_PLOT_SYNOPSIS);
                posterPath = movie.getString(TMDB_POSTER_PATH);
                userRating = movie.getDouble(TMDB_USER_RATING);
                popularity = movie.getDouble(TMDB_POPULARITY);
                releaseDate = Utility
                        .convertToJulian(movie.getString(TMDB_RELEASE_DATE), "yyyy-MM-dd");

                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieEntry._ID, movieId);
                movieValues.put(MovieEntry.COLUMN_TITLE, title);
                movieValues.put(MovieEntry.COLUMN_PLOT_SYNOPSIS, plotSynopsis);
                movieValues.put(MovieEntry.COLUMN_POSTER_PATH, posterPath);
                movieValues.put(MovieEntry.COLUMN_USER_RATING, userRating);
                movieValues.put(MovieEntry.COLUMN_POPULARITY, popularity);
                movieValues.put(MovieEntry.COLUMN_RELEASE_DATE, releaseDate);

                if(orderType.equals(MovieEntry.FILTER_POPULAR)) {
                    movieValues.put(MovieEntry.COLUMN_POPULAR, 1);
                }
                if(orderType.equals(MovieEntry.FILTER_TOP_RATED)) {
                    movieValues.put(MovieEntry.COLUMN_TOP_RATED, 1);
                }

                cVVector.add(movieValues);
            }

            int inserted = 0;
            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "FetchWeatherTask Complete. " + inserted + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
