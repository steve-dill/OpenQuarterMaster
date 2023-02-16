package tech.ebp.oqm.baseStation.service.mongo.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSDownloadOptions;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonDocumentWriter;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.TempFileService;
import tech.ebp.oqm.baseStation.service.mongo.MongoObjectService;
import tech.ebp.oqm.baseStation.service.mongo.MongoService;
import tech.ebp.oqm.baseStation.service.mongo.utils.FileContentsGet;
import tech.ebp.oqm.lib.core.object.FileMainObject;
import tech.ebp.oqm.lib.core.object.media.FileMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class MongoFileService<T extends FileMainObject, S extends SearchObject<T>> extends MongoService<T, S> {
	
	GridFSBucket gridFSBucket = null;
	@Getter(AccessLevel.PRIVATE)
	Codec<FileMetadata> fileMetadataCodec;
	@Getter(AccessLevel.PROTECTED)
	private TempFileService tempFileService;
	
	public MongoFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		String collectionName,
		Class<T> clazz,
		MongoCollection<T> collection
	) {
		super(objectMapper, mongoClient, database, collectionName, clazz, collection);
	}
	
	protected MongoFileService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		Class<T> clazz,
		TempFileService tempFileService
	) {
		super(objectMapper, mongoClient, database, clazz);
		this.fileMetadataCodec = this.getDatabase().getCodecRegistry().get(FileMetadata.class);
		this.tempFileService = tempFileService;
	}
	
	@Override
	public String getCollectionName() {
		return super.getCollectionName() + "-grid";
	}
	
	protected GridFSBucket getGridFSBucket() {
		if (this.gridFSBucket == null) {
			this.gridFSBucket = GridFSBuckets.create(this.getDatabase(), this.getCollectionName());
		}
		return this.gridFSBucket;
	}
	
	protected Document metadataToDocument(FileMetadata object) {
		BsonDocument outDoc = new BsonDocument();
		BsonWriter writer = new BsonDocumentWriter(outDoc);
		
		this.getFileMetadataCodec().encode(
			writer,
			object,
			EncoderContext.builder().build()
		);
		
		return new Document(outDoc);
	}
	
	protected FileMetadata documentToMetadata(Document doc) {
		BsonReader reader = new BsonDocumentReader(doc.toBsonDocument());
		
		return this.getFileMetadataCodec().decode(
			reader,
			DecoderContext.builder().build()
		);
	}
	
	protected GridFSUploadOptions getUploadOps(FileMetadata metadata) {
		return new GridFSUploadOptions()
				   .chunkSizeBytes(1048576)
				   .metadata(
					   this.metadataToDocument(metadata)
				   );
	}
	
	public abstract MongoObjectService<T, S> getFileObjectService();
	
	public long count(ClientSession clientSession) {
		return this.getFileObjectService().count(clientSession);
	}
	
	public long count() {
		return this.count(null);
	}
	
	public T getObject(ClientSession clientSession, ObjectId id) {
		return this.getFileObjectService().get(clientSession, id);
	}
	
	public T getObject(ObjectId id) {
		return this.getObject(null, id);
	}
	
	protected String getFileName(ClientSession clientSession, ObjectId id) {
		return this.getObject(clientSession, id).getFileName();
	}
	
	protected Bson getFileNameQuery(ClientSession clientSession, ObjectId id) {
		return Filters.eq("filename", this.getFileName(clientSession, id));
	}
	
	public List<FileMetadata> getRevisions(ClientSession clientSession, ObjectId id) {
		Bson query = this.getFileNameQuery(clientSession, id);
		
		GridFSFindIterable iterable;
		
		if (clientSession == null) {
			iterable = this.getGridFSBucket().find(query);
		} else {
			iterable = this.getGridFSBucket().find(clientSession, query);
		}
		
		List<FileMetadata> output = new ArrayList<>();
		
		iterable.forEach((GridFSFile file)->{
			output.add(this.documentToMetadata(file.getMetadata()));
		});
		
		return output;
	}
	
	public List<FileMetadata> getRevisions(ObjectId id) {
		return this.getRevisions(null, id);
	}
	
	public FileMetadata getLatestMetadata(ClientSession clientSession, ObjectId id) {
		Bson query = this.getFileNameQuery(clientSession, id);
		
		GridFSFindIterable iterable;
		
		if (clientSession == null) {
			iterable = this.getGridFSBucket().find(query);
		} else {
			iterable = this.getGridFSBucket().find(clientSession, query);
		}
		
		GridFSFile file = iterable.sort(Sorts.descending("uploadDate")).limit(1).first();
		
		return this.documentToMetadata(file.getMetadata());
	}
	
	public FileMetadata getLatestMetadata(ObjectId id) {
		return this.getLatestMetadata(null, id);
	}
	
	protected File downloadGridfsFile(ClientSession clientSession, String filename, String tempFilename, GridFSDownloadOptions options) throws IOException {
		File tempFile = this.getTempFileService().getTempFile(tempFilename, this.getCollectionName());
		
		if (!tempFile.exists()) {
			log.info("File needs added to cache: {}", tempFile);
			try (FileOutputStream os = new FileOutputStream(tempFile)) {
				if (clientSession == null) {
					this.getGridFSBucket().downloadToStream(filename, os, options);
				} else {
					this.getGridFSBucket().downloadToStream(clientSession, filename, os, options);
				}
				
				os.flush();
			} catch(Throwable e) {
				tempFile.delete();
				throw e;
			}
		} else {
			log.info("File already exists in fs cache: {}", tempFile);
		}
		return tempFile;
	}
	
	private String getTempFileName(String fileName, int revision) {
		return FilenameUtils.removeExtension(fileName) + "-" + revision + FilenameUtils.getExtension(fileName);
	}
	
	/**
	 * TODO::
	 *
	 * @param clientSession
	 * @param id
	 *
	 * @return
	 * @throws IOException
	 */
	public FileContentsGet getLatestFile(ClientSession clientSession, ObjectId id) throws IOException {
		T fileObj = this.getObject(clientSession, id);
		
		FileContentsGet.FileContentsGetBuilder<?, ?> outputBuilder = FileContentsGet.builder();
		
		List<FileMetadata> revisions = this.getRevisions(clientSession, id);
		int latestRev = revisions.size() - 1;
		
		FileMetadata metadata = revisions.get(latestRev);
		outputBuilder.metadata(metadata);
		
		GridFSDownloadOptions ops = new GridFSDownloadOptions().revision(latestRev);
		
		outputBuilder.contents(this.downloadGridfsFile(clientSession, fileObj.getFileName(), this.getTempFileName(fileObj.getFileName(), latestRev), ops));
		
		return outputBuilder.build();
	}
	
	public FileContentsGet getLatestFile(ObjectId id) throws IOException {
		return this.getLatestFile(null, id);
	}
	
	public FileContentsGet getLatestFile(String id) throws IOException {
		return this.getLatestFile(null, new ObjectId(id));
	}
	
	
}
