package com.github.bagiasn.geolocation.ui;

import android.app.ActionBar;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;

import com.github.bagiasn.geolocation.R;
import com.github.bagiasn.geolocation.data.MqttHandler;
import com.github.bagiasn.geolocation.data.OnMqttEventListener;
import com.github.bagiasn.geolocation.data.model.IotDevice;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, OnMqttEventListener {
    private static final LatLng GREECE = new LatLng(39.074208, 21.824311);
    private static final int ZOOM_LEVEL = 5;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MqttHandler handler = new MqttHandler(getApplicationContext(), this);
        handler.start();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Set the map.
        this.googleMap = googleMap;
        googleMap.setBuildingsEnabled(true);
        googleMap.setIndoorEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Move camera window closer to Greece.
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(GREECE, ZOOM_LEVEL));
        // Show info to the user.
        ActionBar actionBar = getActionBar();
        if (actionBar == null) return;
        actionBar.setTitle("Listening to topic /home/");
        actionBar.setSubtitle("Connected to 207.154.229.161");
    }

    @Override
    public void onNewDevice(IotDevice device) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(device.getCurrentPosition())
                .title(device.getChipId())
                .snippet(device.getCurrentPosition().toString())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_flag_48px)));
        marker.showInfoWindow();

        device.setMarker(marker);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(device.getCurrentPosition())      // Sets the center of the map to Mountain View
                .zoom(20)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

}
