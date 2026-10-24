//! Manages which apps are granted su, and under what identity/SELinux
//! context — the userspace policy that fronts SC_SU / SC_SU_TASK.

use anyhow::{Context, Result};
use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use std::fs;
use std::path::Path;

use crate::defs::SU_PROFILE_FILE;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SuProfile {
    pub uid: u32,
    pub to_uid: u32,
    pub scontext: String,
    pub allow: bool,
    pub log_access: bool,
    pub notify: bool,
}

impl Default for SuProfile {
    fn default() -> Self {
        Self {
            uid: 0,
            to_uid: 0,
            scontext: "u:r:magisk:s0".to_string(),
            allow: false,
            log_access: true,
            notify: true,
        }
    }
}

#[derive(Debug, Default, Serialize, Deserialize)]
pub struct SuProfileStore {
    pub profiles: HashMap<u32, SuProfile>,
}

impl SuProfileStore {
    pub fn load() -> Result<Self> {
        let path = Path::new(SU_PROFILE_FILE);
        if !path.exists() {
            return Ok(Self::default());
        }
        let text = fs::read_to_string(path).context("read su profile store")?;
        Ok(serde_json::from_str(&text).unwrap_or_default())
    }

    pub fn save(&self) -> Result<()> {
        if let Some(parent) = Path::new(SU_PROFILE_FILE).parent() {
            fs::create_dir_all(parent)?;
        }
        let text = serde_json::to_string_pretty(self)?;
        fs::write(SU_PROFILE_FILE, text)?;
        Ok(())
    }

    pub fn grant(&mut self, uid: u32, to_uid: u32, scontext: &str) {
        self.profiles.insert(
            uid,
            SuProfile { uid, to_uid, scontext: scontext.to_string(), allow: true, ..Default::default() },
        );
    }

    pub fn revoke(&mut self, uid: u32) {
        self.profiles.remove(&uid);
    }

    pub fn is_allowed(&self, uid: u32) -> bool {
        self.profiles.get(&uid).map(|p| p.allow).unwrap_or(false)
    }
}
