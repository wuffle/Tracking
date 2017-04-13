package com.example.gps.gpssystem;

import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;


public class ToastsModel {
    MapsActivityController mA;

    //Seperate class to display toasts to seperate views from model and controllers
    ToastsModel(MapsActivityController ma){
        mA=ma;
    }

    //Show time of journey
    public void postTime(String time){
        Toast toast = Toast.makeText(mA,time,Toast.LENGTH_LONG);
        toast.show();
    }
    public void failedConnection(){
        Toast toast = Toast.makeText(mA, "failed to connect",Toast.LENGTH_SHORT);
        toast.show();
    }
    public void NullDB(){
        Toast toast = Toast.makeText(mA, "Empty Database",Toast.LENGTH_SHORT);
        toast.show();
    }

}
