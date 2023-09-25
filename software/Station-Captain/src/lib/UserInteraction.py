from ScriptInfos import ScriptInfo
from dialog import Dialog
import logging


class UserInteraction:
	"""

	User interaction functions.

	References:
		- https://pythondialog.sourceforge.io
	"""
	def __init__(self):
		self.dialog = Dialog(
			dialog="dialog",
			# autowidgetsize=True
		)
		self.dialog.set_background_title(ScriptInfo.SCRIPT_TITLE)

	def mainMenu(self):
		logging.debug("Running main menu.")
		while True:
			code, choice = self.dialog.menu(
				"Please choose an option:",
				choices=[
					("(1)", "Info / Status"),
					("(2)", "Manage Installation"),
					("(3)", "Snapshots"),
					("(4)", "Captain Settings"),
				],
				title="Main Menu"
			)
			logging.debug('Main menu choice: %s, code: %s', choice, code)
			if code != self.dialog.OK:
				break


		logging.debug("Done running main menu.")


ui = UserInteraction()
