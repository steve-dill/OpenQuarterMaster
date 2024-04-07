package tech.ebp.oqm.core.api.service.mongo.media;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.FileMainObject;
import tech.ebp.oqm.core.api.rest.search.FileSearchObject;
import tech.ebp.oqm.core.api.service.mongo.MongoHistoriedObjectService;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;

/**
 * This is the standard impl of the MongoHistoriedObjectService used to store T.
 *
 * TODO:: figure out what this should actually extend
 */
public class FileObjectService<T extends FileMainObject, S extends FileSearchObject<T>> extends MongoHistoriedObjectService<T, S, CollectionStats> {
	
	private String collectionName;
	
	public FileObjectService(
		Class<T> clazz,
		String collectionName,
		HistoryEventNotificationService hens
	) {
		super(
			clazz,
			false,
			hens
		);
		this.collectionName = collectionName;
	}
	
	@Override
	public String getCollectionName() {
		return this.collectionName;
	}
	
	/**
	 * TODO:: this shoudl be its own class, or at least be capable of so.
	 * @return
	 */
	@Override
	public CollectionStats getStats() {
		return super.addBaseStats(CollectionStats.builder())
				   .build();
	}
}