use std::fs;
use std::path::Path;

/// Mirrors app/src/main/cpp/kp_abi_version into a Rust const so the daemon
/// reports the same KernelPatch ABI target the app was built against.
fn main() {
    let header_path = Path::new("../app/src/main/cpp/kp_abi_version");
    let (major, minor, patch) = if let Ok(text) = fs::read_to_string(header_path) {
        let get = |name: &str| -> u32 {
            text.lines()
                .find(|l| l.contains(name))
                .and_then(|l| l.split_whitespace().last())
                .and_then(|v| v.parse().ok())
                .unwrap_or(0)
        };
        (get("MAJOR"), get("MINOR"), get("PATCH"))
    } else {
        (0, 13, 9)
    };
    println!("cargo:rustc-env=KP_ABI_VERSION={major}.{minor}.{patch}");
    println!("cargo:rerun-if-changed={}", header_path.display());
}
