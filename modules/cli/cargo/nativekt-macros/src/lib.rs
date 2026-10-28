use proc_macro::TokenStream;
use quote::quote;

/// Includes `OUT_DIR/nativekt.rs` in the consuming crate or module.
/// With a string argument, includes `OUT_DIR/<stem>.nativekt.rs` instead.
///
/// ```ignore
/// mod nativekt {
///     nativekt::include_source!();
/// }
/// ```
#[proc_macro]
pub fn include_source(_udl_stem: TokenStream) -> TokenStream {
    quote! {
        include!(concat!(env!("OUT_DIR"), "/nativekt.rs"));
    }.into()
}