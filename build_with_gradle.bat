@echo off
echo Building Reading Tutor App with Gradle

REM Check if Android Studio is installed
IF EXIST "%LOCALAPPDATA%\Android\Sdk" (
    set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
) ELSE IF EXIST "C:\Android" (
    set ANDROID_HOME=C:\Android
) ELSE (
    echo Android SDK not found. Please install Android Studio first.
    exit /b 1
)

echo Using Android SDK at %ANDROID_HOME%

REM Check if gradle is installed
where gradle >nul 2>nul
IF %ERRORLEVEL% NEQ 0 (
    echo Gradle not found. Please install Gradle first.
    echo You can download it from https://gradle.org/install/
    exit /b 1
)

REM Check if gradlew exists, if not create it
IF NOT EXIST gradlew.bat (
    echo Creating gradlew.bat...
    echo @echo off > gradlew.bat
    echo call gradle %%* >> gradlew.bat
)

REM Run gradle build
echo Building APK...
call gradlew.bat clean
call gradlew.bat assembleDebug

IF EXIST app\build\outputs\apk\debug\app-debug.apk (
    echo Build successful! APK is located at:
    echo app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Copy this APK to your phone to install the app.
) ELSE (
    echo Build failed. Please check the error messages above.
    echo If you continue to have issues, please open this project in Android Studio instead.
)