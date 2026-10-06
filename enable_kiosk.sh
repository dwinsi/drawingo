#!/usr/bin/env bash
# ==============================================================================
# enable_kiosk.sh - Elevate Kautuk to Android Device Owner
#
# Sets Kautuk as the Device Owner using Android Device Policy Manager (dpm).
# This grants Kautuk the permission to use startLockTask() in true kiosk mode
# without displaying system pinning confirmation dialogs or allowing home/recents.
# ==============================================================================

set -euo pipefail

PACKAGE_NAME="com.example.kautuk"
RECEIVER_NAME=".device.KautukDeviceAdminReceiver"
COMPONENT="${PACKAGE_NAME}/${RECEIVER_NAME}"

echo "=========================================================="
echo "          Kautuk Toddler Kiosk Setup Tool                "
echo "=========================================================="

# Check if ADB is available
if ! command -v adb &> /dev/null; then
    # Check default Android SDK platform-tools path on macOS
    if [ -x "$HOME/Library/Android/sdk/platform-tools/adb" ]; then
        export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"
    else
        echo "❌ Error: 'adb' command not found."
        echo "Please install Android Platform Tools or add them to your PATH."
        exit 1
    fi
fi

echo "🔍 Checking for connected Android devices..."
adb devices

echo "⏳ Waiting for device to be ready..."
adb wait-for-device

echo "📦 Verifying package installation: ${PACKAGE_NAME}..."
if ! adb shell pm list packages | grep -q "${PACKAGE_NAME}"; then
    echo "⚠️  ${PACKAGE_NAME} is not yet installed on the device."
    echo "   Building and installing debug APK first..."
    ./gradlew installDebug || {
        echo "❌ Failed to install ${PACKAGE_NAME}. Please run './gradlew assembleDebug' and install manually."
        exit 1
    }
fi

echo "🚀 Elevating Kautuk to Device Owner via DPM..."
echo "Executing: adb shell dpm set-device-owner ${COMPONENT}"
echo "----------------------------------------------------------"

set +e
RESULT=$(adb shell dpm set-device-owner "${COMPONENT}" 2>&1)
EXIT_CODE=$?
set -e

echo "${RESULT}"

if [ $EXIT_CODE -eq 0 ] && [[ "${RESULT}" != *"Error"* && "${RESULT}" != *"IllegalStateException"* ]]; then
    echo "----------------------------------------------------------"
    echo "✅ Success! Kautuk is now configured as the Device Owner."
    echo "   startLockTask() will now lock into dedicated Kiosk Mode without popups."
    echo "   To exit kiosk mode in app: Press & hold with 4 fingers for 3 seconds."
else
    echo "----------------------------------------------------------"
    if [[ "${RESULT}" == *"already has a device owner"* ]]; then
        echo "ℹ️  Device already has an owner. If this is Kautuk, it is already active."
    elif [[ "${RESULT}" == *"Not allowed to set the device owner because there are already some accounts"* ]]; then
        echo "⚠️  Notice: Android prevents setting a Device Owner if user accounts (Google, etc.)"
        echo "   already exist on the device. To test on a personal device:"
        echo "   1) Use an Android Emulator or dedicated kiosk test device,"
        echo "   OR"
        echo "   2) Temporarily remove configured accounts in Android Settings > Passwords & Accounts,"
        echo "   OR"
        echo "   3) Use standard screen pinning (the app will still pin and protect against taps)."
    else
        echo "⚠️  Command completed with warnings/errors. See output above."
    fi
fi
