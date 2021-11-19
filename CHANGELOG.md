# Change Log
All notable changes to this project will be documented in this file.

---

## master
* KotlinPoet to 1.10.2 and addressed breaking changes
* Kotlin metadata to 0.2.0

## 2.1.0
* Fixed issue that was preventing the plugin from been applied in the `plugins` block
* Converted `build.gradle`s to `build.gradle.kts`

## 2.0.0
* Upgrade kotlin to 1.5.31 in JsPlugin
* MockingBird plugin

## 1.15.0
* Support for iosSimulatorArm64
* HMPP support

## 1.14.0
* Added support to capture objects and lists while running a test with LOCAL_THREAD mode
* **[DEPRECATED]** `Slot()` use `slot()` instead
* **[DEPRECATED]** `CapturedList()` use `capturedList()` instead

## 1.13.0
* **breaking** If there are multiple mocked responses matching a mock invocation, the one that was added last will be used
* Introduced testing mode, before the behavior was always MULTI_THREAD now it is possible to set LOCAL_THREAD mode to avoid argument freeze on mock invocation

## 1.12.0
* Migrating to Gradle Version Catalog and removed Deps
* Bump gradle to 7.2
* Upgrade kotlin to 1.5.31
* Upgrade AtomicFu to 0.16.3
* Upgrade Stately to 1.1.10-a1

## 1.11.0
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
