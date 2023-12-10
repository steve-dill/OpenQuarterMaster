#!/bin/python3
import os

import docker
from docker.models.containers import Container
import logging
import sys
import json
sys.path.append("/usr/lib/oqm/station-captain/")
from ConfigManager import *

KC_CONTAINER_NAME = "oqm_infra_keycloak"
KC_ADM_SCRIPT = "/opt/keycloak/bin/kcadm.sh"
KC_REALM = "oqm"
KC_CLIENT_CONFIGS_DIR = "/etc/oqm/kcClient/"
logging.basicConfig(level=logging.DEBUG)


def getClientConfigs() -> list:
    configList = os.listdir(KC_CLIENT_CONFIGS_DIR)
    configList = [KC_CLIENT_CONFIGS_DIR + i for i in configList if os.path.isfile(i) and i.endswith(".json")]

    def fileToJson(jsonFile : str) -> object:
        with open(jsonFile) as file:
            return json.load(file)

    configList: list = map(fileToJson, configList)
    logging.debug("Client configs found: [%s]", ' '.join(configList))
    return configList


def getKcContainer() -> Container:
    client = docker.from_env()
    return client.containers.get(KC_CONTAINER_NAME)


def setupAdminConfig(kcContainer: Container | None = None):
    logging.info("Setting up keycloak admin config for future calls.")
    if kcContainer is None:
        kcContainer = getKcContainer()

    runResult = kcContainer.exec_run(
        [
            KC_ADM_SCRIPT, "config", "credentials", "--server", "http://127.0.0.1:8080",
            "--realm", "master",  # admin user exists in master realm
            "--user", mainCM.getConfigVal("infra.keycloak.adminUser"),
            "--password", mainCM.getConfigVal("infra.keycloak.adminPass")
        ])
    if runResult.exit_code != 0:
        logging.error("Failed to setup kc admin credentials: %s", runResult.output)
        raise ChildProcessError("Failed to setup admin credentials")
    logging.debug("Setting up KC creds output: %s", runResult.output)


def updateKc():
    kcContainer = getKcContainer()
    setupAdminConfig(kcContainer)

    runResult = kcContainer.exec_run([
            KC_ADM_SCRIPT,
            "update",
            "realms/"+KC_REALM,
            "-s", "registrationAllowed=" + str(mainCM.getConfigVal("infra.keycloak.options.userSelfRegistration"))
        ])
    if runResult.exit_code != 0:
        logging.error("Failed to set registration allowed: %s", runResult.output)
        raise ChildProcessError("Failed to set registration allowed")
    logging.debug("Setting up KC registration allowed: %s", runResult.output)

    for curClient in getClientConfigs():
        logging.info("Processing client %s", curClient['clientName'])
        # TODO::


updateKc()
