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

import com.movie.flickster.adapter.PosterAdapter;
import com.movie.flickster.data.MovieContract;

/**
 * Fragment to load movie posters
 */
public class PosterFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    RecyclerView mPosterView;
    PosterAdapter mPosterAdapter;

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

        mPosterAdapter = new PosterAdapter(getActivity(), null);
        mPosterAdapter.setOnItemClickListener(new PosterAdapter.OnItemSelectListener() {
            @Override
            public void onItemSelect(View view, int position) {
                Cursor cursor = mPosterAdapter.getCursor();
                if (cursor != null) {
                    cursor.moveToPosition(position);
                    ((Callback) getActivity())
                            .onItemSelected(MovieContract.MovieEntry.buildMovieUri(
                                    cursor.getLong(PosterAdapter.COL_MOVIE_ID)
                            ));
                }
            }
        });

        mPosterView = (RecyclerView) rootView.findViewById(R.id.posters_recycler_view);
        mPosterView.setLayoutManager(new GridLayoutManager(getActivity(),
                getResources().getInteger(R.integer.span_count)));
        mPosterView.setAdapter(mPosterAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(PosterAdapter.MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Get the sort order preference
        String orderType = Utility.getPreferredFilterType(getActivity());

        Uri moviesUri = MovieContract.MovieEntry.buildMovieWithFilter(orderType);

        return new CursorLoader(getActivity(), moviesUri,
                PosterAdapter.MOVIE_COLUMNS, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPosterAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //mPosterView.setAdapter(null);
        mPosterAdapter.changeCursor(null);
    }

    void onFilterTypeChanged() {
        updateMovies();
        getLoaderManager().restartLoader(PosterAdapter.MOVIE_LOADER, null, this);
    }

    private void updateMovies() {
        FetchMoviesTask moviesTask = new FetchMoviesTask(getActivity());
        String filterType = Utility.getPreferredFilterType(getActivity());
        moviesTask.execute(filterType);
    }

}
