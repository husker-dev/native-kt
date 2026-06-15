use crate::nativekt::KString;

mod nativekt;


pub fn test_func(
    k: Option<KString>,
    arg2: i32
) -> KString {
    let str = k.unwrap_or(KString::from("null"));

    println!("rust: {}, {}", str.as_str(), arg2);

    KString::from("Bye! Пока!")
}