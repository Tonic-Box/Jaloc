# Jaloc
A primitive collections library for Java

### Requirements
- Java 8+

### Structure
There are currently Array and List implementations for all Java primitives as well as for user defined structs.
- Primitive Array implementations can be found in [.../impl/arrays/](./src/main/java/com/tonic/jaloc/impl/arrays)
- Primitive List implementations can be found in [.../impl/lists/](./src/main/java/com/tonic/jaloc/impl/lists)

### Examples
Idk, will get to it soon, look at [Main.java](./src/main/java/com/tonic/jaloc/demo/Main.java) for an example I guess for now. The example there showcases how to work with `structs`.

### Notes of Interest
This library facilitates true native collections though the use of `sun.misc.Unsafe`. 
See:
- [UnsafeAccess](./src/main/java/com/tonic/jaloc/memory/core/UnsafeAccess.java) - A utility class to access the `sun.misc.Unsafe` class.
- [UnsafeMemory](./src/main/java/com/tonic/jaloc/memory/internal/UnsafeMemory.java) - A wrapper class to facilitate calling `sun.misc.Unsafe` methods without trigguring a spam of JVM warnings.
- [SystemAllocator](./src/main/java/com/tonic/jaloc/memory/SystemAllocator.java) - A singleton allocator for aligned native memory.

### License
[MIT](./LICENSE)