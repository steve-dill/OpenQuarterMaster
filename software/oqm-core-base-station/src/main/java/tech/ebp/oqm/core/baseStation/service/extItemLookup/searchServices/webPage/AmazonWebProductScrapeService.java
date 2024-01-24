package tech.ebp.oqm.core.baseStation.service.extItemLookup.searchServices.webPage;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import tech.ebp.oqm.core.baseStation.service.extItemLookup.ExtItemLookupProviderInfo;
import tech.ebp.oqm.core.baseStation.service.extItemLookup.ExtItemLookupResult;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Slf4j
@NoArgsConstructor
public class AmazonWebProductScrapeService extends WebPageProductScrapeService {
	private static final List<String> SUPPORTED_HOSTS = List.of("www.amazon.com");
	
	@SneakyThrows
	@Override
	public ExtItemLookupProviderInfo getProviderInfo() {
		//TODO:: add to application.yaml?
		return ExtItemLookupProviderInfo.builder()
				   .displayName("Amazon.com")
				   .acceptsContributions(false)
				   .cost("Free")
				   .enabled(this.isEnabled())
				   .homepage(new URL("https://www.amazon.com/"))
										.build();
	}
	
	@WithSpan
	@Override
	protected ExtItemLookupResult scrapePageContent(Document webPageContent) {
		ExtItemLookupResult.Builder<?, ?> resultBuilder = this.getInitialBuilder(webPageContent);
		
		Map<String, String> atts = new HashMap<>();
		resultBuilder = resultBuilder.unifiedName(webPageContent.getElementById("productTitle").text());
		
		try {
			//TODO:: this isn't working... why?
			resultBuilder = resultBuilder.description(webPageContent.getElementById("productDescription").text());
		} catch(NullPointerException e){
			log.warn("Unable to find description...");
			//			log.debug("Found price? {}", results.text().contains("$94.49"));
		}
		
		
		//		resultBuilder = resultBuilder.description(results.getElementById("tab-description-content").text());
		
		try {
			//TODO:: this isn't working... why?
			atts.put(
				"price",
				webPageContent.getElementById("corePriceDisplay_desktop_feature_div")
					   .text()
			);
		} catch(NullPointerException e){
			log.warn("Unable to find price...");
			//			log.debug("Found price? {}", results.text().contains("$94.49"));
		}
		
		resultBuilder.attributes(atts);
		return resultBuilder.build();
	}
	
	@WithSpan
	@Override
	public List<String> supportedHosts() {
		return SUPPORTED_HOSTS;
	}
}
