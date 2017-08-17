package com.takshak.sponsorship.sponsorshiptakshak;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ListPopupWindowCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.impl.FieldProperty;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.*;
import org.json.JSONObject;
import cz.msebera.android.httpclient.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;
    public static final int REQUEST_LOCATION_CODE = 99;
    private LatLng searchResult;
    private static final String connectionDomain = "http://takshak.herokuapp.com";
    private static AsyncHttpClient httpClient = new AsyncHttpClient();
    private static boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_CODE:
                if((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if(client == null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                }
                return;
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.setOnMarkerDragListener(this);
            //TODO MAKE CAMERA MOVE TO CURRENT POSITION ONLOAD
//            mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
        }
    }

    protected synchronized void buildGoogleApiClient(){
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        client.connect();
    }

    public void searchFunction(View v){
        if(v.getId() == R.id.searchButton){
            EditText tf_location = (EditText)findViewById(R.id.Location);
            String location = tf_location.getText().toString();
            List<Address> addressList;
            MarkerOptions markerOptions = new MarkerOptions();
//TODO ADD ACTIVITY TO SELECT FROM RETURNED LIST OF ADDRESSES
            if(!location.equals("")){
                Geocoder geocoder = new Geocoder(this);
                try {
                    addressList = geocoder.getFromLocationName(location, 5);
                    Address myAddress = addressList.get(0);
                    LatLng latlng = new LatLng(myAddress.getLatitude(), myAddress.getLongitude());
                    markerOptions.position(latlng);
                    markerOptions.title("Entered Location");
                    markerOptions.draggable(true);
                    mMap.clear();
                    mMap.addMarker(markerOptions);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                    this.searchResult = latlng;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
//TODO ADD ACTIVITY TO SHOW ALL LOCATIONS VISITED
    public void submitFunction(View v){
        if(v.getId() == R.id.submitButton){
            EditText tf_companyName = (EditText)findViewById(R.id.companyName);
            final String companyName = tf_companyName.getText().toString();
//TODO MAKE THIS CALL NON-ASYNCHRONOUS
            if((!companyName.equals(""))&&(this.searchResult!=null)){
                final Gson gson = new Gson();
                RequestParams params = new RequestParams();
                boolean flag;
                httpClient.get("http://takshak.herokuapp.com/addChecker/"+companyName, params, new AsyncHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        // called before request is started
                        Toast.makeText(getApplicationContext(), "Request Sent2", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                        ObjectMapper mapper = new ObjectMapper();
                        ArrayList<Parser> objects = null;
                        //TODO CHECK IF ANY RETURNED OBJECT IS IN THE VINCITY OF CURRENT LOCATION
                        try {
                            String resp = new String(response);
                            JavaType type = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Parser.class);
                            objects = mapper.readValue(resp, type);
                            Iterator<Parser> objectsIterator = objects.iterator();
                            boolean flag = true;
                            while(objectsIterator.hasNext()){
                                Parser newobj = objectsIterator.next();
//                        LatLng latlng = new LatLng(newobj.getlatlng().getlatitude(), )
                                //TODO CHECK FOR ANY ENTRY WITH SAME NAME WITHIN A RADIUS
                                flag = false;
                                break;
                            }
                            //TODO IF NONE FOUND, IT IS SAFE TO SEND THE POST REQUEST
                            String latlng = gson.toJson(searchResult);
                            RequestParams params = new RequestParams();
                            params.put("companyName", companyName);
                            params.put("latlng", latlng);
                            httpClient.post(connectionDomain, params, new AsyncHttpResponseHandler() {

                                @Override
                                public void onStart() {
                                    // called before request is started
                                    Toast.makeText(getApplicationContext(), "Request Sent1", Toast.LENGTH_LONG).show();
                                }
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                                    AlertDialog.Builder alertbox = new AlertDialog.Builder(MapsActivity.this);
                                    alertbox.setTitle("Success");
                                    alertbox.setMessage("You are free to go");
                                    alertbox.setCancelable(false);
                                    alertbox.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                                    AlertDialog alert = alertbox.create();
                                    alert.show();
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                                    if(statusCode==400){
                                        AlertDialog.Builder alertbox = new AlertDialog.Builder(MapsActivity.this);
                                        alertbox.setTitle("Warning");
                                        alertbox.setMessage("This company has already been visited by someone previously");
                                        alertbox.setCancelable(false);
                                        alertbox.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                        AlertDialog alert = alertbox.create();
                                        alert.show();
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(), "Server Down", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onRetry(int retryNo) {
                                    // called when request is retried
                                    Toast.makeText(getApplicationContext(), "Retrying", Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (IOException e) {
                            AlertDialog.Builder alertbox2 = new AlertDialog.Builder(MapsActivity.this);
                            alertbox2.setTitle("error");
                            alertbox2.setMessage(e.toString());
                            alertbox2.setCancelable(false);
                            alertbox2.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            AlertDialog alert2 = alertbox2.create();
                            alert2.show();
                            setboolean(false);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
//                        if(statusCode==400){
//                            AlertDialog.Builder alertbox = new AlertDialog.Builder(MapsActivity.this);
//                            alertbox.setTitle("Warning");
//                            alertbox.setMessage("fail");
//                            alertbox.setCancelable(false);
//                            alertbox.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
//
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                }
//                            });
//                            AlertDialog alert = alertbox.create();
//                            alert.show();
//                        }
//                        else{
//                            Toast.makeText(getApplicationContext(), "Server Downgyguy", Toast.LENGTH_LONG).show();
//                        }
//                        setboolean(false);
                            AlertDialog.Builder alertbox = new AlertDialog.Builder(MapsActivity.this);
                            alertbox.setTitle("Warning2");
                            alertbox.setMessage("This company has already been visited by someone previously");
                            alertbox.setCancelable(false);
                            alertbox.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            AlertDialog alert = alertbox.create();
                            alert.show();
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                        Toast.makeText(getApplicationContext(), "Retrying", Toast.LENGTH_LONG).show();
                    }
                });
//                String latlng = gson.toJson(searchResult);
//                RequestParams params = new RequestParams();
//                params.put("companyName", companyName);
//                params.put("latlng", latlng);
////                if(proccedsubmission(companyName, searchResult)){
////                    Toast.makeText(getApplicationContext(), "vvyiyi", Toast.LENGTH_LONG).show();
//                    httpClient.post(connectionDomain, params, new AsyncHttpResponseHandler() {
//
//                        @Override
//                        public void onStart() {
//                            // called before request is started
//                            Toast.makeText(getApplicationContext(), "Request Sent1", Toast.LENGTH_LONG).show();
//                        }
//                        @Override
//                        public void onSuccess(int statusCode, Header[] headers, byte[] response) {
//                            AlertDialog.Builder alertbox = new AlertDialog.Builder(MapsActivity.this);
//                            alertbox.setTitle("Success");
//                            alertbox.setMessage("You are free to go");
//                            alertbox.setCancelable(false);
//                            alertbox.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
//
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                }
//                            });
//                            AlertDialog alert = alertbox.create();
//                            alert.show();
//                        }
//
//                        @Override
//                        public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
//                            // called when response HTTP status is "4XX" (eg. 401, 403, 404)
//                            if(statusCode==400){
//                                AlertDialog.Builder alertbox = new AlertDialog.Builder(MapsActivity.this);
//                                alertbox.setTitle("Warning");
//                                alertbox.setMessage("This company has already been visited by someone previously");
//                                alertbox.setCancelable(false);
//                                alertbox.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
//
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//
//                                    }
//                                });
//                                AlertDialog alert = alertbox.create();
//                                alert.show();
//                            }
//                            else{
//                                Toast.makeText(getApplicationContext(), "Server Down", Toast.LENGTH_LONG).show();
//                            }
//                        }
//
//                        @Override
//                        public void onRetry(int retryNo) {
//                            // called when request is retried
//                            Toast.makeText(getApplicationContext(), "Retrying", Toast.LENGTH_LONG).show();
//                        }
//                    });
//                }
//                else{
//                    AlertDialog.Builder alertbox = new AlertDialog.Builder(MapsActivity.this);
//                    alertbox.setTitle("Warning2");
//                    alertbox.setMessage("This company has already been visited by someone previously");
//                    alertbox.setCancelable(false);
//                    alertbox.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
//
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//
//                        }
//                    });
//                    AlertDialog alert = alertbox.create();
//                    alert.show();
//                }
            }
            else{
                AlertDialog.Builder alertbox = new AlertDialog.Builder(MapsActivity.this);
                alertbox.setTitle("Warning");
                alertbox.setMessage("Please enter a company name");
                alertbox.setCancelable(false);
                alertbox.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alert = alertbox.create();
                alert.show();
            }
        }
    }

    public void setboolean(boolean bool){
        this.flag = bool;
    }

    private boolean proccedsubmission(String companyName, LatLng searchResult) {
        RequestParams params = new RequestParams();
        boolean flag;
        final Gson gson = new Gson();

        httpClient.get("http://takshak.herokuapp.com/addChecker/"+companyName, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
                Toast.makeText(getApplicationContext(), "Request Sent2", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                ObjectMapper mapper = new ObjectMapper();
                ArrayList<Parser> objects = null;
                //TODO CHECK IF ANY RETURNED OBJECT IS IN THE VINCITY OF CURRENT LOCATION
                try {
                    String resp = new String(response);
                    JavaType type = mapper.getTypeFactory().constructCollectionType(ArrayList.class, Parser.class);
                    objects = mapper.readValue(resp, type);
                    Iterator<Parser> objectsIterator = objects.iterator();
                    boolean flag = true;
                    while(objectsIterator.hasNext()){
                        Parser newobj = objectsIterator.next();
//                        LatLng latlng = new LatLng(newobj.getlatlng().getlatitude(), )
                        flag = false;
                        break;
                    }
                    setboolean(flag);
                    Toast.makeText(getApplicationContext(), "here", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    AlertDialog.Builder alertbox2 = new AlertDialog.Builder(MapsActivity.this);
                    alertbox2.setTitle("error");
                    alertbox2.setMessage(e.toString());
                    alertbox2.setCancelable(false);
                    alertbox2.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog alert2 = alertbox2.create();
                    alert2.show();
                    setboolean(false);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                if(statusCode==400){
                    AlertDialog.Builder alertbox = new AlertDialog.Builder(MapsActivity.this);
                    alertbox.setTitle("Warning");
                    alertbox.setMessage("fail");
                    alertbox.setCancelable(false);
                    alertbox.setPositiveButton("Ok", new DialogInterface.OnClickListener(){

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog alert = alertbox.create();
                    alert.show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Server Downgyguy", Toast.LENGTH_LONG).show();
                }
                setboolean(false);
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
                Toast.makeText(getApplicationContext(), "Retrying", Toast.LENGTH_LONG).show();
            }
        });
        Toast.makeText(getApplicationContext(), "exiting2", Toast.LENGTH_LONG).show();
        return this.flag;
    }

    public void currentLocationFunction(View v){
        if(v.getId() == R.id.currentLocationButton){
            if (checkLocationPermission()) {
                lastLocation = LocationServices.FusedLocationApi
                        .getLastLocation(client);
            }

            // Set the map's camera position to the current location of the device.
            if (lastLocation != null) {
                LatLng latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("You are here");
                markerOptions.draggable(true);
                mMap.clear();
                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomBy(10));
                searchResult = latLng;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        if(currentLocationMarker != null){
            currentLocationMarker.remove();
        }

        LatLng latlng =  new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.title("You are here");
        markerOptions.draggable(true);
        mMap.clear();
        currentLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));
        this.searchResult = latlng;
        if(client != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    public boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        }
        else{
            return true;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        this.searchResult = marker.getPosition();
    }
}
