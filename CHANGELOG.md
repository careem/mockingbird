# Change Log
All notable changes to this project will be documented in this file.

---

## master
* Enabled warnings as errors
* Enabled `explictApi()` mode
* Upgrade kotlin to 1.5.21
* Upgrade AtomicFu 0.16.2

## 1.10.0
* Upgrade Kotlin 1.5.10

## 1.9.0
* Upgrade Kotlin 1.5.0
* Support for IR compiler
* Upgrade Stately 1.1.7-a1
* Upgrade AtomicFu 0.16.1

## 1.8.0
* Add typed argument getter to Invocation
* Upgrade Kotlin 1.4.32
* Upgrade Stately 1.1.6-a1
* Upgrade AtomicFu 0.15.2

## 1.7.0
* Upgrade Kotlin 1.4.21
* Included fix done in version (1.4.1)

## 1.6.0 ( 1.4.1 fix not present )

* Upgrade Kotlin 1.4.20

## 1.5.0 ( 1.4.1 fix not present )

* Upgrade Kotlin 1.4.0

## 1.4.1

* Fixed issue where mocking a dependency was freezing the mock as well

## 1.4.0
* Added support for timeout during `verify`
* Added more info when not mocked response
* Added `CapturedList` class to support capture mutable arguments in `verify` function

## 1.3.0
* Supporting Javascript for nodeJs without browser

## 1.2.0
* Added `spy` function to support spy operation
* Enforced type safety for all function, now you will get type error before runtime if you try to `verify` a non Mock

## 1.1.0
* Renaming `threadedTest` to `runOnWorker`
* Adding support for multithreaded tests (experimental, only mock calls are supported from different thread)
* Fixed Expected , actual reversed
* Fixed issue where `threadedTest` was running the body twice

## 1.0.0
* `mockUnit` function
* `mock` function
* `every` function
* `everyAnswer` function
* `slot` feature
* `any` matcher
