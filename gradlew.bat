@rem Gradle startup script for Windows
@rem
@rem NOTE: gradle/wrapper/gradle-wrapper.jar is not committed in this
@rem source-only drop. Android Studio regenerates it on first sync.

@if "%DEBUG%"=="" @echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_HOME=%DIRNAME%

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

if not exist "%CLASSPATH%" (
    echo gradle-wrapper.jar is missing.>&2
    echo Open this project in Android Studio, or run: gradle wrapper --gradle-version 8.9>&2
    exit /b 1
)

set JAVA_EXE=java.exe
if defined JAVA_HOME set JAVA_EXE=%JAVA_HOME%\bin\java.exe

"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=gradlew" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

endlocal
