package com.example.administrator.coolweather.gson;


import com.google.gson.annotations.SerializedName;

public class Basic {

    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;

    public UpDate upDate;

    public class UpDate{
        @SerializedName("loc")
        public String updataTime;
    }
}
