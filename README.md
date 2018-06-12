# ToolLib

[![Build Status - Travis](https://travis-ci.org/eilslabs/RoddyToolLib.svg?branch=develop)](https://travis-ci.org/eilslabs/RoddyToolLib)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2FTheRoddyWMS%2FRoddyToolLib.svg?type=shield)](https://app.fossa.io/projects/git%2Bgithub.com%2FTheRoddyWMS%2FRoddyToolLib?ref=badge_shield)

Tool library used in [BatchEuphoria](https://github.com/eilslabs/BatchEuphoria) and [Roddy](https://github.com/eilslabs/Roddy).

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


## License
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bgithub.com%2FTheRoddyWMS%2FRoddyToolLib.svg?type=large)](https://app.fossa.io/projects/git%2Bgithub.com%2FTheRoddyWMS%2FRoddyToolLib?ref=badge_large)