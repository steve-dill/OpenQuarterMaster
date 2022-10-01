package tech.ebp.oqm.lib.core.rest.auth.roles;

import java.util.List;

public class ServiceRoles {
	
	public static List<String> SERVICE_ROLES = List.of(
		Roles.SERVICE,
		
		Roles.SERVICE_ADMIN,
		Roles.INVENTORY_ADMIN,
		
		Roles.INVENTORY_VIEW,
		Roles.INVENTORY_EDIT
	);
	
	public static List<String> allowedRoles() {
		return SERVICE_ROLES;
	}
	
	public static boolean roleAllowed(String role) {
		return SERVICE_ROLES.contains(role);
	}
	
	
	public static final List<String> SELECTABLE_ROLES = List.of(
		Roles.SERVICE_ADMIN,
		Roles.INVENTORY_ADMIN,
		Roles.INVENTORY_VIEW,
		Roles.INVENTORY_EDIT
	);
	
}
