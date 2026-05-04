---
icon: material/code-braces
---

# NDL file

NDL - is a custom interface definition language, that based on [WebIDL](https://webidl.spec.whatwg.org/) format.

Kotlin and C code generates based on this file when Gradle is synchronized in IDEA.

??? "Example of all possible records"

    ```webidl
    namespace global {
        void simpleFunc();
        boolean myFunc(MyDictionary params, Array<int> flags, MyCallback response);
        [Dealloc] string returnString();
        [Critical] void fastFunc();
    }
    
    enum MyEnum {
        "CASE1",
        "CASE2"
    };
    
    callback MyCallback = boolean (int arg);
    
    dictionary ParentDictionary {
        MyEnum a;
        int b;
    };
    
    dictionary MyDictionary: ParentDictionary {
        int c;
        int d;
    };
    ```

## Types mapping

Many types are currently supported, but there are still limitations.

| NDL      | Kotlin   | C        | Kotlin Array       | C Array            |
|----------|----------|----------|--------------------|--------------------|
| int      | Int      | KInt     | IntArray           | KIntArray          |
| long     | Long     | KLong    | LongArray          | KLongArray         |
| float    | Float    | KFloat   | FloatArray         | KFloatArray        |
| double   | Double   | KDouble  | DoubleArray        | KDoubleArray       |
| char     | Char     | KChar    | CharArray          | KCharArray         |
| boolean  | Boolean  | KBoolean | BooleanArray       | KBooleanArray      |
| byte     | Byte     | KByte    | ByteArray          | KByteArray         |
| short    | Short    | KShort   | ShortArray         | KShortArray        |
| string   | String   | KString  | :x:                |                    |
| void     | Unit     | void     | :x:                |                    |
| Callback | Callback | Callback | Array&lt;Callback> | Array&lt;Callback> |
| Struct   | Struct   | Struct   | Array&lt;Struct>   | Array&lt;Struct>   |

!!! warning
    String type can not be used as an array type

## Functions

Functions are used to call code.
They have the following format:
```webidl
type name(argType arg, ...);
```

To declare a callable function, place it inside `namespace global` block.

Example:
```webidl
namespace global {
    int pow2(int num);
}
```

## Dictionary

A Dictionary is a data container. 

All its fields are copied when passed between Native and Kotlin.

Example:
```webidl
dictionary UserInfo {
    string name;
    string lastName;
    int age;
}

namespace global {
    void sendUserInfo(UserInfo info);
}
```

## Callback

Callback is used to call Kotlin from Native.

Example:
```webidl
callback OnResponse = void (int response);

namespace global {
    void request(OnResponse result);
}
```

## Enums

Enums are some typed constants.

Example:
```webidl
enum MyEnum {
    "CASE1",
    "CASE2"
};
```

## Annotations

There are several annotations that affect the generated code.

- `Dealloc` (read more in [Memory model](memory_model/#dealloc-annotation))
- `Critical` (read more in [Critical](critical))

Annotations can be placed before functions or their arguments.

Example:
```webidl
namespace global {
    [Dealloc] Array<int> returnInts();
    [Critical] void sendFast();
}
```