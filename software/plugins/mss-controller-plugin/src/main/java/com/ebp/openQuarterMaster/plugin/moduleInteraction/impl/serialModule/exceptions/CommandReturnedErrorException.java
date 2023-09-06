package com.ebp.openQuarterMaster.plugin.moduleInteraction.impl.serialModule.exceptions;

import lombok.Getter;
import tech.ebp.oqm.lib.moduleDriver.interaction.command.commands.complex.ErrorCommand;

public class CommandReturnedErrorException extends IllegalStateException {
	
	@Getter
	private final ErrorCommand errorCommand;
	
	public CommandReturnedErrorException(ErrorCommand errorCommand, String s) {
		super(s);
		this.errorCommand = errorCommand;
	}
	
	public CommandReturnedErrorException(ErrorCommand errorCommand, String message, Throwable cause) {
		super(message, cause);
		this.errorCommand = errorCommand;
	}
	
	public CommandReturnedErrorException(ErrorCommand errorCommand, Throwable cause) {
		super(cause);
		this.errorCommand = errorCommand;
	}
}
