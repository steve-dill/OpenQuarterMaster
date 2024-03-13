package tech.ebp.oqm.core.baseStation.interfaces.ui.js;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.tags.Tags;

@Slf4j
@Path("/res/js/")
@Tags({@Tag(name = "JS Utilities")})
@ApplicationScoped
@Produces(MediaType.TEXT_HTML)
public class JsGetters {
	
	@Inject
	@Location("webui/js/icons.js")
	Template icons;
	
	@Inject
	@Location("webui/js/links.js")
	Template links;
	
	@Inject
	@Location("webui/js/carousel.js")
	Template carouselJs;
	String carouselLines = "";
	
	@Inject
	@Location("webui/js/components.js")
	Template componentsJs;
	String attInputLines;
	String keywordInputLines;
	
	private String templateToEscapedJs(TemplateInstance templateInstance){
		return templateInstance
				   .render()
				   .replaceAll("'", "\\\\'")
				   .replaceAll("\n", "\\\\\n")
			;
	}
	
	@Inject
	public JsGetters(
		@Location("tags/carousel.html") Template carouselTemplate,
		@Location("tags/inputs/attInput.html") Template attInputTemplate,
		@Location("tags/inputs/keywordInput.html") Template keywordInputTemplate
	){
		this.carouselLines = this.templateToEscapedJs(carouselTemplate.data("id", ""));
		this.attInputLines = this.templateToEscapedJs(attInputTemplate.instance());
		this.keywordInputLines = this.templateToEscapedJs(keywordInputTemplate.instance());
	}
	
	@GET
	@Path("icons.js")
	@PermitAll
	@Produces("text/javascript")
	public Uni<String> icons() {
		return icons.instance().createUni();
	}
	
	@GET
	@Path("links.js")
	@PermitAll
	@Produces("text/javascript")
	public Uni<String> links() {
		return links.instance().createUni();
	}
	
	@GET
	@Path("carousel.js")
	@PermitAll
	@Produces("text/javascript")
	public Uni<String> carousel() {
		return this.carouselJs
				   .data("carouselLines", this.carouselLines)
				   .createUni();
	}
	
	@GET
	@Path("components.js")
	@PermitAll
	@Produces("text/javascript")
	public Uni<String> components() {
		return this.componentsJs
				   .data("attInputLines", this.attInputLines)
				   .data("keywordInputLines", this.keywordInputLines)
				   .createUni();
	}
}
