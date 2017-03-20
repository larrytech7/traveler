package com.satra.traveler.models;

import com.orm.SugarRecord;

/**
 * Created by Larry Akah on 3/6/17.
 * Class represents Model for quantifying rewards for the user using the app based on the given criteria fields
 */

public class Rewards extends SugarRecord {

    int appShares; //frequency of app shares via the app
    int appTravels; //frequency of travels completed on the app
    int appComments; //frequency of comments
    int appImages; //number of images sent via app;
    int appOfflineGuides; //frequency of opening and completing the application offline guides

    public Rewards() {
    }

    public int getAppShares() {
        return appShares;
    }

    public void setAppShares(int appShares) {
        this.appShares = this.appShares + appShares;
    }

    public int getAppTravels() {
        return appTravels;
    }

    public void setAppTravels(int appTravels) {
        this.appTravels = this.appTravels + appTravels;
    }

    public int getAppComments() {
        return appComments;
    }

    public void setAppComments(int appComments) {
        this.appComments = this.appComments + appComments;
    }

    public int getAppImages() {
        return appImages;
    }

    public void setAppImages(int appImages) {
        this.appImages = this.appImages + appImages;
    }

    public int getAppOfflineGuides() {
        return appOfflineGuides;
    }

    public void setAppOfflineGuides(int appOfflineGuides) {
        this.appOfflineGuides = this.appOfflineGuides + appOfflineGuides;
    }

    public int getPointsAccumulated(){
        return appShares + appComments + appTravels + appImages + appOfflineGuides ;
    }
}
