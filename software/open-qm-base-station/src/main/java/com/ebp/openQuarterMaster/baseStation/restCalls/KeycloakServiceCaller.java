package com.ebp.openQuarterMaster.baseStation.restCalls;

import com.fasterxml.jackson.databind.JsonNode;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("${service.externalAuth.tokenPath}")
@RegisterRestClient(configKey = "keycloak")
public interface KeycloakServiceCaller {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    JsonNode getJwt(
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("scope") String scope,
            @FormParam("grant_type") String grant_type,
            @FormParam("code") String code,
            @FormParam("redirect_uri") String redirect_uri
    );

}
