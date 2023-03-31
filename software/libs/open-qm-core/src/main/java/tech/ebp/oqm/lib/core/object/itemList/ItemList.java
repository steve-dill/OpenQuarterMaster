package tech.ebp.oqm.lib.core.object.itemList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.AttKeywordMainObject;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes a list of actions to take on the storage of an item
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@AllArgsConstructor
public class ItemList extends AttKeywordMainObject {
	
	/**
	 * The name of this list
	 */
	@NonNull
	@NotNull
	@NotBlank
	private String name;
	
	/**
	 * The description for this list
	 */
	@NonNull
	@NotNull
	private String description = "";
	
	/**
	 * Map of items to their associated action.
	 *
	 * TODO:: validator
	 */
	@NonNull
	@NotNull
	private Map<ObjectId, List<@NonNull ItemListAction>> items = new HashMap<>();
	
	/**
	 * If this list was applied or not.
	 */
	private boolean applied = false;
}
