package com.shamim.LocationAlarm;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.shamim.LocationAlarm.nearby.NearbyResponse;
import com.shamim.LocationAlarm.nearby.Result;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnCameraIdleListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnMarkerClickListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 111;
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted = true;
    private FusedLocationProviderClient client;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        tv = findViewById(R.id.latLngTV);
        client = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        //mMap.setOnMarkerDragListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setTrafficEnabled(true);
        if(mLocationPermissionGranted){
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
        //updateLocationUI();

        getCurrentLocation();

        // Add a marker in Sydney and move the camera


    }

    private void getCurrentLocation() {
        if(mLocationPermissionGranted){
            client.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if(location == null){
                                return;
                            }

                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            LatLng currentLatLnt = new LatLng(latitude, longitude);
                            /*Marker myPositionMarker =
                                    mMap.addMarker(new MarkerOptions()
                                    .position(currentLatLnt)
                                    .title("I am here")
                                    .draggable(true));*/
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLnt, 14f));
                            getNearbyPlaces(new LatLng(latitude, longitude));
                        }
                    });
        }
    }

    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getCurrentLocation();
                }
            }
        }
        //updateLocationUI();
    }

    private void getNearbyPlaces(LatLng latLng){
        final String apiKey = getString(R.string.nearby_place_api_key);
        String endUrl = String.format("place/nearbysearch/json?location=%f,%f&radius=1500&type=restaurant&key=%s",
                latLng.latitude, latLng.longitude, apiKey);
        NearbyService service = RetrofitClient.getClient()
                .create(NearbyService.class);
        service.getNearbyPlaces(endUrl)
                .enqueue(new Callback<NearbyResponse>() {
                    @Override
                    public void onResponse(Call<NearbyResponse> call, Response<NearbyResponse> response) {
                        if(response.isSuccessful()){
                            NearbyResponse nearbyResponse = response.body();
                            List<Result> resultList = nearbyResponse.getResults();
                            for(Result r : resultList){
                                double lat = r.getGeometry().getLocation().getLat();
                                double lng = r.getGeometry().getLocation().getLng();
                                LatLng rest = new LatLng(lat, lng);
                                mMap.addMarker(new MarkerOptions()
                                .position(rest)
                                .title(r.getName()));
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<NearbyResponse> call, Throwable t) {

                    }
                });
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Toast.makeText(this, "started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        Toast.makeText(this, "stopped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraIdle() {
        LatLng latLng = mMap.getCameraPosition().target;
        tv.setText(latLng.latitude+" "+latLng.longitude);
        getNearbyPlaces(latLng);
    }

    @Override
    public void onCameraMove() {
        LatLng latLng = mMap.getCameraPosition().target;
        tv.setText(latLng.latitude+" "+latLng.longitude);
        mMap.clear();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_SHORT).show();
        return true;
    }
}
