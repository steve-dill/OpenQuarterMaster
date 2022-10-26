package tech.ebp.oqm.lib.core.rest.unit.custom;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import tech.ebp.oqm.lib.core.units.CustomUnitEntry;
import tech.ebp.oqm.lib.core.units.UnitCategory;

import javax.measure.Unit;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "requestType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = NewBaseCustomUnitRequest.class, name = "BASE"),
	@JsonSubTypes.Type(value = NewDerivedCustomUnitRequest.class, name = "DERIVED")
})
public abstract class NewCustomUnitRequest {
	
	@NonNull
	@NotNull
	private UnitCategory unitCategory;
	
	@NonNull
	@NotNull
	private String name;
	
	@NonNull
	@NotNull
	private String symbol;
	
	public abstract RequestType getRequestType();
	
	public abstract Unit<?> toUnit();
	
	public CustomUnitEntry toCustomUnitEntry() {
		return new CustomUnitEntry(
			this.getUnitCategory(),
			this.toUnit()
		);
	}
	
	public enum RequestType {
		BASE, DERIVED
	}
}
