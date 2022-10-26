package tech.ebp.oqm.lib.core.rest.unit.custom;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.lib.core.validation.annotations.ValidUnit;

import javax.measure.Unit;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NewDerivedCustomUnitRequest extends NewCustomUnitRequest {
	
	@NotNull
	@NonNull
	@ValidUnit
	private Unit<?> baseUnit;
	
	@NotNull
	@NonNull
	private BigDecimal numPerBaseUnit;
	
	@NotNull
	@NonNull
	private DeriveType deriveType;
	
	@Override
	public RequestType getRequestType() {
		return RequestType.DERIVED;
	}
	
	@Override
	public Unit<?> toUnit() {
		switch (this.getDeriveType()) {
			case multiply:
				return this.getBaseUnit().multiply(this.getNumPerBaseUnit());
			case divide:
				return this.getBaseUnit().divide(this.getNumPerBaseUnit());
		}
		throw new IllegalArgumentException("Bad or unsupported derive type. This should not happen.");
	}
	
	public enum DeriveType {
		multiply,
		divide
	}
}
