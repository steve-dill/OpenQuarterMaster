package tech.ebp.oqm.baseStation.utils;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.bson.assertions.Assertions;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.baseStation.testResources.lifecycleManagers.TestResourceLifecycleManager;
import tech.ebp.oqm.baseStation.testResources.testClasses.RunningServerTest;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@QuarkusTest
@QuarkusTestResource(TestResourceLifecycleManager.class)
class TempFileServiceTest extends RunningServerTest {
	
	@Inject
	TempFileService tempFileService;
	
	@Test
	public void testGetTempFile() throws IOException {
		String ext = "txt";
		String prefix = "testFile";
		
		File tempFile = this.tempFileService.getTempFile(prefix, ext);
		
		log.info("Temp file gotten: {}", tempFile);
		
		assertTrue(tempFile.getName().startsWith(prefix));
		assertTrue(FilenameUtils.isExtension(tempFile.getName(), ext));
		
		if(!tempFile.createNewFile()){
			Assertions.fail("Failed to create new file.");
		}
	}
	
	@Test
	public void testGetTwoSameTempFile() throws IOException {
		String ext = "txt";
		String prefix = "testFile";
		
		File tempFileOne = this.tempFileService.getTempFile(prefix, ext);
		File tempFileTwo = this.tempFileService.getTempFile(prefix, ext);
		
		assertNotEquals(tempFileOne, tempFileTwo);
	}
	
}