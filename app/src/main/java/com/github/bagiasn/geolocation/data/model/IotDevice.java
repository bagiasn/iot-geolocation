package com.github.bagiasn.geolocation.data.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IotDevice {
    private String chipId;
    private List<LatLng> positionHistory;
    private Marker marker;

    public IotDevice(String chipId) {
        this.chipId = chipId;
        this.positionHistory = new ArrayList<>();
    }

    public String getChipId() {
        return chipId;
    }

    public void addPosition(LatLng newPosition) {
        positionHistory.add(newPosition);
    }

    public void updateMarker(LatLng newPosition) {
        marker.setPosition(newPosition);
    }

    public LatLng getCurrentPosition() {
        return positionHistory.get(positionHistory.size() - 1);
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IotDevice iotDevice = (IotDevice) o;
        return Objects.equals(chipId, iotDevice.chipId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chipId);
    }

}
