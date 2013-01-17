LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES:= ifconfig.c

LOCAL_MODULE := ifconfig

include $(BUILD_EXECUTABLE)