package com.movie.flickster;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.movie.flickster.adapter.Trailer;
import com.movie.flickster.adapter.TrailerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yoh268 on 7/9/2016.
 */
public class FetchTrailersTask extends AsyncTask<String, Void, List<Trailer>> {

    private final String LOG_TAG = FetchTrailersTask.class.getSimpleName();

    private final Context mContext;
    TrailerAdapter mTrailerAdapter;

    public FetchTrailersTask(Context context, TrailerAdapter trailerAdapter) {
        mContext = context;
        mTrailerAdapter = trailerAdapter;
    }

    @Override
    protected List<Trailer> doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        // Will contain the raw JSON response as a string.
        String videoJsonStr = null;

        if(params.length == 0) {
            return null;
        }

        String movieId = params[0]; // movie ID

        // Do not proceed if there is no internet connection
        if (!Utility.isOnline(mContext)) {
            return null;
        }

        try {
            // Construct the URL for the TheMovieDb query
            final String BASE_URL = "http://api.themoviedb.org/3/movie";
            final String VIDEOS_PATH = "videos";
            final String APPID_PARAM = "api_key";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(movieId)
                    .appendPath(VIDEOS_PATH)
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

            videoJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
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

        try {
            return getTrailerDataFromJson(videoJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Trailer> result) {
        mTrailerAdapter.setItem(result);
    }

    /**
     * Process the JSON string to get the needed values
     *
     * @param videoJsonStr The JSON string to be processed
     * @throws JSONException
     */
    private List<Trailer> getTrailerDataFromJson(String videoJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String VIDEO_RESULTS = "results";
        final String VIDEO_KEY = "key";
        final String VIDEO_NAME = "name";
        final String VIDEO_SITE = "site";
        final String VIDEO_TYPE = "type";

        final String TYPE_TRAILER = "Trailer";
        final String SITE_YOUTUBE = "YouTube";

        List<Trailer> trailerList = new ArrayList<>();

        if (videoJsonStr == null) return null;

        try {
            JSONObject videoJson = new JSONObject(videoJsonStr);
            JSONArray videoArray = videoJson.getJSONArray(VIDEO_RESULTS);

            for (int i = 0; i < videoArray.length(); i++) {
                // Get the JSON object representing a movie
                JSONObject video = videoArray.getJSONObject(i);


                if (video.getString(VIDEO_SITE).equals(SITE_YOUTUBE) &&
                        video.getString(VIDEO_TYPE).equals(TYPE_TRAILER)) {
                    trailerList.add(
                            new Trailer(video.getString(VIDEO_NAME),video.getString(VIDEO_KEY)));

                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return trailerList;
    }
}
