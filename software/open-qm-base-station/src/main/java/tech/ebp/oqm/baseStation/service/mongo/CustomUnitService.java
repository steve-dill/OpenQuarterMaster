package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.rest.search.CustomUnitSearch;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.lib.core.units.CustomUnitEntry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.measure.Unit;
import java.util.List;

@Traced
@Slf4j
@ApplicationScoped
public class CustomUnitService extends MongoHistoriedObjectService<CustomUnitEntry, CustomUnitSearch> {
	
	CustomUnitService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	CustomUnitService(
		//            Validator validator,
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			CustomUnitEntry.class,
			false
		);
	}
	
	@Override
	public void ensureObjectValid(boolean newObject, CustomUnitEntry newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
		//TODO:: ensure name,symbol, tostring? not same as any in default set or held
	}
	
	public long getNextOrderValue() {
		CustomUnitEntry entry = this.listIterator(null, Sorts.descending("order"), null).first();
		
		if (entry == null) {
			return 0;
		}
		return entry.getOrder() + 1L;
	}
	
	public CustomUnitEntry getFromUnit(ClientSession clientSession, Unit unit) {
		List<CustomUnitEntry> matchList = this.list(
			clientSession,
			Filters.eq("unitCreator.symbol", unit.getSymbol()),
			null,
			null
		);
		
		if(matchList.size() == 0){
			throw new DbNotFoundException("Could not find custom unit " + unit, CustomUnitEntry.class);
		}
		if(matchList.size() != 1){
			throw new DbNotFoundException(
				"Could not find custom unit " + unit + " - Too many matched units (" + matchList.size() + ")",
				CustomUnitEntry.class
			);
		}
		
		return matchList.get(0);
	}
}
