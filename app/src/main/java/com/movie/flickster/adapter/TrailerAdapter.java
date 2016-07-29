package com.movie.flickster.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.movie.flickster.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yoh268 on 7/11/2016.
 */
public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.ViewHolder> {

    Context mContext;
    List<Trailer> mItem;

    OnItemSelectListener mOnItemSelectListener;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.trailer_thumb_view) ImageView mImageView;
        @BindView(R.id.trailer_name_text_view) TextView mNameView;

        OnItemSelectListener mSelectListener;

        public ViewHolder(View itemView, OnItemSelectListener selectListener) {
            super(itemView);
            mSelectListener = selectListener;
            itemView.setOnClickListener(this);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View view) {
            mSelectListener.onItemSelect(view, getLayoutPosition());
        }
    }

    public TrailerAdapter(Context context, List<Trailer> item) {
        mContext = context;
        mItem = item;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.movie_trailer, parent, false);
        mOnItemSelectListener = new OnItemSelectListener() {
            @Override
            public void onItemSelect(View view, int position) {
                String youTubeUrl = mItem.get(position).buildYouTubeUrl();
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(youTubeUrl));
                mContext.startActivity(intent);
            }
        };
        return new ViewHolder(view, mOnItemSelectListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Picasso
                .with(mContext)
                .load(mItem.get(position).buildYouTubeImgUrl())
                .into(holder.mImageView);
        holder.mNameView.setText(mItem.get(position).getName());
    }

    @Override
    public int getItemCount() {
        if (null == mItem) return 0;
        return mItem.size();
    }

    public void setItem(List<Trailer> item) {
        if (null != mItem) {
            mItem.clear();
            mItem.addAll(item);
        }
        else mItem = item;

        this.notifyDataSetChanged();
    }

    public interface OnItemSelectListener {
        void onItemSelect(View view, int position);
    }
}
