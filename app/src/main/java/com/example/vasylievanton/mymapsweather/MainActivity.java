package com.example.vasylievanton.mymapsweather;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity {

    GoogleMap googleMap;
    LocationManager locationManager;
    LatLng coordinate;
    TextView country_cityNameTV;
    TextView tempTV;
    TextView hum_presTV;
    private boolean x = true;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        country_cityNameTV = (TextView) findViewById(R.id.country_name);
        tempTV = (TextView) findViewById(R.id.city_name);
        hum_presTV = (TextView) findViewById(R.id.hum_press);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.INVISIBLE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        createMapView();
        initMap();
    }

    private void createMapView() {
        try {
            if (null == googleMap) {
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapView)).getMap();
                if (null == googleMap) {
                    Toast.makeText(getApplicationContext(),
                            "Error creating map", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (NullPointerException exception) {
            Log.e("mapApp", exception.toString());
        }
    }

    private void initMap() {

        if (null != googleMap) {
            //googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.getUiSettings().setMapToolbarEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(false);

            googleMap.setMyLocationEnabled(true);


            googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    if (coordinate == null) {
                        Toast.makeText(getApplicationContext(),"Не определено местоположение!",Toast.LENGTH_SHORT);
                    } else {
                        progressBar.setVisibility(View.VISIBLE);
                        hum_presTV.setVisibility(View.INVISIBLE);
                        country_cityNameTV.setVisibility(View.INVISIBLE);
                        tempTV.setVisibility(View.INVISIBLE);
                        googleMap.clear();
                        WeatherAsyncTask onMap = new WeatherAsyncTask();
                        onMap.execute("http://api.openweathermap.org/data/2.5/weather?lat=" + coordinate.latitude + "&lon=" + coordinate.longitude + "&appid=825a320e06b97ec7b281f12109173ec3");
                        onMap.setOnPhoneListener(new WeatherAsyncTask.onPhoneListener() {
                            @Override
                            public void onResponse(WeatherData webListItems, int success) {
                                progressBar.setVisibility(View.INVISIBLE);
                                country_cityNameTV.setVisibility(View.VISIBLE);
                                tempTV.setVisibility(View.VISIBLE);
                                hum_presTV.setVisibility(View.VISIBLE);
                                googleMap.addMarker(new MarkerOptions().position(coordinate).draggable(false).icon(
                                        BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                                country_cityNameTV.setText("Location : " + webListItems.getCityName() + ", " + webListItems.getCountryName());
                                tempTV.setText("Temperature: " + webListItems.getCurTemperature() + " F");
                                hum_presTV.setText("Humidity: " + webListItems.getCurHumidity() + " %, Pressure: " + webListItems.getCurPressure() + " Pa");
                            }
                        });
                    }
                    return false;
                }
            });

            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(final LatLng latLng) {
                    progressBar.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    hum_presTV.setVisibility(View.INVISIBLE);
                    country_cityNameTV.setVisibility(View.INVISIBLE);
                    tempTV.setVisibility(View.INVISIBLE);
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(latLng).draggable(false).icon(
                            BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                    googleMap.addCircle(new CircleOptions()
                            .center(latLng)
                            .radius(5)
                            .strokeColor(Color.BLUE)
                            .fillColor(Color.BLUE));
                    WeatherAsyncTask onMap = new WeatherAsyncTask();
                    onMap.execute("http://api.openweathermap.org/data/2.5/weather?lat=" + latLng.latitude + "&lon=" + latLng.longitude + "&appid=825a320e06b97ec7b281f12109173ec3");
                    onMap.setOnPhoneListener(new WeatherAsyncTask.onPhoneListener() {
                        @Override
                        public void onResponse(WeatherData webListItems, int success) {
                            progressBar.setVisibility(View.INVISIBLE);
                            country_cityNameTV.setVisibility(View.VISIBLE);
                            tempTV.setVisibility(View.VISIBLE);
                            hum_presTV.setVisibility(View.VISIBLE);

                            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(15).tilt(30).build();
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                            googleMap.animateCamera(cameraUpdate);

                            country_cityNameTV.setText("Location : " + webListItems.getCityName() + ", " + webListItems.getCountryName());
                            tempTV.setText("Temperature: " + webListItems.getCurTemperature() + " F");
                            hum_presTV.setText("Humidity: " + webListItems.getCurHumidity() + " %, Pressure: " + webListItems.getCurPressure() + " Pa");
                        }
                    });
                }
            });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, locationListener);
    }
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }
    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            createLatCord(location);
        }

        @Override
        public void onProviderEnabled(String provider) {
            createLatCord(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private void createLatCord(Location location){
        if (location == null){
            return;
        }
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER) | location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            coordinate = new LatLng(location.getLatitude(),location.getLongitude());
            Log.w("Location: ", location.getLatitude() + "," + location.getLongitude());
            if(x){
                CameraPosition cameraPosition = new CameraPosition.Builder().target(coordinate).zoom(15).tilt(30).build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                googleMap.animateCamera(cameraUpdate);
                WeatherAsyncTask onMap = new WeatherAsyncTask();
                onMap.execute("http://api.openweathermap.org/data/2.5/weather?lat=" + coordinate.latitude + "&lon=" + coordinate.longitude + "&appid=825a320e06b97ec7b281f12109173ec3");
                onMap.setOnPhoneListener(new WeatherAsyncTask.onPhoneListener() {
                    @Override
                    public void onResponse(WeatherData webListItems, int success) {
                        progressBar.setVisibility(View.INVISIBLE);
                        country_cityNameTV.setVisibility(View.VISIBLE);
                        tempTV.setVisibility(View.VISIBLE);
                        hum_presTV.setVisibility(View.VISIBLE);



                        country_cityNameTV.setText("Location : " + webListItems.getCityName() + ", " + webListItems.getCountryName());
                        tempTV.setText("Temperature: " + webListItems.getCurTemperature() + " F");
                        hum_presTV.setText("Humidity: " + webListItems.getCurHumidity() + " %, Pressure: " + webListItems.getCurPressure() + " Pa");
                    }
                });
                x = false;

            }

        }
    }

}

