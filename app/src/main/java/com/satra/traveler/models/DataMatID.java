package com.satra.traveler.models;

/**
 * Created by Steve Jeff on 31/08/2016.
 */
public class DataMatID {
    private String id;
    private String code;
    private String msisdn;
    private String username;
    private String emergency_primary;
    private String emergency_secondary;
    private String created_at;
    private String updated_at;
    private String time_string;
    private String deleted_at;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmergency_primary() {
        return emergency_primary;
    }

    public void setEmergency_primary(String emergency_primary) {
        this.emergency_primary = emergency_primary;
    }

    public String getEmergency_secondary() {
        return emergency_secondary;
    }

    public void setEmergency_secondary(String emergency_secondary) {
        this.emergency_secondary = emergency_secondary;
    }

    public String getTime_string() {
        return time_string;
    }

    public void setTime_string(String time_string) {
        this.time_string = time_string;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(String deleted_at) {
        this.deleted_at = deleted_at;
    }
}
