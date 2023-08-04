package tech.ebp.oqm.baseStation.model.object.itemList;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.itemList.ItemListActionMode;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.Stored;

import javax.validation.constraints.NotNull;

/**
 * TODO:: validator to ensure from/to storage ids straight
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemListAction {
	
	/**
	 * The action this action will take.
	 */
	@NonNull
	@NotNull
	private ItemListActionMode mode;
	
	/**
	 * The storage block this action goes to.
	 */
	private ObjectId targetStorageTo;
	
	/**
	 * The storage block this action comes from.
	 */
	private ObjectId targetStorageFrom;
	
	/**
	 * What to apply the action with.
	 */
	@NonNull
	@NotNull
	private Stored stored;
}
