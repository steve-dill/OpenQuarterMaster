package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.rest.search.SearchObject;
import tech.ebp.oqm.core.api.service.serviceState.db.MongoDatabaseService;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmMongoDatabase;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@Slf4j
public abstract class TopLevelMongoService<T extends MainObject> {
	
	public static String getCollectionNameFromClass(Class<?> clazz) {
		return clazz.getSimpleName();
	}
	
	//TODO:: move to constructor?
	protected static final Validator VALIDATOR;
	
	static {
		try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
			VALIDATOR = validatorFactory.getValidator();
		}
	}
	
	/**
	 * Mapper to help deal with json updates.
	 */
	@Inject
	@Getter(AccessLevel.PROTECTED)
	ObjectMapper objectMapper;
	/**
	 * The MongoDb client.
	 */
	@Inject
	@Getter(AccessLevel.PROTECTED)
	MongoClient mongoClient;
	
	/**
	 * The name of the database to access
	 */
	@Getter
	@ConfigProperty(name = "quarkus.mongodb.database")
	String databasePrefix;
	
	/**
	 * The class this collection is in charge of. Used for logging.
	 */
	@Getter
	protected final Class<T> clazz;
	
	/**
	 * The actual mongo collection.
	 */
	private MongoCollection<T> collection = null;
	
	
	protected TopLevelMongoService(
		Class<T> clazz
	){
		this.clazz = clazz;
	}
	
	/**
	 * TODO::
	 * @return
	 */
	protected MongoDatabase getMongoDatabase(){
		return this.getMongoClient().getDatabase(this.databasePrefix);
	}
	
	/**
	 * Gets the collection for this service.
	 * <p>
	 * Sets up the collection object if not initialized yet.
	 *
	 * @return The Mongo collection for this service.
	 */
	protected MongoCollection<T> getCollection() {
		if (this.collection == null) {
			this.collection = this.getMongoDatabase().getCollection(getCollectionNameFromClass(this.clazz), this.clazz);
		}
		return this.collection;
	}
	
	protected MongoCollection<T> getCollection(OqmMongoDatabase db) {
		//TODO
		return null;
	}
	
	public static TransactionOptions getDefaultTransactionOptions() {
		return TransactionOptions.builder()
								 .readPreference(ReadPreference.primary())
								 .readConcern(ReadConcern.LOCAL)
								 .writeConcern(WriteConcern.MAJORITY)
								 .build();
	}
	
	@WithSpan
	public ClientSession getNewClientSession(boolean startTransaction) {
		ClientSession clientSession = this.getMongoClient().startSession();
		
		if(startTransaction){
			clientSession.startTransaction();
		}
		
		return clientSession;
	}
	
	public ClientSession getNewClientSession() {
		return this.getNewClientSession(false);
	}
	
}
