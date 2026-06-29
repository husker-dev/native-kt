use crate::nativekt::{KString};

mod nativekt;


pub fn test_func(
    arg1: Option<KString>,
    arg2: i32
) -> KString {
    let arg1 = arg1.unwrap_or(KString::from_str("null"));

    println!("rust: {}, {}", arg1.as_str(), arg2);

    KString::from_str("Bye! Пока!")
}