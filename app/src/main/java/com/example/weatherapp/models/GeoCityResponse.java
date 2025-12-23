package com.example.weatherapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeoCityResponse {

    @SerializedName("code")
    public String code;

    @SerializedName("location")
    public List<Location> location;

    public static class Location {

        @SerializedName("name")
        public String name;

        @SerializedName("id")
        public String id;

        @SerializedName("lat")
        public String lat;

        @SerializedName("lon")
        public String lon;

        @SerializedName("adm2")
        public String adm2;

        @SerializedName("adm1")
        public String adm1;

        @SerializedName("country")
        public String country;
    }
}
