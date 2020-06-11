# Change Log
All notable changes to this project will be documented in this file.

---

## master
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

