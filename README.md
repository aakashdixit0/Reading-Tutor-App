# Reading Tutor App

This Android app helps children practice and improve their reading skills. It uses OCR (Optical Character Recognition) to extract text from images, listens to the child’s spoken reading, and provides instant feedback in Hindi.

## Key Features

- **OCR**: Extracts text from images using Google ML Kit  
- **Speech Recognition**: Recognizes the child’s spoken reading  
- **Text Comparison**: Compares original text with spoken text to detect mistakes accurately  
- **Hindi Feedback**: Provides instant feedback in Hindi for mispronounced words  
- **Continuous Reading**: Automatically resumes listening after incorrect words  
- **Motivational Messages**: Encourages children with supportive prompts  

## Technical Details

- Optimized for Android 13 (API 33) but works on Android 5.0+  
- Uses Google ML Kit Text Recognition (for OCR)  
- Uses Android SpeechRecognizer and TextToSpeech APIs  
- Advanced text comparison algorithm with Hindi language support  
- Auto-resume listening capability  
- Motivational feedback system  

## Troubleshooting

### Permissions
If the app is not working correctly, make sure you have granted:  
- Microphone (required)  
- Camera (required)  
- Internet (for OCR)  
- Storage (for images)  

### OCR Issues
- Ensure the image is clear and readable  
- Take the picture in good lighting  
- The image should contain only text (avoid graphics/shapes)  

### Speech Recognition Issues
- Use the app in a quiet environment  
- Speak clearly and slowly  
- Ensure your device supports Hindi language  

## App Flow

1. User uploads an image  
2. OCR extracts text from the image  
3. User taps the “Start Reading” button  
4. App displays the original text  
5. User reads the text aloud  
6. App compares spoken text with the original  
7. App gives instant Hindi feedback on mistakes  
8. App automatically resumes listening after feedback  
9. Process continues with motivational prompts  

## Installation

### Option 1: Install via APK
1. Download `app-debug.apk` from this repository  
2. Open the APK file on your phone to install  
3. Allow installation from unknown sources if prompted  

### Option 2: Build the Project
1. Clone this repository  
2. Run `build_with_gradle.bat` or `build_apk.bat`  
3. Copy the generated `app/build/outputs/apk/debug/app-debug.apk` to your phone  
4. Install by opening the APK file  

## Requirements

- Android 5.0 (API 21) or higher  
- Device with camera and microphone  
- Hindi language support  
- Internet connection (for OCR)