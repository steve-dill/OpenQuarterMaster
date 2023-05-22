#!/bin/bash
echo "script2 got $# args: $@"

HELPTEXT="Usage:"

ARGS_SHORT="hbrd:"
ARGS_LONG="help,backup,restore,dir:"
OPTS=$(getopt -a -n backup-restore-base.sh --options $ARGS_SHORT --longoptions $ARGS_LONG -- "$@")

VALID_ARGUMENTS=$# # Returns the count of arguments that are in short or long options
if [ "$VALID_ARGUMENTS" -eq 0 ]; then
        echo "$HELPTEXT";
        exit 1;
fi

mode=""
targetDir=""

eval set -- "$OPTS"
while :
do
        case "$1" in
                -h | --help)
                        echo "$HELPTEXT";
                        exit;
                ;;
                -b | --backup)
                        mode="backup"
                        shift
                ;;
                -r | --restore)
                        mode="restore"
                        shift
                ;;
                -d | --dir)
                        targetDir="$2"
                        shift 2
                ;;
                --)
                        shift;
                        break
                ;;
        esac
done

if [ -z "$mode" ]; then
        echo "No mode given."
        exit 2;
fi
if [ -z "$targetDir" ]; then
        echo "No target directory given."
        exit 2;
fi

#echo "$mode"
#echo "$targetDir"
export mode
export targetDir