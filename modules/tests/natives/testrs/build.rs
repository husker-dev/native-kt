#[path = "../local_nativekt.rs"]
mod local_nativekt;

fn main() {
    local_nativekt::generate("nativekt.toml")
        .expect("Native-Kt generation failed");
}
