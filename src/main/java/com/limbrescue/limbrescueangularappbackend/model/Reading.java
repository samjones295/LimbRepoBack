package com.limbrescue.limbrescueangularappbackend.model;

import java.sql.Date;

public class Reading {
    //Fields
    private int id;
    private int userid;
    private String date_created;
    private String laterality;
    private String comments;
    //Constructors
    public Reading() {

    }
    public Reading(int id, int userid, String date_created, String laterality, String comments) {
        this.id = id;
        this.userid = userid;
        this.date_created = date_created;
        this.laterality = laterality;
        this.comments = comments;
    }
    //Getters
    public int getId() {
        return id;
    }
    public int getUserId() {
        return userid;
    }
    public String getDate_created() {
        return date_created;
    }
    public String getLaterality() { return laterality; }
    public String getComments() {
        return comments;
    }

    //Setters
    public void setId(int id) {
        this.id = id;
    }
    public void setUserId(int userid) {
        this.userid = userid;
    }
    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }
    public void setLaterality(String laterality) { this.laterality = laterality; }
    public void setComments(String comments) {
        this.comments = comments;
    }
    //ToString
    @Override
    public String toString() {
        return "{" +
                "id: " + id + ", " +
                "userid: " + userid + ", " +
                "date_created: " + date_created + ", " +
                "laterality: " + laterality + ", " +
                "comments: " + comments + ", " +
                "}";
    }
}