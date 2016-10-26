package com.satra.traveler.models;

/**
 * Created by Steve Jeff on 26/08/2016.
 */
public class ResponsStatusMsgMeta {
    private Integer status;
    private String message;
    private DataMeta meta;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString(){
        return this.getStatus()+" : "+this.getMessage();
    }

    public DataMeta getMeta() {
        return meta;
    }

    public void setMeta(DataMeta meta) {
        this.meta = meta;
    }
}
