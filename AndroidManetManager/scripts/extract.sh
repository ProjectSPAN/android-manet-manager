#!/bin/sh

# usage: 
# ./scripts/extract.sh <deviceid> <remotezip> <remotepath>

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "Accept SuperUser prompts on device ..."
echo
adb -s $1 push $DIR/bin/unzip /data/local/tmp/unzip
adb -s $1 shell mkdir -p $3
adb -s $1 shell su -c "chmod 777 /data/local/tmp/unzip"
adb -s $1 shell su -c "./data/local/tmp/unzip -o $2 -d $3"
adb -s $1 shell rm /data/local/tmp/unzip
