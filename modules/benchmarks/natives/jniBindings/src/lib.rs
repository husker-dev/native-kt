mod nativekt;

fn call_jni() {
}

fn call_jni_add(a: i32, b: i32) -> i32 {
    a.wrapping_add(b)
}

fn call_jni_string(_a: String) {
}

fn call_critical_jni_string(_a: &String) {
}