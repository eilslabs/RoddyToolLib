# ToolLib

[![Build Status - Travis](https://travis-ci.org/TheRoddyWMS/RoddyToolLib.svg?branch=master)](https://travis-ci.org/TheRoddyWMS/RoddyToolLib)

Tool library used in [BatchEuphoria](https://github.com/TheRoddyWMS/BatchEuphoria) and [Roddy](https://github.com/TheRoddyWMS/Roddy).

## Build

Building is as simple as

```bash
./gradlew build
```

If you are behind a firewall and need to access the internet via a proxy, you can configure the proxy in `$HOME/.gradle/gradle.properties`:

```groovy
systemProp.http.proxyHost=HTTP_proxy
systemProp.http.proxyPort=HTTP_proxy_port
systemProp.https.proxyHost=HTTPS_proxy
systemProp.https.proxyPort=HTTPS_proxy_port
```

where you substitute the correct proxies and ports required for your environment.

## Changelog

* 0.0.8

  - Refactored `AsyncExecutionResult` and `ExecutionResult` to improve the stdout and stderr handling. This also affects `ExecutionResult.resultLines` field/accessors.
  - Improved support for additional output stream in `LocalExecutionHelper`
  - Changes are required for Roddy 3.6.1 improvements related to better error reporting and handling.  

* 0.0.7

  - Added `AsyncExecutionResult` to handle asynchronous execution of command executions.