package tech.ebp.oqm.baseStation.testResources.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.service.mongo.MongoHistoriedService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TestMongoHistoriedService extends MongoHistoriedService<TestMainObject, TestMainObjectSearch> {
	
	TestMongoHistoriedService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	TestMongoHistoriedService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
			String database
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			TestMainObject.class,
			false
		);
	}
}
