/* SPDX-License-Identifier: GPL-3.0-or-later
 *
 * native_bridge.cpp — JNI glue between the RootCrack manager app and the
 * kpimg supercall ABI (kp_uapi.h). Thin wrapper only: it issues the raw
 * supercall syscall and marshals results back to Kotlin. All actual
 * privileged work happens inside kpimg (kernel) or the rfd daemon
 * (userspace, Rust) — this layer never runs as root itself.
 */
#include <jni.h>
#include <unistd.h>
#include <sys/syscall.h>
#include <cstring>
#include <android/log.h>
#include "kp_uapi.h"

#define LOG_TAG "RootCrackNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

namespace {

long supercall(const char *key, long cmd, long a1 = 0, long a2 = 0, long a3 = 0) {
    return syscall(KP_SYS_SUPERCALL, key, cmd, a1, a2, a3);
}

} // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_com_root_crack_Natives_nativeHello(JNIEnv *env, jobject, jstring jkey) {
    const char *key = env->GetStringUTFChars(jkey, nullptr);
    char buf[256] = {0};
    long rc = supercall(key, KP_SC_HELLO, reinterpret_cast<long>(buf), sizeof(buf));
    env->ReleaseStringUTFChars(jkey, key);
    if (rc < 0) return env->NewStringUTF("");
    return env->NewStringUTF(buf);
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_root_crack_Natives_nativeKernelPatchVersion(JNIEnv *env, jobject, jstring jkey) {
    const char *key = env->GetStringUTFChars(jkey, nullptr);
    char buf[64] = {0};
    long rc = supercall(key, KP_SC_KP_VERSION, reinterpret_cast<long>(buf), sizeof(buf));
    env->ReleaseStringUTFChars(jkey, key);
    if (rc < 0) return env->NewStringUTF("unknown");
    return env->NewStringUTF(buf);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_root_crack_Natives_nativeSuEnabled(JNIEnv *env, jobject, jstring jkey) {
    const char *key = env->GetStringUTFChars(jkey, nullptr);
    long rc = supercall(key, KP_SC_KEY_ROOT_ENABLE, 1);
    env->ReleaseStringUTFChars(jkey, key);
    return rc == 0 ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_root_crack_Natives_nativeKpmNums(JNIEnv *env, jobject, jstring jkey) {
    const char *key = env->GetStringUTFChars(jkey, nullptr);
    long rc = supercall(key, KP_SC_KPM_NUMS);
    env->ReleaseStringUTFChars(jkey, key);
    return static_cast<jint>(rc);
}
