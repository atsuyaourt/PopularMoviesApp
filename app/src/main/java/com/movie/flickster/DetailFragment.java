package com.movie.flickster;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.movie.flickster.data.MovieContract.MovieEntry;
import com.squareup.picasso.Picasso;

/**
 * A fragment to display details about a movie
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] MOVIE_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_PLOT_SYNOPSIS,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_USER_RATING,
            MovieEntry.COLUMN_POSTER_PATH
    };

    static final int COL_MOVIE_ID = 0;
    static final int COL_TITLE = 1;
    static final int COL_PLOT_SYNOPSIS = 2;
    static final int COL_RELEASE_DATE = 3;
    static final int COL_USER_RATING = 4;
    static final int COL_POSTER_PATH = 5;

    // View elements to display movie information
    TextView mTitleView;
    TextView mSynopsisView;
    TextView mReleaseYearView;
    TextView mReleaseDateView;
    TextView mUserRatingView;
    ImageView mPosterView;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.movie_detail, container, false);

        mTitleView = (TextView) rootView.findViewById(R.id.detail_movie_title);
        mSynopsisView = (TextView) rootView.findViewById(R.id.detail_movie_synopsis);
        mReleaseYearView = (TextView) rootView.findViewById(R.id.detail_movie_release_year);
        mReleaseDateView = (TextView) rootView.findViewById(R.id.detail_movie_release_date);
        mUserRatingView = (TextView) rootView.findViewById(R.id.detail_movie_rating);
        mPosterView = (ImageView) rootView.findViewById(R.id.detail_movie_poster_thumb);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");
        if (null != mUri) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    MOVIE_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) { return; }

        mTitleView.setText(data.getString(COL_TITLE));
        mSynopsisView.setText(data.getString(COL_PLOT_SYNOPSIS));
        mUserRatingView.setText(data.getDouble(COL_USER_RATING) + "/10");

        long lDate = data.getLong(COL_RELEASE_DATE);
        mReleaseYearView.setText(Utility.getDateStr(lDate,"yyyy"));
        mReleaseDateView.setText(Utility.getDateStr(lDate,"MMM dd, yyyy"));

        String imageUrl = Utility.buildTMDBPosterUrl(data.getString(COL_POSTER_PATH));
        Picasso
                .with(getActivity())
                .load(imageUrl)
                .error(R.drawable.ic_error_black_24dp)
                .placeholder(R.drawable.progress_animation)
                .into(mPosterView);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
