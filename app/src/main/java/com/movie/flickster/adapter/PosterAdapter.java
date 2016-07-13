package com.movie.flickster.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.movie.flickster.R;
import com.movie.flickster.Utility;
import com.movie.flickster.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by yoh268 on 7/12/2016.
 */
public class PosterAdapter extends CursorRecyclerViewAdapter<PosterAdapter.ViewHolder> {

    public static final int MOVIE_LOADER = 0;

    public static final String[] MOVIE_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH
    };

    public static final int COL_MOVIE_ID = 0;
    public static final int COL_POSTER_PATH = 1;

    Context mContext;
    OnItemSelectListener mOnItemSelectListener;

    public PosterAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.poster_item, parent, false);
        return new ViewHolder(view, mOnItemSelectListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        String imageUrl = Utility.buildTMDBPosterUrl(cursor.getString(COL_POSTER_PATH));
        Picasso
                .with(mContext)
                .load(imageUrl)
                .error(R.drawable.ic_error_black_24dp)
                .placeholder(R.drawable.progress_animation)
                .into(holder.posterView);
    }

    public void setOnItemClickListener(OnItemSelectListener onItemSelectListener) {
        mOnItemSelectListener = onItemSelectListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView posterView;

        OnItemSelectListener mSelectListener;

        public ViewHolder(View itemView, OnItemSelectListener selectListener) {
            super(itemView);
            mSelectListener = selectListener;
            itemView.setOnClickListener(this);
            posterView = (ImageView) itemView.findViewById(R.id.poster_view);

        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition();
            mSelectListener.onItemSelect(view, position);
        }

    }

    public interface OnItemSelectListener {
        void onItemSelect(View view, int position);
    }
}