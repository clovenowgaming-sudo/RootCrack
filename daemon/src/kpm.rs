//! KPM (kernel module) control: load/unload .kpm files into the running
//! kpimg image via supercall, and list what's currently loaded. The .kpm
//! binary format itself is produced/consumed entirely inside kpimg; this
//! file only marshals the load/unload/list requests.

use anyhow::{bail, Result};
use std::ffi::CString;
use std::fs;
use std::path::Path;

use crate::supercall;

#[derive(Debug, Clone)]
pub struct KpmInfo {
    pub name: String,
    pub version: String,
    pub license: String,
    pub author: String,
    pub description: String,
}

pub fn load(key: &str, kpm_path: &Path, args: &str) -> Result<()> {
    if !kpm_path.exists() {
        bail!("kpm file not found: {kpm_path:?}");
    }
    let path_c = CString::new(kpm_path.to_string_lossy().as_bytes())?;
    let args_c = CString::new(args)?;
    supercall::call(
        key,
        supercall::SC_KPM_LOAD,
        path_c.as_ptr() as i64,
        args_c.as_ptr() as i64,
        0,
    )?;
    Ok(())
}

pub fn unload(key: &str, name: &str) -> Result<()> {
    let name_c = CString::new(name)?;
    supercall::call(key, supercall::SC_KPM_UNLOAD, name_c.as_ptr() as i64, 0, 0)?;
    Ok(())
}

pub fn count(key: &str) -> Result<i64> {
    supercall::kpm_count(key)
}

pub fn list_names(key: &str) -> Result<Vec<String>> {
    let n = count(key)?.max(0) as usize;
    let mut buf = vec![0u8; (n + 1) * 64];
    supercall::call(
        key,
        supercall::SC_KPM_LIST,
        buf.as_mut_ptr() as i64,
        buf.len() as i64,
        0,
    )?;
    Ok(buf
        .split(|&b| b == 0)
        .filter(|s| !s.is_empty())
        .map(|s| String::from_utf8_lossy(s).to_string())
        .collect())
}

/// Autoloads every .kpm file under the given directory at boot, in
/// filename order. Used for "install module that ships a kernel module"
/// style KPM modules.
pub fn autoload_dir(key: &str, dir: &Path) -> Result<Vec<String>> {
    let mut loaded = Vec::new();
    if !dir.exists() {
        return Ok(loaded);
    }
    let mut entries: Vec<_> = fs::read_dir(dir)?.filter_map(|e| e.ok()).collect();
    entries.sort_by_key(|e| e.file_name());
    for entry in entries {
        let path = entry.path();
        if path.extension().and_then(|e| e.to_str()) == Some("kpm") {
            if let Err(e) = load(key, &path, "") {
                log::warn!("failed to autoload kpm {path:?}: {e}");
                continue;
            }
            loaded.push(path.display().to_string());
        }
    }
    Ok(loaded)
}
