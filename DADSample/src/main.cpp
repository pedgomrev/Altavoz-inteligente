#include "RestClient.h"
#include "ArduinoJson.h"
#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include <Adafruit_Sensor.h>
#include <DHT.h>
#include "song.h"

#define HumedadPin 5
#define DHTTYPE DHT11

// AQUI DEFINIMOS LA CANCION QUE SE REPRODUCE

int tune[] = // list out the tune according to the musical notation
    {
        M3, M3, M4, M5,
        M5, M4, M3, M2,
        M1, M1, M2, M3,
        M3, M2, M2,
        M3, M3, M4, M5,
        M5, M4, M3, M2,
        M1, M1, M2, M3,
        M2, M1, M1,
        M2, M2, M3, M1,
        M2, M3, M4, M3, M1,
        M2, M3, M4, M3, M2,
        M1, M2, D5, D0,
        M3, M3, M4, M5,
        M5, M4, M3, M4, M2,
        M1, M1, M2, M3,
        M2, M1, M1};

float durt[] = // list out the beats according to the musical notation
    {
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1 + 0.5,
        0.5,
        1 + 1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1 + 0.5,
        0.5,
        1 + 1,
        1,
        1,
        1,
        1,
        1,
        0.5,
        0.5,
        1,
        1,
        1,
        0.5,
        0.5,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        0.5,
        0.5,
        1,
        1,
        1,
        1,
        1 + 0.5,
        0.5,
        1 + 1,
};

const int sensor = 0;
int length;
int AltavozPin = 14;
int LuzPin = 4;
DHT dht(HumedadPin, DHTTYPE);

// current temperature, humidity and light sensor, updated in loop()
float t = 0.0;
float h = 0.0;
int l = 0;
unsigned long previousMillis = 0; // will store last time DHT was updated

// Updates DHT readings every 1 second
const long interval = 1000;

int test_delay = 5000; // so we don't spam the API
boolean describe_tests = true;

RestClient client = RestClient("192.168.241.8", 8080);
WiFiClient espClient;
PubSubClient clientMQTT(espClient);
long lastMsg = 0;
char msg[50];
#define STASSID "POCO X3 NFC"
#define STAPSK "jdj07051234jdj"
const char *mqtt_server = "192.168.241.8";

void setup_wifi()
{
  delay(10);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(STASSID);
  WiFi.mode(WIFI_STA);
  WiFi.begin(STASSID, STAPSK);

  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}
int valorAnterior = 0;
int x = 0;
void callback(char *topic, byte *payload, unsigned int length)
{
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++)
  {
    Serial.print((char)payload[i]);
  }
  Serial.println();

  if ((char)payload[0] == '1')
  {
    if(valorAnterior == 0){
      valorAnterior = 1;
      Serial.print("Encendiendo altavoz");
    }
  }
  else
  {
    if(valorAnterior == 1){
      valorAnterior = 0;
      x = 0;
      Serial.print("Apagando altavoz");
      //DESACTIVAMOS ALTAVOZ
      noTone(AltavozPin);
      digitalWrite(BUILTIN_LED, HIGH); // Turn the LED off by making the voltage HIGH
    }
  }
}

void reconnect()
{
  while (!clientMQTT.connected())
  {
    Serial.print("Attempting MQTT connection...");
    if (clientMQTT.connect("ESP8266Altavoz2"))
    {
      Serial.println("connected");
      clientMQTT.subscribe("altavoz_1");
      clientMQTT.publish("altavoz_1", "Enviando el primer mensaje");
    }
    else
    {
      Serial.print("failed, rc=");
      Serial.print(clientMQTT.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
}

String response;

String serializeBodyHumedad(double valor, int idPlaca, long fecha)
{
  StaticJsonDocument<200> doc;
  doc["valor"] = valor;
  doc["idPlaca"] = idPlaca;
  doc["fecha"] = fecha;
  String output;
  serializeJson(doc, output);
  Serial.println(output);
  return output;
}
String serializeBodyLuz(int encendido, int idPlaca, long fecha)
{
  StaticJsonDocument<200> doc;
  doc["encendido"] = encendido;
  doc["idPlaca"] = idPlaca;
  doc["fecha"] = fecha;

  String output;
  serializeJson(doc, output);

  Serial.println(output);
  return output;
}
String serializeBodyPlaca(String nombre)
{
  StaticJsonDocument<200> doc;
  doc["nombre"] = nombre;
  String output;
  serializeJson(doc, output);
  Serial.println(output);
  return output;
}

void test_status(int statusCode)
{
  delay(test_delay);
  if (statusCode == 200 || statusCode == 201)
  {
    Serial.print("TEST RESULT: ok (");
    Serial.print(statusCode);
    Serial.println(")");
  }
  else
  {
    Serial.print("TEST RESULT: fail (");
    Serial.print(statusCode);
    Serial.println(")");
  }
}

void deserializeBodyHumedad(String responseJson)
{
  if (responseJson != "")
  {
    StaticJsonDocument<200> doc;
    DeserializationError error = deserializeJson(doc, responseJson);
    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }
    JsonArray jsonArr = doc.as<JsonArray>();
    for (JsonObject json : jsonArr)
    {
      int idLuz = json["id"];
      int valor = json["valor"];
      long fecha = json["fecha"];
      int idPlaca = json["idPlaca"];

      Serial.println(idLuz);
      Serial.println(valor);
      Serial.println(fecha);
      Serial.println(idPlaca);
    }
  }
}
void deserializeBodyLuz(String responseJson)
{
  if (responseJson != "")
  {
    StaticJsonDocument<200> doc;
    DeserializationError error = deserializeJson(doc, responseJson);
    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }
    JsonArray jsonArr = doc.as<JsonArray>();
    for (JsonObject json : jsonArr)
    {
      int idLuz = json["id"];
      int encendido = json["encendido"];
      long fecha = json["fecha"];
      int idPlaca = json["idPlaca"];

      Serial.println(idLuz);
      Serial.println(encendido);
      Serial.println(fecha);
      Serial.println(idPlaca);
    }
  }
}
void deserializeBodyPlaca(String responseJson)
{
  if (responseJson != "")
  {
    StaticJsonDocument<200> doc;
    DeserializationError error = deserializeJson(doc, responseJson);
    if (error)
    {
      Serial.print(F("deserializeJson() failed: "));
      Serial.println(error.f_str());
      return;
    }
    JsonArray jsonArr = doc.as<JsonArray>();
    for (JsonObject json : jsonArr)
    {
      int idPlaca = json["idPlaca"];
      String nombre = json["nombre"];

      Serial.println(idPlaca);
      Serial.println(nombre);
    }
  }
}

void test_response()
{
  Serial.println("TEST RESULT: (response body = " + response + ")");
  response = "";
}

void describe(char *description)
{
  if (describe_tests)
    Serial.println(description);
}

void POST_Humedad(double h)
{
  int idPlaca = 1;
  long fecha = 0;
  String post_body = serializeBodyHumedad(h, idPlaca, fecha);
  describe("Test POST with path and body and response");
  test_status(client.post("/api/humedad", post_body.c_str(), &response));
  test_response();
}

void POST_Luz(int l)
{
  int idPlaca = 1;
  long fecha = 0;
  String post_body = serializeBodyLuz(l, idPlaca, fecha);
  describe("Test POST with path and body and response");
  test_status(client.post("/api/luz", post_body.c_str(), &response));
  test_response();
}
// Setup
void setup()
{
  pinMode(LuzPin, INPUT);
  pinMode(AltavozPin, OUTPUT);
  length = sizeof(tune) / sizeof(tune[0]); // calculate the length

  // Serial port for debugging purposes
  Serial.begin(115200);
  dht.begin();

  setup_wifi();

  clientMQTT.setServer(mqtt_server, 1883);
  clientMQTT.setCallback(callback);
}
// Run the tests!
void loop()
{
  if (valorAnterior == 1){
    tone(AltavozPin,tune[x]);
    delay(500*durt[x]);   //adjust the delay time according to tone, you can change the index 500 for the music. 
    noTone(AltavozPin); 
    if (x >= length)
      x = 0;
    else
      x = x + 1;
  }
  if (sensor == 1)
  { 
    unsigned long currentMillis = millis();
    if (currentMillis - previousMillis >= interval)
    {
      previousMillis = currentMillis;
      // Read Humidity
      float newH = dht.readHumidity();
      // if humidity read failed, don't change h value
      if (isnan(newH))
      {
        Serial.println("Failed to read from DHT sensor!");
      }
      else
      {
        h = newH;
        POST_Humedad(h);
        Serial.println("Humidity: ");
        Serial.println(h);
        Serial.println("Light Sensor State:");
        l = digitalRead(LuzPin);
        POST_Luz(l);
        Serial.println(l);
      }
    }
  }
  else
  {
    if (!clientMQTT.connected())
    {
      reconnect();
    }
    clientMQTT.loop();
  }

}
