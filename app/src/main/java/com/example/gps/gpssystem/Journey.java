package com.example.gps.gpssystem;


import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

public class Journey implements Parcelable{

    private ArrayList<Location> positions;
    private String tStart,tEnd;
    private String name;
    private long eHour,eMinutes,eSeconds,
    sHour,sMinute,sSeconds;

    Journey(){
        positions=new ArrayList<Location>();
    }

    protected Journey(Parcel in) {
        positions = in.createTypedArrayList(Location.CREATOR);
        tStart = in.readString();
        tEnd = in.readString();
        name = in.readString();
        eHour = in.readLong();
        eMinutes = in.readLong();
        eSeconds = in.readLong();
        sHour = in.readLong();
        sMinute = in.readLong();
        sSeconds = in.readLong();
    }

    public static final Creator<Journey> CREATOR = new Creator<Journey>() {
        @Override
        public Journey createFromParcel(Parcel in) {
            return new Journey(in);
        }

        @Override
        public Journey[] newArray(int size) {
            return new Journey[size];
        }
    };

    public ArrayList<Location> getPositions(){
        return positions;
    }

    public void addPosition(Location loc){
        positions.add(loc);
    }

    public void setEndTime(){
         eHour=0;
         eMinutes=0;
         eSeconds=0;

        eHour= Calendar.getInstance().get(Calendar.HOUR_OF_DAY);//Get hour of the day
        eMinutes= Calendar.getInstance().get(Calendar.MINUTE);//Get minutes of the day
        eSeconds= Calendar.getInstance().get(Calendar.SECOND);//Get seconds of the day

        tEnd=Long.toString(eHour)+":"+Long.toString(eMinutes)+":"+Long.toString(eSeconds);
    }

    public void setName(String n){
        name=n;
    }

    //Returns the name for the list view
    public String toString(){return "Journey "+name;}
    //Get time started
    public String getStartTime(){
        return tStart;
    }
    //Get time end
    public String getEndTime(){
        return tEnd;
    }

    public void setStartTime(){
        sHour=0;
        sMinute=0;
        sSeconds=0;

        sHour= Calendar.getInstance().get(Calendar.HOUR_OF_DAY);//Get hour of the day
        sMinute= Calendar.getInstance().get(Calendar.MINUTE);//Get minutes of the day
        sSeconds= Calendar.getInstance().get(Calendar.SECOND);//Get seconds of the day

        tStart=Long.toString(sHour)+":"+Long.toString(sMinute)+":"+Long.toString(sSeconds);
    }
    //Return time taken in seconds
    public String getTimeTaken(){
        //Make any potential negative numbers positive
        long s=Math.abs(eSeconds-sSeconds);
        long h=Math.abs(eHour-sHour);
        long m=Math.abs(eMinutes-sMinute);

        return Long.toString(h)+":"+Long.toString(m)+":"+
               Long.toString(s);
         }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(positions);
        dest.writeString(tStart);
        dest.writeString(tEnd);
        dest.writeString(name);
        dest.writeLong(eHour);
        dest.writeLong(eMinutes);
        dest.writeLong(eSeconds);
        dest.writeLong(sHour);
        dest.writeLong(sMinute);
        dest.writeLong(sSeconds);
    }
}
