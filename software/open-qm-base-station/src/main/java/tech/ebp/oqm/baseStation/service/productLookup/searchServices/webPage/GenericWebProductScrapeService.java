package tech.ebp.oqm.baseStation.service.productLookup.searchServices.webPage;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import tech.ebp.oqm.baseStation.model.rest.externalItemLookup.ExtItemLookupProviderInfo;
import tech.ebp.oqm.baseStation.model.rest.externalItemLookup.ExtItemLookupResult;

import jakarta.enterprise.context.ApplicationScoped;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Slf4j
@NoArgsConstructor
public class GenericWebProductScrapeService extends WebPageProductScrapeService {
	
	@SneakyThrows
	@Override
	public ExtItemLookupProviderInfo getProviderInfo() {
		//TODO:: add to application.yaml?
		return ExtItemLookupProviderInfo.builder()
				   .displayName("Generic")
				   .description("Generic page scraper; will attempt to scrape any page given with best-effort.")
				   .acceptsContributions(false)
				   .cost("Free")
				   .enabled(this.isEnabled())
										.build();
	}
	
	@WithSpan
	@Override
	protected ExtItemLookupResult scrapePageContent(Document webPageContent) {
		ExtItemLookupResult.Builder<?, ?> resultBuilder = this.getInitialBuilder(webPageContent);
		
		Map<String, String> atts = new HashMap<>();
		
		//TODO:: this
		
		
		return resultBuilder.build();
	}
	
	
	@Override
	public List<String> supportedHosts() {
		return null;
	}
	
	@Override
	public boolean canParsePage(URL url) {
		return true;
	}
}
