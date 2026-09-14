
fn empty() {
}

fn add_numbers(a: i32, b: i32) -> i32 {
    a.wrapping_add(b)
}

fn pass_string(_a: String) {

}

uniffi::include_scaffolding!("gobley");