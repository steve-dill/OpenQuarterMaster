package com.ebp.openQuarterMaster.plugin.restClients;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.rest.client.reactive.ClientFormParam;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v1/inventory/storage-block")
@RegisterRestClient
@RegisterClientHeaders(BaseStationAuthHeaderFactory.class)
public interface BaseStationStorageBlockRestClient {
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String postNewStorageBlock(
		ObjectNode newStorageBlock
	);
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayNode searchBlocks(
		@BeanParam StorageBlockSearch search
	);
}
