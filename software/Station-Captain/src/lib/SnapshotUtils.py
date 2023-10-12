from enum import Enum
from ConfigManager import *
from ServiceUtils import *
import logging
import subprocess
import datetime
import os
import shutil
import tarfile


class SnapshotTrigger(Enum):
    manual = 1
    scheduled = 2


class SnapshotUtils:
    """

    """
    CRON_NAME = "take-snapshot"

    @staticmethod
    def performSnapshot(snapshotTrigger: SnapshotTrigger) -> bool:
        logging.info("Performing snapshot.")

        snapshotName = "snapshot-%s-%s".format(
            datetime.datetime.now().strftime("%Y.%m.%d-%H.%M.%S"),
            snapshotTrigger.name
        )
        logging.debug("Snapshot name: %s", snapshotName)
        compilingDir = ScriptInfo.TMP_DIR + "/snapshots/" + snapshotName
        compilingConfigsDir = os.path.join(compilingDir, "config")
        compilingServiceConfigsDir = os.path.join(compilingDir, "serviceConfigs")
        dataDir = os.path.join(compilingDir, "/data")

        snapshotLocation = mainCM.getConfigVal("snapshots.location")
        snapshotArchiveName = "%s/%s.tar.gz".format(snapshotLocation, snapshotName)

        try:
            os.makedirs(compilingConfigsDir)
            os.makedirs(compilingServiceConfigsDir)
            os.makedirs(dataDir)
            os.makedirs(snapshotLocation)
        except:
            logging.error("Failed to create directories necessary for snapshot taking.")
            return False

        ServiceUtils.doServiceCommand(ServiceStateCommand.stop, ServiceUtils.SERVICE_ALL)

        try:
            # https://stackoverflow.com/questions/12683834/how-to-copy-directory-recursively-in-python-and-overwrite-all
            shutil.copytree(ScriptInfo.CONFIG_DIR, compilingConfigsDir)
            shutil.copytree(ScriptInfo.SERVICE_CONFIG_DIR, compilingServiceConfigsDir)

            # TODO:: run
            logging.info("Running individual snapshots.")
            for filename in os.listdir(ScriptInfo.SNAPSHOT_SCRIPTS_LOC):
                file = os.path.join(ScriptInfo.SNAPSHOT_SCRIPTS_LOC, filename)
                logging.info("Running script %s", file)
                result = subprocess.run([file, "--snapshot", "-d", compilingDir], shell=False, capture_output=True, text=True, check=True)
                if result.returncode != 0:
                    logging.error("FAILED to run snapshot script, returned %d. Erring script: %s", result.returncode, file)
                    logging.debug("Erring script err output: %s", result.stderr)
                    break
        except Exception as e:
            logging.error("FAILED to compile files for snapshot: %s", e)
            return False
        finally:
            ServiceUtils.doServiceCommand(ServiceStateCommand.start, ServiceUtils.SERVICE_ALL)

        # https://docs.python.org/3.8/library/tarfile.html#tarfile.TarFile.add
        with tarfile.open(snapshotArchiveName, "w:gz") as tar:
            tar.add(compilingConfigsDir, arcname=os.path.basename(compilingConfigsDir))

        logging.info("Done Performing snapshot.")


    @staticmethod
    def restoreFromSnapshot(snapshotFile: str) -> bool:
        logging.info("Performing snapshot Restore.")
        ServiceUtils.doServiceCommand(ServiceStateCommand.stop, ServiceUtils.SERVICE_ALL)

        # TODO:: run snapshot restore

        ServiceUtils.doServiceCommand(ServiceStateCommand.start, ServiceUtils.SERVICE_ALL)
        logging.info("Done Performing snapshot Restore.")
