package com.example.zj.cool.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    public Basic basic;
    public Update update;
    public String status;
    public Now now;
    // public AQI aqi;
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;

    public List<Lifestyle> lifestyle;

}
