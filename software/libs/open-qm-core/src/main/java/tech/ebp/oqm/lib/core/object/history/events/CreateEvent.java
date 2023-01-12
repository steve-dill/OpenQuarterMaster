package tech.ebp.oqm.lib.core.object.history.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.types.ObjectId;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.history.EventType;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;

/**
 * Event for the creation of an object.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
//@SuperBuilder
public class CreateEvent extends ObjectHistoryEvent {
	
	public CreateEvent(ObjectId objectId, InteractingEntity entity) {
		super(objectId, entity);
	}
	
	public CreateEvent(MainObject object, InteractingEntity entity) {
		super(object, entity);
	}
	
	@Override
	public EventType getType() {
		return EventType.CREATE;
	}
}
