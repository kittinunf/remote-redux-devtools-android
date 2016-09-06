# Remote-Redux-Devtools-Android

Remote Redux Devtools for Android on Android Studio - Intellij plugin & lib, it is heavily inspired by [remote-redux-devtools](https://github.com/zalmoxisus/remote-redux-devtools)

## TLDR;
![](https://github.com/kittinunf/remote-redux-devtools-android/blob/master/assets/remote-redux-devtools.gif)

## Installation

### There are 2 main components of remote-redux-devtools-android
In order to make the devTools work, one needs to install 2 components (plugin support & library support).
Plugin will act as a Server and Library will act as a client in communication.

### Plugin
You could download zip file contain plugin at [release](https://github.com/kittinunf/remote-redux-devtools-android/releases) page

### Library
Install via jitpack.io

Choose redux-devtools for your desire Redux implementation, as of now we support [Reduks](https://github.com/beyondeye/Reduks). Other libraries will be coming soon.

- [x] [Reduks](https://github.com/beyondeye/Reduks)

### Gradle
```Groovy
repositories {
    jcenter()
    maven { url "https://jitpack.io" }
}
dependencies {
    compile 'com.github.kittinunf.remote-redux-devtools-android:devtools-reduks:1.0.0.alpha5' //for reduks support
}
```
