package com.movie.flickster;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by yoh268 on 7/11/2016.
 */
public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerHolder> {

    Context mContext;
    List<String> mItem;

    public static class TrailerHolder extends RecyclerView.ViewHolder {
        protected ImageView mImageView;

        public TrailerHolder(View itemView) {
            super(itemView);
            this.mImageView = (ImageView) itemView.findViewById(R.id.poster_view);
        }
    }

    public TrailerAdapter(Context context, List<String> item) {
        mContext = context;
        mItem = item;
    }

    public TrailerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.poster_item, parent, false);
        return new TrailerHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerHolder holder, int position) {
        Picasso
                .with(mContext)
                .load(Utility.buildYouTubeImgUrl(mItem.get(position)))
                .error(R.drawable.ic_error_black_24dp)
                .placeholder(R.drawable.progress_animation)
                .into(holder.mImageView);
    }

    @Override
    public int getItemCount() {
        return mItem.size();
    }
}
