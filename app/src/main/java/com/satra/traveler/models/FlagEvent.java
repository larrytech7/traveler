package com.satra.traveler.models;

import com.orm.SugarRecord;

import java.io.Serializable;

/**
 * Created by Larry Akah on 7/7/17.
 */

public class FlagEvent extends SugarRecord implements Serializable {

    //private enum TYPE {ACCIDENT =1, BAD_ROAD =2, CAR_ISSUE = 3, TRAFFIC=4}
    private int  type ; //type of flag category as described in the enumeration above
    private String description;
    private String location;
    private long timeStamp;
    private String authorId;
    private String tripId;

    public FlagEvent() {
    }

    public FlagEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}
