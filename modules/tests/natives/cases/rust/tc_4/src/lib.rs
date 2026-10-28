use std::sync::Arc;


#[allow(unused)]
pub mod imported {
    nativekt::include_source!();
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