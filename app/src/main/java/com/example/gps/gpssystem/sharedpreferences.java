package com.example.gps.gpssystem;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;


public class sharedpreferences extends MapsActivityController{
    //class to handle the persistant storage of journeys
    public  static final String MyPREFERENCES ="Journeys";

    //Add to file using shared preferences
    public void addToDB(ArrayList<Journey> journey,Context cxt){
        Gson gson = new Gson();
        SharedPreferences.Editor  pref = cxt.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE).edit();//Save to Journeys preference file

        String json = gson.toJson(journey);//Convert journey arraylist to string
        pref.putString("Journey", json);//put array list as a string into file
        pref.apply();
    }

    //Get journeys from shared pref
    public ArrayList<Journey> getFromDB(Context cxt){
        Gson gson = new Gson();

        SharedPreferences pref = cxt.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String json = pref.getString("Journey",null);
        //Return journey array
        return gson.fromJson(json,new TypeToken<List<Journey>>(){}.getType());
    }

}
