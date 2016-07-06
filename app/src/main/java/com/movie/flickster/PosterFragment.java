package com.movie.flickster;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment to load movie posters
 */
public class PosterFragment extends Fragment {

    ProgressDialog mProgressDialog; // progress dialog to show while loading contents

    ImageGridViewAdapter mMovieAdapter; // the adapter to handle movie posters
    List<String> mMovieIds; // will hold the movie IDs

    public PosterFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // This adapter will take data from a source and
        // use it to populate the GridView it is attached to.
        mMovieAdapter = new ImageGridViewAdapter(
                getActivity(), // The current context (this activity)
                new ArrayList<String>());
        mMovieIds = new ArrayList<String>();

        // Get a reference to the GridView, and attach the adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.poster_gridview);
        gridView.setAdapter(mMovieAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String movieId = mMovieIds.get(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, movieId);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        // execute the AsyncTask to fetch the movies
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        moviesTask.execute();
    }

    public class FetchMoviesTask extends AsyncTask<Void, Void, Map<String, String>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected void onPreExecute() { // show the progress dialog
            String message = getResources().getString(R.string.wait_message);
            mProgressDialog = ProgressDialog.show(getActivity(), "", message, false);
        }

        @Override
        protected Map<String, String> doInBackground(Void... voids) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            // Do not proceed if there is no internet connection
            if (!isOnline()) {
                return null;
            }

            try {
                // Get the sort order preference
                SharedPreferences sharedPrefs =
                        PreferenceManager.getDefaultSharedPreferences(getActivity());
                String sortOrder = sharedPrefs.getString(
                        getString(R.string.pref_sort_order_key),
                        getString(R.string.pref_sort_order_title_popular));

                // Construct the URL for the TheMovieDb query
                final String BASE_URL = "http://api.themoviedb.org/3/movie";
                final String APPID_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(sortOrder)
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
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the JSON data, there's no point in attemping
                // to parse it.
                return null;
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

            // try to return the parsed data
            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            if (result != null) {
                // populate the adapter and the ID arraylist
                mMovieAdapter.clear();
                mMovieIds.clear();
                for (String movieId : result.keySet()) {
                    mMovieIds.add(movieId);
                    mMovieAdapter.add(result.get(movieId));
                }
            } else { // else inform the user about the error
                Toast.makeText(
                        getActivity(),
                        R.string.error_fetch_message,
                        Toast.LENGTH_SHORT).show();
            }
            mProgressDialog.dismiss(); // hide the progress dialog
        }

        /**
         * Process the JSON string to get the needed values
         * @param movieJsonStr The JSON string to be processed
         * @return Movies data
         * @throws JSONException
         */
        private Map<String, String> getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            Map<String, String> movieInfo = new HashMap<String, String>();

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_RESULTS = "results";
            final String TMDB_ID = "id";
            final String TMDB_POSTER_PATH = "poster_path";

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMDB_RESULTS);

            for (int i = 0; i < movieArray.length(); i++) {

                // Get the JSON object representing a movie
                JSONObject movie = movieArray.getJSONObject(i);
                // The movie ID
                String movieId = movie.getString(TMDB_ID);
                // The poster URL
                String moviePosterUrl = "http://image.tmdb.org/t/p/w185" +
                        movie.getString(TMDB_POSTER_PATH);
                movieInfo.put(movieId, moviePosterUrl);

            }

            return movieInfo;
        }

        /**
         * Check for internet connectivity
         * Obtained from: http://stackoverflow.com/questions/1560788/
         *     how-to-check-internet-access-on-android-inetaddress-never-timeouts
         * @return Boolean indicating internet connectivity status
         */
        private boolean isOnline() {
            ConnectivityManager cm =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
    }
}
