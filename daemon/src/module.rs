//! Module lifecycle: install from zip, enable/disable, overlay mount at
//! boot, and bookkeeping of module.prop metadata. This mirrors the
//! Magisk-style module contract (post-fs-data.sh / service.sh / system/
//! overlay) but is an independent implementation.

use anyhow::{bail, Context, Result};
use serde::{Deserialize, Serialize};
use std::fs;
use std::path::{Path, PathBuf};
use std::process::Command;

use crate::defs::*;

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
pub struct ModuleProp {
    pub id: String,
    pub name: String,
    pub version: String,
    pub version_code: i64,
    pub author: String,
    pub description: String,
}

pub fn parse_module_prop(dir: &Path) -> Result<ModuleProp> {
    let text = fs::read_to_string(dir.join(MODULE_PROP_FILE))
        .with_context(|| format!("missing {MODULE_PROP_FILE} in {dir:?}"))?;
    let mut prop = ModuleProp::default();
    for line in text.lines() {
        let Some((k, v)) = line.split_once('=') else { continue };
        let v = v.trim().to_string();
        match k.trim() {
            "id" => prop.id = v,
            "name" => prop.name = v,
            "version" => prop.version = v,
            "versionCode" => prop.version_code = v.parse().unwrap_or(0),
            "author" => prop.author = v,
            "description" => prop.description = v,
            _ => {}
        }
    }
    if prop.id.is_empty() {
        bail!("module.prop in {dir:?} has no id");
    }
    Ok(prop)
}

pub fn list_modules() -> Result<Vec<ModuleProp>> {
    let mut out = Vec::new();
    let root = Path::new(MODULES_DIR);
    if !root.exists() {
        return Ok(out);
    }
    for entry in fs::read_dir(root)? {
        let entry = entry?;
        if !entry.file_type()?.is_dir() {
            continue;
        }
        if let Ok(prop) = parse_module_prop(&entry.path()) {
            out.push(prop);
        }
    }
    Ok(out)
}

/// Extracts a module zip into modules_update/<id>, to be swapped into
/// place on next boot (same staged-install pattern Magisk/APatch use so a
/// half-installed module never corrupts a currently-mounted one).
pub fn install_module_zip(zip_path: &Path) -> Result<String> {
    let staging_root = Path::new(MODULES_UPDATE_DIR);
    fs::create_dir_all(staging_root)?;

    let tmp = staging_root.join(".tmp_extract");
    let _ = fs::remove_dir_all(&tmp);
    fs::create_dir_all(&tmp)?;

    let status = Command::new("unzip")
        .arg("-oq")
        .arg(zip_path)
        .arg("-d")
        .arg(&tmp)
        .status()
        .context("spawn unzip")?;
    if !status.success() {
        bail!("unzip failed for {zip_path:?}");
    }

    let prop = parse_module_prop(&tmp)?;
    let dest = staging_root.join(&prop.id);
    let _ = fs::remove_dir_all(&dest);
    fs::rename(&tmp, &dest)?;

    fs::write(root_join(MODULES_DIR, &prop.id).join(MODULE_UPDATE_FILE), "")?;
    Ok(prop.id)
}

fn root_join(root: &str, id: &str) -> PathBuf {
    Path::new(root).join(id)
}

pub fn set_enabled(id: &str, enabled: bool) -> Result<()> {
    let flag = Path::new(MODULES_DIR).join(id).join(MODULE_DISABLE_FILE);
    if enabled {
        if flag.exists() {
            fs::remove_file(flag)?;
        }
    } else {
        fs::write(flag, "")?;
    }
    Ok(())
}

pub fn mark_for_removal(id: &str) -> Result<()> {
    fs::write(Path::new(MODULES_DIR).join(id).join(MODULE_REMOVE_FILE), "")?;
    Ok(())
}

/// Applies staged installs/removals from modules_update/ into modules/.
/// Intended to run once at early boot (post-fs-data stage), before
/// anything mounts module overlays.
pub fn apply_staged_updates() -> Result<()> {
    let staging_root = Path::new(MODULES_UPDATE_DIR);
    let modules_root = Path::new(MODULES_DIR);
    fs::create_dir_all(modules_root)?;
    if !staging_root.exists() {
        return Ok(());
    }
    for entry in fs::read_dir(staging_root)? {
        let entry = entry?;
        if !entry.file_type()?.is_dir() {
            continue;
        }
        let id = entry.file_name();
        let dest = modules_root.join(&id);
        let _ = fs::remove_dir_all(&dest);
        fs::rename(entry.path(), &dest)?;
    }
    Ok(())
}
