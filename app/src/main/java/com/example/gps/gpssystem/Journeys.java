package com.example.gps.gpssystem;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Journeys {
    private ArrayList<Journey> journ;

    Journeys(){
        journ=new ArrayList<>();
    }

    public void addJourney(Journey journey){

        journey.setName(Integer.toString(journ.size()));//Set name of journey to index in string
        //add journey tolist
        journ.add(journey);
    }
    public ArrayList<Journey> getJourney(){
        return journ;
    }

    public void setList(ArrayList<Journey> al){
        journ=al;//Set list to input
    }
}
