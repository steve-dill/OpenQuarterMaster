package com.ebp.openQuarterMaster.baseStation.service.productLookup.searchServices.api;

import com.fasterxml.jackson.databind.JsonNode;
import tech.ebp.oqm.lib.core.rest.externalItemLookup.ExtItemLookupProviderInfo;
import tech.ebp.oqm.lib.core.rest.externalItemLookup.ExtItemLookupResult;

import java.util.List;

public abstract class ItemSearchService {
	
	public abstract ExtItemLookupProviderInfo getProviderInfo();
	
	public abstract boolean isEnabled();
	
	public abstract List<ExtItemLookupResult> jsonNodeToSearchResults(JsonNode results);
}
