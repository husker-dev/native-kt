use std::sync::Arc;

#[allow(unused)]
pub mod imported {
    nativekt::include_source!("tc_5_rust");
}
use imported::*;

fn stub(_e: Arc<VoidCallback>) {

}