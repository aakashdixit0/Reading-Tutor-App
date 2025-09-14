# Reading Tutor App - Instructions

## How to Run the App on Your Phone

### Option 1: Using Android Studio

1. Open Android Studio  
2. Click on "Open an existing project"  
3. Select the `ReadingTutorApp` folder  
4. Once the project loads, connect your phone via USB  
5. Enable USB debugging on your phone (Settings > Developer options > USB debugging)  
6. In Android Studio, click the "Run" button at the top-right  
7. Select your phone as the target device  
8. The app will be installed and launched on your phone  

### Option 2: Using Build Scripts (without Android Studio)

1. Download this project folder onto your computer  
2. Use one of the scripts to build the APK:  
   - **build_with_gradle.bat**: Use this if you have Gradle installed or an internet connection  
     - Double-click in the folder or run `build_with_gradle.bat` from command prompt  
   - **build_apk.bat**: Use this if you have Android SDK and Java JDK installed  
     - Double-click in the folder or run `build_apk.bat` from command prompt  
3. After the build finishes, the APK will be located at:  
   `app\build\outputs\apk\debug\app-debug.apk`  
4. Copy the APK file to your phone (via email, WhatsApp, or USB)  
5. Tap the APK file on your phone to install  
6. Open the app after installation  

### Option 3: Using Pre-Built APK

1. Open Android Studio  
2. Click on "Open an existing project"  
3. Select the `ReadingTutorApp` folder  
4. Once loaded, go to the menu: **Build > Build Bundle(s) / APK(s) > Build APK(s)**  
5. After the build completes, click "locate" to find the APK  
6. Copy the APK to your phone (via email, WhatsApp, or USB)  
7. Tap the APK file on your phone to install  
8. Open the app after installation  

---

## How to Use the App

1. Open the app  
2. Tap "Upload Image / Pick Page" button  
3. Choose a text-containing image from your gallery  
4. After OCR processing, the extracted text will be displayed  
5. Tap the "Start Reading" button  
6. Read the text aloud  
7. The app will compare your spoken text with the original and give feedback in Hindi  

---

## Troubleshooting

### App Issues
- If the app crashes, make sure all required permissions are granted (microphone, camera, storage)  
- If OCR does not work well, use a clear, well-lit image  
- If speech recognition fails, ensure your phone supports the Hindi language  

### Build Script Issues
- **Problems with `build_with_gradle.bat`:**  
  - If Gradle is not found, the script will try to download it automatically  
  - If you don’t have internet, use `build_apk.bat` instead  
  - If compilation errors occur, open the project in Android Studio to debug  

- **Problems with `build_apk.bat`:**  
  - If you see "Android SDK build tools not found":  
    - Install Android SDK (via Android Studio)  
    - Or use `build_with_gradle.bat`  
  - If you see "Java JDK not found":  
    - Install Java JDK and set the JAVA_HOME environment variable  

- **APK Installation Issues:**  
  - If "App not installed" error appears:  
    - Enable "Install from Unknown Sources" on your phone (Settings > Security > Unknown sources)  
    - If an older version is already installed, uninstall it first and then install the new APK  
