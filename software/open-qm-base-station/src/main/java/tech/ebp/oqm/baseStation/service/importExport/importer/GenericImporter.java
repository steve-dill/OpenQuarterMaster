package tech.ebp.oqm.baseStation.service.importExport.importer;

import com.mongodb.client.ClientSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.object.ObjectUtils;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.mongo.MongoHistoriedObjectService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
public class GenericImporter<T extends MainObject, S extends SearchObject<T>> extends ObjectImporter<T, S, MongoHistoriedObjectService<T, S>> {
	
	
	public GenericImporter(MongoHistoriedObjectService<T, S> mongoService) {
		super(mongoService);
	}
	
	private void readInObject(
		ClientSession clientSession,
		File curFile,
		InteractingEntity importingEntity
	) throws IOException {
		try {
			T curObj = ObjectUtils.OBJECT_MAPPER.readValue(curFile, this.getObjectService().getClazz());
			
			ObjectId oldId = curObj.getId();
			ObjectId newId;
			try {
				newId = this.getObjectService().add(clientSession, curObj, importingEntity);
			} catch(Throwable e) {
				log.error("Failed to import object: ", e);
				throw e;
			}
			log.info("Read in object. new id == old? {}", newId.equals(oldId));
			assert newId.equals(oldId); //TODO:: better check?
			
		} catch(Throwable e){
			log.error("Failed to process object file {}: ", curFile, e);
			throw e;
		}
	}
	
	@Override
	public long readInObjects(
		ClientSession clientSession,
		Path directory,
		InteractingEntity importingEntity
	) throws IOException {
		Path objectDirPath = this.getObjDirPath(directory);
		
		if(!Files.exists(objectDirPath)){
			return 0;
		}
		
		List<File> filesForObject = getObjectFiles(objectDirPath);
		
		log.info("Found {} files for {} in {}", filesForObject.size(), this.getObjectService().getCollectionName(), objectDirPath);
		StopWatch sw = StopWatch.createStarted();
		for (File curObjFile : filesForObject) {
			this.readInObject(clientSession, curObjFile, importingEntity);
		}
		
		sw.stop();
		log.info(
			"Read in {} {} objects in {}",
			filesForObject.size(),
			this.getObjectService().getCollectionName(),
			sw
		);
		
		return filesForObject.size();
	}
	
	
}