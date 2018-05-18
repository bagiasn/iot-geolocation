/*
 * IoT Geolocation
 * 
 * This sketch 
 * 1. Retrieves the device's coordinates using the Google Maps Geolocation API.
 * 2. Constructs and sends a message to an MQTT broker.
 * 
 */

#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>

const char* SSID_ = "*****";
const char* PASSWORD_ = "*********";

const char* GOOGLE_HOST = "www.googleapis.com";
const char* GEOLOCATION_API = "/geolocation/v1/geolocate?key=";
const char* API_KEY = "YOUR_API_KEY";

const char* MQTT_USER = "nikos";
const char* MQTT_PWD = "*****";
const char* MQTT_TOPIC = "home";

WiFiClient espClient;
PubSubClient client(espClient);

void setup() {
  Serial.begin(115200);
  while (!Serial) continue;
  Serial.println();
  // Connect to the WiFi first (blocking).
  connectToWiFi();
  // Connect to our mqtt server.
  connectToBroker();
}

void loop() {
  // Generate JSON request for Google Maps Geolocation API.
  String jsonBody = generateJson();
  // Issue the request.
  String jsonResponse = sendApiRequest(jsonBody);
  // Parse the response and construct the message.
  char* message = parseApiResponse(jsonResponse);
  // Publish!
  client.publish(MQTT_TOPIC, message);
  // Sleep for a while.
  delay(5000);
}

/*
 * Attempts WiFi connection with the provided credentials.
 */
void connectToWiFi() {
  WiFi.begin(SSID_, PASSWORD_);

  Serial.print("Connecting");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();

  Serial.print("Connected, IP address: ");
  Serial.println(WiFi.localIP());
}

/*
 * Establishes connection with the MQTT server.
 * 
 */
void connectToBroker() {
  IPAddress serverIp(207, 154, 229, 161);
  client.setServer(serverIp, 1883);
  client.setCallback(callback);
  client.connect("IotClient", MQTT_USER, MQTT_PWD);
}

/*
 * Prints received messages.
 * 
 * Not really used.
 */
void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();
}

/*
 * Constructs manually a JSON object containing the required/optional fields.
 * 
 * Follows the suggested format: https://developers.google.com/maps/documentation/geolocation/intro
 */
String generateJson() {
  String json = "{\n";
  // Since we are not currently using GSM these don't really matter.
  // Will revisit later.
  json+= "\"homeMobileCountryCode\":202,\n"; // Greece MCC
  json+= "\"homeMobileNetworkCode\":1,\n"; // This is Cosmote's MNC (along with 02)
  json+= "\"radioType\":\"gsm\",\n";
  json+= "\"carrier\":\"Cosmote\",\n";
  json+= "\"considerIp\":\"true\",\n";
  // The APs characteristics can be retrieved through scanning.
  int numOfAps = WiFi.scanNetworks();
  if (numOfAps == -1) {
    Serial.println("No WiFi connection! IP Geolocation is coming...");
  } else {
    json+= "\"wifiAccessPoints\":[";
    Serial.print("Number of available networks:");
    Serial.println(numOfAps);
    for (int i = 0; i < numOfAps; i++) {
      json+= "{\n";
      json+= "\"macAddress\":\"";
      json+= WiFi.BSSIDstr(i);
      json+= "\",\n";
      json+= "\"signalStrength\":";
      json+= WiFi.RSSI(i);
      json+= ",\n\"age\":0,\n";
      json+= "\"channel\":";
      json+= WiFi.channel(i);
      if (i == numOfAps - 1) {
        json+= "\n}";
      } else {
        json+= "\n},";
    }
  }
  json+= "]\n}"; 
  }
  // Print the json for error checking.
  Serial.println(json);
  
  return json;
}

/*
 * Sends a POST request with the provided json as body. 
 * 
 * Returns the response.
 */
String sendApiRequest(String jsonBody) {
   WiFiClientSecure apiClient;
   String response = "";
   
   Serial.print("Connecting to Google server: ");
   Serial.println(GOOGLE_HOST);
   Serial.println(GEOLOCATION_API);
   Serial.println(API_KEY);
   if (apiClient.connect(GOOGLE_HOST, 443)) {
      Serial.println("Connected");
      apiClient.print("POST ");
      apiClient.print(GEOLOCATION_API);
      apiClient.print(API_KEY);
      apiClient.println(" HTTP/1.1");
      apiClient.print("Host: ");
      apiClient.println(GOOGLE_HOST);
      apiClient.println("User-Agent: ESP8266/1.0");
      apiClient.println("Connection: close");
      apiClient.println("Content-Type: application/json");
      apiClient.print("Content-Length: ");
      apiClient.println(jsonBody.length());
      apiClient.println();
      apiClient.println(jsonBody);
      // Check if the request timed-out.
      unsigned long timeout = millis();
      while (apiClient.available() == 0) {
         if (millis() - timeout > 10000) {
            Serial.print("Request timeout. :(");
            apiClient.stop();
            return response;
         }
      }
      // Skip response headers.
      char endOfHeaders[] = "\r\n\r\n";
      if (!apiClient.find(endOfHeaders)) {
          Serial.println(F("Invalid response"));
      }
      // Now read the main body.
      while (apiClient.available()) {        
        response+= apiClient.readStringUntil('\r');     
      }
   } else {
      Serial.print("Failed to connect to ");
      Serial.println(GOOGLE_HOST);
   }
   // Terminate the session.
   apiClient.stop();
   
   return response;
}

/*
 * Receives the json body as input and constructs the MQTT message to send.
 * 
 * Using ArduinoJson for easier parsing.
 */
char* parseApiResponse(String json) {
  String msgToSend = String(ESP.getChipId()).c_str();
  // Dynamic allocation - help -> https://arduinojson.org/assistant/
  const size_t bufferSize = 2*JSON_OBJECT_SIZE(2) + 60;
  DynamicJsonBuffer jsonBuffer(bufferSize);
  // Parse JSON object
  JsonObject& root = jsonBuffer.parseObject(json);
  if (!root.success()) {
    Serial.println("Parsing json object failed.");
  } else {
    String lat = root["location"]["lat"];
    String lng = root["location"]["lng"];
    // Using # as delimeter.
    msgToSend+= '#'; 
    msgToSend+= lat;
    msgToSend+= '#'; 
    msgToSend+= lng;
    msgToSend+= '#'; 
  }
  // Convert to char array to confront with the publish method.
  char charToSend[100];
  msgToSend.toCharArray(charToSend, 100);
  
  return charToSend;
}


