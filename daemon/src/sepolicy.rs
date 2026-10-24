//! SELinux policy live-patch helper. Builds a small set of `allow`/`type`
//! statements needed for the daemon + modules to operate under enforcing
//! SELinux, and ships them to kpimg via the kernel storage supercalls
//! rather than writing policy ourselves — kpimg owns policy loading.

use anyhow::Result;
use std::ffi::CString;

use crate::supercall;

pub struct PolicyRule {
    pub statement: String,
}

pub fn default_rules() -> Vec<PolicyRule> {
    vec![
        "allow rootcrack_daemon self capability2 { mac_admin }".to_string(),
        "allow rootcrack_daemon kernel process { fork sigchld }".to_string(),
        "typeattribute rootcrack_daemon mlstrustedsubject".to_string(),
    ]
    .into_iter()
    .map(|statement| PolicyRule { statement })
    .collect()
}

pub fn apply(key: &str, rules: &[PolicyRule]) -> Result<()> {
    for rule in rules {
        let data = CString::new(rule.statement.as_bytes())?;
        supercall::call(
            key,
            supercall::SC_KSTORAGE_WRITE,
            0, // KP_KSTORAGE_SU_GROUP-equivalent policy group id, assigned by kpimg
            data.as_ptr() as i64,
            rule.statement.len() as i64,
        )?;
    }
    Ok(())
}
