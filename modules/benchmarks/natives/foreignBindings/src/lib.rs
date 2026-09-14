mod nativekt;

fn call_foreign() {
}

fn call_foreign_add(a: i32, b: i32) -> i32 {
    a.wrapping_add(b)
}

fn call_foreign_string(_a: &String) {
}

fn call_critical_foreign() {
}

fn call_critical_foreign_add(a: i32, b: i32) -> i32 {
    a.wrapping_add(b)
}

fn call_critical_foreign_string(_a: &String) {
}