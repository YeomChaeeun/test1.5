package com.massivcode.choisfamily.tracking.models;

import java.io.Serializable;

/**
 * Created by massivcode@gmail.com on 2017. 1. 5. 11:17
 */

public class TrackingHistory implements Serializable {
    private long elapsedTime;
    private double averageSpeed;
    private double distance;
    private String pathJson;
    private long date;

    public TrackingHistory(long elapsedTime, double averageSpeed, double distance, String pathJson, long date) {
        this.elapsedTime = elapsedTime;
        this.averageSpeed = averageSpeed;
        this.distance = distance;
        this.pathJson = pathJson;
        this.date = date;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getPathJson() {
        return pathJson;
    }

    public void setPathJson(String pathJson) {
        this.pathJson = pathJson;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TrackingHistory{");
        sb.append("elapsedTime=").append(elapsedTime);
        sb.append(", averageSpeed=").append(averageSpeed);
        sb.append(", distance=").append(distance);
        sb.append(", pathJson='").append(pathJson).append('\'');
        sb.append(", date=").append(date);
        sb.append('}');
        return sb.toString();
    }
}
