use std::sync::Arc;

#[allow(unused)]
pub mod imported {
    nativekt::include_source!();
}
use imported::*;

fn stub(_e: Arc<VoidCallback>) {

}