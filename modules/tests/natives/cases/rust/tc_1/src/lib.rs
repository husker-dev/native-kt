
pub mod nativekt {
    //nativekt::include!();
    #![allow(unused)]
    include!(concat!(env!("OUT_DIR"), "/nativekt.rs"));
}

fn hello_world() {
    println!("Hello, World!");
}