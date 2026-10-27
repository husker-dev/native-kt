pub mod nativekt {
    //nativekt::include!();
    #![allow(unused)]
    include!(concat!(env!("OUT_DIR"), "/nativekt.rs"));
}
use nativekt::*;

fn stub(_e: MyEnum) {

}