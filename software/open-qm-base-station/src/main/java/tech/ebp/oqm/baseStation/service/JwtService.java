package tech.ebp.oqm.baseStation.service;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import io.smallrye.jwt.util.KeyUtils;
import io.smallrye.jwt.util.ResourceUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import tech.ebp.oqm.lib.core.object.interactingEntity.externalService.ExternalService;
import tech.ebp.oqm.lib.core.object.interactingEntity.user.User;
import tech.ebp.oqm.lib.core.rest.auth.externalService.ExternalServiceLoginResponse;
import tech.ebp.oqm.lib.core.rest.auth.user.UserLoginResponse;

import javax.enterprise.context.ApplicationScoped;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TODO:: add field in jwt for service or user
 */
@Slf4j
@ApplicationScoped
public class JwtService {
	
	public static final String JWT_USER_ID_CLAIM = "userId";
	public static final String JWT_SERVICE_ID_CLAIM = "serviceId";
	public static final String JWT_USER_TITLE_CLAIM = "title";
	public static final String JWT_ISSUER_CLAIM = Claims.iss.name();
	
	private final long defaultExpiration;
	private final long extendedExpiration;
	private final long serviceExpiration;
	private final String sigKeyId;
	private final String issuer;
	private final PrivateKey privateKey;
	
	private static PrivateKey getPrivateKey(String location) {
		InputStream contentIS = JwtService.class.getClassLoader().getResourceAsStream(location);
		if (contentIS == null) {
			try {
				contentIS = new FileInputStream(location);
			} catch(FileNotFoundException e) {
				throw new RuntimeException("FAILED to open private key file.", e);
			}
		}
		try (
			InputStream inputStream = contentIS;
		) {
			byte[] tmp = new byte[4096];
			int length = inputStream.read(tmp);
			return KeyUtils.decodePrivateKey(new String(tmp, 0, length), SignatureAlgorithm.RS256);
		} catch(Throwable e) {
			throw new RuntimeException("FAILED to read in private key for jwt creation,", e);
		}
	}
	
	public JwtService(
		@ConfigProperty(name = "mp.jwt.verify.privatekey.location", defaultValue = "")
		String privateKeyLocation,
		@ConfigProperty(name = "mp.jwt.expiration.default")
		long defaultExpiration,
		@ConfigProperty(name = "mp.jwt.expiration.extended")
		long extendedExpiration,
		@ConfigProperty(name = "externalService.serviceTokenExpires")
		long serviceExpiration,
		@ConfigProperty(name = "mp.jwt.verify.issuer")
		String issuer
	) throws Exception {
		this.defaultExpiration = defaultExpiration;
		this.extendedExpiration = extendedExpiration;
		this.serviceExpiration = serviceExpiration;
		this.sigKeyId = privateKeyLocation;
		this.issuer = issuer.trim();
		
		log.info("Private key location provided: {}", privateKeyLocation);
		this.privateKey = getPrivateKey(privateKeyLocation);
	}
	
	/**
	 * Gets a user's jwt. Meant to be used during auth, returns the object meant to return to the user.
	 *
	 * @param user The user to get the jwt for
	 * @param extendedTimeout If the jwt should have an extended expiration period
	 *
	 * @return The response to give back to the user.
	 */
	@WithSpan
	public UserLoginResponse getUserJwt(User user, boolean extendedTimeout) {
		Instant expiration = Instant.now()
								 .plusSeconds((
									 extendedTimeout
										 ? this.extendedExpiration
										 : this.defaultExpiration
								 ));
		
		return new UserLoginResponse(this.generateTokenString(user, expiration), expiration);
	}
	
	@WithSpan
	public ExternalServiceLoginResponse getExtServiceJwt(ExternalService service) {
		Instant expiration = Instant.now().plusSeconds(this.serviceExpiration);
		
		return new ExternalServiceLoginResponse(
			this.generateTokenString(service, expiration),
			expiration
		);
	}
	
	/**
	 * Generates a jwt for use by the user.
	 *
	 * @param user The user to get the jwt for
	 * @param expires When the jwt should expire
	 *
	 * @return The jwt for the user
	 */
	@WithSpan
	public String generateTokenString(
		User user,
		Instant expires
	) {
		//info on what claims are: https://auth0.com/docs/security/tokens/json-web-tokens/json-web-token-claims
		Map<String, Object> rawClaims = this.getUserClaims(user);
		
		JwtClaimsBuilder claims = Jwt.claims(rawClaims);
		
		claims.expiresAt(expires);
		
		return claims.jws().keyId(this.sigKeyId).sign(this.privateKey);
	}
	
	@WithSpan
	public String generateTokenString(
		ExternalService service,
		Instant expires
	) {
		Map<String, Object> rawClaims = this.getServiceClaims(service);
		
		JwtClaimsBuilder claims = Jwt.claims(rawClaims);
		
		claims.expiresAt(expires);
		
		return claims.jws().keyId(this.sigKeyId).sign(this.privateKey);
	}
	
	private Map<String, Object> getUserClaims(User user) {
		Map<String, Object> output = this.getBaseClaims();
		
		String userIdentification = user.getId() + ";" + user.getEmail();
		
		output.put(
			"jti",
			//TODO:: this, properly
			//                user.getId() + "-" + user.getLastLogin().getTime() + "-" + user.getNumLogins()
			user.getId() + "-" + UUID.randomUUID()
		);//TODO: move to utility, test
		output.put(Claims.sub.name(), user.getId());
		output.put(Claims.aud.name(), userIdentification);
		output.put(Claims.upn.name(), user.getUsername());
		output.put(Claims.email.name(), user.getEmail());
		output.put(JWT_USER_TITLE_CLAIM, user.getTitle());
		output.put(Claims.given_name.name(), user.getFirstName());
		output.put(Claims.family_name.name(), user.getLastName());
		output.put(JWT_USER_ID_CLAIM, user.getId());
		
		output.put("roleMappings", new HashMap<String, Object>());
		
		output.put(Claims.groups.name(), user.getRoles());
		
		return output;
	}
	
	private Map<String, Object> getServiceClaims(ExternalService service) {
		Map<String, Object> output = this.getBaseClaims();
		
		String serviceIdentification = service.getId() + ";" + service.getName();
		
		output.put(
			"jti",
			//TODO:: this, properly
			//                user.getId() + "-" + user.getLastLogin().getTime() + "-" + user.getNumLogins()
			service.getId() + "-" + UUID.randomUUID()
		);//TODO: move to utility, test
		output.put(Claims.sub.name(), service.getId());
		output.put(Claims.aud.name(), serviceIdentification);
		output.put(Claims.upn.name(), service.getName());
		output.put(Claims.email.name(), service.getEmail());
		output.put(JWT_SERVICE_ID_CLAIM, service.getId());
		
		output.put("roleMappings", new HashMap<String, Object>());
		
		output.put(Claims.groups.name(), service.getRoles());
		
		return output;
	}
	
	private Map<String, Object> getBaseClaims() {
		Map<String, Object> output = new HashMap<>();
		
		output.put(JWT_ISSUER_CLAIM, this.issuer); // serverInfo.getOrganization() + " - Task Timekeeper Server");
		output.put(Claims.auth_time.name(), Instant.now().getEpochSecond());
		
		return output;
	}
}
