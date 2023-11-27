package tech.ebp.oqm.baseStation.service.mongo.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.interfaces.endpoints.media.FileGet;
import tech.ebp.oqm.baseStation.model.object.FileMainObject;
import tech.ebp.oqm.baseStation.model.object.history.events.UpdateEvent;
import tech.ebp.oqm.baseStation.model.object.history.events.file.NewFileVersionEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.model.object.media.FileHashes;
import tech.ebp.oqm.baseStation.model.object.media.FileMetadata;
import tech.ebp.oqm.baseStation.rest.file.FileUploadBody;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.TempFileService;
import tech.ebp.oqm.baseStation.service.mongo.MongoHistoriedObjectService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@Slf4j
public abstract class MongoHistoriedFileService<T extends FileMainObject, S extends SearchObject<T>, G extends FileGet> extends MongoFileService<T, S, G> {
	
	public static final String NULL_USER_EXCEPT_MESSAGE = "User must exist to perform action.";
	
	/**
	 * TODO:: check if real user. Get userService in constructor?
	 * TODO:: real exception
	 *
	 * @param interactingEntity
	 */
	private static void assertNotNullEntity(InteractingEntity interactingEntity) {
		if (interactingEntity == null) {
			throw new IllegalArgumentException(NULL_USER_EXCEPT_MESSAGE);
		}
		//TODO:: check has id
	}
	
	@Getter
	protected final boolean allowNullEntityForCreate;
	@Getter
	private MongoHistoriedObjectService<T, S> fileObjectService = null;
	
	public MongoHistoriedFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		String collectionName,
		Class<T> clazz,
		MongoCollection<T> collection,
		boolean allowNullEntityForCreate,
		MongoHistoriedObjectService<T, S> fileMetadataService
	) {
		super(objectMapper, mongoClient, database, collectionName, clazz, collection);
		this.allowNullEntityForCreate = allowNullEntityForCreate;
		this.fileObjectService = fileMetadataService;
	}
	
	protected MongoHistoriedFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		Class<T> metadataClazz,
		boolean allowNullEntityForCreate,
		TempFileService tempFileService
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			metadataClazz,
			tempFileService
		);
		this.allowNullEntityForCreate = allowNullEntityForCreate;
		this.fileObjectService =
			new FileObjectService(
				objectMapper,
				mongoClient,
				database,
				metadataClazz
			);
	}
	
	protected MongoHistoriedFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		Class<T> metadataClazz,
		boolean allowNullEntityForCreate,
		TempFileService tempFileService,
		MongoHistoriedObjectService<T, S> historiedObjectService
	) {
		this(
			objectMapper,
			mongoClient,
			database,
			metadataClazz,
			allowNullEntityForCreate,
			tempFileService
		);
		this.fileObjectService = historiedObjectService;
	}
	
	@PostConstruct
	public void setup(){
		// should probably be a TODO to remove this, but unsure how we ever might be able to.
		//ensure gridfs bucket storage is initialized. Required to avoid trying to create during a transaction, which is unsupported by Mongodb.
		if(this.getGridFSBucket().find().limit(1).first() == null){
			FileMetadata metadata = new FileMetadata(
				"init file, disregard",
				0,
				FileHashes.builder().md5("").sha1("").sha256("").build(),
				FileMetadata.TIKA.detect("plain"),
				ZonedDateTime.now()
			);

			GridFSUploadOptions ops = this.getUploadOps(metadata);
			GridFSBucket bucket = this.getGridFSBucket();
			String filename = "init";

			bucket.uploadFromStream(filename, new ByteArrayInputStream("".getBytes()), ops);
		}
		
	}
	
	/**
	 * This is the standard impl of the MongoHistoriedObjectService used to store T.
	 */
	private class FileObjectService extends MongoHistoriedObjectService<T, S> {
		FileObjectService() {//required for DI
			super(null, null, null, null, null, null, false, null);
		}
		
		FileObjectService(
			ObjectMapper objectMapper,
			MongoClient mongoClient,
			String database,
			Class<T> clazz
		) {
			super(
				objectMapper,
				mongoClient,
				database,
				clazz,
				false
			);
			//        this.validator = validator;
		}
	}
	
	@WithSpan
	public ObjectId add(ClientSession clientSession, T fileObject, File file, String fileName, InteractingEntity interactingEntity) throws IOException {
		FileMetadata fileMetadata = new FileMetadata(file);
		fileMetadata.setOrigName(FilenameUtils.getName(fileName));
		
		try (
			InputStream is = new FileInputStream(file)
		) {
			return this.add(
				clientSession,
				fileObject,
				fileMetadata,
				is,
				interactingEntity
			);
		}
	}
	
	public ObjectId add(ClientSession clientSession, T fileObject, File file, InteractingEntity interactingEntity) throws IOException {
		return this.add(clientSession, fileObject, file, file.getName(), interactingEntity);
	}
	
	@WithSpan
	public ObjectId add(ClientSession clientSession, T fileObject, FileUploadBody uploadBody, InteractingEntity interactingEntity) throws IOException {
		File tempFile = this.getTempFileService().getTempFile(
			FilenameUtils.removeExtension(fileObject.getFileName()),
			FilenameUtils.getExtension(fileObject.getFileName()),
			"uploads"
		);
		
		FileUtils.copyInputStreamToFile(uploadBody.file, tempFile);
		
		ObjectId id = this.add(clientSession, fileObject, tempFile, uploadBody.fileName, interactingEntity);
		
		if (!tempFile.delete()) {
			log.warn("Failed to delete temporary upload file: {}", tempFile);
		}
		
		return id;
	}
	
	public ObjectId add(T fileObject, FileUploadBody uploadBody, InteractingEntity interactingEntity) throws IOException {
		return this.add(null, fileObject, uploadBody, interactingEntity);
	}
	
	public ObjectId add(T fileObject, File file, InteractingEntity interactingEntity) throws IOException {
		return this.add(null, fileObject, file, interactingEntity);
	}
	
	@WithSpan
	protected ObjectId add(ClientSession clientSession, T fileObject, FileMetadata metadata, InputStream is, InteractingEntity interactingEntity) {
		ObjectId newId = null;
		GridFSBucket bucket = this.getGridFSBucket();
		
		boolean sessionGiven = clientSession != null;
		try (
			ClientSession session = (sessionGiven ? null : this.getNewClientSession(true));
		) {
			if (!sessionGiven) {
				clientSession = session;
			}
			
			newId = this.getFileObjectService().add(clientSession, fileObject, interactingEntity);
			
			GridFSUploadOptions ops = this.getUploadOps(metadata);
			String filename = newId.toHexString();
			
			fileObject.setFileName(filename);
			this.getFileObjectService().update(clientSession, fileObject);
			//TODO:: this breaks: https://jira.mongodb.org/browse/JAVA-4887  #51 once this is done, cleanup other modifying session logic
			bucket.uploadFromStream(clientSession, filename, is, ops);
			
			if (!sessionGiven) {
				clientSession.commitTransaction();
			}
		}
		
		return newId;
	}
	
	/**
	 * @param givenSession
	 * @param id
	 * @param metadata
	 * @param is
	 * @param interactingEntity
	 *
	 * @return
	 */
	@WithSpan
	protected int updateFile(ClientSession givenSession, ObjectId id, FileMetadata metadata, InputStream is, InteractingEntity interactingEntity) {
		T object = this.getFileObjectService().get(id);
		GridFSBucket bucket = this.getGridFSBucket();
		
		GridFSUploadOptions ops = this.getUploadOps(metadata);
		String filename = object.getFileName();
		boolean sessionGiven = givenSession != null;
		if (sessionGiven) {
			bucket.uploadFromStream(givenSession, filename, is, ops);
			this.getFileObjectService().addHistoryFor(givenSession, object, interactingEntity, new NewFileVersionEvent());
			return this.getRevisions(givenSession, id).size() - 1;
		} else {
			try (
				ClientSession ourSession = this.getNewClientSession(true);
			) {
				bucket.uploadFromStream(ourSession, filename, is, ops);
				this.getFileObjectService().addHistoryFor(ourSession, object, interactingEntity, new NewFileVersionEvent());
				ourSession.commitTransaction();
				return this.getRevisions(ourSession, id).size() - 1;
			}
		}
	}
	
	@WithSpan
	public int updateFile(ClientSession clientSession, ObjectId id, File file, InteractingEntity interactingEntity) throws IOException {
		FileMetadata fileMetadata = new FileMetadata(file);
		
		try (
			InputStream is = new FileInputStream(file)
		) {
			return this.updateFile(
				clientSession,
				id,
				fileMetadata,
				is,
				interactingEntity
			);
		}
	}
	
	public int updateFile(ObjectId id, File file, InteractingEntity interactingEntity) throws IOException {
		return this.updateFile(null, id, file, interactingEntity);
	}
	
	@WithSpan
	public int updateFile(ClientSession clientSession, T fileObject, FileUploadBody uploadBody, InteractingEntity interactingEntity) throws IOException {
		File tempFile = this.getTempFileService().getTempFile(
			FilenameUtils.removeExtension(fileObject.getFileName()),
			FilenameUtils.getExtension(fileObject.getFileName()),
			"uploads"
		);
		FileUtils.copyInputStreamToFile(uploadBody.file, tempFile);
		
		int output;
		if(clientSession == null){
			try (
				ClientSession session = this.getFileObjectService().getNewClientSession(true);
			) {
				output = this.updateFile(session, fileObject.getId(), tempFile, interactingEntity);
				this.getFileObjectService().update(session, fileObject, interactingEntity, new UpdateEvent(fileObject, interactingEntity));
			}
		} else {
			output = this.updateFile(clientSession, fileObject.getId(), tempFile, interactingEntity);
			this.getFileObjectService().update(clientSession, fileObject, interactingEntity, new UpdateEvent(fileObject, interactingEntity));
		}
		
		if (!tempFile.delete()) {
			log.warn("Failed to delete temporary upload file: {}", tempFile);
		}
		
		return output;
	}
	
	@WithSpan
	public long removeAll(ClientSession clientSession, InteractingEntity entity) {
		AtomicLong numRemoved = new AtomicLong();
		boolean sessionGiven = clientSession != null;
		if (sessionGiven) {
			this.getFileObjectService().removeAll(clientSession, entity);
			GridFSBucket bucket = this.getGridFSBucket();
			bucket.find(clientSession).forEach((GridFSFile curFile)->{
				bucket.delete(clientSession, curFile.getId());
			});
			this.getFileObjectService().removeAll(clientSession, entity);
		} else {
			try (
				ClientSession innerSession = this.getNewClientSession(true)
			) {
				this.getFileObjectService().removeAll(innerSession, entity);
				GridFSBucket bucket = this.getGridFSBucket();
				bucket.find(innerSession).forEach((GridFSFile curFile)->{
					bucket.delete(innerSession, curFile.getId());
				});
				this.getFileObjectService().removeAll(innerSession, entity);
				innerSession.commitTransaction();
			}
		}
		
		return numRemoved.get();
	}
	
	public T removeFile(ClientSession cs, ObjectId objectId, InteractingEntity entity){
		T toRemove = this.getFileObjectService().get(cs, objectId);
		
		this.assertNotReferenced(cs, toRemove);
		GridFSBucket bucket = this.getGridFSBucket();
		
		if(cs == null){
			try(ClientSession clientSession = this.getNewClientSession(true)){
				bucket.find(clientSession, Filters.eq("filename", toRemove.getFileName())).forEach(
					(GridFSFile file)->{
						bucket.delete(clientSession, file.getId());
					}
				);
				this.getFileObjectService().remove(clientSession, toRemove.getId(), entity);
				clientSession.commitTransaction();
			}
		}else {
			bucket.find(cs, Filters.eq("filename", toRemove.getFileName())).forEach(
				(GridFSFile file)->{
					bucket.delete(cs, file.getId());
				}
			);
			this.getFileObjectService().remove(cs, toRemove.getId(), entity);
		}
		
		return toRemove;
	}
}