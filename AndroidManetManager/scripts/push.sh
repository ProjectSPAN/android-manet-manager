#!/bin/sh

# usage: 
# ./scripts/push.sh <deviceid> <localfile> <remotefile>

watch -n 1 adb -s $1 shell ls -l $3 & 
echo $! > $1.pid
adb -s $1 push $2 $3 > /dev/null 2>&1
cat $1.pid | xargs kill
rm $1.pid
