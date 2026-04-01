@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Java\jdk-21"

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo Expected JDK 21 at "%JAVA_HOME%".
    echo Update dev-run-client.bat if Java 21 is installed somewhere else.
    exit /b 1
)

call "%~dp0gradlew.bat" runClient
