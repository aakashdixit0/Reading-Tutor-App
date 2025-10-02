# Face Distance Blur App

An Android application designed to enhance eye health and screen safety by automatically blurring the screen when the user's face is too close to the device.

## Features

- **Real-time Face Detection**: Uses Google ML Kit's face detection to continuously monitor the distance between the user's face and the phone
- **Automatic Blur Overlay**: Applies a blur overlay when the user is too close to the screen
- **Accessibility Service**: Runs in the background to monitor face distance across all apps
- **Privacy-First**: All processing is done locally on the device
- **User-Friendly Interface**: Simple controls to start/stop the monitoring service

## How It Works

1. **Camera Monitoring**: The app uses the front camera to continuously capture images
2. **Face Detection**: Google ML Kit analyzes each frame to detect faces
3. **Distance Calculation**: The app calculates the face size ratio to determine proximity
4. **Blur Trigger**: When the face is detected too close (above threshold), a blur overlay is applied
5. **Auto-Recovery**: The blur is automatically removed when the user moves to a safe distance

## Permissions Required

- **Camera**: Required for face detection
- **Overlay Permission**: Required to display blur overlay on other apps
- **Accessibility Service**: Required to run the monitoring service in the background

## Installation

1. Build the project using Android Studio or Gradle:
   ```bash
   ./gradlew assembleDebug
   ```

2. Install the APK on your Android device

3. Grant the required permissions:
   - Camera permission when prompted
   - Overlay permission in Settings
   - Accessibility service permission in Settings

## Usage

1. Open the app
2. Grant all required permissions when prompted
3. Tap "Start Face Distance Monitoring" to begin
4. The service will run in the background and automatically blur the screen when you're too close
5. Tap "Stop Face Distance Monitoring" to stop the service

## Technical Details

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36 (Android 14)
- **Language**: Java
- **Architecture**: Uses Accessibility Service for background operation
- **Dependencies**:
  - CameraX for camera operations
  - ML Kit Face Detection for face recognition
  - AndroidX components for modern Android development

## Safety Features

- **Local Processing**: All face detection is done on-device for privacy
- **Battery Optimization**: Efficient face detection with minimal battery impact
- **Error Handling**: Graceful handling of camera and permission errors
- **Service Lifecycle**: Proper service management to prevent memory leaks

## Customization

You can adjust the sensitivity by modifying the `SAFE_DISTANCE_THRESHOLD` constant in `BlurAccessibilityService.java`:

```java
private static final float SAFE_DISTANCE_THRESHOLD = 0.6f; // Adjust this value
```

- Lower values (e.g., 0.4f) = More sensitive (blur triggers when closer)
- Higher values (e.g., 0.8f) = Less sensitive (blur triggers when further away)

## Troubleshooting

- **Camera not working**: Ensure camera permission is granted
- **Blur not appearing**: Check overlay permission in Settings
- **Service not starting**: Verify accessibility service is enabled
- **App crashes**: Check that all permissions are properly granted

## Privacy

This app respects your privacy:
- No data is sent to external servers
- All face detection processing happens locally
- No personal information is collected or stored
- Camera access is only used for face detection

## Contributing

Feel free to submit issues and enhancement requests!