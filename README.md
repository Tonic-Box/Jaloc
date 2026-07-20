# Jaloc
A primitive collections library for Java

### Requirements
- Java 8+

### Structure
There are currently Array and List implementations for all Java primitives as well as for user defined structs.
- **Array2D** implementations can be found in [.../impl/array2ds/](./src/main/java/com/tonic/jaloc/impl/array2ds)
- **Array** implementations can be found in [.../impl/arrays/](./src/main/java/com/tonic/jaloc/impl/arrays)
- **Dequeue** implementations can be found in [.../impl/dequeues/](./src/main/java/com/tonic/jaloc/impl/dequeues)
- **Fixed** Queue implementations can be found in [.../impl/fixedqueues/](./src/main/java/com/tonic/jaloc/impl/fixedqueues)
- **Heap** implementations can be found in [.../impl/heaps/](./src/main/java/com/tonic/jaloc/impl/heaps)
- **List** implementations can be found in [.../impl/lists/](./src/main/java/com/tonic/jaloc/impl/lists)
- **Map** implementations can be found in [.../impl/maps/](./src/main/java/com/tonic/jaloc/impl/maps)
- **Queue** implementations can be found in [.../impl/queues/](./src/main/java/com/tonic/jaloc/impl/queues)
- **Ring** Buffer implementations can be found in [.../impl/ringbuffers/](./src/main/java/com/tonic/jaloc/impl/ringbuffers)
- **Set** implementations can be found in [.../impl/sets/](./src/main/java/com/tonic/jaloc/impl/sets)
- **Stack** implementations can be found in [.../impl/stacks/](./src/main/java/com/tonic/jaloc/impl/stacks)

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