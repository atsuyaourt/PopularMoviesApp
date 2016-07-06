package com.movie.flickster;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A fragment to display details about a movie
 */
public class DetailFragment extends Fragment {

    ProgressDialog mProgressDialog; // progress dialog to show while loading contents

    // View elements to display movie information
    TextView mMovieTitle;
    TextView mMovieSynopsis;
    TextView mMovieReleaseYear;
    TextView mMovieReleaseDate;
    TextView mMovieUserRating;
    ImageView mMoviePoster;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // Initialize
        mMovieTitle = (TextView) rootView.findViewById(R.id.detail_movie_title);
        mMovieSynopsis = (TextView) rootView.findViewById(R.id.detail_movie_synopsis);
        mMovieReleaseYear = (TextView) rootView.findViewById(R.id.detail_movie_release_year);
        mMovieReleaseDate = (TextView) rootView.findViewById(R.id.detail_movie_release_date);
        mMovieUserRating = (TextView) rootView.findViewById(R.id.detail_movie_rating);
        mMoviePoster = (ImageView) rootView.findViewById(R.id.detail_movie_poster_thumb);

        // The detail Activity called via intent.  Inspect the intent for movie ID.
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String movieId = intent.getStringExtra(Intent.EXTRA_TEXT);

            FetchMovieInfoTask movieInfoTask = new FetchMovieInfoTask();
            movieInfoTask.execute(movieId);
        }

        return rootView;
    }

    public class FetchMovieInfoTask extends AsyncTask<String, Void, Movie> {

        private final String LOG_TAG = FetchMovieInfoTask.class.getSimpleName();

        @Override
        protected void onPreExecute() { // show the progress dialog
            String message = getResources().getString(R.string.wait_message);
            mProgressDialog = ProgressDialog.show(getActivity(),"",message,false);
        }

        @Override
        protected Movie doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Do not proceed if there is no internet connection
            if (!isOnline()) {
                return null;
            }

            // If there's no ID, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {
                // Construct the URL for the TheMovieDb query
                final String BASE_URL = "http://api.themoviedb.org/3/movie";
                final String APPID_PARAM = "api_key";


                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendPath(params[0])
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
                return getMovieInfoFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Movie movie) {
            if (movie != null) { // populate the views
                mMovieTitle.setText(movie.getTitle());
                mMovieSynopsis.setText(movie.getPlotSynopsis());
                mMovieReleaseYear.setText(movie.getReleaseDateStr("yyyy"));
                mMovieReleaseDate.setText(movie.getReleaseDateStr("MMMM dd, yyyy"));
                mMovieUserRating.setText(movie.getUserRating()+"/10");

                Picasso
                        .with(getActivity())
                        .load("http://image.tmdb.org/t/p/w185/"+movie.getPosterPath())
                        .error(R.drawable.ic_error_black_24dp)
                        .placeholder(R.drawable.progress_animation)
                        .into(mMoviePoster);
            }
            else { // else inform the user about the error
                Toast.makeText(
                        getActivity(),
                        R.string.error_fetch_detail_message,
                        Toast.LENGTH_SHORT).show();
            }

            mProgressDialog.dismiss(); // hide the progress dialog
        }

        /**
         * Get movie details from the JSON string response
         * @param movieJsonStr The JSON string to be processed
         * @return A Movie object
         * @throws JSONException
         */
        private Movie getMovieInfoFromJson(String movieJsonStr)
                throws JSONException {

            Movie movieInfo;

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_TITLE = "original_title";
            final String TMDB_PLOT_SYNOPSIS = "overview";
            final String TMDB_POSTER_PATH = "poster_path";
            final String TMDB_USER_RATING = "vote_average";
            final String TMDB_RELEASE_DATE = "release_date";

            JSONObject movieJson = new JSONObject(movieJsonStr);

            movieInfo = new Movie(movieJson.getString(TMDB_TITLE));
            movieInfo.setPlotSynopsis(movieJson.getString(TMDB_PLOT_SYNOPSIS));
            movieInfo.setPosterPath(movieJson.getString(TMDB_POSTER_PATH));
            movieInfo.setUserRating(movieJson.getDouble(TMDB_USER_RATING));

            try {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date date = formatter.parse(movieJson.getString(TMDB_RELEASE_DATE));
                movieInfo.setReleaseDate(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return movieInfo;
        }

        /**
         * Check for internet connectivity
         * Obtained from: http://stackoverflow.com/questions/1560788/
         *     how-to-check-internet-access-on-android-inetaddress-never-timeouts
         * @return Boolean indicating internet connectivity status
         */
        public boolean isOnline() {
            ConnectivityManager cm =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        }
    }
}
