#!/bin/sh

# usage: 
# ./scripts/build-native.sh

# variables
# aosp_path='/home/dev/Desktop/PROJECTS/ANDROID'


# prepare to build "adhoc" binary

# generate "lex.yy.c":

cd ./jni/adhoc-edify/edify

flex lexer.l

# generate "parser.h" and "parser.c"

# export BISON_PKGDATADIR="$aosp_path/external/bison/data"

bison --defines=parser.h --output=parser.c parser.y

cd ../../..


# execute NDK build script

ndk-build


# copy binaries

cp ./libs/armeabi/adhoc ./res/raw/adhoc

cp ./libs/armeabi/ifconfig ./res/raw/ifconfig

cp ./libs/armeabi/iwconfig ./res/raw/iwconfig


