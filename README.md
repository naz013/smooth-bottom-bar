# BottomNavigationView
Simple bottom navigation view library for Android (Kotlin, AndroidX, Spring Animation)

[![](https://jitpack.io/v/naz013/smooth-bottom-bar.svg)](https://jitpack.io/#naz013/smooth-bottom-bar)

Design by Alejandro: [Dribble](https://dribbble.com/shots/6251784-Navigation-Menu-Animation)

Screenshot

<img src="https://github.com/naz013/smooth-bottom-bar/raw/master/res/screenshot.png" width="400" alt="Screenshot">

Sample APP
--------
[Download](https://github.com/naz013/smooth-bottom-bar/raw/master/app/release/app-release.apk)

[Google Play](https://play.google.com/store/apps/details?id=com.github.naz013.example)

Download
--------
Download latest version with Gradle:
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.naz013:smooth-bottom-bar:1.0.1'
    implementation "androidx.dynamicanimation:dynamicanimation:1.0.0"
}
```

Usage
-----
Default (Material colors picker):
```xml
<com.github.naz013.smoothbottombar.SmoothBottomBar
        android:id="@+id/bottomBar"
        android:layout_width="match_parent"
        android:layout_height="72dp"
        android:layout_marginTop="16dp"
        app:bar_selectorColor="#80ffffff"
        app:bar_textColor="#ffffff"
        app:bar_background="#d67388" />
```

License
-------

    Copyright 2019 Nazar Suhovich

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
