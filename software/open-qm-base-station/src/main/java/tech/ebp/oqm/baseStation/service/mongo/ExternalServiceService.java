package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import io.quarkus.security.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.config.ExtServicesConfig;
import tech.ebp.oqm.baseStation.rest.search.ExternalServiceSearch;
import tech.ebp.oqm.baseStation.service.JwtService;
import tech.ebp.oqm.baseStation.service.PasswordService;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.baseStation.utils.AuthMode;
import tech.ebp.oqm.lib.core.object.externalService.ExternalService;
import tech.ebp.oqm.lib.core.rest.externalService.ExternalServiceSetupRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static com.mongodb.client.model.Filters.eq;

@Traced
@Slf4j
@ApplicationScoped
public class ExternalServiceService extends MongoHistoriedService<ExternalService, ExternalServiceSearch> {
	//    private Validator validator;
	private AuthMode authMode;
	private PasswordService passwordService;
	ExtServicesConfig extServicesConfig;
	
	ExternalServiceService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	ExternalServiceService(
		//            Validator validator,
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
			String database,
		@ConfigProperty(name = "service.authMode")
		AuthMode authMode,
		PasswordService passwordService,
		ExtServicesConfig extServicesConfig
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			ExternalService.class,
			true
		);
		this.authMode = authMode;
		this.passwordService = passwordService;
		this.extServicesConfig = extServicesConfig;
		//        this.validator = validator;
	}
	
	@Override
	public void ensureObjectValid(boolean newObject, ExternalService newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
		//TODO:: name not existant
	}
	
	private ExternalService getExternalService(String externalSource, String externalId) {
		if (externalId == null) {
			return null;
		}
		return this.getCollection().find(eq("externIds." + externalSource, externalId)).limit(1).first();
	}
	
	private ExternalService getExternalService(JsonWebToken jwt) {
		String externalSource = jwt.getIssuer();
		String externalId = jwt.getClaim(Claims.sub);
		log.debug("User id from external jwt: {}", externalId);
		ExternalService externalService = this.getExternalService(externalSource, externalId);
		
		if (externalService != null) {
			//TODO:: update from given jwt, if needed?
			return externalService;
		}
		throw new DbNotFoundException("Make sure the calling service hit the setup endpoint first.", ExternalService.class, null);
	}
	
	
	public ExternalService getFromJwt(JsonWebToken jwt) {
		//TODO:: check is user?
		switch (this.authMode) {
			case SELF:
				log.debug("Getting service data from self.");
				String extServiceId = jwt.getClaim(JwtService.JWT_SERVICE_ID_CLAIM);
				if(extServiceId == null){
					return null;
				}
				try {
					return this.get(extServiceId);
				} catch(DbNotFoundException e){
					throw new UnauthorizedException("Service in JWT not found.");
				}
			case EXTERNAL:
				log.debug("Getting external service data ");
				return this.getExternalService(jwt);
		}
		return null;
	}
	
	public ExternalService getFromServiceName(String name){
		ExternalService service = this.getCollection().find(Filters.eq("name", name)).limit(1).first();
		
		if(service == null){
			throw new DbNotFoundException("No service found with name \"" + name + "\"", this.getClazz(), null);
		}
		
		return service;
	}
	
	public ExternalService getFromSetupRequest(ExternalServiceSetupRequest setupRequest){
		ExternalService existentExtService;
		try {
			existentExtService = this.getFromServiceName(setupRequest.getName());
			
			//TODO:: check if needs updated
		} catch(DbNotFoundException e){
			existentExtService = setupRequest.toExtService();
			
			this.add(existentExtService);
		}
		
		return existentExtService;
	}
	
}
