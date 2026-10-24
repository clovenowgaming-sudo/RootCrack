/* SPDX-License-Identifier: GPL-3.0-or-later
 *
 * kp_uapi.h — RootCrack's copy of the public supercall ABI exposed by the
 * KernelPatch kernel image (kpimg) that this manager drives.
 *
 * These syscall numbers and request codes are a fixed, published interface
 * contract of the kpimg binary we ship/download — they are not reimplemented
 * here, only declared, so our own daemon/JNI code can talk to it.
 */
#ifndef ROOTCRACK_KP_UAPI_H
#define ROOTCRACK_KP_UAPI_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

#define KP_SYS_SUPERCALL 45

#define KP_SC_HELLO        0x1000
#define KP_SC_KLOG         0x1004
#define KP_SC_BUILD_TIME   0x1007
#define KP_SC_KP_VERSION   0x1008
#define KP_SC_KERNEL_VERSION 0x1009

#define KP_SC_KEY_GET      0x100a
#define KP_SC_KEY_SET      0x100b
#define KP_SC_KEY_ROOT_ENABLE 0x100c

#define KP_SC_SU           0x1010
#define KP_SC_SU_TASK      0x1011

#define KP_SC_KPM_LOAD     0x1020
#define KP_SC_KPM_UNLOAD   0x1021
#define KP_SC_KPM_CONTROL  0x1022
#define KP_SC_KPM_NUMS     0x1030
#define KP_SC_KPM_LIST     0x1031
#define KP_SC_KPM_INFO     0x1032

#define KP_SC_KSTORAGE_ALLOC_GROUP 0x1040
#define KP_SC_KSTORAGE_WRITE       0x1041
#define KP_SC_KSTORAGE_READ        0x1042
#define KP_SC_KSTORAGE_LIST_IDS    0x1043
#define KP_SC_KSTORAGE_REMOVE      0x1044
#define KP_SC_KSTORAGE_REMOVE_GROUP 0x1045
#define KP_SC_CONTROL_FEATURE      0x1046

#define KP_KSTORAGE_SU_GROUP      0
#define KP_KSTORAGE_EXCLUDE_GROUP 1

#define KP_KEY_MAX_LEN 0x40
#define KP_SCONTEXT_LEN 0x60

struct kp_su_profile {
    uint32_t uid;
    uint32_t to_uid;
    char scontext[KP_SCONTEXT_LEN];
};

#ifdef __cplusplus
}
#endif

#endif // ROOTCRACK_KP_UAPI_H
