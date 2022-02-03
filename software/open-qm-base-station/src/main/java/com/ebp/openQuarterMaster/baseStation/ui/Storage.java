package com.ebp.openQuarterMaster.baseStation.ui;

import com.ebp.openQuarterMaster.baseStation.restCalls.KeycloakServiceCaller;
import com.ebp.openQuarterMaster.baseStation.service.mongo.StorageBlockService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.UserService;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.*;
import com.ebp.openQuarterMaster.lib.core.UnitUtils;
import com.ebp.openQuarterMaster.lib.core.rest.user.UserGetResponse;
import com.ebp.openQuarterMaster.lib.core.storage.storageBlock.StorageBlock;
import com.ebp.openQuarterMaster.lib.core.storage.stored.StoredType;
import com.ebp.openQuarterMaster.lib.core.user.User;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import lombok.extern.slf4j.Slf4j;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;

import static com.ebp.openQuarterMaster.baseStation.ui.UiUtils.getLoadTimestamp;

@Traced
@Slf4j
@Path("/")
@Tags({@Tag(name = "UI")})
@RequestScoped
@Produces(MediaType.TEXT_HTML)
public class Storage extends UiProvider {

    @Inject
    @Location("webui/pages/storage")
    Template storage;

    @Inject
    UserService userService;

    @Inject
    StorageBlockService storageBlockService;

    @Inject
    JsonWebToken jwt;
    @Inject
    @RestClient
    KeycloakServiceCaller ksc;

    @GET
    @Path("storage")
    @RolesAllowed("user")
    @Produces(MediaType.TEXT_HTML)
    public Response storage(
            @Context SecurityContext securityContext,
            //for actual queries
            @QueryParam("label") String label,
            @QueryParam("location") String location,
            @QueryParam("parentLabel") List<String> parents,
            //capacities
            @QueryParam("capacity") List<Integer> capacities,
            @QueryParam("unit") List<String> units,
            //TODO
            @QueryParam("stores") List<ObjectId> stores,
            @QueryParam("storedType") StoredType storedType,
            //attKeywords
            @QueryParam("keyword") List<String> keywords,
            @QueryParam("attributeKey") List<String> attributeKeys,
            @QueryParam("attributeValue") List<String> attributeValues,
            //paging
            @QueryParam("pageSize") Integer pageSize,
            @QueryParam("pageNum") Integer pageNum,
            //sorting
            @QueryParam("sortBy") String sortField,
            @QueryParam("sortType") SortType sortType,
            @CookieParam("jwt_refresh") String refreshToken
    ) {
        logRequestContext(jwt, securityContext);
        User user = userService.getFromJwt(this.jwt);
        List<NewCookie> newCookies = UiUtils.getExternalAuthCookies(refreshAuthToken(ksc, refreshToken));

        Bson sort = SearchUtils.getSortBson(sortField, sortType);
        PagingOptions pageOptions = PagingOptions.fromQueryParams(pageSize, pageNum, true);

        SearchResult<StorageBlock> searchResults = this.storageBlockService.search(
                label,
                location,
                parents,
                SearchUtils.capacityListsToMap(capacities, units),
                stores,
                keywords,
                SearchUtils.attListsToMap(attributeKeys, attributeValues),
                sort,
                pageOptions
        );

        Response.ResponseBuilder responseBuilder = Response.ok(
                storage
                        .data("pageLoadTimestamp", getLoadTimestamp())
                        .data("allowedUnitsMap", UnitUtils.ALLOWED_UNITS_MAP)
                        .data(USER_INFO_DATA_KEY, UserGetResponse.builder(user).build())
                        .data("showSearch", searchResults.isHadSearchQuery())
                        .data("numStorageBlocks", storageBlockService.count())
                        .data("searchResult", searchResults)
                        .data("pagingCalculations", new PagingCalculations(pageOptions, searchResults))
                        .data("storageService", storageBlockService),
                MediaType.TEXT_HTML_TYPE
        );

        if(newCookies != null && !newCookies.isEmpty()){
            responseBuilder.cookie(newCookies.toArray(new NewCookie[]{}));
        }

        return responseBuilder.build();
    }

}
