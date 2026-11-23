#!/usr/bin/env bash
set -e
mkdir -p app/src/main/assets app/src/main/java/com/example/googlefloat
cp settings.gradle build.gradle gradle.properties .
cp app__build.gradle app/build.gradle
cp app__src__main__AndroidManifest.xml app/src/main/AndroidManifest.xml
cp app__src__main__java__com__example__googlefloat__MainActivity.java app/src/main/java/com/example/googlefloat/MainActivity.java
cp app__src__main__java__com__example__googlefloat__FloatingService.java app/src/main/java/com/example/googlefloat/FloatingService.java
cp app__src__main__assets__index.html app/src/main/assets/index.html
cp app__src__main__assets__style.css app/src/main/assets/style.css
cp app__src__main__assets__script.js app/src/main/assets/script.js
