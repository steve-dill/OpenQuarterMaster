#define MSS_VAR_NBLOCKS 64
#define MSS_VAR_NLEDS_PER_BLOCK 3
#define MSS_LED_PIN     2
#define MSS_SPKR_PIN    9

#define DELAY    500

#include <FastLED.h>
#include <ArduinoJson.h>
#include "MssEngine.h"
#include "MssSerialConnector.h"

MssEngine mssEngine(
        MssModInfo(
                "1.0.0",
                "test-01",
                MssModCapabilities(
                        true,
                        true,
                        false
                )
        ),
        new MssSerialConnector(),
        true
);

void setup() {
    mssEngine.init();
}



void loop() {
//    mssEngine.loop();
    mssEngine.lightTest();

//    if(Serial.available()){
//        StaticJsonDocument<256> doc;
//
//
//        DeserializationError error = deserializeJson(doc, Serial);
//
//        if (error) {
//            Serial.print(F("deserializeJson() failed: "));
//            Serial.println(error.f_str());
//        } else {
//            serializeJson(doc, Serial);
//            Serial.println();
//        }
//
//    }
}
