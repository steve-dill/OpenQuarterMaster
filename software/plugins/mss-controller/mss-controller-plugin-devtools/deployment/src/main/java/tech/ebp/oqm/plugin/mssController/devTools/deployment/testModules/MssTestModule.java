package tech.ebp.oqm.plugin.mssController.devTools.deployment.testModules;

import tech.ebp.oqm.plugin.mssController.devTools.runtime.model.command.MssCommand;
import tech.ebp.oqm.plugin.mssController.devTools.runtime.model.command.response.ModuleInfo;

public class MssTestModule {

	private ModuleInfo moduleInfo;

	protected MssTestModule(ModuleInfo moduleInfo) {
		this.moduleInfo = moduleInfo;
	}

	protected MssCommand processCommand(MssCommand command) {
		return command;//TODO
	}
}
