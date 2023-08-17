#ifndef MSS_ENGINE_H
#define MSS_ENGINE_H

#include <Arduino.h>
#include <FastLED.h>
#include <ArduinoJson.h>
#include "MssConnector.h"


class MssEngine {
  private:
    // https://forum.arduino.cc/t/how-to-read-the-id-serial-number-of-an-arduino/45214/8
    String moduleSerialId; //TODO:: change this to ModuleInfo data class
    
    uint16_t numStorageBins;
    uint16_t numLedsInStrip;
    uint16_t ledPin;
    uint16_t ledBrightness;
    MssConnector* connector;
  
  public:
    MssEngine(
      String moduleSerialId,
      MssConnector* connector,
      uint16_t numStorageBins,
      uint16_t numLedsInStrip,
      uint16_t ledPin,
      uint16_t ledBrightness,
      bool hasIndicator
    );

};



#endif
