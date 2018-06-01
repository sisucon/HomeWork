package com.example.sisucon.homework.weatherDB;

import org.litepal.crud.DataSupport;

public class Country extends DataSupport {
    private int id;
    private String countryName;
    private String weatherID;
    private int cityID;

    public Country(String countryName, String weatherID, int cityID) {
        this.countryName = countryName;
        this.weatherID = weatherID;
        this.cityID = cityID;
    }

    public int getid() {
        return id;
    }

    public void setid(int countryID) {
        this.id = countryID;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getWeatherID() {
        return weatherID;
    }

    public void setWeatherID(String weatherID) {
        this.weatherID = weatherID;
    }

    public int getCityID() {
        return cityID;
    }

    public void setCityID(int cityID) {
        this.cityID = cityID;
    }
}
