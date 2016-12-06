package com.satra.traveler.models;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by Larry Akah on 9/6/16.
 */
public class Messages extends SugarRecord {

    @Unique
    String content; //message content needs to be unique to avoid duplicates or redundancies
    String sender; //matricule/phone of the person sending the message
    String author; //Main username of person writing the message
    String date; //date sent
    String imageUrl; //optional image accompanying messages
    long timestamp; // timestamp messaeg was sent (when user first pressed send to send the message)
    int sent; //1-sent, 0-not sent. default should be sent
    boolean source; //true - sent by this user, false - from other users or Traveler

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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }
}
