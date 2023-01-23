package tech.ebp.oqm.baseStation.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import tech.ebp.oqm.lib.core.object.ObjectUtils;

import javax.inject.Singleton;

@Singleton
public class JacksonModuleCustomizer implements ObjectMapperCustomizer {
	
	public void customize(ObjectMapper mapper) {
		ObjectUtils.setupObjectMapper(mapper);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}
}
