package tech.ebp.oqm.lib.core.object.history.events.user;

import lombok.NoArgsConstructor;
import tech.ebp.oqm.lib.core.object.history.EventType;
import tech.ebp.oqm.lib.core.object.history.ObjectHistoryEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Event for the login action of a user.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class UserLoginEvent extends ObjectHistoryEvent {
	
	@Override
	public EventType getType() {
		return EventType.USER_LOGIN;
	}
}
