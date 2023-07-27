package tech.ebp.oqm.baseStation.model.validation.validators;

import org.apache.commons.codec.binary.Base64;
import tech.ebp.oqm.baseStation.model.validation.annotations.ValidBase64;
import tech.ebp.oqm.baseStation.model.validation.validators.Validator;

import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class Base64Validator extends Validator<ValidBase64, String> {
	
	@Override
	public boolean isValid(String base64Str, ConstraintValidatorContext constraintValidatorContext) {
		List<String> errs = new ArrayList<>();
		
		if (base64Str == null) {
			return true;
		} else {
			if (!Base64.isBase64(base64Str)) {
				errs.add("Invalid base 64 string.");
			}
		}
		return this.processValidationResults(errs, constraintValidatorContext);
	}
}
