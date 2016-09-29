package com.satra.traveler.models;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by Larry Akah on 9/6/16.
 */
public class Messages extends SugarRecord {

    @Unique
    String content; //message content needs to be unique to avoid duplicates or redundancies
    String sender; //name/matricule/phone of the person sending the message
    String date; //date sent
    String imageUrl; //optional image accompanying messages
    int sent; //1-sent, 0-not sent. default should be sent

    public Messages() {
    }

    public int getSent() {
        return sent;
    }

    public void setSent(int sent) {
        this.sent = sent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
