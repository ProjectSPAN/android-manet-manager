#!/bin/sh

# usage: 
# ./scripts/push-all.sh <localfile> <remotefile>
# ./scripts/push-all.sh ~/Desktop/APPS/mitremaps/kc_maps.zip /sdcard/mitremaps/kc_maps.zip

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# multiple files (takes much longer than a zip)
# adb devices | awk '$2 == "device" { print "gnome-terminal -t "$1" -x bash -c \" \
#	(adb -s "$1" push '"$1"' '"$2"') ; (exec /bin/bash -i) \"" }' | sh -x

# large file
adb devices | awk '$2 == "device" { print "gnome-terminal -t "$1" -x bash -c \" '"$DIR"'/push.sh "$1" '"$1"' '"$2"' ; read -p '"'Done! Press [enter] to exit ...'"' \" " }' | sh -x
