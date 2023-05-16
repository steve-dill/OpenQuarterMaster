package stationCaptainTest.stepDefinitions.features;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.cienvironment.internal.com.eclipsesource.json.JsonObject;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.utility.MountableFile;
import stationCaptainTest.testResources.BaseStepDefinitions;
import stationCaptainTest.testResources.TestContext;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class ConfigUtilitySteps extends BaseStepDefinitions {
	private static final String TEST_VALUES = "src/test/resources/res/config-util/99-test-values.json";
	private static final String TEST_VALUES_DEST = "/etc/oqm/config/configs/99-test-values.json";
	private static final String TEST_TEMPLATE = "src/test/resources/res/config-util/configTemplate.list";
	private static final String TEST_TEMPLATE_DEST = "/tmp/configTemplate.list";
	
	
	public ConfigUtilitySteps(TestContext context) {
		super(context);
	}
	
	@Override
	@Before
	public void setup(Scenario scenario) {
		this.setScenario(scenario);
	}
	
	
	@And("test config files exist for testing config utility")
	public void testConfigFilesExistForTestingConfigUtility() {
		log.info("Adding test config files to container.");
		this.getContext().getRunningContainer().copyFileToContainer(MountableFile.forHostPath(TEST_VALUES), TEST_VALUES_DEST);
		this.getContext().getRunningContainer().copyFileToContainer(MountableFile.forHostPath(TEST_TEMPLATE), TEST_TEMPLATE_DEST);
	}
	
	
	@And("the configurations are listed as output")
	public void theConfigurationsAreListedAsOutput() throws JsonProcessingException {
		String rawOutput = this.getContext().getContainerExecResult().getStdout();
		ObjectNode object = (ObjectNode) OBJECT_MAPPER.readTree(rawOutput);
		
		assertTrue(object.has("captain"));
		assertTrue(object.has("test"));
	}
}
