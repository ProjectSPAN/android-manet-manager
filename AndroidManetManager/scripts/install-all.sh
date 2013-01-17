#!/bin/bash

# usage: 
# ./scripts/install-all.sh [manager] [logger]

#http://www.cs.utah.edu/dept/old/texinfo/gawk/gawk_4.html#SEC9
#http://www.tek-tips.com/faqs.cfm?fid=1281

# PREBUILT ANDROID SDK [has valid targets]
# export PATH=$PATH:~/Desktop/android-sdk-linux_x86/tools
# export PATH=$PATH:~/Desktop/android-sdk-linux_x86/platform-tools

# create build.xml
#
# android list targets
#
#   id: 9 or "android-10"
#      Name: Android 2.3.3
#      Type: Platform
#      API level: 10
#      Revision: 2
#      Skins: WQVGA432, QVGA, WQVGA400, HVGA, WVGA800 (default), WVGA854
#
# android update project --path . --target 9

# build apk
# ant debug

if [ "$1" == "-c" ] || [ "$2" == "-c" ] || [ "$3" == "-c" ] || [ "$4" == "-c" ]; then
	# copy each device's config file to sdcard
	adb devices | awk '$2 == "device" { print "adb -s "$1" shell \"su -c \\\"cp data/data/org.span/conf/manet.conf /sdcard/manet.conf\\\"\"" }' | sh -x
fi

if [ "$1" == "manager" ] || [ "$2" == "manager" ] || [ "$3" == "manager" ] || [ "$4" == "manager" ]; then
	# variables
	package='org.span'
	apk='../../android-manet-manager/AndroidManetManager/bin/AndroidManetManager.apk'

	# unload each device
	adb devices | awk '$2 == "device" { print "adb -s "$1" uninstall '"$package"'" }' | sh -x

	# load each device
	adb devices | awk '$2 == "device" { print "adb -s "$1" install -r '"$apk"'" }' | sh -x
fi

if [ "$1" == "logger" ] || [ "$2" == "logger" ] || [ "$3" == "logger" ] || [ "$4" == "logger" ]; then
	# variables
	package='org.span.logger'
	apk='../../android-manet-logger/AndroidManetLogger/bin/AndroidManetLogger.apk'

	# unload each device
	adb devices | awk '$2 == "device" { print "adb -s "$1" uninstall '"$package"'" }' | sh -x

	# load each device
	adb devices | awk '$2 == "device" { print "adb -s "$1" install -r '"$apk"'" }' | sh -x
fi
