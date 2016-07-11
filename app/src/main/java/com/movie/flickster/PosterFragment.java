package com.movie.flickster;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.movie.flickster.adapter.MovieAdapter;
import com.movie.flickster.data.MovieContract;

/**
 * Fragment to load movie posters
 */
public class PosterFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MOVIE_LOADER = 0;

    private static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH
    };

    public static final int COL_MOVIE_ID = 0;
    public static final int COL_POSTER_PATH = 1;

    RecyclerView mPosterView;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dateUri);
    }

    public PosterFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_posters, container, false);

        mPosterView = (RecyclerView) rootView.findViewById(R.id.posters_recycler_view);
        mPosterView.setLayoutManager(new GridLayoutManager(getActivity(),
                getResources().getInteger(R.integer.span_count)));

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Get the sort order preference
        String orderType = Utility.getPreferredFilterType(getActivity());

        Uri moviesUri = MovieContract.MovieEntry.buildMovieWithFilter(orderType);

        return new CursorLoader(getActivity(), moviesUri,
                MOVIE_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        final MovieAdapter movieAdapter = new MovieAdapter(getActivity(), data);
        mPosterView.setAdapter(movieAdapter);
        movieAdapter.setOnItemClickListener(new MovieAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Cursor cursor = movieAdapter.getItemAt(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(MovieContract.MovieEntry.buildMovieUri(
                                    cursor.getLong(COL_MOVIE_ID)
                            ));
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPosterView.setAdapter(null);
    }

    void onFilterTypeChanged() {
        updateMovies();
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    private void updateMovies() {
        FetchMoviesTask moviesTask = new FetchMoviesTask(getActivity());
        String filterType = Utility.getPreferredFilterType(getActivity());
        moviesTask.execute(filterType);
    }

}
