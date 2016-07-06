package com.movie.flickster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * An adapter that uses Picasso to load images
 */
public class ImageGridViewAdapter extends ArrayAdapter {
    private Context context;
    private LayoutInflater inflater;

    private List<String> imageUrls;

    public ImageGridViewAdapter(Context context, List<String> imageUrls) {
        super(context, R.layout.grid_item, imageUrls);

        this.context = context;
        this.imageUrls = imageUrls;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
        }

        Picasso
                .with(context)
                .load(imageUrls.get(position))
                .error(R.drawable.ic_error_black_24dp)
                .placeholder(R.drawable.progress_animation)
                .into((ImageView) convertView);

        return convertView;
    }
}