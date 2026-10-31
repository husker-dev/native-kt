#[allow(unused)]
pub mod imported {
    nativekt::include_source!("tc_6_rust");
}
use imported::*;

fn stub1(_e: Vec<i64>) {

}

fn stub2(_e: Vec<MyEnum>) {

}