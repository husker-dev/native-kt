use proc_macro::TokenStream;
use quote::quote;
use syn::LitStr;

/// Includes `OUT_DIR/nativekt.rs` in the consuming crate or module.
/// With a string argument, includes `OUT_DIR/<stem>.nativekt.rs` instead.
///
/// ```ignore
/// mod nativekt {
///     nativekt::include_source!("mylib");
/// }
/// ```
#[proc_macro]
pub fn include_source(udl_stem: TokenStream) -> TokenStream {
    let udl_stem = syn::parse_macro_input!(udl_stem as LitStr);
    quote! {
        include!(concat!(env!("OUT_DIR"), "/", #udl_stem, ".nativekt.rs"));
    }.into()
}