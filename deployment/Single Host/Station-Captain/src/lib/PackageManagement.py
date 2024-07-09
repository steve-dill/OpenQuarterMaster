import os
import subprocess
import logging
import platform

from ServiceUtils import *

class PackageManagement:
    """
    Class to encapsulate methods that deal with package management.

    Helpful tricks:

    See what packages exist:
    grep -h -P -o "^Package: \K.*" /var/lib/apt/lists/deployment.openquartermaster.com_deb-ppa_._Packages | sort -u

    """
    BASE_STATION_PACKAGE = "oqm-core-base+station"
    ALL_OQM = "oqm-*"
    OQM_PLUGINS = "oqm-plugin-*"
    SYSTEM_PACKAGE_MANAGER = None

    @staticmethod
    def getSystemPackageManager() -> str:
        if PackageManagement.SYSTEM_PACKAGE_MANAGER is not None:
            return PackageManagement.SYSTEM_PACKAGE_MANAGER
        logging.debug("Determining the system's package manager.")

        systemReleaseInfo = platform.freedesktop_os_release()
        if ("ID_LIKE" in systemReleaseInfo and systemReleaseInfo['ID_LIKE'].casefold() == "debian".casefold()) or \
                systemReleaseInfo['ID'].casefold() == "Debian".casefold():
            PackageManagement.SYSTEM_PACKAGE_MANAGER = "apt"

        logging.info("Determined system using %s", PackageManagement.SYSTEM_PACKAGE_MANAGER)
        return PackageManagement.SYSTEM_PACKAGE_MANAGER

    @staticmethod
    def runPackageCommand(command: str, package: str = None, *options: str) -> subprocess.CompletedProcess:
        args = []
        args.append(PackageManagement.getSystemPackageManager())
        args.append(command)
        args.extend(options)
        if package is not None:
            args.append(package)

        return subprocess.run(args, shell=False, capture_output=True, text=True, check=False)

    @staticmethod
    def coreInstalled() -> bool:
        logging.debug("Ensuring core components are installed.")
        # TODO:: will likely need updated for yum
        result = PackageManagement.runPackageCommand("list", PackageManagement.BASE_STATION_PACKAGE, "-qq")
        logging.debug("Output of listing core components: " + result.stdout)
        logging.debug("Error Output of listing core components: " + result.stderr)
        return "installed" in result.stdout

    @staticmethod
    def installPackages(packages:list) -> (bool, str):
        logging.info("Installing packages: %s", packages)
        command:list = ["apt-get", "install", "-y"]
        command.extend(packages)
        result = subprocess.run(
            command,
            shell=False, capture_output=True, text=True, check=False
        )
        if result.returncode != 0:
            logging.error("Failed to run install packages command: %s", result.stderr)
            return False, result.stderr
        return True

    @staticmethod
    def removePackages(packages:list) -> (bool, str):
        logging.info("Removing packages: %s", packages)
        command:list = ["apt-get", "remove", "-y", "--purge"]
        command.extend(packages)
        result = subprocess.run(
            command,
            shell=False, capture_output=True, text=True, check=False
        )
        if result.returncode != 0:
            logging.error("Failed to run remove packages command: %s", result.stderr)
            return False, result.stderr
        return True

    @staticmethod
    def installCore():
        # TODO:: update to use new install, package get features
        # TODO:: update with error handling, return
        logging.info("Installing core components.")
        # TODO:: will likely need updated for yum
        result = PackageManagement.runPackageCommand("update")
        result = PackageManagement.runPackageCommand("install", PackageManagement.BASE_STATION_PACKAGE, "-y")
        logging.debug("Result of install: " + result.stdout)
        if result.returncode != 0:
            logging.error("FAILED to install core components: %s", result.stderr)

    @staticmethod
    def updateSystem() -> (bool, str):
        if PackageManagement.getSystemPackageManager() == "apt":
            logging.debug("Updating apt cache.")
            result = subprocess.run(["apt-get", "update"], shell=False, capture_output=True, text=True, check=False)
            if result.returncode != 0:
                logging.error("Failed to run update command: %s", result.stderr)
                return False, result.stderr
            logging.debug("Upgrading apt packages.")
            subprocess.run(["clear"], shell=False, capture_output=False, text=True, check=False)
            result = subprocess.run(["apt-get", "dist-upgrade"], shell=False, capture_output=False, text=True,
                                    check=False)
            if result.returncode != 0:
                logging.error("Failed to run upgrade command: %s", result.stderr)
                return False, result.stderr
        if PackageManagement.getSystemPackageManager() == "yum":
            logging.debug("Upgrading yum packages.")
            subprocess.run(["clear"], shell=False, capture_output=False, text=True, check=False)
            result = subprocess.run(["yum", "update"], shell=False, capture_output=False, text=True, check=False)
            if result.returncode != 0:
                logging.error("Failed to run upgrade command: %s", result.stderr)
                return False, result.stderr
        logging.info("Done updating.")
        return True, None

    @staticmethod
    def promptForAutoUpdates() -> (bool, str):
        if "Ubuntu" in platform.version():
            logging.debug("Prompting user through unattended-upgrades.")
            subprocess.run(["dpkg-reconfigure", "-plow", "unattended-upgrades"], shell=False, capture_output=False,
                           text=True, check=True)
            logging.info("Done.")
            # TODO:: doublecheck automatic restart, setting alert email
        else:
            return False, "Unsupported OS to setup auto updates on."
        return True, None

    @staticmethod
    def getOqmPackagesStr(filter: str = ALL_OQM, installed: bool = True, notInstalled: bool = True):
        logging.debug("Getting OQM packages.")
        result = PackageManagement.runPackageCommand("list", filter, "-qq")
        logging.debug("Output of listing core components: " + result.stdout)
        logging.debug("Error Output of listing core components: " + result.stderr)

        result = result.stdout
        output = []
        for curLine in result.splitlines():
            if installed and notInstalled:
                output.append(curLine)
                continue
            if installed:
                if "installed" in curLine:
                    output.append(curLine)
                continue
            if notInstalled:
                if not "installed" in curLine:
                    output.append(curLine)
        return os.linesep.join(output)

    @staticmethod
    def getPluginDisplayName(package:str):
        # print("Package: " + package)
        return package.split("-")[2].replace("+", " ")

    @staticmethod
    def packageLineToArray(curLine:str) -> (dict):
        output = {}
        # print("cur line: ", curLine)
        output['package'] = curLine.split("/")[0]
        lineParts = curLine.split(" ")
        # print("lineParts: ", lineParts)
        output['version'] = lineParts[1]

        output['installed'] = "installed" in curLine

        return output

    @staticmethod
    def getOqmPackagesList(filter: str = ALL_OQM, installed: bool = True, notInstalled: bool = True):
        logging.debug("Getting OQM packages.")
        result = PackageManagement.getOqmPackagesStr(filter, installed, notInstalled)
        # print("Package list str: " + result)
        result = result.splitlines()
        result = map(PackageManagement.packageLineToArray,result)
        # TODO:: debug
        # print("Package list: ", list(result))
        return result

    @staticmethod
    def ensureOnlyPluginsInstalled(pluginList:list) -> (bool, str):
        logging.debug("Ensuring only plugins in list installed.")

        allInstalledPlugins = map(
            lambda i: i['package'],
            PackageManagement.getOqmPackagesList(PackageManagement.OQM_PLUGINS, installed=True)
        )
        pluginsToRemove = [i for i in allInstalledPlugins if i not in pluginList]

        # TODO Try to figure out how to remove unwanted plugins while not bouncing dependency plugins
        # TODO:: error check
        PackageManagement.removePackages(pluginsToRemove)
        PackageManagement.installPackages(pluginList)

        ServiceUtils.doServiceCommand(ServiceStateCommand.restart, ServiceUtils.SERVICE_ALL)
