package com.satra.traveler.models;

/**
 * Created by Steve Jeff on 31/08/2016.
 */
public class DataMeta {
   private Integer code;
    private String message;
    private Integer matricule_id;
    private Double speed;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getMatricule_id() {
        return matricule_id;
    }

    public void setMatricule_id(Integer matricule_id) {
        this.matricule_id = matricule_id;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }
}
