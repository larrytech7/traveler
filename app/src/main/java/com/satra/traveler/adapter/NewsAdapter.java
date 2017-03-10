package com.satra.traveler.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;
import com.satra.traveler.R;
import com.satra.traveler.models.News;
import com.satra.traveler.utils.Tutility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Larry Akah on 3/10/17.
 */

public class NewsAdapter extends FirebaseRecyclerAdapter<News, NewsAdapter.ViewHolder> {

    private Context c;
    private int lastPosition = -1;
    /**
     * @param modelClass      Firebase will marshall the data at a location into an instance of a class that you provide
     * @param modelLayout     This is the layout used to represent a single item in the list. You will be responsible for populating an
     *                        instance of the corresponding view with the data from an instance of modelClass.
     * @param viewHolderClass The class that hold references to all sub-views in an instance modelLayout.
     * @param ref             The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                        combination of {@code limit()}, {@code startAt()}, and {@code endAt()}.
     */
    public NewsAdapter(Context context, Class<News> modelClass, int modelLayout,
                       Class<ViewHolder> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.c = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    protected void populateViewHolder(ViewHolder viewHolder, News model, int position) {
        //populate views
        viewHolder.newsSourceTextView.setText(model.getNewsSource());
        viewHolder.newsTitle.setText(model.getNewsTitle());
        viewHolder.newsTimeTextView.setText(Tutility.getMicroTimeString(c, model.getNewsTimeStamp(), System.currentTimeMillis(),
                new SimpleDateFormat("dd/MM/yyyy H:mm:s a", Locale.US).format(new Date(model.getNewsTimeStamp()))));
        if (model.getNewsMultimediaLink() != null)
            Glide.with(c)
                    .load(Uri.parse(model.getNewsMultimediaLink()))
                    .placeholder(R.drawable.loading_drawable)
                    .crossFade()
                    .into(viewHolder.newsImageView);
        setAnimation(viewHolder.newsCard, position);
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    /**
     * Appply simple animation to the newscard
     * @param viewToAnimate card layout to apply animation upon
     * @param position position in list of the model being animated
     */
    private void setAnimation(View viewToAnimate, int position){
        if (position > lastPosition){
            Animation animation = AnimationUtils.loadAnimation(c, R.anim.fab_slide_in_from_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final CardView newsCard;
        final ImageView newsImageView;
        final TextView newsTitle, newsTimeTextView, newsSourceTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            newsCard = (CardView) itemView.findViewById(R.id.newsItemCardView);
            newsImageView = (ImageView) itemView.findViewById(R.id.newsImagePreview);
            newsTimeTextView = (TextView) itemView.findViewById(R.id.newsTimestampTextView);
            newsTitle = (TextView) itemView.findViewById(R.id.newsTitleTextView);
            newsSourceTextView = (TextView) itemView.findViewById(R.id.newsSourceTextView);
            //Add onclick listener to cardview layout widget
            newsCard.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //handle click
            Log.d("NEWS_VIEWHOLDER", "Clicked a news item");
        }
    }
}
