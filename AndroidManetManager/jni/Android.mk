AOSP_PATH := /home/dev/Desktop/PROJECTS/ANDROID_PLATFORM_GINGERBREAD
AOSP_SHARED_LIBRARIES_PATH := $(AOSP_PATH)/out/target/product/generic/obj/SHARED_LIBRARIES

LOCAL_PATH := $(my-dir)

PROJECT_PATH := $(call parent-dir, $(LOCAL_PATH))
PROJECT_LIBS_PATH := $(PROJECT_PATH)/libs/armeabi
PROJECT_RAW_PATH  := $(PROJECT_PATH)/res/raw

include $(call all-makefiles-under, $(LOCAL_PATH))

	