# !/bin/bash

FILEPATH=$1
FILEKEY=$2

curl -S "http://134.175.176.173:8080/api/upload" -F "file=@${FILEPATH}" -F "fileName=simplereminder-debug" -F "description=Simple Reminder Debug" -F "version=1.0.0" -F "key=${FILEKEY}"
