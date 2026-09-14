mod nativekt;


fn call_critical_jvmci() {
}

fn call_critical_jvmci_add(a: i32, b: i32) -> i32 {
    a.wrapping_add(b)
}

fn call_critical_jvmci_string(_a: &String) {
}