# `nativekt` Cargo dependency

The `nativekt` crate re-exports optional functionality from two crates:

- `nativekt-build`: build-script helpers and bundled CLI executables.
- `nativekt-macros`: the `include!` procedural macro.

Features:

- `build`: exposes `generate` and `generate_with_bin_dir`.
- `bundled`: enables `build` and embeds the host CLI.
- `macro`: exposes `nativekt::include!`.

No features are enabled by default.

For a local CLI directory, use `features = ["build"]`
in `[build-dependencies]` and call `nativekt::generate_with_bin_dir`.
For macros, enable `features = ["macro"]` in `[dependencies]`.
For the bundled CLI, enable `features = ["bundled"]` in `[build-dependencies]`.

The build helper currently generates `OUT_DIR/nativekt.rs`, which can be
included with:

```rust,ignore
nativekt::include_source!()
```