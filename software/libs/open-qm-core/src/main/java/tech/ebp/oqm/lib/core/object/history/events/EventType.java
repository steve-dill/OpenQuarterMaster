package tech.ebp.oqm.lib.core.object.history.events;

public enum EventType {
	CREATE,
	UPDATE,
	DELETE,
	USER_LOGIN,
	USER_ENABLED,
	USER_DISABLED,
	ITEM_EXPIRY_WARNING,
	ITEM_EXPIRED,
	ITEM_ADD,
	ITEM_SUBTRACT,
	ITEM_TRANSFER,
	EXT_SERVICE_SETUP,
	EXT_SERVICE_AUTH,
}
