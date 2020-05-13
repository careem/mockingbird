# MockingBird

A Koltin multiplatform library that provides an easier way to mock and write unit tests for a multiplatform project

## Setup

In you multiplatform project include

```groovy
implementation "com.careem.mockingbird:mockingbird:1.0.0"
```

## Usage

MockingBird doesn't use any annotation processor or reflection. This means that it is a bit more verbose to
write respect to libraries like `Mockito` or `Mockk`

### Mocks
The first step you need to do is create a mock class for the object you want to mock, 
you need a mock for each dependency type you want to mock

The library provides 2 functions to help you write your mocks.
1. `mock` this function allows you to mock non-Unit methods 
1. `mockUnit` this function allows you to mock Unit methods 

These helpers enable you to map you mock invocations to MockingBird environment.

Your mock class must implements `Mock` in addition to extending the real class or implementing an interface

You can find and example on how to create a mock on the code below

```koltin
interface MyDependency {
    fun method1(str: String)
    fun method2(str: String, value: Int)
    fun method3(value1: Int, value2: Int): Int
}

class MyDependencyMock : MyDependency, Mock {
    object Method {
        const val method1 = "method1"
        const val method2 = "method2"
        const val method3 = "method3"
    }

    object Arg {
        const val str = "str"
        const val value = "value"
        const val value1 = "value1"
        const val value2 = "value2"
    }

    override fun method1(str: String) = mockUnit(
        methodName = Method.method1,
        arguments = mapOf(
            Arg.str to str
        )
    )

    override fun method2(str: String, value: Int) = mockUnit(
        methodName = Method.method2,
        arguments = mapOf(
            Arg.str to str,
            Arg.value to value
        )
    )

    override fun method3(value1: Int, value2: Int): Int = mock(
        methodName = Method.method3,
        arguments = mapOf(
            Arg.value1 to value1,
            Arg.value2 to value2
        )
    )
}    
```

### Mocking

When your mocks are ready you can write your tests and specify the behavior you want when a method 
on you mock is called.

To do that you will use:
1. `every`
2. `everyAnswer`

#### every
Every allows you to specify the value you want to return for a specific invocation

If you want to return 3 when `myDependencyMock.method3(4, 5)` is called your `every` will look like
```kotlin
testMock.every(
    methodName = MyDependencyMock.Method.method3,
    arguments = mapOf(MyDependencyMock.Arg.value1 to 4, MyDependencyMock.Arg.value2 to 5)
) { 3 }
```

If you don't care about the value for the second argument for example you can mock it like

```kotlin
testMock.every(
    methodName = MyDependencyMock.Method.method3,
    arguments = mapOf(MyDependencyMock.Arg.value1 to 4, MyDependencyMock.Arg.value2 to any())
) { 3 }
```

#### everyAnswer

// TODO complete here


### Verify

// TODO complete here

### Slot 

// TODO complete here
