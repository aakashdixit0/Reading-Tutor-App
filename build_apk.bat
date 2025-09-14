@echo off
echo Building APK for Reading Tutor App

REM Check if Android SDK exists in common locations
IF EXIST "%LOCALAPPDATA%\Android\Sdk" (
    set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk
) ELSE IF EXIST "C:\Android" (
    set ANDROID_HOME=C:\Android
) ELSE (
    echo Android SDK not found. Please install Android Studio first.
    exit /b 1
)

REM Check for Java installation
IF "%JAVA_HOME%"=="" (
    echo JAVA_HOME is not set. Checking common locations...
    IF EXIST "C:\Program Files\Java\jdk-11" (
        set JAVA_HOME=C:\Program Files\Java\jdk-11
    ) ELSE IF EXIST "C:\Program Files\Java\jdk1.8.0" (
        set JAVA_HOME=C:\Program Files\Java\jdk1.8.0
    ) ELSE (
        echo Java JDK not found. Please install JDK 8 or 11.
        exit /b 1
    )
)

echo Using Android SDK at %ANDROID_HOME%
echo Using Java at %JAVA_HOME%

REM Check for build tools
IF NOT EXIST "%ANDROID_HOME%\build-tools" (
    echo Android SDK build tools not found!
    echo Please install build tools using Android SDK Manager.
    echo Trying to use Gradle instead...
    
    IF EXIST "build_with_gradle.bat" (
        echo Running build_with_gradle.bat instead...
        call build_with_gradle.bat
        exit /b %ERRORLEVEL%
    ) ELSE (
        echo build_with_gradle.bat not found either.
        echo Please install Android SDK build tools or use Android Studio.
        exit /b 1
    )
)

echo Checking for build tools...
IF EXIST "%ANDROID_HOME%\build-tools" (
    echo Checking for latest build tools version...
    for /f "delims=" %%a in ('dir /b /ad "%ANDROID_HOME%\build-tools" ^| sort /r') do (
        set BUILD_TOOLS_VERSION=%%a
        goto :found_build_tools
    )
    
    :found_build_tools
    IF "%BUILD_TOOLS_VERSION%"=="" (
        echo No build tools versions found in %ANDROID_HOME%\build-tools
        echo Trying to use Gradle instead...
        
        IF EXIST "build_with_gradle.bat" (
            echo Running build_with_gradle.bat instead...
            call build_with_gradle.bat
            exit /b %ERRORLEVEL%
        ) ELSE (
            echo build_with_gradle.bat not found either.
            echo Please install Android SDK build tools or use Android Studio.
            exit /b 1
        )
    ) ELSE (
        echo Using build tools version %BUILD_TOOLS_VERSION%
    )
) ELSE (
    echo Android SDK build tools directory not found!
    echo Trying to use Gradle instead...
    
    IF EXIST "build_with_gradle.bat" (
        echo Running build_with_gradle.bat instead...
        call build_with_gradle.bat
        exit /b %ERRORLEVEL%
    ) ELSE (
        echo build_with_gradle.bat not found either.
        echo Please install Android SDK build tools or use Android Studio.
        exit /b 1
    )
)

echo Checking for latest platform version...
for /f "delims=" %%a in ('dir /b /ad "%ANDROID_HOME%\platforms" ^| sort /r') do (
    set PLATFORM_VERSION=%%a
    goto :found_platform
)
:found_platform
echo Using platform %PLATFORM_VERSION%

echo Cleaning project...
if exist "app\build" rmdir /s /q "app\build"

echo Creating output directories...
mkdir "app\build\outputs\apk\debug"

echo Compiling resources...
IF NOT EXIST "%ANDROID_HOME%\build-tools\%BUILD_TOOLS_VERSION%\aapt2.exe" (
    echo AAPT2 not found! Please check your Android SDK installation.
    exit /b 1
)

"%ANDROID_HOME%\build-tools\%BUILD_TOOLS_VERSION%\aapt2.exe" compile --dir "app\src\main\res" -o "app\build\res.zip"
IF %ERRORLEVEL% NEQ 0 (
    echo Failed to compile resources.
    exit /b 1
)

echo Linking resources...
IF NOT EXIST "%ANDROID_HOME%\platforms\%PLATFORM_VERSION%\android.jar" (
    echo Android platform JAR not found! Please check your Android SDK installation.
    exit /b 1
)

"%ANDROID_HOME%\build-tools\%BUILD_TOOLS_VERSION%\aapt2.exe" link -I "%ANDROID_HOME%\platforms\%PLATFORM_VERSION%\android.jar" --manifest "app\src\main\AndroidManifest.xml" -o "app\build\outputs\apk\debug\app-debug-unsigned.apk" "app\build\res.zip"
IF %ERRORLEVEL% NEQ 0 (
    echo Failed to link resources.
    exit /b 1
)

echo Signing APK...
IF NOT EXIST "%ANDROID_HOME%\build-tools\%BUILD_TOOLS_VERSION%\apksigner.bat" (
    echo APK Signer not found! Please check your Android SDK installation.
    exit /b 1
)

IF NOT EXIST debug.keystore (
    echo Creating debug keystore...
    "%JAVA_HOME%\bin\keytool" -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
)

"%ANDROID_HOME%\build-tools\%BUILD_TOOLS_VERSION%\apksigner.bat" sign --ks debug.keystore --ks-pass pass:android --key-pass pass:android --out "app\build\outputs\apk\debug\app-debug.apk" "app\build\outputs\apk\debug\app-debug-unsigned.apk"
IF %ERRORLEVEL% NEQ 0 (
    echo Failed to sign APK.
    exit /b 1
)

echo Build successful! APK is located at:
echo app\build\outputs\apk\debug\app-debug.apk
echo.
echo Copy this APK to your phone to install the app.