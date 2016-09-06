package com.satra.traveler.models;

/**
 * Created by Steve Jeff on 26/08/2016.
 */
public class ResponsStatusMsgData {
    private Integer status;
    private String message;
    private DataMatID[] data;

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

    public DataMatID[] getData() {
        return data;
    }

    public void setData(DataMatID[] data) {
        this.data = data;
    }
}
