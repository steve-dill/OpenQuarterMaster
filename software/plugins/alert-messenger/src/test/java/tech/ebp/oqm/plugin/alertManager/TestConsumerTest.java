package tech.ebp.oqm.plugin.alertManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.OqmDatabaseService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.OqmCoreApiClientService;
import tech.ebp.oqm.lib.core.api.quarkus.runtime.sso.KcClientAuthService;
import tech.ebp.oqm.plugin.alertMessenger.TestConsumer;

import java.time.LocalDateTime;

@Slf4j
@QuarkusTest
public class TestConsumerTest {

	@Inject
	OqmDatabaseService oqmDatabaseService;
	@Inject
	KcClientAuthService serviceAccountService;
	@RestClient
	OqmCoreApiClientService coreApiClientService;
	@Inject
	ObjectMapper objectMapper;

	@Inject
	TestConsumer testConsumer;

	@Test
	public void test() throws InterruptedException {
		String newid = this.coreApiClientService
			.storageBlockAdd(
				this.serviceAccountService.getAuthString(),
				"default",
				this.objectMapper.createObjectNode()
					.put("label", "new block")
			)
			.await().indefinitely();

		log.info("Id of new block: {}", newid);

		LocalDateTime timeout = LocalDateTime.now().plusSeconds(10);
		while(!this.testConsumer.isReceived()){
			if(LocalDateTime.now().isAfter(timeout)){
				Assertions.fail("Failed to receive message.");
			}

			Thread.sleep(250);
		}
	}

}
