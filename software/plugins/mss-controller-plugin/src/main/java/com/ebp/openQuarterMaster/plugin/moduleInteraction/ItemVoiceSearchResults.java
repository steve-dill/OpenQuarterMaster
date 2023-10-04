package com.ebp.openQuarterMaster.plugin.moduleInteraction;

import com.ebp.openQuarterMaster.plugin.restClients.searchObj.InventoryItemSearch;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemVoiceSearchResults extends ItemSearchResults {
	private ObjectNode voiceProcessingResult;
	private InventoryItemSearch inventoryItemSearch;
	
	public static ItemVoiceSearchResults from(
		ItemSearchResults itemSearchResults,
		ObjectNode voiceProcessingResult,
		InventoryItemSearch inventoryItemSearch
	){
		ItemVoiceSearchResults output = new ItemVoiceSearchResults();
		
		output.setVoiceProcessingResult(voiceProcessingResult);
		output.setWithModuleBlocks(itemSearchResults.getWithModuleBlocks());
		output.setWithoutModuleBlocks(itemSearchResults.getWithoutModuleBlocks());
		output.setInventoryItemSearch(inventoryItemSearch);
		
		return output;
	}
}
