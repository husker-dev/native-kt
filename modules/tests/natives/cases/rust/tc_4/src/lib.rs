use std::sync::Arc;

mod nativekt;

struct EmptyInterface;
struct MyInterface;

fn stub1(_e: &Arc<EmptyInterface>) {

}

fn stub2(_e: &Arc<MyInterface>) {

}

impl MyInterface {
    fn func(&self) {

    }
}