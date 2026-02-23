#!/bin/bash
set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
IOS_APP="$PROJECT_ROOT/iosApp"

echo "==> Cleaning shared module..."
"$PROJECT_ROOT/gradlew" :shared:clean

echo "==> Building Kotlin/Native framework..."
"$PROJECT_ROOT/gradlew" :shared:podPublishDebugXCFramework

echo "==> Re-installing CocoaPods..."
cd "$IOS_APP"
pod install

echo ""
echo "Done. Now open Xcode, do Product → Clean Build Folder (⇧⌘K), then Build (⌘B)."
