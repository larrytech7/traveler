package com.satra.traveler.models;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by Larry Akah on 9/6/16.
 */
public class Messages extends SugarRecord {

    @Unique
    private
    String content; //message content needs to be unique to avoid duplicates or redundancies
    private String sender; //matricule of the person sending the message
    private String phonenumber; //phone number of person sending message
    private String author; //Main username of person writing the message
    private String date; //date sent
    private String imageUrl; //optional image accompanying messages
    private long timestamp; // timestamp message was sent (when user first pressed send to send the message)
    private int sent; //1-sent, 0-not sent. default should be sent
    private String category;

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

    public String getPhonenumber() {
        return phonenumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
}
