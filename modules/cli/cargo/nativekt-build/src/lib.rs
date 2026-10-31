use std::env;
use std::fs;
use std::io;
use std::io::Error;
use std::path::{Path, PathBuf};
use std::process::Command;

#[cfg(all(
    feature = "bundled",
    target_os = "linux",
    target_arch = "x86_64",
    target_env = "gnu"
))]
const CLI: Option<&[u8]> = Some(include_bytes!(concat!("../bin/nativekt-cli-linux-x64")));

#[cfg(all(
    feature = "bundled",
    target_os = "linux",
    target_arch = "aarch64",
    target_env = "gnu"
))]
const CLI: Option<&[u8]> = Some(include_bytes!(concat!("../bin/nativekt-cli-linux-arm64")));

#[cfg(all(feature = "bundled",
    target_os = "macos",
    target_arch = "aarch64"
))]
const CLI: Option<&[u8]> = Some(include_bytes!(concat!("../bin/nativekt-cli-macos-arm64")));

#[cfg(all(
    feature = "bundled",
    target_os = "windows",
    any(target_arch = "x86_64", target_arch = "aarch64")
))]
const CLI: Option<&[u8]> = Some(include_bytes!(concat!(
    "../bin/nativekt-cli-windows-x64.exe"
)));

#[cfg(not(all(
    feature = "bundled",
    any(
        all(
            target_os = "linux",
            target_env = "gnu",
            any(target_arch = "x86_64", target_arch = "aarch64")
        ),
        all(target_os = "macos", target_arch = "aarch64"),
        all(
            target_os = "windows",
            any(target_arch = "x86_64", target_arch = "aarch64")
        ),
    )
)))]
const CLI: Option<&[u8]> = None;

/// Generates `OUT_DIR/nativekt.rs` and returns its absolute path.
pub fn generate(config_file: impl AsRef<Path>) -> io::Result<PathBuf> {
    generate_impl(config_file.as_ref(), None)
}

/// Generates bindings with the host CLI found in `bin_dir`.
/// Files are named `nativekt-cli-<platform>` (with `.exe` for Windows).
pub fn generate_with_bin_dir(
    config_file: impl AsRef<Path>,
    bin_dir: impl AsRef<Path>,
) -> io::Result<PathBuf> {
    generate_impl(config_file.as_ref(), Some(bin_dir.as_ref()))
}

fn generate_impl(config_file: &Path, bin_dir: Option<&Path>) -> io::Result<PathBuf> {
    let manifest_dir = PathBuf::from(env::var_os("CARGO_MANIFEST_DIR").unwrap());
    let out_dir = PathBuf::from(env::var_os("OUT_DIR").unwrap());
    let config = manifest_dir.join(config_file);
    println!("cargo:rerun-if-changed={}", config.display());

    // Extract CLI tool
    let tool = match bin_dir {
        Some(directory) => {
            let tool = cli_from_dir(&manifest_dir, directory)?;
            println!("cargo:rerun-if-changed={}", tool.display());
            tool
        }
        None => match CLI {
            Some(bytes) => extract_cli(&out_dir, bytes)?,
            None => {
                return Err(Error::other("No bundled Native-Kt CLI for this host"));
            }
        },
    };

    // Get NDL path
    let ndl_path = PathBuf::from(checked_output(
        Command::new(&tool)
            .args(["print-ndl-path", "--config"])
            .arg(&config)
            .current_dir(&manifest_dir),
        "NDL path query",
    )?);
    println!("cargo:rerun-if-changed={}", ndl_path.display());

    // Get Name
    let name = checked_output(
        Command::new(&tool)
            .args(["print-name", "--config"])
            .arg(&config)
            .current_dir(&manifest_dir),
        "Name query",
    )?;

    // Generate
    fs::create_dir_all(&out_dir)?;
    checked_output(
        Command::new(&tool)
            .args(["generate", "--config"])
            .arg(&config)
            .arg("--output")
            .arg(&out_dir)
            .current_dir(&manifest_dir),
        "generation",
    )?;

    // Generate description
    fs::write(out_dir.join(name.clone() + ".nativekt.txt"), config.display().to_string())
        .expect("Failed to write description");

    Ok(out_dir.join(name + ".nativekt.rs"))
}

fn cli_from_dir(manifest_dir: &Path, bin_dir: &Path) -> io::Result<PathBuf> {
    let directory = manifest_dir.join(bin_dir);
    if !directory.is_dir() {
        return Err(Error::other(format!(
            "Native-Kt binary directory does not exist or is not a directory: {}",
            directory.display(),
        )));
    }
    let linux_gnu = cfg!(target_env = "gnu");
    let name = match (env::consts::OS, env::consts::ARCH) {
        ("linux", "x86_64") if linux_gnu => "nativekt-cli-linux-x64",
        ("linux", "aarch64") if linux_gnu => "nativekt-cli-linux-arm64",
        ("macos", "aarch64") => "nativekt-cli-macos-arm64",
        ("windows", "x86_64" | "aarch64") => "nativekt-cli-windows-x64.exe",
        _ => {
            return Err(Error::other(format!(
                "Unsupported Native-Kt CLI host: {}/{} (Linux requires glibc)",
                env::consts::OS, env::consts::ARCH
            )))
        },
    };
    Ok(directory.join(name))
}

fn checked_output(command: &mut Command, operation: &str) -> Result<String, Error> {
    let output = command.output().map_err(|error| {
        Error::new(
            error.kind(),
            format!("Cannot start Native-Kt {operation}: {error}"),
        )
    })?;
    if !output.status.success() {
        return Err(Error::other(format!(
            "Native-Kt {operation} failed ({}).\nstdout:\n{}\nstderr:\n{}",
            output.status,
            String::from_utf8_lossy(&output.stdout),
            String::from_utf8_lossy(&output.stderr),
        )));
    }
    Ok(std::str::from_utf8(&output.stdout).unwrap().trim_end_matches(['\r', '\n']).to_string())
}

fn extract_cli(out_dir: &Path, bytes: &[u8]) -> io::Result<PathBuf> {
    let directory = out_dir.join("nativekt-tool");
    fs::create_dir_all(&directory)?;
    let name = if cfg!(windows) {
        "nativekt-cli.exe"
    } else {
        "nativekt-cli"
    };
    let executable = directory.join(name);
    fs::write(&executable, bytes)?;
    make_executable(&executable)?;
    Ok(executable)
}

fn make_executable(path: &Path) -> io::Result<()> {
    #[cfg(unix)]
    {
        use std::os::unix::fs::PermissionsExt;
        fs::set_permissions(path, fs::Permissions::from_mode(0o755))?;
    }
    #[cfg(not(unix))]
    let _ = path;
    Ok(())
}
