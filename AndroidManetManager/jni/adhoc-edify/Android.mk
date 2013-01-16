WORKING_PATH := $(call my-dir)

include $(CLEAR_VARS)


# build libedify.a
include $(WORKING_PATH)/edify/Android.mk


# set local path set that NDK will pull libraries from the Android source code
LOCAL_PATH := $(AOSP_SHARED_LIBRARIES_PATH)


include $(CLEAR_VARS)

LOCAL_MODULE     := cutils
LOCAL_SRC_FILES  := /libcutils_intermediates/LINKED/libcutils.so

include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE     := hardware_legacy
LOCAL_SRC_FILES  := /libhardware_legacy_intermediates/LINKED/libhardware_legacy.so

include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE     := libutils
LOCAL_SRC_FILES  := /libutils_intermediates/LINKED/libutils.so

include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE     := libbinder
LOCAL_SRC_FILES  := /libbinder_intermediates/LINKED/libbinder.so

include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE     := libwpa_client
LOCAL_SRC_FILES  := /libwpa_client_intermediates/LINKED/libwpa_client.so

include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE     := libnetutils
LOCAL_SRC_FILES  := /libnetutils_intermediates/LINKED/libnetutils.so

include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE     := libc
LOCAL_SRC_FILES  := /libc_intermediates/LINKED/libc.so

include $(PREBUILT_SHARED_LIBRARY)


# revert back to working path to build adhoc binary
LOCAL_PATH := $(WORKING_PATH)

updater_src_files := \
	install.c \
	adhoc.c 

include $(CLEAR_VARS)

LOCAL_SRC_FILES  += $(updater_src_files)

LOCAL_C_INCLUDES += $(dir $(inc))
LOCAL_C_INCLUDES += $(AOSP_PATH)/system/core/include
LOCAL_C_INCLUDES += $(AOSP_PATH)/hardware/libhardware_legacy/include

LOCAL_STATIC_LIBRARIES := edify

LOCAL_SHARED_LIBRARIES := hardware_legacy cutils utils binder libwpa_client libnetutils libc

LOCAL_MODULE := adhoc

include $(BUILD_EXECUTABLE)



