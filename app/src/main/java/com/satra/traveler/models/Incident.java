package com.satra.traveler.models;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by Larry Akah on 6/14/16.
 */
public class Incident extends SugarRecord {

    @Unique
    private String key;
    private String matricule;
    private String agency;
    private double speed;
    private double acc;
    private double acc_last; //previous acceleration value
    private double latitude;
    private double longitude;
    private long timestamp;
    private int type ; //type of incident if it can be specifically determined. 1-accident, 2-near-miss,  etc ...

    // orientation data in degree
    private float azimuth; //angle of rotation about the -z axis which represents the angle between the device's y axis and the magnetic north pole. When facing north, this angle is 0, when facing south, this angle is 180. Likewise, when facing east, this angle is 90, and when facing west, this angle is -90.
    private float pitch; //angle of rotation about the x axis which represents the angle between a plane parallel to the device's screen and a plane parallel to the ground
    private float roll;  // angle of rotation about the y axis which represents the angle between a plane perpendicular to the device's screen and a plane perpendicular to the ground


    public Incident() {
    }

    public float getAzimuth() {
        return azimuth;
    }

    public void setAzimuth(float azimuth) {
        this.azimuth = azimuth;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getAcc() {
        return acc;
    }

    public void setAcc(double acc) {
        this.acc = acc;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getAcc_last() {
        return acc_last;
    }

    public void setAcc_last(double acc_last) {
        this.acc_last = acc_last;
    }
}
