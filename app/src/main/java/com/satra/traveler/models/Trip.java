package com.satra.traveler.models;

import com.orm.SugarRecord;
import com.orm.dsl.NotNull;
import com.orm.dsl.Unique;

import java.io.Serializable;

/**
 * Created by Larry Akah on 6/14/16.
 * Class represents a model for a trip/journey/voyage. Stores attributes and is modifyable
 */
public class Trip extends SugarRecord implements Serializable{

    private String date_end; //date trip is ended or cancelled by user
    @NotNull
    public int status; //indicates the state of teh current trip [0-on going/active, 1-finished, 2-cancelled]
    @Unique
    private long tid; //travel instance id
    @NotNull
    private String bus_immatriculation;
    @NotNull
    private String agency_name;
    private String contact_name; //name of relative contact
    private String contact_number;
    @NotNull
    private String date_start; //date trip is registered and assumed to begin
    @NotNull
    private String departure; //town of departure
    @NotNull
    private String destination; //town of destination

    private double destinationLatitude;

    private double destinationLongitude;

    private double departureLatitude;

    private double departureLongitude;

    User user;

    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public double getDepartureLatitude() {
        return departureLatitude;
    }

    public void setDepartureLatitude(double departureLatitude) {
        this.departureLatitude = departureLatitude;
    }

    public double getDepartureLongitude() {
        return departureLongitude;
    }

    public void setDepartureLongitude(double departureLongitude) {
        this.departureLongitude = departureLongitude;
    }

    public Trip() {
    }

    public long getTid() {
        return tid;
    }

    public void setId(long id) {
        this.tid = id;
    }

    public String getBus_immatriculation() {
        return bus_immatriculation;
    }

    public void setBus_immatriculation(String bus_immatriculation) {
        this.bus_immatriculation = bus_immatriculation;
    }

    public String getAgency_name() {
        return agency_name;
    }

    public void setAgency_name(String agency_name) {
        this.agency_name = agency_name;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public String getContact_number() {
        return contact_number;
    }

    public void setContact_number(String contact_number) {
        this.contact_number = contact_number;
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

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Trip ID -> "+tid+"\nAgency -> "+agency_name+"\nDeparture -> "+departure
                +"\nDestination -> "+destination+"\nDeparture Time -> "+date_start+"\nDate end -> "+date_end
                +"\nStatus -> "+status;
    }
}
