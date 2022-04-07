package com.ebp.openQuarterMaster.driverServer;

import com.ebp.openQuarterMaster.driverServer.serial.SerialPortWrapper;
import com.ebp.openQuarterMaster.driverServer.testUtils.lifecycleManagers.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.driverServer.testUtils.testClasses.RunningServerTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@QuarkusTest
@QuarkusTestResource(
    value = TestResourceLifecycleManager.class,
    initArgs = {
//        @ResourceArg(name = TestResourceLifecycleManager.NUM_SERIAL_PORTS_ARG, value = "2")
    },
    restrictToAnnotatedClass = true
)
public class ReactiveGreetingResourceTest extends RunningServerTest {

    @ConfigProperty(name = "serial.extraPorts", defaultValue = "")
    List<String> serialPorts;
    
    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/hello")
          .then()
             .statusCode(200)
             .body(is("Hello RESTEasy Reactive"));
    }
    
    @Test
    public void testSerialStuff() throws InterruptedException {
        log.info("Serial ports: {}", serialPorts);
    
        SerialPortWrapper wrapper = new SerialPortWrapper(serialPorts.get(0));
        
        wrapper.acquireLock();
        
        wrapper.writeLine("$P");
        
        TestResourceLifecycleManager.getPortManager().processHw();
        
        String returned = wrapper.readLine();
        
        assertEquals("Got ping request.", returned);
    
        returned = wrapper.readLine();
        
        assertEquals("$P", returned);
    }

}