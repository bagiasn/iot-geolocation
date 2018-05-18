# WiFi Indoor Positioning showcase
Displays the position of connected ESP8266 devices using the coordinates provided by the Google Maps Geolocation API.

## Deployment

The nodemcu folder contains the Arduino sketch.

The MQTT broker is a Mosquitto server running on Ubuntu 16.04 with a basic setup.
```
sudo apt-get install mosquitto mosquitto-clients  + password configuration
```

The Android app provides a way to compare the Geolocation API results with a real floor plan.

## Resources - links

* [ArduinoIDE - NodeMCU V3 LoLin](http://henrysbench.capnfatz.com/henrys-bench/arduino-projects-tips-and-more/arduino-esp8266-lolin-nodemcu-getting-started/) - Setup ArduinoIDE for flashing NodeMCU

* [Google Maps Geolocation API](https://developers.google.com/maps/documentation/geolocation/intro) - Overview of the API.
* [Microsoft Visio](https://www.microsoft.com/en-us/store/collections/visio/pc) - Used to create the floor plan.
