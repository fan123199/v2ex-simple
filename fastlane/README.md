fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android test

```sh
[bundle exec] fastlane android test
```

Runs all the tests

### android beta

```sh
[bundle exec] fastlane android beta
```

Submit a new Beta Build to Crashlytics Beta

### android deploybundle

```sh
[bundle exec] fastlane android deploybundle
```

(bundle)Deploy a new version to the Google Play

### android deploy

```sh
[bundle exec] fastlane android deploy
```

(apk)Deploy a new version to the Google Play

### android release

```sh
[bundle exec] fastlane android release
```

(bundle)Deploy a new version to the Google Play

### android promote

```sh
[bundle exec] fastlane android promote
```

promote to production

### android build_for_screengrab

```sh
[bundle exec] fastlane android build_for_screengrab
```

Build debug and test APK for screenshots

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
