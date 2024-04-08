package tech.ebp.oqm.core.api.service.serviceState.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.service.mongo.TopLevelMongoService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@ApplicationScoped
public class MongoDatabaseService extends TopLevelMongoService<OqmMongoDatabase> {
	
	@Getter
	@Setter(AccessLevel.PRIVATE)
	private DbCache databaseCache = null;
	
	/**
	 * The name of the database to access
	 */
	@Getter
	@ConfigProperty(name = "quarkus.mongodb.database")
	String databasePrefix;
	
	/**
	 * The actual mongo collection.
	 */
	private MongoCollection<OqmMongoDatabase> collection = null;
	
	protected MongoDatabaseService() {
		super(OqmMongoDatabase.class);
	}
	
	/**
	 * Gets the collection for this service.
	 * <p>
	 * Sets up the collection object if not initialized yet.
	 *
	 * @return The Mongo collection for this service.
	 */
	protected MongoCollection<OqmMongoDatabase> getCollection() {
		if (this.collection == null) {
			this.collection = this.getMongoDatabase().getCollection(OqmMongoDatabase.class.getSimpleName(), OqmMongoDatabase.class);
		}
		return this.collection;
	}
	
	
	public void refreshCache(){
		log.info("Refreshing cache of databases.");
		this.setDatabaseCache(new DbCache(this.getMongoClient(), this.getCollection().find(), this.getDatabaseCache()));
	}
	
	public List<DbCacheEntry> getDatabases(){
		return this.getDatabaseCache().getDbCache();
	}
	
	private DbCacheEntry getOqmDatabase(@NonNull String idOrName, boolean refreshedCache){
		Optional<DbCacheEntry> cacheResult = this.getDatabaseCache().getFromIdOrName(idOrName);
		
		if(cacheResult.isEmpty()){
			if(!refreshedCache){
				log.info("Cache miss! Refreshing cache.");
				this.refreshCache();
				log.info("Cache miss after refresh! Database with name or id \"{}\" not found.", idOrName);
				return this.getOqmDatabase(idOrName, true);
			}
			throw new NotFoundException("Database not found with name or id \"" + idOrName + "\"");
		}
		
		return cacheResult.get();
	}
	
	public DbCacheEntry getOqmDatabase(@NonNull String idOrName){
		return this.getOqmDatabase(idOrName, false);
	}
	
	public ObjectId addOqmDatabase(@NonNull String name, @Nullable Set<String> userIds){
		OqmMongoDatabase newDatabase = OqmMongoDatabase.builder()
										   .name(name)
										   .usersAllowed(userIds)
										   .build();
		
		boolean dbNameExists = this.getCollection().find(Filters.eq("name", newDatabase.getName())).into(new ArrayList<>()).isEmpty();
		if(dbNameExists){
			//TODO:: better exception
			throw new IllegalArgumentException("Database with name \""+newDatabase.getName()+"\" already exists.");
		}
		
		return this.getCollection().insertOne(newDatabase).getInsertedId().asObjectId().getValue();
	}
	
	public MongoDatabase getMongoDatabase(@NonNull String idOrName){
	
	}
}
