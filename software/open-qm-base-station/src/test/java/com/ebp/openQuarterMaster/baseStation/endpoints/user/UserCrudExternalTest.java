package com.ebp.openQuarterMaster.baseStation.endpoints.user;

import com.ebp.openQuarterMaster.baseStation.testResources.TestResourceLifecycleManager;
import com.ebp.openQuarterMaster.baseStation.testResources.data.TestUserService;
import com.ebp.openQuarterMaster.baseStation.testResources.profiles.ExternalAuthTestProfile;
import com.ebp.openQuarterMaster.baseStation.testResources.testClasses.RunningServerTest;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserCreateRequest;
import com.ebp.openQuarterMaster.lib.core.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;


@Slf4j
@QuarkusTest
@TestProfile(ExternalAuthTestProfile.class)
@QuarkusTestResource(value = TestResourceLifecycleManager.class, initArgs = @ResourceArg(name=TestResourceLifecycleManager.EXTERNAL_AUTH_ARG, value="true"), restrictToAnnotatedClass = true)
@TestHTTPEndpoint(UserCrud.class)
class UserCrudExternalTest extends RunningServerTest {
    @Inject
    ObjectMapper objectMapper;
    @Inject
    TestUserService testUserService;

    @Test
    public void testCreateUserAttempt() throws JsonProcessingException {
        User testUser = this.testUserService.getTestUser(false, false);

        UserCreateRequest ucr = new UserCreateRequest(
                testUser.getFirstName(),
                testUser.getLastName(),
                testUser.getUsername(),
                testUser.getEmail(),
                testUser.getTitle(),
                "1!Letmein",
                testUser.getAttributes()
        );

        String errorMessage = given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(ucr))
                .when()
                .post()
                .then()
                .statusCode(Response.Status.FORBIDDEN.getStatusCode())
                .extract().body().toString();

        log.info("Error Message: {}", errorMessage);
//        assertEquals("User not found.", errorMessage.getError());
    }

}