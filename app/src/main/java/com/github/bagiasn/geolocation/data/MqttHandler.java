package com.github.bagiasn.geolocation.data;

import android.content.Context;
import android.util.Log;

import com.github.bagiasn.geolocation.data.model.IotDevice;
import com.google.android.gms.maps.model.LatLng;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

public class MqttHandler implements MqttCallback {
    // Logging tag.
    private static final String TAG = MqttHandler.class.getSimpleName();
    private static final String MQTT_SERVER = "tcp://207.154.229.161:1883";
    private static final String MQTT_USER = "nikos";
    private static final String MQTT_PWD = "jarvis34";
    private static final String TOPIC = "test";
    private static final int QOS = 1;

    private  MqttAndroidClient client;
    private OnMqttEventListener callback;
    private ArrayList<IotDevice> iotDevices;

    public MqttHandler(Context context, OnMqttEventListener callback) {
        this.client =  new MqttAndroidClient(context, MQTT_SERVER, MqttClient.generateClientId());
        this.callback = callback;
        this.iotDevices = new ArrayList<>();
    }

    public void start() {
        if (client != null) {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(MQTT_USER);
            options.setPassword(MQTT_PWD.toCharArray());

            try {
                // Connect to the server.
                IMqttToken token = client.connect(options);
                client.setCallback(this);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i(TAG, "Connected to broker.");
                        try {
                            IMqttToken subToken = client.subscribe(TOPIC, QOS);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {

                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                    Log.e(TAG, "Subscription failed. Cause: " + exception.getMessage());
                                }
                            });
                        } catch (MqttException e) {
                            Log.e(TAG, "Exception raised: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.e(TAG, "onFailure");
                    }
                });
            } catch (MqttException e) {
                Log.e(TAG, "onFailure");
            }
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.e(TAG, "Connection lost. Cause: " + cause.getLocalizedMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        Log.i(TAG, "Message arrived from topic " + topic + ": " + message.toString());

        IotDevice device = extractDevice(message.toString());
        if (device == null) return;

        callback.onNewDevice(device);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    private IotDevice extractDevice(String message) {
        String[] splitMessage = message.split("#");
        if (splitMessage.length < 3) {
            Log.e(TAG, "Message malformed. Ignoring");
        } else {
            double lat = Double.valueOf(splitMessage[1]);
            double lang = Double.valueOf(splitMessage[2]);

            IotDevice newDevice = new IotDevice(splitMessage[0]);
            if (iotDevices.contains(newDevice)) {
                IotDevice device = iotDevices.get(iotDevices.indexOf(newDevice));
                device.updateMarker(new LatLng(lat, lang));
            } else {
                iotDevices.add(newDevice);
                newDevice.addPosition(new LatLng(lat, lang));
                return newDevice;
            }
        }
        return null;
    }
}
