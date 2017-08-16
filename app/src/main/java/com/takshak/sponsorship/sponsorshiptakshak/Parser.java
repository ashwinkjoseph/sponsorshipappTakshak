package com.takshak.sponsorship.sponsorshiptakshak;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by Ashwin Joseph on 15-08-2017.
 */

public class Parser {
    private String _id;
    private String companyName;
    private LatLng2 latlng;
    private int __v;
    public String get_id(){
        return this._id;
    }
    public void set_id(String _id){
        this._id = _id;
    }
    public String getcompanyName(){
        return companyName;
    }
    public void setcompanyName(String companyName){
        this.companyName = companyName;
    }
    public LatLng2 getlatlng(){
        return latlng;
    }
    public void setlatlng(String latlng) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructType(LatLng2.class);
        this.latlng = mapper.readValue(latlng, type);
    }
    public int get_v(){
        return this.__v;
    }
    public void set__v(int _v){
        this.__v = _v;
    }
}