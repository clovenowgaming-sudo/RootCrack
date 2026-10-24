//! Thin Rust wrapper around the kpimg supercall syscall. This talks to a
//! fixed, external kernel ABI (see app/src/main/cpp/kp_uapi.h for the C
//! mirror of the same constants) — it issues syscalls, it does not
//! implement kernel-side patching logic itself.

use anyhow::{bail, Result};
use std::ffi::CString;

pub const SYS_SUPERCALL: i64 = 45;

pub const SC_HELLO: i64 = 0x1000;
pub const SC_KLOG: i64 = 0x1004;
pub const SC_KP_VERSION: i64 = 0x1008;
pub const SC_KEY_ROOT_ENABLE: i64 = 0x100c;
pub const SC_SU: i64 = 0x1010;
pub const SC_SU_TASK: i64 = 0x1011;
pub const SC_KPM_LOAD: i64 = 0x1020;
pub const SC_KPM_UNLOAD: i64 = 0x1021;
pub const SC_KPM_CONTROL: i64 = 0x1022;
pub const SC_KPM_NUMS: i64 = 0x1030;
pub const SC_KPM_LIST: i64 = 0x1031;
pub const SC_KPM_INFO: i64 = 0x1032;
pub const SC_KSTORAGE_WRITE: i64 = 0x1041;
pub const SC_KSTORAGE_READ: i64 = 0x1042;
pub const SC_KSTORAGE_LIST_IDS: i64 = 0x1043;
pub const SC_KSTORAGE_REMOVE: i64 = 0x1044;

/// Raw supercall. `key` is the daemon's SuperKey, issued once at install
/// time and required by kpimg for every privileged request.
pub fn call(key: &str, cmd: i64, a1: i64, a2: i64, a3: i64) -> Result<i64> {
    let ckey = CString::new(key)?;
    let rc = unsafe {
        libc::syscall(
            SYS_SUPERCALL as libc::c_long,
            ckey.as_ptr(),
            cmd as libc::c_long,
            a1 as libc::c_long,
            a2 as libc::c_long,
            a3 as libc::c_long,
        )
    };
    if rc < 0 {
        bail!("supercall 0x{cmd:x} failed: errno {}", std::io::Error::last_os_error());
    }
    Ok(rc as i64)
}

pub fn hello(key: &str) -> Result<String> {
    let mut buf = vec![0u8; 256];
    call(key, SC_HELLO, buf.as_mut_ptr() as i64, buf.len() as i64, 0)?;
    let end = buf.iter().position(|&b| b == 0).unwrap_or(buf.len());
    Ok(String::from_utf8_lossy(&buf[..end]).to_string())
}

pub fn kernelpatch_version(key: &str) -> Result<String> {
    let mut buf = vec![0u8; 64];
    call(key, SC_KP_VERSION, buf.as_mut_ptr() as i64, buf.len() as i64, 0)?;
    let end = buf.iter().position(|&b| b == 0).unwrap_or(buf.len());
    Ok(String::from_utf8_lossy(&buf[..end]).to_string())
}

pub fn root_enabled(key: &str) -> Result<bool> {
    Ok(call(key, SC_KEY_ROOT_ENABLE, 1, 0, 0)? == 0)
}

pub fn kpm_count(key: &str) -> Result<i64> {
    call(key, SC_KPM_NUMS, 0, 0, 0)
}
