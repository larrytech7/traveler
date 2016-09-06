package com.satra.traveler.models;

import com.orm.SugarRecord;

/**
 * Created by Larry Akah on 9/6/16.
 */
public class Messages extends SugarRecord {

    String content; //message content
    String sender; //name/matricule/phone of the person sending the message
    String date; //date sent

    public Messages() {
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
}
