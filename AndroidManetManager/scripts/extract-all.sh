#!/bin/sh

# usage: 
# ./scripts/extract-all.sh <remotezip> <remotepath>
# ./scripts/extract-all.sh /sdcard/mitremaps/kc_maps.zip /sdcard/mitremaps

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# extract zip on each device
adb devices | awk '$2 == "device" { print "gnome-terminal -t "$1" -x bash -c \" '"$DIR"'/extract.sh "$1" '"$1"' '"$2"' ; echo ; read -p '"'Done! Press [enter] to exit ...'"' \" " }' | sh -x
