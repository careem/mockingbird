# MockingBird
![version](https://img.shields.io/badge/version-1.6.0-blue) [![Build Status](https://app.bitrise.io/app/0f4e1b30e3e56dfb/status.svg?token=iHecTZF7GpuyTMqiFj618Q&branch=master)](https://app.bitrise.io/build/c0b2c4e103c222bb) 

A Koltin multiplatform library that provides an easier way to mock and write unit tests for a multiplatform project

## Disclaimers
This project may contain experimental code and may not be ready for general use. Support and/or releases may be limited.

## Setup

In you multiplatform project include

```groovy
implementation "com.careem.mockingbird:mockingbird:1.2.0"
```

## Usage

MockingBird doesn't use any annotation processor or reflection. This means that it is a bit more verbose
with respect to libraries like `Mockito` or `Mockk`

### Mocks
The first step you need to do is create a mock class for the object you want to mock, 
you need a mock for each dependency type you want to mock

The library provides 2 functions to help you write your mocks.
1. `mock` this function allows you to mock non-Unit methods 
1. `mockUnit` this function allows you to mock Unit methods 

These helpers enable you to map your mock invocations to MockingBird environment.

Your mock class must implements `Mock` in addition to extending the real class or implementing an interface

You can find an example on how to create a mock on the code below

```kotlin
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
        const val method4 = "method4"
    }

    object Arg {
        const val str = "str"
        const val value = "value"
        const val value1 = "value1"
        const val value2 = "value2"
        const val object1 = "object1"
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

    override fun method4(object1: Object): Int = mock(
        methodName = Method.method4,
        arguments = mapOf(
            Arg.object1 to object1
        )
    )
}    
```

### Spies

When you need a combination of real behavior and mocked behavior you can use `spy` with spy you wrap
wrap a real implementation. Doing so Mocking Bird will record the interactions with the spied object.

To mock a specific invocation you can use the spied object like a normal mock, see sections below for further details.

A Spy sample object is reported here
```kotlin
interface MyDependency {
    fun method1(str: String)
    fun method2(str: String, value: Int)
    fun method3(value1: Int, value2: Int): Int
    fun method4(): Int
}

class MyDependencySpy(private val delegate: MyDependency) : MyDependency, Spy {

    object Method {
        const val method1 = "method1"
        const val method2 = "method2"
        const val method3 = "method3"
        const val method4 = "method4"
    }

    object Arg {
        const val str = "str"
        const val value = "value"
        const val value1 = "value1"
        const val value2 = "value2"
    }

    override fun method1(str: String) = spy(
        methodName = Method.method1,
        arguments = mapOf(
            Arg.str to str
        ),
        delegate = { delegate.method1(str) }
    )

    override fun method2(str: String, value: Int) = spy(
        methodName = Method.method2,
        arguments = mapOf(
            Arg.str to str,
            Arg.value to value
        ),
        delegate = { delegate.method2(str, value) }
    )

    override fun method3(value1: Int, value2: Int): Int = spy(
        methodName = Method.method3,
        arguments = mapOf(
            Arg.value1 to value1,
            Arg.value2 to value2
        ),
        delegate = { delegate.method3(value1, value2) }
    )

    override fun method4(): Int = spy(
        methodName = Method.method4,
        delegate = { delegate.method4() }
    )
}

class MyDependencyImpl : MyDependency {
    private var value: AtomicInt = atomic(0)
    override fun method1(str: String) {

    }

    override fun method2(str: String, value: Int) {

    }

    override fun method3(value1: Int, value2: Int): Int {
        value.value = value1 + value2
        return value.value
    }

    override fun method4(): Int {
        return value.value
    }
}
```


### Mocking

When your mocks are ready you can write your tests and specify the behavior you want when a method 
on your mock is called, and verify the method is called your mock.

To do that you will use:
1. `every`
2. `everyAnswer`
3. `verify`

#### every
`every` allows you to specify the value you want to return for a specific invocation

If you want to return 3 when `myDependencyMock.method3(4, 5)` is called, your `every` will look like
```kotlin
testMock.every(
    methodName = MyDependencyMock.Method.method3,
    arguments = mapOf(MyDependencyMock.Arg.value1 to 4, MyDependencyMock.Arg.value2 to 5)
) { 3 }
```

#### everyAnswers

If you wish to perform specific logic when you mock is called you can use `everyAnswer`, here you can 
specify the behavior you want for your mock. A typical use case is when you want to invoke a callback 
that was passed as parameter to the mocked function.

The code for a callback invocation will look like

```kotlin
myMock.everyAnswers(
    methodName = MyMock.Method.run,
    arguments = mapOf(
        MyMock.Arg.callback to callback,
    )
) {
    val callback = it.arguments[MyMock.Arg.callback] as () -> Unit
    callback.invoke()
}
```

#### Verify

After the invocation of your mock is defined, you need to verify it is invoked to make your unit test
valid. For example if you want to verify `myDependencyMock.method3(4, 5)` is invoked, you should do
something like:
```kotlin
testMock.verify(
    exactly = 1,
    methodName = MyDependencyMock.Method.method3,
    arguments = mapOf(MyDependencyMock.Arg.value1 to 4, MyDependencyMock.Arg.value2 to 5),
    timeoutMillis = 5000L
)
```
Note: `exactly` is how many times you want to verify invocation of your mock is invoked, by default
it will be 1, so no need to set it up if you want to verify exactly 1 time invocation.

Note: when `timeoutMillis` is set with a value greater than 0 the test condition will be evaluated multiple
times up to `timeoutMillis`. If the condition is not satisfied within the given timeout the verify will fail.

### Matching

When your mocks are ready you can write your tests and specify the behavior you want when a method 
on your mock is called, and verify the method is called your mock.
Sometimes besides mocking, we want to verify the equality of argument that passed to the mock's
invocation, sometimes we don't care about the argument value or sometimes we want to strongly verify 
that the invocation is **not** invoked no matter what arguments is passed. In all these cases, we need
matching arguments. 

To do matching, you will use:
1. `any()`
2. `Slot` and `capture`

#### Any

As it looks like, `any()` matcher will give you ability to ignore the compare of argument when mocking
invocation or verify it. For example, if you want to return 3 when `myDependencyMock.method3` is 
called no matter what two arguments is passed in, your `every` will look like:
```kotlin
testMock.every(
    methodName = MyDependencyMock.Method.method3,
    arguments = mapOf(MyDependencyMock.Arg.value1 to any(), MyDependencyMock.Arg.value2 to any())
) { 3 }
```
By doing this, both `myDependencyMock.method3(1,2)` or `myDependencyMock.method3(3,4)` will all 
returns 3. Similar to this `every`, you can easily verify `myDependencyMock.method3` is invoked and
ignore the argument comparing by:
```kotlin
testMock.verify(
    exactly = 1,
    methodName = MyDependencyMock.Method.method3,
    arguments = mapOf(MyDependencyMock.Arg.value1 to any(), MyDependencyMock.Arg.value2 to any())
)
```
A normal use case on verify with `any()` matcher is verify invocation is invoked `exactly = 0` with
`any()` arguments which means it is never invoked completely.

#### Arguments Capturing

Another use case for matching is: for example you want to verify `myDependencyMock.method4(object1)`
is invoked, but the reference of object1 is not mocked or initiated inside the test case, In this case,
an easy way to verify, or say matching arguments, is create a object `Slot` or `CapturedList` and then
`capture` this object when verify the invocation, something like:
```kotlin
val objectSlot = Slot<Object>()
val objectCapturedList = CapturedList<Object>()

testMock.every(
    methodName = MyDependencyMock.Method.method1,
    arguments = mapOf(MyDependencyMock.Arg.str to TEST_STRING)
) {}

// capturing by slot
testMock.verify(
    methodName = MyDependencyMock.Method.method4,
    arguments = mapOf(MyDependencyMock.Arg.object1 to capture(objectSlot))
)
assertEquals(expectedProperty, objectSlot.captured.property)

// capturing by capturedList
testMock.verify(
    methodName = MyDependencyMock.Method.method4,
    arguments = mapOf(MyDependencyMock.Arg.object1 to capture(objectCapturedList))
)
assertTrue { objectCapturedList.assertSize(2) }
assertTrue { capturedList.assertItem(0, expectedProperty) }
assertTrue { capturedList.assertItem(1, expectedProperty) }
```
For capturing slot, a common use case for this capturing is when a new instance is created inside testing
method and you want to compare some properties of the captured object initialized correctly. For capturing
list, a common use case is invocation is invoked multiple times and you want to verify the arguments of
each separately.

## License

    Copyright Careem, an Uber Technologies Inc. company

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
