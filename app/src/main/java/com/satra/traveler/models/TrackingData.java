package com.satra.traveler.models;

import com.orm.SugarRecord;

/**
 * Created by Larry Akah on 9/12/16.
 */
public class TrackingData extends SugarRecord{

    private long timestamp; //id of the data. Used to reprensent the timestamp for registering the data
    private String location; //text location
    private double longitude;
    private double latitude;
    private double speed;
    private String trackingMatricule;
    private double bearing; //represent altitude,orientation
    private double temperature; //temperature
    private String sender; //phone number of sending device

    public TrackingData() {
    }

    public String getTrackingMatricule() {
        return trackingMatricule;
    }

    public void setTrackingMatricule(String trackingMatricule) {
        this.trackingMatricule = trackingMatricule;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
