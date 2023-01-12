package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.rest.search.HistorySearch;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbHistoryNotFoundException;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.ObjectUtils;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.lib.core.object.history.events.CreateEvent;
import tech.ebp.oqm.lib.core.object.history.events.DeleteEvent;
import tech.ebp.oqm.lib.core.object.history.events.UpdateEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;

import static com.mongodb.client.model.Filters.eq;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@Slf4j
@Traced
public class MongoHistoryService<T extends MainObject> extends MongoService<ObjectHistoryEvent, HistorySearch> {
	
	public static final String COLLECTION_HISTORY_APPEND = "-history";
	
	private final Class<T> clazzForObjectHistoryIsFor;
	
	public MongoHistoryService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		Class<T> clazz
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			getCollectionNameFromClass(clazz) + COLLECTION_HISTORY_APPEND,
			ObjectHistoryEvent.class,
			null
		);
		this.clazzForObjectHistoryIsFor = clazz;
	}
	
	public ObjectHistoryEvent getHistoryFor(ClientSession clientSession, ObjectId id) {
		ObjectHistoryEvent found;
		if (clientSession != null) {
			found = getCollection()
						.find(clientSession, eq("objectId", id))
						.limit(1)
						.first();
		} else {
			found = getCollection()
						.find(eq("objectId", id))
						.limit(1)
						.first();
		}
		if (found == null) {
			throw new DbHistoryNotFoundException(this.clazzForObjectHistoryIsFor, id);
		}
		return found;
	}
	
	public ObjectHistoryEvent getHistoryFor(ObjectId id) {
		return this.getHistoryFor(null, id);
	}
	
	public ObjectHistoryEvent getHistoryFor(ClientSession clientSession, T object) {
		return this.getHistoryFor(clientSession, object.getId());
	}
	
	public ObjectHistoryEvent getHistoryFor(T object) {
		return this.getHistoryFor(null, object);
	}
	
	public ObjectId objectCreated(ClientSession session, T created, InteractingEntity entity) {
		try {
			this.getHistoryFor(session, created);
			throw new IllegalStateException(
				"History already exists for object " + this.clazzForObjectHistoryIsFor.getSimpleName() + " with id: " + created.getId()
			);
		} catch(DbNotFoundException e) {
			// no history record should exist.
		}
		
		ObjectHistoryEvent history = new CreateEvent(created, entity);
		
		return this.add(session, history);
	}
	
	public ObjectId objectCreated(T created, InteractingEntity interactingEntity) {
		return this.objectCreated(null, created, interactingEntity);
	}
	
	public ObjectId objectUpdated(
		ClientSession clientSession,
		T updated,
		InteractingEntity entity,
		ObjectNode updateJson,
		String description
	) {
		
		UpdateEvent event = new UpdateEvent(updated, entity);
		
		if (updateJson != null) {
			event.setFieldsUpdated(ObjectUtils.fieldListFromJson(updateJson));
		}
		
		return this.add(clientSession, event);
	}
	
	public ObjectId objectUpdated(T updated, InteractingEntity entity, ObjectNode updateJson, String description) {
		return this.objectUpdated(null, updated, entity, updateJson, description);
	}
	
	
	public ObjectId objectUpdated(ClientSession clientSession, T updated, InteractingEntity entity, ObjectNode updateJson) {
		return this.objectUpdated(clientSession, updated, entity, updateJson, "");
	}
	
	public ObjectId objectUpdated(T updated, InteractingEntity entity, ObjectNode updateJson) {
		return this.objectUpdated(null, updated, entity, updateJson);
	}
	
	public ObjectId objectDeleted(ClientSession clientSession, T updated, InteractingEntity entity, String description) {
		DeleteEvent event = (DeleteEvent) new DeleteEvent(updated, entity)
											  .setDescription(description);
		
		return this.add(clientSession, event);
	}
	
	public ObjectId objectDeleted(T updated, InteractingEntity entity, String description) {
		return this.objectDeleted(null, updated, entity, description);
	}
	
	public ObjectId objectDeleted(ClientSession clientSession, T updated, InteractingEntity entity) {
		return this.objectDeleted(clientSession, updated, entity, "");
	}
	
	public ObjectId objectDeleted(T updated, InteractingEntity entity) {
		return this.objectDeleted(null, updated, entity);
	}
}
