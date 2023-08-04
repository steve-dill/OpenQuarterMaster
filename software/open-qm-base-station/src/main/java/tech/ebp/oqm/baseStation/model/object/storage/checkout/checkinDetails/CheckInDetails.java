package tech.ebp.oqm.baseStation.model.object.storage.checkout.checkinDetails;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.AttKeywordContaining;
import tech.ebp.oqm.baseStation.model.object.storage.checkout.checkinDetails.CheckInType;
import tech.ebp.oqm.baseStation.model.object.storage.checkout.checkinDetails.LossCheckin;
import tech.ebp.oqm.baseStation.model.object.storage.checkout.checkinDetails.ReturnCheckin;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "checkinType"
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = ReturnCheckin.class, name = "RETURN"),
	@JsonSubTypes.Type(value = LossCheckin.class, name = "LOSS"),
})
@BsonDiscriminator
public abstract class CheckInDetails implements AttKeywordContaining {
	
	public abstract CheckInType getCheckinType();
	
	/**
	 * Notes about the checkin
	 */
	@NonNull
	@NotNull
	private String notes = "";
	
	/**
	 * When the checkin took place
	 */
	@NonNull
	@NotNull
	private ZonedDateTime checkinDateTime = ZonedDateTime.now();
	
	/**
	 * List of images related to the checkin.
	 */
	@NonNull
	@NotNull
	List<@NonNull ObjectId> imageIds = new ArrayList<>();
	
	/**
	 * Attributes this object might have, usable for any purpose.
	 */
	@NotNull
	@NonNull
	private Map<@NotBlank @NotNull String, String> attributes = new HashMap<>();
	
	/**
	 * Keywords for the object
	 */
	@NotNull
	@NonNull
	private List<@NotBlank String> keywords = new ArrayList<>();
}
