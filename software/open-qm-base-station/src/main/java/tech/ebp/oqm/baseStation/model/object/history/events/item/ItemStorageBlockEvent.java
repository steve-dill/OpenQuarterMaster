package tech.ebp.oqm.baseStation.model.object.history.events.item;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.MainObject;
import tech.ebp.oqm.baseStation.model.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.baseStation.model.object.interactingEntity.InteractingEntity;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
//@SuperBuilder
public abstract class ItemStorageBlockEvent extends ObjectHistoryEvent {
	
	public ItemStorageBlockEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public ItemStorageBlockEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	@NotNull
	//	@NonNull
	private ObjectId storageBlockId;
	
	private String identifier = null;
	
	private Integer index = null;
}
