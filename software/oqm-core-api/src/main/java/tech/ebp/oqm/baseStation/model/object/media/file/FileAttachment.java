package tech.ebp.oqm.baseStation.model.object.media.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.FileMainObject;

import java.util.List;
import java.util.Map;

@Data
//@AllArgsConstructor
//@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FileAttachment extends FileMainObject {
	
	public FileAttachment(
		@NotNull @NonNull @Size(max = 500) String description,
		@NotNull @NonNull @NotBlank @Size(max = 500) String source
	) {
		super(description, source);
	}
	
	public FileAttachment() {
		super();
	}
	
	public FileAttachment(
		ObjectId id,
		Map<@NotBlank @NotNull String, String> attributes,
		List<@NotBlank String> keywords
	) {
		super(id, attributes, keywords);
	}
}
