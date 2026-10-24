//! Command-line surface of `rfd`. This is what the Android app's su/shell
//! bridge and boot scripts invoke (e.g. `rfd module install x.zip`,
//! `rfd su-daemon`, `rfd kpm load foo.kpm`).

use anyhow::Result;
use clap::{Parser, Subcommand};
use std::path::PathBuf;

use crate::{kpm, module, sepolicy, su_profile, supercall};

#[derive(Parser)]
#[command(name = "rfd", about = "RootCrack userspace daemon")]
pub struct Cli {
    /// SuperKey issued at patch time; required for every kernel operation.
    #[arg(long, env = "ROOTCRACK_SUPERKEY")]
    pub key: String,

    #[command(subcommand)]
    pub cmd: Command,
}

#[derive(Subcommand)]
pub enum Command {
    /// Print kpimg hello banner + KernelPatch version, to sanity-check the key.
    Status,
    /// Run the early-boot staged-module apply + KPM autoload + policy patch.
    BootService,
    Module {
        #[command(subcommand)]
        action: ModuleAction,
    },
    Kpm {
        #[command(subcommand)]
        action: KpmAction,
    },
    Su {
        #[command(subcommand)]
        action: SuAction,
    },
}

#[derive(Subcommand)]
pub enum ModuleAction {
    List,
    Install { zip: PathBuf },
    Enable { id: String },
    Disable { id: String },
    Remove { id: String },
}

#[derive(Subcommand)]
pub enum KpmAction {
    List,
    Load { path: PathBuf, #[arg(default_value = "")] args: String },
    Unload { name: String },
}

#[derive(Subcommand)]
pub enum SuAction {
    Grant { uid: u32, #[arg(default_value_t = 0)] to_uid: u32 },
    Revoke { uid: u32 },
    List,
}

pub fn run(cli: Cli) -> Result<()> {
    match cli.cmd {
        Command::Status => {
            let hello = supercall::hello(&cli.key)?;
            let ver = supercall::kernelpatch_version(&cli.key)?;
            let root = supercall::root_enabled(&cli.key)?;
            println!("kpimg hello : {hello}");
            println!("KernelPatch : {ver}");
            println!("root enabled: {root}");
        }
        Command::BootService => {
            module::apply_staged_updates()?;
            let loaded = kpm::autoload_dir(&cli.key, std::path::Path::new(crate::defs::KPM_DIR))?;
            log::info!("autoloaded {} kpm module(s)", loaded.len());
            sepolicy::apply(&cli.key, &sepolicy::default_rules())?;
        }
        Command::Module { action } => match action {
            ModuleAction::List => {
                for m in module::list_modules()? {
                    println!("{} ({}) v{}", m.id, m.name, m.version);
                }
            }
            ModuleAction::Install { zip } => {
                let id = module::install_module_zip(&zip)?;
                println!("staged module '{id}', applies on next boot");
            }
            ModuleAction::Enable { id } => module::set_enabled(&id, true)?,
            ModuleAction::Disable { id } => module::set_enabled(&id, false)?,
            ModuleAction::Remove { id } => module::mark_for_removal(&id)?,
        },
        Command::Kpm { action } => match action {
            KpmAction::List => {
                for n in kpm::list_names(&cli.key)? {
                    println!("{n}");
                }
            }
            KpmAction::Load { path, args } => kpm::load(&cli.key, &path, &args)?,
            KpmAction::Unload { name } => kpm::unload(&cli.key, &name)?,
        },
        Command::Su { action } => {
            let mut store = su_profile::SuProfileStore::load()?;
            match action {
                SuAction::Grant { uid, to_uid } => {
                    store.grant(uid, to_uid, "u:r:magisk:s0");
                    store.save()?;
                    println!("granted su to uid {uid}");
                }
                SuAction::Revoke { uid } => {
                    store.revoke(uid);
                    store.save()?;
                    println!("revoked su from uid {uid}");
                }
                SuAction::List => {
                    for (uid, p) in &store.profiles {
                        println!("{uid} -> allow={} to_uid={}", p.allow, p.to_uid);
                    }
                }
            }
        }
    }
    Ok(())
}
