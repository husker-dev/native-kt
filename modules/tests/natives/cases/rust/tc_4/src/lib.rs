use std::sync::Arc;

pub mod nativekt {
    //nativekt::include!();
    #![allow(unused)]
    include!(concat!(env!("OUT_DIR"), "/nativekt.rs"));
}

struct EmptyInterface;
struct MyInterface;

fn stub1(_e: Arc<EmptyInterface>) {

}

fn stub2(_e: Arc<MyInterface>) {

}

impl MyInterface {
    fn func(&self) {

    }
}