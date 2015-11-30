LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := eFlyerHandler
LOCAL_SRC_FILES := eFlyerHandler.c
LOCAL_SHARED_LIBRARIES := gstreamer_android
LOCAL_LDLIBS := -landroid -llog
include $(BUILD_SHARED_LIBRARY)

GSTREAMER_ROOT        := /home/yasmidrog/ndks/gstreamer-1.0-android-arm-1.6.1
GSTREAMER_NDK_BUILD_PATH  := $(GSTREAMER_ROOT)/share/gst-android/ndk-build
include $(GSTREAMER_NDK_BUILD_PATH)/plugins.mk
GSTREAMER_PLUGINS         := $(GSTREAMER_PLUGINS_CORE) $(GSTREAMER_PLUGINS_PLAYBACK) $(GSTREAMER_PLUGINS_CODECS) $(GSTREAMER_PLUGINS_CODECS_RESTRICTED) $(GSTREAMER_PLUGINS_NET) $(GSTREAMER_PLUGINS_SYS)
GSTREAMER_EXTRA_DEPS      := gstreamer-video-1.0 
include $(GSTREAMER_NDK_BUILD_PATH)/gstreamer-1.0.mk