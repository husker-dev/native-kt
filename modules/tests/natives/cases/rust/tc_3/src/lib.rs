pub mod nativekt {
    //nativekt::include!();
    #![allow(unused)]
    include!(concat!(env!("OUT_DIR"), "/nativekt.rs"));
}
use nativekt::*;

fn stub1(_e: EmptyDictionary1) {

}

fn stub2(_e: MyDictionary) {

}