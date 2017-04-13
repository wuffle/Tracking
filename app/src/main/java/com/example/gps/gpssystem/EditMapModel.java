package com.example.gps.gpssystem;

import android.location.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class EditMapModel extends MapsActivityController{
    private List<Polyline> lines;
    public Marker mCurrentLocationMarker,StartMarker;
    private Location  mCurrentLocation;

    EditMapModel(){
        lines=new ArrayList<>();
    }

    public void drawRoute(Location start, Location end){
        //Draw line between start and end coords
        Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(getCoords(start),getCoords(end)));
        //Keep track of the lines added
        lines.add(polyline1);
    }

    //Two different markers to aid the viewer
    public void CurrentLoc(LatLng spot){
        //Remove old marker
        if (mCurrentLocationMarker != null) {
            mCurrentLocationMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(spot);
        markerOptions.title("Start");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrentLocationMarker = mMap.addMarker(markerOptions);
        //Move camera to new location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(spot,11));
    }
    //Place marker at start of journey
    public void startLocation(LatLng spot){
        if (StartMarker != null) {
            StartMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(spot);
        markerOptions.title("Start");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
        StartMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(spot,11));
    }

    //Remove markers from map
    public void cleanMap(){
        //Remove start location marker
        if(mCurrentLocationMarker!=null) {
            mCurrentLocationMarker.remove();
        }
        if (StartMarker != null) {
            StartMarker.remove();
        }
        //Iterate through list of polylines and remove them from the amp
        for(int i=0;i< lines.size();i++){
            lines.get(i).remove();
        }
        //clear list of poly lines
        lines.clear();
    }

    public void loadRouteFromMemory(ArrayList<Location> route){
        cleanMap();//Clean map incase of any markers or polylines
        ArrayList<Location> loc=route;//Temp store for the array
        //Iterate through each positon stored in the journey selected
        for(int i=0;i<loc.size();i++) {
            if(i==0){
                startLocation(getCoords(loc.get(0)));//If first position mark as starting point
            }else {
                drawRoute(loc.get(i-1),loc.get(i));//Else pass the current and previous locations to drawer
            }
        }
        CurrentLoc(getCoords(loc.get(route.size()-1)));
    }

}
