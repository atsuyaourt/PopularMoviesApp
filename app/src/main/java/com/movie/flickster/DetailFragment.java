package com.movie.flickster;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.movie.flickster.adapter.ReviewAdapter;
import com.movie.flickster.adapter.TrailerAdapter;
import com.movie.flickster.data.MovieContract.MovieEntry;
import com.movie.flickster.task.FetchReviewsTask;
import com.movie.flickster.task.FetchTrailersTask;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.Unbinder;

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
            MovieEntry.COLUMN_POSTER_PATH,
            MovieEntry.COLUMN_FAVORITE
    };

    static final int COL_MOVIE_ID = 0;
    static final int COL_TITLE = 1;
    static final int COL_PLOT_SYNOPSIS = 2;
    static final int COL_RELEASE_DATE = 3;
    static final int COL_USER_RATING = 4;
    static final int COL_POSTER_PATH = 5;
    static final int COL_FAVORITE = 6;

    // View elements to display movie information
    @BindView(R.id.detail_movie_title) TextView mTitleView;
    @BindView(R.id.detail_movie_synopsis) TextView mSynopsisView;
    @BindView(R.id.detail_movie_year_text) TextView mReleaseYearView;
    @BindView(R.id.detail_movie_date_text) TextView mReleaseDateView;
    @BindView(R.id.detail_movie_rating_text) TextView mUserRatingView;
    @BindView(R.id.detail_movie_poster_thumb) ImageView mPosterView;
    @BindView(R.id.detail_movie_favorite_check) CheckBox mFavoriteCheck;

    @BindView(R.id.trailer_recycler_view) RecyclerView mTrailerView;
    @BindView(R.id.review_recycler_view) RecyclerView mReviewView;

    TrailerAdapter mTrailerAdapter;
    ReviewAdapter mReviewAdapter;

    private Unbinder mUnbinder;

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
        mUnbinder = ButterKnife.bind(this, rootView);

        mTrailerAdapter = new TrailerAdapter(getActivity(), null);

        LinearLayoutManager trailerLManager = new LinearLayoutManager(getActivity());
        trailerLManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mTrailerView.setLayoutManager(trailerLManager);
        mTrailerView.setAdapter(mTrailerAdapter);

        mReviewAdapter = new ReviewAdapter(getActivity(), null);

        LinearLayoutManager reviewLManager = new LinearLayoutManager(getActivity());
        reviewLManager.setOrientation(LinearLayoutManager.VERTICAL);
        mReviewView.setLayoutManager(reviewLManager);
        mReviewView.setAdapter(mReviewAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) { return; }

        mTitleView.setText(data.getString(COL_TITLE));
        mSynopsisView.setText(data.getString(COL_PLOT_SYNOPSIS));
        mUserRatingView.setText(Math.round(data.getDouble(COL_USER_RATING) * 10) / 10.0 + "/10");

        long lDate = data.getLong(COL_RELEASE_DATE);
        mReleaseYearView.setText(Utility.getDateStr(lDate,"yyyy"));
        mReleaseDateView.setText(Utility.getDateStr(lDate,"MMM dd, yyyy"));

        mFavoriteCheck.setChecked(data.getInt(COL_FAVORITE) == 1);

        updateDetails(data.getLong(COL_MOVIE_ID));

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

    private void updateDetails(long movieId) {
        FetchTrailersTask trailersTask = new FetchTrailersTask(getActivity(), mTrailerAdapter);
        trailersTask.execute(Long.toString(movieId));
        FetchReviewsTask reviewsTask = new FetchReviewsTask(getActivity(), mReviewAdapter);
        reviewsTask.execute(Long.toString(movieId));
    }

    @OnCheckedChanged(R.id.detail_movie_favorite_check)
    public void FavoriteCheckedChanged(CompoundButton compoundButton, boolean b) {
        ContentValues updatedValues = new ContentValues(1);
        updatedValues.put(MovieEntry.COLUMN_FAVORITE, b ? 1 : 0);
        getActivity().getContentResolver()
                .update(MovieEntry.CONTENT_URI, updatedValues, MovieEntry._ID + " = ?",
                        new String[]{Long.toString(ContentUris.parseId(mUri))});
    }
}
