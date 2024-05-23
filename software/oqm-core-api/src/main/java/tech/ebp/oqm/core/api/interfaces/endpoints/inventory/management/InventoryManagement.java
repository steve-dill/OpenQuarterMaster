package tech.ebp.oqm.core.api.interfaces.endpoints.inventory.management;


import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import tech.ebp.oqm.core.api.interfaces.endpoints.EndpointProvider;
import tech.ebp.oqm.core.api.model.rest.auth.roles.Roles;
import tech.ebp.oqm.core.api.rest.dataImportExport.DataImportResult;
import tech.ebp.oqm.core.api.rest.dataImportExport.ImportBundleFileBody;
import tech.ebp.oqm.core.api.scheduled.ExpiryProcessor;
import tech.ebp.oqm.core.api.service.importExport.exporting.DatabaseExportService;
import tech.ebp.oqm.core.api.service.importExport.importing.DataImportService;
import tech.ebp.oqm.core.api.service.importExport.exporting.DataExportOptions;
import tech.ebp.oqm.core.api.service.mongo.DatabaseManagementService;
import tech.ebp.oqm.core.api.service.serviceState.db.DbCacheEntry;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmMongoDatabase;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 *
 * TODO:: refactor to more specific classes
 *
 * https://mkyong.com/java/how-to-create-tar-gz-in-java/
 */
@Slf4j
@Path(EndpointProvider.ROOT_API_ENDPOINT_V1 + "/inventory/manage")
@Tags({@Tag(name = "Inventory Management", description = "Endpoints for inventory management.")})
@RequestScoped
public class InventoryManagement extends EndpointProvider {
	
	@Inject
	DatabaseExportService databaseExportService;
	
	@Inject
	DataImportService dataImportService;
	
	@Inject
	ExpiryProcessor expiryProcessor;
	
	@Inject
	DatabaseManagementService dbms;

	@Inject
	OqmDatabaseService oqmDatabaseService;
	
	@Blocking
	@GET
	@Path("db/{oqmDbIdOrName}/export")
	@Operation(
		summary = "Creates a bundle of all inventory data stored."
	)
	@APIResponse(
		responseCode = "200",
		description = "Export bundle created.",
		content = @Content(
			mediaType = "application/tar+gzip"
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_ADMIN)
	@Produces("application/tar+gzip")
	public Response export(
			//TODO:: options as bean param? figure this out
	) throws IOException {
		File outputFile = databaseExportService.exportDataToBundle(new DataExportOptions());
		
		Response.ResponseBuilder response = Response.ok(outputFile);
		response.header("Content-Disposition", "attachment;filename=" + outputFile.getName());
		return response.build();
	}
	
	@Blocking
	@POST
	@Path("import/file/bundle")
	@Operation(
		summary = "."
	)
	@APIResponse(
		responseCode = "200",
		description = "Object added.",
		content = @Content(
			mediaType = MediaType.APPLICATION_JSON
		)
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_ADMIN)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response importData(
		@BeanParam ImportBundleFileBody body
	) throws IOException {
		DataImportResult result = this.dataImportService.importBundle(
			body.file,
			body.fileName,
			this.getInteractingEntity(),
			body.options
		);
		
		return Response.ok(result).build();
	}
	
	@Blocking
	@GET
	@Path("processExpiry")
	@Operation(
		summary = "Manually triggers the process to search for expired items and processing thereof."
	)
	@APIResponse(
		responseCode = "200",
		description = "Process triggered."
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_ADMIN)
	public Response triggerSearchAndProcessExpiring() {
		//TODO:: multithreaded
		for(DbCacheEntry curDb : this.oqmDatabaseService.getDatabases()) {
			expiryProcessor.searchAndProcessExpiring(curDb.getDbId().toHexString());
		}

		return Response.ok().build();
	}
	
	@Blocking
	@DELETE
	@Path("/db/{oqmDbIdOrName}/clearDb")
	@Operation(
		summary = "Manually triggers the process to clear the database."
	)
	@APIResponse(
		responseCode = "200",
		description = "Process triggered."
	)
	@APIResponse(
		responseCode = "400",
		description = "Bad request given. Data given could not pass validation.",
		content = @Content(mediaType = "text/plain")
	)
	@RolesAllowed(Roles.INVENTORY_ADMIN)
	public Map<String, Long> clearDatabase(
		@PathParam("oqmDbIdOrName")
		String oqmDbIdOrName
	) {
		return this.dbms.clearDb(oqmDbIdOrName, this.getInteractingEntity());
	}
	
	//TODO:: prune histories
	//TODO:: piecemeal import
}
