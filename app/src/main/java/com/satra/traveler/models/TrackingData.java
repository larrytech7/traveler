package com.satra.traveler.models;

import com.orm.SugarRecord;

/**
 * Created by Larry Akah on 9/12/16.
 */
public class TrackingData extends SugarRecord{

    private long id;
    private String location;
    private double longitude;
    private double latitude;
    private double speed;
    private String trackingMatricule;

    public TrackingData() {
    }

    public String getTrackingMatricule() {
        return trackingMatricule;
    }

    public void setTrackingMatricule(String trackingMatricule) {
        this.trackingMatricule = trackingMatricule;
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
