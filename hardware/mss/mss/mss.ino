#define MSS_VAR_NBLOCKS 64
#define MSS_VAR_NLEDS_PER_BLOCK 3
#define MSS_LED_PIN     2
#define MSS_SPKR_PIN    9

#define DELAY    500

#include <FastLED.h>
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
    //mssEngine.loop();
    mssEngine.lightTest();
}
