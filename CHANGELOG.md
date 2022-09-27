# Change Log

All notable changes to this project will be documented in this file.

---

## master

## 2.8.0
* Bumping dependencies and migration to Kotlin 1.7.10
* Support mock generation for interfaces with generics

## 2.7.0
* MockingBird plugin support ksp codegen out of the box of google ksp plugin is applied
* Introduced Ksp code generation using the @Mock annotation
* Fix for mock generation to set the right visibility

## 2.6.0

* **breaking** Fix issue https://github.com/careem/mockingbird/issues/116, if you are generate Mocks manually you need to
  override `public val uuid: String` like `override val uuid: String by uuid()` if you are using the plugin no changes are
  required on your side
* * Fix issue: https://github.com/careem/mockingbird/issues/109, Stale mocks are not always removed.

## 2.5.0

* Reduce dependencies of `generateMocks` task and make sure mocks are always generated prior to building tests

## 2.4.0

* Kotlin to 1.6.21
* Jacoco to 0.8.8
* AtomicFu to 0.17.2
* kotlinPoet to 1.11.0
* kotlinxMetadata to 0.4.2
* mockk to 1.12.3

## 2.3.0

* Fix issues #86 and #113

## 2.2.0

* Kotlin 1.5+ required to use the code generation plugin
* XCode 12.5.+ now required
* yarn.lock has been added to VCS
* Gradle to 7.3.3
* Kotlin to 1.6.10
* AtomicFu to 0.17.0
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
* Introduced testing mode, before the behavior was always MULTI_THREAD now it is possible to set LOCAL_THREAD mode to avoid
  argument freeze on mock invocation

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
