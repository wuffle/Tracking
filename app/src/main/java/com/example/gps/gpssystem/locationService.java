package com.example.gps.gpssystem;


import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.LocationListener;

import java.io.Serializable;

//Keep track of locations while app is backgrounded
public class locationService extends Service implements LocationListener {
    private Journey journ;

    @Override
    public void onCreate() {
        journ=new Journey();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        journ.addPosition(location);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Intent intent = new Intent("UpdateLocation");
        intent.putExtra("Location",journ);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
