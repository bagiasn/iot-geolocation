package com.github.bagiasn.geolocation.ui;

import android.app.Activity;
import android.os.Bundle;

import com.github.bagiasn.geolocation.R;
import com.github.bagiasn.geolocation.data.MqttHandler;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MqttHandler handler = new MqttHandler(getApplicationContext());
        handler.start();
    }

}
