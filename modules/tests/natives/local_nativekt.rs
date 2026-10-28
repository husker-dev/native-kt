
use std::env;
use std::io;
use std::path::{Path, PathBuf};

pub fn generate(config_file: impl AsRef<Path>) -> io::Result<PathBuf> {
    let manifest_dir = env::var_os("CARGO_MANIFEST_DIR")
        .ok_or_else(|| io::Error::other("CARGO_MANIFEST_DIR is not set"))?;
    let root = repository_root(Path::new(&manifest_dir))?;

    nativekt::generate_with_bin_dir(
        config_file,
        root.join("modules/cli/build/nativekt-cargo/nativekt-build/bin"),
    )
}

fn repository_root(manifest_dir: &Path) -> io::Result<PathBuf> {
    let manifest_dir = manifest_dir.canonicalize()?;
    manifest_dir
        .ancestors()
        .find(|path| path.join("gradlew").is_file())
        .map(Path::to_path_buf)
        .ok_or_else(|| io::Error::other("Cannot find the Native-Kt repository root"))
}
