package tech.ebp.oqm.plugin.mssController.devTools.runtime.restClient.searchObjects;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
@Getter
public class ImageSearch extends FileSearchObject {
	@QueryParam("title") String imageTitle;
	@QueryParam("source") String imageSource;
	
}
