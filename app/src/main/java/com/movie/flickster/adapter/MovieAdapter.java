package com.movie.flickster.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.movie.flickster.PosterFragment;
import com.movie.flickster.R;
import com.movie.flickster.Utility;
import com.squareup.picasso.Picasso;

/**
 * Created by yoh268 on 7/12/2016.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    CursorAdapter mCursorAdapter;
    Context mContext;

    OnItemClickListener mOnItemClickListener;

    public  MovieAdapter(Context context, Cursor cursor) {
        mContext = context;

        mCursorAdapter = new CursorAdapter(mContext, cursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
                View view = LayoutInflater.from(context).inflate(R.layout.poster_item, viewGroup, false);
                return view;
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
        };
    }


    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Passing the inflater job to the cursor-adapter
        View view = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        mCursorAdapter.getCursor().moveToPosition(position);
        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
        holder.posterView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    public Cursor getItemAt(int position) {
        return (Cursor) mCursorAdapter.getItem(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView posterView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.posterView = (ImageView) itemView.findViewById(R.id.poster_view);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

}