package com.ebp.openQuarterMaster.plugin.moduleInteraction.command.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetModuleInfoResponse extends CommandResponse {
	private ModuleInfo response;
}
