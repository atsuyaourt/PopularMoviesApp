package com.movie.flickster.task;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.movie.flickster.BuildConfig;
import com.movie.flickster.Utility;
import com.movie.flickster.adapter.Review;
import com.movie.flickster.adapter.ReviewAdapter;

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
 * Created by yoh268 on 7/14/2016.
 */
public class FetchReviewsTask extends AsyncTask<String, Void, List<Review>> {
    private final String LOG_TAG = FetchReviewsTask.class.getSimpleName();

    private final Context mContext;
    ReviewAdapter mReviewAdapter;

    public FetchReviewsTask(Context context, ReviewAdapter reviewAdapter) {
        mContext = context;
        mReviewAdapter = reviewAdapter;
    }

    @Override
    protected List<Review> doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        // Will contain the raw JSON response as a string.
        String reviewJsonStr = null;

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
            final String REVIEW_PATH = "reviews";
            final String APPID_PARAM = "api_key";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(movieId)
                    .appendPath(REVIEW_PATH)
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

            reviewJsonStr = buffer.toString();
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
            return getReviewDataFromJson(reviewJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Review> result) {
        mReviewAdapter.setItem(result);
    }

    /**
     * Process the JSON string to get the needed values
     *
     * @param reviewJsonStr The JSON string to be processed
     * @throws JSONException
     */
    private List<Review> getReviewDataFromJson(String reviewJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String REVIEW_RESULTS = "results";
        final String REVIEW_AUTHOR = "author";
        final String REVIEW_CONTENT = "content";
        final String REVIEW_URL = "url";

        List<Review> reviewList = new ArrayList<>();

        if (reviewJsonStr == null) return null;

        try {
            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray(REVIEW_RESULTS);

            for (int i = 0; i < reviewArray.length(); i++) {
                // Get the JSON object representing a movie
                reviewJson = reviewArray.getJSONObject(i);
                Review  review =
                        new Review(reviewJson.getString(REVIEW_AUTHOR),
                                reviewJson.getString(REVIEW_CONTENT));

                review.setUrl(reviewJson.getString(REVIEW_URL));

                reviewList.add(review);
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return reviewList;
    }
}
