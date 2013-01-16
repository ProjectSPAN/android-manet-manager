# NOTE: Android NDK will not build a static library unless some other build process
# in the Android.mk makes use of it (i.e. NDK attempts to "optimize out" the need to
# build the static library).

LOCAL_PATH := $(call my-dir)

edify_src_files := \
	expr.c \
	lex.yy.c \
	parser.c

# "-x c" forces the lex/yacc files to be compiled as c;
# the build system otherwise forces them to be c++.
edify_cflags := -x c

#
# Build the device-side library
#
include $(CLEAR_VARS)

LOCAL_MODULE := libedify

LOCAL_CFLAGS := $(edify_cflags)

LOCAL_SRC_FILES := $(edify_src_files)

include $(BUILD_STATIC_LIBRARY)
