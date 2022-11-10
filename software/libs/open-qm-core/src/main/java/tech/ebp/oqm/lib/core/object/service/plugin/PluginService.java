package tech.ebp.oqm.lib.core.object.service.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.service.Service;
import tech.ebp.oqm.lib.core.object.service.ServiceType;
import tech.ebp.oqm.lib.core.object.service.plugin.components.PageComponent;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PluginService extends Service {
	
	@NonNull
	@NotNull
	List<PageComponent> pageComponents = new ArrayList<>();
	
	@Override
	public ServiceType getServiceType() {
		return ServiceType.PLUGIN;
	}
}
