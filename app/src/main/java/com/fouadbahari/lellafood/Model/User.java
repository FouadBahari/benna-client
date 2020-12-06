package com.fouadbahari.lellafood.Model;

import com.google.firebase.database.DatabaseReference;

public class User {

    private String name,phone,address,uid;
    private double lat,lng;

    public User() {
    }

    public User( String name, String phone, String address,String uid) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.uid =uid;
    }

    public User(String phone, String uid) {
        this.phone = phone;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
