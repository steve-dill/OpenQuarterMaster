#
# Functions to handle backing up and restoring data
#

function backRes_backup(){
	# TODO:: stop everything?

	# Setup locations
	local backupName="backup-$(date +"%Y.%m.%d-%H.%M.%S")"
	local compilingDir="$TMP_DIR/backup/$backupName";
	local configsDir="$compilingDir/configs"
	local dataDir="$compilingDir/data"

	mkdir -p "$configsDir"
	mkdir -p "$dataDir"

	#echo "Calling backup scripts"

	for backupScript in "$BACKUP_SCRIPTS_LOC"/*; do
		echo "$backupScript --backup \"$compilingDir\"";
	done

	local backupLocation=$(oqm-config -g backups.location)
	local backupArchiveName="$backupLocation$backupName.tar.gz"
	mkdir -p "$backupLocation"

	#echo "Backup archive: $backupArchiveName"

	tar -czvf "$backupArchiveName" -C "$compilingDir" $(ls -A "$compilingDir")

	rm -rf "$compilingDir"
}

function backRes_restore(){
	local something=""
	# TODO

}