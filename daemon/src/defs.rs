//! Shared filesystem paths and constants for the rfd daemon.

pub const WORKING_DIR: &str = "/data/adb/rootcrack";
pub const MODULES_DIR: &str = "/data/adb/rootcrack/modules";
pub const MODULES_UPDATE_DIR: &str = "/data/adb/rootcrack/modules_update";
pub const SU_PROFILE_FILE: &str = "/data/adb/rootcrack/su_profiles.json";
pub const KPM_DIR: &str = "/data/adb/rootcrack/kpm";
pub const LOG_DIR: &str = "/data/adb/rootcrack/log";
pub const BINARY_NAME: &str = "rfd";

pub const MODULE_PROP_FILE: &str = "module.prop";
pub const MODULE_DISABLE_FILE: &str = "disable";
pub const MODULE_REMOVE_FILE: &str = "remove";
pub const MODULE_UPDATE_FILE: &str = "update";
pub const MODULE_SKIP_MOUNT_FILE: &str = "skip_mount";
