package tech.ebp.oqm.baseStation.testResources.data;

import tech.ebp.oqm.lib.core.object.storage.storageBlock.StorageBlock;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;

@ApplicationScoped
public class StorageBlockTestObjectCreator extends TestObjectCreator<StorageBlock> {
	private static final String TEST_IMAGE = "/testFiles/test_image.png";
	
	@Override
	public StorageBlock getTestObject() {
		StorageBlock block = new StorageBlock(
			faker.name().fullName(),
			faker.name().fullName(),
			faker.lorem().paragraph(),
			faker.locality().localeString(),
			null,
			new ArrayList<>(),
			new ArrayList<>()
		);
		
		return block;
	}
}
