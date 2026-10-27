pub mod nativekt {
    //nativekt::include!();
    #![allow(unused)]
    include!(concat!(env!("OUT_DIR"), "/nativekt.rs"));
}
use nativekt::*;

fn stub1(_e: Vec<i64>) {

}

fn stub2(_e: Vec<MyEnum>) {

}