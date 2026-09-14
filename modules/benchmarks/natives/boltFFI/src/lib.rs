use boltffi::export;

#[export]
fn empty() {
}

#[export]
fn add_numbers(a: i32, b: i32) -> i32 {
    a.wrapping_add(b)
}

#[export]
fn pass_string(_a: String) {}