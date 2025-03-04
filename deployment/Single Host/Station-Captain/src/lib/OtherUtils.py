import os
import shutil
from ConfigManager import *
from ServiceUtils import *
import docker
from LogUtils import *


class OtherUtils:
    log = LogUtils.setupLogger("OtherUtils")

    @staticmethod
    def human_size(numBytes: int, units=None):
        """
        Returns a human readable string representation of bytes
        https://stackoverflow.com/questions/1094841/get-human-readable-version-of-file-size
        """
        if units is None:
            units = [' bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB']
        return str(numBytes) + units[0] if numBytes < 1024 else OtherUtils.human_size(numBytes >> 10, units[1:])
