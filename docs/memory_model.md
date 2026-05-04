---
icon: lucide/memory-stick
---

# Memory control

It is important to understand the lifetime of different objects to prevent memory leaks.

## Function arguments

`Array` and `string` arguments are deallocated when the function returns, 
so they must not be stored beyond the function's lifetime.

This does not apply to arguments of type `callback`.

## String and Array

String and Arrays are passed as a struct by value. 

To free them up, you should clean their inner pointers:

- String: `free(myString.data);`
- Array: `free(myArray.elements);`

Example:
```c
void pass(KString stringArg) {

    KString someString = KString_new("test", 4);
    KIntArray someArray = KIntArray_of(1, 2, 3);

    free(someString.data);
    free(someArray.elements);
    // DO NOT FREE FUNCTION ARGUMENTS !!!
}
```

!!! warning
    It is important to remember that this does not free each array's element individually.

## Callbacks

To free them up, call `callback->free` function.

Example:
```c
void pass(MyCallback* callback) {
    callback->free(callback);
}

```

## `[Dealloc]` annotation

Tells the generator that this element should be cleared after the function call.

Can be used when returning a string, array or callback from function whose ownership is no longer
in the native world.

Example:
```webidl
namespace global {
    [Dealloc] Array<int> returnInts();
}
```
```c
KIntArray returnInts() {
    // Here we allocate int array and lost it after returning.
    // As soon as we annotate function with [Dealloc], generator will free it up
    return KIntArray_of(1, 2, 3);
}
```