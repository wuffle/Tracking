package com.example.gps.gpssystem;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;

import com.google.android.gms.location.LocationListener;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;


public class MapsActivityController extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static GoogleMap mMap;
    private SupportMapFragment mapFrag;
    private GoogleApiClient mGoogleApiClient;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private ToastsModel toast;
    private EditMapModel eM;
    boolean tracking;
    private Journey journ;
    private Location mCurrentLocation, mPreviousLocation;
    private Journeys journeys;
    Button button1;
    private sharedpreferences pref;
    private boolean clicked = false;//Tracks if switch has been used
    private ArrayAdapter<Journey> adapter;
    private  Journey temp;//Temp locations
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        toast = new ToastsModel(this);
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
        tracking = true;
        //By default tracking is on so begin timer when journey is created
        journ = new Journey();
        journeys = new Journeys();
        eM = new EditMapModel();
        pref = new sharedpreferences();//Initialize preferences
        listClick();//Listen for listview click
        checkTracking();//Listen for switch schange
        journeyButton();//Listen for button press

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                journeys.getJourney());

        //Listen for service to send location data
        mReceiver = new BroadcastReceiver() {
            @Override
           public void onReceive(Context context, Intent intent) {
                temp= intent.getExtras().getParcelable("Location");
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mReceiver, new IntentFilter("UpdateLocation"));
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onPause() {
        if (!tracking) {
            finish();
        }
        //LocationServices.FusedLocationApi.removeLocationUpdates(
               // mGoogleApiClient, this);//Stop tracking
        //Run service in background to keep track of location
        startService(new Intent(this, locationService.class));
        super.onPause();
    }

    @Override
    public void onResume() {
        if (!tracking) {
            return;
        }
        if (mGoogleApiClient != null) {
            //Reconnect to google maps
            mGoogleApiClient.reconnect();
        }
      // stopService(new Intent(this, locationService.class));
        super.onResume();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //When connected to google maps get location and set map
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        //Set marker at current location
        //Update when location changes
        eM.startLocation(getCoords(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)));
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);//Set our current location
        journ.addPosition(mCurrentLocation);
        journ.setStartTime();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    //Whenever location changes
    @Override
    public void onLocationChanged(Location location) {
        //Check if tracking is enabled if not leave
        if (!tracking) {
            return;
        }
        mPreviousLocation = mCurrentLocation;
        //Store current location
        mCurrentLocation = location;
        //Set previous location to current if moved
        if (mPreviousLocation != null) {
            eM.drawRoute(mPreviousLocation, mCurrentLocation);
        }
        //Add current location to list for the journey
        journ.addPosition(mCurrentLocation);
        //Place current location marker
        eM.CurrentLoc(getCoords(location));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        toast.failedConnection();
    }

    //save location when activity pauses
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    //Get permisson if dont currently have
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivityController.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    //Check tracking switch for true or false
    public boolean checkTracking() {
        //find switch from included layout
        View includedLayout = findViewById(R.id.overlay);
        Switch s = (Switch) includedLayout.findViewById(R.id.tSwitch);

        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                //if switch is check true else false
                if (isChecked) {
                    //Resume position update if switch is true
                    tracking = true;
                    mCurrentLocation = null;
                    journ = new Journey();//Create new journey
                    onResume();
                } else {
                    //If switch is false dont update current position by pausing
                    //Add current journey to list of journeys and create new journey
                    tracking = false;
                    mPreviousLocation = null;
                    if (eM.StartMarker != null) {
                        eM.StartMarker.remove();//Remove start marker from map
                    }
                    if (mCurrentLocation != null) {
                        journeys.addJourney(journ);//Add journey to storage
                        journ.setEndTime();//Save time taken
                        toast.postTime("Time taken " + journ.getTimeTaken());//Show time taken toast
                        pref.addToDB(journeys.getJourney(), getApplicationContext());
                        adapter.notifyDataSetChanged();//Notify adapter that there is another journey
                    }
                    eM.cleanMap();//Clear google maps of lines.
                }

            }
        });
        return tracking;
    }

    public void journeyButton() {
        View includedLayout = findViewById(R.id.overlay);
        button1 = (Button) includedLayout.findViewById(R.id.bJourney);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View includedLayout = findViewById(R.id.overlay);
                /*
                   When the button is first clicked the switch's visibility is set to gone s
                   this prevents the UI from being cramped.
                   The list view is set to visible and the buttons text changes
                   The inverse of this happens when the button  is clicked again
                   Controled by the clicked boolean
                 */
                if (!clicked) {
                    Switch s = (Switch) includedLayout.findViewById(R.id.tSwitch);
                    s.setVisibility(View.GONE);
                    ListView lv = (ListView) includedLayout.findViewById(R.id.journList);
                    lv.setVisibility(View.VISIBLE);
                    button1.setText("Back");
                    updateList();
                    clicked = true;
                } else {
                    Switch s = (Switch) includedLayout.findViewById(R.id.tSwitch);
                    s.setVisibility(View.VISIBLE);
                    ListView lv = (ListView) includedLayout.findViewById(R.id.journList);
                    lv.setVisibility(View.GONE);
                    button1.setText("Journey");
                    clicked = false;
                }
            }
        });
    }

    ///Get current location co-ordinates
    public LatLng getCoords(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public void updateList() {
        if (pref.getFromDB(getApplicationContext()) != null) {//Check shared pref isn't null
            ListView lv;
            View includedLayout = findViewById(R.id.overlay);
            lv = (ListView) includedLayout.findViewById(R.id.journList);;
            journeys.setList(pref.getFromDB(getApplicationContext()));//Set the list to that of the one stored.
            adapter.clear();//clear old adapter
            adapter.addAll(journeys.getJourney());//update adapter
            lv.setAdapter(adapter);
        } else {
            toast.NullDB();//If not show toast.
        }

    }

    public void listClick() {
        ListView lv;
        View includedLayout = findViewById(R.id.overlay);
        lv = (ListView) includedLayout.findViewById(R.id.journList);//Get list view
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = "Started at " + adapter.getItem(position).getStartTime()
                        + "\n" + "End time " + adapter.getItem(position).getEndTime();

                toast.postTime(s);//Toast the time taken for that journey
                toast.postTime("Time taken " + adapter.getItem(position).getTimeTaken());//Show time taken toast
                eM.loadRouteFromMemory(adapter.getItem(position).getPositions());//Draw route of journey
            }
        });
    }

}
