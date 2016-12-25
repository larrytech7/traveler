package com.satra.traveler.models;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.satra.traveler.R.id.bus_immatriculation;

/**
 * Created by Larry Akah on 6/11/16.
 */

public class User extends SugarRecord{

    @Unique
    private long uid; //unique user id.
    private String username; //pseudo
    private String useremail;
    private String userphone; //phone number
    private String password;
    private String date_registered;
    private String current_matricule; ///currently registered matricule
    private String emergency_primary; //first emergency contact number
    private String emergency_secondary; // secondary emergency contact number
    private long updated_at; // lastly updated date

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

    public String getCurrent_matricule() {
        return current_matricule;
    }

    public void setCurrent_matricule(String current_matricule) {
        this.current_matricule = current_matricule;
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

    public String getUseremail() {
        return useremail;
    }

    public List<Trip> getTrips(){
        return Trip.find(Trip.class, "user = ?", String.valueOf(getId()));
    }

    public void setUseremail(String useremail) {
        this.useremail = useremail;
    }

    public long getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(long updated_at) {
        this.updated_at = updated_at;
    }

    public Map getUserMap(){
        Map<String, Object> usermap = new HashMap<>();
        usermap.put("matricule", current_matricule);
        usermap.put("emergency_primary", emergency_primary);
        usermap.put("emergency_secondary", emergency_secondary);
        usermap.put("updated_at", updated_at );
        usermap.put("useremail", useremail);
        usermap.put("username", username);
        usermap.put("userphone", userphone);

        return usermap;
    }
}
