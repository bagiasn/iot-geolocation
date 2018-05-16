package com.github.bagiasn.geolocation.ui;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, OnMqttEventListener {

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
    }

    @Override
    public void onNewDevice(IotDevice device) {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .position(device.getCurrentPosition())
                .title(device.getChipId())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        device.setMarker(marker);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(device.getCurrentPosition(), 20.0f));

    }

}
