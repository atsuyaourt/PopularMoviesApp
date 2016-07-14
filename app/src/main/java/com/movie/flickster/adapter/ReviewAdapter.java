package com.movie.flickster.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.movie.flickster.R;

import java.util.List;

/**
 * Created by yoh268 on 7/14/2016.
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder>{
    Context mContext;
    List<Review> mItem;

    OnItemSelectListener mOnItemSelectListener;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mAuthorView;
        TextView mContentView;

        OnItemSelectListener mSelectListener;

        public ViewHolder(View itemView, OnItemSelectListener selectListener) {
            super(itemView);
            mSelectListener = selectListener;
            itemView.setOnClickListener(this);
            this.mAuthorView = (TextView) itemView.findViewById(R.id.review_author_text_view);
            this.mContentView = (TextView) itemView.findViewById(R.id.review_content_text_view);
        }

        @Override
        public void onClick(View view) {
            mSelectListener.onItemSelect(view, getLayoutPosition());
        }
    }

    public ReviewAdapter(Context context, List<Review> item) {
        mContext = context;
        mItem = item;
    }

    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.movie_review, parent, false);
        mOnItemSelectListener = new OnItemSelectListener() {
            @Override
            public void onItemSelect(View view, int position) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(mItem.get(position).getUrl()));
                mContext.startActivity(intent);
            }
        };
        return new ViewHolder(view, mOnItemSelectListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mAuthorView.setText(mItem.get(position).getAuthor());
        holder.mContentView.setText(mItem.get(position).getPreviewContent());
    }

    @Override
    public int getItemCount() {
        if (null == mItem) return 0;
        return mItem.size();
    }

    public void setItem(List<Review> item) {
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
