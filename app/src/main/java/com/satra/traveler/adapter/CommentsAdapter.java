package com.satra.traveler.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;
import com.satra.traveler.R;
import com.satra.traveler.models.Comments;
import com.satra.traveler.utils.Tutility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Larry Akah on 3/11/17.
 */

public class CommentsAdapter extends FirebaseRecyclerAdapter<Comments, CommentsAdapter.ViewHolder> {

    private int lastPosition = -1;
    private Context c;

    /**
     * @param modelClass      Firebase will marshall the data at a location into an instance of a class that you provide
     * @param modelLayout     This is the layout used to represent a single item in the list. You will be responsible for populating an
     *                        instance of the corresponding view with the data from an instance of modelClass.
     * @param viewHolderClass The class that hold references to all sub-views in an instance modelLayout.
     * @param ref             The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                        combination of {@code limit()}, {@code startAt()}, and {@code endAt()}.
     */
    public CommentsAdapter(Context context, Class<Comments> modelClass, int modelLayout, Class<ViewHolder> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        this.c = context;
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    @Override
    protected void populateViewHolder(ViewHolder viewHolder, Comments model, int position) {
        //populate comments
        viewHolder.authorTextView.setText(model.getAuthor());
        viewHolder.messageTextView.setText(model.getComment());
        viewHolder.dateTextView.setText(Tutility.getMicroTimeString(c, model.getDateTime(),
                System.currentTimeMillis(),
                new SimpleDateFormat("dd/MM/yyyy H:mm:s a", Locale.US).format(new Date(model.getDateTime()))));
        setAnimation((View) viewHolder.authorTextView.getParent(), position);
    }

    /**
     * Apply simple animation to the parent layout
     * @param viewToAnimate layout to apply animation upon
     * @param position position in list of the model being animated
     */
    private void setAnimation(View viewToAnimate, int position){
        if (position > lastPosition){
            Animation animation = AnimationUtils.loadAnimation(c, R.anim.fab_slide_in_from_right);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView authorTextView, messageTextView, dateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            messageTextView = (TextView) itemView.findViewById(R.id.messageContent);
            authorTextView = (TextView) itemView.findViewById(R.id.messageMatricule);
            dateTextView = (TextView) itemView.findViewById(R.id.messageDate);
        }
    }
}
