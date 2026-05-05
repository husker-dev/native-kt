---
icon: simple/c
---

# C header

The C header contains an API for interacting with C/C++.

It contains a small runtime with type definitions, as well as structures, enums, and so on.

## Types

Types mirror Kotlin's one but are prefixed with K (e.g., KInt, KBoolean)

```C
KInt a = 213;
KBoolean b = false;
```

### String

A string is represented as a structure that holds a pointer to its byte data and a length field:
```c
typedef struct KString {
    const char* data;
    KInt length;
} KString;
```

It can be created using the `KString_new` function, 
which takes a pointer to bytes and length.
```c
KString myString = KString_new("hello", 5);
```

To free a string, you need to call `free()` on `data`:
```c
free(myString.data);
```


### Arrays

An array is represented as a structure containing a pointer to its elements and a size:

```c
typedef struct KArray {
    const void* elements;
    KInt size;
} KArray;
```

Arrays of primitives also have their own separate types.

There are two functions for creating arrays: 

- `_new` takes elements and a size;
- `_of` uses variadic arguments.

```c
// _new
KInt* elements = (KInt*) malloc(3 * sizeof(KInt));
elements[0] = 1;
elements[1] = 2;
elements[2] = 3;
KIntArray arr1 = KIntArray_new(elements, 3);

// _of
KIntArray arr2 = KIntArray_of(1, 2, 3);
```

!!! warning
    Strings cannot be used in arrays.

To free an array, you need to call `free()` on `elements`:
```c
free(arr1.elements);
```

## Functions

The developer is required to implement all functions listed at the very end of the file in the `Functions` block.
Without implementation, the program will not compile.

## Dictionary

Dictionaries are represented as `struct` types in C.

When sent from Kotlin to Native, these structs are copied.

```webidl title="NDL"
dictionary MyDictionary {
    int a;
};
```
```c title="C"
struct MyDictionary {
	KInt a;
};
```

For convenient allocation, each dictionary type provides a `_new` function 
that calls `malloc` and initializes the fields.

```c
MyDictionary* func() {
    return MyDictionary_new(123);
}
```

## Enum

Enums look like regular enums in C, but each element has a prefix with the name of the enum.

```webidl title="NDL"
enum MyEnum {
    "CASE1",
    "CASE2"
};
```
```c title="C"
typedef enum {
	MyEnum_CASE1,
	MyEnum_CASE2
} MyEnum;
```

## Callback

Callbacks have the following form in C:
```c
struct MyCallback {
    void *m;
    void (*invoke)(MyCallback* _ /*, args... */);
    void (*free)(MyCallback* _);
};
```

!!! note "Field `m` is metadata. Do not modify it."

### Invoking

To call a callback, there's the `invoke` function. 

To invoke a callback, call its `invoke` function, passing the required arguments. 
It may return a value depending on the callback signature.

```webidl title="NDL"
callback MyCallback = int (char arg);

namespace global {
    void request(MyCallback response);
}
```
```c title="C"
void request(MyCallback* response) {
    KInt result = response->invoke(response, 'b');
    // ...
}
```

### Lifetime

Callbacks are references to real Kotlin objects.
Therefore, a developer responsible for managing their lifetime explicitly.

To release this reference, call the `->free` function:
```c
response->free(response);
```

