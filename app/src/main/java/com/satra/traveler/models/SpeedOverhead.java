package com.satra.traveler.models;

import com.orm.SugarRecord;
import com.orm.dsl.NotNull;
import com.orm.dsl.Unique;

import java.io.Serializable;

/**
 * Created by Steve tueno on 11/10/16.
 * Class represents a model for a speed overhead. Stores attributes and is modifyable
 */
public class SpeedOverhead extends SugarRecord implements Serializable{

    @Unique
    private long id; //travel instance id
    @NotNull
    private String date_start; //date de debut depassement
    @NotNull
    private String date_end; //date de fin depassement

    private double latitude_start;

    private double longitude_start;

    private double latitude_end;

    private double longitude_end;

    private double speed_start;

    private double speed_end;

    @NotNull
    private String tripid;


    public double getSpeed_start() {
        return speed_start;
    }

    public void setSpeed_start(double speed_start) {
        this.speed_start = speed_start;
    }

    public double getSpeed_end() {
        return speed_end;
    }

    public void setSpeed_end(double speed_end) {
        this.speed_end = speed_end;
    }

    public long getTid() {
        return id;
    }

    public void setTid(long tid) {
        this.id = tid;
    }

    public String getDate_start() {
        return date_start;
    }

    public void setDate_start(String date_start) {
        this.date_start = date_start;
    }

    public String getDate_end() {
        return date_end;
    }

    public void setDate_end(String date_end) {
        this.date_end = date_end;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude_start() {
        return latitude_start;
    }

    public void setLatitude_start(double latitude_start) {
        this.latitude_start = latitude_start;
    }

    public double getLongitude_start() {
        return longitude_start;
    }

    public void setLongitude_start(double longitude_start) {
        this.longitude_start = longitude_start;
    }

    public double getLatitude_end() {
        return latitude_end;
    }

    public void setLatitude_end(double latitude_end) {
        this.latitude_end = latitude_end;
    }

    public double getLongitude_end() {
        return longitude_end;
    }

    public void setLongitude_end(double longitude_end) {
        this.longitude_end = longitude_end;
    }

    public String getTripid() {
        return tripid;
    }

    public void setTripid(String tripid) {
        this.tripid = tripid;
    }
}
