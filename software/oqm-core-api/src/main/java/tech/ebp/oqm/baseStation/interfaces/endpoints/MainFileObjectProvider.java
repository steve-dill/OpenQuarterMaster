package tech.ebp.oqm.baseStation.interfaces.endpoints;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.interfaces.endpoints.media.FileGet;
import tech.ebp.oqm.baseStation.model.object.FileMainObject;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.rest.file.FileUploadBody;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.mongo.MongoHistoriedObjectService;
import tech.ebp.oqm.baseStation.service.mongo.file.MongoHistoriedFileService;
import tech.ebp.oqm.baseStation.service.mongo.search.PagingCalculations;
import tech.ebp.oqm.baseStation.service.mongo.search.SearchResult;

/**
 * Main abstract method to handle standard CRUD operations for MainObjects
 * <p>
 *
 * @param <T>
 * @param <S>
 */
@Slf4j
@NoArgsConstructor
public abstract class MainFileObjectProvider<T extends FileMainObject, S extends SearchObject<T>, G extends FileGet, F extends FileUploadBody> extends ObjectProvider {
	
	public abstract MongoHistoriedFileService<T, S, G> getFileObjectService();
	
	@WithSpan
	protected Response.ResponseBuilder getSearchResponseBuilder(
		@BeanParam S searchObject
	) {
		SearchResult<T> searchResult = this.getFileObjectService().getFileObjectService().search(searchObject, true);
		return this.getSearchResultResponseBuilder(searchResult);
	}
	
	
	
	//<editor-fold desc="CRUD operations">
	
//	@POST
//	@Operation(
//		summary = "Adds a file."
//	)
//	@APIResponse(
//		responseCode = "200",
//		description = "Object added.",
//		content = @Content(
//			mediaType = MediaType.APPLICATION_JSON,
//			schema = @Schema(
//				implementation = ObjectId.class
//			)
//		)
//	)
//	@APIResponse(
//		responseCode = "400",
//		description = "Bad request given. Data given could not pass validation.",
//		content = @Content(mediaType = "text/plain")
//	)
//	@RolesAllowed(Roles.INVENTORY_ADMIN)
//	@Consumes(MediaType.MULTIPART_FORM_DATA)
//	@Produces(MediaType.APPLICATION_JSON)
//	public abstract Response add(
//		@Context SecurityContext securityContext,
//		@BeanParam F body
//	) throws IOException;
	
	@WithSpan
	public Response search(
		//		@BeanParam
		S searchObject
	) {
		return this.getSearchResponseBuilder(searchObject).build();
	}
	
	//</editor-fold>
	
	//<editor-fold desc="History">
	
//	@GET
//	@Path("{id}/history")
//	@Operation(
//		summary = "Gets a particular object's history."
//	)
//	@APIResponse(
//		responseCode = "200",
//		description = "Object retrieved.",
//		content = {
//			@Content(
//				mediaType = "application/json",
//				schema = @Schema(type = SchemaType.ARRAY, implementation = ObjectHistoryEvent.class)
//			)
//		}
//	)
//	@APIResponse(
//		responseCode = "400",
//		description = "Bad request given. Data given could not pass validation.",
//		content = @Content(mediaType = "text/plain")
//	)
//	@APIResponse(
//		responseCode = "404",
//		description = "No history found for object with that id.",
//		content = @Content(mediaType = "text/plain")
//	)
//	@Produces(MediaType.APPLICATION_JSON)
//	@RolesAllowed(Roles.INVENTORY_VIEW)
	@WithSpan
	public Response getHistoryForObject(
		@PathParam("id") String id,
		@BeanParam HistorySearch searchObject,
		@HeaderParam("accept") String acceptHeaderVal,
		@HeaderParam("searchFormId") String searchFormId
	) {
		log.info("Retrieving specific {} history with id {} from REST interface", this.getFileObjectService().getClazz().getSimpleName(), id);
		
		searchObject.setObjectId(new ObjectId(id));
		
		SearchResult<ObjectHistoryEvent> searchResult = this.getFileObjectService().getFileObjectService().searchHistory(searchObject, false);
		return this.getSearchResultResponseBuilder(searchResult).build();
	}
	
	//	@GET
	//	@Path("history")
	//	@Operation(
	//		summary = "Searches the history for the objects."
	//	)
	//	@APIResponse(
	//		responseCode = "200",
	//		description = "Blocks retrieved.",
	//		content = {
	//			@Content(
	//				mediaType = "application/json",
	//				schema = @Schema(
	//					type = SchemaType.ARRAY,
	//					implementation = ObjectHistoryEvent.class
	//				)
	//			)
	//		},
	//		headers = {
	//			@Header(name = "num-elements", description = "Gives the number of elements returned in the body."),
	//			@Header(name = "query-num-results", description = "Gives the number of results in the query given.")
	//		}
	//	)
	//	@Produces(MediaType.APPLICATION_JSON)
	//	@RolesAllowed(UserRoles.INVENTORY_VIEW)
	@WithSpan
	public SearchResult<ObjectHistoryEvent> searchHistory(
		@BeanParam HistorySearch searchObject
	) {
		log.info("Searching for objects with: {}", searchObject);
		
		return this.getFileObjectService().getFileObjectService().searchHistory(searchObject, false);
	}
	//</editor-fold>
}
