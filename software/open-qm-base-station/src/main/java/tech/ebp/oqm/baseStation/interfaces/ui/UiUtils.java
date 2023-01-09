package tech.ebp.oqm.baseStation.interfaces.ui;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriInfo;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class UiUtils {
	
	public static final int DEFAULT_COOKIE_AGE = 86400; //1 day
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss a LLL dd, uu z");
	
	public static String getLoadTimestamp() {
		return ZonedDateTime.now().format(DATE_TIME_FORMATTER);
	}
	
	public static NewCookie getNewCookie(
		UriInfo uriInfo,
		String cookieName,
		String value,
		String comment,
		int maxAgeSecs
	) {
		return new NewCookie(
			new Cookie(
				cookieName,
				value,
				"/",
				uriInfo.getBaseUri().getHost()
			),
			comment,
			maxAgeSecs,
			false
		);
	}
	
	public static NewCookie getRemovalCookie(UriInfo uriInfo, String cookieName) {
		return getNewCookie(
			uriInfo,
			cookieName,
			"",
			"To remove \"" + cookieName + "\" cookie.",
			0
		);
	}
	
	public static NewCookie getAuthRemovalCookie(UriInfo uriInfo) {
		return getRemovalCookie(
			uriInfo,
			ConfigProvider.getConfig().getValue("mp.jwt.token.cookie", String.class)
		);
	}
	
	public static NewCookie getAuthCookie(
		UriInfo uriInfo,
		String jwt,
		int ageMaxSecs
	) {
		return getNewCookie(
			uriInfo,
			ConfigProvider.getConfig().getValue("mp.jwt.token.cookie", String.class),
			jwt,
			"JWT from externl auth.",
			ageMaxSecs
		);
	}
	
	public static List<NewCookie> getExternalAuthCookies(
		UriInfo uriInfo,
		JsonNode keycloakResponse
	) {
		if (keycloakResponse == null) {
			return Collections.emptyList();
		}
		
		List<String> fields = new ArrayList<>();
		for (Iterator<String> it = keycloakResponse.fieldNames(); it.hasNext(); ) {
			fields.add(it.next());
		}
		log.info("Fields from keycloak: {}", fields);
		
		if (!keycloakResponse.has("access_token")) {
			log.warn("Failed to get token from keycloak (token not in data)");
			//TODO:: handle
			throw new IllegalStateException("Token not in data");
		}
		
		String jwt = keycloakResponse.get("access_token").asText();
		int jwt_expires_in = keycloakResponse.get("expires_in").asInt();
		String refresh_token = null;
		int refreshExpiresIn = jwt_expires_in;
		if (keycloakResponse.has("refresh_token")) {
			refresh_token = keycloakResponse.get("refresh_token").asText();
			refreshExpiresIn = keycloakResponse.get("refresh_expires_in").asInt();
		}
		
		log.debug("JWT got from external auth: {}", jwt);
		log.debug("Public key to verify sig: {}", ConfigProvider.getConfig().getValue("mp.jwt.verify.publickey.location", String.class));
		
		List<NewCookie> newCookies = new ArrayList<>();
		
		newCookies.add(
			UiUtils.getNewCookie(
				uriInfo,
				ConfigProvider.getConfig().getValue("mp.jwt.token.cookie", String.class),
				jwt,
				"JWT from external auth",
				jwt_expires_in
			)
		);
		if (refresh_token != null) {
			newCookies.add(
				UiUtils.getNewCookie(
					uriInfo,
					ConfigProvider.getConfig().getValue("mp.jwt.token.cookie", String.class) + "_refresh",
					refresh_token,
					"JWT refresh token.",
					refreshExpiresIn
				)
			);
		}
		
		return newCookies;
	}
}
