package com.satra.traveler.models;

/**
 * Created by Larry Akah on 3/11/17.
 */

public class Comments {

    String author;
    String comment;
    long dateTime;

    public Comments() {
    }

    public Comments(String author, String comment, long postTime) {
        this.author = author;
        this.comment = comment;
        this.dateTime = postTime;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }
}
