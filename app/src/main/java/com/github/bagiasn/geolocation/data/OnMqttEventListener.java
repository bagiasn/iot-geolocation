package com.github.bagiasn.geolocation.data;

import com.github.bagiasn.geolocation.data.model.IotDevice;

public interface OnMqttEventListener {

    void onNewDevice(IotDevice device);
}
