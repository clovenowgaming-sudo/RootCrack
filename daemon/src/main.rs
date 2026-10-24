mod cli;
mod defs;
mod kpm;
mod module;
mod sepolicy;
mod su_profile;
mod supercall;

use clap::Parser;

fn main() -> anyhow::Result<()> {
    #[cfg(target_os = "android")]
    android_logger::init_once(android_logger::Config::default().with_tag("rfd"));
    #[cfg(not(target_os = "android"))]
    env_logger_init();

    let cli = cli::Cli::parse();
    cli::run(cli)
}

#[cfg(not(target_os = "android"))]
fn env_logger_init() {
    let _ = env_logger::try_init();
}
