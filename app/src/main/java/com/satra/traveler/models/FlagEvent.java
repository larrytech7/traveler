package com.satra.traveler.models;

import com.orm.SugarRecord;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by Larry Akah on 7/7/17.
 */

public class FlagEvent extends SugarRecord implements Serializable {

    //private enum TYPE {ACCIDENT =1, BAD_ROAD =2, CAR_ISSUE = 3, TRAFFIC=4}
    private int  type ; //type of flag category as described in the enumeration above
    private String description; //description of the flag event
    private String location; //named location
    private long timeStamp; //time of flagging
    private String authorId; //user id making the flag
    private String tripId; //
    private String country; //resident country
    private double locLatitude; //latitude coordinate of location
    private double locLongitude; //longitude coordinate of location

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

    public String getCountry() {
        return country;
    }

    public void setCountry(@NotNull String country) {
        this.country = country;
    }

    public double getLocLatitude() {
        return locLatitude;
    }

    public void setLocLatitude(double locLatitude) {
        this.locLatitude = locLatitude;
    }

    public double getLocLongitude() {
        return locLongitude;
    }

    public void setLocLongitude(double locLongitude) {
        this.locLongitude = locLongitude;
    }
}
