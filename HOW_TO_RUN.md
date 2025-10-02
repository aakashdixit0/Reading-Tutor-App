# How to Run - Face Distance Blur App

This guide explains how to build, install, and run the Face Distance Blur App on your Android device.

## Prerequisites
- Android Studio (Arctic Fox or newer recommended)
- Java Development Kit (JDK 17 or higher)
- Android device (API Level 24+ / Android 7.0 or higher) or emulator
- USB Debugging enabled on device (if installing via USB)

## Steps to Run

### 1. Clone the Repository
```bash
git clone https://github.com/aakashdixit0/Face-Distance-Blur-App.git
cd Face-Distance-Blur-App


2. Open in Android Studio
Launch Android Studio

Select Open an existing project

Choose the Face-Distance-Blur-App folder

Let Gradle sync and finish setup


3. Build the APK
You can build the project in two ways:

Using Gradle from terminal:

bash
Copy code
./gradlew assembleDebug
Using Android Studio:

Go to Build > Build Bundle(s)/APK(s) > Build APK(s)

Wait for the build to finish

The APK will be located at:

Copy code
app/build/outputs/apk/debug/app-debug.apk


4. Install the APK
On a physical device (with USB Debugging enabled):

bash
Copy code
adb install app/build/outputs/apk/debug/app-debug.apk
Or manually: Transfer the APK to your phone and install it


5. Grant Required Permissions
When you open the app for the first time:

Allow Camera permission

Enable Overlay permission (Settings → Apps → Special Access → Draw over other apps)

Enable Accessibility Service (Settings → Accessibility → Installed Services → Face Distance Blur App)


6. Start Monitoring
Open the app

Tap Start Face Distance Monitoring

If your face comes too close, the screen will automatically blur

Tap Stop Face Distance Monitoring to disable the service

Troubleshooting
Camera not working → Check Camera permission

Blur not appearing → Enable Overlay permission

Service not starting → Enable Accessibility Service

App crashes → Ensure all permissions are granted

Notes
Minimum SDK: 24 (Android 7.0)

Target SDK: 36 (Android 14)

Language: Java

Privacy: All processing is done locally on the device, no data is uploaded