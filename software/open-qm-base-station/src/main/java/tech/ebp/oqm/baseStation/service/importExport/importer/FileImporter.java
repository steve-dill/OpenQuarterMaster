package tech.ebp.oqm.baseStation.service.importExport.importer;

import com.mongodb.client.ClientSession;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tech.ebp.oqm.baseStation.model.object.FileMainObject;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.mongo.file.MongoHistoriedFileService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public abstract class FileImporter<T extends FileMainObject, S extends SearchObject<T>, M extends MongoHistoriedFileService<T, S>> {
	
	protected static List<File> getObjectFiles(Path directory) throws IOException {
		try (
			Stream<Path> paths = Files.walk(
				directory,
				1
			)
		) {
			return paths
					   .filter(Files::isRegularFile)
					   .filter((Path path)->{
						   return path.toString().endsWith(".json");
					   })
					   .map(Path::toFile)
					   .collect(Collectors.toList());
		}
	}
	
	@Getter
	private final M objectService;
	
	protected FileImporter(M mongoService){
		this.objectService = mongoService;
	}
	
	protected Path getObjDirPath(Path directory){
		return directory.resolve(this.getObjectService().getCollectionName());
	}
	
	public abstract long readInObjects(
		ClientSession clientSession,
		Path directory,
		InteractingEntity importingEntity
	) throws IOException;
}