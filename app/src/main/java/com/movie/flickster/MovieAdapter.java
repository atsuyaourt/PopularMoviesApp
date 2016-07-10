package com.movie.flickster;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by yoh268 on 7/10/2016.
 */
public class MovieAdapter extends CursorAdapter {

    public MovieAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String imageUrl = Utility.buildTMDBPosterUrl(cursor.getString(PosterFragment.COL_POSTER_PATH));
        Picasso
                .with(context)
                .load(imageUrl)
                .error(R.drawable.ic_error_black_24dp)
                .placeholder(R.drawable.progress_animation)
                .into((ImageView) view);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        return view;
    }
}
