use std::sync::Arc;


#[allow(unused)]
pub mod imported {
    nativekt::include_source!("tc_4_rust");
}

pub struct EmptyInterface;
pub struct MyInterface;

fn stub1(_e: Arc<EmptyInterface>) {

}

fn stub2(_e: Arc<MyInterface>) {

}

impl MyInterface {
    fn func(&self) {

    }
}