package com.satra.traveler.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.satra.traveler.R;
import com.satra.traveler.models.Messages;
import com.satra.traveler.models.User;
import com.satra.traveler.utils.Tutility;

import java.util.List;

/**
 * Created by Larry Akah on 9/6/16.
 */
public class MessagingAdapter extends FirebaseRecyclerAdapter<Messages, MessagingAdapter.ViewHolder> {

    private Context context;
    private List<Messages> messagesList;
    private User muser;
    private final int incoming_viewType = 0;
    private final int outgoing_viewType = 1;

    public MessagingAdapter(Class<Messages> modelClass, int modelLayout,
                            Class<ViewHolder> viewHolderClass, DatabaseReference ref,
                            List<Messages> messagesList, User me, Context ctx) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        setHasStableIds(true);
        this.context = ctx;
        this.messagesList = messagesList;
        this.muser = me;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case incoming_viewType:
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_layout, parent, false));
            case outgoing_viewType:
                return new ViewHolder((LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_layout_outgoing, parent, false)));
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public int getItemViewType(int position) {
        Messages m = getItem(position);
        if (muser.getUserphone().equalsIgnoreCase(m.getPhonenumber()))
            return outgoing_viewType;
        return incoming_viewType;
    }


    @Override
    public void cleanup() {
        super.cleanup();
    }

    @Override
    protected void populateViewHolder(ViewHolder holder, Messages model, int position) {
       // if (model.getPhonenumber().equalsIgnoreCase(muser.getUserphone())){
        //    populateView(new ViewHolder(), model);
       // }else{
            populateView(holder,model);
       // }
    }

    private void populateView(ViewHolder holder, Messages model){
        holder.messageMatricule.setText(context.getString(R.string.author_title, model.getAuthor(), model.getSender()));
        holder.messageText.setText(model.getContent());
        holder.messageDate.setText(Tutility.getTimeDifference(context, model.getTimestamp(), System.nanoTime(), model.getDate()));
        int status = model.getSent();
        if (status == 1){
            holder.messageStatusText.setText(context.getString(R.string.message_received));
            holder.messageStatusText.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.ic_done), null);
        }else{
            holder.messageStatusText.setText(context.getString(R.string.message_not_received));
            holder.messageStatusText.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.ic_done_one), null);
        }
        if (model.getImageUrl() != null && !model.getImageUrl().isEmpty())
            Glide.with(context)
                    .load(Uri.parse(model.getImageUrl()))
                    .placeholder(R.drawable.loading)
                    .crossFade()
                    .into(holder.mediaImage);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView messageText, messageMatricule, messageDate, messageStatusText;
        ImageView mediaImage;

        ViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.messageContent);
            messageMatricule = (TextView) itemView.findViewById(R.id.messageMatricule);
            messageDate = (TextView) itemView.findViewById(R.id.messageDate);
            messageStatusText = (TextView) itemView.findViewById(R.id.messageStatusText);
            mediaImage = (ImageView) itemView.findViewById(R.id.mediaImageView);
        }
    }
}
