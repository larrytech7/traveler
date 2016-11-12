package com.satra.traveler.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.satra.traveler.R;
import com.satra.traveler.models.Messages;

import java.util.List;

/**
 * Created by Larry Akah on 9/6/16.
 */
public class MessagingAdapter extends RecyclerView.Adapter<MessagingAdapter.ViewHolder> {

    Context context;
    List<Messages> messagesList;

    public MessagingAdapter(Context context, List<Messages> messagesList) {
        this.context = context;
        this.messagesList = messagesList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_message_layout, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Messages message = getItem(position);
        holder.messageMatricule.setText(context.getString(R.string.author_title, message.getAuthor(), message.getSender()));
        holder.messageText.setText(message.getContent());
        holder.messageDate.setText(message.getDate());
        int status = message.getSent();
        if (status == 1){
            holder.messageStatusText.setText(context.getString(R.string.message_received));
            holder.messageStatusText.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.ic_done), null);
        }else{
            holder.messageStatusText.setText(context.getString(R.string.message_not_received));
            holder.messageStatusText.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getResources().getDrawable(R.drawable.ic_done_one), null);
        }
    }

    public Messages getItem(int position){
        return messagesList == null? null:messagesList.get(position);
    }

    @Override
    public int getItemCount() {
        return messagesList == null? 0: messagesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView messageText, messageMatricule, messageDate, messageStatusText;

        public ViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.messageContent);
            messageMatricule = (TextView) itemView.findViewById(R.id.messageMatricule);
            messageDate = (TextView) itemView.findViewById(R.id.messageDate);
            messageStatusText = (TextView) itemView.findViewById(R.id.messageStatusText);
        }
    }
}
