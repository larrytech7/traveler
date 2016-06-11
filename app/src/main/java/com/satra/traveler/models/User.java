package com.satra.traveler.models;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by Larry Akah on 6/11/16.
 */

public class User extends SugarRecord{

    @Unique
    private long uid;
    private String username;
    private String userphone;
    private String password;
    private String date_registered;

    public User() {
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long id) {
        this.uid = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserphone() {
        return userphone;
    }

    public void setUserphone(String userphone) {
        this.userphone = userphone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDate_registered() {
        return date_registered;
    }

    public void setDate_registered(String date_registered) {
        this.date_registered = date_registered;
    }
}
